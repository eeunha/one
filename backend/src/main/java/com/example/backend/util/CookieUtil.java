package com.example.backend.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public void addJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS 환경에서는 true로 변경
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일

        response.addCookie(cookie);
    }

    /**
     * 특정 쿠키를 즉시 만료시킵니다.
     * @param response HttpServletResponse
     * @param name 삭제할 쿠키 이름
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