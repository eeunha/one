package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좋아요/좋아요 취소 요청에 대한 응답 DTO
 * isLiked와 최신 likeCount를 포함하여 클라이언트에게 상태를 전달합니다.
 */
@Getter // 1. 필드 값 조회를 위해 Getter 필수
@Builder
@NoArgsConstructor // 2. 기본 생성자 (JSON 직렬화/역직렬화 시 필요)
@AllArgsConstructor // 3. 모든 필드를 인자로 받는 생성자 (객체 생성 시 편리)
public class LikeResponseDTO {

    // 게시글의 최신 좋아요 수
    private int likeCount;

    // 현재 사용자의 좋아요 상태 (true: 좋아요 상태, false: 좋아요 취소 상태)
    private boolean isLiked;
}
