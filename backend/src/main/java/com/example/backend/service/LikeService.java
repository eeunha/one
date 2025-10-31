package com.example.backend.service;

import com.example.backend.dto.LikeResponseDTO;
import com.example.backend.entity.Like;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.exception.AlreadyLikeException;
import com.example.backend.repository.LikeRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeResponseDTO likePost(Long postId, Long userId) {

        // 1. 게시글 존재 확인 (404 Not Found 예외 처리)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글(Post ID: " + postId + ")를 찾을 수 없습니다."));

        // 2. 사용자 엔티티 조회 (Like 엔티티 생성을 위해 필요)
        // 실제로는 User 엔티티를 직접 사용하는 대신 DTO나 ID만 사용할 수도 있습니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자(User ID: " + userId + ")를 찾을 수 없습니다."));

        // 3. 중복 좋아요 확인 및 처리
        if (likeRepository.existsByPost_IdAndUser_Id(postId, userId)) {
            // 이미 좋아요를 눌렀다면, 이미 좋아요가 되어있다는 예외를 던집니다.
            throw new AlreadyLikeException("사용자(User ID: " + userId + ")는 이미 이 게시글에 좋아요를 눌렀습니다.");
        }

        // -- 좋아요 처리 --

        // 4. 좋아요 테이블에 값 넣기 (postid, userid)
        likeRepository.save(
                Like.builder()
                        .post(post)
                        .user(user)
                        .build()
        );

        // 5. posts 테이블의 like_count만 1 증가
        // updated_at 갱신 방지를 위해 Native Query를 호출합니다.
        postRepository.incrementLikeCount(postId);

        // -- 응답 데이터 준비 --

        // 6. Native Query 후에는 Post 엔티티의 likeCount 값이 메모리에서 즉시 갱신되지 않습니다.
        // JPA/Hibernate의 캐시를 무시하고 최신 상태를 가져오거나,
        // 간단하게 현재 조회한 값에 +1을 하여 DTO를 구성합니다. (여기서는 +1 사용)
        // *주의: post.getLikeCount()는 Native Query 실행 전의 값입니다.
        int newLikeCount = post.getLikeCount() + 1;

        // 7. DTO 반환
        return LikeResponseDTO.builder()
                .likeCount(newLikeCount)
                .isLiked(true)
                .build();
    }

    @Transactional
    public LikeResponseDTO unlikePost(Long postId, Long userId) {


        return new LikeResponseDTO();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer incrementLikeCount(Long postId) {
        postRepository.incrementLikeCount(postId);

        Integer latestLikeCount = postRepository.findLikeCountByIdNative(postId);

        return latestLikeCount;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer decrementLikeCount(Long postId) {
        postRepository.decrementLikeCount(postId);

        Integer latestLikeCount = postRepository.findLikeCountByIdNative(postId);

        return latestLikeCount;
    }
}
