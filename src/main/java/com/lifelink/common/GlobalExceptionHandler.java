package com.lifelink.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalStateException(IllegalStateException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status, String error, String message) {
        ApiError apiError = new ApiError(error, message, LocalDateTime.now());
        return new ResponseEntity<>(apiError, status);
    }
}
