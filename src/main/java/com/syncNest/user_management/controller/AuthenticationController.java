package com.syncNest.user_management.controller;

import com.syncNest.user_management.domain.User;
import com.syncNest.user_management.modal.*;
import com.syncNest.user_management.security.JwtServiceConfiguration;
import com.syncNest.user_management.security.TokenBlacklistServiceConfiguration;
import com.syncNest.user_management.service.AuthService;
import com.syncNest.user_management.service.OtpService;
import com.syncNest.user_management.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {

    private final AuthService authService;
    private final OtpService otpService;
    private final RefreshTokenService refreshTokenService;
    private final JwtServiceConfiguration jwtService;
    private final TokenBlacklistServiceConfiguration blacklistService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO loginResponse = authService.authenticate(loginRequest);
        // 🍪 Securely attach refreshToken in HttpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("strict")
                .maxAge(60 * 60 * 48)
                .build();
        // Return only accessToken in body
        LoginResponseDTO responseBody = LoginResponseDTO.builder()
                .accessToken(loginResponse.getAccessToken())
                .expiresIn(jwtService.getTokenValiditySeconds()) // ⬅️ You'll define this
                .tokenType("Bearer")
                .user(loginResponse.getUser())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(responseBody);
    }


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> register(@RequestBody @Valid RegistrationRequestDTO registrationDTO) {
        User registeredUser = authService.register(registrationDTO);

        otpService.generateAndSendOtp(registrationDTO.getUsername());

        Map<String, Object> userData = Map.of(
                "id", registeredUser.getId(),
                "username", registeredUser.getUsername(),
                "message", "OTP sent to email. Please verify."
        );

        URI location = URI.create("/users/" + registeredUser.getId());
        return ResponseEntity.created(location).body(userData);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody @Valid OtpVerifyDTO dto) {
        boolean isValid = otpService.verifyOtp(dto.getEmail(), dto.getOtp());
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid or expired OTP"));
        }

        User user = authService.activateUser(dto.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP verified and account activated", "user", user));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return refreshTokenService.findByToken(refreshTokenRequestDTO.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(rotatedToken -> {
                    String accessToken = jwtService.generateToken(rotatedToken.getUser().getUsername());

                    // 🍪 Attach rotated refresh token as HttpOnly cookie
                    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", rotatedToken.getToken())
                            .httpOnly(true)
                            .secure(true) // ⚠️ Set to true in prod
                            .path("/")
                            .sameSite("strict")
                            .maxAge(60 * 60 * 48)
                            .build();

                    RefreshTokenResponseDTO responseBody = new RefreshTokenResponseDTO(
                            accessToken,
                            null, // ❌ Don't expose refreshToken in body
                            jwtService.getTokenValiditySeconds(),
                            "Bearer"
                    );

                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                            .body(responseBody);
                })
                .orElseThrow(() -> new RuntimeException("Refresh Token is not in DB..!!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(name = "Authorization", required = false) String accessToken,
            HttpServletRequest request,
            HttpServletResponse response) {
        // Use existing logout service logic
        authService.logout(request, response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/csrf")
    public ResponseEntity<?> csrf() {
        return ResponseEntity.ok().build();
    }
}