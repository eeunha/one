package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResultWrapper {

    // 응답 바디로 나갈 정보 (AT, id, email, name, role 등)
    private LoginResponseDTO loginResponseDTO;

    // HttpOnly 쿠키로 헤더에 나갈 정보
    private String refreshToken;
}
