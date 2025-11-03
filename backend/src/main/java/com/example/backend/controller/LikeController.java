package com.example.backend.controller;

import com.example.backend.dto.LikeResponseDTO;
import com.example.backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static com.example.backend.util.AuthUtil.getUserIdFromPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/likes")
public class LikeController {

    private final LikeService likeService;

    // 현재 사용자의 게시글 좋아요 상태 조회
    @GetMapping("/status")
    public ResponseEntity<LikeResponseDTO> fetchLikeStatus(@PathVariable Long postId, Principal principal) {

        System.out.println("LikeController - fetchLikeStatus 진입");

        Long userId = getUserIdFromPrincipal(principal);

        LikeResponseDTO likeResponseDTO = likeService.getLikeStatus(postId, userId);

        return ResponseEntity.ok(likeResponseDTO);
    }

    @PostMapping
    public ResponseEntity<LikeResponseDTO> likePost(@PathVariable Long postId, Principal principal) {

        System.out.println("LikeController - likePost 진입");

        Long userId = getUserIdFromPrincipal(principal);

        LikeResponseDTO likeResponseDTO = likeService.addLike(postId, userId);

        return ResponseEntity.ok(likeResponseDTO);
    }

    @DeleteMapping
    public ResponseEntity<LikeResponseDTO> unlikePost(@PathVariable Long postId, Principal principal) {

        System.out.println("LikeController - unlikePost 진입");

        Long userId = getUserIdFromPrincipal(principal);

        LikeResponseDTO likeResponseDTO = likeService.removeLike(postId, userId);

        return ResponseEntity.ok(likeResponseDTO);
    }
}
