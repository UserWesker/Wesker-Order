package com.wesker.order.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Duration;
import java.time.Instant;

public final class JwtUtil {

    private static final String ADMIN_ID_CLAIM = "adminId";

    private JwtUtil() {
    }

    public static String createJWT(String secretKey, Duration ttl, long adminId) {
        if (adminId <= 0) {
            throw new IllegalArgumentException("无效的登录身份");
        }
        Instant now = Instant.now();
        return JWT.create()
                .withClaim(ADMIN_ID_CLAIM, adminId)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(ttl))
                .sign(Algorithm.HMAC256(secretKey));
    }

    public static long verifyAdminId(String secretKey, String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secretKey))
                .withClaimPresence("exp")
                .withClaimPresence(ADMIN_ID_CLAIM)
                .build()
                .verify(token);
        Long adminId = jwt.getClaim(ADMIN_ID_CLAIM).asLong();
        if (adminId == null || adminId <= 0) {
            throw new JWTVerificationException("无效的登录身份");
        }
        return adminId;
    }
}
