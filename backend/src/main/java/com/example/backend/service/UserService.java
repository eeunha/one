package com.example.backend.service;

import com.example.backend.dto.LoginResponseDto;
import com.example.backend.dto.UserDTO;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // 구글 code 처리
    public LoginResponseDto processGoogleLogin(String email, String name) {

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setSnsProvider("google");
                    return userRepository.save(newUser);
                });

        // Access, Refresh Token 생성
        String accessToken = jwtUtil.generateToken(email);
        String refreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusWeeks(2));
        userRepository.save(user);

        return new LoginResponseDto(user.getName(), user.getEmail(), accessToken);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
