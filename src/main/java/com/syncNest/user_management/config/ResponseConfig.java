package com.syncNest.user_management.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ResponseConfig {
    public static ResponseEntity<Map<String, Object>> buildSuccessResponse(String message, Object data, URI location) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        response.put("data", data);

        if (location != null) {
            return ResponseEntity.created(location).body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    public static ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}