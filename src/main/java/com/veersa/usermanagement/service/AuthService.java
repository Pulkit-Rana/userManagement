package com.veersa.usermanagement.service;

import com.veersa.usermanagement.domain.User;
import com.veersa.usermanagement.modal.LoginRequestDTO;
import com.veersa.usermanagement.modal.LoginResponseDTO;
import com.veersa.usermanagement.modal.RegistrationDTO;
import com.veersa.usermanagement.repository.RoleRepository;
import com.veersa.usermanagement.repository.UserRepository;
import com.veersa.usermanagement.security.JwtServiceConfiguration;
import com.veersa.usermanagement.security.TokenBlacklistServiceConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtServiceConfiguration jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistServiceConfiguration blacklistService;

    public User register(RegistrationDTO registrationDTO) {
        var userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Role User Not Initialized"));

        var user = User.builder()
                .username(registrationDTO.getUsername())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .enabled(true) // Explicitly set to enabled
                .isLocked(false)
                // Explicitly set to unlocked
                .roles(Set.of(userRole))
                .build();
        // Save the user to the database
        return userRepository.save(user);
    }

    public LoginResponseDTO authenticate(LoginRequestDTO request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var refreshToken = refreshTokenService.createRefreshToken(request.getUsername());
        var jwtToken = jwtService.generateToken(request.getUsername());
        return LoginResponseDTO.builder()
                .accessToken(jwtToken)
                .token(refreshToken.getToken())
                .build();
    }
}
