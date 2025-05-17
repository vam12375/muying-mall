-- 创建评价表
CREATE TABLE IF NOT EXISTS `comment` (
  `comment_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `order_id` int(11) NOT NULL COMMENT '订单ID',
  `content` text COMMENT '评价内容',
  `rating` tinyint(1) DEFAULT '5' COMMENT '评分(1-5星)',
  `images` varchar(500) DEFAULT NULL COMMENT '图片(英文逗号分隔)',
  `is_anonymous` tinyint(1) DEFAULT '0' COMMENT '是否匿名(0-否,1-是)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0-隐藏,1-显示)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表'; 