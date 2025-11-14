-- ========================================
-- 个人中心诊断脚本
-- Profile Diagnosis Script
-- ========================================

-- 1. 检查当前数据库
SELECT DATABASE() AS current_database;

-- 2. 检查admin用户的完整信息
SELECT 
    user_id,
    username,
    nickname,
    email,
    phone,
    role,
    status,
    CASE 
        WHEN status = 0 THEN '禁用'
        WHEN status = 1 THEN '正常'
        ELSE CONCAT('未知状态: ', status)
    END AS status_text,
    create_time,
    update_time
FROM user
WHERE role = 'admin'
ORDER BY user_id;

-- 3. 检查是否有status为NULL的admin用户
SELECT 
    user_id,
    username,
    nickname,
    status,
    CASE 
        WHEN status IS NULL THEN 'NULL'
        WHEN status = 0 THEN '禁用'
        WHEN status = 1 THEN '正常'
        ELSE CONCAT('异常值: ', status)
    END AS status_check
FROM user
WHERE role = 'admin';

-- 4. 检查admin_login_record表是否有登录记录
SELECT 
    COUNT(*) AS total_login_records,
    COUNT(DISTINCT admin_id) AS unique_admins
FROM admin_login_record;

-- 5. 检查特定admin用户的登录记录
SELECT 
    alr.id,
    alr.admin_id,
    alr.admin_name,
    alr.login_time,
    alr.ip_address,
    alr.login_status,
    u.username,
    u.status AS user_status
FROM admin_login_record alr
LEFT JOIN user u ON alr.admin_id = u.user_id
WHERE u.role = 'admin'
ORDER BY alr.login_time DESC
LIMIT 5;

-- 6. 统计每个admin的登录次数
SELECT 
    u.user_id,
    u.username,
    u.nickname,
    u.status,
    COUNT(alr.id) AS login_count
FROM user u
LEFT JOIN admin_login_record alr ON u.user_id = alr.admin_id
WHERE u.role = 'admin'
GROUP BY u.user_id, u.username, u.nickname, u.status;

-- 7. 检查是否有字段为NULL的情况
SELECT 
    user_id,
    username,
    CASE WHEN nickname IS NULL THEN 'NULL' ELSE 'OK' END AS nickname_check,
    CASE WHEN email IS NULL THEN 'NULL' ELSE 'OK' END AS email_check,
    CASE WHEN phone IS NULL THEN 'NULL' ELSE 'OK' END AS phone_check,
    CASE WHEN role IS NULL THEN 'NULL' ELSE 'OK' END AS role_check,
    CASE WHEN status IS NULL THEN 'NULL' ELSE 'OK' END AS status_check
FROM user
WHERE role = 'admin';

-- ========================================
-- 诊断结果说明
-- ========================================
-- 
-- 1. current_database: 应该显示 'muying_mall'
-- 2. admin用户信息: 检查status字段的值（应该是1表示正常）
-- 3. status检查: 确认没有NULL或异常值
-- 4. 登录记录统计: 确认有登录记录
-- 5. 最近登录记录: 检查登录记录是否正常
-- 6. 登录次数统计: 确认loginCount能正确计算
-- 7. NULL字段检查: 确认关键字段不为NULL
-- 
-- ========================================
