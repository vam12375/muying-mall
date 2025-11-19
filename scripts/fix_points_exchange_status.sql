-- 修复 points_exchange 表的 status 字段类型
-- 将 status 从 tinyint 改为 varchar 以支持字符串状态值
-- Source: 修复积分兑换状态字段类型不匹配问题

USE muying_mall;

-- 1. 备份现有数据的状态映射
-- 0:待发货 -> pending
-- 1:已发货 -> shipped  
-- 2:已完成 -> completed
-- 3:已取消 -> cancelled

-- 2. 添加临时字段
ALTER TABLE `points_exchange` 
ADD COLUMN `status_new` VARCHAR(20) NULL COMMENT '新状态字段(pending:待发货,shipped:已发货,completed:已完成,cancelled:已取消)' AFTER `status`;

-- 3. 迁移数据：将整数状态转换为字符串状态
UPDATE `points_exchange` 
SET `status_new` = CASE 
    WHEN `status` = 0 THEN 'pending'
    WHEN `status` = 1 THEN 'shipped'
    WHEN `status` = 2 THEN 'completed'
    WHEN `status` = 3 THEN 'cancelled'
    ELSE 'pending'
END;

-- 4. 删除旧的 status 字段
ALTER TABLE `points_exchange` DROP COLUMN `status`;

-- 5. 重命名新字段为 status
ALTER TABLE `points_exchange` 
CHANGE COLUMN `status_new` `status` VARCHAR(20) NOT NULL DEFAULT 'pending' 
COMMENT '状态(pending:待发货,shipped:已发货,completed:已完成,cancelled:已取消)';

-- 6. 重建索引
ALTER TABLE `points_exchange` DROP INDEX `idx_status`;
ALTER TABLE `points_exchange` ADD INDEX `idx_status`(`status` ASC) USING BTREE;

-- 完成
SELECT '积分兑换表 status 字段类型修复完成' AS message;
