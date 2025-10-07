package com.example.backend.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${app.cookie.secure}")
    private boolean secure;

    /**
     * JWT 토큰을 HttpOnly 쿠키에 추가합니다.
     *
     * @param response      HttpServletResponse
     * @param name          쿠키 이름 (e.g., "refreshToken")
     * @param value         쿠키 값 (JWT)
     * @param maxAgeSeconds 쿠키 유효 기간 (초 단위)
     */
    public void addJwtCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        
        System.out.println("CookieUtil - addJwtCookie 진입");
        
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax") // CSRF 방지를 위해 "Strict" 또는 "Lax"를 사용합니다.
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 특정 쿠키를 즉시 만료시킵니다.
     *
     * @param response HttpServletResponse
     * @param name     삭제할 쿠키 이름
     */
    public void expireCookie(HttpServletResponse response, String name) {
        System.out.println("CookieUtil - expireCookie 진입");

        // 쿠키 값을 비우고, maxAge를 0으로 설정하여 즉시 만료시킵니다.
        addJwtCookie(response, name, "", 0);
    }

    /**
     * HttpServletRequest에서 특정 이름의 쿠키 값을 가져옵니다.
     * @param request HttpServletRequest
     * @param name 찾을 쿠키 이름
     * @return 쿠키 값, 없으면 null
     */
    public String getCookieValue(HttpServletRequest request, String name) {
        System.out.println("CookieUtil - getCookieValue 진입");
        System.out.println("getCookieValue, request: " + request);

        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                System.out.println("getCookieValue, cookie: " + cookie);
                return cookie.getValue();
            }
        }
        return null;
    }
}