package com.example.backend.repository;

import com.example.backend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Post 엔티티에 대한 데이터 접근(Repository) 인터페이스
 * JpaRepository를 상속받아 기본 CRUD 메서드(save, findById, findAll, delete 등)를 자동 제공받습니다.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // JpaRepository에 이미 Page<Post> findAll(Pageable pageable)이 정의되어 있으므로,
    // 페이지네이션을 위한 기본 findAll 메서드는 별도로 선언하지 않아도 됩니다.

    // 게시글 조회 시 updated_at 변경을 막기 위해, 조회수 업데이트는 별도의 Native Query로 처리
    @Modifying // DML 쿼리임을 명시 (데이터 변경)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    // 게시글 ID로 게시글과 작성자(User)를 한 번의 쿼리로 가져옵니다.
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.id = :postId")
    Optional<Post> findPostWithAuthorById(@Param("postId") Long postId);
    // 기존 findById() 대신 이 메서드를 사용하면 Post와 User가 즉시 로드되어 LazyException이 해결됩니다.

    // native Query로 DB에서 최신 ViewCount만 가져오기
    @Query(value = "SELECT p.view_count FROM posts p WHERE p.id = :postId", nativeQuery = true)
    Integer findViewCountByIdNative(@Param("postId") Long postId);

    /**
     * 특정 작성자(User)가 작성한 모든 게시글 목록을 조회합니다.
     * 'WHERE user_id = ?' 쿼리를 자동 생성합니다.
     * @param authorId 조회할 작성자의 ID
     * @return 해당 작성자가 쓴 Post 목록
     */
    List<Post> findAllByAuthorId(Long authorId);
}
