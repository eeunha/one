package com.example.backend.service;

import com.example.backend.entity.Comment;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // === 1. 댓글 생성 (Create) ===
    @Transactional
    public Comment createComment(Long postId, Long userId, String content) {

        // 1. 작성자(User)와 게시글(Post) 존재 여부 검증 및 엔티티 조회
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("작성자(User ID: " + userId + ")를 찾을 수 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글(Post ID: " + postId + ")을 찾을 수 없습니다."));

        // 2. Comment 엔티티 생성 및 저장
        Comment newComment = new Comment(content, author, post);
        return commentRepository.save(newComment);
    }

    // === 2. 게시글별 댓글 목록 조회 (Read) ===
    public List<Comment> getCommentsByPost(Long postId) {
        // PostRepository를 통해 Post의 존재 여부를 먼저 검증할 수도 있지만,
        // Repository에서 WHERE post_id = ? 쿼리를 직접 날려 성능 최적화를 합니다.
        // 이때 댓글이 없는 경우 빈 리스트를 반환합니다.
        return commentRepository.findByPostId(postId);
    }

    // === 3. 댓글 수정 (Update) ===
    @Transactional
    public Comment updateComment(Long commentId, Long userId, String newContent) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글(Comment ID: " + commentId + ")을 찾을 수 없습니다."));

        // 1. 권한 검사 (작성자 본인인지 확인)
        if (!comment.getAuthor().getId().equals(userId)) {
            // 언체크 예외로 권한 없음을 알림
            throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다.");
        }

        // 2. 객체 상태 변경
        comment.setContent(newContent);

        // 3. 트랜잭션 종료 시 자동 UPDATE (Dirty Checking)
        return comment;
    }

    // === 4. 댓글 소프트 삭제 (Delete) ===
    @Transactional
    public void deleteSoftComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글(Comment ID: " + commentId + ")을 찾을 수 없습니다."));

        // 1. 권한 검사
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }

        // 2. 소프트 삭제 처리 (DB에서 실제 데이터 삭제는 안 함)
        comment.markAsDeleted(); // BaseTime 엔티티에 정의된 메서드 호출
    }
}
