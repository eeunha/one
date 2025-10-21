package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.exception.RefreshTokenExpiredException;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * 로그아웃: DB에서 사용자의 리프레시 토큰을 무효화합니다.
     * @param refreshToken 쿠키에서 추출한 리프레시 토큰
     */
    @Transactional // 쓰기 작업이 필요합니다.
    public void logout (String refreshToken) {

        System.out.println("AuthService - logout 메소드 진입");

        // 1. 리프레시 토큰에서 사용자 id 추출
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 2. userId를 이용해 사용자 엔티티 찾기
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found the given refresh token."));

        // 3. 사용자 엔티티의 리프레시 토큰을 null로 설정하여 무효화한다.
        // ⭐️ 개선: setter 대신 엔티티 비즈니스 메서드 사용
        user.updateRefreshToken(null, null);
        
        // updatedAt 갱신
        user.updateModifiedAt();
        System.out.println("로그아웃 시 updated_at 갱신 완료");

        // 4. JPA의 변경 감지(Dirty Checking)가 처리하므로 save()는 생략 가능하지만, 명시적으로 호출해도 무방합니다.
        userRepository.save(user);
    }

    @Transactional // 데이터 일관성 보장을 위해 트랜잭션 유지
    public String refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("refreshAccessToken 메소드 진입");

        // 1. 쿠키에서 Refresh Token 가져오기
        String refreshToken = extractRefreshTokenFromCookie(request);

        System.out.println("refreshToken from cookie: " + refreshToken);

        if (refreshToken == null) {
            throw new RefreshTokenExpiredException("Refresh token missing. Please login again.");
        }

        // 2. DB에서 RefreshToken 유효성 체크
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenExpiredException("Invalid refresh token"));

        // 3. Refresh Token 만료 확인
        if (user.getRefreshTokenExpiry() == null || user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            // 토큰 만료 시 DB에서도 토큰 정보를 제거합니다.
            user.updateRefreshToken(null, null);
            userRepository.save(user);
            throw new RefreshTokenExpiredException("Refresh token expired. Please log in again.");
        }

        // 4-1. 새 Access Token 생성
        String newAccessToken = jwtUtil.generateAccessToken(user);

        // 4-2. updated_at 갱신 (AT 재발급을 사용자 활동으로 간주하고 갱신)
        user.updateModifiedAt();
        
        System.out.println("at 재발급 후 updated_at 갱신 완료");

        // 4-3. 변경사항 저장
        // Note: RT 필드는 변경되지 않았지만, updated_at 필드가 변경되었으므로 UPDATE 쿼리가 실행됩니다.
        userRepository.save(user);

        // 5. 새 Refresh Token은 발급하지 않으므로, 기존 RT를 응답에 포함하거나 아무것도 하지 않습니다.
        // 만약 기존 RT를 클라이언트에게 계속 전달해야 한다면, 여기서 처리해야 합니다.
        // (현재 컨트롤러는 Access Token만 응답합니다.)

        System.out.println("newAccessToken: " + newAccessToken);

        return newAccessToken;
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {

        System.out.println("extractRefreshTokenFromCookie 메소드 진입");
        
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }
        return refreshToken;
    }
}
