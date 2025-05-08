-- 添加version列到order表
ALTER TABLE `order`
ADD COLUMN `version` INT NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁控制';

-- 添加version列到payment表(如果还没有)
ALTER TABLE `payment`
ADD COLUMN `version` INT NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁控制';

-- 为version列添加索引以提高性能
ALTER TABLE `order`
ADD INDEX `idx_version` (`version`);

ALTER TABLE `payment`
ADD INDEX `idx_version` (`version`);

-- 输出提示信息
SELECT 'Version columns added successfully' AS message;