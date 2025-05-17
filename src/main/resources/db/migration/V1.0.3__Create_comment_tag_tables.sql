-- 创建评价标签表
CREATE TABLE IF NOT EXISTS `comment_tag` (
  `tag_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
  `tag_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '标签类型：1-系统标签，2-用户自定义标签',
  `product_category_id` int(11) DEFAULT NULL COMMENT '关联的商品分类ID（可为空）',
  `usage_count` int(11) NOT NULL DEFAULT '0' COMMENT '使用次数',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`),
  KEY `idx_product_category` (`product_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价标签表';

-- 创建评价标签关系表
CREATE TABLE IF NOT EXISTS `comment_tag_relation` (
  `relation_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `comment_id` int(11) NOT NULL COMMENT '评价ID',
  `tag_id` int(11) NOT NULL COMMENT '标签ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_comment_tag` (`comment_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价标签关系表';

-- 添加外键约束（如果需要）
ALTER TABLE `comment_tag_relation` 
  ADD CONSTRAINT `fk_tag_relation_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`comment_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_tag_relation_tag` FOREIGN KEY (`tag_id`) REFERENCES `comment_tag` (`tag_id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 插入一些默认标签
INSERT INTO `comment_tag` (`tag_name`, `tag_type`, `product_category_id`, `status`) VALUES
('物流快', 1, NULL, 1),
('质量好', 1, NULL, 1),
('性价比高', 1, NULL, 1),
('描述相符', 1, NULL, 1),
('服务好', 1, NULL, 1),
('包装精美', 1, NULL, 1),
('送货快', 1, NULL, 1),
('正品保障', 1, NULL, 1),
('材质优良', 1, NULL, 1),
('做工精细', 1, NULL, 1); 