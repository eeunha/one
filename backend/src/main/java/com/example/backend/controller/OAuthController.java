package com.example.backend.controller;

import com.example.backend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;

@Controller // @RestController 대신 @Controller 사용 (리다이렉션을 위해)
@RequestMapping("/oauth")
public class OAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final UserService userService;
    private final WebClient webClient; // WebClient 주입

    // 생성자에서 WebClient.Builder를 주입받아 초기화 (Best Practice)
    public OAuthController(UserService userService, WebClient.Builder webClientBuilder) {
        this.userService = userService;
        this.webClient = webClientBuilder.build();
    }

    @GetMapping("/callback/google")
    public String googleCallback(@RequestParam("code") String code, HttpServletResponse httpServletResponse) throws IOException {

        // 1. code -> access token 교환 (WebClient 사용)
        String tokenUrl = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        // WebClient를 사용한 POST 요청
        Map<String, String> tokenResponse = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block(); // 비동기 응답을 동기적으로 기다림

        String accessTokenFromGoogle = tokenResponse.get("access_token");

        // 2. access_token -> Google API로 사용자 정보 가져오기 (WebClient 사용)
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        // WebClient를 사용한 GET 요청
        Map<String, Object> userInfo = webClient.get()
                .uri(userInfoUrl)
                .headers(headers -> headers.setBearerAuth(accessTokenFromGoogle))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

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
        httpServletResponse.addCookie(cookie);

        // 5. 프론트 페이지로 리다이렉트 (redirect: 접두어 사용)
        return "redirect:http://localhost:8086/profile";
    }
}