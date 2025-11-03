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
// Soft Delete 구현: 실제 DELETE 대신 deleted_at을 업데이트
@org.hibernate.annotations.SQLDelete(sql = "UPDATE posts SET deleted_at = NOW() WHERE id = ?")
// ⭐ 핵심: 쿼리 실행 시 이 조건이 자동으로 추가되어 삭제된 레코드를 제외합니다. ⭐
@org.hibernate.annotations.Where(clause = "deleted_at IS NULL")
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

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    // === 비즈니스 로직 편의 메서드 ===

    // ✨ Likes와의 일대다(OneToMany) 관계 추가
    // mappedBy = "post" : Like 엔티티의 'post' 필드에 의해 매핑되었음을 알림 (연관 관계의 주인이 아님)
    // orphanRemoval = true : 컬렉션(likes)에서 특정 Like 객체를 제거했을 때 해당 Like 객체를 자동으로 물리 삭제하는 기능
    @Builder.Default
    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    // --- 댓글 (Comment) 매핑 (소프트 삭제를 위해 cascade 설정 없음) ---
    // Comment의 post 필드에 의해 매핑됨
    @Builder.Default
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
