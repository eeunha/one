package com.example.backend.repository;

import com.example.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * 특정 작성자(User)가 작성한 모든 댓글의 user_id를 더미 id로 수정합니다.
     * @param originalId 현재 작성자의 ID
     * @param dummyId 수정할 작성자의 더미 ID
     * @return 수정한 Comment 개수
     */
    // @where 때문에 삭제된 것은 변경되지 않았음. 그래서 nativeQuery 사용.
    @Modifying
    @Query(value = "UPDATE comments SET user_id = :dummyId WHERE user_id = :originalId", nativeQuery = true)
    int bulkUpdateAuthorIdToDummy(@Param("originalId") Long originalId, @Param("dummyId") Long dummyId);
}
