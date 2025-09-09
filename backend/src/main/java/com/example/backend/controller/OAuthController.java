package com.example.backend.controller;

import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final UserService userService;

    public OAuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
    }

    @GetMapping("/callback/google")
    public void googleCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {

        // 1. code -> access token 교환
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        Map<String, String> tokenResponse = restTemplate.postForObject(tokenUrl, params, Map.class);
        String accessTokenFromGoogle = tokenResponse.get("access_token");

        // 2. access_token -> Google API로 사용자 정보 가져오기
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessTokenFromGoogle);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userInfoResponse  = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> userInfo = userInfoResponse.getBody();
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        // 3. UserService 호출 (JWT 발급)
        String jwtToken = userService.processGoogleLogin(email, name);

        // 4. JWT를 쿠키로 브라우저에 전달
        Cookie cookie = new Cookie("accessToken", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬 개발 시 false, 배포 시 true
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(cookie);

        // 5. 프론트 페이지로 리다이렉트
        response.sendRedirect("http://localhost:8086/profile");
    }
}
