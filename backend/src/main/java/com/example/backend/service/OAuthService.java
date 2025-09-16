package com.example.backend.service;

import com.example.backend.dto.JwtAndProfileResponseDTO;
import com.example.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class OAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient;
    private final UserService userService;

    public OAuthService(UserService userService, WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.userService = userService;
    }

    /**
     * Google로부터 받은 인증 코드를 처리하고, JWT와 프로필 정보를 반환합니다.
     * @param code Google에서 받은 인증 코드
     * @return JWT와 프로필 정보가 담긴 DTO
     */
    public JwtAndProfileResponseDTO getJwtAndProfileResponse(String code) {
        
        System.out.println("getJwtAndProfileResponse 진입");
        
        // 1. code -> access token 교환 (WebClient 사용)
        String tokenUrl = "https://oauth2.googleapis.com/token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        Map<String, String> tokenResponse = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block();

        String accessTokenFromGoogle = tokenResponse.get("access_token");

        // 2. access_token -> Google API로 사용자 정보 가져오기 (WebClient 사용)
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
        Map<String, Object> userInfo = webClient.get()
                .uri(userInfoUrl)
                .headers(headers -> headers.setBearerAuth(accessTokenFromGoogle))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        // 3. UserService를 호출하여 두 개의 JWT 토큰을 Map 형태로 받아서 반환
        return userService.processGoogleLogin(email, name);
    }
}