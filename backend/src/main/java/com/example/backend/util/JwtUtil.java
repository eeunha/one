package com.example.backend.util;

import com.example.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenValidityInSeconds;
    private final long refreshTokenValidityInSeconds;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds
    ) {
        // 시크릿 키는 UTF-8 바이트 배열을 기반으로 HMAC SHA 키로 안전하게 변환됩니다.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInSeconds = accessTokenValidityInSeconds;
        this.refreshTokenValidityInSeconds = refreshTokenValidityInSeconds;
    }

    private String createToken (Long id, String role, long validityInSeconds) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(id));

        claims.put("role", role);

        long nowMillis = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(nowMillis + validityInSeconds * 1000))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(User user) {
        System.out.println("JwtUtil - generateAccessToken 메소드 진입");
        System.out.println("accessTokenValidity: " + accessTokenValidityInSeconds);

        return createToken(user.getId(), user.getRole(), accessTokenValidityInSeconds);
    }

    public String generateRefreshToken(User user) {
        System.out.println("JwtUtil - generateRefreshToken 메소드 진입");
        System.out.println("refreshTokenValidity: " + refreshTokenValidityInSeconds);

        return createToken(user.getId(), user.getRole(), refreshTokenValidityInSeconds);
    }

    public Claims parseClaims(String token) {
        System.out.println("JwtUtil - parseClaims 메소드 진입");
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw e;
        }
    }

    /**
     * 토큰에서 사용자 Id을 추출합니다.
     * @param token 검증할 JWT
     * @return 사용자 Id 숫자
     */
    public Long getUserIdFromToken(String token) {
        System.out.println("JwtUtil - getUserIdFromToken 메소드 진입");

        // Claims 파싱 시 예외가 발생할 수 있으므로 parseClaims를 사용
        String subject = parseClaims(token).getSubject();// 👈 파싱 로직 재활용

        return Long.parseLong(subject);
    }

    /**
     * 토큰에서 사용자 Role을 추출합니다.
     * @param token 검증할 JWT
     * @return 사용자 Role 문자열
     */
    public String getRoleFromToken(String token) {
        System.out.println("JwtUtil - getRoleFromToken 메소드 진입");

        // Claims 파싱 시 예외가 발생할 수 있으므로 parseClaims를 사용
        // get("role", String.class)를 사용하여 Claims에서 role 속성을 String 타입으로 안전하게 가져옵니다.
        return parseClaims(token).get("role", String.class);
    }
}
