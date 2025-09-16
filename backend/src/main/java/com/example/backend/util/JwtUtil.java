package com.example.backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenValidity,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenValidity
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public String generateAccessToken(Long id) {
        return Jwts.builder()
                // id를 String으로 변환하여 subject에 담습니다.
                .setSubject(String.valueOf(id)) //JWT의 주체. 사용자 식별자
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long id) {
        return Jwts.builder()
                // id를 String으로 변환하여 subject에 담습니다.
                .setSubject(String.valueOf(id))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            // 잘못된 JWT 서명
            System.err.println("Invalid JWT Signature: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            // 만료된 JWT 토큰
            System.err.println("Expired JWT Token: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 JWT 토큰
            System.err.println("Unsupported JWT Token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // JWT Claims 문자열이 비어있음
            System.err.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        // 토큰의 Subject에서 사용자 ID를 Long 타입으로 변환하여 반환
        String subject = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        return Long.parseLong(subject);
    }

    // id에서 가져오는 걸로 수정할 것
    // 토큰에서 email 정보 추출
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
