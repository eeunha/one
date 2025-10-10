package com.example.backend.controller;

import com.example.backend.entity.Post;
import com.example.backend.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // === 1. 게시글 생성 (POST /api/posts) ===
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Map<String, Object> payload) {

        // 🚨 실제로는 인증 컨텍스트에서 userId를 추출해야 합니다.
        Long userId = ((Number) payload.get("userId")).longValue();
        String title = (String) payload.get("title");
        String content = (String) payload.get("content");

        try {
            Post createPost = postService.createPost(userId, title, content);
            return new ResponseEntity<>(createPost, HttpStatus.CREATED);// 201 Created
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    // === 2. 게시글 목록 조회 (GET /api/posts) ===
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        // 실제로는 Pageable 객체를 받아 페이징 처리를 해야 하지만, 간단히 전체 목록을 반환합니다.
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    // === 3. 게시글 상세 조회 (GET /api/posts/{postId}) ===
    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPostDetail(@PathVariable Long postId) {
        try {
            Post post = postService.getPostDetail(postId);
            return ResponseEntity.ok(post); // 200 OK
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    // === 4. 게시글 수정 (PUT /api/posts/{postId}) ===
    @PutMapping("/{postId}")
    public ResponseEntity<Post> updatePost(@PathVariable Long postId, @RequestBody Map<String, Object> payload) {

        Long userId = ((Number) payload.get("userId")).longValue();
        String newTitle = (String) payload.get("title");
        String newContent = (String) payload.get("content");

        try {
            Post post = postService.updatePost(postId, userId, newTitle, newContent);
            return ResponseEntity.ok(post);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found (게시글 없음)
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden (권한 없음)
        }
    }

    // === 5. 게시글 소프트 삭제 (DELETE /api/posts/{postId}) ===
    @DeleteMapping("/{postId}")
    public ResponseEntity<Post> deleteSoftPost(@PathVariable Long postId, @RequestBody Map<String, Object> payload) {

        // DELETE 요청의 Body 사용은 RESTful 표준에 완전히 맞지는 않지만, 테스트 편의를 위해 사용
        Long userId = ((Number) payload.get("userId")).longValue();

        try {
            postService.softDeletePost(postId, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content (성공적으로 삭제)
        } catch(EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
        }
    }


}
