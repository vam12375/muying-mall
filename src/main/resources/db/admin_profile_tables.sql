-- 管理员个人中心相关表
-- Admin Profile Tables
-- 
-- Source: 基于 AdminLoginRecord 和 AdminOperationLog 实体
-- 遵循 KISS, YAGNI, SOLID 原则
-- 
-- 说明：admin_id 关联到 user 表的 user_id（role='admin'的用户）

-- 管理员登录记录表
CREATE TABLE IF NOT EXISTS `admin_login_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` INT UNSIGNED NOT NULL COMMENT '管理员ID（关联user表的user_id，role=admin）',
  `admin_name` VARCHAR(50) NOT NULL COMMENT '管理员用户名',
  `login_time` DATETIME NOT NULL COMMENT '登录时间',
  `logout_time` DATETIME DEFAULT NULL COMMENT '登出时间',
  `ip_address` VARCHAR(50) NOT NULL COMMENT 'IP地址',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理信息',
  `device_type` VARCHAR(20) DEFAULT NULL COMMENT '设备类型(Desktop/Mobile/Tablet)',
  `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器信息',
  `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
  `login_status` VARCHAR(20) NOT NULL DEFAULT 'success' COMMENT '登录状态(success/failed)',
  `failure_reason` VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
  `session_id` VARCHAR(100) DEFAULT NULL COMMENT '会话ID',
  `duration_seconds` INT DEFAULT NULL COMMENT '会话时长(秒)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_admin_id` (`admin_id`),
  INDEX `idx_login_time` (`login_time`),
  INDEX `idx_ip_address` (`ip_address`),
  INDEX `idx_session_id` (`session_id`),
  CONSTRAINT `fk_admin_login_user` FOREIGN KEY (`admin_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员登录记录表（admin_id关联user表）';

-- 管理员操作日志表
CREATE TABLE IF NOT EXISTS `admin_operation_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` INT UNSIGNED NOT NULL COMMENT '管理员ID（关联user表的user_id，role=admin）',
  `admin_name` VARCHAR(50) NOT NULL COMMENT '管理员用户名',
  `operation` VARCHAR(100) NOT NULL COMMENT '操作名称',
  `module` VARCHAR(50) NOT NULL COMMENT '操作模块',
  `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型(CREATE/READ/UPDATE/DELETE/EXPORT/IMPORT/LOGIN/LOGOUT)',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '操作目标类型',
  `target_id` VARCHAR(100) DEFAULT NULL COMMENT '操作目标ID',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
  `response_status` INT DEFAULT NULL COMMENT '响应状态码',
  `ip_address` VARCHAR(50) NOT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理信息',
  `operation_result` VARCHAR(20) NOT NULL DEFAULT 'success' COMMENT '操作结果(success/failed)',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `execution_time_ms` BIGINT DEFAULT NULL COMMENT '执行时间(毫秒)',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_admin_id` (`admin_id`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_operation_type` (`operation_type`),
  INDEX `idx_module` (`module`),
  INDEX `idx_operation_result` (`operation_result`),
  CONSTRAINT `fk_admin_operation_user` FOREIGN KEY (`admin_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表（admin_id关联user表）';

-- 管理员在线状态表
CREATE TABLE IF NOT EXISTS `admin_online_status` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` INT UNSIGNED NOT NULL COMMENT '管理员ID（关联user表的user_id，role=admin）',
  `admin_name` VARCHAR(50) NOT NULL COMMENT '管理员用户名',
  `session_id` VARCHAR(100) NOT NULL COMMENT '会话ID',
  `login_time` DATETIME NOT NULL COMMENT '登录时间',
  `last_activity_time` DATETIME NOT NULL COMMENT '最后活动时间',
  `ip_address` VARCHAR(50) NOT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理信息',
  `is_online` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否在线(1:在线,0:离线)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  INDEX `idx_admin_id` (`admin_id`),
  INDEX `idx_is_online` (`is_online`),
  INDEX `idx_last_activity_time` (`last_activity_time`),
  CONSTRAINT `fk_admin_online_user` FOREIGN KEY (`admin_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员在线状态表（admin_id关联user表）';

-- 插入示例数据（可选）
-- INSERT INTO `admin_login_records` (`admin_id`, `admin_name`, `login_time`, `ip_address`, `location`, `device_type`, `browser`, `os`, `login_status`, `session_id`)
-- VALUES 
-- (1, 'admin', NOW(), '127.0.0.1', '本地', 'Desktop', 'Chrome', 'Windows', 'success', 'test-session-1'),
-- (1, 'admin', DATE_SUB(NOW(), INTERVAL 1 DAY), '127.0.0.1', '本地', 'Mobile', 'Safari', 'iOS', 'success', 'test-session-2');

-- INSERT INTO `admin_operation_logs` (`admin_id`, `admin_name`, `operation`, `module`, `operation_type`, `request_method`, `request_url`, `response_status`, `ip_address`, `operation_result`)
-- VALUES
-- (1, 'admin', '查看商品列表', '商品管理', 'READ', 'GET', '/admin/products', 200, '127.0.0.1', 'success'),
-- (1, 'admin', '新增商品', '商品管理', 'CREATE', 'POST', '/admin/products', 200, '127.0.0.1', 'success'),
-- (1, 'admin', '编辑订单', '订单管理', 'UPDATE', 'PUT', '/admin/orders/1', 200, '127.0.0.1', 'success');
