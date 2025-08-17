package com.syncNest.user_management.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
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

    // ✅ Field validation (register, login, OTP inputs, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        return Map.of(
                "status", "error",
                "code", 400,
                "message", "Validation failed. Please correct the fields.",
                "errors", fieldErrors
        );
    }

    // ✅ Duplicate key (e.g. email already exists during registration)
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            return Map.of(
                    "status", "error",
                    "code", 400,
                    "message", "Email already registered."
            );
        }
        return Map.of(
                "status", "error",
                "code", 400,
                "message", "Database integrity violation",
                "details", message
        );
    }

    // ✅ Illegal arguments (used by register service, OTP service, etc.)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Map.of(
                "status", "error",
                "code", 400,
                "message", ex.getMessage()
        );
    }

    // ✅ Constraint violations (e.g. from manual validator calls)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConstraintViolationException(ConstraintViolationException ex) {
        return Map.of(
                "status", "error",
                "code", 400,
                "message", "Constraint violation",
                "details", ex.getMessage()
        );
    }

    // ✅ Spring Security: login errors
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleAuthenticationException(AuthenticationException ex) {
        String msg = ex.getMessage();
        if (msg.contains("Bad credentials")) {
            return Map.of(
                    "status", "error",
                    "code", 401,
                    "message", "Invalid email or password."
            );
        } else if (msg.contains("User is disabled")) {
            return Map.of(
                    "status", "error",
                    "code", 401,
                    "message", "Account is disabled. Please verify OTP or contact support."
            );
        } else if (msg.contains("User account is locked")) {
            return Map.of(
                    "status", "error",
                    "code", 401,
                    "message", "Account is locked. Try again later."
            );
        }

        return Map.of(
                "status", "error",
                "code", 401,
                "message", "Authentication failed"
        );
    }

    // ✅ Forbidden access (e.g. trying to verify someone else’s OTP)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAccessDeniedException(AccessDeniedException ex) {
        return Map.of(
                "status", "error",
                "code", 403,
                "message", "Access denied"
        );
    }

    // ✅ Runtime-level failure (fallback for client-side known issues)
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleRuntimeException(RuntimeException ex) {
        return Map.of(
                "status", "error",
                "code", 400,
                "message", ex.getMessage()
        );
    }

    // ✅ Catch-all fallback (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGenericException(Exception ex) {
        return Map.of(
                "status", "error",
                "code", 500,
                "message", "Something went wrong",
                "details", ex.getMessage()
        );
    }
}
