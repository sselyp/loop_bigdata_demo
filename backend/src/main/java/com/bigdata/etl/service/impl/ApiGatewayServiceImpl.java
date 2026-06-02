package com.bigdata.etl.service.impl;

import com.bigdata.etl.model.entity.*;
import com.bigdata.etl.repository.*;
import com.bigdata.etl.service.ApiGatewayService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiGatewayServiceImpl implements ApiGatewayService {

    private final ApiDefinitionMapper apiMapper;
    private final ApiKeyMapper keyMapper;
    private final ApiCallLogMapper logMapper;

    // ========== API Management ==========

    @Override
    public List<ApiDefinition> listApis(String status) {
        LambdaQueryWrapper<ApiDefinition> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            w.eq(ApiDefinition::getStatus, status.toUpperCase());
        }
        w.orderByDesc(ApiDefinition::getCreateTime);
        return apiMapper.selectList(w);
    }

    @Override
    public ApiDefinition getApi(Long id) {
        return apiMapper.selectById(id);
    }

    @Override
    @Transactional
    public ApiDefinition createApi(ApiDefinition api) {
        if (api.getStatus() == null || api.getStatus().isBlank()) {
            api.setStatus("DRAFT");
        }
        if (api.getVersion() == null || api.getVersion().isBlank()) {
            api.setVersion("v1");
        }
        if (api.getMethod() == null || api.getMethod().isBlank()) {
            api.setMethod("GET");
        }
        apiMapper.insert(api);
        log.info("Created API: id={}, name={}, path={}", api.getId(), api.getName(), api.getPath());
        return api;
    }

    @Override
    public void updateApi(ApiDefinition api) {
        apiMapper.updateById(api);
        log.info("Updated API: id={}", api.getId());
    }

    @Override
    public void deleteApi(Long id) {
        apiMapper.deleteById(id);
        log.info("Deleted API: id={}", id);
    }

    @Override
    @Transactional
    public void publishApi(Long id) {
        ApiDefinition api = apiMapper.selectById(id);
        if (api != null) {
            api.setStatus("PUBLISHED");
            api.setPublishedAt(LocalDateTime.now());
            apiMapper.updateById(api);
            log.info("Published API: id={}, name={}", id, api.getName());
        }
    }

    @Override
    @Transactional
    public void deprecateApi(Long id) {
        ApiDefinition api = apiMapper.selectById(id);
        if (api != null) {
            api.setStatus("DEPRECATED");
            api.setDeprecatedAt(LocalDateTime.now());
            apiMapper.updateById(api);
            log.info("Deprecated API: id={}, name={}", id, api.getName());
        }
    }

    // ========== API Key Management ==========

    @Override
    public List<ApiKey> listKeys() {
        LambdaQueryWrapper<ApiKey> w = new LambdaQueryWrapper<>();
        w.orderByDesc(ApiKey::getCreateTime);
        return keyMapper.selectList(w);
    }

    @Override
    @Transactional
    public ApiKey createKey(ApiKey key) {
        if (key.getKeyValue() == null || key.getKeyValue().isBlank()) {
            key.setKeyValue("ak-" + UUID.randomUUID().toString().substring(0, 16));
        }
        if (key.getSecretValue() == null || key.getSecretValue().isBlank()) {
            key.setSecretValue(UUID.randomUUID().toString());
        }
        if (key.getStatus() == null || key.getStatus().isBlank()) {
            key.setStatus("ACTIVE");
        }
        keyMapper.insert(key);
        log.info("Created API key: id={}, name={}", key.getId(), key.getName());
        return key;
    }

    @Override
    @Transactional
    public void revokeKey(Long id) {
        ApiKey key = keyMapper.selectById(id);
        if (key != null) {
            key.setStatus("REVOKED");
            keyMapper.updateById(key);
            log.info("Revoked API key: id={}", id);
        }
    }

    @Override
    public boolean validateKey(String keyValue) {
        LambdaQueryWrapper<ApiKey> w = new LambdaQueryWrapper<>();
        w.eq(ApiKey::getKeyValue, keyValue)
         .eq(ApiKey::getStatus, "ACTIVE");
        ApiKey key = keyMapper.selectOne(w);
        if (key == null) return false;
        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
            key.setStatus("EXPIRED");
            keyMapper.updateById(key);
            return false;
        }
        key.setLastUsedAt(LocalDateTime.now());
        keyMapper.updateById(key);
        return true;
    }

    // ========== Call Logging ==========

    @Override
    public List<ApiCallLog> listCallLogs(Long apiId, int limit) {
        LambdaQueryWrapper<ApiCallLog> w = new LambdaQueryWrapper<>();
        if (apiId != null) {
            w.eq(ApiCallLog::getApiId, apiId);
        }
        w.orderByDesc(ApiCallLog::getCreateTime);
        if (limit > 0) w.last("LIMIT " + limit);
        return logMapper.selectList(w);
    }

    @Override
    @Transactional
    public void logCall(ApiCallLog log) {
        logMapper.insert(log);
    }
}
