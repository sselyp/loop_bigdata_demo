package com.bigdata.etl.interceptor;

import com.bigdata.etl.repository.ApiKeyMapper;
import com.bigdata.etl.model.entity.ApiKey;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * Interceptor that validates API key on protected endpoints.
 * Configure in WebMvcConfigurer to protect /api/etl/** and /api/gateway/apis/**
 * Excludes Swagger UI and health check endpoints.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final ApiKeyMapper apiKeyMapper;

    private static final String HEADER_NAME = "X-API-Key";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();

        // Skip auth for Swagger, health, and gateway management endpoints
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.startsWith("/health") || path.startsWith("/api/gateway/keys")) {
            return true;
        }

        String apiKey = request.getHeader(HEADER_NAME);
        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(401);
            log.warn("Missing API key for path={}", path);
            return false;
        }

        LambdaQueryWrapper<ApiKey> w = new LambdaQueryWrapper<>();
        w.eq(ApiKey::getKeyValue, apiKey)
         .eq(ApiKey::getStatus, "ACTIVE");
        ApiKey key = apiKeyMapper.selectOne(w);

        if (key == null) {
            response.setStatus(403);
            log.warn("Invalid or revoked API key used: key={}", apiKey.substring(0, Math.min(8, apiKey.length())));
            return false;
        }

        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
            key.setStatus("EXPIRED");
            apiKeyMapper.updateById(key);
            response.setStatus(403);
            log.warn("Expired API key used: id={}", key.getId());
            return false;
        }

        // Update last used timestamp
        key.setLastUsedAt(LocalDateTime.now());
        apiKeyMapper.updateById(key);

        return true;
    }
}
