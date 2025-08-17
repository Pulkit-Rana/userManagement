package com.syncNest.user_management.repository;

import com.syncNest.user_management.domain.UserOAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOAuthRepository extends JpaRepository<UserOAuth, Long> {

    // Find OAuth record by provider + provider user ID (Google sub, Apple sub, etc.)
    Optional<UserOAuth> findByProviderAndProviderUserId(String provider, String providerUserId);

    // Find all OAuth records linked to a specific user
    java.util.List<UserOAuth> findByUserId(Long userId);

    // Optional: if you want only Google OAuth for a user
    Optional<UserOAuth> findByUserIdAndProvider(Long userId, String provider);
}

