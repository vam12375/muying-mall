-- ============================================
-- 育儿圈功能模块数据库表设计
-- 创建时间: 2025-12-08
-- ============================================

-- 1. 话题标签表
CREATE TABLE IF NOT EXISTS `circle_topic` (
    `topic_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '话题ID',
    `name` VARCHAR(50) NOT NULL COMMENT '话题名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '话题图标',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '话题描述',
    `post_count` INT DEFAULT 0 COMMENT '帖子数量',
    `follow_count` INT DEFAULT 0 COMMENT '关注数量',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='育儿圈话题表';

-- 2. 帖子表
CREATE TABLE IF NOT EXISTS `circle_post` (
    `post_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `topic_id` INT DEFAULT NULL COMMENT '话题ID',
    `content` TEXT NOT NULL COMMENT '帖子内容',
    `images` TEXT DEFAULT NULL COMMENT '图片列表(JSON数组)',
    `product_id` INT DEFAULT NULL COMMENT '关联商品ID',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `share_count` INT DEFAULT 0 COMMENT '分享数',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    `is_hot` TINYINT DEFAULT 0 COMMENT '是否热门：0-否，1-是',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-删除，1-正常，2-审核中',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_topic_id` (`topic_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_status_create` (`status`, `create_time` DESC),
    KEY `idx_hot` (`is_hot`, `like_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='育儿圈帖子表';

-- 3. 帖子评论表
CREATE TABLE IF NOT EXISTS `circle_comment` (
    `comment_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID(回复)',
    `reply_user_id` INT DEFAULT NULL COMMENT '被回复用户ID',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-删除，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='育儿圈评论表';


-- 4. 点赞表（帖子和评论通用）
CREATE TABLE IF NOT EXISTS `circle_like` (
    `like_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `target_id` BIGINT NOT NULL COMMENT '目标ID(帖子ID或评论ID)',
    `target_type` TINYINT NOT NULL COMMENT '目标类型：1-帖子，2-评论',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
    KEY `idx_target` (`target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='育儿圈点赞表';

-- 5. 用户关注表
CREATE TABLE IF NOT EXISTS `circle_follow` (
    `follow_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关注ID',
    `user_id` INT NOT NULL COMMENT '用户ID(关注者)',
    `follow_user_id` INT NOT NULL COMMENT '被关注用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
    KEY `idx_follow_user` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='育儿圈关注表';

-- 初始化默认话题
INSERT INTO `circle_topic` (`name`, `description`, `sort_order`) VALUES
('晒娃日常', '分享宝宝的可爱瞬间', 1),
('好物推荐', '分享母婴好物使用心得', 2),
('育儿经验', '交流育儿技巧和经验', 3),
('孕期记录', '记录孕期的点点滴滴', 4),
('辅食分享', '分享宝宝辅食制作', 5),
('成长记录', '记录宝宝成长里程碑', 6);


-- 添加SKU相关字段到帖子表
ALTER TABLE `circle_post` ADD COLUMN `sku_id` INT DEFAULT NULL COMMENT '关联SKU ID' AFTER `product_id`;
ALTER TABLE `circle_post` ADD COLUMN `sku_specs` VARCHAR(500) DEFAULT NULL COMMENT 'SKU规格信息(JSON)' AFTER `sku_id`;

-- 6. 消息通知表
CREATE TABLE IF NOT EXISTS `circle_message` (
    `message_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `user_id` INT NOT NULL COMMENT '接收用户ID',
    `from_user_id` INT NOT NULL COMMENT '发送用户ID',
    `type` TINYINT NOT NULL COMMENT '消息类型：1-点赞帖子，2-点赞评论，3-评论，4-回复，5-关注',
    `target_id` BIGINT NOT NULL COMMENT '目标ID(帖子ID或评论ID)',
    `content` VARCHAR(255) DEFAULT NULL COMMENT '消息内容摘要',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_user_time` (`user_id`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='育儿圈消息通知表';
