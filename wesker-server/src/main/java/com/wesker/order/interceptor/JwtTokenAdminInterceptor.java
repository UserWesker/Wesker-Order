package com.wesker.order.interceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.wesker.order.constant.MessageConstant;
import com.wesker.order.properties.JwtProperties;
import com.wesker.order.result.Result;
import com.wesker.order.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenAdminInterceptor implements HandlerInterceptor {
    public static final String ADMIN_ID_ATTRIBUTE = "authenticatedAdminId";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProperties jwtProperties;
    private final JsonMapper jsonMapper;

    public JwtTokenAdminInterceptor(JwtProperties jwtProperties, JsonMapper jsonMapper) {
        this.jwtProperties = jwtProperties;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        Long adminId = verifyAdminId(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (adminId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            jsonMapper.writeValue(response.getOutputStream(), Result.error(MessageConstant.LOGIN_REQUIRED));
            return false;
        }

        request.setAttribute(ADMIN_ID_ATTRIBUTE, adminId);
        return true;
    }

    private Long verifyAdminId(String authorization) {
        if (authorization == null
                || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        try {
            return JwtUtil.verifyAdminId(jwtProperties.adminSecretKey(), token);
        } catch (JWTVerificationException exception) {
            return null;
        }
    }
}
