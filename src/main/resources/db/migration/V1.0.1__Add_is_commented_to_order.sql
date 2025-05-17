-- 为订单表添加评价状态列
ALTER TABLE `order` ADD COLUMN `is_commented` TINYINT(1) DEFAULT 0 COMMENT '是否已评价：0-未评价，1-已评价' AFTER `cancel_reason`; 