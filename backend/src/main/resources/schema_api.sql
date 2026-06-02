-- 统一数据服务与API管理
-- API生命周期 + 认证鉴权

USE etl_platform;

-- API定义表
CREATE TABLE IF NOT EXISTS api_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL COMMENT 'API名称',
    path VARCHAR(300) NOT NULL COMMENT 'API路径 /api/v1/data/xxx',
    method VARCHAR(10) DEFAULT 'GET' COMMENT 'GET/POST/PUT/DELETE',
    description VARCHAR(500),
    version VARCHAR(20) DEFAULT 'v1' COMMENT 'API版本',
    datasource_id BIGINT COMMENT '关联数据源',
    query_template TEXT COMMENT 'SQL模板/查询配置',
    response_type VARCHAR(30) DEFAULT 'JSON' COMMENT '响应类型',
    rate_limit INT DEFAULT 100 COMMENT '每分钟限流',
    cache_ttl INT DEFAULT 0 COMMENT '缓存秒数',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/DEPRECATED/OFFLINE',
    published_at DATETIME,
    deprecated_at DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_path_method (path, method),
    INDEX idx_status (status)
) COMMENT='API定义';

-- API密钥表
CREATE TABLE IF NOT EXISTS api_key (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL COMMENT '密钥名称',
    key_value VARCHAR(100) NOT NULL UNIQUE COMMENT '密钥值',
    secret_value VARCHAR(200) NOT NULL COMMENT '密钥Secret',
    role_name VARCHAR(100) COMMENT '关联角色',
    rate_limit_override INT COMMENT '自定义限流',
    expires_at DATETIME COMMENT '过期时间',
    last_used_at DATETIME,
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/REVOKED/EXPIRED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_key (key_value)
) COMMENT='API密钥';

-- API调用日志
CREATE TABLE IF NOT EXISTS api_call_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    api_id BIGINT,
    api_key_id BIGINT,
    caller_ip VARCHAR(50),
    request_method VARCHAR(10),
    request_path VARCHAR(500),
    request_params TEXT COMMENT '请求参数JSON',
    response_status INT COMMENT 'HTTP状态码',
    response_time_ms BIGINT COMMENT '响应毫秒',
    error_message TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_api_id (api_id),
    INDEX idx_create_time (create_time),
    INDEX idx_api_key (api_key_id)
) COMMENT='API调用日志';
