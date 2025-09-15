package com.example.backend.controller;

import com.example.backend.dto.JwtAndProfileResponseDTO;
import com.example.backend.service.OAuthService;
import com.example.backend.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

// 로그인 인증 과정을 시작하고 완료
// (최초 인증에 집중)
@Controller
@RequestMapping("/oauth")
public class OAuthController {

    private final OAuthService oAuthService;
    private final CookieUtil cookieUtil;

    public OAuthController(OAuthService oAuthService, CookieUtil cookieUtil) {
        this.oAuthService = oAuthService;
        this.cookieUtil = cookieUtil;
    }

    @GetMapping("/callback/google")
    public ResponseEntity<JwtAndProfileResponseDTO> googleCallback(@RequestParam("code") String code, HttpServletResponse httpServletResponse) throws IOException {

        // 1. OAuthService를 호출하여 모든 정보를 담은 DTO 객체를 받음
        JwtAndProfileResponseDTO response = oAuthService.getJwtAndProfileResponse(code);

        // 2. CookieUtil을 사용하여 쿠키에 토큰을 추가
        cookieUtil.addJwtCookies(httpServletResponse, response.getAccessToken(), response.getRefreshToken());

        // 3. ResponseEntity를 사용하여 상태코드 200 OK와 함께 응답 본문에 프로필 정보를 포함
        return ResponseEntity.ok(response);
    }
}