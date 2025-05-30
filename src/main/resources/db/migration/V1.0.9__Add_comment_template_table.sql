-- 创建评价模板表
CREATE TABLE IF NOT EXISTS `comment_template` (
  `template_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_content` varchar(1000) NOT NULL COMMENT '模板内容',
  `template_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '模板类型：1-系统预设，2-用户自定义',
  `min_rating` tinyint(4) DEFAULT NULL COMMENT '适用评分范围（最小值）',
  `max_rating` tinyint(4) DEFAULT NULL COMMENT '适用评分范围（最大值）',
  `category_id` int(11) DEFAULT NULL COMMENT '适用商品类别ID',
  `use_count` int(11) NOT NULL DEFAULT 0 COMMENT '使用次数',
  `user_id` int(11) DEFAULT NULL COMMENT '创建用户ID（系统模板为null）',
  `weight` int(11) NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`template_id`),
  KEY `idx_template_type` (`template_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='评价模板表';

-- 插入系统预设模板
INSERT INTO `comment_template` VALUES (1, '好评模板1', '质量非常好，物流很快，包装也很完整，非常满意的一次购物体验！', 1, 4, 5, NULL, 0, NULL, 100, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (2, '好评模板2', '商品质量很好，与描述一致，非常满意，下次还会购买！', 1, 4, 5, NULL, 0, NULL, 90, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (3, '好评模板3', '收到货了，包装很好，物流很快，商品质量也不错，价格也实惠，很满意！', 1, 4, 5, NULL, 0, NULL, 80, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (4, '中评模板1', '商品质量一般，没有想象中的那么好，但价格便宜，物有所值。', 1, 3, 3, NULL, 0, NULL, 70, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (5, '中评模板2', '商品基本符合描述，但做工一般，希望卖家能够改进。', 1, 3, 3, NULL, 0, NULL, 60, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (6, '差评模板1', '收到的商品与描述不符，质量较差，不太满意。', 1, 1, 2, NULL, 0, NULL, 50, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (7, '差评模板2', '物流太慢了，商品质量也一般，不是很满意。', 1, 1, 2, NULL, 0, NULL, 40, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (8, '商家好评回复', '亲爱的顾客，感谢您对我们产品的认可与支持！您的满意是我们最大的动力。如有任何关于产品使用的问题，欢迎随时联系我们的售后客服。我们将持续提供优质的母婴产品和服务，期待您的再次光临！', 1, 4, 5, NULL, 0, NULL, 200, 1, '2025-05-27 19:11:23', '2025-05-27 19:11:23');
INSERT INTO `comment_template` VALUES (9, '商家中评回复', '感谢您的购买和反馈！我们非常重视您的体验，对于没能完全满足您的期望深表歉意。我们会认真考虑您的建议，不断改进产品和服务质量。如有任何具体问题需要解决，欢迎联系客服，我们会为您提供专业的售后支持。', 1, 3, 3, NULL, 0, NULL, 180, 1, '2025-05-27 19:11:23', '2025-05-27 19:11:23');
INSERT INTO `comment_template` VALUES (10, '商家差评回复', '非常抱歉没能为您带来满意的购物体验！您的反馈对我们至关重要，我们已记录您反映的问题，将立即进行改进。为表歉意，请联系我们的客服处理售后事宜，我们会尽最大努力解决您遇到的问题，并提供相应的补偿方案。感谢您的理解和支持！', 1, 1, 2, NULL, 0, NULL, 160, 1, '2025-05-27 19:11:23', '2025-05-27 19:11:23');

-- 创建评价奖励配置表
CREATE TABLE IF NOT EXISTS `comment_reward_config` (
  `config_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `reward_type` varchar(50) NOT NULL COMMENT '奖励类型：points-积分',
  `reward_value` int(11) NOT NULL COMMENT '奖励值',
  `min_content_length` int(11) NOT NULL DEFAULT 0 COMMENT '最小内容长度要求',
  `require_image` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否要求图片：0-不要求，1-要求',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='评价奖励配置表';

-- 插入默认评价奖励配置
INSERT INTO `comment_reward_config` 
(`reward_type`, `reward_value`, `min_content_length`, `require_image`, `status`) 
VALUES 
('points', 5, 0, 0, 1),
('points', 10, 50, 0, 1),
('points', 15, 50, 1, 1); 