package com.bigdata.etl.service;

import com.bigdata.etl.model.entity.StorageQuota;
import java.util.List;

public interface StorageQuotaService {
    List<StorageQuota> listAll();
    StorageQuota getById(Long id);
    StorageQuota create(StorageQuota record);
    void update(StorageQuota record);
    void delete(Long id);
}
