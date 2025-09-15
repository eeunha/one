package com.example.backend.config;

import com.example.backend.dto.JwtAndProfileResponseDTO;
import com.example.backend.service.UserService;
import com.example.backend.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final UserService userService;
    private final CookieUtil cookieUtil; // ★ CookieUtil 주입

    public SecurityConfig(UserService userService, CookieUtil cookieUtil) {
        this.userService = userService;
        this.cookieUtil = cookieUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ★ CORS 설정을 Security에 통합
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth/**", "/auth/**", "/login/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler())
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/oauth2/authorization")
                        )
                        .redirectionEndpoint(endpoint -> endpoint
                                .baseUri("/login/oauth2/code/*")
                        )
                );
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");

            // 1. UserService를 호출하여 JwtAndProfileResponse 객체를 받음
            JwtAndProfileResponseDTO jwtAndProfileResponse = userService.processGoogleLogin(email, name);

            String accessToken = jwtAndProfileResponse.getAccessToken();
            String refreshToken = jwtAndProfileResponse.getRefreshToken();

            // 2. 두 개의 토큰을 쿠키에 담는 새로운 메서드를 호출
            cookieUtil.addJwtCookies(response, accessToken, refreshToken);

            // 3. (선택사항) 프로필 정보를 세션에 저장하거나 리다이렉트 시 쿼리 파라미터로 추가
            // 현재는 쿠키만 추가하고 리다이렉트하는 방식이므로 추가적인 로직은 필요 없습니다.

            response.sendRedirect("http://localhost:8086/profile");
        };
    }

    // ★ CORS 설정 Bean을 SecurityConfig에 추가
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8086")); // 허용할 프론트 주소
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}