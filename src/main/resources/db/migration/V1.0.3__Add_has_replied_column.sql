-- 添加has_replied字段到comment表
ALTER TABLE `comment` ADD COLUMN `has_replied` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已回复：0-否，1-是';

-- 更新现有数据：根据comment_reply表设置has_replied值
UPDATE `comment` c 
SET c.`has_replied` = 1 
WHERE EXISTS (
    SELECT 1 FROM `comment_reply` cr 
    WHERE cr.`comment_id` = c.`comment_id`
); 