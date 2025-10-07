package com.example.backend.controller;

import com.example.backend.dto.AccessTokenAndProfileResponseDTO;
import com.example.backend.dto.JwtAndProfileResponseDTO;
import com.example.backend.exception.RefreshTokenExpiredException;
import com.example.backend.service.AuthService;
import com.example.backend.service.OAuthService;
import com.example.backend.service.UserService;
import com.example.backend.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 로그인 된(인증된) 사용자의 상태 관리 및 필요한 데이터 제공
// (인증 이후 상태 관리에 집중)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final OAuthService oAuthService;
    private final UserService userService;
    private final CookieUtil cookieUtil;
    private final AuthService authService;

    public AuthController(OAuthService oAuthService, UserService userService, CookieUtil cookieUtil, AuthService authService) {
        this.oAuthService = oAuthService;
        this.userService = userService;
        this.cookieUtil = cookieUtil;
        this.authService = authService;
    }

    /**
     * Google 로그인 후 프론트엔드에서 인증 코드를 보내는 API 엔드포인트
     * 이 컨트롤러는 리다이렉션 대신 JSON 응답을 반환합니다.
     * @param requestBody frontend에서 받은 code가 담긴 요청 본문
     * @return JWT와 사용자 프로필 정보가 담긴 DTO
     */
    @PostMapping("/google/login")
    public ResponseEntity<JwtAndProfileResponseDTO> googleLogin(
            @RequestBody Map<String, String> requestBody, HttpServletResponse response
    ) {
        String code = requestBody.get("code");
        
        System.out.println("googleLogin 메소드 진입");

        // 1. OAuthService를 호출하여 모든 JWT와 프로필 정보를 받습니다.
        JwtAndProfileResponseDTO fullResponse = oAuthService.getJwtAndProfileResponse(code);

        System.out.println("로그인 후 refresh: " + fullResponse.getRefreshToken());

        // 2. 리프레시 토큰을 HttpOnly 쿠키에 담아 반환합니다.
        // 이 쿠키는 자바스크립트로 접근할 수 없어 XSS 공격에 안전합니다.
        cookieUtil.addJwtCookie(response, "refreshToken", fullResponse.getRefreshToken(), 60 * 60 * 24 * 7);
//        cookieUtil.addJwtCookie(response, "refreshToken", fullResponse.getRefreshToken(), 10);
//        cookieUtil.addJwtCookie(response, "refreshToken", fullResponse.getRefreshToken(), 60 * 60 * 24 + 20);

        // 3. 응답 바디에는 리프레시 토큰을 제외한 액세스 토큰과 프로필 정보만 담아 반환합니다.
        fullResponse.setRefreshToken(null);
        return ResponseEntity.ok(fullResponse);
    }

    // 새로고침 시 사용자 정보를 복구하는 API
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        System.out.println("getProfile 메소드 진입");
        
        try {
            // Spring Security의 Authentication 객체에서 사용자 ID 추출
            // JwtUtil을 통해 토큰이 이미 검증된 상태이므로 별도의 유효성 검사는 불필요
            String userIdFromToken = authentication.getName(); // JWT 토큰의 subject(사용자 ID)를 가져옴

            // 사용자 ID를 기반으로 DB에서 프로필 정보를 조회하고 새로운 토큰을 생성
            AccessTokenAndProfileResponseDTO responseDto = userService.getProfileWithNewToken(userIdFromToken);

            if (responseDto != null) {
                return ResponseEntity.ok(responseDto); // 200 OK
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"); // 404 Not Found
            }
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
            String newAccessToken = authService.refreshAccessToken(request, response);

            System.out.println("AuthController - newAccessToken token: " + newAccessToken);

            // 2. 새 Access Token 응답
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (RefreshTokenExpiredException e) {
            // 3. Refresh Token 만료 등 예외 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

}