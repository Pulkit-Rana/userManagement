package com.syncNest.user_management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation errors (HTTP 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        return Map.of(
                "status", "error",
                "code", HttpStatus.BAD_REQUEST.value(),
                "message", "Bad Request: Validation failed. Please check your input.",
                "errors", fieldErrors
        );
    }

    // Handle illegal argument exceptions (HTTP 400)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Map.of(
                "status", "error",
                "code", HttpStatus.BAD_REQUEST.value(),
                "message", "Bad Request: " + ex.getMessage()
        );
    }

    // Handle Spring Security authentication errors (HTTP 401)
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleAuthenticationException(AuthenticationException ex) {
        return Map.of(
                "status", "error",
                "code", HttpStatus.UNAUTHORIZED.value(),
                "message", "Unauthorized: " + ex.getMessage()
        );
    }

    // Handle access denied errors (HTTP 403)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAccessDeniedException(AccessDeniedException ex) {
        return Map.of(
                "status", "error",
                "code", HttpStatus.FORBIDDEN.value(),
                "message", "Forbidden: " + ex.getMessage()
        );
    }

    // Handle runtime exceptions (HTTP 400 or other client errors)
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleRuntimeException(RuntimeException ex) {
        return Map.of(
                "status", "error",
                "code", HttpStatus.BAD_REQUEST.value(),
                "message", "Bad Request: " + ex.getMessage()
        );
    }

    // Catch-all for any other exceptions (HTTP 500)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGenericException(Exception ex) {
        return Map.of(
                "status", "error",
                "code", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "message", "Internal Server Error: " + ex.getMessage()
        );
    }
}
