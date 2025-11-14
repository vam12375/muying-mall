-- ========================================
-- 用户管理模块诊断脚本
-- User Module Diagnosis Script
-- ========================================

-- 1. 检查当前数据库
SELECT DATABASE() AS current_database;

-- 2. 检查 user 表是否存在
SELECT COUNT(*) AS user_table_exists 
FROM information_schema.tables 
WHERE table_schema = 'muying_mall' 
  AND table_name = 'user';

-- 3. 检查 phone 字段是否存在
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_KEY,
    COLUMN_COMMENT
FROM information_schema.columns 
WHERE table_schema = 'muying_mall' 
  AND table_name = 'user' 
  AND column_name = 'phone';

-- 4. 查看 user 表完整结构
DESCRIBE user;

-- 5. 查看 user_account 表完整结构
DESCRIBE user_account;

-- 6. 测试实际的 JOIN 查询（模拟后端 SQL）
SELECT 
  ua.account_id, 
  ua.user_id, 
  ua.balance, 
  ua.total_recharge, 
  ua.total_consumption, 
  ua.status, 
  ua.create_time, 
  ua.update_time,
  u.username, 
  u.nickname, 
  u.email, 
  u.phone
FROM user_account ua
LEFT JOIN user u ON ua.user_id = u.user_id
LIMIT 5;

-- 7. 检查数据量
SELECT 
    'user' AS table_name, 
    COUNT(*) AS record_count 
FROM user
UNION ALL
SELECT 
    'user_account' AS table_name, 
    COUNT(*) AS record_count 
FROM user_account;

-- 8. 检查是否有 phone 字段为 NULL 的用户
SELECT 
    user_id,
    username,
    nickname,
    email,
    phone,
    CASE 
        WHEN phone IS NULL THEN 'NULL'
        WHEN phone = '' THEN 'EMPTY'
        ELSE 'HAS_VALUE'
    END AS phone_status
FROM user
LIMIT 10;

-- 9. 检查 user_account 和 user 的关联情况
SELECT 
    COUNT(DISTINCT ua.user_id) AS accounts_with_user,
    COUNT(DISTINCT u.user_id) AS total_users,
    (SELECT COUNT(*) FROM user_account) AS total_accounts
FROM user_account ua
LEFT JOIN user u ON ua.user_id = u.user_id;

-- 10. 检查是否有孤立的 user_account（没有对应的 user）
SELECT 
    ua.account_id,
    ua.user_id,
    ua.balance,
    CASE 
        WHEN u.user_id IS NULL THEN 'ORPHAN'
        ELSE 'OK'
    END AS status
FROM user_account ua
LEFT JOIN user u ON ua.user_id = u.user_id
WHERE u.user_id IS NULL;

-- ========================================
-- 诊断结果说明
-- ========================================
-- 
-- 1. current_database: 应该显示 'muying_mall'
-- 2. user_table_exists: 应该显示 1
-- 3. phone 字段信息: 应该显示字段详情
-- 4. DESCRIBE user: 应该包含 phone 字段
-- 5. JOIN 查询: 应该成功返回数据
-- 6. 数据量: 应该显示表中的记录数
-- 7. phone_status: 检查 phone 字段的数据状态
-- 8. 关联情况: 检查表之间的关联是否正常
-- 9. 孤立记录: 应该没有孤立的 user_account
-- 
-- ========================================
