package com.example.backend.dto;

import com.example.backend.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class PostResponseDTO {

    private Long id;
    private String title;
    private String content;
    private int viewCount;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private List<CommentResponseDTO> comments;

    public PostResponseDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.viewCount = post.getViewCount();
        this.authorId = post.getAuthor().getId();
        this.authorName = post.getAuthor().getName();
        this.createdAt = post.getCreatedAt();

        if (post.getComments() != null) {
            this.comments = post.getComments().stream()
                    .filter(c -> c.getDeletedAt() == null)
                    .map(CommentResponseDTO::new)
                    .collect(Collectors.toList());
        }
    }
}
