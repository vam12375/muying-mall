-- =====================================================
-- 订单商品表添加SKU字段
-- 执行时间: 2024-12-02
-- 说明: 为order_product表添加sku_id和sku_code字段，支持SKU库存管理
-- =====================================================

-- 添加SKU ID字段
ALTER TABLE `order_product` 
ADD COLUMN `sku_id` BIGINT NULL COMMENT 'SKU ID' AFTER `specs`,
ADD COLUMN `sku_code` VARCHAR(64) NULL COMMENT 'SKU编码' AFTER `sku_id`;

-- 添加索引
ALTER TABLE `order_product` ADD INDEX `idx_sku_id` (`sku_id`);
