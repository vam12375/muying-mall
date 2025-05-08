-- 检查主要表是否包含version字段
-- 这个脚本帮助识别那些需要使用乐观锁但缺少version字段的表

-- 检查order表
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN 'order表已包含version字段'
        ELSE 'order表缺少version字段'
    END AS check_result
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'order'
AND COLUMN_NAME = 'version';

-- 检查payment表
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN 'payment表已包含version字段'
        ELSE 'payment表缺少version字段'
    END AS check_result
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'payment'
AND COLUMN_NAME = 'version';

-- 检查user表
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN 'user表已包含version字段'
        ELSE 'user表缺少version字段'
    END AS check_result
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'user'
AND COLUMN_NAME = 'version';

-- 检查product表
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN 'product表已包含version字段'
        ELSE 'product表缺少version字段'
    END AS check_result
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'product'
AND COLUMN_NAME = 'version';

-- 列出所有可能需要乐观锁控制的表
SELECT 
    DISTINCT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME IN ('order', 'payment', 'user', 'product', 'cart', 'coupon', 'points_exchange')
AND TABLE_NAME NOT IN (
    SELECT DISTINCT TABLE_NAME 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND COLUMN_NAME = 'version'
); 