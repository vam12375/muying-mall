-- 添加 phone 字段到 user 表
-- 创建时间: 2025-11-14
-- 目的: 修复用户管理模块 SQL 查询错误

USE muying_mall;

-- 添加 phone 字段
ALTER TABLE `user` 
ADD COLUMN `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号' 
AFTER `email`;

-- 添加索引以提高查询性能
ALTER TABLE `user` 
ADD INDEX `idx_phone`(`phone` ASC) USING BTREE;

-- 验证字段是否添加成功
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH, 
    COLUMN_COMMENT 
FROM 
    INFORMATION_SCHEMA.COLUMNS 
WHERE 
    TABLE_SCHEMA = 'muying_mall' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'phone';
