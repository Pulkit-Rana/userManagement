package com.syncNest.user_management.repository;

import com.syncNest.user_management.domain.RefreshToken;
import com.syncNest.user_management.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserOrderByExpiryDateDesc(User user);
    List<RefreshToken> findByUser(User user);

    void deleteByUser_Username(String userUsername);

    void deleteByToken(String token);

    void deleteByUserId(UUID userId);
}
