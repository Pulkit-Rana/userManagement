package com.syncNest.user_management.service;

import com.syncNest.user_management.domain.RefreshToken;
import com.syncNest.user_management.domain.User;
import com.syncNest.user_management.exception.TokenRefreshException;
import com.syncNest.user_management.repository.RefreshTokenRepository;
import com.syncNest.user_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${refresh-token.expiration.milliseconds}") // 30 days
    private long refreshTokenDurationMs;

    @Value("${refresh-token.max.count}")
    private int refreshTokenMaxCount;

    @Transactional
    // Create a new refresh token for a user (MySQL)
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User with username '" + username + "' not found"
                ));
        // Ensure the user doesn't exceed the maximum allowed tokens
        List<RefreshToken> userTokens = refreshTokenRepository.findByUserOrderByExpiryDateDesc(user);
        if (userTokens.size() >= refreshTokenMaxCount) {
            RefreshToken oldestToken = userTokens.get(0);
            refreshTokenRepository.delete(oldestToken);
            log.info("Deleted oldest refresh token with ID: {}", oldestToken.getId());
        }
        String tokenValue = String.valueOf(UUID.randomUUID());
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Verify if a refresh token is expired, delete if expired
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            log.info("Deleting expired refresh token: {}", token.getId());
            refreshTokenRepository.delete(token);  // Delete from MySQL if expired
            throw new TokenRefreshException(token.getToken(), "Refresh token has expired. Please log in again.");
        }
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        token.setToken(UUID.randomUUID().toString()); // rotate value
        return refreshTokenRepository.save(token);
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteByToken(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }
}
