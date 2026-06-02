package com.bigdata.etl.service.impl;

import com.bigdata.etl.model.entity.StorageQuota;
import com.bigdata.etl.repository.StorageQuotaMapper;
import com.bigdata.etl.service.StorageQuotaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageQuotaServiceImpl implements StorageQuotaService {

    private final StorageQuotaMapper mapper;

    @Override
    public List<StorageQuota> listAll() {
        return mapper.selectList(null);
    }

    @Override
    public StorageQuota getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public StorageQuota create(StorageQuota record) {
        if (record.getStatus() == null || record.getStatus().isBlank()) {
            record.setStatus("ACTIVE");
        }
        mapper.insert(record);
        log.info("Created storage quota: id={}, tenantId={}, backendId={}", record.getId(), record.getTenantId(), record.getBackendId());
        return record;
    }

    @Override
    public void update(StorageQuota record) {
        mapper.updateById(record);
        log.info("Updated storage quota: id={}", record.getId());
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
        log.info("Deleted storage quota: id={}", id);
    }

    public List<StorageQuota> listByTenant(Long tenantId) {
        LambdaQueryWrapper<StorageQuota> w = new LambdaQueryWrapper<>();
        w.eq(StorageQuota::getTenantId, tenantId);
        return mapper.selectList(w);
    }

    public List<StorageQuota> listByBackend(Long backendId) {
        LambdaQueryWrapper<StorageQuota> w = new LambdaQueryWrapper<>();
        w.eq(StorageQuota::getBackendId, backendId);
        return mapper.selectList(w);
    }
}
