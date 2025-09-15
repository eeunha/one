package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtAndProfileResponseDTO {
    private String accessToken;
    private String refreshToken;
    private Long id;
    private String email;
    private String name;
}
