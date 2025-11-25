-- 为cart表添加SKU相关字段
-- 执行时间：2025-11-24

-- 1. 添加sku_id字段
ALTER TABLE `cart` 
ADD COLUMN `sku_id` BIGINT NULL COMMENT 'SKU ID' AFTER `product_id`,
ADD INDEX `idx_sku_id`(`sku_id` ASC);

-- 2. 修改specs字段类型为JSON（如果还不是）
ALTER TABLE `cart` 
MODIFY COLUMN `specs` JSON NULL COMMENT 'SKU规格信息（JSON格式）';
