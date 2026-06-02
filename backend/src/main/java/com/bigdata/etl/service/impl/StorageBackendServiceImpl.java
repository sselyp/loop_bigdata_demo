package com.bigdata.etl.service.impl;

import com.bigdata.etl.model.entity.StorageBackend;
import com.bigdata.etl.repository.StorageBackendMapper;
import com.bigdata.etl.service.StorageBackendService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageBackendServiceImpl implements StorageBackendService {

    private final StorageBackendMapper mapper;

    @Override
    public List<StorageBackend> listAll() {
        LambdaQueryWrapper<StorageBackend> w = new LambdaQueryWrapper<>();
        w.orderByDesc(StorageBackend::getCreateTime);
        return mapper.selectList(w);
    }

    @Override
    public StorageBackend getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public StorageBackend create(StorageBackend record) {
        if (record.getStatus() == null || record.getStatus().isBlank()) {
            record.setStatus("ACTIVE");
        }
        mapper.insert(record);
        log.info("Created storage backend: id={}, name={}, type={}", record.getId(), record.getName(), record.getType());
        return record;
    }

    @Override
    public void update(StorageBackend record) {
        mapper.updateById(record);
        log.info("Updated storage backend: id={}", record.getId());
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
        log.info("Deleted storage backend: id={}", id);
    }
}
