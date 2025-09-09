package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // 구글 로그인 처리: email, name을 받아 사용자 생성 또는 조회 후 JWT 발급
    public String processGoogleLogin(String email, String name) {

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setSnsProvider("google");
                    return userRepository.save(newUser);
                });

        // JWT 토큰 생성 (Access Token)
        String accessToken = jwtUtil.generateToken(email);

        // Refresh Token 생성 및 DB 저장
        String refreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusWeeks(2));
        userRepository.save(user);

        // 쿠키를 통해 컨트롤러에서 브라우저에 전달
        return accessToken;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
