-- =====================================================
-- SKU体系数据库表结构
-- 作者: 青柠檬
-- 日期: 2024-11-24
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 商品规格表 (规格维度定义)
-- ----------------------------
DROP TABLE IF EXISTS `product_specs`;
CREATE TABLE `product_specs` (
  `spec_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '规格ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `spec_name` varchar(50) NOT NULL COMMENT '规格名称（如：颜色、尺寸、段数）',
  `spec_values` json NOT NULL COMMENT '规格值列表（JSON数组）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`spec_id`),
  INDEX `idx_product_id` (`product_id`),
  CONSTRAINT `fk_specs_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格表';

-- ----------------------------
-- 商品SKU表
-- ----------------------------
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `sku_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) NOT NULL COMMENT 'SKU名称',
  `spec_values` json NOT NULL COMMENT '规格值组合（JSON格式）',
  `price` decimal(10,2) NOT NULL COMMENT 'SKU价格',
  `stock` int NOT NULL DEFAULT 0 COMMENT 'SKU库存',
  `sku_image` varchar(255) DEFAULT NULL COMMENT 'SKU图片',
  `weight` decimal(10,3) DEFAULT NULL COMMENT '重量(kg)',
  `volume` decimal(10,6) DEFAULT NULL COMMENT '体积(m³)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `version` int NOT NULL DEFAULT 1 COMMENT '乐观锁版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`sku_id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_status` (`status`),
  CONSTRAINT `fk_sku_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU表';

-- ----------------------------
-- SKU库存变更日志表
-- ----------------------------
DROP TABLE IF EXISTS `product_sku_stock_log`;
CREATE TABLE `product_sku_stock_log` (
  `log_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `sku_id` bigint UNSIGNED NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(50) NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) DEFAULT NULL COMMENT 'SKU名称',
  `order_id` int UNSIGNED DEFAULT NULL COMMENT '订单ID',
  `change_type` varchar(20) NOT NULL COMMENT '变更类型：DEDUCT-扣减，RESTORE-恢复，ADJUST-调整',
  `change_quantity` int NOT NULL COMMENT '变更数量',
  `before_stock` int NOT NULL COMMENT '变更前库存',
  `after_stock` int NOT NULL COMMENT '变更后库存',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  INDEX `idx_sku_id` (`sku_id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_change_type` (`change_type`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SKU库存变更日志表';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 示例数据：为商品1（惠氏奶粉）添加SKU
-- =====================================================

-- 添加规格维度
INSERT INTO `product_specs` (`product_id`, `spec_name`, `spec_values`, `sort_order`) VALUES
(1, '段数', '["1段(0-6个月)", "2段(6-12个月)", "3段(1-3岁)"]', 1),
(1, '规格', '["400g", "900g"]', 2);

-- 添加SKU
INSERT INTO `product_sku` (`product_id`, `sku_code`, `sku_name`, `spec_values`, `price`, `stock`, `status`) VALUES
(1, 'WY001-1-400', '惠氏启赋1段 400g', '{"段数": "1段(0-6个月)", "规格": "400g"}', 298.00, 50, 1),
(1, 'WY001-1-900', '惠氏启赋1段 900g', '{"段数": "1段(0-6个月)", "规格": "900g"}', 498.00, 30, 1),
(1, 'WY001-2-400', '惠氏启赋2段 400g', '{"段数": "2段(6-12个月)", "规格": "400g"}', 308.00, 40, 1),
(1, 'WY001-2-900', '惠氏启赋2段 900g', '{"段数": "2段(6-12个月)", "规格": "900g"}', 518.00, 25, 1),
(1, 'WY001-3-400', '惠氏启赋3段 400g', '{"段数": "3段(1-3岁)", "规格": "400g"}', 288.00, 60, 1),
(1, 'WY001-3-900', '惠氏启赋3段 900g', '{"段数": "3段(1-3岁)", "规格": "900g"}', 488.00, 35, 1);

-- 更新商品1的has_sku标志和价格范围
UPDATE `product` SET 
  `has_sku` = 1, 
  `min_price` = 288.00, 
  `max_price` = 518.00 
WHERE `product_id` = 1;
