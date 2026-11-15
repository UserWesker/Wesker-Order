package com.wesker.order.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.wesker.order.constant.AccountRole;

import java.time.Duration;
import java.time.Instant;

public final class JwtUtil {

    private static final String ACCOUNT_ID_CLAIM = "accountId";
    private static final String ROLE_CLAIM = "role";

    private JwtUtil() {
    }

    public static String createJWT(String secretKey, Duration ttl, long accountId, String role) {
        if (accountId <= 0 || !isKnownRole(role)) {
            throw new IllegalArgumentException("无效的登录身份");
        }
        Instant now = Instant.now();
        return JWT.create()
                .withClaim(ACCOUNT_ID_CLAIM, accountId)
                .withClaim(ROLE_CLAIM, role)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(ttl))
                .sign(Algorithm.HMAC256(secretKey));
    }

    public static VerifiedAccount verifyAccount(String secretKey, String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secretKey))
                .withClaimPresence("exp")
                .withClaimPresence(ACCOUNT_ID_CLAIM)
                .withClaimPresence(ROLE_CLAIM)
                .build()
                .verify(token);
        Long accountId = jwt.getClaim(ACCOUNT_ID_CLAIM).asLong();
        String role = jwt.getClaim(ROLE_CLAIM).asString();
        if (accountId == null || accountId <= 0 || !isKnownRole(role)) {
            throw new JWTVerificationException("无效的登录身份");
        }
        return new VerifiedAccount(accountId, role);
    }

    private static boolean isKnownRole(String role) {
        return AccountRole.ADMIN.equals(role) || AccountRole.EMPLOYEE.equals(role);
    }

    public record VerifiedAccount(long accountId, String role) {
    }
}
