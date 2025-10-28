package com.example.backend.service;

import com.example.backend.dto.LoginResponseDTO;
import com.example.backend.dto.LoginResultWrapper;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.RefreshTokenExpiredException;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
//@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserWithdrawalService userWithdrawalService;
    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    // ⭐️ 더미 유저의 고유 이메일 상수를 정의합니다.
    public static final String WITHDRAWN_USER_EMAIL = "system-withdrawn@dummy.com";

    private final AuthService authService;
    private final PostService postService;
    private final CommentService commentService;
    private final UserWithdrawalService withdrawalService;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // ⭐️ 초기화 메서드 추가
    @Transactional
    public void initializeDummyUser() {

        // 고유 이메일로 더미 유저 존재 여부 확인
        if (userRepository.findByEmail(WITHDRAWN_USER_EMAIL).isEmpty()) {

            User dummyUser = User.builder()
                    .email(WITHDRAWN_USER_EMAIL)
                    .name("탈퇴한 회원")
                    .snsProvider("system")
                    .snsId("-1")
                    .role(Role.ROLE_WITHDRAWN)
                    .build();

            // 2. save()를 통해 DB가 안전하게 INSERT 및 ID 자동 할당
            User savedUser = userRepository.save(dummyUser);

            // 3. (선택 사항) 초기화 시 할당된 ID를 확인하여 로그 출력
            System.out.println("✅ 시스템 더미 탈퇴 회원 생성 완료. 할당된 ID: " + savedUser.getId());
        }
    }

    /**
     * 구글 로그인 처리를 담당합니다.
     * 이메일과 이름을 받아 사용자를 생성하거나 조회한 후, JWT를 발급합니다.
     *
     * @param email Google로부터 받은 사용자의 이메일
     * @param name  Google로부터 받은 사용자의 이름
     * @param snsId Google로부터 받은 사용자의 고유 ID (sns_id로 저장)
     * @return 액세스 토큰, 리프레시 토큰 및 사용자 정보가 담긴 DTO
     */
    @Transactional
    public LoginResultWrapper processGoogleLogin(String email, String name, String snsId) {

        System.out.println("UserService - processGoogleLogin 진입");
        
        // 1. 이메일로 기존 사용자가 있는지 조회합니다. 없으면 새로 생성합니다.
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // User newUser = User.builder()...build(); // 기존 코드 삭제

                    // 🔥 정적 팩토리 메서드 또는 생성자를 사용하여 JPA가 Auditing 필드를 주입할 기회를 줍니다.
                    User newUser = User.createSocialUser(email, name, "google", snsId, Role.ROLE_USER);

                    return userRepository.save(newUser);
                });

        // 2. JWT 토큰을 생성합니다.
        // JWT의 주체(subject)는 보안을 위해 사용자 ID를 사용합니다.
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // 3. 리프레시 토큰을 DB에 저장합니다.
        // 이는 토큰 재발급 시 사용자의 유효성을 확인하는 데 필요합니다.
        // ⭐️ 개선: setter 대신 엔티티 비즈니스 메서드 사용
        user.updateRefreshToken(
                refreshToken,
                LocalDateTime.now().plusSeconds(refreshTokenValidityInSeconds)
        );
        userRepository.save(user);

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO(accessToken, user.getId(), user.getEmail(), user.getName(), user.getRole().name());

        // 4. 토큰과 프로필 정보를 DTO에 담아서 반환합니다.
        // 이 DTO는 AuthController에서 사용됩니다.

        // 쿠키를 통해 컨트롤러에서 브라우저에 전달
        return new LoginResultWrapper(loginResponseDTO, refreshToken);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        System.out.println("UserService - loadUserByUsername 메소드 진입");
        
        // userId는 Long 타입이므로 String으로 받은 username을 Long으로 변환
        Long userId = Long.parseLong(username);

        System.out.println("userId: " + userId);;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        System.out.println("user: " + user);;

        if (user.getDeletedAt() != null) {
            // 이미 탈퇴된 계정이라면, 토큰이 유효해도 사용자를 찾을 수 없다고 처리
            throw new UsernameNotFoundException("User is deleted at: " + user.getDeletedAt());
        }

        // ⭐️ User 객체에서 Role(권한) 정보를 가져와 SimpleGrantedAuthority로 변환합니다.
        // 현재는 단일 Role(String) 필드가 있다고 가정하고 List로 변환합니다.
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));

        System.out.println("authorities: " + authorities);;

        // User 객체를 UserDetails로 변환하여 반환
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()), // UserDetails의 username은 고유 식별자(여기서는 ID)로 사용
                "", // 비밀번호는 없으므로 빈 문자열
//                Collections.emptyList() // 권한(roles) 정보는 비워둡니다
                authorities // ⭐️ 권한(Role) 정보를 authorities에 담아 전달합니다.
        );
    }

    /**
     * 사용자 ID로 User 엔티티를 조회합니다.
     * @param userId 사용자의 고유 ID
     * @return User 엔티티
     */
    @Transactional(readOnly = true)
    public User getUserByUserId(Long userId) {
        System.out.println("UserService - getUserByUserId 메소드 진입");
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Transactional
    public void withdrawUser(Long currentUserId, HttpServletRequest request) {
        System.out.println("UserService - withdrawUser 진입");
        System.out.println("UserService - currentUserId: " + currentUserId);

        // 1. AuthService를 통해 Refresh Token 추출
        String refreshToken = authService.extractRefreshTokenFromCookie(request);

        System.out.println("UserService - withdrawUser - extractRefreshTokenFromCookie 종료");

        // 2. RT가 없으면 예외 처리 (Controller가 처리할 수도 있지만, 서비스에서 처리하는 것이 일관적)
        if (refreshToken == null) {
            // 탈퇴를 시도하는 상황에서 RT가 없으면 보안상 로그인이 필요함을 알리는 것이 적절
            throw new RefreshTokenExpiredException("Refresh token missing. Please login again to withdraw.");
        }

        System.out.println("UserService - withdrawUser - rt 존재");

        // 2. Refresh Token 무효화 (보안을 위해 가장 먼저 처리)
        authService.logout(refreshToken);

        System.out.println("UserService - withdrawUser - 로그아웃 완료");

        // 3. 사용자 엔티티 조회
//        User user = userRepository.findById(currentUserId)
//                .orElseThrow(() -> new RuntimeException("User not found with id: " + currentUserId));

        userWithdrawalService.executeWithdrawal(currentUserId);

        System.out.println("UserService - withdrawUser - 최종 완료");
    }

    /**
     * 시스템 더미 탈퇴 회원 엔티티를 조회합니다.
     * @return 더미 회원 User 엔티티
     * @throws RuntimeException 더미 회원이 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public User getWithdrawnUser() {
        return userRepository.findByEmail(WITHDRAWN_USER_EMAIL)
                .orElseThrow(() -> new RuntimeException("시스템 더미 탈퇴 회원(ID)를 찾을 수 없습니다."));
    }
}
