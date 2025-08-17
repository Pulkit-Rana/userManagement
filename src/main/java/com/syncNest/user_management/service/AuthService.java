package com.syncNest.user_management.service;

import com.syncNest.user_management.domain.AuthProvider;
import com.syncNest.user_management.domain.Profile;
import com.syncNest.user_management.domain.User;
import com.syncNest.user_management.domain.UserRole;
import com.syncNest.user_management.modal.*;
import com.syncNest.user_management.repository.RoleRepository;
import com.syncNest.user_management.repository.UserRepository;
import com.syncNest.user_management.security.JwtServiceConfiguration;
import com.syncNest.user_management.security.TokenBlacklistServiceConfiguration;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtServiceConfiguration jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistServiceConfiguration blacklistService;
    private final OtpService otpService;

    // ===================== REGISTER ======================

    public User register(RegistrationRequestDTO dto) {
        UserRole userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("No such role: USER"));

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .enabled(false)
                .isVerified(false)
                .provider(AuthProvider.LOCAL)
                .roles(Set.of(userRole))
                .build();

        Profile profile = Profile.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .lastLoginDate(LocalDateTime.now())
                .user(user) // ✅ critical: connect Profile -> User
                .build();

        user.setProfile(profile); // ✅ critical: connect User -> Profile

        return userRepository.save(user); // ✅ Cascade saves both
    }

    // ===================== LOGIN ======================

    public LoginResponseDTO authenticate(LoginRequestDTO request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()
                )
        );

        User user = (User) auth.getPrincipal();
        updateLastLogin(user);
        return generateLoginResponse(user);
    }

    public LoginResponseDTO verifyOtpAndLogin(OtpVerifyDTO dto) {
        otpService.verifyAndConsumeOtpOrThrow(dto.getEmail(), dto.getOtp());
        User user = activateUser(dto.getEmail());
        updateLastLogin(user);
        return generateLoginResponse(user);
    }

    private void updateLastLogin(User user) {
        if (user.getProfile() != null) {
            user.getProfile().setLastLoginDate(LocalDateTime.now());
        }
        userRepository.save(user);
    }

    public User activateUser(String email) {
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        user.setVerified(true);
        return userRepository.save(user);
    }

    // ===================== LOGOUT ======================

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = extractAccessToken(request);
        if (token != null && !jwtService.isTokenExpired(token)) {
            blacklistService.addToBlacklist(token);
            log.info("Access token blacklisted.");
        }

        String username = extractUsernameFromRefreshToken(request);
        if (username != null) {
            userRepository.findByUsername(username).ifPresent(user -> {
                refreshTokenService.deleteByUserId(user.getId());
                log.info("Deleted refresh tokens for user {}", username);
            });
        }

        expireRefreshCookie(response);
    }

    // ===================== PRIVATE ======================

    private String extractAccessToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    private String extractUsernameFromRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                String token = cookie.getValue();
                return refreshTokenService.findByToken(token)
                        .map(t -> {
                            refreshTokenService.deleteByToken(token);
                            return t.getUser().getUsername();
                        }).orElse(null);
            }
        }
        return null;
    }

    private void expireRefreshCookie(HttpServletResponse response) {
        Cookie expiredCookie = new Cookie("refreshToken", null);
        expiredCookie.setPath("/");
        expiredCookie.setHttpOnly(true);
        expiredCookie.setSecure(true);
        expiredCookie.setMaxAge(0);
        expiredCookie.setAttribute("SameSite", "strict");
        response.addCookie(expiredCookie);
    }

    private LoginResponseDTO generateLoginResponse(User user) {
        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getTokenValiditySeconds())
                .user(mapToUserDTO(user))
                .build();
    }

    private UserDTO mapToUserDTO(User user) {
        Profile profile = user.getProfile();
        UserProfileDTO profileDTO = (profile != null)
                ? UserProfileDTO.builder()
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .build()
                : new UserProfileDTO();

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles().stream().map(UserRole::getName).collect(Collectors.toSet()))
                .profile(profileDTO)
                .build();
    }
}
