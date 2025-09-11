package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys; // 추가된 import
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey; // 추가된 import
import java.nio.charset.StandardCharsets; // 추가된 import
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil; // ★ JwtUtil 주입

    public AuthController(UserService userService, JwtUtil jwtUtil) { // ★ 생성자 수정
        this.userService = userService;
        this.jwtUtil = jwtUtil;
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
}