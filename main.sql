CREATE TABLE `cart` (
  `cart_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `selected` tinyint(1) NULL DEFAULT 1 COMMENT '是否选中：0-否，1-是',
  `specs` json NULL COMMENT '规格信息',
  `specs_hash` varchar(32) NULL COMMENT '规格信息哈希值，用于唯一索引',
  `price_snapshot` decimal(10,2) NULL COMMENT '加入购物车时的价格快照',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0-无效, 1-有效, 2-已下单, 3-库存不足',
  `expire_time` datetime NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`cart_id`) USING BTREE,
  UNIQUE INDEX `idx_user_product_specs`(`user_id`, `product_id`, `specs_hash`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE,
  INDEX `idx_update_time`(`update_time`) USING BTREE,
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 购物车清理存储过程
-- ----------------------------
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `proc_clean_expired_cart_items`()
BEGIN
  -- 删除超过30天未更新的购物车项
  DELETE FROM `cart` WHERE `update_time` < DATE_SUB(NOW(), INTERVAL 30 DAY);
  
  -- 删除已经标记为过期的购物车项
  DELETE FROM `cart` WHERE `expire_time` IS NOT NULL AND `expire_time` < NOW();
  
  -- 将已下单的购物车项标记为已下单状态
  UPDATE `cart` SET `status` = 2 
  WHERE `cart_id` IN (
    SELECT c.cart_id FROM `cart` c
    JOIN `order_product` og ON c.product_id = og.product_id 
    JOIN `order` o ON og.order_id = o.order_id
    WHERE c.user_id = o.user_id AND o.create_time > c.create_time
  );
END //
DELIMITER ;

-- ----------------------------
-- 购物车事件：每天自动清理过期购物车
-- ----------------------------
CREATE EVENT IF NOT EXISTS `evt_daily_clean_cart`
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_DATE + INTERVAL 1 DAY
DO
  CALL proc_clean_expired_cart_items();

-- ----------------------------
-- 触发器：生成规格哈希值
-- ----------------------------
DELIMITER //
CREATE TRIGGER IF NOT EXISTS `trg_cart_before_insert`
BEFORE INSERT ON `cart`
FOR EACH ROW
BEGIN
  -- 生成规格哈希值
  IF NEW.specs IS NOT NULL THEN
    SET NEW.specs_hash = MD5(JSON_UNQUOTE(JSON_EXTRACT(NEW.specs, '$')));
  ELSE
    SET NEW.specs_hash = MD5('');
  END IF;
END //

CREATE TRIGGER IF NOT EXISTS `trg_cart_before_update`
BEFORE UPDATE ON `cart`
FOR EACH ROW
BEGIN
  -- 生成规格哈希值
  IF NEW.specs IS NOT NULL AND (OLD.specs IS NULL OR JSON_EXTRACT(NEW.specs, '$') != JSON_EXTRACT(OLD.specs, '$')) THEN
    SET NEW.specs_hash = MD5(JSON_UNQUOTE(JSON_EXTRACT(NEW.specs, '$')));
  ELSEIF NEW.specs IS NULL AND OLD.specs IS NOT NULL THEN
    SET NEW.specs_hash = MD5('');
  END IF;
END //
DELIMITER ;
----------------------------
CREATE TABLE `category`  (
  `category_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` int UNSIGNED NULL DEFAULT 0 COMMENT '父分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类图标',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

----------------------------
CREATE TABLE `comment`  (
  `comment_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `order_id` int UNSIGNED NOT NULL COMMENT '订单ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评价内容',
  `rating` tinyint NOT NULL DEFAULT 5 COMMENT '评分(1-5)',
  `images` json NULL COMMENT '评价图片',
  `is_anonymous` tinyint(1) NULL DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价表' ROW_FORMAT = Dynamic;
-------------------------------
CREATE TABLE `coupon` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `name` varchar(100) NOT NULL COMMENT '优惠券名称',
  `type` varchar(20) NOT NULL COMMENT '优惠券类型：FIXED-固定金额, PERCENTAGE-百分比折扣',
  `value` decimal(10, 2) NOT NULL COMMENT '优惠券面值/折扣值',
  `min_spend` decimal(10, 2) NOT NULL DEFAULT 0 COMMENT '最低消费金额',
  `max_discount` decimal(10, 2) NULL COMMENT '最大折扣金额（针对百分比折扣）',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-可用, INACTIVE-不可用',
  `category_ids` varchar(255) NULL COMMENT '适用分类ID，多个用逗号分隔，NULL表示全场通用',
  `brand_ids` varchar(255) NULL COMMENT '适用品牌ID，多个用逗号分隔，NULL表示所有品牌',
  `product_ids` varchar(255) NULL COMMENT '适用商品ID，多个用逗号分隔，NULL表示所有商品',
  `is_stackable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可叠加使用：0-不可叠加，1-可叠加',
  `total_quantity` int NOT NULL DEFAULT 0 COMMENT '发行总量，0表示不限量',
  `used_quantity` int NOT NULL DEFAULT 0 COMMENT '已使用数量',
  `received_quantity` int NOT NULL DEFAULT 0 COMMENT '已领取数量',
  `start_time` datetime NOT NULL COMMENT '有效期开始时间',
  `end_time` datetime NOT NULL COMMENT '有效期结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_start_end_time`(`start_time`, `end_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券表' ROW_FORMAT = Dynamic;
-------------------------------
-- 添加优惠券表批次ID和规则ID字段
ALTER TABLE `coupon` 
ADD COLUMN `batch_id` int UNSIGNED NULL COMMENT '批次ID' AFTER `name`,
ADD COLUMN `rule_id` int UNSIGNED NULL COMMENT '规则ID' AFTER `batch_id`,
ADD INDEX `idx_batch_id`(`batch_id`) USING BTREE,
ADD INDEX `idx_rule_id`(`rule_id`) USING BTREE;
ALTER TABLE `coupon` 
ADD COLUMN `user_limit` INT DEFAULT 1 COMMENT '每用户最大领取次数，默认1次，0表示不限制';
-------------------------------
CREATE TABLE `coupon_rule` (
  `rule_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `name` varchar(100) NOT NULL COMMENT '规则名称',
  `type` tinyint NOT NULL COMMENT '规则类型：0-满减，1-直减，2-折扣',
  `rule_content` text NOT NULL COMMENT '规则内容JSON',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券规则表' ROW_FORMAT = Dynamic;
-------------------------------
CREATE TABLE `coupon_batch` (
  `batch_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '批次ID',
  `coupon_name` varchar(100) NOT NULL COMMENT '优惠券名称',
  `rule_id` int UNSIGNED NOT NULL COMMENT '规则ID',
  `total_count` int NOT NULL COMMENT '优惠券总数量',
  `assign_count` int NOT NULL DEFAULT 0 COMMENT '已分配数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`batch_id`) USING BTREE,
  INDEX `idx_rule_id`(`rule_id`) USING BTREE,
  CONSTRAINT `fk_coupon_batch_rule` FOREIGN KEY (`rule_id`) REFERENCES `coupon_rule` (`rule_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券批次表' ROW_FORMAT = Dynamic;
-------------------------------
-- ----------------------------
-- 创建触发器：更新用户优惠券状态（过期）
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_user_coupon_expire_check`;
DELIMITER //
CREATE TRIGGER `trg_user_coupon_expire_check`
BEFORE UPDATE ON `user_coupon`
FOR EACH ROW
BEGIN
  -- 如果过期时间已到且状态为未使用，则修改状态为已过期
  IF NEW.expire_time < NOW() AND NEW.status = 'UNUSED' THEN
    SET NEW.status = 'EXPIRED';
  END IF;
END //
DELIMITER ;

-- ----------------------------
-- 创建定时任务：每天检查过期优惠券
-- ----------------------------
DROP EVENT IF EXISTS `evt_daily_expire_coupon_check`;
DELIMITER //
CREATE EVENT `evt_daily_expire_coupon_check`
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_DATE + INTERVAL 1 DAY
DO
BEGIN
  UPDATE `user_coupon` 
  SET `status` = 'EXPIRED' 
  WHERE `status` = 'UNUSED' AND `expire_time` < NOW();
END //
DELIMITER ;

-- 更新订单表，添加优惠券相关字段
ALTER TABLE `order` 
ADD COLUMN `coupon_id` bigint UNSIGNED NULL COMMENT '使用的优惠券ID' AFTER `discount_amount`,
ADD COLUMN `coupon_discount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠券抵扣金额' AFTER `coupon_id`;
-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `favorite_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`favorite_id`) USING BTREE,
  UNIQUE INDEX `idx_user_product`(`user_id`, `product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;
--------------------------------
   CREATE TABLE `inventory_log` (
     `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
     `product_id` int UNSIGNED NOT NULL,
     `change_amount` int NOT NULL COMMENT '变动数量，正数为增加，负数为减少',
     `type` varchar(20) NOT NULL COMMENT '类型：purchase-进货，sale-销售，return-退货，adjustment-调整',
     `reference_id` varchar(50) COMMENT '关联ID，如订单号',
     `remaining` int NOT NULL COMMENT '变动后剩余库存',
     `operator` varchar(50) COMMENT '操作人',
     `remark` varchar(255) COMMENT '备注',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
     PRIMARY KEY (`id`),
     INDEX `idx_product_id` (`product_id`),
     INDEX `idx_type` (`type`),
     INDEX `idx_create_time` (`create_time`),
     CONSTRAINT `fk_inventory_log_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变动日志表';
--------------------------------
-- 创建会员等级表
CREATE TABLE IF NOT EXISTS `member_level` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '等级ID',
  `level_name` varchar(50) NOT NULL COMMENT '等级名称',
  `min_points` int(11) NOT NULL COMMENT '最低积分要求',
  `discount` decimal(5,2) DEFAULT '1.00' COMMENT '折扣率（0.1-1之间）',
  `icon` varchar(255) DEFAULT NULL COMMENT '等级图标',
  `description` varchar(255) DEFAULT NULL COMMENT '等级描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_level_name` (`level_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

--------------------------------
CREATE TABLE `product`  (
  `product_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `category_id` int UNSIGNED NOT NULL COMMENT '分类ID',
  `brand_id` int UNSIGNED NULL DEFAULT NULL COMMENT '品牌ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `product_sn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品编号',
  `product_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品主图',
  `product_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详情',
  `price_new` decimal(10, 2) NOT NULL COMMENT '现价',
  `price_old` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `sales` int NOT NULL DEFAULT 0 COMMENT '销量',
  `support` int NOT NULL DEFAULT 0 COMMENT '支持人数',
  `rating` decimal(2, 1) NULL DEFAULT 5.0 COMMENT '评分',
  `review_count` int NULL DEFAULT 0 COMMENT '评价数量',
  `product_status` enum('上架','下架') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '上架' COMMENT '商品状态',
  `is_hot` tinyint(1) NULL DEFAULT 0 COMMENT '是否热门：0-否，1-是',
  `is_new` tinyint(1) NULL DEFAULT 0 COMMENT '是否新品：0-否，1-是',
  `is_recommend` tinyint(1) NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`product_id`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_brand_id`(`brand_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;
--------------------------------
-- 优化商品查询
   ALTER TABLE `product` ADD INDEX `idx_category_status_price` (`category_id`, `product_status`, `price_new`);
   -- 价格非负约束
   ALTER TABLE `product` ADD CONSTRAINT `chk_price_positive` CHECK (`price_new` >= 0 AND `price_old` >= 0);
   -- 评分范围约束
   ALTER TABLE `product` ADD CONSTRAINT `chk_rating_range` CHECK (`rating` >= 1.0 AND `rating` <= 5.0);
   -- 库存非负约束
   ALTER TABLE `product` ADD CONSTRAINT `chk_stock_positive` CHECK (`stock` >= 0);
   -- 销量非负约束
   ALTER TABLE `product` ADD CONSTRAINT `chk_sales_positive` CHECK (`sales` >= 0);
   -- 支持人数非负约束
   
-------------------------------
CREATE TABLE `product_image`  (
  `image_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `type` enum('main','detail','desc') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'main' COMMENT '图片类型：main-主图，detail-详情图，desc-描述图',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`image_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品图片表' ROW_FORMAT = Dynamic;
--------------------------------
CREATE TABLE `product_specs` (
     `spec_id` int UNSIGNED NOT NULL AUTO_INCREMENT,
     `product_id` int UNSIGNED NOT NULL,
     `spec_name` varchar(50) NOT NULL COMMENT '规格名称，如颜色、尺寸',
     `spec_values` json NOT NULL COMMENT '规格值列表',
     `sort_order` int DEFAULT 0,
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     PRIMARY KEY (`spec_id`),
     INDEX `idx_product_id` (`product_id`),
     CONSTRAINT `fk_product_specs_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格表';
   ---------------------------------
CREATE TABLE `order` (
  `order_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `actual_amount` decimal(10, 2) NOT NULL COMMENT '实付金额',
  `status` varchar(20) NOT NULL DEFAULT 'pending_payment' COMMENT '订单状态：pending_payment-待付款，pending_shipment-待发货，shipped-已发货，completed-已完成，cancelled-已取消',
  `payment_id` int UNSIGNED DEFAULT NULL COMMENT '支付ID，关联payment表',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '支付方式：alipay-支付宝，wechat-微信，wallet-钱包',
  `shipping_method` varchar(20) DEFAULT NULL COMMENT '配送方式',
  `shipping_fee` decimal(10, 2) DEFAULT 0.00 COMMENT '运费',
  `address_id` int UNSIGNED NOT NULL COMMENT '收货地址ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `receiver_province` varchar(50) NOT NULL COMMENT '省份',
  `receiver_city` varchar(50) NOT NULL COMMENT '城市',
  `receiver_district` varchar(50) NOT NULL COMMENT '区/县',
  `receiver_address` varchar(255) NOT NULL COMMENT '详细地址',
  `receiver_zip` varchar(10) DEFAULT NULL COMMENT '邮编',
  `remark` varchar(255) DEFAULT NULL COMMENT '订单备注',
  `discount_amount` decimal(10, 2) DEFAULT 0.00 COMMENT '优惠金额',
  `points_used` int DEFAULT 0 COMMENT '使用的积分',
  `points_discount` decimal(10, 2) DEFAULT 0.00 COMMENT '积分抵扣金额',
  `paid_time` datetime DEFAULT NULL COMMENT '支付时间',
  `shipping_time` datetime DEFAULT NULL COMMENT '发货时间',
  `completion_time` datetime DEFAULT NULL COMMENT '完成时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `is_deleted` tinyint(1) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_payment_id` (`payment_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;
-- 为订单表添加物流字段
ALTER TABLE `order` 
ADD COLUMN `tracking_no` VARCHAR(64) COMMENT '物流单号' AFTER `shipping_time`,
ADD COLUMN `shipping_company` VARCHAR(64) COMMENT '物流公司' AFTER `tracking_no`; 
-- 优化订单查询
   ALTER TABLE `order` ADD INDEX `idx_user_status_create_time` (`user_id`, `status`, `create_time`);

--添加
ALTER TABLE `order` ADD COLUMN `pay_time` DATETIME COMMENT '支付时间' AFTER `payment_id`;
   -- 添加缺失的外键关联
ALTER TABLE `order_product` 
ADD CONSTRAINT `fk_order_product_order` FOREIGN KEY (`order_id`) 
REFERENCES `order` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE;
--------------------------------
CREATE TABLE `order_product`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` int UNSIGNED NOT NULL COMMENT '订单ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `product_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10, 2) NOT NULL COMMENT '商品价格',
  `quantity` int NOT NULL COMMENT '商品数量',
  `specs` json NULL COMMENT '规格信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单商品表' ROW_FORMAT = Dynamic;
--------------------------------
-- 创建支付表
CREATE TABLE IF NOT EXISTS `payment` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '支付ID',
  `payment_no` varchar(64) NOT NULL COMMENT '支付单号',
  `order_id` int UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `payment_method` varchar(20) NOT NULL COMMENT '支付方式: alipay, wechat, bank',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态: 0-待支付 1-支付中 2-支付成功 3-支付失败 4-已关闭',
  `transaction_id` varchar(64) DEFAULT NULL COMMENT '第三方支付流水号',
  `payment_time` datetime DEFAULT NULL COMMENT '支付成功时间',
  `expire_time` datetime DEFAULT NULL COMMENT '支付超时时间',
  `notify_url` varchar(255) DEFAULT NULL COMMENT '异步通知地址',
  `return_url` varchar(255) DEFAULT NULL COMMENT '同步返回地址',
  `extra` text DEFAULT NULL COMMENT '支付附加信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_payment_no`(`payment_no`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_order_no`(`order_no`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付表' ROW_FORMAT = Dynamic;
ALTER TABLE payment 
ADD COLUMN pay_time DATETIME COMMENT '支付时间' AFTER status;
ALTER TABLE payment 
ADD COLUMN return_url VARCHAR(255) COMMENT '回调地址' AFTER update_time,
ADD COLUMN expire_time DATETIME COMMENT '过期时间' AFTER return_url;
-- -------------------------
-- 为Order表添加payment_id字段，关联到Payment表
ALTER TABLE `order`
ADD COLUMN IF NOT EXISTS `payment_id` bigint UNSIGNED NULL COMMENT '支付ID，关联payment表' AFTER `status`,
ADD INDEX IF NOT EXISTS `idx_payment_id` (`payment_id`) USING BTREE,
ADD CONSTRAINT IF NOT EXISTS `fk_order_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;
-------------------------------
-- 积分兑换记录表
CREATE TABLE IF NOT EXISTS `points_exchange` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '兑换单号',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '兑换数量',
  `points` int(11) NOT NULL COMMENT '消耗积分',
  `address_id` int UNSIGNED DEFAULT NULL COMMENT '收货地址ID',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号码',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态(0:待发货,1:已发货,2:已完成,3:已取消)',
  `tracking_no` varchar(50) DEFAULT NULL COMMENT '物流单号',
  `tracking_company` varchar(50) DEFAULT NULL COMMENT '物流公司',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_exchange_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_exchange_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_exchange_address` FOREIGN KEY (`address_id`) REFERENCES `user_address` (`address_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换记录表';

-- ----------------------------
-- 积分历史记录表
-- ----------------------------
DROP TABLE IF EXISTS `points_history`;
CREATE TABLE `points_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `points` int(11) NOT NULL COMMENT '积分变动数量（正为增加，负为减少）',
  `type` varchar(20) NOT NULL COMMENT '类型：earn(获得), spend(消费)',
  `source` varchar(50) NOT NULL COMMENT '来源：order(订单), signin(签到), review(评论), register(注册), exchange(兑换)等',
  `reference_id` varchar(50) DEFAULT NULL COMMENT '关联ID（如订单ID等）',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分历史记录表';
-- ----------------------------
-- 积分商城商品表
CREATE TABLE IF NOT EXISTS `points_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `description` text COMMENT '商品描述',
  `image` varchar(255) NOT NULL COMMENT '商品图片',
  `points` int(11) NOT NULL COMMENT '所需积分',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '库存数量',
  `category` varchar(50) NOT NULL COMMENT '商品分类(virtual:虚拟商品,physical:实物商品,coupon:优惠券,vip:会员特权)',
  `need_address` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否需要收货地址(0:否,1:是)',
  `need_phone` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否需要手机号(0:否,1:是)',
  `is_hot` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否热门(0:否,1:是)',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态(0:下架,1:上架)',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商城商品表';
--
-- ----------------------------
-- 创建积分奖励表
CREATE TABLE IF NOT EXISTS `points_reward` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '奖励ID',
  `name` varchar(200) NOT NULL COMMENT '奖励名称',
  `description` varchar(1000) DEFAULT NULL COMMENT '奖励描述',
  `points` int(11) NOT NULL COMMENT '兑换所需积分',
  `type` varchar(50) NOT NULL COMMENT '奖励类型(product-实物商品, coupon-优惠券, service-服务)',
  `image` varchar(255) DEFAULT NULL COMMENT '奖励图片',
  `stock` int(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
  `exchanged_count` int(11) NOT NULL DEFAULT 0 COMMENT '已兑换数量',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `visible` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否显示',
  `product_id` bigint(20) DEFAULT NULL COMMENT '关联商品ID',
  `coupon_id` bigint(20) DEFAULT NULL COMMENT '关联优惠券ID',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_type` (`type`),
  INDEX `idx_visible` (`visible`),
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='积分奖励表';
-- ----------------------------
-- 创建积分规则表
CREATE TABLE IF NOT EXISTS `points_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `title` varchar(100) NOT NULL COMMENT '规则标题',
  `description` varchar(500) NOT NULL COMMENT '规则描述',
  `type` varchar(50) NOT NULL COMMENT '规则类型',
  `value` int(11) NOT NULL DEFAULT 0 COMMENT '规则值（积分数量）',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_type` (`type`),
  INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='积分规则表';
-- 插入默认积分规则数据
INSERT INTO `points_rule` (`title`, `description`, `type`, `points_value`, `sort`, `enabled`) VALUES
('购物奖励', '购物可获得订单金额1%的积分', 'shopping', 0, 1, 1),
('评价奖励', '评价商品可获得10-30积分', 'review', 20, 2, 1),
('每日签到', '每日签到可获得5-20积分', 'signin', 20, 3, 1),
('邀请好友', '成功邀请新用户注册可获得100积分', 'invite', 100, 4, 1),
('完善资料', '首次完善个人资料可获得50积分', 'profile', 50, 5, 1),
('积分有效期', '积分有效期为一年，请及时使用', 'expiration', 0, 6, 1),
('会员等级', '积分达到一定数量可提升会员等级，享受更多优惠', 'member', 0, 7, 1);
--------------------------------
CREATE TABLE `user`  (
  `user_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `gender` enum('male','female','unknown') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'unknown' COMMENT '性别',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `role` enum('admin','user') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'user' COMMENT '角色',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username`) USING BTREE,
  UNIQUE INDEX `idx_email`(`email`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;
--------------------------------
CREATE TABLE `user_address`  (
  `address_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `receiver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `province` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区/县',
  `detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `postal_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮政编码',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认地址：0-否，1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`address_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户地址表' ROW_FORMAT = Dynamic;
--------------------------------
CREATE TABLE IF NOT EXISTS `user_message` (
  `message_id` varchar(36) NOT NULL COMMENT '消息ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '消息类型：ORDER-订单消息，SYSTEM-系统消息，REMIND-提醒消息',
  `title` varchar(100) NOT NULL COMMENT '消息标题',
  `content` text NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `extra` json DEFAULT NULL COMMENT '额外信息，如关联的订单ID等',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_is_read`(`is_read`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户消息表' ROW_FORMAT = Dynamic;
-- ----------------------------
-- 用户积分表
-- ----------------------------
DROP TABLE IF EXISTS `user_points`;
CREATE TABLE `user_points` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '积分ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `points` int(11) DEFAULT '0' COMMENT '积分总数',
  `level` varchar(50) DEFAULT '普通会员' COMMENT '会员等级',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';
--------------------------------
CREATE TABLE `user_coupon` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `coupon_id` bigint UNSIGNED NOT NULL COMMENT '优惠券ID',
  `batch_id` int UNSIGNED NULL COMMENT '批次ID',
  `status` varchar(20) NOT NULL DEFAULT 'UNUSED' COMMENT '状态：UNUSED-未使用, USED-已使用, EXPIRED-已过期, FROZEN-已冻结',
  `use_time` datetime NULL COMMENT '使用时间',
  `order_id` bigint UNSIGNED NULL COMMENT '关联订单ID',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户优惠券表' ROW_FORMAT = Dynamic;
