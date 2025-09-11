package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 로그인 성공 후 전달
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String name;
    private String email;
    private String accessToken;
}
