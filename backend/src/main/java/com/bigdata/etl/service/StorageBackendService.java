package com.bigdata.etl.service;

import com.bigdata.etl.model.entity.StorageBackend;
import java.util.List;

public interface StorageBackendService {
    List<StorageBackend> listAll();
    StorageBackend getById(Long id);
    StorageBackend create(StorageBackend record);
    void update(StorageBackend record);
    void delete(Long id);
}
