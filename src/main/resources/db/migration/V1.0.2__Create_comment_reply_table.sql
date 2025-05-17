-- 创建评价回复表
CREATE TABLE IF NOT EXISTS `comment_reply` (
  `reply_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `comment_id` int(11) NOT NULL COMMENT '评价ID',
  `content` varchar(500) NOT NULL COMMENT '回复内容',
  `reply_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '回复类型：1-商家回复，2-用户追评',
  `reply_user_id` int(11) DEFAULT NULL COMMENT '回复用户ID（商家回复时为管理员ID）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`reply_id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价回复表';

-- 添加外键约束（如果需要）
ALTER TABLE `comment_reply` 
  ADD CONSTRAINT `fk_comment_reply_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`comment_id`) ON DELETE CASCADE ON UPDATE CASCADE; 