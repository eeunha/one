package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.secret}")
    private String secretKey;

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // 예: JWT Access Token으로 사용자 프로필 조회
    @GetMapping("/profile")
    public Map<String, String> getProfile(@CookieValue("accessToken") String token) {

        // 쿠키에서 JWT 읽기
        String email = Jwts.parser()
                .setSigningKey(secretKey.getBytes())
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
