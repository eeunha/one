package com.example.backend.controller;

import com.example.backend.dto.PostCreateRequestDTO;
import com.example.backend.dto.PostResponseDTO;
import com.example.backend.dto.PostUpdateRequestDTO;
import com.example.backend.entity.Post;
import com.example.backend.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // === 1. 게시글 생성 (POST /api/posts) ===
    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(@Valid @RequestBody PostCreateRequestDTO request,
                                           Principal principal) {

        Long userId = Long.valueOf(principal.getName());

        try {
            Post createdPost = postService.createPost(userId, request.getTitle(), request.getContent());
            return new ResponseEntity<>(new PostResponseDTO(createdPost), HttpStatus.CREATED); // 201 Created
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    // === 2. 게시글 목록 조회 (GET /api/posts) ===
    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        // 실제로는 Pageable 객체를 받아 페이징 처리를 해야 하지만, 간단히 전체 목록을 반환합니다.
        List<Post> posts = postService.getAllPosts();

        List<PostResponseDTO> response = posts.stream()
                .map(PostResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // === 3. 게시글 상세 조회 (GET /api/posts/{postId}) ===
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getPostDetail(@PathVariable Long postId) {
        try {
            Post post = postService.getPostDetail(postId);
            return ResponseEntity.ok(new PostResponseDTO(post)); // 200 OK
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    // === 4. 게시글 수정 (PUT /api/posts/{postId}) ===
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> updatePost(@PathVariable Long postId, @Valid @RequestBody PostUpdateRequestDTO request, Principal principal) {

        Long userId = Long.valueOf(principal.getName());

        try {
            Post updatedPost = postService.updatePost(postId, userId, request.getNewTitle(), request.getNewContent());
            return ResponseEntity.ok(new PostResponseDTO(updatedPost));
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found (게시글 없음)
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden (권한 없음)
        }
    }

    // === 5. 게시글 소프트 삭제 (DELETE /api/posts/{postId}) ===
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteSoftPost(@PathVariable Long postId, Principal principal) {

        // DELETE 요청의 Body 사용은 RESTful 표준에 완전히 맞지는 않지만, 테스트 편의를 위해 사용
        Long userId = Long.valueOf(principal.getName());

        try {
            postService.deleteSoftPost(postId, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content (성공적으로 삭제)
        } catch(EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
        }
    }
}
