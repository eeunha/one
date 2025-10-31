package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter // 1. 필드 값 조회를 위해 Getter 필수
@NoArgsConstructor // 2. 기본 생성자 (JSON 직렬화/역직렬화 시 필요)
@AllArgsConstructor // 3. 모든 필드를 인자로 받는 생성자 (객체 생성 시 편리)
public class LikeResponseDTO {
    private int likeCount;
    private boolean isLiked;
}
