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
    public Datasource getById(Long id) {
        return datasourceMapper.selectById(id);
    }

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
        // Validate host and database to prevent SSRF and JDBC injection
        String host = datasource.getHost();
        String db = datasource.getDatabase();
        if (host == null || !host.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException("Invalid host: " + host);
        }
        if (db == null || !db.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid database name: " + db);
        }
        // Block private/reserved IP ranges to prevent SSRF (allow override for dev/test)
        String allowPrivate = System.getenv("ETL_ALLOW_PRIVATE_HOST");
        if (!"true".equalsIgnoreCase(allowPrivate) && isPrivateAddress(host)) {
            throw new IllegalArgumentException("Private IP addresses are not allowed: " + host
                + ". Set ETL_ALLOW_PRIVATE_HOST=true to allow in dev/test environments.");
        }

        String url = buildJdbcUrl(datasource);
        String password = datasource.getPassword();
        if (password != null && !password.isBlank()) {
            try {
                password = CryptoUtils.decrypt(password);
            } catch (Exception e) {
                log.error("Failed to decrypt password for datasource id={}", datasource.getId(), e);
                throw new IllegalStateException("Failed to decrypt datasource password. " +
                    "Ensure the datasource was stored with the current ETL_ENCRYPTION_KEY.");
            }
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

    private boolean isPrivateAddress(String host) {
        try {
            java.net.InetAddress addr = java.net.InetAddress.getByName(host);
            byte[] octets = addr.getAddress();
            if (octets.length == 4) {
                int first = octets[0] & 0xFF;
                int second = octets[1] & 0xFF;
                // 10.0.0.0/8
                if (first == 10) return true;
                // 172.16.0.0/12
                if (first == 172 && second >= 16 && second <= 31) return true;
                // 192.168.0.0/16
                if (first == 192 && second == 168) return true;
                // 127.0.0.0/8
                if (first == 127) return true;
                // 0.0.0.0/8
                if (first == 0) return true;
                // 169.254.0.0/16
                if (first == 169 && second == 254) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private String buildJdbcUrl(Datasource ds) {
        // Sanitize inputs to prevent JDBC URL injection
        String host = ds.getHost() != null ? ds.getHost().replaceAll("[^a-zA-Z0-9._-]", "") : "localhost";
        String db = ds.getDatabase() != null ? ds.getDatabase().replaceAll("[^a-zA-Z0-9_]", "") : "";
        return switch (ds.getType().toUpperCase()) {
            case "MYSQL", "TIDB" ->
                String.format("jdbc:mysql://%s:%d/%s?useSSL=false&connectTimeout=5000&socketTimeout=5000",
                        host, ds.getPort(), db);
            case "POSTGRESQL", "PG" ->
                String.format("jdbc:postgresql://%s:%d/%s?connectTimeout=5&socketTimeout=5",
                        host, ds.getPort(), db);
            case "ORACLE" ->
                String.format("jdbc:oracle:thin:@%s:%d:%s", host, ds.getPort(), db);
            case "SQLSERVER", "MSSQL" ->
                String.format("jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;loginTimeout=5",
                        host, ds.getPort(), db);
            default -> throw new IllegalArgumentException("Unsupported database type: " + ds.getType());
        };
    }
}
