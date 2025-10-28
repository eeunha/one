package com.example.backend.controller;

import com.example.backend.dto.LoginResultWrapper;
import com.example.backend.dto.ProfileResponseDTO;
import com.example.backend.entity.User;
import com.example.backend.exception.RefreshTokenExpiredException;
import com.example.backend.exception.UserWithdrawnException;
import com.example.backend.service.AuthService;
import com.example.backend.service.OAuthService;
import com.example.backend.service.UserService;
import com.example.backend.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// 로그인 된(인증된) 사용자의 상태 관리 및 필요한 데이터 제공
// (인증 이후 상태 관리에 집중)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor // ⭐️ final 필드를 주입받는 생성자를 자동으로 생성합니다.
public class AuthController {

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    private final OAuthService oAuthService;
    private final UserService userService;
    private final CookieUtil cookieUtil;
    private final AuthService authService;

    /**
     * Google 로그인 후 프론트엔드에서 인증 코드를 보내는 API 엔드포인트
     * 이 컨트롤러는 리다이렉션 대신 JSON 응답을 반환합니다.
     * @param requestBody frontend에서 받은 code가 담긴 요청 본문
     * @return JWT와 사용자 프로필 정보가 담긴 DTO
     */
    @PostMapping("/google/login")
    public ResponseEntity<?> googleLogin(
            @RequestBody Map<String, String> requestBody, HttpServletResponse response
    ) {
        System.out.println("googleLogin 메소드 진입");

        String code = requestBody.get("code");

        try {
            // 1. 서비스로부터 Wrapper 객체를 받습니다.
            LoginResultWrapper resultWrapper = oAuthService.getJwtAndProfileResponse(code);

            System.out.println("로그인 후 refresh: " + resultWrapper.getRefreshToken());

            // 2. Wrapper에서 RT를 꺼내 HttpOnly 쿠키에 담아 헤더로 보냅니다.
            cookieUtil.addJwtCookie(response, "refreshToken", resultWrapper.getRefreshToken(), refreshTokenValidityInSeconds); // 1일 20초 (s)

            // 3. Wrapper에서 응답 DTO를 꺼내 바디로 반환합니다.
            return ResponseEntity.ok(resultWrapper.getLoginResponseDTO());
        } catch (UserWithdrawnException e) {
            // ⭐️ UserWithdrawnException 발생 시 403과 메시지를 직접 반환 ⭐️
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "UserWithdrawn");
            errorResponse.put("message", e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN); // 403

        } catch (Exception e) {
            // 그 외 예상치 못한 모든 오류를 500으로 처리
            return new ResponseEntity<>("로그인 처리 중 알 수 없는 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 새로고침 시 사용자 정보를 복구하는 API
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        System.out.println("getProfile 메소드 진입");

        try {
            // Spring Security의 Authentication 객체에서 사용자 ID 추출
            String userIdFromToken = authentication.getName(); // JWT 토큰의 subject(사용자 ID)를 가져옴
            Long userId = Long.valueOf(userIdFromToken);

            // 1. 사용자 ID를 기반으로 DB에서 프로필 정보를 조회합니다.
            User user = userService.getUserByUserId(userId);

            // 2. 응답 DTO를 구성합니다. (액세스 토큰은 응답에 포함하지 않음 - 기존 토큰 사용)
            ProfileResponseDTO responseDto = new ProfileResponseDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(responseDto); // 200 OK

        } catch (Exception e) {
            // 토큰이 유효하지 않거나 만료된 경우 Spring Security가 401을 처리함.
            // 하지만 예외 처리의 견고함을 위해 명시적인 에러 메시지를 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        }
    }

    // 로그아웃 기능
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("logout 메소드 진입");
        System.out.println("request: " + request + " response: " + response);

        // 1. 요청에서 리프레시 토큰 쿠키를 찾습니다.
        String refreshToken = cookieUtil.getCookieValue(request, "refreshToken");

        // --- 디버깅용 로그 추가 ---
        System.out.println("Received logout request. Extracted refreshToken: " + refreshToken);
        // -----------------------

        // 2. 리프레시 토큰이 존재하면 DB에서 삭제합니다.
        if (refreshToken != null) {
            try {
                authService.logout(refreshToken);
                System.out.println("User logged out successfully.");
            } catch (Exception e) {
                System.err.println("Logout failed due to an error: " + e.getMessage());
                // 에러 발생 시에도 쿠키는 삭제하도록 진행
            }
        } else {
            System.err.println("Logout request failed: No refresh token found in cookie.");
        }

        // 3. 브라우저의 리프레시 토큰 쿠키를 만료시킵니다.
        cookieUtil.expireCookie(response, "refreshToken");
        System.out.println("쿠키 만료 이후");

        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {

        System.out.println("refreshToken 메소드 진입");
        try {
            String newAccessToken = authService.refreshAccessToken(request);

            System.out.println("AuthController - newAccessToken token: " + newAccessToken);

            // 2. 새 Access Token 응답
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (RefreshTokenExpiredException e) {
            // 3. Refresh Token 만료 등 예외 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

}