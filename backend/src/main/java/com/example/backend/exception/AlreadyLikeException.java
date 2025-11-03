package com.example.backend.exception;

public class AlreadyLikeException extends RuntimeException {
    public AlreadyLikeException(String message) {
        super(message);
    }
}
