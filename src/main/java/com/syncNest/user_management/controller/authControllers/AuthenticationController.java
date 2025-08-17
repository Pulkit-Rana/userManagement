package com.syncNest.user_management.controller.authControllers;

import com.syncNest.user_management.domain.User;
import com.syncNest.user_management.exception.TokenRefreshException;
import com.syncNest.user_management.modal.*;
import com.syncNest.user_management.security.JwtServiceConfiguration;
import com.syncNest.user_management.service.AuthService;
import com.syncNest.user_management.service.OtpService;
import com.syncNest.user_management.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private boolean isProd() {
        return "prod".equalsIgnoreCase(activeProfile);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO loginResponse = authService.authenticate(loginRequest); // Throws BadCredentialsException if invalid

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(isProd())
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 60 * 48) // 2 days
                .build();

        LoginResponseDTO responseBody = LoginResponseDTO.builder()
                .accessToken(loginResponse.getAccessToken())
                .expiresIn(jwtService.getTokenValiditySeconds())
                .tokenType("Bearer")
                .user(loginResponse.getUser())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(responseBody);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<GenericSuccessResponseDTO> register(@RequestBody @Valid RegistrationRequestDTO registrationDTO) {
        User registeredUser = authService.register(registrationDTO);
        otpService.generateAndSendOtp(registrationDTO.getUsername());

        URI location = URI.create("/users/" + registeredUser.getId());

        return ResponseEntity.created(location).body(
                new GenericSuccessResponseDTO("OTP sent to email. Please verify.", Map.of(
                        "id", registeredUser.getId(),
                        "username", registeredUser.getUsername()
                ))
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<LoginResponseDTO> verifyOtp(@RequestBody @Valid OtpVerifyDTO dto) {
        // 1. OTP verification & login is delegated to AuthService (clean separation)
        LoginResponseDTO loginResponse = authService.verifyOtpAndLogin(dto);

        // 2. Set HttpOnly secure refreshToken cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(60 * 60 * 48)
                .build();

        // 3. Return accessToken in body and refreshToken in cookie
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(
                        LoginResponseDTO.builder()
                                .accessToken(loginResponse.getAccessToken())
                                .tokenType("Bearer")
                                .expiresIn(loginResponse.getExpiresIn())
                                .user(loginResponse.getUser())
                                .build()
                );
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO dto) {
        return refreshTokenService.findByToken(dto.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(rotatedToken -> {
                    String accessToken = jwtService.generateToken(rotatedToken.getUser().getUsername());

                    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", rotatedToken.getToken())
                            .httpOnly(true)
                            .secure(isProd())
                            .path("/")
                            .sameSite("Strict")
                            .maxAge(60 * 60 * 48)
                            .build();

                    RefreshTokenResponseDTO response = new RefreshTokenResponseDTO(
                            accessToken,
                            null,
                            jwtService.getTokenValiditySeconds(),
                            "Bearer"
                    );

                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                            .body(response);
                })
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found or expired"));
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericSuccessResponseDTO> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(request, response);

        return ResponseEntity.ok(
                new GenericSuccessResponseDTO("Logged out successfully", null)
        );
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf() {
        return ResponseEntity.ok().build();
    }
}
