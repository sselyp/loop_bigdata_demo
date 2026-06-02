package com.bigdata.etl.service;

import com.bigdata.etl.model.entity.StorageTenant;
import java.util.List;

public interface StorageTenantService {
    List<StorageTenant> listAll();
    StorageTenant getById(Long id);
    StorageTenant create(StorageTenant record);
    void update(StorageTenant record);
    void delete(Long id);
}
