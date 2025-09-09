package com.example.backend.controller;

import com.example.backend.dto.LoginResponseDto;
import com.example.backend.dto.SnsLoginRequestDto;
import com.example.backend.dto.UserDTO;
import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
    public LoginResponseDto googleCallback(@RequestParam("code") String code) {

        // 1. code -> access token 교환
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, params, Map.class);
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2. access_token -> 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userInfoResponse  = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                entity,
                Map.class
        );

        String email = (String) userInfoResponse.getBody().get("email");
        String name = (String) userInfoResponse.getBody().get("name");

        // 3. User DB 저장 및 JWT 발급
        return userService.processGoogleLogin(email, name);
    }
}
