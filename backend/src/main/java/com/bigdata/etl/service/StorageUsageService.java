package com.bigdata.etl.service;

import com.bigdata.etl.model.entity.StorageUsage;
import java.util.List;

public interface StorageUsageService {
    List<StorageUsage> listAll();
    StorageUsage getById(Long id);
    StorageUsage create(StorageUsage record);
    void update(StorageUsage record);
    void delete(Long id);
}
