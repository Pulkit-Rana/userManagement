package com.syncNest.user_management.service;

import com.syncNest.user_management.domain.Profile;
import com.syncNest.user_management.domain.RefreshToken;
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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public User register(RegistrationRequestDTO registrationDTO) {
        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("No Such Role as USER. "));
        var user = User.builder()
                .username(registrationDTO.getUsername())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .enabled(false)
                .isVerified(false)
                // Explicitly set to unlocked
                .roles(Set.of(userRole))
                .build();
        //sendVerificationEmail(user);
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
        User user = (User) auth.getPrincipal();
        var refreshToken = refreshTokenService.createRefreshToken(request.getUsername());
        var jwtToken = jwtService.generateToken(request.getUsername());

        UserDTO userDTO = mapToUserDTO(user);

        return LoginResponseDTO.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .user(userDTO) // ✅ Include user info in response
                .build();
    }

    public User activateUser(String email) {
        User user = userRepository.findByUsername(email).orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        user.setVerified(true);
        return userRepository.save(user);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. Extract access token
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String accessToken = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
            username = jwtService.extractUsername(accessToken);
            blacklistService.addToBlacklist(accessToken);
            log.info("Blacklisted access token: {}", accessToken);
        }

        // 2. Delete refresh tokens by username (best effort cleanup)
        if (username == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        String refreshToken = cookie.getValue();
                        Optional<RefreshToken> token = refreshTokenService.findByToken(refreshToken);
                        if (token.isPresent()) {
                            username = token.get().getUser().getUsername();
                            refreshTokenService.deleteByToken(refreshToken);
                            log.info("Deleted refresh token from cookie.");
                        }
                    }
                }
            }
        }

        if (username != null) {
            userRepository.findByUsername(username).ifPresent(user -> {
                refreshTokenService.deleteByUserId(user.getId());
                log.info("Deleted refresh tokens for user {}", user.getUsername());
            });
        }

        // Expire the cookie
        Cookie expiredCookie = new Cookie("refreshToken", null);
        expiredCookie.setPath("/");
        expiredCookie.setHttpOnly(true);
        expiredCookie.setSecure(true);
        expiredCookie.setMaxAge(0);
        expiredCookie.setAttribute("SameSite", "strict");
        response.addCookie(expiredCookie);
    }


    private UserDTO mapToUserDTO(User user) {
        return getUserDTO(user);
    }

    private UserDTO getUserDTO(User user) {
        Profile profile = user.getProfile();
        UserProfileDTO profileDTO = null;
        if (profile != null) {
            profileDTO = UserProfileDTO.builder()
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .profilePictureUrl(profile.getProfilePictureUrl())
                    .build();
        }
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles().stream()
                        .map(UserRole::getName)
                        .collect(Collectors.toSet()))
                .profile(profileDTO)
                .build();
    }

}
