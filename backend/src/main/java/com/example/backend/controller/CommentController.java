package com.example.backend.controller;

import com.example.backend.entity.Comment;
import com.example.backend.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    // === 1. 댓글 생성 (POST /api/posts/{postId}/comments) ===
    @PostMapping
    public ResponseEntity<Comment> createComment (@PathVariable Long postId, @RequestBody Map<String, Object> payload) {

        // 🚨 실제로는 인증 토큰에서 userId를 추출해야 하지만, 테스트를 위해 요청 바디에서 받습니다.
        Long userId = ((Number) payload.get("userId")).longValue();
        String content = (String) payload.get("content");

        try {
            Comment createdComment = commentService.createComment(userId, postId, content);
            return new ResponseEntity<>(createdComment, HttpStatus.CREATED); // 201 Created
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    // === 2. 게시글별 댓글 목록 조회 (GET /api/posts/{postId}/comments) ===
    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long postId) {
        // 댓글이 없어도 빈 리스트 []를 반환하며 200 OK 처리합니다.
        List<Comment> comments = commentService.getCommentsByPost(postId);
        return new ResponseEntity<>(comments, HttpStatus.OK);// 200 OK
    }

    // === 3. 댓글 수정 (PUT /api/posts/{postId}/comments/{commentId}) ===
    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment (@PathVariable Long commentId, @RequestBody Map<String, Object> payload) {

        // userId는 권한 검사를 위해 사용됩니다.
        Long userId = ((Number) payload.get("userId")).longValue();
        String newContent = (String) payload.get("content");

        try {
            Comment updatedComment = commentService.updateComment(userId, commentId, newContent);
            return new ResponseEntity<>(updatedComment, HttpStatus.OK); // 200 OK
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found (댓글이 없음)
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden (권한 없음)
        }
    }

    //d
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> softDeleteComment (@PathVariable Long commentId, @RequestBody Map<String, Object> payload) {

        Long userId = ((Number) payload.get("userId")).longValue();

        try {
            commentService.softDeleteComment(commentId, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content (성공적으로 삭제)
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
        }
    }
}
