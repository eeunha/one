package com.example.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserWithdrawnException extends RuntimeException {
    public UserWithdrawnException(String message) {
        super(message);
    }
}
