package com.example.backend.controller;

import com.example.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // ⭐️ 1. URI를 /me로 변경하고, DELETE 메서드를 사용합니다.
    // ⭐️ 2. @AuthenticationPrincipal을 사용하여 인증된 사용자 ID를 가져옵니다.
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawUser(
            @AuthenticationPrincipal Long currentUserId, // ⭐️ JWT에서 추출된 Long 타입의 사용자 ID
            HttpServletRequest request
    ) {

        // 서비스 호출: 현재 인증된 ID와 함께 Refresh Token을 전달합니다.
        // 탈퇴 로직은 사용자 인증 정보와 RT 무효화 로직이 모두 필요합니다.
        userService.withdrawUser(currentUserId, request);

        // RESTful 표준 응답: 204 No Content
        return ResponseEntity.noContent().build();
    }
}
