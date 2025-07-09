-- 管理员登录记录表
CREATE TABLE admin_login_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    admin_id INT NOT NULL COMMENT '管理员ID',
    admin_name VARCHAR(50) NOT NULL COMMENT '管理员用户名',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    logout_time DATETIME NULL COMMENT '登出时间',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    location VARCHAR(100) NULL COMMENT '登录地点',
    user_agent TEXT NULL COMMENT '用户代理信息',
    device_type VARCHAR(20) NULL COMMENT '设备类型(Desktop/Mobile/Tablet)',
    browser VARCHAR(50) NULL COMMENT '浏览器信息',
    os VARCHAR(50) NULL COMMENT '操作系统',
    login_status VARCHAR(20) NOT NULL DEFAULT 'success' COMMENT '登录状态(success/failed)',
    failure_reason VARCHAR(200) NULL COMMENT '失败原因',
    session_id VARCHAR(100) NULL COMMENT '会话ID',
    duration_seconds INT NULL COMMENT '会话时长(秒)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_admin_id (admin_id),
    INDEX idx_login_time (login_time),
    INDEX idx_ip_address (ip_address),
    INDEX idx_login_status (login_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员登录记录表';

-- 管理员操作日志表
CREATE TABLE admin_operation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    admin_id INT NOT NULL COMMENT '管理员ID',
    admin_name VARCHAR(50) NOT NULL COMMENT '管理员用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作名称',
    module VARCHAR(50) NOT NULL COMMENT '操作模块',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型(CREATE/READ/UPDATE/DELETE)',
    target_type VARCHAR(50) NULL COMMENT '操作目标类型',
    target_id VARCHAR(50) NULL COMMENT '操作目标ID',
    request_method VARCHAR(10) NOT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
    request_url VARCHAR(500) NOT NULL COMMENT '请求URL',
    request_params TEXT NULL COMMENT '请求参数',
    response_status INT NULL COMMENT '响应状态码',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    user_agent TEXT NULL COMMENT '用户代理信息',
    operation_result VARCHAR(20) NOT NULL DEFAULT 'success' COMMENT '操作结果(success/failed)',
    error_message TEXT NULL COMMENT '错误信息',
    execution_time_ms BIGINT NULL COMMENT '执行时间(毫秒)',
    description TEXT NULL COMMENT '操作描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_admin_id (admin_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_module (module),
    INDEX idx_create_time (create_time),
    INDEX idx_operation_result (operation_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

-- 管理员在线状态表
CREATE TABLE admin_online_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    admin_id INT NOT NULL UNIQUE COMMENT '管理员ID',
    admin_name VARCHAR(50) NOT NULL COMMENT '管理员用户名',
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    last_activity_time DATETIME NOT NULL COMMENT '最后活动时间',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    user_agent TEXT NULL COMMENT '用户代理信息',
    is_online TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否在线(1:在线,0:离线)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_admin_id (admin_id),
    INDEX idx_session_id (session_id),
    INDEX idx_is_online (is_online),
    INDEX idx_last_activity_time (last_activity_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员在线状态表';
