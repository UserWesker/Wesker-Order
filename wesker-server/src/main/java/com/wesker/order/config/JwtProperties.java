package com.wesker.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties(prefix = "wesker.jwt")
public record JwtProperties(String adminSecretKey, Duration adminTtl) {

    public JwtProperties {
        if (adminSecretKey == null || adminSecretKey.isBlank()
                || adminSecretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("Set WESKER_JWT_SECRET to a secret of at least 32 bytes");
        }
        if (adminTtl == null || adminTtl.isZero() || adminTtl.isNegative()) {
            throw new IllegalArgumentException("JWT lifetime must be positive");
        }
    }
}
