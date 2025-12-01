-- 搜索统计表
-- 用于记录用户搜索行为和统计热门搜索词

DROP TABLE IF EXISTS `search_statistics`;
CREATE TABLE `search_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `keyword` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '搜索关键词',
  `search_count` int NOT NULL DEFAULT 1 COMMENT '搜索次数',
  `result_count` bigint DEFAULT 0 COMMENT '搜索结果数量',
  `user_id` int UNSIGNED DEFAULT NULL COMMENT '用户ID',
  `source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'web' COMMENT '搜索来源：web, mobile, api',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户代理',
  `search_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
  `response_time` bigint DEFAULT NULL COMMENT '响应时间（毫秒）',
  `has_click` tinyint(1) DEFAULT 0 COMMENT '是否有点击结果',
  `clicked_product_id` int DEFAULT NULL COMMENT '点击的商品ID',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '搜索会话ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_keyword` (`keyword`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_search_time` (`search_time`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_result_count` (`result_count`),
  CONSTRAINT `fk_search_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索统计表';

-- 用户搜索历史表
-- 用于存储用户的搜索历史记录
DROP TABLE IF EXISTS `user_search_history`;
CREATE TABLE `user_search_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `keyword` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '搜索关键词',
  `search_count` int NOT NULL DEFAULT 1 COMMENT '搜索次数',
  `last_search_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后搜索时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_user_keyword` (`user_id`, `keyword`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_last_search_time` (`last_search_time`),
  CONSTRAINT `fk_search_history_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户搜索历史表';
