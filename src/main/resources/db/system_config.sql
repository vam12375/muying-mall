-- 系统配置表
-- 用于存储系统的各项配置参数

CREATE TABLE IF NOT EXISTS `system_config` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键名',
    `config_value` TEXT COMMENT '配置值',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称（中文描述）',
    `config_group` VARCHAR(50) NOT NULL COMMENT '配置分组：basic-基础配置, business-业务配置, points-积分配置, payment-支付配置, logistics-物流配置, upload-上传配置',
    `value_type` VARCHAR(20) NOT NULL DEFAULT 'string' COMMENT '值类型：string-字符串, number-数字, boolean-布尔, json-JSON对象, image-图片',
    `description` VARCHAR(500) COMMENT '配置描述',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_config_group` (`config_group`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 注意：默认配置数据会在应用启动时自动初始化，无需手动插入
