package com.example.backend.controller;

import com.example.backend.dto.CommentRequestDTO;
import com.example.backend.dto.CommentResponseDTO;
import com.example.backend.entity.Comment;
import com.example.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    // === 1. 댓글 생성 (POST /api/posts/{postId}/comments) ===
    @PostMapping
    public ResponseEntity<CommentResponseDTO> createComment (@PathVariable Long postId, @Valid @RequestBody CommentRequestDTO request, Principal principal) {

        Long userId = Long.valueOf(principal.getName());

        Comment createdComment = commentService.createComment(postId, userId, request.getContent());

        return new ResponseEntity<>(new CommentResponseDTO(createdComment), HttpStatus.CREATED); // 201 Created
    }

    // === 2. 게시글별 댓글 목록 조회 (GET /api/posts/{postId}/comments) ===
    @GetMapping
    public ResponseEntity<List<CommentResponseDTO>> getComments(@PathVariable Long postId) {
        // 댓글이 없어도 빈 리스트 []를 반환하며 200 OK 처리합니다.
        List<Comment> comments = commentService.getCommentsByPost(postId);

        // 삭제되지 않은 댓글만 필터링 후 Response DTO로 변환
        List<CommentResponseDTO> response = comments.stream()
                .filter(c -> c.getDeletedAt() == null)
                .map(CommentResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response); // 200 OK
    }

    // === 3. 댓글 수정 (PUT /api/posts/{postId}/comments/{commentId}) ===
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateComment (@PathVariable Long commentId, @Valid @RequestBody CommentRequestDTO request, Principal principal) {

        Long userId = Long.valueOf(principal.getName());

        // 예외 처리는 Global Handler로 위임 (권한 없음, 댓글 없음 등)
        Comment updatedComment = commentService.updateComment(commentId, userId, request.getContent());

        return new ResponseEntity<>(new CommentResponseDTO(updatedComment), HttpStatus.OK); // 200 OK
    }

    // === 4. 댓글 소프트 삭제 (DELETE /api/posts/{postId}/comments/{commentId}) ===
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteSoftComment (@PathVariable Long commentId, Principal principal) {

        Long userId = Long.valueOf(principal.getName());

        // 예외 처리는 Global Handler로 위임
        commentService.deleteSoftComment(commentId, userId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content (성공적으로 삭제)
    }
}
