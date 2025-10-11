package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@Builder // ⭐️ Builder 패턴 적용
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 사용을 위한 private 전체 생성자
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 protected 무인자 생성자
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // --- 작성자 (User) 매핑: ManyToOne ---
    // DB 컬럼: user_id (BIGINT) -> JPA 객체: User author
    // Post (多)는 User (1)에 속하며, Post가 연관 관계의 주인이 됩니다.
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 설정 (Post 조회 시 User는 필요할 때만 로딩)
    @JoinColumn(name = "user_id", nullable = false) // DB의 FK 컬럼 이름인 "user_id"를 명시적으로 지정
    private User author; // 작성자 User 객체

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    // === 비즈니스 로직 편의 메서드 ===

    // --- 댓글 (Comment) 매핑 (소프트 삭제를 위해 cascade 설정 없음) ---
    // Comment의 post 필드에 의해 매핑됨
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    /**
     * 조회수를 증가시키는 메서드
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Post 객체 생성 시 필수 정보를 설정하는 생성자
     */
    public Post(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.viewCount = 0;
    }
}
