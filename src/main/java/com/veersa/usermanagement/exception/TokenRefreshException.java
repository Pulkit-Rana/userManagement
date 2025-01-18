package com.veersa.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN) // 403 Forbidden
public class TokenRefreshException extends RuntimeException {

    /**
     * Constructs a new TokenRefreshException with the specified detail message.
     *
     * @param message the detail message
     */
    public TokenRefreshException(String message) {
        super(message);
    }

    public TokenRefreshException(String token, String s) {
    }
}
