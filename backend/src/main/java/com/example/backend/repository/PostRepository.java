package com.example.backend.repository;

import com.example.backend.dto.PostResponseDTO;
import com.example.backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Post 엔티티에 대한 데이터 접근(Repository) 인터페이스
 * JpaRepository를 상속받아 기본 CRUD 메서드(save, findById, findAll, delete 등)를 자동 제공받습니다.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = """
        SELECT NEW com.example.backend.dto.PostResponseDTO(
            p.id, p.title, p.content, p.viewCount, p.likeCount,
            COUNT(c.id),
            p.author.id, p.author.name, p.createdAt
        )
        FROM Post p
        JOIN p.author a
        LEFT JOIN Comment c ON c.post = p AND c.deletedAt IS NULL
        WHERE p.deletedAt IS NULL
        GROUP BY p.id, p.title, p.content, p.viewCount, p.likeCount, p.author.id, p.author.name, p.createdAt
        ORDER BY p.likeCount DESC, p.createdAt DESC
    """)
    Page<PostResponseDTO> findTopNByLikeCount(Pageable pageable);

    // 게시글 조회 시 updated_at 변경을 막기 위해, 조회수 업데이트는 별도의 Native Query로 처리
    @Modifying // DML 쿼리임을 명시 (데이터 변경)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    // 좋아요 눌렀을 때 updated_at 변경을 막기 위해, 조회수 업데이트는 별도의 Native Query로 처리
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    // 좋아요 취소했을 때 updated_at 변경을 막기 위해, 조회수 업데이트는 별도의 Native Query로 처리
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId")
    void decrementLikeCount(@Param("postId") Long postId);

    // 게시글 ID로 게시글과 작성자(User)를 한 번의 쿼리로 가져옵니다.
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.id = :postId")
    Optional<Post> findPostWithAuthorById(@Param("postId") Long postId);
    // 기존 findById() 대신 이 메서드를 사용하면 Post와 User가 즉시 로드되어 LazyException이 해결됩니다.

    // native Query로 DB에서 최신 ViewCount만 가져오기
    @Query(value = "SELECT p.view_count FROM posts p WHERE p.id = :postId", nativeQuery = true)
    Integer findViewCountByIdNative(@Param("postId") Long postId);

    // native Query로 DB에서 최신 likeCount만 가져오기
    @Query(value = "SELECT p.like_count FROM posts p WHERE p.id = :postId", nativeQuery = true)
    Integer findLikeCountByIdNative(@Param("postId") Long postId);

    /**
     * 특정 작성자(User)가 작성한 모든 게시글의 user_id를 더미 id로 수정합니다.
     * @param originalId 현재 작성자의 ID
     * @param dummyId 수정할 작성자의 더미 ID
     * @return 수정한 Post 개수
     */
    // @where 때문에 삭제된 것은 변경되지 않았음. 그래서 nativeQuery 사용.
    @Modifying
    @Query(value = "UPDATE posts SET user_id = :dummyId WHERE user_id = :originalId", nativeQuery = true)
    int bulkUpdateAuthorIdToDummy(@Param("originalId") Long originalId, @Param("dummyId") Long dummyId);
}
