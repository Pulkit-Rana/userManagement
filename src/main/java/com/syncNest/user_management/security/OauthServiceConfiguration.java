package com.syncNest.user_management.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.syncNest.user_management.domain.User;
import com.syncNest.user_management.domain.UserRole;
import com.syncNest.user_management.repository.RoleRepository;
import com.syncNest.user_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OauthServiceConfiguration {
    private final UserRepository userRepository;
    private final RoleRepository userRoleRepository;
    private final JwtServiceConfiguration jwtServiceConfiguration;

    private final String CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID"; // TODO: replace with real client id

    public String processGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(CLIENT_ID)).build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.error("Invalid ID token.");
                throw new IllegalArgumentException("Invalid ID token.");
            }

            Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            // Check if user already exists
            Optional<User> existingUser = userRepository.findByUsername(email);
            User user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
            } else {
                // Default role USER assign
                UserRole userRole = userRoleRepository.findByName("USER")
                        .orElseThrow(() -> new RuntimeException("Default role USER not found."));

                user = User.builder()
                        .username(email)
                        .roles(Collections.singleton(userRole))
                        .build();

                user = userRepository.save(user);
            }

            // Generate JWT token
            return jwtServiceConfiguration.generateToken(user.getUsername());

        } catch (Exception e) {
            log.error("Google token processing failed: {}", e.getMessage(), e);
            throw new RuntimeException("Google authentication failed", e);
        }
    }
}
