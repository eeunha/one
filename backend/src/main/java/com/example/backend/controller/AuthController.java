package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys; // 추가된 import
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey; // 추가된 import
import java.nio.charset.StandardCharsets; // 추가된 import
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final SecretKey secretKey; // String 대신 SecretKey 타입으로 변경

    // 생성자에서 SecretKey 객체를 한 번만 생성하여 재사용 (Best Practice)
    public AuthController(UserService userService, @Value("${jwt.secret}") String secret) {
        this.userService = userService;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 예: JWT Access Token으로 사용자 프로필 조회
    @GetMapping("/profile")
    public Map<String, String> getProfile(@CookieValue("accessToken") String token) {

        // 쿠키에서 JWT 읽기 (수정된 부분)
        String email = Jwts.parserBuilder()
                .setSigningKey(this.secretKey) // 생성자에서 만든 SecretKey 사용
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        User user = userService.getUserByEmail(email);

        Map<String, String> profile = new HashMap<>();
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        return profile;
    }
}