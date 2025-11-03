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
// Soft Delete 구현: 실제 DELETE 대신 deleted_at을 업데이트
@org.hibernate.annotations.SQLDelete(sql = "UPDATE comments SET deleted_at = NOW() WHERE id = ?")
// ⭐ 핵심: 쿼리 실행 시 이 조건이 자동으로 추가되어 삭제된 레코드를 제외합니다. ⭐
@org.hibernate.annotations.Where(clause = "deleted_at IS NULL")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
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
}
