package com.bigdata.etl.service.impl;

import com.bigdata.etl.model.entity.StorageUsage;
import com.bigdata.etl.repository.StorageUsageMapper;
import com.bigdata.etl.service.StorageUsageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageUsageServiceImpl implements StorageUsageService {

    private final StorageUsageMapper mapper;

    @Override
    public List<StorageUsage> listAll() {
        return mapper.selectList(null);
    }

    @Override
    public StorageUsage getById(Long id) {
        return mapper.selectById(id);
    }

    public List<StorageUsage> listByTenant(Long tenantId) {
        LambdaQueryWrapper<StorageUsage> w = new LambdaQueryWrapper<>();
        w.eq(StorageUsage::getTenantId, tenantId);
        return mapper.selectList(w);
    }

    @Override
    public StorageUsage create(StorageUsage record) {
        mapper.insert(record);
        return record;
    }

    @Override
    public void update(StorageUsage record) {
        record.setLastCalcTime(LocalDateTime.now());
        mapper.updateById(record);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    public void recalculate(Long tenantId, Long backendId) {
        LambdaQueryWrapper<StorageUsage> w = new LambdaQueryWrapper<>();
        w.eq(StorageUsage::getTenantId, tenantId)
         .eq(StorageUsage::getBackendId, backendId);
        StorageUsage usage = mapper.selectOne(w);
        if (usage == null) {
            usage = new StorageUsage();
            usage.setTenantId(tenantId);
            usage.setBackendId(backendId);
            usage.setUsedStorageMb(0L);
            usage.setTableCount(0);
            usage.setLastCalcTime(LocalDateTime.now());
            mapper.insert(usage);
        } else {
            usage.setLastCalcTime(LocalDateTime.now());
            mapper.updateById(usage);
        }
        log.info("Recalculated storage usage: tenantId={}, backendId={}", tenantId, backendId);
    }
}
