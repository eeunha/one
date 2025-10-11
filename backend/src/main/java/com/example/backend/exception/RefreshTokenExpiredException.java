package com.example.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Refresh Token이 만료되거나 유효하지 않아 재로그인이 필요한 경우 발생하는 예외
 * GlobalExceptionHandler에서 HTTP 401 Unauthorized로 처리됩니다.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
