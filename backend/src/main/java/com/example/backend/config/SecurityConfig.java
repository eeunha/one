package com.example.backend.config;

import com.example.backend.filter.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    public SecurityConfig(JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS 설정을 적용합니다.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. CSRF 보호를 비활성화합니다.
                .csrf(AbstractHttpConfigurer::disable)

                // 3. 세션 관리를 STATELESS로 설정합니다. JWT를 사용하므로 세션은 필요하지 않습니다.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. JWT 필터를 추가합니다.
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // 1. Refresh 및 로그인/가입은 인증 없이 허용 (가장 높은 권한)
                        .requestMatchers("/auth/refresh", "/auth/google/login").permitAll()

                        // 2. /auth/profile과 같은 나머지 /auth 경로는 인증 필요
                        //    (토큰이 유효해야만 접근 가능)
                        .requestMatchers("/auth/profile", "/auth/logout").authenticated()

                                // 나머지 모든 요청은 인증이 필요합니다.
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    // CORS 설정을 위한 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 주소만 허용합니다.
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8086"));
        // 모든 HTTP 메서드 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // `withCredentials: true` 요청을 허용합니다.
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}