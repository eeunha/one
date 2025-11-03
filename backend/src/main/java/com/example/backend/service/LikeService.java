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

    @Transactional(readOnly = true)
    public LikeResponseDTO getLikeStatus(Long postId, Long userId) {

        System.out.println("LikeService - getLikeStatus 진입");

        // 게시글 존재 확인 (404 Not Found 예외 처리)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글(Post ID: " + postId + ")를 찾을 수 없습니다."));

        // 좋아요 수 가져오기 (Post.likeCount 필드를 읽지 않고 Like 테이블에서 직접 계산)
        long calculatedLikeCount = likeRepository.countByPost_id(postId);

        // 현재 사용자가 좋아요 눌렀나 확인하기
        boolean isLiked = userId != null && likeRepository.existsByPost_IdAndUser_Id(postId, userId);

        // 두 데이터로 dto 만들어서 반환하기
        return LikeResponseDTO.builder()
                .likeCount((int) calculatedLikeCount)
                .isLiked(isLiked)
                .build();
    }

    @Transactional
    public LikeResponseDTO addLike(Long postId, Long userId) {

        System.out.println("LikeService - addLike 진입");

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
        Integer latestLikeCount = this.updateLikeCountIncrement(postId);

        // 6. DTO 반환
        return LikeResponseDTO.builder()
                .likeCount(latestLikeCount)
                .isLiked(true)
                .build();
    }

    @Transactional
    public LikeResponseDTO removeLike(Long postId, Long userId) {

        System.out.println("LikeService - removeLike 진입");

        // 1. 게시글 존재 확인 (404 Not Found 예외 처리)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글(Post ID: " + postId + ")를 찾을 수 없습니다."));

        // 2. 좋아요 기록 확인 및 삭제
        Like like = likeRepository.findByPost_IdAndUser_Id(postId, userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글에 대한 좋아요 기록(Post ID: " + postId + ", User ID: " + userId + ")을 찾을 수 없습니다."));

        likeRepository.delete(like);

        // 3. posts 테이블의 like_count만 1 감소
        // updated_at 갱신 방지를 위해 Native Query를 호출합니다.
        Integer latestLikeCount = this.updateLikeCountDecrement(postId);

        // 4. DTO 반환
        return LikeResponseDTO.builder()
                .likeCount(latestLikeCount)
                .isLiked(false)
                .build();
    }

    /**
     * Post의 like_count를 1 증가시키는 전용 메서드.
     * REQUIRES_NEW 트랜잭션을 사용하여 부모 트랜잭션의 JPA 캐시 영향을 받지 않고,
     * PostRepository의 Native Query를 실행하여 updated_at 갱신을 방지하고 최신 카운트를 가져옵니다.
     * @param postId 게시글 ID
     * @return 갱신된 최신 좋아요 수
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer updateLikeCountIncrement(Long postId) {

        System.out.println("LikeService - updateLikeCountIncrement 진입");

        postRepository.incrementLikeCount(postId);
        // Native Query로 DB에서 최신 카운트만 가져와 반환
        return postRepository.findLikeCountByIdNative(postId);
    }

    /**
     * Post의 like_count를 1 감소시키는 전용 메서드.
     * REQUIRES_NEW 트랜잭션을 사용하여 updated_at 갱신을 방지하고 최신 카운트를 가져옵니다.
     * @param postId 게시글 ID
     * @return 갱신된 최신 좋아요 수
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer updateLikeCountDecrement(Long postId) {

        System.out.println("LikeService - updateLikeCountDecrement 진입");

        postRepository.decrementLikeCount(postId);
        // Native Query로 DB에서 최신 카운트만 가져와 반환
        return postRepository.findLikeCountByIdNative(postId);
    }
}
