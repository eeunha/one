package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isLiked")
    private boolean isLiked;

    /**
     * ⭐️ 수동 게터 추가 및 @JsonIgnore 적용 ⭐️
     * * Lombok이 생성하는 public boolean isLiked() 메서드를 오버라이드합니다.
     * @JsonIgnore를 붙여 Jackson이 이 메서드를 통해 'liked'라는 JSON 필드를 생성하는 것을 방지합니다.
     * 최종 JSON에는 @JsonProperty("isLiked")를 통해 'isLiked'만 남게 됩니다.
     */
    @JsonIgnore
    public boolean isLiked() {
        return this.isLiked;
    }
}
