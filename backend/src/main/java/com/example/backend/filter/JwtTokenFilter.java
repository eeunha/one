package com.example.backend.filter;

import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();

        // Context Path: /api
        // 요청 URI: /api/auth/refresh
        if (requestURI.contains("/api/auth/refresh") || requestURI.contains("/api/auth/google/login")) {
            // 이 경로는 JWT 검증을 건너뛰고 바로 다음 필터로 넘겨야 합니다.
            // Spring Security의 .permitAll() 규칙에 따라 처리될 것입니다.
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        Long userId = null;
        String jwt = null;

        System.out.println("JwtTokenFilter - doFilterInternal 진입");
        
        // JWT 토큰 추출
        // HTTP 요청 헤더에서 "Authorization: Bearer <token>" 형식의 토큰을 가져옵니다.
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);

            try {
                // 토큰 유효성 검사
                Claims claims = jwtUtil.parseClaims(jwt);
                System.out.println("토큰 유효성 검사 완료");

                // 클레임에서 사용자 아이디 가져오기
                userId = (Long) Long.parseLong(claims.getSubject());

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userService.loadUserByUsername(String.valueOf(userId));

                    System.out.println("인증 객체 생성");

                    // 유효한 토큰인 경우, 인증 객체를 생성합니다.
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Spring Security 컨텍스트에 인증 정보를 설정합니다.
                    // 이제 이 요청은 인증된 상태로 처리됩니다.
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }

            } catch (ExpiredJwtException e) {
                // Access Token 만료 시
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired JWT token");
                return;
            } catch (JwtException e) {
                // 위조 등 기타 오류
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
        }

        // 다음 필터로 요청을 넘깁니다.
        filterChain.doFilter(request, response);
    }
}
