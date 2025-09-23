package com.example.backend.exception;

public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
