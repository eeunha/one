package com.example.backend.dto;

import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostResponseDTO {

    private Long id;
    private String title;
    private String content;

    private int viewCount;
    private int likeCount;

    private int commentCount;

    private Long authorId;
    private String authorName;

    private LocalDateTime createdAt;

    public PostResponseDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();

        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();

        if (post.getCreatedAt() != null) {
            long activeCommentCount = post.getComments().stream()
                    .filter(c -> c.getDeletedAt() == null)
                    .count();
            this.commentCount = (int) activeCommentCount;
        } else {
            this.commentCount = 0;
        }

        // N+1 문제를 방지하기 위해 Fetch Join으로 로드된 author 정보를 사용합니다.
        User author = post.getAuthor();
        this.authorId = author.getId();
        this.authorName = author.getName();

        this.createdAt = post.getCreatedAt();
    }
}
