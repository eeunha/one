package com.example.backend.controller;

import com.example.backend.dto.PostCreateRequestDTO;
import com.example.backend.dto.PostResponseDTO;
import com.example.backend.dto.PostUpdateRequestDTO;
import com.example.backend.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // JWT/OAuth2 인증 구현 후, Principal 객체에서 Long 형태의 사용자 ID를 추출하는 헬퍼 메서드
    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            // 인증되지 않은 경우 임시 사용자 ID 반환 (실제 환경에서는 인증 예외를 던져야 함)
            return 1L;
        }
        return Long.valueOf(principal.getName());
    }

    // === 1. 게시글 생성 (POST /api/posts) ===
    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(@Valid @RequestBody PostCreateRequestDTO request,
                                           Principal principal) {

        Long userId = getUserIdFromPrincipal(principal);

        PostResponseDTO createdPostDTO = postService.createPost(userId, request.getTitle(), request.getContent());

        // DTO로 변환하여 201 Created 응답
        return new ResponseEntity<>(createdPostDTO, HttpStatus.CREATED);
    }

    // === 2. 게시글 목록 조회 (GET /api/posts) ===
    @GetMapping
    public ResponseEntity<Page<PostResponseDTO>> getPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // ⭐️ 파라미터 추가: Pageable 객체를 받아 페이징 처리
        // Service가 Page<PostResponseDTO>를 반환합니다.
        Page<PostResponseDTO> postPage = postService.getPosts(pageable);

        // 200 OK 응답 (ResponseEntity.ok() 편의 메서드 사용)
        return ResponseEntity.ok(postPage);
    }

    // === 3. 게시글 상세 조회 (GET /api/posts/{postId}) ===
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getPostDetail(@PathVariable Long postId) {

        // 리소스를 찾지 못하면 Service에서 EntityNotFoundException이 발생하고, Handler가 404 처리
        PostResponseDTO postDTO = postService.getPostDetail(postId);

        return ResponseEntity.ok(postDTO); // 200 OK
    }

    // === 4. 게시글 수정 (PUT /api/posts/{postId}) ===
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequestDTO request,
            Principal principal) {

        Long userId = getUserIdFromPrincipal(principal);

        // 권한이 없거나 리소스가 없으면 Service에서 예외가 발생하고, Handler가 403/404 처리
        PostResponseDTO updatedPostDTO = postService.updatePost(
                postId, userId, request.getNewTitle(), request.getNewContent());

        // 200 OK 응답
        return ResponseEntity.ok(updatedPostDTO);
    }

    // === 5. 게시글 소프트 삭제 (DELETE /api/posts/{postId}) ===
    // ⭐️ 수정: 관리자 권한(ADMIN)이거나 게시글의 작성자일 경우만 허용
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('ADMIN') or @postService.isPostOwner(#postId, principal.name)")
    public ResponseEntity<Void> deleteSoftPost(@PathVariable Long postId, Principal principal) {

        // DELETE 요청의 Body 사용은 RESTful 표준에 완전히 맞지는 않지만, 테스트 편의를 위해 사용
        Long userId = getUserIdFromPrincipal(principal);

        // 예외 처리는 모두 Handler로 위임
        postService.deleteSoftPost(postId, userId);

        // 본문 없는 204 No Content 응답
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content (성공적으로 삭제)
    }
}
