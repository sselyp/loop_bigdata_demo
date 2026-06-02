package com.bigdata.etl.service.impl;

import com.bigdata.etl.common.CryptoUtils;
import com.bigdata.etl.model.entity.Datasource;
import com.bigdata.etl.model.entity.EtlExecution;
import com.bigdata.etl.model.entity.EtlTask;
import com.bigdata.etl.repository.DatasourceMapper;
import com.bigdata.etl.repository.EtlExecutionMapper;
import com.bigdata.etl.repository.EtlTaskMapper;
import com.bigdata.etl.service.EtlTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EtlTaskServiceImpl implements EtlTaskService {

    private final EtlTaskMapper etlTaskMapper;
    private final EtlExecutionMapper etlExecutionMapper;
    private final DatasourceMapper datasourceMapper;
    private final DataSource appDataSource;

    @Override
    public List<EtlTask> listAll() {
        LambdaQueryWrapper<EtlTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(EtlTask::getCreateTime);
        return etlTaskMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public EtlTask create(EtlTask task) {
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("ENABLED");
        }
        if (task.getScheduleType() == null || task.getScheduleType().isBlank()) {
            task.setScheduleType("MANUAL");
        }
        if (task.getSyncMode() == null || task.getSyncMode().isBlank()) {
            task.setSyncMode("FULL");
        }
        etlTaskMapper.insert(task);
        log.info("Created ETL task: id={}, name={}, syncMode={}", task.getId(), task.getName(), task.getSyncMode());
        return task;
    }

    @Override
    @Transactional
    public void update(EtlTask task) {
        etlTaskMapper.updateById(task);
        log.info("Updated ETL task: id={}", task.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        etlTaskMapper.deleteById(id);
        log.info("Deleted ETL task: id={}", id);
    }

    @Override
    @Transactional
    public Long runTask(Long taskId) {
        EtlTask task = etlTaskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        if ("DISABLED".equals(task.getStatus())) {
            throw new IllegalStateException("Task is disabled: " + taskId);
        }
        if ("INCREMENTAL".equals(task.getSyncMode())) {
            if (task.getIncrementalColumn() == null || task.getIncrementalColumn().isBlank()) {
                throw new IllegalArgumentException("Incremental mode requires incrementalColumn to be set");
            }
            if (!task.getIncrementalColumn().matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                throw new IllegalArgumentException(
                    "incrementalColumn must be a valid SQL column name: [a-zA-Z_][a-zA-Z0-9_]*");
            }
        }

        EtlExecution execution = new EtlExecution();
        execution.setTaskId(taskId);
        execution.setStatus("RUNNING");
        execution.setStartTime(LocalDateTime.now());
        etlExecutionMapper.insert(execution);
        log.info("Started ETL execution: id={}, taskId={}", execution.getId(), taskId);

        executeTask(task, execution);
        return execution.getId();
    }

    @Override
    public List<EtlExecution> getExecutions(Long taskId) {
        LambdaQueryWrapper<EtlExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EtlExecution::getTaskId, taskId)
               .orderByDesc(EtlExecution::getStartTime);
        return etlExecutionMapper.selectList(wrapper);
    }

    private void executeTask(EtlTask task, EtlExecution execution) {
        Datasource sourceDs = datasourceMapper.selectById(task.getSourceDatasourceId());
        if (sourceDs == null) {
            markExecutionFailed(execution, "Source datasource not found: " + task.getSourceDatasourceId());
            return;
        }

        try {
            long rowsProcessed = doSync(task, sourceDs);
            execution.setStatus("SUCCESS");
            execution.setRowsProcessed(rowsProcessed);
            execution.setEndTime(LocalDateTime.now());
            etlExecutionMapper.updateById(execution);

            task.setLastRunStatus("SUCCESS");
            task.setLastRunTime(LocalDateTime.now());
            etlTaskMapper.updateById(task);

            log.info("ETL execution success: executionId={}, taskId={}, rows={}", execution.getId(), task.getId(), rowsProcessed);
        } catch (Exception e) {
            log.error("ETL execution failed: executionId={}, taskId={}", execution.getId(), task.getId(), e);
            markExecutionFailed(execution, e.getMessage());
            task.setLastRunStatus("FAILED");
            task.setLastRunTime(LocalDateTime.now());
            etlTaskMapper.updateById(task);
        }
    }

    private long doSync(EtlTask task, Datasource sourceDs) throws SQLException {
        String sourceUrl = buildJdbcUrl(sourceDs);
        String selectSql = buildSelectSql(task);
        String password = sourceDs.getPassword();
        try {
            password = CryptoUtils.decrypt(password);
        } catch (Exception e) {
            log.error("Failed to decrypt datasource password for id={}", sourceDs.getId(), e);
            throw new IllegalStateException("Failed to decrypt datasource password. " +
                "Ensure the datasource was stored with the current ETL_ENCRYPTION_KEY.");
        }

        String targetTable = task.getTargetTable() != null ? task.getTargetTable() : task.getSourceTable() + "_synced";

        try (Connection srcConn = DriverManager.getConnection(sourceUrl, sourceDs.getUsername(), password);
             Statement stmt = srcConn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql);
             Connection tgtConn = appDataSource.getConnection()) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            List<String> columns = new ArrayList<>();
            List<String> placeholders = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String colName = meta.getColumnName(i);
                columns.add(colName);
                placeholders.add("?");
            }

            String insertSql = buildUpsertSql(targetTable, columns, placeholders);
            tgtConn.setAutoCommit(false);

            long rows = 0;
            int batchSize = 500;
            try (PreparedStatement pstmt = tgtConn.prepareStatement(insertSql)) {
                while (rs.next()) {
                    for (int i = 0; i < columnCount; i++) {
                        pstmt.setObject(i + 1, rs.getObject(i + 1));
                    }
                    pstmt.addBatch();
                    rows++;
                    if (rows % batchSize == 0) {
                        pstmt.executeBatch();
                        tgtConn.commit();
                        log.debug("Synced {} rows for task {}", rows, task.getId());
                    }
                }
                // Final batch
                if (rows % batchSize != 0) {
                    pstmt.executeBatch();
                    tgtConn.commit();
                }
            }
            log.info("Sync complete for task {}: {} rows written to {}", task.getId(), rows, targetTable);
            return rows;
        }
    }

    private String buildUpsertSql(String table, List<String> columns, List<String> placeholders) {
        // MySQL/TiDB INSERT ... ON DUPLICATE KEY UPDATE for idempotent writes
        String cols = String.join(", ", columns);
        String vals = String.join(", ", placeholders);
        StringBuilder updates = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) updates.append(", ");
            updates.append(columns.get(i)).append(" = VALUES(").append(columns.get(i)).append(")");
        }
        return String.format("INSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s",
                table, cols, vals, updates.toString());
    }

    private String buildSelectSql(EtlTask task) {
        StringBuilder sql = new StringBuilder("SELECT ");

        if (task.getFieldMapping() != null && !task.getFieldMapping().isBlank()
                && !"[]".equals(task.getFieldMapping().trim())) {
            String fieldMapping = task.getFieldMapping().trim();
            boolean first = true;
            String regex = "[\[\]{}\"]";
            for (String part : fieldMapping.replaceAll(regex, "").split(",")) {
                if (part.startsWith("source:")) {
                    if (!first) sql.append(", ");
                    sql.append(part.substring(7));
                    first = false;
                }
            }
            if (first) {
                sql.append("*");
            }
        } else {
            sql.append("*");
        }

        sql.append(" FROM ").append(task.getSourceTable());

        if ("INCREMENTAL".equals(task.getSyncMode()) && task.getIncrementalColumn() != null) {
            String lastRun = task.getLastRunTime() != null ? task.getLastRunTime().toString() : "1970-01-01 00:00:00";
            sql.append(" WHERE ").append(task.getIncrementalColumn()).append(" > ").append(quote(lastRun));
        }

        return sql.toString();
    }

    private String quote(String value) {
        return "'" + value + "'";
    }

    private void markExecutionFailed(EtlExecution execution, String errorMessage) {
        execution.setStatus("FAILED");
        execution.setEndTime(LocalDateTime.now());
        execution.setErrorMessage(errorMessage != null && errorMessage.length() > 2000
                ? errorMessage.substring(0, 2000) : errorMessage);
        etlExecutionMapper.updateById(execution);
    }

    private String buildJdbcUrl(Datasource ds) {
        String host = ds.getHost() != null ? ds.getHost().replaceAll("[^a-zA-Z0-9._-]", "") : "localhost";
        String db = ds.getDatabase() != null ? ds.getDatabase().replaceAll("[^a-zA-Z0-9_]", "") : "";
        return switch (ds.getType().toUpperCase()) {
            case "MYSQL", "TIDB" ->
                String.format("jdbc:mysql://%s:%d/%s?useSSL=false&connectTimeout=10000&socketTimeout=30000",
                        host, ds.getPort(), db);
            case "POSTGRESQL", "PG" ->
                String.format("jdbc:postgresql://%s:%d/%s?connectTimeout=10&socketTimeout=30",
                        host, ds.getPort(), db);
            case "ORACLE" ->
                String.format("jdbc:oracle:thin:@%s:%d:%s", host, ds.getPort(), db);
            case "SQLSERVER", "MSSQL" ->
                String.format("jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;loginTimeout=10",
                        host, ds.getPort(), db);
            default -> throw new IllegalArgumentException("Unsupported database type: " + ds.getType());
        };
    }
}
