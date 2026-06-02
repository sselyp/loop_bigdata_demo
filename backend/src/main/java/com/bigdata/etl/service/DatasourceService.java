package com.bigdata.etl.service;

import com.bigdata.etl.model.entity.Datasource;
import java.util.List;

public interface DatasourceService {
    List<Datasource> listAll();
    Datasource getById(Long id);
    Datasource create(Datasource datasource);
    void update(Datasource datasource);
    void delete(Long id);
    boolean testConnection(Datasource datasource);
}
