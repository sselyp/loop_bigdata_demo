-- 计算引擎管理表
-- 批流一体：Flink/Spark/Trino

USE etl_platform;

-- 计算引擎表
CREATE TABLE IF NOT EXISTS compute_engine (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '引擎名称',
    type VARCHAR(30) NOT NULL COMMENT 'FLINK/SPARK/TRINO',
    endpoint VARCHAR(500) NOT NULL COMMENT '引擎地址 http://host:port',
    config_json JSON COMMENT '引擎配置',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT='计算引擎';

-- 计算任务表
CREATE TABLE IF NOT EXISTS compute_job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    engine_id BIGINT NOT NULL COMMENT '计算引擎ID',
    name VARCHAR(200) NOT NULL COMMENT '任务名称',
    job_type VARCHAR(30) NOT NULL COMMENT 'BATCH/STREAM/QUERY',
    sql_content TEXT COMMENT 'SQL语句',
    job_config_json JSON COMMENT '任务配置',
    status VARCHAR(30) DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/SUCCESS/FAILED/CANCELLED',
    submitted_at DATETIME,
    started_at DATETIME,
    finished_at DATETIME,
    result_summary TEXT COMMENT '结果摘要',
    error_message TEXT,
    submitted_by VARCHAR(100) COMMENT '提交人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_engine_id (engine_id),
    INDEX idx_status (status)
) COMMENT='计算任务';

-- 查询会话表
CREATE TABLE IF NOT EXISTS query_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    engine_id BIGINT NOT NULL,
    session_key VARCHAR(100) COMMENT '会话标识',
    current_sql TEXT COMMENT '当前SQL',
    status VARCHAR(20) DEFAULT 'IDLE' COMMENT 'IDLE/RUNNING/CLOSED',
    created_by VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_active_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_engine_id (engine_id)
) COMMENT='查询会话';
