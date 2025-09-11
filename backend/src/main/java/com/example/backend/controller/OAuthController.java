package com.example.backend.controller;

import com.example.backend.service.OAuthService;
import com.example.backend.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

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
    public String googleCallback(@RequestParam("code") String code, HttpServletResponse httpServletResponse) throws IOException {

        // 1. 새로운 OAuthService를 호출하여 JWT 토큰을 받음
        String jwtToken = oAuthService.getJwtTokenFromGoogleAuth(code);

        // 2. JwtUtil 대신 CookieUtil을 사용하여 쿠키를 추가
        cookieUtil.addJwtCookie(httpServletResponse, jwtToken);

        // 3. 프론트 페이지로 리다이렉트
        return "redirect:http://localhost:8086/profile";
    }
}