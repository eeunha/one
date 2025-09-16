package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtAndProfileResponseDTO {
    private String accessToken;
    private String refreshToken;
    private Long id;
    private String email;
    private String name;
}
