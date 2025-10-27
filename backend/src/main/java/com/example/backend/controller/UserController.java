package com.example.backend.controller;

import com.example.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // ⭐️ 1. URI를 /me로 변경하고, DELETE 메서드를 사용합니다.
    // ⭐️ 2. Principal을 사용하여 인증된 사용자 ID를 가져옵니다.
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawUser(
            Principal principal,
            HttpServletRequest request
    ) {
        System.out.println("UserController - withdrawUser 진입");

        String currentUserIdString = principal.getName();

        System.out.println("UserController - currentUserIdString: " + currentUserIdString);

        if (currentUserIdString == null) {
            throw new RuntimeException("인증된 사용자 ID를 찾을 수 없습니다.");
        }

        Long currentUserId = Long.parseLong(currentUserIdString);
        System.out.println("UserController - currentUserId: " + currentUserId);

        // 서비스 호출: 현재 인증된 ID와 함께 Refresh Token을 전달합니다.
        // 탈퇴 로직은 사용자 인증 정보와 RT 무효화 로직이 모두 필요합니다.
        userService.withdrawUser(currentUserId, request);

        // RESTful 표준 응답: 204 No Content
        return ResponseEntity.noContent().build();
    }
}
