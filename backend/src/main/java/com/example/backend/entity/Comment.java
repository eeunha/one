package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder // ⭐️ Builder 패턴 적용
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 사용을 위한 private 전체 생성자
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 protected 무인자 생성자
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    // --- 작성자 (User) 매핑 ---
    @ManyToOne(fetch = FetchType.LAZY)
    // ⭐️ DB 컬럼명 user_id를 명시적으로 지정
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    // --- 게시글 (Post) 매핑 ---
    @ManyToOne(fetch = FetchType.LAZY)
    // ⭐️ DB 컬럼명 post_id를 명시적으로 지정
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // 이 댓글이 달린 게시글 객체

    /**
     * Comment 객체 생성 시 필수 정보를 설정하는 생성자
     */
    public Comment(String content, User author, Post post) {
        this.content = content;
        this.author = author;
        this.post = post;
    }
}
