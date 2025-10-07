package com.example.backend.service;

import com.example.backend.dto.AccessTokenAndProfileResponseDTO;
import com.example.backend.dto.JwtAndProfileResponseDTO;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 구글 로그인 처리를 담당합니다.
     * 이메일과 이름을 받아 사용자를 생성하거나 조회한 후, JWT를 발급합니다.
     *
     * @param email Google로부터 받은 사용자의 이메일
     * @param name  Google로부터 받은 사용자의 이름
     * @return 액세스 토큰, 리프레시 토큰 및 사용자 정보가 담긴 DTO
     */
    public JwtAndProfileResponseDTO processGoogleLogin(String email, String name) {

        System.out.println("UserService - processGoogleLogin 진입");
        
        // 1. 이메일로 기존 사용자가 있는지 조회합니다. 없으면 새로 생성합니다.
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setSnsProvider("google");
                    return userRepository.save(newUser);
                });

        // 2. JWT 토큰을 생성합니다.
        // JWT의 주체(subject)는 보안을 위해 사용자 ID를 사용합니다.
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 3. 리프레시 토큰을 DB에 저장합니다.
        // 이는 토큰 재발급 시 사용자의 유효성을 확인하는 데 필요합니다.
        user.setRefreshToken(refreshToken);
//        user.setRefreshTokenExpiry(LocalDateTime.now().plusWeeks(2));
//        user.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(10));
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7)); // 7일
        userRepository.save(user);

        // 4. 토큰과 프로필 정보를 DTO에 담아서 반환합니다.
        // 이 DTO는 AuthController에서 사용됩니다.

        // 쿠키를 통해 컨트롤러에서 브라우저에 전달
        return new JwtAndProfileResponseDTO(accessToken, refreshToken, user.getId(), user.getEmail(), user.getName());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        System.out.println("UserService - loadUserByUsername 메소드 진입");
        
        // userId는 Long 타입이므로 String으로 받은 username을 Long으로 변환
        Long userId = Long.parseLong(username);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // User 객체를 UserDetails로 변환하여 반환
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()), // UserDetails의 username은 고유 식별자(여기서는 ID)로 사용
                "", // 비밀번호는 없으므로 빈 문자열
                Collections.emptyList() // 권한(roles) 정보는 비워둡니다
        );
    }

    /**
     * 사용자 ID로 User 엔티티를 조회합니다.
     * @param userId 사용자의 고유 ID
     * @return User 엔티티
     */
    public User getUserByUserId(Long userId) {
        System.out.println("UserService - getUserByUserId 메소드 진입");
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    /**
     * 유효한 토큰에서 받은 사용자 ID(String)로 프로필 정보와 새로운 토큰을 함께 조회합니다.
     * @param userIdFromToken 유효한 토큰에서 추출한 사용자 ID (String)
     * @return 프로필 정보와 새로운 토큰이 담긴 AccessTokenAndProfileResponseDTO
     */
    public AccessTokenAndProfileResponseDTO getProfileWithNewToken(String userIdFromToken) {

        System.out.println("UserService - getProfileWithNewToken 메소드 진입");

        try {
            Long userId = Long.parseLong(userIdFromToken);
            User user = getUserByUserId(userId);
            String newAccessToken = jwtUtil.generateAccessToken(user.getId());
            return new AccessTokenAndProfileResponseDTO(newAccessToken, user.getId(), user.getEmail(), user.getName());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid user ID format in token: " + userIdFromToken);
        }
    }
}
