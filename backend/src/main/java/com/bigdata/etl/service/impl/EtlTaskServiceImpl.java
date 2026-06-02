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
            if (task.getIncrementalColumn().matches(".*[;'—].*")) {
                throw new IllegalArgumentException("incrementalColumn contains invalid characters");
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
        } catch (Exception ignored) {
            // may not be encrypted yet
        }

        try (Connection srcConn = DriverManager.getConnection(sourceUrl, sourceDs.getUsername(), password);
             Statement stmt = srcConn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(meta.getColumnName(i));
            }

            long rows = 0;
            while (rs.next()) {
                rows++;
                if (rows % 1000 == 0) {
                    log.debug("Synced {} rows for task {}", rows, task.getId());
                }
            }
            return rows;
        }
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
        return switch (ds.getType().toUpperCase()) {
            case "MYSQL", "TIDB" ->
                String.format("jdbc:mysql://%s:%d/%s?useSSL=false&connectTimeout=10000&socketTimeout=30000",
                        ds.getHost(), ds.getPort(), ds.getDatabase());
            case "POSTGRESQL", "PG" ->
                String.format("jdbc:postgresql://%s:%d/%s?connectTimeout=10&socketTimeout=30",
                        ds.getHost(), ds.getPort(), ds.getDatabase());
            case "ORACLE" ->
                String.format("jdbc:oracle:thin:@%s:%d:%s", ds.getHost(), ds.getPort(), ds.getDatabase());
            case "SQLSERVER", "MSSQL" ->
                String.format("jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;loginTimeout=10",
                        ds.getHost(), ds.getPort(), ds.getDatabase());
            default -> throw new IllegalArgumentException("Unsupported database type: " + ds.getType());
        };
    }
}
