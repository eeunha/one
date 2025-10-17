package com.example.backend.repository;

import com.example.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Comment 엔티티에 대한 데이터 접근(Repository) 인터페이스
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글(Post)에 달린 모든 댓글 목록을 조회합니다.
     * 엔티티 필드 이름(post)을 사용하여 'WHERE post_id = ?' 쿼리를 자동 생성합니다.
     * @param postId 댓글을 조회할 게시글의 ID
     * @return 해당 게시글의 Comment 목록
     */
    List<Comment> findByPostId(Long postId);

    /**
     * 특정 작성자(User)가 작성한 댓글 목록을 조회합니다.
     * 'WHERE user_id = ?' 쿼리를 자동 생성합니다.
     * @param authorId 조회할 작성자의 ID
     * @return 해당 작성자가 쓴 Comment 목록
     */
    List<Comment> findByAuthorId(Long authorId);
}
