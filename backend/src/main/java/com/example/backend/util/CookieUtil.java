package com.example.backend.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public void addJwtCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)        // JS에서 접근 불가
//                .secure(true)          // HTTPS에서만 전송
                .path("/")
                .maxAge(15 * 60)       // 15분
                .sameSite("Lax")       // 일부 안전한 요청에서만 전송
                .build();

        // Refresh Token
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)        // JS 접근 불가
//                .secure(true)          // HTTPS에서만 전송
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7일
                .sameSite("Strict")    // CSRF 방지
                .build();

        // 응답 헤더에 추가
        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }

    /**
     * 특정 쿠키를 즉시 만료시킵니다.
     *
     * @param response HttpServletResponse
     * @param name     삭제할 쿠키 이름
     */
    public void expireCookie(HttpServletResponse response, String name, boolean isSecure) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(isSecure); // HTTPS 환경에서만 전송
        cookie.setPath("/");
        cookie.setMaxAge(0); // MaxAge를 0으로 설정하여 즉시 만료

        response.addCookie(cookie);
    }
}