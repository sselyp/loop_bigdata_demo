package com.bigdata.etl.service.impl;

import com.bigdata.etl.model.entity.StorageTenant;
import com.bigdata.etl.repository.StorageTenantMapper;
import com.bigdata.etl.service.StorageTenantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageTenantServiceImpl implements StorageTenantService {

    private final StorageTenantMapper mapper;

    @Override
    public List<StorageTenant> listAll() {
        LambdaQueryWrapper<StorageTenant> w = new LambdaQueryWrapper<>();
        w.orderByDesc(StorageTenant::getCreateTime);
        return mapper.selectList(w);
    }

    @Override
    public StorageTenant getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public StorageTenant create(StorageTenant record) {
        if (record.getStatus() == null || record.getStatus().isBlank()) {
            record.setStatus("ACTIVE");
        }
        mapper.insert(record);
        log.info("Created storage tenant: id={}, name={}, code={}", record.getId(), record.getName(), record.getCode());
        return record;
    }

    @Override
    public void update(StorageTenant record) {
        mapper.updateById(record);
        log.info("Updated storage tenant: id={}", record.getId());
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
        log.info("Deleted storage tenant: id={}", id);
    }
}
