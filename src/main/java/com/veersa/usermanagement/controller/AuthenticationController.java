package com.veersa.usermanagement.controller;

import com.veersa.usermanagement.config.ResponseConfig;
import com.veersa.usermanagement.domain.RefreshToken;
import com.veersa.usermanagement.domain.User;
import com.veersa.usermanagement.modal.LoginRequestDTO;
import com.veersa.usermanagement.modal.LoginResponseDTO;
import com.veersa.usermanagement.modal.RefreshTokenRequestDTO;
import com.veersa.usermanagement.modal.RegistrationDTO;
import com.veersa.usermanagement.security.JwtServiceConfiguration;
import com.veersa.usermanagement.service.RefreshTokenService;
import com.veersa.usermanagement.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtServiceConfiguration jwtService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO loginResponse = authService.authenticate(loginRequest);
        return ResponseConfig.buildSuccessResponse("Login successful", loginResponse, null);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> register(@RequestBody @Valid RegistrationDTO registrationDTO) {
        User registeredUser = authService.register(registrationDTO);

        Map<String, Object> userData = Map.of(
                "id", registeredUser.getId(),
                "username", registeredUser.getUsername()
        );

        URI location = URI.create("/users/" + registeredUser.getId());
        return ResponseConfig.buildSuccessResponse("User registered successfully", userData, location);
    }

    @PostMapping("/refreshToken")
    public LoginResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return refreshTokenService.findByToken(refreshTokenRequestDTO.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(userInfo -> {
                    String accessToken = jwtService.generateToken(userInfo.getUsername());
                    return LoginResponseDTO.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenRequestDTO.getToken()).build();
                }).orElseThrow(() -> new RuntimeException("Refresh Token is not in DB..!!"));
    }


}