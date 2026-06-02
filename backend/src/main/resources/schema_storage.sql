-- 数据存储层管理表
-- 混合存储 + 多租户资源管理

USE etl_platform;

-- 存储后端表
CREATE TABLE IF NOT EXISTS storage_backend (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '存储名称',
    type VARCHAR(30) NOT NULL COMMENT 'TIDB/HDFS/S3/MINIO/LOCAL',
    config_json JSON COMMENT '连接配置 {"host":"...","port":4000,...}',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT='存储后端';

-- 租户表
CREATE TABLE IF NOT EXISTS storage_tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '租户名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '租户编码',
    description VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    admin_user VARCHAR(100) COMMENT '管理员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) COMMENT='存储租户';

-- 租户存储配额表
CREATE TABLE IF NOT EXISTS storage_quota (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    backend_id BIGINT NOT NULL COMMENT '存储后端ID',
    max_storage_mb BIGINT DEFAULT 1024 COMMENT '最大存储(MB)',
    max_connections INT DEFAULT 10 COMMENT '最大连接数',
    max_tables INT DEFAULT 100 COMMENT '最大表数',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_backend (tenant_id, backend_id)
) COMMENT='存储配额';

-- 存储使用统计表
CREATE TABLE IF NOT EXISTS storage_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    backend_id BIGINT NOT NULL,
    used_storage_mb BIGINT DEFAULT 0 COMMENT '已用存储(MB)',
    table_count INT DEFAULT 0 COMMENT '表数量',
    last_calc_time DATETIME COMMENT '上次统计时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_backend_usage (tenant_id, backend_id)
) COMMENT='存储使用统计';
