-- 搜索统计表
CREATE TABLE IF NOT EXISTS `search_statistics` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `keyword` VARCHAR(255) NOT NULL COMMENT '搜索关键词',
    `search_count` INT(11) NOT NULL DEFAULT 1 COMMENT '搜索次数',
    `result_count` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '搜索结果数量',
    `user_id` INT(11) NULL DEFAULT NULL COMMENT '用户ID',
    `source` VARCHAR(50) NOT NULL DEFAULT 'web' COMMENT '搜索来源：web, mobile, api等',
    `ip_address` VARCHAR(45) NULL DEFAULT NULL COMMENT '搜索IP地址',
    `user_agent` TEXT NULL DEFAULT NULL COMMENT '用户代理',
    `search_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    `response_time` BIGINT(20) NULL DEFAULT NULL COMMENT '响应时间（毫秒）',
    `has_click` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否有点击结果',
    `clicked_product_id` INT(11) NULL DEFAULT NULL COMMENT '点击的商品ID',
    `session_id` VARCHAR(255) NULL DEFAULT NULL COMMENT '搜索会话ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_keyword` (`keyword`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_search_time` (`search_time`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_keyword_user_time` (`keyword`, `user_id`, `create_time`),
    INDEX `idx_result_count` (`result_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索统计表';

-- 创建分区表（按月分区，提高查询性能）
-- ALTER TABLE search_statistics PARTITION BY RANGE (YEAR(create_time) * 100 + MONTH(create_time)) (
--     PARTITION p202401 VALUES LESS THAN (202402),
--     PARTITION p202402 VALUES LESS THAN (202403),
--     PARTITION p202403 VALUES LESS THAN (202404),
--     PARTITION p202404 VALUES LESS THAN (202405),
--     PARTITION p202405 VALUES LESS THAN (202406),
--     PARTITION p202406 VALUES LESS THAN (202407),
--     PARTITION p202407 VALUES LESS THAN (202408),
--     PARTITION p202408 VALUES LESS THAN (202409),
--     PARTITION p202409 VALUES LESS THAN (202410),
--     PARTITION p202410 VALUES LESS THAN (202411),
--     PARTITION p202411 VALUES LESS THAN (202412),
--     PARTITION p202412 VALUES LESS THAN (202501),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 插入一些示例数据
INSERT INTO `search_statistics` (`keyword`, `search_count`, `result_count`, `user_id`, `source`, `search_time`) VALUES
('奶粉', 156, 45, NULL, 'web', '2024-01-15 10:30:00'),
('纸尿裤', 134, 38, NULL, 'web', '2024-01-15 11:15:00'),
('婴儿车', 98, 22, NULL, 'web', '2024-01-15 14:20:00'),
('奶瓶', 87, 31, NULL, 'web', '2024-01-15 16:45:00'),
('玩具', 76, 89, NULL, 'web', '2024-01-15 18:30:00'),
('辅食', 65, 27, NULL, 'web', '2024-01-16 09:15:00'),
('童装', 54, 156, NULL, 'web', '2024-01-16 13:20:00'),
('安全座椅', 43, 12, NULL, 'web', '2024-01-16 15:45:00'),
('洗护用品', 38, 67, NULL, 'web', '2024-01-16 17:30:00'),
('益智玩具', 32, 45, NULL, 'web', '2024-01-16 19:15:00');

-- 创建视图：热门搜索关键词
CREATE OR REPLACE VIEW `v_hot_search_keywords` AS
SELECT 
    `keyword`,
    SUM(`search_count`) AS `total_searches`,
    AVG(`result_count`) AS `avg_results`,
    COUNT(DISTINCT `user_id`) AS `unique_users`,
    MAX(`search_time`) AS `last_search_time`
FROM `search_statistics`
WHERE `create_time` >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY `keyword`
ORDER BY `total_searches` DESC;

-- 创建视图：搜索统计概览
CREATE OR REPLACE VIEW `v_search_overview` AS
SELECT 
    DATE(`search_time`) AS `search_date`,
    COUNT(DISTINCT `keyword`) AS `unique_keywords`,
    SUM(`search_count`) AS `total_searches`,
    AVG(`result_count`) AS `avg_results`,
    AVG(`response_time`) AS `avg_response_time`,
    SUM(CASE WHEN `has_click` = 1 THEN 1 ELSE 0 END) AS `click_count`,
    SUM(CASE WHEN `result_count` = 0 THEN 1 ELSE 0 END) AS `no_result_count`
FROM `search_statistics`
WHERE `search_time` >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(`search_time`)
ORDER BY `search_date` DESC;

-- 创建存储过程：清理过期数据
DELIMITER //
CREATE PROCEDURE `CleanExpiredSearchStatistics`(IN days_to_keep INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE affected_rows INT DEFAULT 0;
    
    -- 删除过期数据
    DELETE FROM `search_statistics` 
    WHERE `create_time` < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
    
    SET affected_rows = ROW_COUNT();
    
    -- 记录清理日志
    SELECT CONCAT('清理了 ', affected_rows, ' 条过期搜索统计记录') AS result;
    
END //
DELIMITER ;

-- 创建事件：自动清理过期数据（每天凌晨2点执行，保留90天数据）
-- SET GLOBAL event_scheduler = ON;
-- CREATE EVENT IF NOT EXISTS `auto_clean_search_statistics`
-- ON SCHEDULE EVERY 1 DAY
-- STARTS '2024-01-01 02:00:00'
-- DO
--   CALL CleanExpiredSearchStatistics(90);
