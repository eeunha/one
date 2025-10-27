package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.exception.RefreshTokenExpiredException;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logout (String refreshToken) {

        System.out.println("AuthService - logout 메소드 진입");

        // 1. 리프레시 토큰에서 사용자 id 추출
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        System.out.println("AuthService - userId: " + userId);

        // 2. userId를 이용해 사용자 엔티티 찾기
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found the given refresh token."));

        System.out.println("AuthService - user: " + user);

        // 3. 사용자 엔티티의 리프레시 토큰을 null로 설정하여 무효화한다.
        // ⭐️ 개선: setter 대신 엔티티 비즈니스 메서드 사용
        user.updateRefreshToken(null, null);

        System.out.println("AuthService - user.getRefreshTokenExpiry: " + user.getRefreshTokenExpiry());

        // ⭐️ 디버깅용 로그 추가 ⭐️
        System.out.println("AuthService: RT 무효화 성공 - userId: " + userId);
    }

    @Transactional
    public String refreshAccessToken(HttpServletRequest request) {
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

        // 4. 새 Access Token 생성
        String newAccessToken = jwtUtil.generateAccessToken(user);

        System.out.println("newAccessToken: " + newAccessToken);

        return newAccessToken;
    }

    public String extractRefreshTokenFromCookie(HttpServletRequest request) {

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
