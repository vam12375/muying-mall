-- ============================================
-- 用户账户表添加积分和优惠券字段
-- 用途：为 user_account 表添加 points 和 coupon_count 字段
-- ============================================

-- 1. 检查并添加 points 字段（积分）
ALTER TABLE user_account 
ADD COLUMN IF NOT EXISTS points INT DEFAULT 0 COMMENT '用户积分' AFTER balance;

-- 2. 检查并添加 coupon_count 字段（优惠券数量）
ALTER TABLE user_account 
ADD COLUMN IF NOT EXISTS coupon_count INT DEFAULT 0 COMMENT '优惠券数量' AFTER points;

-- 3. 更新现有记录的默认值
UPDATE user_account 
SET points = COALESCE(points, 0),
    coupon_count = COALESCE(coupon_count, 0)
WHERE points IS NULL OR coupon_count IS NULL;

-- 4. 验证字段是否添加成功
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'user_account'
  AND COLUMN_NAME IN ('points', 'coupon_count');

-- 5. 查看表结构
DESC user_account;

-- 6. 查看示例数据
SELECT 
    account_id,
    user_id,
    balance,
    points,
    coupon_count,
    status,
    create_time
FROM user_account
LIMIT 5;
