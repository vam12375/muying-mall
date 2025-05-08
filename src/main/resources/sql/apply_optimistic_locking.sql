-- 为多个表添加乐观锁支持
-- 这个脚本为需要乐观锁控制的主要表添加version字段

-- 定义SQL程序块，以便安全执行ALTER TABLE语句
DELIMITER //

-- 为order表添加version字段
DROP PROCEDURE IF EXISTS add_version_to_order;
CREATE PROCEDURE add_version_to_order()
BEGIN
    DECLARE column_exists INT;
    SELECT COUNT(*) INTO column_exists FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order' AND COLUMN_NAME = 'version';
    
    IF column_exists = 0 THEN
        ALTER TABLE `order` ADD COLUMN `version` INT NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁控制';
        ALTER TABLE `order` ADD INDEX `idx_version` (`version`);
        SELECT 'Added version column to order table' AS result;
    ELSE
        SELECT 'Version column already exists in order table' AS result;
    END IF;
END//

-- 为payment表添加version字段
DROP PROCEDURE IF EXISTS add_version_to_payment;
CREATE PROCEDURE add_version_to_payment()
BEGIN
    DECLARE column_exists INT;
    SELECT COUNT(*) INTO column_exists FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment' AND COLUMN_NAME = 'version';
    
    IF column_exists = 0 THEN
        ALTER TABLE `payment` ADD COLUMN `version` INT NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁控制';
        ALTER TABLE `payment` ADD INDEX `idx_version` (`version`);
        SELECT 'Added version column to payment table' AS result;
    ELSE
        SELECT 'Version column already exists in payment table' AS result;
    END IF;
END//

-- 为user表添加version字段
DROP PROCEDURE IF EXISTS add_version_to_user;
CREATE PROCEDURE add_version_to_user()
BEGIN
    DECLARE column_exists INT;
    SELECT COUNT(*) INTO column_exists FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'version';
    
    IF column_exists = 0 THEN
        ALTER TABLE `user` ADD COLUMN `version` INT NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁控制';
        ALTER TABLE `user` ADD INDEX `idx_version` (`version`);
        SELECT 'Added version column to user table' AS result;
    ELSE
        SELECT 'Version column already exists in user table' AS result;
    END IF;
END//

-- 为product表添加version字段
DROP PROCEDURE IF EXISTS add_version_to_product;
CREATE PROCEDURE add_version_to_product()
BEGIN
    DECLARE column_exists INT;
    SELECT COUNT(*) INTO column_exists FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product' AND COLUMN_NAME = 'version';
    
    IF column_exists = 0 THEN
        ALTER TABLE `product` ADD COLUMN `version` INT NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁控制';
        ALTER TABLE `product` ADD INDEX `idx_version` (`version`);
        SELECT 'Added version column to product table' AS result;
    ELSE
        SELECT 'Version column already exists in product table' AS result;
    END IF;
END//

-- 定义执行所有添加版本字段的主程序
DROP PROCEDURE IF EXISTS apply_optimistic_locking;
CREATE PROCEDURE apply_optimistic_locking()
BEGIN
    CALL add_version_to_order();
    CALL add_version_to_payment();
    CALL add_version_to_user();
    CALL add_version_to_product();
    
    SELECT 'Applied optimistic locking to all required tables' AS final_result;
END//

DELIMITER ;

-- 执行主程序
CALL apply_optimistic_locking();

-- 清理临时存储过程
DROP PROCEDURE IF EXISTS add_version_to_order;
DROP PROCEDURE IF EXISTS add_version_to_payment;
DROP PROCEDURE IF EXISTS add_version_to_user;
DROP PROCEDURE IF EXISTS add_version_to_product;
DROP PROCEDURE IF EXISTS apply_optimistic_locking; 