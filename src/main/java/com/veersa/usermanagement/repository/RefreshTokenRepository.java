package com.veersa.usermanagement.repository;

import com.veersa.usermanagement.domain.RefreshToken;
import com.veersa.usermanagement.domain.User;
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

}
