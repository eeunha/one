package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.util.CookieUtil;
import com.example.backend.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys; // 추가된 import
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey; // 추가된 import
import java.nio.charset.StandardCharsets; // 추가된 import
import java.util.HashMap;
import java.util.Map;

// 로그인 된(인증된) 사용자의 상태 관리 및 필요한 데이터 제공
// (인증 이후 상태 관리에 집중)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil, CookieUtil cookieUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
    }

    // JWT Access Token으로 사용자 프로필 조회
    @GetMapping("/profile")
    public Map<String, String> getProfile(@CookieValue("accessToken") String token) {

        // ★ JWT 검증 및 정보 추출 로직을 JwtUtil에 위임
        if (!jwtUtil.validateToken(token)) {
            // 토큰이 유효하지 않으면 JWTException을 발생시킵니다.
            throw new JwtException("Invalid or expired JWT token.");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userService.getUserByEmail(email);

        Map<String, String> profile = new HashMap<>();
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        return profile;
    }

    // 로그아웃 기능
    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        cookieUtil.expireCookie(response, "accessToken", false);
    }
}