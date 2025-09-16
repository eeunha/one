package com.example.backend.controller;

import com.example.backend.dto.JwtAndProfileResponseDTO;
import com.example.backend.entity.User;
import com.example.backend.service.AuthService;
import com.example.backend.service.OAuthService;
import com.example.backend.service.UserService;
import com.example.backend.util.CookieUtil;
import com.example.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// 로그인 된(인증된) 사용자의 상태 관리 및 필요한 데이터 제공
// (인증 이후 상태 관리에 집중)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final OAuthService oAuthService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final AuthService authService;

    public AuthController(OAuthService oAuthService, UserService userService, JwtUtil jwtUtil, CookieUtil cookieUtil, AuthService authService) {
        this.oAuthService = oAuthService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
        this.authService = authService;
    }

    /**
     * Google 로그인 후 프론트엔드에서 인증 코드를 보내는 API 엔드포인트
     * 이 컨트롤러는 리다이렉션 대신 JSON 응답을 반환합니다.
     * @param requestBody frontend에서 받은 code가 담긴 요청 본문
     * @return JWT와 사용자 프로필 정보가 담긴 DTO
     */
//    /google/token
    @PostMapping("/google/login")
    public ResponseEntity<JwtAndProfileResponseDTO> googleLogin(
            @RequestBody Map<String, String> requestBody, HttpServletResponse response
    ) {
        String code = requestBody.get("code");
        
        System.out.println("googleLogin 메소드 진입");

        // 1. OAuthService를 호출하여 모든 JWT와 프로필 정보를 받습니다.
        JwtAndProfileResponseDTO fullResponse = oAuthService.getJwtAndProfileResponse(code);

        // 2. 리프레시 토큰을 HttpOnly 쿠키에 담아 반환합니다.
        // 이 쿠키는 자바스크립트로 접근할 수 없어 XSS 공격에 안전합니다.
        cookieUtil.addJwtCookie(response, "refreshToken", fullResponse.getRefreshToken(), 60 * 60 * 24 * 7);

        // 3. 응답 바디에는 리프레시 토큰을 제외한 액세스 토큰과 프로필 정보만 담아 반환합니다.
        fullResponse.setRefreshToken(null);
        return ResponseEntity.ok(fullResponse);
    }

    // JWT Access Token으로 사용자 프로필 조회
    @GetMapping("/profile")
    public Map<String, String> getProfile(@CookieValue("accessToken") String token) {

        // ★ JWT 검증 및 정보 추출 로직을 JwtUtil에 위임
        if (!jwtUtil.validateToken(token)) {
            // 토큰이 유효하지 않으면 JWTException을 발생시킵니다.
            throw new JwtException("Invalid or expired JWT token.");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userService.getUserByEmail(email);

        Map<String, String> profile = new HashMap<>();
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        return profile;
    }

    // 로그아웃 기능
    @PostMapping("/logout")
    public void logout(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);

        cookieUtil.expireCookie(response, "refreshToken");
    }
}