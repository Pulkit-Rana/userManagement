package com.veersa.usermanagement.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceConfiguration {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtServiceConfiguration jwtService;

    public void addToBlacklist(String token) {
        if ((token == null) || jwtService.isTokenExpired(token)) {
            return; // Skip invalid or expired tokens
        }
        Date expiry = jwtService.extractExpiration(token);
        long expiration = expiry.getTime() - System.currentTimeMillis();
        if (expiration > 0) {
            redisTemplate.opsForValue().set(token, "blacklisted", expiration, TimeUnit.MILLISECONDS);
        }
    }

    public Boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
