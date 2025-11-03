package com.example.backend.repository;

import com.example.backend.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByPost_IdAndUser_Id(Long postId, Long userId);

    Optional<Like> findByPost_IdAndUser_Id(Long postId, Long userId);

    // 레코드 개수를 세는 행위이므로 표준에 따라 int가 아닌 long 사용
    long countByPost_id(Long postId);
}