package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.exception.RefreshTokenExpiredException;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.CookieUtil;
import com.example.backend.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, CookieUtil cookieUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
    }

    public void logout (String refreshToken) {

        System.out.println("AuthService - logout 메소드 진입");

        // 1. 리프레시 토큰에서 사용자 id 추출
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 2. userId를 이용해 사용자 엔티티 찾기
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found the given refresh token."));

        // 3. 사용자 엔티티의 리프레시 토큰을 null로 설정하여 무효화한다.
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
    }

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
        if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenExpiredException("Refresh token expired.");
        }

        // 4. 새 Access Token 발급
        String newAccessToken = jwtUtil.generateAccessToken(user.getId());

        System.out.println("newAccessToken: " + newAccessToken);

        // 6. Refresh Token 만료 임박 시 새 Refresh Token 발급
        if (isRefreshTokenExpiresSoon(user)) {
            System.out.println("리프레시 토큰 만료 임박");

            String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
            user.setRefreshToken(newRefreshToken);
//            user.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(10)); // 리프레시토큰 기간 10초
            user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7)); // 리프레시토큰 기간 7일
            userRepository.save(user);

            // 쿠키에 새 Refresh Token 설정
            cookieUtil.addJwtCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 7); // 7일 (s)
//            cookieUtil.addJwtCookie(response, "refreshToken", refreshToken, 10); // 10초 (s)
//            cookieUtil.addJwtCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 + 20); // 1일 20초 (s)
        }

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

    // 8. Refresh Token 만료 임박 체크 (예: 남은 기간 1일 이하)
    private boolean isRefreshTokenExpiresSoon(User user) {
        System.out.println("isRefreshTokenExpiresSoon 메소드 진입");
        return ChronoUnit.DAYS.between(LocalDateTime.now(), user.getRefreshTokenExpiry()) <= 1;
    }
}
