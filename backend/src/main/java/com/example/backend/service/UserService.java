package com.example.backend.service;

import com.example.backend.dto.JwtAndProfileResponseDTO;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // 구글 로그인 처리: email, name을 받아 사용자 생성 또는 조회 후 JWT 발급
    public JwtAndProfileResponseDTO processGoogleLogin(String email, String name) {

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setSnsProvider("google");
                    return userRepository.save(newUser);
                });

        // 1. JWT 토큰 생성 (Access Token & Refresh Token)
        // JWT에 사용자 ID만 담도록 수정
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Refresh Token DB 저장
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusWeeks(2));
        userRepository.save(user);

        // 3. 토큰과 프로필 정보를 DTO에 담아서 반환
        JwtAndProfileResponseDTO response = new JwtAndProfileResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());

        // 쿠키를 통해 컨트롤러에서 브라우저에 전달
        return response;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
