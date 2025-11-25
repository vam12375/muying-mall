-- 为order_product表添加SKU相关字段
-- 执行时间：2025-11-24

-- 1. 添加sku_id字段
ALTER TABLE `order_product` 
ADD COLUMN `sku_id` BIGINT NULL COMMENT 'SKU ID' AFTER `product_id`,
ADD INDEX `idx_sku_id`(`sku_id` ASC);

-- 2. 添加sku_code字段（SKU编码，用于显示）
ALTER TABLE `order_product` 
ADD COLUMN `sku_code` VARCHAR(50) NULL COMMENT 'SKU编码' AFTER `sku_id`;

-- 3. 添加sku_name字段（SKU名称，包含规格信息）
ALTER TABLE `order_product` 
ADD COLUMN `sku_name` VARCHAR(200) NULL COMMENT 'SKU名称（含规格）' AFTER `sku_code`;

-- 4. 修改specs字段注释，明确其用途
ALTER TABLE `order_product` 
MODIFY COLUMN `specs` JSON NULL COMMENT 'SKU规格信息（JSON格式）';
