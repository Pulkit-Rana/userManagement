package com.veersa.usermanagement.repository;

import com.veersa.usermanagement.domain.RefreshToken;
import com.veersa.usermanagement.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserOrderByExpiryDateDesc(User user);
    List<RefreshToken> findByUser(User user);

}
