-- ========================================
-- 修复操作日志中的 operation_result 字段
-- Fix operation_result field in admin_operation_logs
-- ========================================

-- 问题：旧数据中 operation_result 字段为 NULL
-- 解决：根据 error_message 字段判断操作结果

USE muying_mall;

-- 1. 查看当前 NULL 值的数量
SELECT 
    COUNT(*) AS total_records,
    SUM(CASE WHEN operation_result IS NULL THEN 1 ELSE 0 END) AS null_count,
    SUM(CASE WHEN operation_result = 'success' THEN 1 ELSE 0 END) AS success_count,
    SUM(CASE WHEN operation_result = 'failed' THEN 1 ELSE 0 END) AS failed_count
FROM admin_operation_logs;

-- 2. 更新 NULL 值
-- 规则：如果 error_message 为 NULL 或空，则认为操作成功，否则失败
UPDATE admin_operation_logs
SET operation_result = CASE
    WHEN error_message IS NULL OR error_message = '' THEN 'success'
    ELSE 'failed'
END
WHERE operation_result IS NULL;

-- 3. 验证更新结果
SELECT 
    COUNT(*) AS total_records,
    SUM(CASE WHEN operation_result IS NULL THEN 1 ELSE 0 END) AS null_count,
    SUM(CASE WHEN operation_result = 'success' THEN 1 ELSE 0 END) AS success_count,
    SUM(CASE WHEN operation_result = 'failed' THEN 1 ELSE 0 END) AS failed_count
FROM admin_operation_logs;

-- 4. 查看最近10条记录
SELECT 
    id,
    admin_name,
    operation,
    module,
    operation_result,
    error_message,
    create_time
FROM admin_operation_logs
ORDER BY id DESC
LIMIT 10;

-- ========================================
-- 执行说明
-- ========================================
-- 
-- 1. 在 MySQL 客户端或 Navicat 中执行此脚本
-- 2. 检查更新前后的统计数据
-- 3. 确认所有 NULL 值已被正确更新
-- 
-- ========================================
