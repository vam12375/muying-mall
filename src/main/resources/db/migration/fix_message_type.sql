-- 修复user_message表的type字段和清理旧消息
-- 将大写格式和数字格式统一为小写下划线格式
-- 执行前请备份数据库！

-- ========================================
-- 第一部分：统一type字段格式
-- ========================================

-- 1. 将大写格式转换为小写格式
UPDATE user_message SET type = 'order' WHERE type = 'ORDER';
UPDATE user_message SET type = 'system' WHERE type = 'SYSTEM';
UPDATE user_message SET type = 'remind' WHERE type = 'REMIND';
UPDATE user_message SET type = 'points' WHERE type = 'POINTS';
UPDATE user_message SET type = 'checkin' WHERE type = 'CHECKIN';
UPDATE user_message SET type = 'shipping_reminder' WHERE type = 'SHIPPING_REMINDER';
UPDATE user_message SET type = 'coupon' WHERE type = 'COUPON';
UPDATE user_message SET type = 'comment_reward' WHERE type = 'COMMENT_REWARD';

-- 2. 将数字格式转换为小写格式
UPDATE user_message SET type = 'order' WHERE type = '1';
UPDATE user_message SET type = 'remind' WHERE type = '2';
UPDATE user_message SET type = 'system' WHERE type = '3';
UPDATE user_message SET type = 'points' WHERE type = '4';
UPDATE user_message SET type = 'checkin' WHERE type = '5';

-- ========================================
-- 第二部分：清理旧的英文状态码消息
-- ========================================

-- 删除包含英文状态码的订单消息（这些是旧格式，会被新消息替代）
DELETE FROM user_message 
WHERE type = 'order' 
  AND (content LIKE '%[pending_shipment]%' 
    OR content LIKE '%[shipped]%'
    OR content LIKE '%[completed]%'
    OR content LIKE '%[pending_payment]%'
    OR content LIKE '%[cancelled]%');

-- ========================================
-- 第三部分：查看清理结果
-- ========================================

-- 查看各类型消息数量
SELECT type, COUNT(*) as count 
FROM user_message 
WHERE status = 1
GROUP BY type 
ORDER BY count DESC;

-- 查看最近的订单消息（验证是否还有英文状态码）
SELECT message_id, type, title, content, create_time
FROM user_message 
WHERE type = 'order' 
  AND status = 1
ORDER BY create_time DESC 
LIMIT 10;
