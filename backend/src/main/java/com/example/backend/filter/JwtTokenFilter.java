package com.example.backend.filter;

import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
        final String authHeader = request.getHeader("Authorization");
        Long userId = null;
        String jwt = null;

        // 1. JWT 토큰 추출
        // HTTP 요청 헤더에서 "Authorization: Bearer <token>" 형식의 토큰을 가져옵니다.
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);

            // 토큰에서 사용자 ID를 추출합니다.
            userId = jwtUtil.getUserIdFromToken(jwt);
        }

        // 2. JWT 토큰 유효성 검사 및 인증
        // 토큰에서 사용자 id를 가져왔고, 아직 인증 정보가 없는 경우에만 실행합니다.
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // id를 이용해 사용자 정보를 로드합니다.
            UserDetails userDetails = userService.loadUserByUsername(String.valueOf(userId));

            // 토큰의 유효성을 검증합니다.
            // 기존에는 UserDetails를 매개변수로 받았지만, JwtUtil에 맞게 수정합니다.
            if (jwtUtil.validateToken(jwt)) {
                // 유효한 토큰인 경우, 인증 객체를 생성합니다.
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Spring Security 컨텍스트에 인증 정보를 설정합니다.
                // 이제 이 요청은 인증된 상태로 처리됩니다.
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 다음 필터로 요청을 넘깁니다.
        filterChain.doFilter(request, response);
    }
}
