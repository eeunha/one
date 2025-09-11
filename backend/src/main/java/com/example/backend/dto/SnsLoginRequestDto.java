package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 클라이언트에서 보낸 구글 code
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnsLoginRequestDto {
    private String code;
}
