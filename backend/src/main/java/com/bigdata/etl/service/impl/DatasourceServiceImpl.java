package com.bigdata.etl.service.impl;

import com.bigdata.etl.common.CryptoUtils;
import com.bigdata.etl.model.entity.Datasource;
import com.bigdata.etl.repository.DatasourceMapper;
import com.bigdata.etl.service.DatasourceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatasourceServiceImpl implements DatasourceService {

    private final DatasourceMapper datasourceMapper;

    @Override
    public List<Datasource> listAll() {
        LambdaQueryWrapper<Datasource> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Datasource::getCreateTime);
        return datasourceMapper.selectList(wrapper);
    }

    @Override
    public Datasource create(Datasource datasource) {
        if (datasource.getStatus() == null || datasource.getStatus().isBlank()) {
            datasource.setStatus("ACTIVE");
        }
        if (datasource.getPassword() != null && !datasource.getPassword().isBlank()) {
            datasource.setPassword(CryptoUtils.encrypt(datasource.getPassword()));
        }
        datasourceMapper.insert(datasource);
        log.info("Created datasource: id={}, name={}, type={}", datasource.getId(), datasource.getName(), datasource.getType());
        return datasource;
    }

    @Override
    public void update(Datasource datasource) {
        if (datasource.getPassword() != null && !datasource.getPassword().isBlank()) {
            datasource.setPassword(CryptoUtils.encrypt(datasource.getPassword()));
        }
        datasourceMapper.updateById(datasource);
        log.info("Updated datasource: id={}", datasource.getId());
    }

    @Override
    public void delete(Long id) {
        datasourceMapper.deleteById(id);
        log.info("Deleted datasource: id={}", id);
    }

    @Override
    public boolean testConnection(Datasource datasource) {
        String url = buildJdbcUrl(datasource);
        String password = datasource.getPassword();
        try {
            password = CryptoUtils.decrypt(password);
        } catch (Exception ignored) {
            // password may not be encrypted yet (legacy data)
        }
        try (Connection conn = DriverManager.getConnection(url, datasource.getUsername(), password)) {
            boolean valid = conn.isValid(5);
            if (valid) {
                log.info("Connection test success: {}@{}:{}", datasource.getType(), datasource.getHost(), datasource.getPort());
            }
            return valid;
        } catch (Exception e) {
            log.warn("Connection test failed for datasource {}: {}", datasource.getName(), e.getMessage());
            return false;
        }
    }

    private String buildJdbcUrl(Datasource ds) {
        return switch (ds.getType().toUpperCase()) {
            case "MYSQL", "TIDB" ->
                String.format("jdbc:mysql://%s:%d/%s?useSSL=false&connectTimeout=5000&socketTimeout=5000",
                        ds.getHost(), ds.getPort(), ds.getDatabase());
            case "POSTGRESQL", "PG" ->
                String.format("jdbc:postgresql://%s:%d/%s?connectTimeout=5&socketTimeout=5",
                        ds.getHost(), ds.getPort(), ds.getDatabase());
            case "ORACLE" ->
                String.format("jdbc:oracle:thin:@%s:%d:%s", ds.getHost(), ds.getPort(), ds.getDatabase());
            case "SQLSERVER", "MSSQL" ->
                String.format("jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;loginTimeout=5",
                        ds.getHost(), ds.getPort(), ds.getDatabase());
            default -> throw new IllegalArgumentException("Unsupported database type: " + ds.getType());
        };
    }
}
