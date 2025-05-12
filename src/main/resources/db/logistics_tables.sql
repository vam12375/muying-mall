-- ----------------------------
-- Table structure for logistics_company
-- ----------------------------
DROP TABLE IF EXISTS `logistics_company`;
CREATE TABLE `logistics_company` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '物流公司ID',
  `code` varchar(10) NOT NULL COMMENT '物流公司代码，用于生成物流单号前缀',
  `name` varchar(50) NOT NULL COMMENT '物流公司名称',
  `contact` varchar(50) NULL COMMENT '联系人',
  `phone` varchar(20) NULL COMMENT '联系电话',
  `address` varchar(255) NULL COMMENT '公司地址',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `logo` varchar(255) NULL COMMENT '物流公司logo',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流公司表';

-- ----------------------------
-- Records of logistics_company
-- ----------------------------
INSERT INTO `logistics_company` VALUES (1, 'SF', '顺丰速运', '客服', '95338', '深圳市宝安区福永大道', 1, 'logistics/sf.png', 1, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (2, 'ZT', '中通快递', '客服', '95311', '上海市青浦区华新镇华志路', 1, 'logistics/zt.png', 2, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (3, 'YT', '圆通速递', '客服', '95554', '上海市青浦区华新镇华徐公路', 1, 'logistics/yt.png', 3, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (4, 'YD', '韵达速递', '客服', '95546', '上海市青浦区盈港东路', 1, 'logistics/yd.png', 4, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (5, 'EMS', 'EMS快递', '客服', '11183', '北京市西城区金融大街', 1, 'logistics/ems.png', 5, NOW(), NOW());

-- 新增物流公司
INSERT INTO `logistics_company` VALUES (6, 'STO', '申通快递', '客服', '95543', '上海市青浦区华徐公路', 1, 'logistics/sto.png', 6, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (7, 'BEST', '百世快递', '客服', '95320', '杭州市萧山区经济技术开发区', 1, 'logistics/best.png', 7, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (8, 'JT', '极兔速递', '客服', '950616', '上海市松江区', 1, 'logistics/jt.png', 8, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (9, 'JD', '京东物流', '客服', '950616', '北京市通州区', 1, 'logistics/jd.png', 9, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (10, 'DB', '德邦快递', '客服', '95353', '上海市青浦区', 1, 'logistics/db.png', 10, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (11, 'ZJS', '宅急送', '客服', '4006789000', '北京市顺义区', 1, 'logistics/zjs.png', 11, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (12, 'HTKY', '百世快运', '客服', '4009565656', '上海市', 1, 'logistics/htky.png', 12, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (13, 'YZPY', '邮政快递包裹', '客服', '11185', '北京市西城区', 1, 'logistics/yzpy.png', 13, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (14, 'ANE', '安能物流', '客服', '4001009288', '上海市', 1, 'logistics/ane.png', 14, NOW(), NOW());
INSERT INTO `logistics_company` VALUES (15, 'FAST', '快捷快递', '客服', '4008000222', '广东省东莞市', 1, 'logistics/fast.png', 15, NOW(), NOW());

-- ----------------------------
-- Table structure for logistics
-- ----------------------------
DROP TABLE IF EXISTS `logistics`;
CREATE TABLE `logistics` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '物流ID',
  `order_id` int UNSIGNED NOT NULL COMMENT '订单ID',
  `company_id` int UNSIGNED NOT NULL COMMENT '物流公司ID',
  `tracking_no` varchar(50) NOT NULL COMMENT '物流单号',
  `status` varchar(20) NOT NULL DEFAULT 'CREATED' COMMENT '物流状态：CREATED-已创建，SHIPPING-运输中，DELIVERED-已送达，EXCEPTION-异常',
  `sender_name` varchar(50) NULL COMMENT '发件人姓名',
  `sender_phone` varchar(20) NULL COMMENT '发件人电话',
  `sender_address` varchar(255) NULL COMMENT '发件地址',
  `receiver_name` varchar(50) NOT NULL COMMENT '收件人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收件人电话',
  `receiver_address` varchar(255) NOT NULL COMMENT '收件地址',
  `shipping_time` datetime NULL COMMENT '发货时间',
  `delivery_time` datetime NULL COMMENT '送达时间',
  `remark` varchar(255) NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_id` (`order_id`),
  UNIQUE KEY `idx_tracking_no` (`tracking_no`),
  INDEX `idx_company_id` (`company_id`),
  INDEX `idx_status` (`status`),
  CONSTRAINT `fk_logistics_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_logistics_company` FOREIGN KEY (`company_id`) REFERENCES `logistics_company` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流表';

-- ----------------------------
-- Table structure for logistics_track
-- ----------------------------
DROP TABLE IF EXISTS `logistics_track`;
CREATE TABLE `logistics_track` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '轨迹ID',
  `logistics_id` bigint UNSIGNED NOT NULL COMMENT '物流ID',
  `tracking_time` datetime NOT NULL COMMENT '轨迹时间',
  `location` varchar(100) NULL COMMENT '当前位置',
  `status` varchar(20) NOT NULL COMMENT '当前状态',
  `content` varchar(255) NOT NULL COMMENT '轨迹内容',
  `operator` varchar(50) NULL COMMENT '操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_logistics_id` (`logistics_id`),
  INDEX `idx_tracking_time` (`tracking_time`),
  CONSTRAINT `fk_track_logistics` FOREIGN KEY (`logistics_id`) REFERENCES `logistics` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流轨迹表';