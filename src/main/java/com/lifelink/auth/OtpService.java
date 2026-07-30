package com.lifelink.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private static final String OTP_PREFIX = "OTP_";
    private static final long OTP_EXPIRATION_MINUTES = 5;

    public void sendOtp(String phone) {
        String otp = generateOtp();
        String key = OTP_PREFIX + phone;
        
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
        
        // In a real scenario, integrate with Twilio or another SMS gateway here.
        log.info("MOCK SMS: Your LifeLink OTP is: {} (Sent to {})", otp, phone);
    }

    public boolean verifyOtp(String phone, String otp) {
        String key = OTP_PREFIX + phone;
        String storedOtp = redisTemplate.opsForValue().get(key);
        
        if (storedOtp != null && storedOtp.equals(otp)) {
            // OTP is valid, remove it so it cannot be reused
            redisTemplate.delete(key);
            return true;
        }
        
        return false;
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate 6-digit OTP
        return String.valueOf(otp);
    }
}
