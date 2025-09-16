package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public void logout (String refreshToken) {
        // 1. 리프레시 토큰에서 사용자 id 추출
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 2. userId를 이용해 사용자 엔티티 찾기
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found the given refresh token."));

        // 3. 사용자 엔티티의 리프레시 토큰을 null로 설정하여 무효화한다.
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
    }
}
