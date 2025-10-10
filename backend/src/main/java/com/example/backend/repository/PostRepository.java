package com.example.backend.repository;

import com.example.backend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Post 엔티티에 대한 데이터 접근(Repository) 인터페이스
 * JpaRepository를 상속받아 기본 CRUD 메서드(save, findById, findAll, delete 등)를 자동 제공받습니다.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    // extends JpaRepository<Post, Long>: 이 한 줄이 Post 엔티티(Post.class)를 사용하고,
    //   기본 키 타입(Long.class)으로 데이터베이스와 통신하는 모든 기본 CRUD 기능을 자동으로 Spring Data JPA에 등록합니다.


    /**
     * 특정 작성자(User)가 작성한 게시글 목록을 조회합니다.
     * 엔티티 필드 이름(author)으로 자동 쿼리가 생성됩니다.
     * @param authorId 조회할 작성자의 ID
     * @return 해당 작성자가 쓴 Post 목록
     */
    List<Post> findByAuthorId(Long authorId);
}
