package com.example.backend.config;

import com.example.backend.exception.RefreshTokenExpiredException;
import com.example.backend.exception.UserWithdrawnException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기 (Global Exception Handler)
 * 애플리케이션 내 모든 Controller에서 발생하는 특정 예외들을 한 곳에서 처리합니다.
 * 이를 통해 Controller의 try-catch 블록을 제거하고 코드를 간결하게 유지합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * DTO 유효성 검사 실패 (Validation Error) 처리 - 400 Bad Request
     * @Valid 어노테이션 사용 시 검증 실패하면 이 메서드가 호출되어, 오류 필드와 메시지를 JSON으로 반환합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        // 발생한 모든 오류를 순회하며 필드 이름과 오류 메시지를 추출합니다.
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // 400 Bad Request 상태 코드와 함께 오류 Map을 응답합니다.
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * RefreshTokenExpiredException 처리: 토큰이 만료되어 재로그인이 필요할 때 - 401 Unauthorized
     */
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleRefreshTokenExpiredException(RefreshTokenExpiredException e) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", "토큰이 만료되어 재로그인이 필요합니다.");
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED); // 401
    }

    /**
     * EntityNotFoundException 처리: 데이터베이스에서 리소스를 찾지 못했을 때 - 404 Not Found
     * (Post, Comment, User 등을 찾지 못했을 때 발생)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException e) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", "Not Found");
        errorDetails.put("message", e.getMessage() != null ? e.getMessage() : "요청한 리소스를 찾을 수 없습니다.");
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND); // 404
    }

    /**
     * IllegalArgumentException 처리: 잘못된 인자 또는 권한이 없을 때 - 403 Forbidden / 400 Bad Request
     * (수정/삭제 권한이 없거나, 비즈니스 로직 상 잘못된 인자가 전달되었을 때 발생)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", "Request Error");
        errorDetails.put("message", e.getMessage() != null ? e.getMessage() : "잘못된 요청 인자입니다.");

        // 예외 메시지에 '권한' 관련 단어가 포함되어 있다면 403 (Forbidden)을 반환하여 권한 오류임을 명시합니다.
        if (e.getMessage() != null && e.getMessage().contains("권한")) {
            return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN); // 403
        }

        // 그 외의 IllegalArgumentException은 400 (Bad Request)으로 처리합니다.
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // ⭐️ UserWithdrawnException 처리: 탈퇴 회원 재로그인 시도 - 403 Forbidden ⭐️
    @ExceptionHandler(UserWithdrawnException.class)
    public ResponseEntity<Map<String, String>> handleUserWithdrawnException(UserWithdrawnException e) {
        Map<String, String> errorDetails = new HashMap<>();

        // 프론트엔드가 오류 유형을 명확히 판단하도록 "error" 필드에 특정 코드를 넣습니다.
        errorDetails.put("error", "User Withdrawn");

        // 사용자에게 보여줄 메시지를 그대로 반환합니다.
        errorDetails.put("message", e.getMessage());

        // 403 Forbidden (계정 상태 문제로 인한 접근 금지) 상태 반환
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
}
