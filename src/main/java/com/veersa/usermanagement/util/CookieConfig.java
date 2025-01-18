//package com.veersa.usermanagement.util;
//
//import com.veersa.usermanagement.security.JwtServiceConfiguration;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseCookie;
//import org.springframework.stereotype.Component;
//
//import java.time.Duration;
//
//@Component
//@RequiredArgsConstructor
//public class CookieConfig {
//
//    private final JwtServiceConfiguration jwtService;
//
//    public ResponseCookie createRefreshCookie(String token) {
//        return ResponseCookie.from("refreshToken", token)
//                .httpOnly(true)
//                .secure(true)
//                .sameSite("Strict")
//                .maxAge(Duration.ofDays(7))
//                .path("/auth/refresh")
//                .build();
//    }
//
//    public ResponseCookie clearRefreshCookie() {
//        return ResponseCookie.from("refreshToken", "")
//                .httpOnly(true)
//                .secure(true)
//                .sameSite("Strict")
//                .maxAge(0)
//                .path("/auth/refresh")
//                .build();
//    }
//}
//
