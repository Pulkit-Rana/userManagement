// OtpService.java
package com.syncNest.user_management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String OTP_PREFIX = "OTP:";
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(5);
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    public void generateAndSendOtp(String email) {
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        String hashedOtp = Sha512DigestUtils.shaHex(otp);
        redisTemplate.opsForValue().set(OTP_PREFIX + email, hashedOtp, OTP_EXPIRY);
        emailService.sendOtp(email, otp);
    }

    public void verifyAndConsumeOtpOrThrow(String email, String enteredOtp) {
        String redisKey = OTP_PREFIX + email;
        Object storedHash = redisTemplate.opsForValue().get(redisKey);

        if (storedHash == null) {
            throw new IllegalArgumentException("OTP expired or invalid.");
        }

        String hashedEnteredOtp = Sha512DigestUtils.shaHex(enteredOtp);
        if (!hashedEnteredOtp.equals(storedHash.toString())) {
            throw new IllegalArgumentException("Incorrect OTP.");
        }

        redisTemplate.delete(redisKey); // consume OTP after verification
    }

    public boolean isOtpVerified(String email) {
        return !redisTemplate.hasKey(OTP_PREFIX + email);
    }
}
