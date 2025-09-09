package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

//    @GetMapping("/me")
//    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authHeader) {
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
//        }
//
//        String token = authHeader.substring(7);
//        String email = JwtUtil.getEmailFromToken(token);
//        String name = JwtUtil.getNameFromToken(token);
//
//        return ResponseEntity.ok(Map.of(
//                "email", email,
//                "name", name
//        ));
//    }

    // 예: JWT Access Token으로 사용자 프로필 조회
    @GetMapping("/profile")
    public Map<String, String> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        // 간단 테스트용: 토큰에서 email 추출 (실제 JWT 검증 필요)
        String email = Jwts.parser()
                .setSigningKey("mySecretKey123456".getBytes())
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
