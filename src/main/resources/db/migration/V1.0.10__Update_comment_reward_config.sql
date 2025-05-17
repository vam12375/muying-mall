-- 更新评价奖励配置表，添加更多字段支持多层级奖励
ALTER TABLE `comment_reward_config`
ADD COLUMN `reward_name` varchar(50) NOT NULL DEFAULT '基础奖励' COMMENT '奖励名称' AFTER `reward_type`,
ADD COLUMN `reward_description` varchar(200) DEFAULT NULL COMMENT '奖励描述' AFTER `reward_name`,
ADD COLUMN `reward_level` tinyint(4) NOT NULL DEFAULT 1 COMMENT '奖励等级：1-基础，2-进阶，3-高级' AFTER `reward_description`,
ADD COLUMN `min_rating` tinyint(4) DEFAULT NULL COMMENT '最低评分要求' AFTER `min_content_length`,
ADD COLUMN `min_images` tinyint(4) NOT NULL DEFAULT 0 COMMENT '最低图片数量要求' AFTER `require_image`,
ADD COLUMN `is_first_comment` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否首次评价奖励：0-否，1-是' AFTER `min_images`;

-- 清空现有配置
TRUNCATE TABLE `comment_reward_config`;

-- 插入多层级奖励配置
INSERT INTO `comment_reward_config` 
(`reward_type`, `reward_name`, `reward_description`, `reward_level`, `reward_value`, `min_content_length`, `min_rating`, `require_image`, `min_images`, `is_first_comment`, `status`) 
VALUES 
-- 基础奖励
('points', '基础评价奖励', '完成订单评价获得基础积分', 1, 5, 0, NULL, 0, 0, 0, 1),

-- 质量奖励
('points', '优质评价奖励', '评价内容详细，字数超过50字', 2, 10, 50, NULL, 0, 0, 0, 1),
('points', '图文评价奖励', '评价包含图片，更加直观', 2, 15, 0, NULL, 1, 1, 0, 1),
('points', '高质量图文评价', '评价内容详细且包含图片', 3, 20, 50, NULL, 1, 1, 0, 1),
('points', '多图评价奖励', '评价包含3张及以上图片', 3, 25, 0, NULL, 1, 3, 0, 1),

-- 首评奖励
('points', '首次评价奖励', '首次评价商品获得额外奖励', 2, 10, 0, NULL, 0, 0, 1, 1),

-- 评分相关奖励
('points', '好评奖励', '给出4-5星好评', 1, 8, 0, 4, 0, 0, 0, 1); 