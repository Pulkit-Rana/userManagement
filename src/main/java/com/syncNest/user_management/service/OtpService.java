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
    private static final Duration OTP_EXPIRY = Duration.ofSeconds(60);
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    public void generateAndSendOtp(String email) {
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        String hashedOtp = Sha512DigestUtils.shaHex(otp);

        redisTemplate.opsForValue().set(OTP_PREFIX + email, hashedOtp, OTP_EXPIRY);
        emailService.sendOtp(email, otp); // implement using JavaMailSender
    }

    public boolean verifyOtp(String email, String enteredOtp) {
        String redisKey = OTP_PREFIX + email;
        Object value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) return false;

        String hashedEnteredOtp = Sha512DigestUtils.shaHex(enteredOtp);
        boolean isValid = hashedEnteredOtp.equals(value.toString());

        if (isValid) redisTemplate.delete(redisKey); // one-time use
        return isValid;
    }

    public boolean isOtpVerified(String email) {
        return !redisTemplate.hasKey(OTP_PREFIX + email); // If deleted, assume verified
    }
}
