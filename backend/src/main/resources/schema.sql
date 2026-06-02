-- ETL平台数据库初始化脚本
-- 兼容 TiDB / MySQL

CREATE DATABASE IF NOT EXISTS etl_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE etl_platform;

-- 数据源表
CREATE TABLE IF NOT EXISTS etl_datasource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    type VARCHAR(20) NOT NULL COMMENT '数据源类型: MYSQL/POSTGRESQL/ORACLE/SQLSERVER',
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    `database` VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT='ETL数据源';

-- ETL任务表
CREATE TABLE IF NOT EXISTS etl_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '任务名称',
    source_datasource_id BIGINT NOT NULL COMMENT '源数据源ID',
    source_table VARCHAR(200) NOT NULL COMMENT '源表名',
    target_table VARCHAR(200) NOT NULL COMMENT '目标表名(TiDB)',
    sync_mode VARCHAR(20) NOT NULL COMMENT 'FULL/INCREMENTAL',
    incremental_column VARCHAR(100) COMMENT '增量字段',
    field_mapping JSON COMMENT '字段映射 [{source,target}]',
    schedule_type VARCHAR(20) DEFAULT 'MANUAL' COMMENT 'MANUAL/CRON',
    cron_expression VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    last_run_status VARCHAR(20) COMMENT 'SUCCESS/FAILED/RUNNING',
    last_run_time DATETIME,
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT='ETL任务';

-- ETL执行记录表
CREATE TABLE IF NOT EXISTS etl_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL COMMENT 'RUNNING/SUCCESS/FAILED',
    rows_processed BIGINT DEFAULT 0,
    error_message TEXT,
    start_time DATETIME,
    end_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id),
    INDEX idx_start_time (start_time)
) COMMENT='ETL执行记录';
