-- 数据治理服务表
-- 元数据/血缘/质量/权限/脱敏/审计

USE etl_platform;

-- 元数据-表信息
CREATE TABLE IF NOT EXISTS gov_metadata_table (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    datasource_id BIGINT NOT NULL COMMENT '所属数据源',
    table_schema VARCHAR(100) COMMENT 'Schema名',
    table_name VARCHAR(200) NOT NULL COMMENT '表名',
    table_comment VARCHAR(500) COMMENT '表注释',
    row_count_estimate BIGINT DEFAULT 0 COMMENT '估计行数',
    storage_mb BIGINT DEFAULT 0 COMMENT '存储大小MB',
    owner VARCHAR(100) COMMENT '负责人',
    tags VARCHAR(500) COMMENT '标签',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_datasource (datasource_id)
) COMMENT='元数据-表';

-- 元数据-列信息
CREATE TABLE IF NOT EXISTS gov_metadata_column (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    table_id BIGINT NOT NULL COMMENT '所属表',
    column_name VARCHAR(200) NOT NULL COMMENT '列名',
    ordinal_position INT COMMENT '列序号',
    data_type VARCHAR(100) COMMENT '数据类型',
    is_nullable VARCHAR(3) DEFAULT 'YES',
    column_default VARCHAR(500),
    column_comment VARCHAR(500),
    is_pk TINYINT DEFAULT 0,
    is_fk TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_table (table_id)
) COMMENT='元数据-列';

-- 数据血缘
CREATE TABLE IF NOT EXISTS gov_lineage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_table_id BIGINT NOT NULL COMMENT '源表ID',
    source_column VARCHAR(200) COMMENT '源列名',
    target_table_id BIGINT NOT NULL COMMENT '目标表ID',
    target_column VARCHAR(200) COMMENT '目标列名',
    transform_expr TEXT COMMENT '转换表达式',
    etl_task_id BIGINT COMMENT '关联ETL任务',
    lineage_type VARCHAR(30) DEFAULT 'ETL' COMMENT 'ETL/MANUAL/DERIVED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_source (source_table_id),
    INDEX idx_target (target_table_id)
) COMMENT='数据血缘';

-- 数据质量规则
CREATE TABLE IF NOT EXISTS gov_quality_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL COMMENT '规则名称',
    table_id BIGINT NOT NULL COMMENT '关联表',
    rule_type VARCHAR(30) NOT NULL COMMENT 'NOT_NULL/UNIQUE/RANGE/REGEX/CUSTOM_SQL/ROW_COUNT/FRESHNESS',
    column_name VARCHAR(200) COMMENT '列名',
    rule_config JSON COMMENT '规则配置 {"min":0,"max":100,"pattern":"..."}',
    severity VARCHAR(20) DEFAULT 'WARN' COMMENT 'ERROR/WARN/INFO',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='数据质量规则';

-- 数据质量检查记录
CREATE TABLE IF NOT EXISTS gov_quality_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL COMMENT 'PASS/FAIL/ERROR',
    actual_value TEXT COMMENT '实际值',
    expected_value TEXT COMMENT '期望值',
    error_count BIGINT DEFAULT 0,
    executed_sql TEXT COMMENT '执行的SQL',
    check_time DATETIME,
    duration_ms BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rule (rule_id),
    INDEX idx_check_time (check_time)
) COMMENT='数据质量检查';

-- 数据脱敏规则
CREATE TABLE IF NOT EXISTS gov_masking_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    table_id BIGINT NOT NULL,
    column_name VARCHAR(200) NOT NULL,
    mask_type VARCHAR(30) NOT NULL COMMENT 'FULL_MASK/PARTIAL_MASK/EMAIL_MASK/PHONE_MASK/HASH/REPLACE',
    mask_config JSON COMMENT '脱敏配置',
    status VARCHAR(20) DEFAULT 'ENABLED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='数据脱敏规则';

-- 数据权限
CREATE TABLE IF NOT EXISTS gov_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(100) NOT NULL COMMENT '角色名',
    table_id BIGINT NOT NULL,
    grant_select TINYINT DEFAULT 1 COMMENT 'SELECT权限',
    grant_insert TINYINT DEFAULT 0 COMMENT 'INSERT权限',
    grant_update TINYINT DEFAULT 0 COMMENT 'UPDATE权限',
    grant_delete TINYINT DEFAULT 0 COMMENT 'DELETE权限',
    row_filter_expr VARCHAR(500) COMMENT '行过滤表达式',
    column_allow_list VARCHAR(1000) COMMENT '允许的列,逗号分隔',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role_name),
    INDEX idx_table (table_id)
) COMMENT='数据权限';

-- 操作审计日志
CREATE TABLE IF NOT EXISTS gov_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(50) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    operator VARCHAR(100) COMMENT '操作人',
    detail TEXT COMMENT '操作详情JSON',
    client_ip VARCHAR(50),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operator (operator),
    INDEX idx_target (target_type, target_id),
    INDEX idx_create_time (create_time)
) COMMENT='操作审计日志';
