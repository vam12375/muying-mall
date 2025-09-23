/*
 Navicat Premium Dump SQL

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : muying_mall

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 20/07/2025 10:28:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account_transaction
-- ----------------------------
DROP TABLE IF EXISTS `account_transaction`;
CREATE TABLE `account_transaction`  (
  `transaction_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `transaction_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易单号',
  `account_id` int UNSIGNED NOT NULL COMMENT '账户ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '交易金额',
  `balance` decimal(10, 2) NOT NULL COMMENT '交易后余额',
  `type` tinyint NOT NULL COMMENT '交易类型：1-充值，2-消费，3-退款，4-提现，5-转账，6-收入，7-其他',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式：alipay-支付宝，wechat-微信支付，bank-银行卡，balance-余额，other-其他',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '交易状态：0-失败，1-成功，2-处理中，3-已取消',
  `related_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联ID（如订单ID、退款ID等）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易描述',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`transaction_id`) USING BTREE,
  UNIQUE INDEX `idx_transaction_no`(`transaction_no` ASC) USING BTREE,
  INDEX `idx_account_id`(`account_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_transaction_account` FOREIGN KEY (`account_id`) REFERENCES `user_account` (`account_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账户交易记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of account_transaction
-- ----------------------------
INSERT INTO `account_transaction` VALUES (1, 'TR17492800103864e98c41f', 5, 8, 1000.00, 1000.00, 1, 'admin', 1, NULL, '管理员充值', NULL, '2025-06-07 15:06:50', '2025-06-07 15:06:50');
INSERT INTO `account_transaction` VALUES (2, 'TR174928018839784f55af1', 5, 8, 5000.00, 6000.00, 1, 'admin', 1, NULL, '管理员充值', NULL, '2025-06-07 15:09:48', '2025-06-07 15:09:48');
INSERT INTO `account_transaction` VALUES (7, 'RCH17495685738525347', 5, 8, 1000.00, 7000.00, 1, 'alipay', 1, NULL, '账户充值', '第三方交易号: manual-1749617089458', '2025-06-10 23:16:14', '2025-06-11 12:44:49');
INSERT INTO `account_transaction` VALUES (8, 'RCH17496184407030519', 5, 8, 200.00, 7200.00, 1, 'wechat', 1, NULL, '账户充值', '第三方交易号: manual-1749622774120', '2025-06-11 13:07:21', '2025-06-11 14:19:35');
INSERT INTO `account_transaction` VALUES (9, 'RCH17496225867880316', 5, 8, 500.00, 7700.00, 1, 'wechat', 1, NULL, '账户充值', '第三方交易号: manual-1749622791523', '2025-06-11 14:16:27', '2025-06-11 14:19:52');
INSERT INTO `account_transaction` VALUES (10, 'RCH17496232048474703', 5, 8, 500.00, 8200.00, 1, 'wechat', 1, NULL, '账户充值', '第三方交易号: manual-1749623375077', '2025-06-11 14:26:45', '2025-06-11 14:29:36');
INSERT INTO `account_transaction` VALUES (11, 'RCH17496236288125638', 5, 8, 1000.00, 9200.00, 1, 'wechat', 1, NULL, '账户充值', '第三方交易号: manual-1749623633347', '2025-06-11 14:33:49', '2025-06-11 14:33:53');
INSERT INTO `account_transaction` VALUES (12, 'RCH17496239725471848', 5, 8, 200.00, 9400.00, 1, 'wechat', 1, NULL, '账户充值', '第三方交易号: manual-1749623975878', '2025-06-11 14:39:33', '2025-06-11 14:39:36');
INSERT INTO `account_transaction` VALUES (13, 'RCH17496333721151657', 5, 8, 600.00, 10000.00, 1, 'wechat', 1, NULL, '账户充值', '第三方交易号: manual-1749633377469', '2025-06-11 17:16:12', '2025-06-11 17:16:17');
INSERT INTO `account_transaction` VALUES (14, 'TR1749635683181184117c2', 5, 8, -179.00, 9821.00, 2, 'wechat', 1, NULL, '账户消费扣款', '系统扣款', '2025-06-11 17:54:43', '2025-06-13 20:27:23');
INSERT INTO `account_transaction` VALUES (15, 'TR17509980491080eb0e5ce', 5, 8, -398.00, 9423.00, 4, 'admin', 1, NULL, '管理员调整余额', '系统扣款', '2025-06-27 12:20:49', '2025-06-27 12:20:49');
INSERT INTO `account_transaction` VALUES (17, 'RCH17510104059414323', 5, 8, 100.00, 9523.00, 1, 'wechat', 1, NULL, '账户充值', '第三方交易号: manual-1751010411384', '2025-06-27 15:46:46', '2025-06-27 15:46:51');
INSERT INTO `account_transaction` VALUES (18, 'RCH17510104634259894', 5, 8, 500.00, 10023.00, 1, 'wechat', 1, NULL, '账户充值', '第三方交易号: manual-1751010468020', '2025-06-27 15:47:43', '2025-06-27 15:47:48');
INSERT INTO `account_transaction` VALUES (19, 'RCH17510105011787925', 5, 8, 200.00, 0.00, 1, 'alipay', 0, NULL, '账户充值', NULL, '2025-06-27 15:48:21', '2025-06-27 15:48:21');
INSERT INTO `account_transaction` VALUES (20, 'RCH17520341439367408', 5, 8, 1000.00, 0.00, 1, 'alipay', 0, NULL, '账户充值', NULL, '2025-07-09 12:09:04', '2025-07-09 12:09:04');
INSERT INTO `account_transaction` VALUES (21, 'RCH17520391216564454', 5, 8, 100.00, 0.00, 1, 'alipay', 0, NULL, '账户充值', NULL, '2025-07-09 13:32:02', '2025-07-09 13:32:02');
INSERT INTO `account_transaction` VALUES (22, 'RCH17520431951302793', 5, 8, 1000.00, 11023.00, 1, 'wechat', 1, NULL, '账户充值', '第三方交易号: manual-1752043201139', '2025-07-09 14:39:55', '2025-07-09 14:40:01');
INSERT INTO `account_transaction` VALUES (23, 'RCH17520432121475957', 5, 8, 1000.00, 12023.00, 1, 'alipay', 1, NULL, '账户充值', '第三方交易号: manual-1752043894757', '2025-07-09 14:40:12', '2025-07-09 14:51:35');
INSERT INTO `account_transaction` VALUES (24, 'RCH17520439134356984', 5, 8, 5000.00, 17023.00, 1, 'alipay', 1, NULL, '账户充值', '第三方交易号: manual_1752044604594', '2025-07-09 14:51:53', '2025-07-09 15:03:25');
INSERT INTO `account_transaction` VALUES (25, 'TR175204519200546968d66', 5, 8, 899.00, 16124.00, 2, 'wallet', 1, '255', '账户消费扣款', NULL, '2025-07-09 15:13:12', '2025-07-09 15:13:12');
INSERT INTO `account_transaction` VALUES (26, 'TR1752045322384f7edded4', 5, 8, 899.00, 17023.00, 3, 'wallet', 1, NULL, '订单退款：OD175204518724432e360', NULL, '2025-07-09 15:15:22', '2025-07-09 15:15:22');
INSERT INTO `account_transaction` VALUES (27, 'RCH17520464540790786', 6, 9, 5000.00, 5000.00, 1, 'wechat', 1, NULL, '账户充值', '第三方交易号: manual_1752046459449', '2025-07-09 15:34:14', '2025-07-09 15:34:19');
INSERT INTO `account_transaction` VALUES (28, 'TR17520643385641113c9a6', 6, 9, 2580.00, 2420.00, 2, 'wallet', 1, '256', '账户消费扣款', NULL, '2025-07-09 20:32:19', '2025-07-09 20:32:19');
INSERT INTO `account_transaction` VALUES (29, 'TR1752065966624644601fb', 6, 9, 179.00, 2241.00, 2, 'wallet', 1, '257', '账户消费扣款', NULL, '2025-07-09 20:59:27', '2025-07-09 20:59:27');
INSERT INTO `account_transaction` VALUES (30, 'RCH17521143386100409', 6, 9, 1000.00, 3241.00, 1, 'alipay', 1, NULL, '账户充值', '手动完成充值: 1752115912741', '2025-07-10 10:25:39', '2025-07-10 10:51:53');
INSERT INTO `account_transaction` VALUES (31, 'RCH17522112411025753', 5, 8, 1000.00, 18023.00, 1, 'wechat', 1, NULL, '账户充值', '手动完成充值: 1752211415460', '2025-07-11 13:20:41', '2025-07-11 13:23:35');
INSERT INTO `account_transaction` VALUES (32, 'RCH17522114485211903', 5, 8, 1000.00, 19023.00, 1, 'wechat', 1, NULL, '账户充值', '手动完成充值: 1752211453838', '2025-07-11 13:24:09', '2025-07-11 13:24:14');
INSERT INTO `account_transaction` VALUES (33, 'RCH17522184329235882', 6, 9, 1000.00, 4241.00, 1, 'wechat', 1, NULL, '账户充值', '手动完成充值: 1752218438252', '2025-07-11 15:20:33', '2025-07-11 15:20:38');
INSERT INTO `account_transaction` VALUES (34, 'TR1752663370682d38514e9', 5, 8, 179.00, 18844.00, 2, 'wallet', 1, '265', '账户消费扣款', NULL, '2025-07-16 18:56:11', '2025-07-16 18:56:11');
INSERT INTO `account_transaction` VALUES (35, 'RCH17581850045207100', 5, 8, 100.00, 18944.00, 1, 'wechat', 1, NULL, '账户充值', '手动完成充值: 1758185008730', '2025-09-18 16:43:25', '2025-09-18 16:43:29');

-- ----------------------------
-- Table structure for admin_login_records
-- ----------------------------
DROP TABLE IF EXISTS `admin_login_records`;
CREATE TABLE `admin_login_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` int NOT NULL COMMENT '管理员ID',
  `admin_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理员用户名',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `logout_time` datetime NULL DEFAULT NULL COMMENT '登出时间',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'IP地址',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录地点',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户代理信息',
  `device_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备类型(Desktop/Mobile/Tablet)',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '浏览器信息',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作系统',
  `login_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'success' COMMENT '登录状态(success/failed)',
  `failure_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '失败原因',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话ID',
  `duration_seconds` int NULL DEFAULT NULL COMMENT '会话时长(秒)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_login_time`(`login_time` ASC) USING BTREE,
  INDEX `idx_ip_address`(`ip_address` ASC) USING BTREE,
  INDEX `idx_login_status`(`login_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员登录记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_login_records
-- ----------------------------

-- ----------------------------
-- Table structure for admin_online_status
-- ----------------------------
DROP TABLE IF EXISTS `admin_online_status`;
CREATE TABLE `admin_online_status`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` int NOT NULL COMMENT '管理员ID',
  `admin_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理员用户名',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `last_activity_time` datetime NOT NULL COMMENT '最后活动时间',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'IP地址',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户代理信息',
  `is_online` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否在线(1:在线,0:离线)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_is_online`(`is_online` ASC) USING BTREE,
  INDEX `idx_last_activity_time`(`last_activity_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员在线状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_online_status
-- ----------------------------

-- ----------------------------
-- Table structure for admin_operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `admin_operation_logs`;
CREATE TABLE `admin_operation_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` int NOT NULL COMMENT '管理员ID',
  `admin_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理员用户名',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作名称',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作模块',
  `operation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型(CREATE/READ/UPDATE/DELETE)',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作目标类型',
  `target_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作目标ID',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `response_status` int NULL DEFAULT NULL COMMENT '响应状态码',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'IP地址',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户代理信息',
  `operation_result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'success' COMMENT '操作结果(success/failed)',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `execution_time_ms` bigint NULL DEFAULT NULL COMMENT '执行时间(毫秒)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '操作描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_operation_type`(`operation_type` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_operation_result`(`operation_result` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_operation_logs
-- ----------------------------
INSERT INTO `admin_operation_logs` VALUES (1, 1, 'admin', '查看统计信息', '管理员管理', 'READ', '', NULL, 'GET', '/api/admin/statistics', '{\"_t\":\"1750949164532\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36', 'success', NULL, 106, '执行了查看统计信息，模块：管理员管理', '2025-06-26 22:46:05');
INSERT INTO `admin_operation_logs` VALUES (2, 1, 'admin', '查看统计信息', '管理员管理', 'READ', '', NULL, 'GET', '/api/admin/statistics', '{\"_t\":\"1750949164532\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36', 'success', NULL, 106, '执行了查看统计信息，模块：管理员管理', '2025-06-26 22:46:05');
INSERT INTO `admin_operation_logs` VALUES (3, 1, 'admin', '查看统计信息', '管理员管理', 'READ', '', NULL, 'GET', '/api/admin/statistics', '{\"_t\":\"1750996443184\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 6, '执行了查看统计信息，模块：管理员管理', '2025-06-27 11:54:03');
INSERT INTO `admin_operation_logs` VALUES (4, 1, 'admin', '查看统计信息', '管理员管理', 'READ', '', NULL, 'GET', '/api/admin/statistics', '{\"_t\":\"1750996443185\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 5, '执行了查看统计信息，模块：管理员管理', '2025-06-27 11:54:03');
INSERT INTO `admin_operation_logs` VALUES (5, 1, 'admin', '查看统计信息', '管理员管理', 'READ', '', NULL, 'GET', '/api/admin/statistics', '{\"_t\":\"1750996735426\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 108, '执行了查看统计信息，模块：管理员管理', '2025-06-27 11:58:56');
INSERT INTO `admin_operation_logs` VALUES (6, 1, 'admin', '查看统计信息', '管理员管理', 'READ', '', NULL, 'GET', '/api/admin/statistics', '{\"_t\":\"1750996735426\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 108, '执行了查看统计信息，模块：管理员管理', '2025-06-27 11:58:56');
INSERT INTO `admin_operation_logs` VALUES (7, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997111481\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 11, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:05:12');
INSERT INTO `admin_operation_logs` VALUES (8, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997111481\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 11, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:05:12');
INSERT INTO `admin_operation_logs` VALUES (9, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997303003\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 90, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:08:23');
INSERT INTO `admin_operation_logs` VALUES (10, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997303002\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 90, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:08:23');
INSERT INTO `admin_operation_logs` VALUES (11, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997618885\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 18, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:13:39');
INSERT INTO `admin_operation_logs` VALUES (12, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997618885\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 17, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:13:39');
INSERT INTO `admin_operation_logs` VALUES (13, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997631909\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 3, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:13:52');
INSERT INTO `admin_operation_logs` VALUES (14, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997631909\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 5, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:13:52');
INSERT INTO `admin_operation_logs` VALUES (15, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997631938\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 4, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:13:52');
INSERT INTO `admin_operation_logs` VALUES (16, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997631938\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 4, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:13:52');
INSERT INTO `admin_operation_logs` VALUES (17, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997713185\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 4, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:15:13');
INSERT INTO `admin_operation_logs` VALUES (18, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997713185\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 5, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:15:13');
INSERT INTO `admin_operation_logs` VALUES (19, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997751715\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 3, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:15:52');
INSERT INTO `admin_operation_logs` VALUES (20, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997751715\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 3, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:15:52');
INSERT INTO `admin_operation_logs` VALUES (21, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997755230\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 3, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:15:55');
INSERT INTO `admin_operation_logs` VALUES (22, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997755230\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 3, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:15:55');
INSERT INTO `admin_operation_logs` VALUES (23, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997822457\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 3, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:17:02');
INSERT INTO `admin_operation_logs` VALUES (24, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1750997822457\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 3, '执行了查看系统日志，模块：系统管理', '2025-06-27 12:17:02');
INSERT INTO `admin_operation_logs` VALUES (25, 1, 'admin', '查看统计信息', '管理员管理', 'READ', '', NULL, 'GET', '/api/admin/statistics', '{\"_t\":\"1750997835357\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 84, '执行了查看统计信息，模块：管理员管理', '2025-06-27 12:17:15');
INSERT INTO `admin_operation_logs` VALUES (26, 1, 'admin', '查看统计信息', '管理员管理', 'READ', '', NULL, 'GET', '/api/admin/statistics', '{\"_t\":\"1750997835356\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 84, '执行了查看统计信息，模块：管理员管理', '2025-06-27 12:17:15');
INSERT INTO `admin_operation_logs` VALUES (27, 1, 'admin', '查看操作记录', '管理员管理', 'READ', '', '1', 'GET', '/api/admin/operation-records', '{\"size\":\"10\",\"_t\":\"1750997838133\",\"module\":\"\",\"page\":\"1\",\"operation\":\"\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 4, '执行了查看操作记录，模块：管理员管理', '2025-06-27 12:17:18');
INSERT INTO `admin_operation_logs` VALUES (28, 1, 'admin', '查看操作记录', '管理员管理', 'READ', '', '1', 'GET', '/api/admin/operation-records', '{\"size\":\"10\",\"_t\":\"1750997838133\",\"module\":\"\",\"page\":\"1\",\"operation\":\"\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 4, '执行了查看操作记录，模块：管理员管理', '2025-06-27 12:17:18');
INSERT INTO `admin_operation_logs` VALUES (29, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1752033714160\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 7, '执行了查看系统日志，模块：系统管理', '2025-07-09 12:01:54');
INSERT INTO `admin_operation_logs` VALUES (30, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1752033714160\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 7, '执行了查看系统日志，模块：系统管理', '2025-07-09 12:01:54');
INSERT INTO `admin_operation_logs` VALUES (31, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '3', 'GET', '/api/admin/system/logs', '{\"_t\":\"1752033723123\",\"pageSize\":\"10\",\"page\":\"3\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 6, '执行了查看系统日志，模块：系统管理', '2025-07-09 12:02:03');
INSERT INTO `admin_operation_logs` VALUES (32, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '2', 'GET', '/api/admin/system/logs', '{\"_t\":\"1752033725063\",\"pageSize\":\"10\",\"page\":\"2\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 5, '执行了查看系统日志，模块：系统管理', '2025-07-09 12:02:05');
INSERT INTO `admin_operation_logs` VALUES (33, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1752033726191\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 4, '执行了查看系统日志，模块：系统管理', '2025-07-09 12:02:06');
INSERT INTO `admin_operation_logs` VALUES (34, 1, 'admin', '查看系统日志详情', '系统管理', 'READ', '', '32', 'GET', '/api/admin/system/logs/32', '{\"_t\":\"1752033728699\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 2, '执行了查看系统日志详情，模块：系统管理', '2025-07-09 12:02:09');
INSERT INTO `admin_operation_logs` VALUES (35, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1754529442678\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 95, '执行了查看系统日志，模块：系统管理', '2025-08-07 09:17:23');
INSERT INTO `admin_operation_logs` VALUES (36, 1, 'admin', '查看系统日志', '系统管理', 'READ', '', '1', 'GET', '/api/admin/system/logs', '{\"_t\":\"1754529442678\",\"pageSize\":\"10\",\"page\":\"1\"}', 200, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36', 'success', NULL, 95, '执行了查看系统日志，模块：系统管理', '2025-08-07 09:17:23');

-- ----------------------------
-- Table structure for brand
-- ----------------------------
DROP TABLE IF EXISTS `brand`;
CREATE TABLE `brand`  (
  `brand_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '品牌名称',
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌logo',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '品牌描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`brand_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '品牌表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of brand
-- ----------------------------
INSERT INTO `brand` VALUES (1, '惠氏', 'brands/wyeth.png', '惠氏营养品是全球知名的婴幼儿营养品牌', 1, 1, '2025-03-05 21:58:47', '2025-05-20 10:16:40');
INSERT INTO `brand` VALUES (2, '美素佳儿', 'brands/friso.png', '美素佳儿是荷兰皇家菲仕兰旗下的婴幼儿奶粉品牌', 2, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `brand` VALUES (3, '帮宝适', 'brands/pampers.png', '帮宝适是宝洁公司旗下的婴儿纸尿裤品牌', 3, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `brand` VALUES (4, '花王', 'brands/huawang.png', '花王是日本著名的个人护理用品品牌', 4, 1, '2025-03-05 21:58:47', '2025-03-17 16:10:15');
INSERT INTO `brand` VALUES (5, '爱他美', 'brands/aptamil.png', '爱他美是来自德国的高端婴幼儿配方奶粉品牌，专注婴幼儿营养研究超过40年', 5, 1, '2025-05-18 10:30:00', '2025-05-15 19:55:42');
INSERT INTO `brand` VALUES (6, '费雪', 'brands/fisher-price.png', '费雪是全球著名玩具品牌，专注于儿童早期发展和教育玩具设计', 6, 1, '2025-05-18 10:32:15', '2025-05-15 19:55:44');
INSERT INTO `brand` VALUES (7, '飞利浦新安怡', 'brands/avent.png', '飞利浦新安怡是全球领先的婴幼儿喂养用品品牌，专注于研发安全舒适的喂养产品', 7, 1, '2025-05-18 10:34:30', '2025-05-15 19:55:46');
INSERT INTO `brand` VALUES (8, '美德乐', 'brands/medela.png', '美德乐是源自瑞士的全球知名母乳喂养解决方案品牌，专注于吸奶器和母乳喂养辅助产品', 8, 1, '2025-05-18 10:36:45', '2025-03-18 10:36:45');
INSERT INTO `brand` VALUES (9, '贝亲', 'brands/pigeon.png', '贝亲是日本知名的母婴用品品牌，拥有超过60年的婴幼儿护理经验', 9, 1, '2025-05-18 10:39:00', '2025-03-18 10:39:00');
INSERT INTO `brand` VALUES (10, '嘉宝', 'brands/gerber.png', '嘉宝是全球领先的婴幼儿辅食品牌，提供营养均衡的婴幼儿食品和零食', 10, 1, '2025-05-18 10:41:15', '2025-03-18 10:41:15');

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `cart_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `selected` tinyint(1) NULL DEFAULT 1 COMMENT '是否选中：0-否，1-是',
  `specs` json NULL COMMENT '规格信息',
  `specs_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格信息哈希值，用于唯一索引',
  `price_snapshot` decimal(10, 2) NULL DEFAULT NULL COMMENT '加入购物车时的价格快照',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-无效, 1-有效, 2-已下单, 3-库存不足',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`cart_id`) USING BTREE,
  UNIQUE INDEX `idx_user_goods_specs`(`user_id` ASC, `product_id` ASC, `specs_hash` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE,
  CONSTRAINT `fk_cart_goods` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 155 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cart
-- ----------------------------
INSERT INTO `cart` VALUES (152, 8, 104, 1, 0, NULL, 'd41d8cd98f00b204e9800998ecf8427e', 180.00, 1, NULL, '2025-09-19 12:11:39', '2025-09-19 12:11:48');
INSERT INTO `cart` VALUES (153, 8, 100, 1, 0, NULL, 'd41d8cd98f00b204e9800998ecf8427e', 179.00, 1, NULL, '2025-09-19 12:11:41', '2025-09-19 12:11:48');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
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
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, 0, '奶粉', 'categorys/milk.png', 1, 1, '2025-03-05 21:58:47', '2025-05-09 10:53:45');
INSERT INTO `category` VALUES (2, 0, '护理', 'categorys/diaper.png', 2, 1, '2025-03-05 21:58:47', '2025-05-09 10:53:49');
INSERT INTO `category` VALUES (3, 0, '服饰', 'categorys/clothing.png', 3, 1, '2025-03-05 21:58:47', '2025-05-09 10:53:53');
INSERT INTO `category` VALUES (4, 0, '玩具', 'categorys/toy.png', 4, 1, '2025-03-05 21:58:47', '2025-05-09 10:53:56');
INSERT INTO `category` VALUES (5, 0, '洗护', 'categorys/care.png', 5, 1, '2025-03-05 21:58:47', '2025-05-09 10:54:02');
INSERT INTO `category` VALUES (6, 0, '喂养', 'categorys/feeding.png', 6, 1, '2025-03-05 21:58:47', '2025-05-09 10:54:05');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
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
  `has_replied` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已回复：0-否，1-是',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_rating_time`(`product_id` ASC, `rating` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 215 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 2, 1, 1, '奶粉很好，宝宝很喜欢喝，消化也好，会继续购买', 5, '[\"comment/img_01_01.jpg\"]', 0, 1, '2025-05-07 10:30:00', '2025-05-07 10:30:00', 0);
INSERT INTO `comment` VALUES (2, 2, 4, 1, '尿不湿很好用，透气不闷热，宝宝用了不红屁屁', 4, NULL, 0, 1, '2025-05-07 10:35:00', '2025-05-07 10:35:00', 0);
INSERT INTO `comment` VALUES (3, 3, 1, 3, '奶粉品质不错，包装完好，物流很快', 5, '[\"comment/img_03_01.jpg\", \"comment/img_03_02.jpg\"]', 0, 1, '2025-05-07 09:30:00', '2025-05-07 09:30:00', 0);
INSERT INTO `comment` VALUES (4, 4, 2, 4, '婴儿床质量很好，做工精细，宝宝睡得很舒服', 5, '[\"comment/img_04_01.jpg\"]', 0, 1, '2025-05-06 14:20:00', '2025-05-06 14:20:00', 0);
INSERT INTO `comment` VALUES (5, 5, 3, 5, '婴儿车推起来很轻松，折叠也方便，很适合带宝宝出门', 4, NULL, 0, 1, '2025-05-06 16:45:00', '2025-05-06 16:45:00', 0);
INSERT INTO `comment` VALUES (6, 6, 5, 6, '湿巾柔软不刺激，非常适合宝宝使用', 5, NULL, 0, 1, '2025-05-05 09:15:00', '2025-05-05 09:15:00', 0);
INSERT INTO `comment` VALUES (7, 7, 6, 7, '奶瓶质量一般，有点小漏水', 2, '[\"comment/img_07_01.jpg\"]', 0, 1, '2025-05-05 11:30:00', '2025-05-05 11:30:00', 0);
INSERT INTO `comment` VALUES (8, 8, 7, 8, '辅食机很好用，打出来的泥很细腻，宝宝很喜欢', 5, '[\"comment/img_08_01.jpg\", \"comment/img_08_02.jpg\"]', 0, 1, '2025-05-04 15:20:00', '2025-05-04 15:20:00', 0);
INSERT INTO `comment` VALUES (9, 9, 8, 9, '婴儿玩具材质安全，色彩鲜艳，宝宝很喜欢', 4, '[\"comment/img_09_01.jpg\"]', 0, 1, '2025-05-04 16:10:00', '2025-05-04 16:10:00', 0);
INSERT INTO `comment` VALUES (10, 10, 9, 10, '儿童餐椅设计合理，可调节高度，使用方便', 5, '[\"comment/img_10_01.jpg\"]', 0, 1, '2025-05-03 10:40:00', '2025-05-03 10:40:00', 0);
INSERT INTO `comment` VALUES (11, 11, 10, 11, '婴儿背带舒适透气，宝宝穿着很舒服，家长也不累', 5, '[\"comment/img_11_01.jpg\", \"comment/img_11_02.jpg\"]', 0, 1, '2025-05-03 13:50:00', '2025-05-03 13:50:00', 0);
INSERT INTO `comment` VALUES (12, 12, 11, 12, '安抚奶嘴质量不错，宝宝很喜欢', 4, NULL, 0, 1, '2025-05-02 09:25:00', '2025-05-02 09:25:00', 0);
INSERT INTO `comment` VALUES (13, 13, 12, 13, '婴儿洗发水很温和，不刺激眼睛，洗后头发柔顺', 5, '[\"comment/img_13_01.jpg\"]', 0, 1, '2025-05-02 14:30:00', '2025-05-02 14:30:00', 0);
INSERT INTO `comment` VALUES (14, 14, 13, 14, '儿童牙刷刷毛太硬了，不太适合小宝宝', 2, NULL, 0, 1, '2025-05-01 10:15:00', '2025-05-01 10:15:00', 0);
INSERT INTO `comment` VALUES (15, 15, 14, 15, '婴儿浴盆大小合适，放水放洗浴用品都很方便', 4, '[\"comment/img_15_01.jpg\"]', 0, 1, '2025-05-01 16:40:00', '2025-05-01 16:40:00', 0);
INSERT INTO `comment` VALUES (16, 16, 15, 16, '儿童餐具材质安全，造型可爱，宝宝很愿意用', 5, '[\"comment/img_16_01.jpg\", \"comment/img_16_02.jpg\"]', 0, 1, '2025-04-30 12:20:00', '2025-04-30 12:20:00', 0);
INSERT INTO `comment` VALUES (17, 17, 16, 17, '儿童读物内容简单有趣，插图精美，适合启蒙', 5, NULL, 0, 1, '2025-04-30 15:10:00', '2025-04-30 15:10:00', 0);
INSERT INTO `comment` VALUES (18, 18, 17, 18, '婴儿护肤霜滋润不油腻，宝宝用了皮肤很好', 4, '[\"comment/img_18_01.jpg\"]', 0, 1, '2025-04-29 09:50:00', '2025-04-29 09:50:00', 0);
INSERT INTO `comment` VALUES (19, 19, 18, 19, '婴儿口水巾吸水性一般，容易湿透', 3, NULL, 0, 1, '2025-04-29 14:15:00', '2025-04-29 14:15:00', 0);
INSERT INTO `comment` VALUES (20, 20, 19, 20, '儿童防蚊液效果很好，没有刺激性气味，宝宝用着舒服', 5, '[\"comment/img_20_01.jpg\"]', 0, 1, '2025-04-28 11:30:00', '2025-04-28 11:30:00', 0);
INSERT INTO `comment` VALUES (21, 3, 5, 21, '这个产品质量真的很好，宝宝很喜欢，值得推荐给其他妈妈', 5, '[\"comment/img_21_01.jpg\"]', 0, 0, '2025-05-07 16:20:00', '2025-05-07 16:20:00', 0);
INSERT INTO `comment` VALUES (22, 5, 8, 22, '收到商品后发现有点小问题，但是客服很快就解决了，服务态度不错', 4, NULL, 0, 0, '2025-05-07 15:40:00', '2025-05-07 15:40:00', 0);
INSERT INTO `comment` VALUES (23, 7, 10, 23, '宝宝用了这个产品后皮肤有点过敏，不太适合敏感肌肤的宝宝', 2, '[\"comment/img_23_01.jpg\"]', 0, 0, '2025-05-07 14:30:00', '2025-05-07 14:30:00', 0);
INSERT INTO `comment` VALUES (24, 8, 3, 24, '这个婴儿车真的很适合出门使用，轻便又实用', 5, NULL, 0, 1, '2025-04-15 09:20:00', '2025-04-15 09:20:00', 0);
INSERT INTO `comment` VALUES (25, 9, 4, 25, '宝宝尿不湿质量不错，吸水性强，宝宝用着舒服', 4, NULL, 0, 1, '2025-04-10 14:30:00', '2025-04-10 14:30:00', 0);
INSERT INTO `comment` VALUES (26, 10, 1, 26, '奶粉溶解度高，宝宝很容易接受，消化也不错', 5, NULL, 0, 1, '2025-04-05 11:25:00', '2025-04-05 11:25:00', 0);
INSERT INTO `comment` VALUES (27, 11, 2, 27, '婴儿床质量很好，组装简单，宝宝睡得香', 5, NULL, 0, 1, '2025-03-28 15:40:00', '2025-03-28 15:40:00', 0);
INSERT INTO `comment` VALUES (28, 12, 5, 28, '湿巾材质柔软，宝宝用起来很舒服，不刺激', 4, NULL, 0, 1, '2025-03-20 10:15:00', '2025-03-20 10:15:00', 0);
INSERT INTO `comment` VALUES (29, 13, 6, 29, '奶瓶设计合理，宝宝很容易抓握，喝奶很方便', 5, NULL, 0, 1, '2025-03-15 12:30:00', '2025-03-15 12:30:00', 0);
INSERT INTO `comment` VALUES (30, 14, 7, 30, '辅食机操作简单，清洗方便，做出的辅食宝宝很喜欢', 4, NULL, 0, 1, '2025-03-10 09:45:00', '2025-03-10 09:45:00', 0);
INSERT INTO `comment` VALUES (31, 15, 8, 31, '玩具质量很好，色彩鲜艳，宝宝玩得很开心', 5, NULL, 0, 1, '2025-03-05 14:20:00', '2025-03-05 14:20:00', 0);
INSERT INTO `comment` VALUES (32, 16, 9, 32, '餐椅稳固安全，清洁也方便，很实用', 4, NULL, 0, 1, '2025-02-25 11:30:00', '2025-02-25 11:30:00', 0);
INSERT INTO `comment` VALUES (33, 17, 10, 33, '背带材质不错，透气舒适，宝宝不会觉得闷热', 5, NULL, 0, 1, '2025-02-20 09:15:00', '2025-02-20 09:15:00', 0);
INSERT INTO `comment` VALUES (34, 18, 11, 34, '安抚奶嘴形状符合人体工学，宝宝很容易接受', 4, NULL, 0, 1, '2025-02-15 15:40:00', '2025-02-15 15:40:00', 0);
INSERT INTO `comment` VALUES (35, 19, 12, 35, '洗发水香味淡雅，泡沫丰富，洗后头发柔顺', 5, NULL, 0, 1, '2025-02-10 10:25:00', '2025-02-10 10:25:00', 0);
INSERT INTO `comment` VALUES (36, 20, 13, 36, '牙刷大小合适，刷毛软硬适中，宝宝刷牙不抗拒', 4, NULL, 0, 1, '2025-01-28 14:15:00', '2025-01-28 14:15:00', 0);
INSERT INTO `comment` VALUES (37, 2, 14, 37, '浴盆大小合适，底部防滑，洗宝宝很安全', 5, NULL, 0, 1, '2025-01-20 09:30:00', '2025-01-20 09:30:00', 0);
INSERT INTO `comment` VALUES (38, 3, 15, 38, '餐具造型可爱，材质安全，宝宝很喜欢', 4, NULL, 0, 1, '2025-01-15 11:45:00', '2025-01-15 11:45:00', 0);
INSERT INTO `comment` VALUES (39, 4, 16, 39, '读物内容丰富，图片精美，很适合宝宝早教', 5, NULL, 0, 1, '2025-01-10 15:20:00', '2025-01-10 15:20:00', 0);
INSERT INTO `comment` VALUES (40, 5, 17, 40, '护肤霜滋润效果好，宝宝用了皮肤不干燥', 4, NULL, 0, 1, '2025-01-05 10:10:00', '2025-01-05 10:10:00', 0);
INSERT INTO `comment` VALUES (41, 6, 18, 41, '口水巾吸水性很好，材质柔软，宝宝用着舒服', 5, NULL, 0, 1, '2025-02-24 09:15:00', '2025-02-24 09:15:00', 0);
INSERT INTO `comment` VALUES (42, 7, 19, 42, '防蚊液效果显著，天然配方，对宝宝皮肤很温和', 4, NULL, 0, 1, '2025-02-26 14:30:00', '2025-02-26 14:30:00', 0);
INSERT INTO `comment` VALUES (43, 8, 1, 43, '奶粉品质很好，宝宝喝了不上火，推荐购买', 5, NULL, 0, 1, '2025-03-01 10:20:00', '2025-03-01 10:20:00', 0);
INSERT INTO `comment` VALUES (44, 9, 2, 44, '婴儿床做工精细，漆料环保，值得购买', 5, NULL, 0, 1, '2025-03-03 16:45:00', '2025-03-03 16:45:00', 0);
INSERT INTO `comment` VALUES (45, 10, 3, 45, '婴儿车轻便实用，折叠方便，外出必备', 4, NULL, 0, 1, '2025-03-05 11:30:00', '2025-03-05 11:30:00', 0);
INSERT INTO `comment` VALUES (46, 11, 4, 46, '尿不湿透气性好，宝宝用了不会红屁股', 5, NULL, 0, 1, '2025-03-08 09:20:00', '2025-03-08 09:20:00', 0);
INSERT INTO `comment` VALUES (47, 12, 5, 47, '湿巾质地柔软，不含酒精，很安全', 4, NULL, 0, 1, '2025-03-10 15:40:00', '2025-03-10 15:40:00', 0);
INSERT INTO `comment` VALUES (48, 13, 6, 48, '奶瓶防胀气设计很好，宝宝喝奶不会吐泡', 5, NULL, 0, 1, '2025-03-12 12:25:00', '2025-03-12 12:25:00', 0);
INSERT INTO `comment` VALUES (49, 14, 7, 49, '辅食机容量适中，清洗方便，很实用', 4, NULL, 0, 1, '2025-03-15 10:15:00', '2025-03-15 10:15:00', 0);
INSERT INTO `comment` VALUES (50, 15, 8, 50, '玩具安全无毒，设计新颖，宝宝玩得很开心', 5, NULL, 0, 1, '2025-03-18 14:30:00', '2025-03-18 14:30:00', 0);
INSERT INTO `comment` VALUES (51, 16, 9, 51, '餐椅结实耐用，调节方便，性价比高', 4, NULL, 0, 1, '2025-03-20 11:45:00', '2025-03-20 11:45:00', 0);
INSERT INTO `comment` VALUES (52, 17, 10, 52, '背带人体工学设计，背着宝宝不累', 5, NULL, 0, 1, '2025-03-23 09:30:00', '2025-03-23 09:30:00', 0);
INSERT INTO `comment` VALUES (53, 18, 11, 53, '安抚奶嘴材质安全，宝宝很喜欢', 4, NULL, 0, 1, '2025-03-25 16:20:00', '2025-03-25 16:20:00', 0);
INSERT INTO `comment` VALUES (54, 19, 12, 54, '洗发水无泪配方，洗头不怕进眼睛', 5, NULL, 0, 1, '2025-03-28 13:15:00', '2025-03-28 13:15:00', 0);
INSERT INTO `comment` VALUES (55, 20, 13, 55, '牙刷手柄防滑设计，宝宝好抓握', 4, NULL, 0, 1, '2025-03-30 10:40:00', '2025-03-30 10:40:00', 0);
INSERT INTO `comment` VALUES (56, 2, 14, 56, '浴盆材质环保，深度合适，很好用', 5, NULL, 0, 1, '2025-04-02 15:25:00', '2025-04-02 15:25:00', 0);
INSERT INTO `comment` VALUES (57, 3, 15, 57, '餐具分格设计，训练宝宝自己吃饭好帮手', 4, NULL, 0, 1, '2025-04-05 11:30:00', '2025-04-05 11:30:00', 0);
INSERT INTO `comment` VALUES (58, 4, 16, 58, '绘本故事生动有趣，很适合宝宝阅读', 5, NULL, 0, 1, '2025-04-08 14:20:00', '2025-04-08 14:20:00', 0);
INSERT INTO `comment` VALUES (59, 5, 17, 59, '护肤霜补水保湿效果明显，宝宝皮肤改善了', 4, NULL, 0, 1, '2025-04-10 09:45:00', '2025-04-10 09:45:00', 0);
INSERT INTO `comment` VALUES (60, 6, 18, 60, '口水巾手感细腻，不掉毛，质量好', 5, NULL, 0, 1, '2025-04-12 16:30:00', '2025-04-12 16:30:00', 0);
INSERT INTO `comment` VALUES (61, 7, 19, 61, '防蚊液持久有效，气味清新自然', 4, NULL, 0, 1, '2025-04-15 10:20:00', '2025-04-15 10:20:00', 0);
INSERT INTO `comment` VALUES (62, 8, 1, 62, '奶粉营养均衡，宝宝喝了长得好', 5, NULL, 0, 1, '2025-04-18 13:40:00', '2025-04-18 13:40:00', 0);
INSERT INTO `comment` VALUES (63, 9, 2, 63, '婴儿床透气性好，宝宝睡眠质量提高了', 5, NULL, 0, 1, '2025-04-20 15:15:00', '2025-04-20 15:15:00', 0);
INSERT INTO `comment` VALUES (64, 10, 3, 64, '婴儿车避震效果好，宝宝坐着很舒服', 4, NULL, 0, 1, '2025-04-23 11:30:00', '2025-04-23 11:30:00', 0);
INSERT INTO `comment` VALUES (65, 11, 4, 65, '尿不湿吸收量大，用着很放心', 5, NULL, 0, 1, '2025-04-25 14:25:00', '2025-04-25 14:25:00', 0);
INSERT INTO `comment` VALUES (66, 12, 5, 66, '湿巾便携装设计很贴心，外出很方便', 4, NULL, 0, 1, '2025-04-28 09:50:00', '2025-04-28 09:50:00', 0);
INSERT INTO `comment` VALUES (67, 13, 6, 67, '奶瓶刻度清晰，容量适中，很实用', 5, NULL, 0, 1, '2025-04-30 16:15:00', '2025-04-30 16:15:00', 0);
INSERT INTO `comment` VALUES (68, 14, 7, 68, '辅食机噪音小，打泥细腻，很满意', 4, NULL, 0, 1, '2025-05-02 10:40:00', '2025-05-02 10:40:00', 0);
INSERT INTO `comment` VALUES (69, 15, 8, 69, '玩具益智有趣，促进宝宝智力发展', 5, NULL, 0, 1, '2025-05-05 13:20:00', '2025-05-05 13:20:00', 0);
INSERT INTO `comment` VALUES (70, 16, 9, 70, '餐椅便携轻巧，带出去很方便', 4, NULL, 0, 1, '2025-05-08 15:45:00', '2025-05-08 15:45:00', 0);
INSERT INTO `comment` VALUES (71, 17, 10, 71, '背带透气舒适，夏天用也不会热', 5, NULL, 0, 1, '2025-05-10 11:30:00', '2025-05-10 11:30:00', 0);
INSERT INTO `comment` VALUES (72, 18, 11, 72, '安抚奶嘴消毒方便，备用装很实惠', 4, NULL, 0, 1, '2025-05-12 14:15:00', '2025-05-12 14:15:00', 0);
INSERT INTO `comment` VALUES (73, 19, 12, 73, '洗发水去垢力强，不伤发质，很好', 5, NULL, 0, 1, '2025-05-15 09:40:00', '2025-05-15 09:40:00', 0);
INSERT INTO `comment` VALUES (74, 20, 13, 74, '牙刷软毛设计，清洁效果好，宝宝喜欢', 4, NULL, 0, 1, '2025-05-18 16:25:00', '2025-05-18 16:25:00', 0);
INSERT INTO `comment` VALUES (75, 2, 14, 75, '浴盆排水设计合理，用完好收纳', 5, NULL, 0, 1, '2025-05-20 10:50:00', '2025-05-20 10:50:00', 0);
INSERT INTO `comment` VALUES (76, 3, 15, 76, '餐具分格设计，训练宝宝自己吃饭好帮手', 4, NULL, 0, 1, '2025-05-22 13:30:00', '2025-05-22 13:30:00', 0);
INSERT INTO `comment` VALUES (77, 4, 16, 77, '绘本质量好，不易破损，很耐用', 5, NULL, 0, 1, '2025-05-25 15:15:00', '2025-05-25 15:15:00', 0);
INSERT INTO `comment` VALUES (78, 5, 17, 78, '护肤霜补水保湿效果明显，宝宝皮肤改善了', 4, NULL, 0, 1, '2025-05-28 11:40:00', '2025-05-28 11:40:00', 0);
INSERT INTO `comment` VALUES (79, 6, 18, 79, '口水巾大小适中，性价比高', 5, NULL, 0, 1, '2025-05-30 14:20:00', '2025-05-30 14:20:00', 0);
INSERT INTO `comment` VALUES (80, 7, 19, 80, '防蚊液安全无刺激，效果持久', 4, NULL, 0, 1, '2025-06-01 09:45:00', '2025-06-01 09:45:00', 0);
INSERT INTO `comment` VALUES (81, 8, 1, 81, '奶粉溶解度高，没有结块，冲泡方便', 5, NULL, 0, 1, '2025-02-24 10:30:00', '2025-02-24 10:30:00', 0);
INSERT INTO `comment` VALUES (82, 9, 2, 82, '婴儿床围栏设计安全，宝宝睡觉更放心', 5, NULL, 0, 1, '2025-02-26 15:45:00', '2025-02-26 15:45:00', 0);
INSERT INTO `comment` VALUES (83, 10, 3, 83, '婴儿车座椅角度可调，宝宝睡觉很舒服', 4, NULL, 0, 1, '2025-03-01 11:20:00', '2025-03-01 11:20:00', 0);
INSERT INTO `comment` VALUES (84, 11, 4, 84, '尿不湿边缘柔软，不会勒到宝宝', 5, NULL, 0, 1, '2025-03-03 14:35:00', '2025-03-03 14:35:00', 0);
INSERT INTO `comment` VALUES (85, 12, 5, 85, '湿巾材质厚实，擦拭清洁很干净', 4, NULL, 0, 1, '2025-03-05 09:50:00', '2025-03-05 09:50:00', 0);
INSERT INTO `comment` VALUES (86, 13, 6, 86, '奶瓶材质安全，耐高温消毒没问题', 5, NULL, 0, 1, '2025-03-08 16:15:00', '2025-03-08 16:15:00', 0);
INSERT INTO `comment` VALUES (87, 14, 7, 87, '辅食机操作简单，一键完成，很方便', 4, NULL, 0, 1, '2025-03-10 13:40:00', '2025-03-10 13:40:00', 0);
INSERT INTO `comment` VALUES (88, 15, 8, 88, '玩具色彩鲜艳，能吸引宝宝注意力', 5, NULL, 0, 1, '2025-03-12 10:25:00', '2025-03-12 10:25:00', 0);
INSERT INTO `comment` VALUES (89, 16, 9, 89, '餐椅高度合适，宝宝吃饭姿势正确', 4, NULL, 0, 1, '2025-03-15 15:50:00', '2025-03-15 15:50:00', 0);
INSERT INTO `comment` VALUES (90, 17, 10, 90, '背带肩带加宽设计，减轻肩部压力', 5, NULL, 0, 1, '2025-03-18 12:15:00', '2025-03-18 12:15:00', 0);
INSERT INTO `comment` VALUES (91, 18, 11, 91, '安抚奶嘴形状符合人体工学，宝宝含着舒服', 4, NULL, 0, 1, '2025-03-20 09:30:00', '2025-03-20 09:30:00', 0);
INSERT INTO `comment` VALUES (92, 19, 12, 92, '洗发水香味清新，不刺激眼睛', 5, NULL, 0, 1, '2025-03-23 14:45:00', '2025-03-23 14:45:00', 0);
INSERT INTO `comment` VALUES (93, 20, 13, 93, '牙刷大小适中，宝宝刷牙很积极', 4, NULL, 0, 1, '2025-03-25 11:20:00', '2025-03-25 11:20:00', 0);
INSERT INTO `comment` VALUES (94, 2, 14, 94, '浴盆防滑设计，给宝宝洗澡更安全', 5, NULL, 0, 1, '2025-03-28 16:35:00', '2025-03-28 16:35:00', 0);
INSERT INTO `comment` VALUES (95, 3, 15, 95, '餐具材质安全，可以放心使用', 4, NULL, 0, 1, '2025-03-30 13:50:00', '2025-03-30 13:50:00', 0);
INSERT INTO `comment` VALUES (96, 4, 16, 96, '绘本内容寓教于乐，很适合早教', 5, NULL, 0, 1, '2025-04-02 10:15:00', '2025-04-02 10:15:00', 0);
INSERT INTO `comment` VALUES (97, 5, 17, 97, '护肤霜不油腻，宝宝用了不会长痱子', 4, NULL, 0, 1, '2025-04-05 15:40:00', '2025-04-05 15:40:00', 0);
INSERT INTO `comment` VALUES (98, 6, 18, 98, '口水巾边缘车线工整，耐洗耐用', 5, NULL, 0, 1, '2025-04-08 12:25:00', '2025-04-08 12:25:00', 0);
INSERT INTO `comment` VALUES (99, 7, 19, 99, '防蚊液喷头设计合理，使用方便', 4, NULL, 0, 1, '2025-04-10 09:50:00', '2025-04-10 09:50:00', 0);
INSERT INTO `comment` VALUES (100, 8, 1, 100, '奶粉口感清淡，宝宝很喜欢喝', 5, NULL, 0, 1, '2025-04-12 14:15:00', '2025-04-12 14:15:00', 0);
INSERT INTO `comment` VALUES (101, 9, 2, 101, '婴儿床高度可调节，照顾宝宝更轻松', 5, NULL, 0, 1, '2025-04-15 11:30:00', '2025-04-15 11:30:00', 0);
INSERT INTO `comment` VALUES (102, 10, 3, 102, '婴儿车推起来很轻便，单手操作没问题', 4, NULL, 0, 1, '2025-04-18 16:45:00', '2025-04-18 16:45:00', 0);
INSERT INTO `comment` VALUES (103, 11, 4, 103, '尿不湿弹性好，不会漏尿', 5, NULL, 0, 1, '2025-04-20 13:20:00', '2025-04-20 13:20:00', 0);
INSERT INTO `comment` VALUES (104, 12, 5, 104, '湿巾独立包装，携带使用很方便', 4, NULL, 0, 1, '2025-04-23 10:35:00', '2025-04-23 10:35:00', 0);
INSERT INTO `comment` VALUES (105, 13, 6, 105, '奶瓶握把设计人性化，宝宝能自己拿', 5, NULL, 0, 1, '2025-04-25 15:50:00', '2025-04-25 15:50:00', 0);
INSERT INTO `comment` VALUES (106, 14, 7, 106, '辅食机清洗方便，不会藏污纳垢', 4, NULL, 0, 1, '2025-04-28 12:15:00', '2025-04-28 12:15:00', 0);
INSERT INTO `comment` VALUES (107, 15, 8, 107, '玩具材质环保，可以放心玩耍', 5, NULL, 0, 1, '2025-04-30 09:40:00', '2025-04-30 09:40:00', 0);
INSERT INTO `comment` VALUES (108, 16, 9, 108, '餐椅折叠收纳方便，不占空间', 4, NULL, 0, 1, '2025-05-02 14:55:00', '2025-05-02 14:55:00', 0);
INSERT INTO `comment` VALUES (109, 17, 10, 109, '背带设计时尚，外出很有面子', 5, NULL, 0, 1, '2025-05-05 11:30:00', '2025-05-05 11:30:00', 0);
INSERT INTO `comment` VALUES (110, 18, 11, 110, '安抚奶嘴易清洗，卫生放心', 4, NULL, 0, 1, '2025-05-08 16:45:00', '2025-05-08 16:45:00', 0);
INSERT INTO `comment` VALUES (111, 19, 12, 111, '洗发水泡沫丰富，清洗很干净', 5, NULL, 0, 1, '2025-05-10 13:20:00', '2025-05-10 13:20:00', 0);
INSERT INTO `comment` VALUES (112, 20, 13, 112, '牙刷颜色鲜艳，宝宝很喜欢', 4, NULL, 0, 1, '2025-05-12 10:35:00', '2025-05-12 10:35:00', 0);
INSERT INTO `comment` VALUES (113, 2, 14, 113, '浴盆大小合适，使用空间充足', 5, NULL, 0, 1, '2025-05-15 15:50:00', '2025-05-15 15:50:00', 0);
INSERT INTO `comment` VALUES (114, 3, 15, 114, '餐具套装实用，价格实惠', 4, NULL, 0, 1, '2025-05-18 12:15:00', '2025-05-18 12:15:00', 0);
INSERT INTO `comment` VALUES (115, 4, 16, 115, '绘本图画精美，宝宝爱不释手', 5, NULL, 0, 1, '2025-05-20 09:40:00', '2025-05-20 09:40:00', 0);
INSERT INTO `comment` VALUES (116, 5, 17, 116, '护肤霜保质期长，用着很放心', 4, NULL, 0, 1, '2025-05-22 14:55:00', '2025-05-22 14:55:00', 0);
INSERT INTO `comment` VALUES (117, 6, 18, 117, '口水巾颜色款式多，搭配衣服好看', 5, NULL, 0, 1, '2025-05-25 11:30:00', '2025-05-25 11:30:00', 0);
INSERT INTO `comment` VALUES (118, 7, 19, 118, '防蚊液驱蚊效果好，夏天必备', 4, NULL, 0, 1, '2025-05-28 16:45:00', '2025-05-28 16:45:00', 0);
INSERT INTO `comment` VALUES (119, 8, 1, 119, '奶粉保质期长，储存方便', 5, NULL, 0, 1, '2025-05-30 13:20:00', '2025-05-30 13:20:00', 0);
INSERT INTO `comment` VALUES (120, 9, 2, 120, '婴儿床组装简单，说明书清晰', 4, NULL, 0, 1, '2025-06-01 10:35:00', '2025-06-01 10:35:00', 0);
INSERT INTO `comment` VALUES (121, 10, 3, 121, '婴儿车配件齐全，性价比高', 5, NULL, 0, 1, '2025-02-24 11:45:00', '2025-02-24 11:45:00', 0);
INSERT INTO `comment` VALUES (122, 11, 4, 122, '尿不湿大小码标识清晰，不会买错', 4, NULL, 0, 1, '2025-02-26 16:20:00', '2025-02-26 16:20:00', 0);
INSERT INTO `comment` VALUES (123, 12, 5, 123, '湿巾补充装实惠，家庭装很划算', 5, NULL, 0, 1, '2025-03-01 13:35:00', '2025-03-01 13:35:00', 0);
INSERT INTO `comment` VALUES (124, 13, 6, 124, '奶瓶刻度准确，冲奶粉很方便', 4, NULL, 0, 1, '2025-03-03 10:50:00', '2025-03-03 10:50:00', 0);
INSERT INTO `comment` VALUES (125, 14, 7, 125, '辅食机说明书详细，新手也能轻松上手', 5, NULL, 0, 1, '2025-03-05 15:15:00', '2025-03-05 15:15:00', 0);
INSERT INTO `comment` VALUES (126, 15, 8, 126, '玩具适合宝宝年龄段，很有教育意义', 4, NULL, 0, 1, '2025-03-08 12:30:00', '2025-03-08 12:30:00', 0);
INSERT INTO `comment` VALUES (127, 16, 9, 127, '餐椅清洁简单，不会藏污纳垢', 5, NULL, 0, 1, '2025-03-10 09:45:00', '2025-03-10 09:45:00', 0);
INSERT INTO `comment` VALUES (128, 17, 10, 128, '背带结实耐用，背了很久都没坏', 4, NULL, 0, 1, '2025-03-12 14:20:00', '2025-03-12 14:20:00', 0);
INSERT INTO `comment` VALUES (129, 18, 11, 129, '安抚奶嘴带防尘盖，外出使用卫生', 5, NULL, 0, 1, '2025-03-15 11:35:00', '2025-03-15 11:35:00', 0);
INSERT INTO `comment` VALUES (130, 19, 12, 130, '洗发水温和不刺激，宝宝洗头不哭闹', 4, NULL, 0, 1, '2025-03-18 16:50:00', '2025-03-18 16:50:00', 0);
INSERT INTO `comment` VALUES (131, 20, 13, 131, '牙刷柄部防滑，宝宝好抓握', 5, NULL, 0, 1, '2025-03-20 13:15:00', '2025-03-20 13:15:00', 0);
INSERT INTO `comment` VALUES (132, 2, 14, 132, '浴盆温度计准确，水温控制得当', 4, NULL, 0, 1, '2025-03-23 10:30:00', '2025-03-23 10:30:00', 0);
INSERT INTO `comment` VALUES (133, 3, 15, 133, '餐具不含双酚A，可以放心使用', 5, NULL, 0, 1, '2025-03-25 15:45:00', '2025-03-25 15:45:00', 0);
INSERT INTO `comment` VALUES (134, 4, 16, 134, '绘本故事有趣，宝宝很爱听', 4, NULL, 0, 1, '2025-03-28 12:20:00', '2025-03-28 12:20:00', 0);
INSERT INTO `comment` VALUES (135, 5, 17, 135, '护肤霜保湿效果好，宝宝皮肤水嫩', 5, NULL, 0, 1, '2025-03-30 09:35:00', '2025-03-30 09:35:00', 0);
INSERT INTO `comment` VALUES (136, 6, 18, 136, '口水巾吸水快干，很实用', 4, NULL, 0, 1, '2025-04-02 14:50:00', '2025-04-02 14:50:00', 0);
INSERT INTO `comment` VALUES (137, 7, 19, 137, '防蚊液味道清新，不刺鼻', 5, NULL, 0, 1, '2025-04-05 11:15:00', '2025-04-05 11:15:00', 0);
INSERT INTO `comment` VALUES (138, 8, 1, 138, '奶粉营养全面，宝宝喝了长得好', 5, '[\"comment/img_138_01.jpg\"]', 0, 1, '2025-04-08 16:30:00', '2025-04-08 16:30:00', 0);
INSERT INTO `comment` VALUES (139, 9, 2, 139, '婴儿床稳固耐用，性价比高', 4, NULL, 0, 1, '2025-04-10 13:45:00', '2025-04-10 13:45:00', 0);
INSERT INTO `comment` VALUES (140, 10, 3, 140, '婴儿车转向灵活，推着很省力', 5, '[\"comment/img_140_01.jpg\", \"comment/img_140_02.jpg\"]', 0, 1, '2025-04-12 10:20:00', '2025-04-12 10:20:00', 0);
INSERT INTO `comment` VALUES (141, 11, 4, 141, '尿不湿包装很好，没有破损，质量有保证', 5, '[\"comment/img_141_01.jpg\"]', 0, 1, '2025-04-13 11:25:00', '2025-04-13 11:25:00', 0);
INSERT INTO `comment` VALUES (142, 12, 5, 142, '湿巾用着特别放心，宝宝皮肤没有过敏', 4, NULL, 0, 1, '2025-04-14 13:40:00', '2025-04-14 13:40:00', 0);
INSERT INTO `comment` VALUES (143, 13, 6, 143, '奶瓶防摔设计很好，宝宝自己拿着喝也不怕', 5, '[\"comment/img_143_01.jpg\", \"comment/img_143_02.jpg\"]', 0, 1, '2025-04-15 15:20:00', '2025-04-15 15:20:00', 0);
INSERT INTO `comment` VALUES (144, 14, 7, 144, '辅食机的搅拌功能很强大，打出来的泥很细腻', 4, '[\"comment/img_144_01.jpg\"]', 0, 1, '2025-04-16 09:30:00', '2025-04-16 09:30:00', 0);
INSERT INTO `comment` VALUES (145, 15, 8, 145, '玩具的质量真的没得说，宝宝玩了好久都不坏', 5, NULL, 0, 1, '2025-04-17 14:15:00', '2025-04-17 14:15:00', 0);
INSERT INTO `comment` VALUES (146, 16, 9, 146, '餐椅的安全带设计很人性化，宝宝坐着很稳当', 5, '[\"comment/img_146_01.jpg\"]', 0, 1, '2025-04-18 16:40:00', '2025-04-18 16:40:00', 0);
INSERT INTO `comment` VALUES (147, 17, 10, 147, '背带的肩带很宽，背着不勒肩，很舒服', 4, '[\"comment/img_147_01.jpg\", \"comment/img_147_02.jpg\"]', 0, 1, '2025-04-19 10:25:00', '2025-04-19 10:25:00', 0);
INSERT INTO `comment` VALUES (148, 18, 11, 148, '安抚奶嘴的材质很软，宝宝用着很舒服', 5, NULL, 0, 1, '2025-04-20 13:50:00', '2025-04-20 13:50:00', 0);
INSERT INTO `comment` VALUES (149, 19, 12, 149, '洗发水的味道很清淡，宝宝很喜欢', 4, '[\"comment/img_149_01.jpg\"]', 0, 1, '2025-04-21 15:30:00', '2025-04-21 15:30:00', 0);
INSERT INTO `comment` VALUES (150, 20, 13, 150, '牙刷的毛很软，宝宝刷牙不会不舒服', 5, NULL, 0, 1, '2025-04-22 11:20:00', '2025-04-22 11:20:00', 0);
INSERT INTO `comment` VALUES (151, 2, 14, 151, '浴盆的大小刚刚好，放在浴室很合适', 4, '[\"comment/img_151_01.jpg\"]', 0, 1, '2025-04-23 14:45:00', '2025-04-23 14:45:00', 0);
INSERT INTO `comment` VALUES (152, 3, 15, 152, '餐具的颜色很漂亮，宝宝吃饭积极性提高了', 5, '[\"comment/img_152_01.jpg\", \"comment/img_152_02.jpg\"]', 0, 1, '2025-04-24 16:30:00', '2025-04-24 16:30:00', 0);
INSERT INTO `comment` VALUES (153, 4, 16, 153, '绘本的内容很有趣，宝宝每天都要看', 4, NULL, 0, 1, '2025-04-25 09:15:00', '2025-04-25 09:15:00', 0);
INSERT INTO `comment` VALUES (154, 5, 17, 154, '护肤霜很好推开，不会有粘腻感', 5, '[\"comment/img_154_01.jpg\"]', 0, 1, '2025-04-26 11:40:00', '2025-04-26 11:40:00', 0);
INSERT INTO `comment` VALUES (155, 6, 18, 155, '口水巾的吸水性很好，而且晾干很快', 4, NULL, 0, 1, '2025-04-27 13:25:00', '2025-04-27 13:25:00', 0);
INSERT INTO `comment` VALUES (156, 7, 19, 156, '防蚊液喷出来的雾很细，不会呛到宝宝', 5, '[\"comment/img_156_01.jpg\", \"comment/img_156_02.jpg\"]', 0, 1, '2025-04-28 15:50:00', '2025-04-28 15:50:00', 0);
INSERT INTO `comment` VALUES (157, 8, 1, 157, '奶粉的营养很全面，宝宝喝了长得很好', 4, '[\"comment/img_157_01.jpg\"]', 0, 1, '2025-04-29 10:35:00', '2025-04-29 10:35:00', 0);
INSERT INTO `comment` VALUES (158, 9, 2, 158, '婴儿床的床垫很舒服，宝宝睡觉很香', 5, NULL, 0, 1, '2025-04-30 12:20:00', '2025-04-30 12:20:00', 0);
INSERT INTO `comment` VALUES (159, 10, 3, 159, '婴儿车的遮阳棚很大，阳光晒不到宝宝', 5, '[\"comment/img_159_01.jpg\"]', 0, 1, '2025-05-01 14:45:00', '2025-05-01 14:45:00', 0);
INSERT INTO `comment` VALUES (160, 11, 4, 160, '尿不湿的透气性很好，不会闷热', 5, NULL, 0, 1, '2025-05-02 16:30:00', '2025-05-02 16:30:00', 0);
INSERT INTO `comment` VALUES (161, 12, 5, 161, '湿巾的性价比很高，量很足', 4, '[\"comment/img_159_01.jpg\"]', 0, 1, '2025-05-02 16:30:00', '2025-05-02 16:30:00', 0);
INSERT INTO `comment` VALUES (162, 13, 6, 162, '奶瓶的密封性很好，不会漏奶', 5, '[\"comment/img_200_01.jpg\", \"comment/img_200_02.jpg\"]', 0, 1, '2025-05-02 16:30:00', '2025-05-02 16:30:00', 0);
INSERT INTO `comment` VALUES (163, 14, 7, 163, '辅食机的容量刚好，一次能做一顿的量', 4, NULL, 0, 1, '2025-05-05 13:25:00', '2025-05-05 13:25:00', 0);
INSERT INTO `comment` VALUES (164, 15, 8, 164, '玩具的设计很有创意，能培养宝宝的动手能力', 5, '[\"comment/img_164_01.jpg\", \"comment/img_164_02.jpg\"]', 0, 1, '2025-05-06 15:50:00', '2025-05-06 15:50:00', 0);
INSERT INTO `comment` VALUES (165, 16, 9, 165, '餐椅的托盘可以调节距离，很实用', 4, '[\"comment/img_165_01.jpg\"]', 0, 1, '2025-05-07 10:35:00', '2025-05-07 10:35:00', 0);
INSERT INTO `comment` VALUES (166, 17, 10, 166, '背带的搭扣很结实，不用担心会松动', 5, NULL, 0, 1, '2025-05-08 12:20:00', '2025-05-08 12:20:00', 0);
INSERT INTO `comment` VALUES (167, 18, 11, 167, '安抚奶嘴的尺寸很合适，宝宝含着很舒服', 4, '[\"comment/img_167_01.jpg\"]', 0, 1, '2025-05-09 14:45:00', '2025-05-09 14:45:00', 0);
INSERT INTO `comment` VALUES (168, 19, 12, 168, '洗发水用完头发很柔顺，不会打结', 5, '[\"comment/img_168_01.jpg\", \"comment/img_168_02.jpg\"]', 0, 1, '2025-05-10 16:30:00', '2025-05-10 16:30:00', 0);
INSERT INTO `comment` VALUES (169, 20, 13, 169, '牙刷的手柄设计很人性化，宝宝自己刷牙也方便', 4, NULL, 0, 1, '2025-05-11 09:15:00', '2025-05-11 09:15:00', 0);
INSERT INTO `comment` VALUES (170, 2, 14, 170, '浴盆的排水很快，不会积水发霉', 5, '[\"comment/img_170_01.jpg\"]', 0, 1, '2025-05-12 11:40:00', '2025-05-12 11:40:00', 0);
INSERT INTO `comment` VALUES (171, 3, 15, 171, '餐具的材质很好，不会有异味', 4, NULL, 0, 1, '2025-05-13 13:25:00', '2025-05-13 13:25:00', 0);
INSERT INTO `comment` VALUES (172, 4, 16, 172, '绘本的纸质很好，不容易破损', 5, '[\"comment/img_172_01.jpg\", \"comment/img_172_02.jpg\"]', 0, 1, '2025-05-14 15:50:00', '2025-05-14 15:50:00', 0);
INSERT INTO `comment` VALUES (173, 5, 17, 173, '护肤霜的滋润效果很好，宝宝皮肤变得很好', 5, '[\"comment/img_173_01.jpg\"]', 0, 1, '2025-05-15 10:35:00', '2025-05-15 10:35:00', 0);
INSERT INTO `comment` VALUES (174, 6, 18, 174, '口水巾的尺寸很合适，不会太大也不会太小', 5, NULL, 0, 1, '2025-05-16 12:20:00', '2025-05-16 12:20:00', 0);
INSERT INTO `comment` VALUES (175, 7, 19, 175, '防蚊液的持久性很好，喷一次管很久', 5, '[\"comment/img_175_01.jpg\"]', 0, 1, '2025-05-17 14:45:00', '2025-05-17 14:45:00', 0);
INSERT INTO `comment` VALUES (176, 8, 1, 176, '奶粉的价格很实惠，性价比高', 4, NULL, 0, 1, '2025-05-18 16:30:00', '2025-05-18 16:30:00', 0);
INSERT INTO `comment` VALUES (177, 9, 2, 177, '婴儿床的款式很好看，和家里的装修很搭', 5, '[\"comment/img_196_01.jpg\", \"comment/img_196_02.jpg\"]', 0, 1, '2025-05-19 09:15:00', '2025-05-19 09:15:00', 0);
INSERT INTO `comment` VALUES (178, 10, 3, 178, '婴儿车的承重很好，放很多东西都不怕', 4, '[\"comment/img_197_01.jpg\"]', 0, 1, '2025-05-20 11:40:00', '2025-05-20 11:40:00', 0);
INSERT INTO `comment` VALUES (179, 11, 4, 179, '尿不湿的透气性很好，不会闷热', 5, NULL, 0, 1, '2025-05-21 13:25:00', '2025-05-21 13:25:00', 0);
INSERT INTO `comment` VALUES (180, 12, 5, 180, '湿巾的水分很足，擦拭很干净', 5, '[\"comment/img_180_01.jpg\", \"comment/img_180_02.jpg\"]', 0, 1, '2025-05-22 15:50:00', '2025-05-22 15:50:00', 0);
INSERT INTO `comment` VALUES (181, 13, 6, 181, '奶瓶的防胀气设计很好，宝宝不会吐奶', 4, '[\"comment/img_181_01.jpg\"]', 0, 1, '2025-05-23 10:35:00', '2025-05-23 10:35:00', 0);
INSERT INTO `comment` VALUES (182, 14, 7, 182, '辅食机的清洗很方便，不会有死角', 5, NULL, 0, 1, '2025-05-24 12:20:00', '2025-05-24 12:20:00', 0);
INSERT INTO `comment` VALUES (183, 15, 8, 183, '玩具的安全性很好，没有尖锐的边角', 4, '[\"comment/img_183_01.jpg\"]', 0, 1, '2025-05-25 14:45:00', '2025-05-25 14:45:00', 0);
INSERT INTO `comment` VALUES (184, 16, 9, 184, '餐椅的材质很好，擦拭很方便', 5, '[\"comment/img_184_01.jpg\", \"comment/img_184_02.jpg\"]', 0, 1, '2025-05-26 16:30:00', '2025-05-26 16:30:00', 0);
INSERT INTO `comment` VALUES (185, 17, 10, 185, '背带的透气性很好，夏天用也不会闷', 4, NULL, 0, 1, '2025-05-27 09:15:00', '2025-05-27 09:15:00', 0);
INSERT INTO `comment` VALUES (186, 18, 11, 186, '安抚奶嘴的卫生很好保持，好清洗', 5, '[\"comment/img_186_01.jpg\"]', 0, 1, '2025-05-28 11:40:00', '2025-05-28 11:40:00', 0);
INSERT INTO `comment` VALUES (187, 19, 12, 187, '洗发水的起泡很好，好冲洗', 4, NULL, 0, 1, '2025-05-29 13:25:00', '2025-05-29 13:25:00', 0);
INSERT INTO `comment` VALUES (188, 20, 13, 188, '牙刷的质量很好，不掉毛', 5, '[\"comment/img_188_01.jpg\", \"comment/img_188_02.jpg\"]', 0, 1, '2025-05-30 15:50:00', '2025-05-30 15:50:00', 0);
INSERT INTO `comment` VALUES (189, 2, 14, 189, '浴盆的防滑效果很好，很安全', 4, '[\"comment/img_189_01.jpg\"]', 0, 1, '2025-05-31 10:35:00', '2025-05-31 10:35:00', 0);
INSERT INTO `comment` VALUES (190, 3, 15, 190, '餐具的分格设计很实用，食物不会混在一起', 5, NULL, 0, 1, '2025-06-01 12:20:00', '2025-06-01 12:20:00', 0);
INSERT INTO `comment` VALUES (191, 4, 16, 191, '绘本的教育意义很好，内容很适合宝宝', 4, '[\"comment/img_191_01.jpg\"]', 0, 1, '2025-05-31 14:45:00', '2025-05-31 14:45:00', 0);
INSERT INTO `comment` VALUES (192, 5, 17, 192, '护肤霜的滋润效果很好，宝宝皮肤变得很好', 5, '[\"comment/img_192_01.jpg\", \"comment/img_192_02.jpg\"]', 0, 1, '2025-05-31 16:30:00', '2025-05-31 16:30:00', 0);
INSERT INTO `comment` VALUES (193, 6, 18, 193, '口水巾的款式很多，可以搭配不同的衣服', 4, NULL, 0, 1, '2025-06-01 09:15:00', '2025-06-01 09:15:00', 0);
INSERT INTO `comment` VALUES (194, 7, 19, 194, '防蚊液的持久性很好，喷一次管很久', 5, '[\"comment/img_194_01.jpg\"]', 0, 1, '2025-06-01 11:40:00', '2025-06-01 11:40:00', 0);
INSERT INTO `comment` VALUES (195, 8, 1, 195, '奶粉的价格很实惠，性价比高', 3, NULL, 0, 1, '2025-06-01 13:25:00', '2025-05-30 09:34:37', 0);
INSERT INTO `comment` VALUES (196, 9, 2, 196, '婴儿床的款式很好看，和家里的装修很搭', 5, '[\"comment/img_196_01.jpg\", \"comment/img_196_02.jpg\"]', 0, 1, '2025-06-01 15:50:00', '2025-05-28 22:51:04', 1);
INSERT INTO `comment` VALUES (197, 10, 3, 197, '婴儿车的承重很好，放很多东西都不怕', 4, '[\"comment/img_197_01.jpg\"]', 0, 1, '2025-06-01 17:35:00', '2025-06-01 17:35:00', 0);
INSERT INTO `comment` VALUES (198, 11, 4, 198, '尿不湿的透气性很好，不会闷热', 5, NULL, 0, 1, '2025-06-01 19:20:00', '2025-06-01 19:20:00', 0);
INSERT INTO `comment` VALUES (199, 12, 5, 199, '湿巾的性价比很高，量很足', 4, '[\"comment/img_199_01.jpg\"]', 0, 1, '2025-06-01 21:05:00', '2025-06-01 21:05:00', 0);
INSERT INTO `comment` VALUES (200, 13, 6, 200, '奶瓶的密封性很好，不会漏奶', 5, '[\"comment/goods65.jpg\", \"comment/goods66.jpg\"]', 0, 1, '2025-06-01 22:50:00', '2025-05-16 15:39:09', 0);
INSERT INTO `comment` VALUES (201, 9, 53, 201, '牙刷的质量很好，不掉毛', 5, NULL, 0, 1, '2025-04-25 09:19:46', '2025-04-25 09:19:58', 0);
INSERT INTO `comment` VALUES (202, 8, 54, 208, '非常好的商品2222', 5, NULL, 0, 1, '2025-05-17 19:28:10', '2025-05-18 09:12:49', 0);
INSERT INTO `comment` VALUES (207, 8, 58, 206, '非常好的商品，下次会继续购买', 5, NULL, 0, 1, '2025-05-18 09:31:01', '2025-05-18 09:31:01', 0);
INSERT INTO `comment` VALUES (208, 8, 65, 222, '收到货了，包装很好，物流很快，商品质量也不错，价格也实惠，很满意！', 5, NULL, 0, 1, '2025-05-27 20:10:00', '2025-05-27 20:10:00', 0);
INSERT INTO `comment` VALUES (209, 8, 56, 205, '一般，比较满意', 3, NULL, 0, 1, '2025-05-27 22:53:00', '2025-05-30 09:30:48', 0);
INSERT INTO `comment` VALUES (210, 8, 77, 231, '商品质量很好，与描述一致，非常满意，下次还会购买！', 4, NULL, 0, 1, '2025-05-28 22:17:44', '2025-05-28 22:51:04', 1);
INSERT INTO `comment` VALUES (211, 8, 56, 207, '质量非常好，物流很快，包装也很完整，非常满意的一次购物体验！', 5, NULL, 0, 1, '2025-05-30 09:34:50', '2025-05-30 09:34:50', 0);
INSERT INTO `comment` VALUES (212, 8, 60, 236, '质量非常好，物流很快，包装也很完整，非常满意的一次购物体验！', 5, NULL, 0, 1, '2025-06-06 09:47:15', '2025-06-06 09:54:28', 1);
INSERT INTO `comment` VALUES (213, 8, 60, 237, '不太喜欢啊', 2, NULL, 0, 1, '2025-06-06 13:37:45', '2025-06-06 13:39:09', 0);
INSERT INTO `comment` VALUES (214, 8, 95, 260, '一般般，不太喜欢', 3, NULL, 0, 1, '2025-07-11 13:17:03', '2025-07-11 13:18:24', 0);

-- ----------------------------
-- Table structure for comment_reply
-- ----------------------------
DROP TABLE IF EXISTS `comment_reply`;
CREATE TABLE `comment_reply`  (
  `reply_id` int NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `comment_id` int NOT NULL COMMENT '评价ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '回复内容',
  `reply_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '回复类型：1-商家回复，2-用户追评',
  `reply_user_id` int NULL DEFAULT NULL COMMENT '回复用户ID（商家回复时为管理员ID）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`reply_id`) USING BTREE,
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价回复表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment_reply
-- ----------------------------
INSERT INTO `comment_reply` VALUES (1, 196, '质量非常好，物流很快，包装也很完整，非常满意的一次购物体验！', 1, 1, '2025-05-27 18:03:40', '2025-05-27 18:03:40');
INSERT INTO `comment_reply` VALUES (2, 210, '亲爱的顾客，感谢您对我们产品的认可与支持！您的满意是我们最大的动力。如有任何关于产品使用的问题，欢迎随时联系我们的售后客服。我们将持续提供优质的母婴产品和服务，期待您的再次光临！', 1, 1, '2025-05-28 22:39:36', '2025-05-28 22:39:36');
INSERT INTO `comment_reply` VALUES (3, 212, '亲爱的顾客，感谢您对我们产品的认可与支持！您的满意是我们最大的动力。如有任何关于产品使用的问题，欢迎随时联系我们的售后客服。我们将持续提供优质的母婴产品和服务，期待您的再次光临！非常好的用户', 1, 1, '2025-06-06 09:54:28', '2025-06-06 09:54:28');

-- ----------------------------
-- Table structure for comment_reward_config
-- ----------------------------
DROP TABLE IF EXISTS `comment_reward_config`;
CREATE TABLE `comment_reward_config`  (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `reward_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '奖励类型：points-积分',
  `reward_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '基础奖励' COMMENT '奖励名称',
  `reward_description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '奖励描述',
  `reward_level` tinyint NOT NULL DEFAULT 1 COMMENT '奖励等级：1-基础，2-进阶，3-高级',
  `reward_value` int NOT NULL COMMENT '奖励值',
  `min_content_length` int NOT NULL DEFAULT 0 COMMENT '最小内容长度要求',
  `min_rating` tinyint NULL DEFAULT NULL COMMENT '最低评分要求',
  `require_image` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否要求图片：0-不要求，1-要求',
  `min_images` tinyint NOT NULL DEFAULT 0 COMMENT '最低图片数量要求',
  `is_first_comment` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否首次评价奖励：0-否，1-是',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价奖励配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment_reward_config
-- ----------------------------
INSERT INTO `comment_reward_config` VALUES (1, 'points', '基础评价奖励', '完成订单评价获得基础积分', 1, 5, 0, NULL, 0, 0, 0, 1, '2025-05-27 19:37:47', '2025-05-27 19:37:47');
INSERT INTO `comment_reward_config` VALUES (2, 'points', '优质评价奖励', '评价内容详细，字数超过50字', 2, 10, 50, NULL, 0, 0, 0, 1, '2025-05-27 19:37:47', '2025-05-27 19:37:47');
INSERT INTO `comment_reward_config` VALUES (3, 'points', '图文评价奖励', '评价包含图片，更加直观', 2, 15, 0, NULL, 1, 1, 0, 1, '2025-05-27 19:37:47', '2025-05-27 19:37:47');
INSERT INTO `comment_reward_config` VALUES (4, 'points', '高质量图文评价', '评价内容详细且包含图片', 3, 20, 50, NULL, 1, 1, 0, 1, '2025-05-27 19:37:47', '2025-05-27 19:37:47');
INSERT INTO `comment_reward_config` VALUES (5, 'points', '多图评价奖励', '评价包含3张及以上图片', 3, 25, 0, NULL, 1, 3, 0, 1, '2025-05-27 19:37:47', '2025-05-27 19:37:47');
INSERT INTO `comment_reward_config` VALUES (6, 'points', '首次评价奖励', '首次评价商品获得额外奖励', 2, 10, 0, NULL, 0, 0, 1, 1, '2025-05-27 19:37:47', '2025-05-27 19:37:47');
INSERT INTO `comment_reward_config` VALUES (7, 'points', '好评奖励', '给出4-5星好评', 1, 8, 0, 4, 0, 0, 0, 1, '2025-05-27 19:37:47', '2025-05-27 19:37:47');
INSERT INTO `comment_reward_config` VALUES (8, 'points', '基础评价奖励', '完成商品评价获得奖励', 1, 10, 5, 1, 0, 0, 0, 1, '2025-05-27 23:06:36', '2025-05-27 23:06:36');

-- ----------------------------
-- Table structure for comment_tag
-- ----------------------------
DROP TABLE IF EXISTS `comment_tag`;
CREATE TABLE `comment_tag`  (
  `tag_id` int NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `tag_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '标签类型：1-系统标签，2-用户自定义标签',
  `product_category_id` int NULL DEFAULT NULL COMMENT '关联的商品分类ID（可为空）',
  `usage_count` int NOT NULL DEFAULT 0 COMMENT '使用次数',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tag_id`) USING BTREE,
  UNIQUE INDEX `uk_tag_name`(`tag_name` ASC) USING BTREE,
  INDEX `idx_product_category`(`product_category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment_tag
-- ----------------------------
INSERT INTO `comment_tag` VALUES (1, '物流快', 1, NULL, 4, 1, '2025-05-17 20:19:20', '2025-06-06 09:47:15');
INSERT INTO `comment_tag` VALUES (2, '质量好', 1, NULL, 2, 1, '2025-05-17 20:19:20', '2025-06-06 13:39:09');
INSERT INTO `comment_tag` VALUES (3, '性价比高', 1, NULL, 5, 1, '2025-05-17 20:19:20', '2025-06-06 09:47:15');
INSERT INTO `comment_tag` VALUES (4, '描述相符', 1, NULL, 0, 1, '2025-05-17 20:19:20', '2025-05-17 20:19:20');
INSERT INTO `comment_tag` VALUES (5, '服务好', 1, NULL, 0, 1, '2025-05-17 20:19:20', '2025-05-17 20:19:20');
INSERT INTO `comment_tag` VALUES (6, '包装精美', 1, NULL, 0, 1, '2025-05-17 20:19:20', '2025-07-11 13:18:24');
INSERT INTO `comment_tag` VALUES (7, '送货快', 1, NULL, 1, 1, '2025-05-17 20:19:20', '2025-05-30 09:30:49');
INSERT INTO `comment_tag` VALUES (8, '正品保障', 1, NULL, 1, 1, '2025-05-17 20:19:20', '2025-05-28 22:17:44');
INSERT INTO `comment_tag` VALUES (9, '材质优良', 1, NULL, 2, 1, '2025-05-17 20:19:20', '2025-05-28 22:17:44');
INSERT INTO `comment_tag` VALUES (10, '做工精细', 1, NULL, 1, 1, '2025-05-17 20:19:20', '2025-05-18 09:31:01');

-- ----------------------------
-- Table structure for comment_tag_relation
-- ----------------------------
DROP TABLE IF EXISTS `comment_tag_relation`;
CREATE TABLE `comment_tag_relation`  (
  `relation_id` int NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `comment_id` int NOT NULL COMMENT '评价ID',
  `tag_id` int NOT NULL COMMENT '标签ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`relation_id`) USING BTREE,
  UNIQUE INDEX `uk_comment_tag`(`comment_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价标签关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment_tag_relation
-- ----------------------------
INSERT INTO `comment_tag_relation` VALUES (18, 207, 1, '2025-05-18 09:31:01');
INSERT INTO `comment_tag_relation` VALUES (19, 207, 2, '2025-05-18 09:31:01');
INSERT INTO `comment_tag_relation` VALUES (20, 207, 9, '2025-05-18 09:31:01');
INSERT INTO `comment_tag_relation` VALUES (21, 207, 3, '2025-05-18 09:31:01');
INSERT INTO `comment_tag_relation` VALUES (22, 207, 10, '2025-05-18 09:31:01');
INSERT INTO `comment_tag_relation` VALUES (23, 208, 3, '2025-05-27 20:10:00');
INSERT INTO `comment_tag_relation` VALUES (24, 210, 3, '2025-05-28 22:17:44');
INSERT INTO `comment_tag_relation` VALUES (25, 210, 1, '2025-05-28 22:17:44');
INSERT INTO `comment_tag_relation` VALUES (26, 210, 2, '2025-05-28 22:17:44');
INSERT INTO `comment_tag_relation` VALUES (27, 210, 9, '2025-05-28 22:17:44');
INSERT INTO `comment_tag_relation` VALUES (28, 210, 8, '2025-05-28 22:17:44');
INSERT INTO `comment_tag_relation` VALUES (29, 209, 7, '2025-05-30 09:30:49');
INSERT INTO `comment_tag_relation` VALUES (30, 195, 1, '2025-05-30 09:34:37');
INSERT INTO `comment_tag_relation` VALUES (31, 211, 3, '2025-05-30 09:34:50');
INSERT INTO `comment_tag_relation` VALUES (32, 212, 3, '2025-06-06 09:47:15');
INSERT INTO `comment_tag_relation` VALUES (33, 212, 1, '2025-06-06 09:47:15');

-- ----------------------------
-- Table structure for comment_template
-- ----------------------------
DROP TABLE IF EXISTS `comment_template`;
CREATE TABLE `comment_template`  (
  `template_id` int NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `template_content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板内容',
  `template_type` tinyint NOT NULL DEFAULT 1 COMMENT '模板类型：1-系统预设，2-用户自定义',
  `min_rating` tinyint NULL DEFAULT NULL COMMENT '适用评分范围（最小值）',
  `max_rating` tinyint NULL DEFAULT NULL COMMENT '适用评分范围（最大值）',
  `category_id` int NULL DEFAULT NULL COMMENT '适用商品类别ID',
  `use_count` int NOT NULL DEFAULT 0 COMMENT '使用次数',
  `user_id` int NULL DEFAULT NULL COMMENT '创建用户ID（系统模板为null）',
  `weight` int NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`template_id`) USING BTREE,
  INDEX `idx_template_type`(`template_type` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment_template
-- ----------------------------
INSERT INTO `comment_template` VALUES (1, '好评模板1', '质量非常好，物流很快，包装也很完整，非常满意的一次购物体验！', 1, 4, 5, NULL, 0, NULL, 100, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (2, '好评模板2', '商品质量很好，与描述一致，非常满意，下次还会购买！', 1, 4, 5, NULL, 0, NULL, 90, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (3, '好评模板3', '收到货了，包装很好，物流很快，商品质量也不错，价格也实惠，很满意！', 1, 4, 5, NULL, 0, NULL, 80, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (4, '中评模板1', '商品质量一般，没有想象中的那么好，但价格便宜，物有所值。', 1, 3, 3, NULL, 0, NULL, 70, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (5, '中评模板2', '商品基本符合描述，但做工一般，希望卖家能够改进。', 1, 3, 3, NULL, 0, NULL, 60, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (6, '差评模板1', '收到的商品与描述不符，质量较差，不太满意。', 1, 1, 2, NULL, 0, NULL, 50, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (7, '差评模板2', '物流太慢了，商品质量也一般，不是很满意。', 1, 1, 2, NULL, 0, NULL, 40, 1, '2025-05-27 17:56:26', '2025-05-27 17:56:26');
INSERT INTO `comment_template` VALUES (8, '商家好评回复', '亲爱的顾客，感谢您对我们产品的认可与支持！您的满意是我们最大的动力。如有任何关于产品使用的问题，欢迎随时联系我们的售后客服。我们将持续提供优质的母婴产品和服务，期待您的再次光临！', 1, 4, 5, NULL, 0, NULL, 200, 1, '2025-05-27 19:11:23', '2025-05-27 19:11:23');
INSERT INTO `comment_template` VALUES (9, '商家中评回复', '感谢您的购买和反馈！我们非常重视您的体验，对于没能完全满足您的期望深表歉意。我们会认真考虑您的建议，不断改进产品和服务质量。如有任何具体问题需要解决，欢迎联系客服，我们会为您提供专业的售后支持。', 1, 3, 3, NULL, 0, NULL, 180, 1, '2025-05-27 19:11:23', '2025-05-27 19:11:23');
INSERT INTO `comment_template` VALUES (10, '商家差评回复', '非常抱歉没能为您带来满意的购物体验！您的反馈对我们至关重要，我们已记录您反映的问题，将立即进行改进。为表歉意，请联系我们的客服处理售后事宜，我们会尽最大努力解决您遇到的问题，并提供相应的补偿方案。感谢您的理解和支持！', 1, 1, 2, NULL, 0, NULL, 160, 1, '2025-05-27 19:11:23', '2025-05-27 19:11:23');

-- ----------------------------
-- Table structure for coupon
-- ----------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '优惠券名称',
  `batch_id` int UNSIGNED NULL DEFAULT NULL COMMENT '批次ID',
  `rule_id` int UNSIGNED NULL DEFAULT NULL COMMENT '规则ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '优惠券类型：FIXED-固定金额, PERCENTAGE-百分比折扣',
  `value` decimal(10, 2) NOT NULL COMMENT '优惠券面值/折扣值',
  `min_spend` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '最低消费金额',
  `max_discount` decimal(10, 2) NULL DEFAULT NULL COMMENT '最大折扣金额（针对百分比折扣）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-可用, INACTIVE-不可用',
  `category_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用分类ID，多个用逗号分隔，NULL表示全场通用',
  `brand_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用品牌ID，多个用逗号分隔，NULL表示所有品牌',
  `product_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用商品ID，多个用逗号分隔，NULL表示所有商品',
  `is_stackable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可叠加使用：0-不可叠加，1-可叠加',
  `total_quantity` int NOT NULL DEFAULT 0 COMMENT '发行总量，0表示不限量',
  `used_quantity` int NOT NULL DEFAULT 0 COMMENT '已使用数量',
  `received_quantity` int NOT NULL DEFAULT 0 COMMENT '已领取数量',
  `start_time` datetime NOT NULL COMMENT '有效期开始时间',
  `end_time` datetime NOT NULL COMMENT '有效期结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `user_limit` int NULL DEFAULT 1 COMMENT '每用户最大领取次数，默认1次，0表示不限制',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_end_time`(`start_time` ASC, `end_time` ASC) USING BTREE,
  INDEX `idx_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_rule_id`(`rule_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of coupon
-- ----------------------------
INSERT INTO `coupon` VALUES (1, '新用户优惠券', 1, 1, 'FIXED', 50.00, 50.01, NULL, 'ACTIVE', NULL, NULL, NULL, 0, 1010, 2, 7, '2025-04-22 19:54:13', '2025-09-23 00:00:00', '2025-04-23 19:54:13', '2025-05-13 15:55:33', 10);
INSERT INTO `coupon` VALUES (2, '满100减15', 2, 2, 'FIXED', 15.00, 100.00, NULL, 'ACTIVE', NULL, NULL, NULL, 0, 500, 2, 2, '2025-04-22 19:54:13', '2025-05-23 19:54:13', '2025-04-23 19:54:13', '2025-04-24 13:58:53', 1);
INSERT INTO `coupon` VALUES (3, '满200减30', 2, 2, 'FIXED', 30.00, 200.00, NULL, 'ACTIVE', NULL, NULL, NULL, 0, 300, 0, 2, '2025-04-22 19:54:13', '2025-05-23 19:54:13', '2025-04-23 19:54:13', '2025-04-23 20:33:22', 1);
INSERT INTO `coupon` VALUES (4, '满300减50', 2, 2, 'FIXED', 50.00, 300.00, NULL, 'ACTIVE', NULL, NULL, NULL, 0, 200, 1, 1, '2025-04-22 19:54:13', '2025-05-23 19:54:13', '2025-04-23 19:54:13', '2025-04-23 21:36:07', 1);
INSERT INTO `coupon` VALUES (5, '全场9折', 3, 3, 'PERCENTAGE', 0.90, 100.00, 50.00, 'ACTIVE', NULL, NULL, NULL, 0, 100, 0, 0, '2025-04-22 19:54:13', '2025-05-08 19:54:13', '2025-04-23 19:54:13', '2025-04-23 20:21:30', 1);
INSERT INTO `coupon` VALUES (6, '母婴用品85折', 4, 4, 'PERCENTAGE', 0.85, 200.00, 100.00, 'ACTIVE', NULL, NULL, NULL, 0, 300, 0, 1, '2025-04-22 19:54:13', '2025-05-13 19:54:13', '2025-04-23 19:54:13', '2025-04-23 20:22:34', 1);
INSERT INTO `coupon` VALUES (7, '奶粉尿裤满减', 5, 5, 'FIXED', 50.00, 300.00, NULL, 'ACTIVE', NULL, NULL, NULL, 0, 200, 1, 1, '2025-04-22 19:54:13', '2025-05-18 19:54:13', '2025-04-23 19:54:13', '2025-04-24 13:28:54', 1);
INSERT INTO `coupon` VALUES (8, '玩具图书8折', 3, 3, 'PERCENTAGE', 0.80, 150.00, 60.00, 'ACTIVE', NULL, NULL, NULL, 0, 150, 0, 1, '2025-04-22 19:54:13', '2025-05-08 19:54:13', '2025-04-23 19:54:13', '2025-04-23 20:22:37', 1);
INSERT INTO `coupon` VALUES (9, '婴儿洗护满减', 4, 4, 'FIXED', 25.00, 150.00, NULL, 'ACTIVE', NULL, NULL, NULL, 0, 200, 0, 1, '2025-04-22 19:54:13', '2025-05-13 19:54:13', '2025-04-23 19:54:13', '2025-04-24 13:46:01', 1);
INSERT INTO `coupon` VALUES (10, '孕妇专享9折', 5, 5, 'PERCENTAGE', 0.90, 200.00, 80.00, 'ACTIVE', '', '', '', 0, 100, 0, 2, '2025-04-22 19:54:13', '2028-05-31 19:54:13', '2025-04-23 19:54:13', '2025-06-18 19:15:21', 1);

-- ----------------------------
-- Table structure for coupon_batch
-- ----------------------------
DROP TABLE IF EXISTS `coupon_batch`;
CREATE TABLE `coupon_batch`  (
  `batch_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '批次ID',
  `coupon_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '优惠券名称',
  `rule_id` int UNSIGNED NOT NULL COMMENT '规则ID',
  `total_count` int NOT NULL COMMENT '优惠券总数量',
  `assign_count` int NOT NULL DEFAULT 0 COMMENT '已分配数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`batch_id`) USING BTREE,
  INDEX `idx_rule_id`(`rule_id` ASC) USING BTREE,
  CONSTRAINT `fk_coupon_batch_rule` FOREIGN KEY (`rule_id`) REFERENCES `coupon_rule` (`rule_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券批次表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of coupon_batch
-- ----------------------------
INSERT INTO `coupon_batch` VALUES (1, '新用户专享-满50减10券', 1, 1000, 50, '2025-04-23 19:55:38', '2025-04-23 19:55:38');
INSERT INTO `coupon_batch` VALUES (2, '618促销-满100减15券', 2, 500, 100, '2025-04-23 19:55:38', '2025-04-23 19:55:38');
INSERT INTO `coupon_batch` VALUES (3, '会员日-9折优惠券', 3, 300, 30, '2025-04-23 19:55:38', '2025-04-23 19:55:38');
INSERT INTO `coupon_batch` VALUES (4, '婴儿节-母婴85折券', 4, 200, 20, '2025-04-23 19:55:38', '2025-04-23 19:55:38');
INSERT INTO `coupon_batch` VALUES (5, '奶粉尿裤专享满减券', 5, 200, 15, '2025-04-23 19:55:38', '2025-04-23 19:55:38');

-- ----------------------------
-- Table structure for coupon_rule
-- ----------------------------
DROP TABLE IF EXISTS `coupon_rule`;
CREATE TABLE `coupon_rule`  (
  `rule_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称',
  `type` tinyint NOT NULL COMMENT '规则类型：0-满减，1-直减，2-折扣',
  `rule_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则内容JSON',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of coupon_rule
-- ----------------------------
INSERT INTO `coupon_rule` VALUES (1, '新用户满50减10', 0, '{\"threshold\": 50.00, \"amount\": 10.00, \"use_range\": 0, \"receive_count\": 1, \"is_mutex\": true, \"receive_started_at\": \"2025-03-01 00:00:00\", \"receive_ended_at\": \"2025-12-31 23:59:59\", \"use_started_at\": \"2025-03-01 00:00:00\", \"use_ended_at\": \"2025-12-31 23:59:59\"}', '2025-04-23 19:54:50', '2025-04-23 19:54:50');
INSERT INTO `coupon_rule` VALUES (2, '全场满100减15', 0, '{\"threshold\": 100.00, \"amount\": 15.00, \"use_range\": 0, \"receive_count\": 1, \"is_mutex\": true, \"receive_started_at\": \"2025-03-01 00:00:00\", \"receive_ended_at\": \"2025-12-31 23:59:59\", \"use_started_at\": \"2025-03-01 00:00:00\", \"use_ended_at\": \"2025-12-31 23:59:59\"}', '2025-04-23 19:54:50', '2025-04-23 19:54:50');
INSERT INTO `coupon_rule` VALUES (3, '全场9折优惠', 2, '{\"threshold\": 100.00, \"discount\": 0.90, \"max_discount\": 50.00, \"use_range\": 0, \"receive_count\": 1, \"is_mutex\": true, \"receive_started_at\": \"2025-03-01 00:00:00\", \"receive_ended_at\": \"2025-12-31 23:59:59\", \"use_started_at\": \"2025-03-01 00:00:00\", \"use_ended_at\": \"2025-12-31 23:59:59\"}', '2025-04-23 19:54:50', '2025-04-23 19:54:50');
INSERT INTO `coupon_rule` VALUES (4, '母婴用品85折', 2, '{\"threshold\": 200.00, \"discount\": 0.85, \"max_discount\": 100.00, \"use_range\": 2, \"category_ids\": \"1,2,3,4,5\", \"receive_count\": 1, \"is_mutex\": true, \"receive_started_at\": \"2025-03-01 00:00:00\", \"receive_ended_at\": \"2025-12-31 23:59:59\", \"use_started_at\": \"2025-03-01 00:00:00\", \"use_ended_at\": \"2025-12-31 23:59:59\"}', '2025-04-23 19:54:50', '2025-04-23 19:54:50');
INSERT INTO `coupon_rule` VALUES (5, '奶粉尿裤专享', 0, '{\"threshold\": 300.00, \"amount\": 50.00, \"use_range\": 2, \"category_ids\": \"2,7\", \"receive_count\": 1, \"is_mutex\": true, \"receive_started_at\": \"2025-03-01 00:00:00\", \"receive_ended_at\": \"2025-12-31 23:59:59\", \"use_started_at\": \"2025-03-01 00:00:00\", \"use_ended_at\": \"2025-12-31 23:59:59\"}', '2025-04-23 19:54:50', '2025-04-23 19:54:50');

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
  UNIQUE INDEX `idx_user_goods`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_user_goods_time`(`user_id` ASC, `product_id` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (1, 2, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `favorite` VALUES (2, 2, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `favorite` VALUES (3, 3, 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `favorite` VALUES (4, 3, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `favorite` VALUES (8, 9, 59, '2025-04-09 15:27:05', '2025-04-09 15:27:05');
INSERT INTO `favorite` VALUES (14, 1, 59, '2025-04-11 11:39:24', '2025-04-11 11:39:24');
INSERT INTO `favorite` VALUES (15, 1, 53, '2025-04-11 11:39:58', '2025-04-11 11:39:58');
INSERT INTO `favorite` VALUES (16, 1, 58, '2025-04-11 11:40:15', '2025-04-11 11:40:15');
INSERT INTO `favorite` VALUES (18, 9, 58, '2025-04-14 12:38:03', '2025-04-14 12:38:03');
INSERT INTO `favorite` VALUES (24, 9, 60, '2025-04-18 09:29:44', '2025-04-18 09:29:44');
INSERT INTO `favorite` VALUES (27, 9, 8, '2025-04-26 15:17:51', '2025-04-26 15:17:51');
INSERT INTO `favorite` VALUES (28, 9, 56, '2025-04-28 22:49:38', '2025-04-28 22:49:38');
INSERT INTO `favorite` VALUES (29, 9, 36, '2025-04-28 22:50:32', '2025-04-28 22:50:32');
INSERT INTO `favorite` VALUES (32, 8, 58, '2025-05-09 18:28:59', '2025-05-09 18:28:59');
INSERT INTO `favorite` VALUES (33, 8, 23, '2025-05-11 22:26:25', '2025-05-11 22:26:25');
INSERT INTO `favorite` VALUES (35, 8, 54, '2025-05-12 21:03:47', '2025-05-12 21:03:47');
INSERT INTO `favorite` VALUES (36, 8, 59, '2025-05-13 21:41:44', '2025-05-13 21:41:44');
INSERT INTO `favorite` VALUES (37, 8, 66, '2025-05-15 22:08:22', '2025-05-15 22:08:22');
INSERT INTO `favorite` VALUES (38, 8, 64, '2025-05-21 21:52:51', '2025-05-21 21:52:51');
INSERT INTO `favorite` VALUES (39, 8, 35, '2025-05-22 17:43:46', '2025-05-22 17:43:46');
INSERT INTO `favorite` VALUES (42, 8, 68, '2025-05-29 23:23:07', '2025-05-29 23:23:07');
INSERT INTO `favorite` VALUES (43, 8, 93, '2025-05-30 11:33:35', '2025-05-30 11:33:35');
INSERT INTO `favorite` VALUES (45, 8, 84, '2025-06-06 11:22:00', '2025-06-06 11:22:00');
INSERT INTO `favorite` VALUES (47, 58, 113, '2025-06-09 22:40:32', '2025-06-09 22:40:32');
INSERT INTO `favorite` VALUES (48, 58, 95, '2025-06-09 22:40:45', '2025-06-09 22:40:45');
INSERT INTO `favorite` VALUES (49, 58, 60, '2025-06-09 22:42:12', '2025-06-09 22:42:12');
INSERT INTO `favorite` VALUES (51, 8, 83, '2025-07-09 15:12:47', '2025-07-09 15:12:47');
INSERT INTO `favorite` VALUES (52, 59, 85, '2025-07-10 14:32:13', '2025-07-10 14:32:13');
INSERT INTO `favorite` VALUES (53, 59, 84, '2025-07-10 14:37:51', '2025-07-10 14:37:51');
INSERT INTO `favorite` VALUES (54, 8, 95, '2025-07-11 13:14:10', '2025-07-11 13:14:10');
INSERT INTO `favorite` VALUES (55, 9, 77, '2025-07-11 15:08:38', '2025-07-11 15:08:38');
INSERT INTO `favorite` VALUES (56, 9, 104, '2025-07-11 15:15:52', '2025-07-11 15:15:52');
INSERT INTO `favorite` VALUES (57, 8, 102, '2025-07-14 12:49:55', '2025-07-14 12:49:55');
INSERT INTO `favorite` VALUES (58, 8, 69, '2025-07-14 13:04:47', '2025-07-14 13:04:47');
INSERT INTO `favorite` VALUES (59, 8, 50, '2025-07-14 13:05:07', '2025-07-14 13:05:07');

-- ----------------------------
-- Table structure for logistics
-- ----------------------------
DROP TABLE IF EXISTS `logistics`;
CREATE TABLE `logistics`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '物流ID',
  `order_id` int UNSIGNED NOT NULL COMMENT '订单ID',
  `company_id` int UNSIGNED NOT NULL COMMENT '物流公司ID',
  `tracking_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物流单号',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CREATED' COMMENT '物流状态：CREATED-已创建，SHIPPING-运输中，DELIVERED-已送达，EXCEPTION-异常',
  `sender_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发件人姓名',
  `sender_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发件人电话',
  `sender_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发件地址',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人电话',
  `receiver_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件地址',
  `shipping_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '送达时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  UNIQUE INDEX `idx_tracking_no`(`tracking_no` ASC) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_logistics_company` FOREIGN KEY (`company_id`) REFERENCES `logistics_company` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_logistics_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物流表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of logistics
-- ----------------------------
INSERT INTO `logistics` VALUES (2, 205, 1, 'SF2505091322338066', 'DELIVERED', NULL, NULL, NULL, '王大锤', '13666522545', '广东省深圳市福田区紫阳大道1555号', '2025-05-09 13:22:33', '2025-05-09 13:38:24', '自动更新为已送达', '2025-05-09 13:22:33', '2025-05-09 13:22:33');
INSERT INTO `logistics` VALUES (3, 204, 4, 'YD2505092043151360', 'CREATED', NULL, NULL, NULL, '王五', '13444522365', '上海市上海市静安区胜利达到157号', '2025-05-09 20:43:16', NULL, NULL, '2025-05-09 20:43:16', '2025-05-09 20:43:16');
INSERT INTO `logistics` VALUES (4, 202, 5, 'EMS2505092044137839', 'DELIVERED', NULL, NULL, NULL, '王大锤', '13666522545', '广东省深圳市福田区紫阳大道1555号', '2025-05-09 20:44:13', '2025-05-09 20:44:51', '自动更新为已送达', '2025-05-09 20:44:13', '2025-05-09 20:44:13');
INSERT INTO `logistics` VALUES (5, 206, 2, 'ZT2505092120432303', 'DELIVERED', NULL, NULL, NULL, '王大锤', '13666522545', '广东省深圳市福田区紫阳大道1555号', '2025-05-09 21:20:43', '2025-05-09 21:21:14', '自动更新为已送达', '2025-05-09 21:20:43', '2025-05-09 21:20:43');
INSERT INTO `logistics` VALUES (6, 207, 6, 'STO2505092208250563', 'DELIVERED', NULL, NULL, NULL, '王大锤', '13666522545', '广东省深圳市福田区紫阳大道1555号', '2025-05-09 22:08:25', '2025-05-09 22:08:50', '自动更新为已送达', '2025-05-09 22:08:25', '2025-05-09 22:08:25');
INSERT INTO `logistics` VALUES (7, 208, 8, 'JT2505092245138415', 'DELIVERED', NULL, NULL, NULL, '王大锤', '13666522545', '广东省深圳市福田区紫阳大道1555号', '2025-05-09 22:45:14', '2025-05-09 22:45:39', '自动更新为已送达', '2025-05-09 22:45:14', '2025-05-09 22:45:14');
INSERT INTO `logistics` VALUES (8, 203, 7, 'BEST2505102255353745', 'CREATED', NULL, NULL, NULL, '李四', '13666522356', '上海上海市徐汇区地中海10086号', '2025-05-10 22:55:36', NULL, NULL, '2025-05-10 22:55:36', '2025-05-10 22:55:36');
INSERT INTO `logistics` VALUES (9, 209, 3, 'YT2505102301099841', 'CREATED', NULL, NULL, NULL, '张三', '13666544577', '江苏省苏州市吴江区太湖公园18888号', '2025-05-10 23:01:10', NULL, NULL, '2025-05-10 23:01:10', '2025-05-10 23:01:10');
INSERT INTO `logistics` VALUES (10, 210, 5, 'EMS2505102306050130', 'CREATED', NULL, NULL, NULL, '李四', '13666522356', '上海上海市徐汇区地中海10086号', '2025-05-10 23:06:06', NULL, NULL, '2025-05-10 23:06:06', '2025-05-10 23:06:06');
INSERT INTO `logistics` VALUES (11, 222, 13, 'YZPY2505202254008936', 'CREATED', NULL, NULL, NULL, '李狗蛋', '15777455256', '北京市北京市朝阳区胜利街道1887号', '2025-05-20 22:54:01', NULL, NULL, '2025-05-20 22:54:01', '2025-05-20 22:54:01');
INSERT INTO `logistics` VALUES (12, 231, 3, 'YT2505282216561622', 'CREATED', NULL, NULL, NULL, '李狗蛋', '15777455256', '北京市北京市朝阳区胜利街道1887号', '2025-05-28 22:16:56', NULL, NULL, '2025-05-28 22:16:56', '2025-05-28 22:16:56');
INSERT INTO `logistics` VALUES (13, 229, 4, 'YD2505292305010709', 'SHIPPING', NULL, NULL, NULL, '王大锤', '13666522545', '广东省深圳市福田区紫阳大道1555号', '2025-05-29 23:05:01', NULL, NULL, '2025-05-29 23:05:01', '2025-05-29 23:05:01');
INSERT INTO `logistics` VALUES (14, 236, 3, 'YT2506060939567253', 'DELIVERED', NULL, NULL, NULL, '王大锤', '13666522545', '广东省深圳市福田区紫阳大道1555号', '2025-06-06 09:39:56', '2025-06-06 09:45:48', NULL, '2025-06-06 09:39:56', '2025-06-06 09:39:56');
INSERT INTO `logistics` VALUES (15, 235, 5, 'EMS2506061332085716', 'SHIPPING', NULL, NULL, NULL, '王大锤', '13666522545', '广东省深圳市福田区紫阳大道1555号', '2025-06-06 13:32:09', NULL, NULL, '2025-06-06 13:32:09', '2025-06-06 13:32:09');
INSERT INTO `logistics` VALUES (16, 237, 7, 'BEST2506061336586498', 'CREATED', NULL, NULL, NULL, '李狗蛋', '15777455256', '北京市北京市朝阳区胜利街道1887号', '2025-06-06 13:36:58', NULL, NULL, '2025-06-06 13:36:58', '2025-06-06 13:36:58');
INSERT INTO `logistics` VALUES (17, 256, 3, 'YT2507092032483667', 'DELIVERED', NULL, NULL, NULL, '王五', '13444522365', '上海市上海市静安区胜利达到157号', '2025-07-09 20:32:49', '2025-07-10 14:23:31', NULL, '2025-07-09 20:32:49', '2025-07-09 20:32:49');
INSERT INTO `logistics` VALUES (18, 1, 1, 'SF2507106320', 'CREATED', NULL, NULL, NULL, '张三', '13800138000', '上海市浦东新区陆家嘴金融中心', '2025-07-10 14:26:02', NULL, NULL, '2025-07-10 14:26:02', '2025-07-10 14:26:02');
INSERT INTO `logistics` VALUES (19, 259, 5, 'EMS2507101449400205', 'DELIVERED', NULL, NULL, NULL, 'pwz', '15222566568', '安徽省合肥市包河区西湖区10086号', '2025-07-10 14:49:41', '2025-07-10 15:32:56', '系统自动更新：物流已完成送达', '2025-07-10 14:49:41', '2025-07-10 14:49:41');
INSERT INTO `logistics` VALUES (23, 260, 2, 'ZT2507111316109731', 'DELIVERED', NULL, NULL, NULL, '李狗蛋', '15777455256', '北京市北京市朝阳区胜利街道1887号', '2025-07-11 13:16:11', '2025-07-11 13:16:27', '系统自动更新：物流已完成送达', '2025-07-11 13:16:11', '2025-07-11 13:16:11');

-- ----------------------------
-- Table structure for logistics_company
-- ----------------------------
DROP TABLE IF EXISTS `logistics_company`;
CREATE TABLE `logistics_company`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '物流公司ID',
  `code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物流公司代码，用于生成物流单号前缀',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物流公司名称',
  `contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公司地址',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司logo',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物流公司表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of logistics_company
-- ----------------------------
INSERT INTO `logistics_company` VALUES (1, 'SF', '顺丰速运', '客服', '95338', '深圳市宝安区福永大道', 1, 'logistics/sf.png', 1, '2025-05-09 11:40:42', '2025-05-09 11:40:42');
INSERT INTO `logistics_company` VALUES (2, 'ZT', '中通快递', '客服', '95311', '上海市青浦区华新镇华志路', 1, 'logistics/zt.png', 2, '2025-05-09 11:40:42', '2025-05-09 11:40:42');
INSERT INTO `logistics_company` VALUES (3, 'YT', '圆通速递', '客服', '95554', '上海市青浦区华新镇华徐公路', 1, 'logistics/yt.png', 3, '2025-05-09 11:40:42', '2025-05-09 11:40:42');
INSERT INTO `logistics_company` VALUES (4, 'YD', '韵达速递', '客服', '95546', '上海市青浦区盈港东路', 1, 'logistics/yd.png', 4, '2025-05-09 11:40:42', '2025-05-09 11:40:42');
INSERT INTO `logistics_company` VALUES (5, 'EMS', 'EMS快递', '客服', '11183', '北京市西城区金融大街', 1, 'logistics/ems.png', 5, '2025-05-09 11:40:42', '2025-05-09 11:40:42');
INSERT INTO `logistics_company` VALUES (6, 'STO', '申通快递', '客服', '95543', '上海市青浦区华徐公路', 1, 'logistics/sto.png', 6, '2025-05-09 18:01:59', '2025-05-09 18:01:59');
INSERT INTO `logistics_company` VALUES (7, 'BEST', '百世快递', '客服', '95320', '杭州市萧山区经济技术开发区', 1, 'logistics/best.png', 7, '2025-05-09 18:01:59', '2025-05-09 18:01:59');
INSERT INTO `logistics_company` VALUES (8, 'JT', '极兔速递', '客服', '950616', '上海市松江区', 1, 'logistics/jt.png', 8, '2025-05-09 18:01:59', '2025-05-09 18:01:59');
INSERT INTO `logistics_company` VALUES (9, 'JD', '京东物流', '客服', '950616', '北京市通州区', 1, 'logistics/jd.png', 9, '2025-05-09 18:01:59', '2025-05-09 18:01:59');
INSERT INTO `logistics_company` VALUES (10, 'DB', '德邦快递', '客服', '95353', '上海市青浦区', 1, 'logistics/db.png', 10, '2025-05-09 18:01:59', '2025-05-09 18:01:59');
INSERT INTO `logistics_company` VALUES (11, 'ZJS', '宅急送', '客服', '4006789000', '北京市顺义区', 1, 'logistics/zjs.png', 11, '2025-05-09 18:01:59', '2025-05-09 18:01:59');
INSERT INTO `logistics_company` VALUES (12, 'HTKY', '百世快运', '客服', '4009565656', '上海市', 1, 'logistics/htky.png', 12, '2025-05-09 18:01:59', '2025-05-09 18:01:59');
INSERT INTO `logistics_company` VALUES (13, 'YZPY', '邮政快递包裹', '客服', '11185', '北京市西城区', 1, 'logistics/yzpy.png', 13, '2025-05-09 18:01:59', '2025-05-09 18:01:59');
INSERT INTO `logistics_company` VALUES (14, 'ANE', '安能物流', '客服', '4001009288', '上海市', 1, 'logistics/ane.png', 14, '2025-05-09 18:01:59', '2025-05-09 18:01:59');
INSERT INTO `logistics_company` VALUES (15, 'FAST', '快捷快递', '客服', '4008000222', '广东省东莞市', 1, 'logistics/fast.png', 15, '2025-05-09 18:01:59', '2025-05-09 18:01:59');

-- ----------------------------
-- Table structure for logistics_trace
-- ----------------------------
DROP TABLE IF EXISTS `logistics_trace`;
CREATE TABLE `logistics_trace`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `logistics_id` bigint NOT NULL COMMENT '物流信息ID',
  `tracking_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物流运单号',
  `trace_time` datetime NULL DEFAULT NULL COMMENT '最后更新轨迹时间',
  `trace_content` json NULL COMMENT '物流轨迹内容（JSON格式）',
  `trace_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址所在地区（精确到市区）',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trace_time`(`trace_time` ASC) USING BTREE,
  INDEX `idx_tracking_no`(`tracking_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1915573932244312067 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物流轨迹表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of logistics_trace
-- ----------------------------
INSERT INTO `logistics_trace` VALUES (1, 0, '1743163268524221', '2025-04-24 17:03:59', '[{\"time\": \"2025-03-28 20:01:09.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}, {\"time\": \"2025-03-28 20:03:52.000000\", \"content\": \"包裹正在派送\", \"location\": \"上海市\"}, {\"time\": \"2025-03-28 20:03:54.000000\", \"content\": \"包裹已到达分拣中心\", \"location\": \"广州市\"}, {\"time\": \"2025-03-28 20:03:59.000000\", \"content\": \"包裹已送达，请注意查收\", \"location\": \"上海市上海市静安区胜利达到157号\"}, {\"time\": \"2025-03-28 20:03:59.000000\", \"content\": \"包裹已送达，请注意查收\", \"location\": \"上海市\"}, {\"time\": \"2025-03-28 20:04:01.000000\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"上海市上海市静安区胜利达到157号\"}, {\"time\": \"2025-03-28 20:04:01\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"杭州市\"}]', '上海市上海市静安区胜利达到157号', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (2, 0, '1743311497479303', '2025-04-24 17:03:59', '[{\"time\": \"2025-03-30 13:11:38.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}, {\"time\": \"2025-03-30 13:11:47.000000\", \"content\": \"包裹正在运输中\", \"location\": \"上海市\"}]', '上海市', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (3, 0, '1744335125819435', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-11 09:32:06.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}, {\"time\": \"2025-04-11 09:32:29.000000\", \"content\": \"包裹正在派送\", \"location\": \"杭州市\"}, {\"time\": \"2025-04-11 09:32:32.000000\", \"content\": \"包裹已到达分拣中心\", \"location\": \"武汉市\"}, {\"time\": \"2025-04-11 09:32:33.000000\", \"content\": \"包裹已送达，请注意查收\", \"location\": \"上海市上海市静安区胜利达到157号\"}, {\"time\": \"2025-04-11 09:32:33.000000\", \"content\": \"包裹已到达目的地\", \"location\": \"北京市\"}, {\"time\": \"2025-04-11 09:32:35.000000\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"上海市上海市静安区胜利达到157号\"}, {\"time\": \"2025-04-11 09:32:35.000000\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"成都市\"}]', '上海市上海市静安区胜利达到157号', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (4, 0, '1744551738738376', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-13 21:42:19.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}, {\"time\": \"2025-04-13 22:18:58.000000\", \"content\": \"包裹正在派送\", \"location\": \"上海市\"}, {\"time\": \"2025-04-13 22:19:00.000000\", \"content\": \"包裹正在派送\", \"location\": \"杭州市\"}, {\"time\": \"2025-04-13 22:19:01.000000\", \"content\": \"包裹已送达，请注意查收\", \"location\": \"北京市北京市丰台区115745\"}, {\"time\": \"2025-04-13 22:19:01.000000\", \"content\": \"包裹已到达目的地\", \"location\": \"杭州市\"}, {\"time\": \"2025-04-13 22:19:02.000000\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"北京市北京市丰台区115745\"}, {\"time\": \"2025-04-13 22:19:02.000000\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"北京市\"}]', '北京市北京市丰台区115745', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (5, 0, '1744640047350301', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-14 22:14:07.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}, {\"time\": \"2025-04-14 22:14:17.000000\", \"content\": \"包裹已到达分拣中心\", \"location\": \"成都市\"}, {\"time\": \"2025-04-14 22:14:17.000000\", \"content\": \"包裹正在运输中\", \"location\": \"成都市\"}, {\"time\": \"2025-04-14 22:14:18.000000\", \"content\": \"包裹已送达，请注意查收\", \"location\": \"北京市北京市丰台区115745\"}, {\"time\": \"2025-04-14 22:14:18.000000\", \"content\": \"包裹已到达目的地\", \"location\": \"深圳市\"}, {\"time\": \"2025-04-14 22:14:19.000000\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"北京市北京市丰台区115745\"}, {\"time\": \"2025-04-14 22:14:19.000000\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"武汉市\"}]', '武汉市', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (6, 0, '1744640140150042', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-14 22:15:40.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}]', NULL, '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (7, 0, '1744640143229068', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-14 22:15:43.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}, {\"time\": \"2025-04-14 22:16:25.000000\", \"content\": \"包裹正在派送\", \"location\": \"杭州市\"}, {\"time\": \"2025-04-14 22:16:26.000000\", \"content\": \"包裹已到达分拣中心\", \"location\": \"杭州市\"}, {\"time\": \"2025-04-14 22:16:26.000000\", \"content\": \"包裹已送达，请注意查收\", \"location\": \"上海市上海市静安区胜利达到157号\"}, {\"time\": \"2025-04-14 22:16:26.000000\", \"content\": \"包裹已到达目的地\", \"location\": \"深圳市\"}, {\"time\": \"2025-04-14 22:16:26.000000\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"上海市上海市静安区胜利达到157号\"}, {\"time\": \"2025-04-14 22:16:26.000000\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"武汉市\"}]', '杭州市', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (8, 0, '1744640146843598', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-14 22:15:47.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}]', NULL, '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (9, 0, '1744695909430896', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-15 13:45:09.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}, {\"time\": \"2025-04-16 20:04:38.000000\", \"content\": \"包裹正在派送\", \"location\": \"深圳市\"}, {\"time\": \"2025-04-16 20:04:40.000000\", \"content\": \"包裹已到达分拣中心\", \"location\": \"广州市\"}, {\"time\": \"2025-04-16 20:04:41.000000\", \"content\": \"包裹已送达，请注意查收\", \"location\": \"浙江省杭州市西湖区三墩路1555号\"}, {\"time\": \"2025-04-16 20:04:41.000000\", \"content\": \"包裹已到达目的地\", \"location\": \"深圳市\"}, {\"time\": \"2025-04-16 20:04:43.000000\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"浙江省杭州市西湖区三墩路1555号\"}, {\"time\": \"2025-04-16 20:04:43.000000\", \"content\": \"包裹已签收，感谢您的使用\", \"location\": \"广州市\"}]', '浙江省杭州市西湖区三墩路1555号', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (10, 0, '1744808398992627', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-16 20:59:59.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}]', NULL, '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (11, 0, '1744945043856166', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-18 10:57:24.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}]', NULL, '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (12, 0, '1744983207447404', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-18 21:33:27.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}]', NULL, '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (13, 0, '1745047920466690', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-19 15:32:01.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}]', NULL, '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (14, 0, '1745048757245512', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-19 15:45:57.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}]', NULL, '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (15, 0, '1745048847024006', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-19 15:47:27.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}]', NULL, '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (16, 0, '1745050197505754', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-19 16:09:58.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}]', NULL, '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (17, 0, '1745050524832409', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-19 16:15:25.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": null}]', NULL, '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (18, 0, '1745053111251509', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-19 16:58:31.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": \"未知\"}]', '未知', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (19, 0, '1745054900921268', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-19 17:28:21.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": \"未知\"}]', '未知', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (20, 0, '1745141821611516', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-20 17:37:02.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": \"未知\"}]', '未知', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (21, 0, '1745146322530764', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-20 18:52:03.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": \"未知\"}]', '未知', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (22, 0, '1745388634950233', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-23 14:10:35.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": \"未知\"}]', '未知', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (23, 0, '1745479030017388', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-24 15:17:10.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": \"未知\"}, {\"time\": \"2025-04-24 16:19:36.000000\", \"content\": \"【广州市】已揽收\", \"location\": \"广州市\"}, {\"time\": \"2025-04-24 16:20:43.000000\", \"content\": \"【深圳市】派送中\", \"location\": \"深圳市\"}, {\"time\": \"2025-04-24 16:20:45.000000\", \"content\": \"【北京市】已发出，下一站\", \"location\": \"北京市\"}, {\"time\": \"2025-04-24 16:20:46.000000\", \"content\": \"【上海市】已发出，下一站\", \"location\": \"上海市\"}, {\"time\": \"2025-04-24 16:20:47.000000\", \"content\": \"【广州市】运输中\", \"location\": \"广州市\"}, {\"time\": \"2025-04-24 16:20:47.000000\", \"content\": \"【上海市】派送中\", \"location\": \"上海市\"}, {\"time\": \"2025-04-24 16:20:48.000000\", \"content\": \"【江苏】您的快件已签收，签收人：张三，感谢您使用中通快递\", \"location\": \"江苏\"}]', '江苏市', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (24, 0, '1745484159139862', '2025-04-24 17:03:59', '[{\"time\": \"2025-04-24 16:42:39.000000\", \"content\": \"包裹已发货，等待揽收\", \"location\": \"未知\"}, {\"time\": \"2025-04-24 16:42:45.000000\", \"content\": \"【上海市】已发出，下一站\", \"location\": \"上海市\"}, {\"time\": \"2025-04-24 16:42:48.000000\", \"content\": \"【杭州市】已到达\", \"location\": \"杭州市\"}, {\"time\": \"2025-04-24 16:42:51.000000\", \"content\": \"【北京市】运输中\", \"location\": \"北京市\"}]', '北京市', '2025-04-24 17:03:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (25, 0, '1745486492168386', '2025-04-24 19:37:33', '[{\"time\": \"2023-01-01T00:00:00\", \"content\": \"物流信息已更新\", \"location\": \"系统自动生成\"}]', '江苏', '2025-04-24 19:37:33', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (1915375148625252354, 0, '1745495615421985', '2025-04-24 20:01:14', '[{\"time\": \"2025-04-24T20:01:13.723937400\", \"content\": \"【江苏】您的快件已签收，签收人：张三，感谢您使用邮政EMS\", \"location\": \"江苏\"}, {\"time\": \"2025-04-24T18:01:13.693688900\", \"content\": \"配送员正在派送途中\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T16:01:13.693688900\", \"content\": \"快件已分配给配送员\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T10:01:13.693688900\", \"content\": \"快件已到达目的地仓库\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T04:01:13.693688900\", \"content\": \"快件已发往目的地\", \"location\": \"中转站\"}, {\"time\": \"2025-04-23T20:01:13.693688900\", \"content\": \"快件已到达中转站\", \"location\": \"中转站\"}, {\"time\": \"2025-04-23T02:01:13.693688900\", \"content\": \"快件已发往中转站\", \"location\": \"发货地\"}, {\"time\": \"2025-04-22T20:01:13.693688900\", \"content\": \"快件在发货地仓库完成分拣\", \"location\": \"发货地\"}, {\"time\": \"2025-04-21T20:01:13.693688900\", \"content\": \"快件已被揽收\", \"location\": \"发货地\"}, {\"time\": \"2025-04-24T20:01:12.691053700\", \"content\": \"【杭州市】派送中\", \"location\": \"杭州市\"}, {\"time\": \"2025-04-24T18:01:12.674698400\", \"content\": \"配送员正在派送途中\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T16:01:12.674698400\", \"content\": \"快件已分配给配送员\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T10:01:12.674698400\", \"content\": \"快件已到达目的地仓库\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T04:01:12.674698400\", \"content\": \"快件已发往目的地\", \"location\": \"中转站\"}, {\"time\": \"2025-04-23T20:01:12.674698400\", \"content\": \"快件已到达中转站\", \"location\": \"中转站\"}, {\"time\": \"2025-04-23T02:01:12.674698400\", \"content\": \"快件已发往中转站\", \"location\": \"发货地\"}, {\"time\": \"2025-04-22T20:01:12.674698400\", \"content\": \"快件在发货地仓库完成分拣\", \"location\": \"发货地\"}, {\"time\": \"2025-04-21T20:01:12.674698400\", \"content\": \"快件已被揽收\", \"location\": \"发货地\"}, {\"time\": \"2025-04-24T20:01:11.791662\", \"content\": \"【上海市】运输中\", \"location\": \"上海市\"}, {\"time\": \"2025-04-24T18:01:11.193194300\", \"content\": \"配送员正在派送途中\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T16:01:11.193194300\", \"content\": \"快件已分配给配送员\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T10:01:11.193194300\", \"content\": \"快件已到达目的地仓库\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T04:01:11.193194300\", \"content\": \"快件已发往目的地\", \"location\": \"中转站\"}, {\"time\": \"2025-04-23T20:01:11.193194300\", \"content\": \"快件已到达中转站\", \"location\": \"中转站\"}, {\"time\": \"2025-04-23T02:01:11.193194300\", \"content\": \"快件已发往中转站\", \"location\": \"发货地\"}, {\"time\": \"2025-04-22T20:01:11.193194300\", \"content\": \"快件在发货地仓库完成分拣\", \"location\": \"发货地\"}, {\"time\": \"2025-04-21T20:01:11.193194300\", \"content\": \"快件已被揽收\", \"location\": \"发货地\"}, {\"time\": \"2025-04-24T20:00:12.763086300\", \"content\": \"【杭州市】已到达\", \"location\": \"杭州市\"}, {\"time\": \"2025-04-24T18:00:12.742352300\", \"content\": \"配送员正在派送途中\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T16:00:12.742352300\", \"content\": \"快件已分配给配送员\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T10:00:12.742352300\", \"content\": \"快件已到达目的地仓库\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T04:00:12.742352300\", \"content\": \"快件已发往目的地\", \"location\": \"中转站\"}, {\"time\": \"2025-04-23T20:00:12.742352300\", \"content\": \"快件已到达中转站\", \"location\": \"中转站\"}, {\"time\": \"2025-04-23T02:00:12.742352300\", \"content\": \"快件已发往中转站\", \"location\": \"发货地\"}, {\"time\": \"2025-04-22T20:00:12.742352300\", \"content\": \"快件在发货地仓库完成分拣\", \"location\": \"发货地\"}, {\"time\": \"2025-04-21T20:00:12.742352300\", \"content\": \"快件已被揽收\", \"location\": \"发货地\"}, {\"time\": \"2025-04-24T19:59:56.640361400\", \"content\": \"【北京市】派送中\", \"location\": \"北京市\"}, {\"time\": \"2025-04-21T19:59:56.032991800\", \"content\": \"快件已被揽收\", \"location\": \"发货地\"}, {\"time\": \"2025-04-22T19:59:56.032991800\", \"content\": \"快件在发货地仓库完成分拣\", \"location\": \"发货地\"}, {\"time\": \"2025-04-23T01:59:56.032991800\", \"content\": \"快件已发往中转站\", \"location\": \"发货地\"}, {\"time\": \"2025-04-23T19:59:56.032991800\", \"content\": \"快件已到达中转站\", \"location\": \"中转站\"}, {\"time\": \"2025-04-24T03:59:56.032991800\", \"content\": \"快件已发往目的地\", \"location\": \"中转站\"}, {\"time\": \"2025-04-24T09:59:56.032991800\", \"content\": \"快件已到达目的地仓库\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T15:59:56.032991800\", \"content\": \"快件已分配给配送员\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T17:59:56.032991800\", \"content\": \"配送员正在派送途中\", \"location\": \"收货地\"}]', '江苏', '2025-04-24 20:01:14', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (1915377286898208770, 0, '1745494951873789', '2025-04-24 20:14:59', '[{\"time\": \"2025-04-24T20:14:59.326277100\", \"content\": \"【江苏】您的快件已签收，签收人：张三，感谢您使用申通快递\", \"location\": \"江苏\"}, {\"time\": \"2025-04-24T20:14:49.780339200\", \"content\": \"【江苏】您的快件已签收，签收人：张三，感谢您使用申通快递\", \"location\": \"江苏\"}, {\"time\": \"2025-04-24T20:14:47.771282500\", \"content\": \"【江苏】您的快件已签收，签收人：张三，感谢您使用申通快递\", \"location\": \"江苏\"}, {\"time\": \"2025-04-24T20:14:46.830522900\", \"content\": \"【江苏】您的快件已签收，签收人：张三，感谢您使用申通快递\", \"location\": \"江苏\"}, {\"time\": \"2025-04-24T20:14:46.108213100\", \"content\": \"【江苏】您的快件已签收，签收人：张三，感谢您使用申通快递\", \"location\": \"江苏\"}, {\"time\": \"2025-04-24T20:14:44.743746\", \"content\": \"【江苏】您的快件已签收，签收人：张三，感谢您使用申通快递\", \"location\": \"江苏\"}, {\"time\": \"2025-04-24T20:14:44.270919100\", \"content\": \"【上海市】已揽收\", \"location\": \"上海市\"}, {\"time\": \"2025-04-24T20:14:43.623804300\", \"content\": \"【杭州市】已发出，下一站\", \"location\": \"杭州市\"}, {\"time\": \"2025-04-24T20:14:42.424965900\", \"content\": \"【深圳市】已到达\", \"location\": \"深圳市\"}, {\"time\": \"2025-04-24T20:14:37.345349100\", \"content\": \"【上海市】已发出，下一站\", \"location\": \"上海市\"}, {\"time\": \"2025-04-24T20:08:26.445146\", \"content\": \"【杭州市】已发出，下一站\", \"location\": \"杭州市\"}, {\"time\": \"2025-04-21T20:08:25.851243600\", \"content\": \"快件已被揽收\", \"location\": \"发货地\"}, {\"time\": \"2025-04-22T20:08:25.851243600\", \"content\": \"快件在发货地仓库完成分拣\", \"location\": \"发货地\"}, {\"time\": \"2025-04-23T02:08:25.851243600\", \"content\": \"快件已发往中转站\", \"location\": \"发货地\"}, {\"time\": \"2025-04-23T20:08:25.851243600\", \"content\": \"快件已到达中转站\", \"location\": \"中转站\"}, {\"time\": \"2025-04-24T04:08:25.851243600\", \"content\": \"快件已发往目的地\", \"location\": \"中转站\"}, {\"time\": \"2025-04-24T10:08:25.851243600\", \"content\": \"快件已到达目的地仓库\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T16:08:25.851243600\", \"content\": \"快件已分配给配送员\", \"location\": \"收货地\"}, {\"time\": \"2025-04-24T18:08:25.851243600\", \"content\": \"配送员正在派送途中\", \"location\": \"收货地\"}]', '江苏', '2025-04-24 20:14:59', '2025-04-26 15:27:03');
INSERT INTO `logistics_trace` VALUES (1915573932244312066, 0, '1745543389690786', '2025-04-25 09:09:50', '[{\"time\": \"2025-04-25T09:09:49.749003100\", \"content\": \"包裹已发货，等待揽收\", \"location\": \"未知\"}]', '未知', '2025-04-25 09:09:50', '2025-04-26 15:27:03');

-- ----------------------------
-- Table structure for logistics_track
-- ----------------------------
DROP TABLE IF EXISTS `logistics_track`;
CREATE TABLE `logistics_track`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '轨迹ID',
  `logistics_id` bigint UNSIGNED NOT NULL COMMENT '物流ID',
  `tracking_time` datetime NOT NULL COMMENT '轨迹时间',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前位置',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '当前状态',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '轨迹内容',
  `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `details_json` json NULL COMMENT '轨迹详情JSON数据',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_logistics_id`(`logistics_id` ASC) USING BTREE,
  INDEX `idx_tracking_time`(`tracking_time` ASC) USING BTREE,
  CONSTRAINT `fk_track_logistics` FOREIGN KEY (`logistics_id`) REFERENCES `logistics` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 73 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物流轨迹表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of logistics_track
-- ----------------------------
INSERT INTO `logistics_track` VALUES (1, 2, '2025-05-09 13:22:33', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-09 13:22:33', NULL);
INSERT INTO `logistics_track` VALUES (2, 2, '2025-05-09 13:38:04', NULL, 'SHIPPING', '包裹已发出，运输中，备注：自动更新为运输中', '系统更新', '2025-05-09 13:38:04', NULL);
INSERT INTO `logistics_track` VALUES (3, 2, '2025-05-09 13:38:04', '揽收', 'SHIPPING', '快递已揽收, 广州市白云区分拣中心', '系统自动', '2025-05-09 13:38:04', NULL);
INSERT INTO `logistics_track` VALUES (4, 2, '2025-05-09 13:38:09', '运输', 'SHIPPING', '已到达广州市中心分拣中心', '系统自动', '2025-05-09 13:38:09', NULL);
INSERT INTO `logistics_track` VALUES (5, 2, '2025-05-09 13:38:14', '离开', 'SHIPPING', '离开广州市中心分拣中心，发往杭州市', '系统自动', '2025-05-09 13:38:14', NULL);
INSERT INTO `logistics_track` VALUES (6, 2, '2025-05-09 13:38:19', '到达', 'SHIPPING', '已到达杭州市分拣中心', '系统自动', '2025-05-09 13:38:19', NULL);
INSERT INTO `logistics_track` VALUES (7, 2, '2025-05-09 13:38:24', '派送', 'DELIVERED', '快递员已揽收，正在派送中', '系统自动', '2025-05-09 13:38:24', NULL);
INSERT INTO `logistics_track` VALUES (8, 2, '2025-05-09 13:38:24', NULL, 'DELIVERED', '包裹已送达，备注：自动更新为已送达', '系统更新', '2025-05-09 13:38:24', NULL);
INSERT INTO `logistics_track` VALUES (9, 3, '2025-05-09 20:43:16', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-09 20:43:16', NULL);
INSERT INTO `logistics_track` VALUES (10, 4, '2025-05-09 20:44:13', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-09 20:44:13', NULL);
INSERT INTO `logistics_track` VALUES (11, 4, '2025-05-09 20:44:31', NULL, 'SHIPPING', '包裹已发出，运输中，备注：自动更新为运输中', '系统更新', '2025-05-09 20:44:31', NULL);
INSERT INTO `logistics_track` VALUES (12, 4, '2025-05-09 20:44:31', '揽收', 'SHIPPING', '快递已揽收, 广州市白云区分拣中心', '系统自动', '2025-05-09 20:44:31', NULL);
INSERT INTO `logistics_track` VALUES (13, 4, '2025-05-09 20:44:36', '运输', 'SHIPPING', '已到达广州市中心分拣中心', '系统自动', '2025-05-09 20:44:36', NULL);
INSERT INTO `logistics_track` VALUES (14, 4, '2025-05-09 20:44:41', '离开', 'SHIPPING', '离开广州市中心分拣中心，发往杭州市', '系统自动', '2025-05-09 20:44:41', NULL);
INSERT INTO `logistics_track` VALUES (15, 4, '2025-05-09 20:44:46', '到达', 'SHIPPING', '已到达杭州市分拣中心', '系统自动', '2025-05-09 20:44:46', NULL);
INSERT INTO `logistics_track` VALUES (16, 4, '2025-05-09 20:44:51', '派送', 'DELIVERED', '快递员已揽收，正在派送中', '系统自动', '2025-05-09 20:44:51', NULL);
INSERT INTO `logistics_track` VALUES (17, 4, '2025-05-09 20:44:51', NULL, 'DELIVERED', '包裹已送达，备注：自动更新为已送达', '系统更新', '2025-05-09 20:44:51', NULL);
INSERT INTO `logistics_track` VALUES (18, 5, '2025-05-09 21:20:43', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-09 21:20:43', NULL);
INSERT INTO `logistics_track` VALUES (19, 5, '2025-05-09 21:20:53', NULL, 'SHIPPING', '包裹已发出，运输中，备注：自动更新为运输中', '系统更新', '2025-05-09 21:20:53', NULL);
INSERT INTO `logistics_track` VALUES (20, 5, '2025-05-09 21:20:53', '揽收', 'SHIPPING', '快递已揽收, 广州市白云区分拣中心', '系统自动', '2025-05-09 21:20:53', NULL);
INSERT INTO `logistics_track` VALUES (21, 5, '2025-05-09 21:20:59', '运输', 'SHIPPING', '已到达广州市中心分拣中心', '系统自动', '2025-05-09 21:20:59', NULL);
INSERT INTO `logistics_track` VALUES (22, 5, '2025-05-09 21:21:04', '离开', 'SHIPPING', '离开广州市中心分拣中心，发往杭州市', '系统自动', '2025-05-09 21:21:04', NULL);
INSERT INTO `logistics_track` VALUES (23, 5, '2025-05-09 21:21:09', '到达', 'SHIPPING', '已到达杭州市分拣中心', '系统自动', '2025-05-09 21:21:09', NULL);
INSERT INTO `logistics_track` VALUES (24, 5, '2025-05-09 21:21:14', '派送', 'DELIVERED', '快递员已揽收，正在派送中', '系统自动', '2025-05-09 21:21:14', NULL);
INSERT INTO `logistics_track` VALUES (25, 5, '2025-05-09 21:21:14', NULL, 'DELIVERED', '包裹已送达，备注：自动更新为已送达', '系统更新', '2025-05-09 21:21:14', NULL);
INSERT INTO `logistics_track` VALUES (26, 6, '2025-05-09 22:08:25', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-09 22:08:25', NULL);
INSERT INTO `logistics_track` VALUES (27, 6, '2025-05-09 22:08:30', NULL, 'SHIPPING', '包裹已发出，运输中，备注：自动更新为运输中', '系统更新', '2025-05-09 22:08:30', NULL);
INSERT INTO `logistics_track` VALUES (28, 6, '2025-05-09 22:08:30', '揽收', 'SHIPPING', '快递已揽收, 广州市白云区分拣中心', '系统自动', '2025-05-09 22:08:30', NULL);
INSERT INTO `logistics_track` VALUES (29, 6, '2025-05-09 22:08:35', '运输', 'SHIPPING', '已到达广州市中心分拣中心', '系统自动', '2025-05-09 22:08:35', NULL);
INSERT INTO `logistics_track` VALUES (30, 6, '2025-05-09 22:08:40', '离开', 'SHIPPING', '离开广州市中心分拣中心，发往杭州市', '系统自动', '2025-05-09 22:08:40', NULL);
INSERT INTO `logistics_track` VALUES (31, 6, '2025-05-09 22:08:45', '到达', 'SHIPPING', '已到达杭州市分拣中心', '系统自动', '2025-05-09 22:08:45', NULL);
INSERT INTO `logistics_track` VALUES (32, 6, '2025-05-09 22:08:50', '派送', 'DELIVERED', '快递员已揽收，正在派送中', '系统自动', '2025-05-09 22:08:50', NULL);
INSERT INTO `logistics_track` VALUES (33, 6, '2025-05-09 22:08:50', NULL, 'DELIVERED', '包裹已送达，备注：自动更新为已送达', '系统更新', '2025-05-09 22:08:50', NULL);
INSERT INTO `logistics_track` VALUES (34, 7, '2025-05-09 22:45:14', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-09 22:45:14', NULL);
INSERT INTO `logistics_track` VALUES (35, 7, '2025-05-09 22:45:18', NULL, 'SHIPPING', '包裹已发出，运输中，备注：自动更新为运输中', '系统更新', '2025-05-09 22:45:18', NULL);
INSERT INTO `logistics_track` VALUES (36, 7, '2025-05-09 22:45:18', '揽收', 'SHIPPING', '快递已揽收, 广州市白云区分拣中心', '系统自动', '2025-05-09 22:45:18', NULL);
INSERT INTO `logistics_track` VALUES (37, 7, '2025-05-09 22:45:24', '运输', 'SHIPPING', '已到达广州市中心分拣中心', '系统自动', '2025-05-09 22:45:24', NULL);
INSERT INTO `logistics_track` VALUES (38, 7, '2025-05-09 22:45:29', '离开', 'SHIPPING', '离开广州市中心分拣中心，发往杭州市', '系统自动', '2025-05-09 22:45:29', NULL);
INSERT INTO `logistics_track` VALUES (39, 7, '2025-05-09 22:45:34', '到达', 'SHIPPING', '已到达杭州市分拣中心', '系统自动', '2025-05-09 22:45:34', NULL);
INSERT INTO `logistics_track` VALUES (40, 7, '2025-05-09 22:45:39', '派送', 'DELIVERED', '快递员已揽收，正在派送中', '系统自动', '2025-05-09 22:45:39', NULL);
INSERT INTO `logistics_track` VALUES (41, 7, '2025-05-09 22:45:39', NULL, 'DELIVERED', '包裹已送达，备注：自动更新为已送达', '系统更新', '2025-05-09 22:45:39', NULL);
INSERT INTO `logistics_track` VALUES (42, 8, '2025-05-10 22:55:36', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-10 22:55:36', NULL);
INSERT INTO `logistics_track` VALUES (43, 9, '2025-05-10 23:01:10', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-10 23:01:10', NULL);
INSERT INTO `logistics_track` VALUES (44, 10, '2025-05-10 23:06:06', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-10 23:06:06', NULL);
INSERT INTO `logistics_track` VALUES (45, 11, '2025-05-20 22:54:01', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-20 22:54:01', NULL);
INSERT INTO `logistics_track` VALUES (46, 12, '2025-05-28 22:16:56', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-28 22:16:56', '{\"type\": \"initial\", \"logisticsInfo\": {\"companyId\": 3, \"trackingNo\": \"YT2505282216561622\"}, \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (47, 13, '2025-05-29 23:05:01', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-05-29 23:05:01', '{\"type\": \"initial\", \"logisticsInfo\": {\"companyId\": 4, \"trackingNo\": \"YD2505292305010709\"}, \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (48, 13, '2025-05-29 23:08:33', NULL, 'SHIPPING', '包裹已发出，运输中', '系统更新', '2025-05-29 23:08:33', '{\"type\": \"statusChange\", \"previousStatus\": \"SHIPPING\", \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (49, 13, '2025-05-29 23:08:56', NULL, 'SHIPPING', '包裹已发出，运输中', '系统更新', '2025-05-29 23:08:56', '{\"type\": \"statusChange\", \"previousStatus\": \"SHIPPING\", \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (50, 14, '2025-06-06 09:39:56', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-06-06 09:39:56', '{\"type\": \"initial\", \"logisticsInfo\": {\"companyId\": 3, \"trackingNo\": \"YT2506060939567253\"}, \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (51, 14, '2025-06-06 09:45:48', NULL, 'DELIVERED', '包裹已送达', '系统更新', '2025-06-06 09:45:48', '{\"type\": \"statusChange\", \"previousStatus\": \"DELIVERED\", \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (52, 15, '2025-06-06 13:32:09', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-06-06 13:32:09', '{\"type\": \"initial\", \"logisticsInfo\": {\"companyId\": 5, \"trackingNo\": \"EMS2506061332085716\"}, \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (53, 15, '2025-06-06 13:33:26', NULL, 'SHIPPING', '包裹已发出，运输中', '系统更新', '2025-06-06 13:33:26', '{\"type\": \"statusChange\", \"previousStatus\": \"SHIPPING\", \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (54, 16, '2025-06-06 13:36:58', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-06-06 13:36:58', '{\"type\": \"initial\", \"logisticsInfo\": {\"companyId\": 7, \"trackingNo\": \"BEST2506061336586498\"}, \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (55, 17, '2025-07-09 20:32:49', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-07-09 20:32:49', '{\"type\": \"initial\", \"logisticsInfo\": {\"companyId\": 3, \"trackingNo\": \"YT2507092032483667\"}, \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (56, 17, '2025-07-10 14:10:01', NULL, 'SHIPPING', '包裹已发出，运输中', '系统更新', '2025-07-10 14:10:01', '{\"type\": \"statusChange\", \"previousStatus\": \"SHIPPING\", \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (57, 17, '2025-07-10 14:23:31', NULL, 'DELIVERED', '包裹已送达', '系统更新', '2025-07-10 14:23:31', '{\"type\": \"statusChange\", \"previousStatus\": \"DELIVERED\", \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (58, 18, '2025-07-10 14:26:02', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-07-10 14:26:02', '{\"type\": \"initial\", \"logisticsInfo\": {\"companyId\": 1, \"trackingNo\": \"SF2507106320\"}, \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (59, 19, '2025-07-10 14:49:41', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-07-10 14:49:41', '{\"type\": \"initial\", \"logisticsInfo\": {\"companyId\": 5, \"trackingNo\": \"EMS2507101449400205\"}, \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (60, 19, '2025-07-10 15:32:56', '发货仓库', 'SHIPPING', '快件已从仓库发出，正在运往分拣中心', 'System', '2025-07-10 15:32:56', '{\"type\": \"autoGenerated\", \"isLast\": false, \"stepIndex\": 0, \"batchIndex\": 0, \"generatedAt\": \"2025-07-10T07:32:56.284Z\", \"templateName\": \"step_1\", \"additionalInfo\": {\"totalSteps\": 5, \"batchGenerated\": true}, \"batchGenerated\": true, \"batchTimestamp\": \"2025-07-10T15:32:56.332173300\"}');
INSERT INTO `logistics_track` VALUES (61, 19, '2025-07-10 16:02:56', '分拣中心', 'SHIPPING', '快件已到达分拣中心，正在分拣', 'System', '2025-07-10 15:32:56', '{\"type\": \"autoGenerated\", \"isLast\": false, \"stepIndex\": 1, \"batchIndex\": 1, \"generatedAt\": \"2025-07-10T07:32:56.284Z\", \"templateName\": \"step_2\", \"additionalInfo\": {\"totalSteps\": 5, \"batchGenerated\": true}, \"batchGenerated\": true, \"batchTimestamp\": \"2025-07-10T15:32:56.332173300\"}');
INSERT INTO `logistics_track` VALUES (62, 19, '2025-07-10 16:32:56', '分拣中心', 'SHIPPING', '快件已从分拣中心发出，正在运往配送站', 'System', '2025-07-10 15:32:56', '{\"type\": \"autoGenerated\", \"isLast\": false, \"stepIndex\": 2, \"batchIndex\": 2, \"generatedAt\": \"2025-07-10T07:32:56.284Z\", \"templateName\": \"step_3\", \"additionalInfo\": {\"totalSteps\": 5, \"batchGenerated\": true}, \"batchGenerated\": true, \"batchTimestamp\": \"2025-07-10T15:32:56.332173300\"}');
INSERT INTO `logistics_track` VALUES (63, 19, '2025-07-10 17:02:56', '配送站', 'SHIPPING', '快件已到达配送站，等待配送', 'System', '2025-07-10 15:32:56', '{\"type\": \"autoGenerated\", \"isLast\": false, \"stepIndex\": 3, \"batchIndex\": 3, \"generatedAt\": \"2025-07-10T07:32:56.284Z\", \"templateName\": \"step_4\", \"additionalInfo\": {\"totalSteps\": 5, \"batchGenerated\": true}, \"batchGenerated\": true, \"batchTimestamp\": \"2025-07-10T15:32:56.332173300\"}');
INSERT INTO `logistics_track` VALUES (64, 19, '2025-07-10 17:32:56', '收货地址', 'DELIVERED', '快件已被签收，感谢您使用我们的服务', 'System', '2025-07-10 15:32:56', '{\"type\": \"autoGenerated\", \"isLast\": true, \"stepIndex\": 4, \"batchIndex\": 4, \"generatedAt\": \"2025-07-10T07:32:56.284Z\", \"deliveryInfo\": {\"isDelivered\": true, \"deliveryMethod\": \"standard\", \"requiresSignature\": true}, \"templateName\": \"step_5\", \"additionalInfo\": {\"totalSteps\": 5, \"batchGenerated\": true}, \"batchGenerated\": true, \"batchTimestamp\": \"2025-07-10T15:32:56.332173300\"}');
INSERT INTO `logistics_track` VALUES (65, 19, '2025-07-10 15:32:56', NULL, 'DELIVERED', '包裹已送达，备注：系统自动更新：物流已完成送达', '系统更新', '2025-07-10 15:32:56', '{\"type\": \"statusChange\", \"remark\": \"系统自动更新：物流已完成送达\", \"previousStatus\": \"DELIVERED\", \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (66, 23, '2025-07-11 13:16:11', NULL, 'CREATED', '物流信息已创建，等待揽收', '系统', '2025-07-11 13:16:11', '{\"type\": \"initial\", \"logisticsInfo\": {\"companyId\": 2, \"trackingNo\": \"ZT2507111316109731\"}, \"systemGenerated\": true}');
INSERT INTO `logistics_track` VALUES (67, 23, '2025-07-11 13:16:27', '发货仓库', 'SHIPPING', '快件已从仓库发出，正在运往分拣中心', 'System', '2025-07-11 13:16:27', '{\"type\": \"autoGenerated\", \"isLast\": false, \"stepIndex\": 0, \"batchIndex\": 0, \"generatedAt\": \"2025-07-11T05:16:27.179Z\", \"templateName\": \"step_1\", \"additionalInfo\": {\"totalSteps\": 5, \"batchGenerated\": true}, \"batchGenerated\": true, \"batchTimestamp\": \"2025-07-11T13:16:27.208374300\"}');
INSERT INTO `logistics_track` VALUES (68, 23, '2025-07-11 13:46:27', '分拣中心', 'SHIPPING', '快件已到达分拣中心，正在分拣', 'System', '2025-07-11 13:16:27', '{\"type\": \"autoGenerated\", \"isLast\": false, \"stepIndex\": 1, \"batchIndex\": 1, \"generatedAt\": \"2025-07-11T05:16:27.179Z\", \"templateName\": \"step_2\", \"additionalInfo\": {\"totalSteps\": 5, \"batchGenerated\": true}, \"batchGenerated\": true, \"batchTimestamp\": \"2025-07-11T13:16:27.208374300\"}');
INSERT INTO `logistics_track` VALUES (69, 23, '2025-07-11 14:16:27', '分拣中心', 'SHIPPING', '快件已从分拣中心发出，正在运往配送站', 'System', '2025-07-11 13:16:27', '{\"type\": \"autoGenerated\", \"isLast\": false, \"stepIndex\": 2, \"batchIndex\": 2, \"generatedAt\": \"2025-07-11T05:16:27.179Z\", \"templateName\": \"step_3\", \"additionalInfo\": {\"totalSteps\": 5, \"batchGenerated\": true}, \"batchGenerated\": true, \"batchTimestamp\": \"2025-07-11T13:16:27.208374300\"}');
INSERT INTO `logistics_track` VALUES (70, 23, '2025-07-11 14:46:27', '配送站', 'SHIPPING', '快件已到达配送站，等待配送', 'System', '2025-07-11 13:16:27', '{\"type\": \"autoGenerated\", \"isLast\": false, \"stepIndex\": 3, \"batchIndex\": 3, \"generatedAt\": \"2025-07-11T05:16:27.179Z\", \"templateName\": \"step_4\", \"additionalInfo\": {\"totalSteps\": 5, \"batchGenerated\": true}, \"batchGenerated\": true, \"batchTimestamp\": \"2025-07-11T13:16:27.208374300\"}');
INSERT INTO `logistics_track` VALUES (71, 23, '2025-07-11 15:16:27', '收货地址', 'DELIVERED', '快件已被签收，感谢您使用我们的服务', 'System', '2025-07-11 13:16:27', '{\"type\": \"autoGenerated\", \"isLast\": true, \"stepIndex\": 4, \"batchIndex\": 4, \"generatedAt\": \"2025-07-11T05:16:27.179Z\", \"deliveryInfo\": {\"isDelivered\": true, \"deliveryMethod\": \"standard\", \"requiresSignature\": true}, \"templateName\": \"step_5\", \"additionalInfo\": {\"totalSteps\": 5, \"batchGenerated\": true}, \"batchGenerated\": true, \"batchTimestamp\": \"2025-07-11T13:16:27.208374300\"}');
INSERT INTO `logistics_track` VALUES (72, 23, '2025-07-11 13:16:27', NULL, 'DELIVERED', '包裹已送达，备注：系统自动更新：物流已完成送达', '系统更新', '2025-07-11 13:16:27', '{\"type\": \"statusChange\", \"remark\": \"系统自动更新：物流已完成送达\", \"previousStatus\": \"DELIVERED\", \"systemGenerated\": true}');

-- ----------------------------
-- Table structure for member_level
-- ----------------------------
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '等级ID',
  `level_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '等级名称',
  `min_points` int NOT NULL COMMENT '最低积分要求',
  `discount` decimal(5, 2) NULL DEFAULT 1.00 COMMENT '折扣率（0.1-1之间）',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '等级图标',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '等级描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_level_name`(`level_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会员等级表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of member_level
-- ----------------------------
INSERT INTO `member_level` VALUES (1, '普通会员', 0, 1.00, NULL, '注册即可享受，无折扣优惠', '2025-03-19 13:38:12', '2025-03-19 13:38:12');
INSERT INTO `member_level` VALUES (2, '银牌会员', 3000, 0.98, NULL, '享受全场商品98折优惠', '2025-03-19 13:38:12', '2025-03-19 13:38:12');
INSERT INTO `member_level` VALUES (3, '金牌会员', 10000, 0.95, NULL, '享受全场商品95折优惠', '2025-03-19 13:38:12', '2025-03-19 13:38:12');
INSERT INTO `member_level` VALUES (4, '钻石会员', 30000, 0.92, NULL, '享受全场商品92折优惠', '2025-03-19 13:38:12', '2025-03-19 13:38:12');
INSERT INTO `member_level` VALUES (5, '至尊会员', 100000, 0.90, NULL, '享受全场商品9折优惠，专享客户服务', '2025-03-19 13:38:12', '2025-03-19 13:38:12');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `order_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `actual_amount` decimal(10, 2) NOT NULL COMMENT '实付金额',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending_payment' COMMENT '订单状态：pending_payment-待付款，pending_shipment-待发货，shipped-已发货，completed-已完成，cancelled-已取消',
  `payment_id` int UNSIGNED NULL DEFAULT NULL COMMENT '支付ID，关联payment表',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式：alipay-支付宝，wechat-微信，wallet-钱包',
  `shipping_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配送方式',
  `shipping_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '运费',
  `address_id` int UNSIGNED NOT NULL COMMENT '收货地址ID',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人电话',
  `receiver_province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `receiver_city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `receiver_district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区/县',
  `receiver_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `receiver_zip` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮编',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单备注',
  `discount_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '优惠金额',
  `coupon_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '使用的优惠券ID',
  `coupon_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '优惠券金额',
  `coupon_discount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠券抵扣金额',
  `points_used` int NULL DEFAULT 0 COMMENT '使用的积分',
  `points_discount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '积分抵扣金额',
  `paid_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `shipping_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `tracking_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流单号',
  `shipping_company` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司',
  `completion_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `is_commented` tinyint(1) NULL DEFAULT 0 COMMENT '是否已评价：0-未评价，1-已评价',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁控制',
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_payment_id`(`payment_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_status_create_time`(`user_id` ASC, `status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_version`(`version` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 268 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (1, 'ORDER17428992193116059', 8, 99.90, 99.90, '待发货', 60, NULL, 'alipay', 'standard', 0.00, 5, '收货人2', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-25 18:40:19', '2025-03-25 20:23:15', 1);
INSERT INTO `order` VALUES (2, 'ORDER17429055908353061', 8, 259.80, 259.80, '待发货', 61, NULL, 'alipay', 'standard', 0.00, 4, '收货人3', '13444588765', '广东省', '深圳市', '罗湖区', '三墩157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-25 20:26:31', '2025-03-25 20:26:36', 1);
INSERT INTO `order` VALUES (3, 'ORDER17429059946469017', 8, 29.90, 29.90, '待发货', 62, NULL, 'alipay', 'standard', 0.00, 8, '收货人1', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-25 20:33:15', '2025-03-25 20:33:19', 1);
INSERT INTO `order` VALUES (4, 'ORDER17429062182848188', 8, 99.90, 99.90, '待发货', 63, NULL, 'alipay', 'standard', 0.00, 8, '收货人1', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-25 20:36:58', '2025-03-25 20:37:03', 1);
INSERT INTO `order` VALUES (5, 'ORDER17429063563726994', 8, 358.00, 358.00, '待发货', 64, NULL, 'alipay', 'standard', 0.00, 5, '收货人2', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-25 20:39:16', '2025-03-25 20:39:20', 1);
INSERT INTO `order` VALUES (6, 'ORDER17429065185254784', 8, 69.90, 69.90, '待发货', 65, NULL, 'alipay', 'standard', 0.00, 5, '收货人2', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-25 20:41:59', '2025-03-25 20:42:00', 1);
INSERT INTO `order` VALUES (7, 'ORDER17429067366344955', 8, 179.80, 179.80, '待发货', 66, NULL, 'alipay', 'standard', 0.00, 5, '收货人2', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-25 20:45:37', '2025-03-25 20:45:38', 1);
INSERT INTO `order` VALUES (8, 'ORDER17429069358481384', 8, 99.90, 99.90, '待发货', 67, NULL, 'alipay', 'standard', 0.00, 5, '收货人2', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-25 20:48:56', '2025-03-25 20:51:03', 1);
INSERT INTO `order` VALUES (9, 'ORDER17429895540598177', 8, 3349.00, 3349.00, '待发货', 68, NULL, 'wechat', 'standard', 0.00, 8, '收货人2', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-26 19:45:54', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (10, 'ORDER17431449048035508', 9, 189.90, 189.90, '已完成', 69, NULL, 'alipay', 'standard', 0.00, 7, '收货人3', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-03-28 20:01:09', '1743163268524221', '顺丰速运', '2025-03-28 20:15:18', NULL, NULL, 0, 0, '2025-03-28 14:55:05', '2025-03-28 20:15:18', 1);
INSERT INTO `order` VALUES (11, 'ORDER17433100841148442', 9, 379.80, 379.80, '待收货', 70, NULL, 'wechat', 'standard', 0.00, 9, '收货人2', '13666522356', '上海市', '上海市', '静安区', '南昌路10086号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-03-30 13:11:37', '1743311497479303', '中通快递', NULL, NULL, NULL, 0, 0, '2025-03-30 12:48:04', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (12, 'ORDER17433427340697529', 9, 59.90, 59.90, '待发货', 71, NULL, 'wechat', 'standard', 0.00, 7, '收货人3', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-30 21:52:14', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (13, 'ORDER17433435585039867', 9, 159.90, 159.90, '待发货', 72, NULL, 'wechat', 'standard', 0.00, 6, '收货人1', '13555622458', '北京市', '北京市', '丰台区', '115745', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-30 22:05:59', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (14, 'ORDER17434213216393884', 9, 2239.20, 2239.20, '待发货', 73, NULL, 'alipay', 'standard', 0.00, 9, '收货人2', '13666522356', '上海市', '上海市', '静安区', '南昌路10086号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-03-31 19:42:02', '2025-03-31 19:42:04', 1);
INSERT INTO `order` VALUES (15, 'ORDER17443350818553567', 9, 10.00, 10.00, '已完成', 74, NULL, 'alipay', 'standard', 0.00, 7, '收货人1', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-11 09:32:06', '1744335125819435', '顺丰速运', '2025-04-11 09:32:41', NULL, NULL, 0, 0, '2025-04-11 09:31:22', '2025-04-11 09:32:41', 1);
INSERT INTO `order` VALUES (16, 'ORDER17443368784063579', 9, 189.90, 189.90, '待发货', 75, NULL, 'wechat', 'standard', 0.00, 9, '收货人2', '13666522356', '上海市', '上海市', '静安区', '南昌路10086号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-11 10:01:18', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (17, 'ORDER17443428080401753', 8, 199.90, 199.90, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 8, '收货人1', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:10:31', '超时未支付自动取消', 0, 0, '2025-04-11 11:40:08', '2025-04-15 14:10:31', 1);
INSERT INTO `order` VALUES (18, 'ORDER17443550667287320', 8, 139.00, 139.00, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 8, '收货人2', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:10:31', '超时未支付自动取消', 0, 0, '2025-04-11 15:04:27', '2025-04-15 14:10:31', 1);
INSERT INTO `order` VALUES (19, 'ORDER17443550885766478', 8, 139.00, 139.00, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 8, '收货人2', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:10:31', '超时未支付自动取消', 0, 0, '2025-04-11 15:04:49', '2025-04-15 14:10:31', 1);
INSERT INTO `order` VALUES (20, 'ORDER17443551064959816', 8, 139.00, 139.00, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 8, '收货人2', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:10:31', '超时未支付自动取消', 0, 0, '2025-04-11 15:05:06', '2025-04-15 14:10:31', 1);
INSERT INTO `order` VALUES (21, 'ORDER17443551411937606', 8, 139.00, 139.00, '待发货', 76, NULL, 'alipay', 'standard', 0.00, 8, '收货人2', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-11 15:05:41', '2025-04-11 15:23:40', 1);
INSERT INTO `order` VALUES (23, 'ORDER17443563929058771', 8, 298.00, 298.00, '待发货', 77, NULL, 'alipay', 'standard', 0.00, 8, '收货人2', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-11 15:26:33', '2025-04-11 15:26:36', 1);
INSERT INTO `order` VALUES (24, 'ORDER17443783487882645', 9, 189.90, 189.90, '待收货', 78, NULL, 'wechat', 'standard', 0.00, 6, '收货人1', '13555622458', '北京市', '北京市', '丰台区', '115745', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-13 21:42:19', '1744551738738376', '圆通速递', NULL, NULL, NULL, 0, 0, '2025-04-11 21:32:29', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (35, 'ORDER17445500621193029', 9, 699.90, 649.90, '已取消', 85, NULL, 'alipay', 'standard', 0.00, 6, '收货人1', '13555622458', '北京市', '北京市', '丰台区', '115745', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:17:30', '超时未支付自动取消', 0, 0, '2025-04-13 21:14:22', '2025-04-15 14:17:30', 1);
INSERT INTO `order` VALUES (36, 'ORDER17445503174596522', 9, 699.90, 649.90, '已取消', 86, NULL, 'alipay', 'standard', 0.00, 6, '收货人1', '13555622458', '北京市', '北京市', '丰台区', '115745', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:17:30', '超时未支付自动取消', 0, 0, '2025-04-13 21:18:37', '2025-04-15 14:17:30', 1);
INSERT INTO `order` VALUES (37, 'ORDER17445509559397100', 9, 699.90, 649.90, '已取消', 87, NULL, 'alipay', 'standard', 0.00, 9, '收货人2', '13666522356', '上海市', '上海市', '静安区', '南昌路10086号', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:17:30', '超时未支付自动取消', 0, 0, '2025-04-13 21:29:16', '2025-04-15 14:17:30', 1);
INSERT INTO `order` VALUES (38, 'ORDER17445517632626037', 9, 298.00, 248.00, '已取消', 88, NULL, 'alipay', 'standard', 0.00, 9, '收货人2', '13666522356', '上海市', '上海市', '静安区', '南昌路10086号', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:17:30', '超时未支付自动取消', 0, 0, '2025-04-13 21:42:43', '2025-04-15 14:17:30', 1);
INSERT INTO `order` VALUES (39, 'ORDER17445549299174761', 9, 399.90, 369.90, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 9, '收货人2', '13666522356', '上海市', '上海市', '静安区', '南昌路10086号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:17:30', '超时未支付自动取消', 0, 0, '2025-04-13 22:35:30', '2025-04-15 14:17:30', 1);
INSERT INTO `order` VALUES (40, 'ORDER17445549568288974', 9, 399.90, 349.90, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 7, '收货人3', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:17:30', '超时未支付自动取消', 0, 0, '2025-04-13 22:35:57', '2025-04-15 14:17:30', 1);
INSERT INTO `order` VALUES (41, 'ORDER17445549654586614', 9, 399.90, 379.90, '已取消', 89, NULL, 'alipay', 'standard', 0.00, 7, '收货人3', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 20.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:17:30', '超时未支付自动取消', 0, 0, '2025-04-13 22:36:05', '2025-04-15 14:17:30', 1);
INSERT INTO `order` VALUES (42, 'ORDER17446055257039754', 9, 89.90, 39.90, 'PENDING_SHIPMENT', 96, '2025-04-14 14:01:45', 'alipay', 'standard', 0.00, 7, '收货人3', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-14 12:38:46', '2025-04-14 14:01:45', 1);
INSERT INTO `order` VALUES (43, 'ORDER17446089940392244', 9, 149.00, 149.00, '已完成', 95, '2025-04-14 14:00:58', 'alipay', 'standard', 0.00, 16, '收货人2', '13666544586', '江苏省', '苏州市', '吴江区', '1555555', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-14 22:15:47', '1744640146843598', '邮政EMS', '2025-04-14 22:16:41', NULL, NULL, 0, 0, '2025-04-14 13:36:34', '2025-04-14 22:16:41', 1);
INSERT INTO `order` VALUES (44, 'ORDER17446103742268284', 9, 99.90, 99.90, '待收货', 94, '2025-04-14 14:00:11', 'alipay', 'standard', 0.00, 7, '收货人3', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-14 22:15:43', '1744640143229068', '邮政EMS', NULL, NULL, NULL, 0, 0, '2025-04-14 13:59:34', '2025-04-14 22:15:43', 1);
INSERT INTO `order` VALUES (45, 'ORDER17446351380987716', 9, 399.90, 349.90, '已完成', 97, '2025-04-14 20:52:23', 'alipay', 'standard', 0.00, 6, '收货人1', '13555622458', '北京市', '北京市', '丰台区', '115745', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-14 22:15:40', '1744640140150042', '申通快递', '2025-04-14 22:16:39', NULL, NULL, 0, 0, '2025-04-14 20:52:18', '2025-04-14 22:16:39', 1);
INSERT INTO `order` VALUES (46, 'ORDER17446395616614757', 9, 59.90, 9.90, '已完成', 98, '2025-04-14 22:06:05', 'alipay', 'standard', 0.00, 6, '收货人1', '13555622458', '北京市', '北京市', '丰台区', '115745', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-14 22:14:07', '1744640047350301', '中通快递', '2025-04-14 22:16:37', NULL, NULL, 0, 0, '2025-04-14 22:06:02', '2025-04-14 22:16:37', 1);
INSERT INTO `order` VALUES (47, 'ORDER17446955490614819', 9, 99.90, 49.90, '已完成', 99, '2025-04-15 13:44:26', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-15 13:45:09', '1744695909430896', '中通快递', '2025-04-16 20:04:47', NULL, NULL, 0, 0, '2025-04-15 13:39:09', '2025-04-16 20:04:47', 1);
INSERT INTO `order` VALUES (48, 'ORDER17446969408377345', 9, 189.90, 139.90, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:17:30', '超时未支付自动取消', 0, 0, '2025-04-15 14:02:21', '2025-04-15 14:17:30', 1);
INSERT INTO `order` VALUES (49, 'ORDER17446974753518683', 9, 189.90, 139.90, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-15 14:17:30', '超时未支付自动取消', 0, 0, '2025-04-15 14:11:15', '2025-04-15 14:17:30', 1);
INSERT INTO `order` VALUES (50, 'ORDER17448059023224799', 9, 1069.40, 1019.40, 'PENDING_SHIPMENT', 100, '2025-04-16 20:18:52', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 50.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-16 20:18:22', '2025-04-16 20:18:52', 1);
INSERT INTO `order` VALUES (51, 'ORDER17448064050339956', 9, 1069.40, 1039.40, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 7, 'qqq', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-16 20:28:08', '超时未支付自动取消', 0, 0, '2025-04-16 20:26:45', '2025-04-16 20:28:08', 1);
INSERT INTO `order` VALUES (52, 'ORDER17448076037709411', 9, 379.80, 379.80, 'PENDING_SHIPMENT', 101, '2025-04-16 20:47:44', 'alipay', 'standard', 0.00, 7, 'qqq', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-16 20:46:44', '2025-04-16 20:47:44', 1);
INSERT INTO `order` VALUES (53, 'ORDER17448083186517324', 9, 239.60, 239.60, '已完成', 102, '2025-04-16 20:58:48', 'alipay', 'standard', 0.00, 7, 'qqq', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-16 20:59:59', '1744808398992627', '京东物流', '2025-04-16 21:27:25', NULL, NULL, 0, 0, '2025-04-16 20:58:39', '2025-04-16 21:27:25', 1);
INSERT INTO `order` VALUES (54, 'ORDER17448106746512253', 9, 379.80, 379.80, 'PENDING_SHIPMENT', 103, '2025-04-16 21:38:03', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-16 21:37:55', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (55, 'ORDER17449398848975895', 9, 899.70, 889.70, '待收货', 104, '2025-04-18 09:40:01', 'alipay', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 10.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-18 10:57:24', '1744945043856166', '韵达快递', NULL, NULL, NULL, 0, 0, '2025-04-18 09:31:25', '2025-04-18 10:57:24', 1);
INSERT INTO `order` VALUES (56, 'ORDER17449528547744283', 9, 59.90, 59.90, 'PENDING_SHIPMENT', 105, '2025-04-18 13:24:32', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 13:07:35', '2025-04-18 13:24:32', 1);
INSERT INTO `order` VALUES (57, 'ORDER17449795558219373', 9, 189.90, 179.90, '待发货', 106, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 10.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 20:32:36', '2025-04-18 20:32:52', 1);
INSERT INTO `order` VALUES (58, 'ORDER17449803210071190', 9, 199.90, 179.90, 'PENDING_SHIPMENT', 107, '2025-04-18 21:13:47', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 20.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 20:45:21', '2025-04-18 21:13:47', 1);
INSERT INTO `order` VALUES (59, 'ORDER17449807430696568', 9, 149.00, 139.00, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 10.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-18 20:53:55', '超时未支付自动取消', 0, 0, '2025-04-18 20:52:23', '2025-04-18 20:53:55', 1);
INSERT INTO `order` VALUES (60, 'ORDER17449813436552135', 9, 89.90, 89.90, '待发货', 110, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 21:02:24', '2025-04-18 21:02:42', 1);
INSERT INTO `order` VALUES (61, 'ORDER17449816129176380', 9, 399.90, 369.90, 'PENDING_SHIPMENT', 114, '2025-04-18 21:13:43', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-18 21:08:40', '超时未支付自动取消', 0, 0, '2025-04-18 21:06:53', '2025-04-18 21:13:43', 1);
INSERT INTO `order` VALUES (62, 'ORDER17449819551205775', 9, 199.90, 179.90, 'PENDING_SHIPMENT', 113, '2025-04-18 21:12:45', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 20.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 21:12:35', '2025-04-18 21:12:45', 1);
INSERT INTO `order` VALUES (63, 'ORDER17449820595288082', 9, 139.00, 129.00, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 10.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-18 21:15:40', '超时未支付自动取消', 0, 0, '2025-04-18 21:14:20', '2025-04-18 21:15:40', 1);
INSERT INTO `order` VALUES (64, 'ORDER17449829297755677', 9, 199.90, 179.90, '待收货', 116, '2025-04-18 21:28:59', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 20.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-18 21:33:27', '1744983207447404', '顺丰速运', NULL, NULL, NULL, 0, 0, '2025-04-18 21:28:50', '2025-04-18 21:33:27', 1);
INSERT INTO `order` VALUES (65, 'ORDER17449833960601450', 9, 399.90, 369.90, 'PENDING_SHIPMENT', 120, '2025-04-18 21:38:04', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 21:36:36', '2025-04-18 21:38:04', 1);
INSERT INTO `order` VALUES (66, 'ORDER17449839678137295', 9, 399.90, 389.90, 'PENDING_SHIPMENT', 121, '2025-04-18 21:46:09', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 10.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 21:46:08', '2025-04-18 21:46:09', 1);
INSERT INTO `order` VALUES (67, 'ORDER17449840414779475', 9, 1039.60, 1009.60, 'PENDING_SHIPMENT', 122, '2025-04-18 21:47:27', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 21:47:21', '2025-04-18 21:47:27', 1);
INSERT INTO `order` VALUES (68, 'ORDER17449844147359468', 9, 2999.00, 2969.00, 'PENDING_SHIPMENT', 123, '2025-04-18 21:53:44', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 21:53:35', '2025-04-18 21:53:44', 1);
INSERT INTO `order` VALUES (69, 'ORDER17449845632983598', 9, 298.00, 268.00, 'PENDING_SHIPMENT', 125, '2025-04-18 21:56:06', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 21:56:03', '2025-04-18 21:56:06', 1);
INSERT INTO `order` VALUES (70, 'ORDER17449852325764091', 9, 2088.90, 2058.90, 'PENDING_SHIPMENT', 126, '2025-04-18 22:39:08', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 22:07:13', '2025-04-18 22:39:08', 1);
INSERT INTO `order` VALUES (71, 'ORDER17449852869362755', 9, 894.00, 864.00, 'PENDING_SHIPMENT', 128, '2025-04-18 22:08:54', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 22:08:07', '2025-04-18 22:08:54', 1);
INSERT INTO `order` VALUES (72, 'ORDER17449871938104796', 9, 778.70, 748.70, 'PENDING_SHIPMENT', 129, '2025-04-18 22:40:00', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 22:39:54', '2025-04-18 22:40:00', 1);
INSERT INTO `order` VALUES (73, 'ORDER17449879317155084', 9, 2296.00, 2286.00, 'PENDING_SHIPMENT', 131, '2025-04-18 22:52:55', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 10.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 22:52:12', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (74, 'ORDER17449879795469034', 9, 2296.00, 2266.00, 'PENDING_SHIPMENT', 132, '2025-04-18 22:53:03', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-18 22:53:00', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (75, 'ORDER17450441292649624', 9, 447.00, 417.00, 'PENDING_SHIPMENT', 133, '2025-04-19 14:29:03', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-19 14:28:49', '2025-04-19 14:29:03', 1);
INSERT INTO `order` VALUES (76, 'ORDER17450450886945756', 9, 299.70, 269.70, '待发货', 134, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-19 14:44:49', '2025-04-19 14:44:50', 1);
INSERT INTO `order` VALUES (77, 'ORDER17450451873077695', 9, 319.60, 289.60, 'PENDING_SHIPMENT', 137, '2025-04-19 14:46:58', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-19 14:46:27', '2025-04-19 14:46:58', 1);
INSERT INTO `order` VALUES (78, 'ORDER17450453576811598', 9, 99.90, 99.90, 'PENDING_SHIPMENT', 138, '2025-04-19 14:49:23', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-19 14:49:18', '2025-04-19 14:49:23', 1);
INSERT INTO `order` VALUES (79, 'ORDER17450459875746430', 9, 399.90, 369.90, '待收货', 139, '2025-04-19 14:59:50', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-19 16:58:31', '1745053111251509', '圆通速递', NULL, NULL, NULL, 0, 0, '2025-04-19 14:59:48', '2025-04-19 16:58:31', 1);
INSERT INTO `order` VALUES (80, 'ORDER17450473725228396', 9, 59.90, 59.90, '待收货', 140, '2025-04-19 15:22:57', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-19 15:47:27', '1745048847024006', '圆通速递', NULL, NULL, NULL, 0, 0, '2025-04-19 15:22:53', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (81, 'ORDER17450478614373007', 9, 399.90, 369.90, '已完成', 141, '2025-04-19 15:31:07', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-19 15:32:00', '1745047920466690', '顺丰速运', '2025-04-19 15:32:09', NULL, NULL, 0, 0, '2025-04-19 15:31:01', '2025-04-19 15:32:09', 1);
INSERT INTO `order` VALUES (82, 'ORDER17450486780378791', 9, 239.60, 209.60, '已完成', 142, '2025-04-19 15:44:41', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-19 15:45:57', '1745048757245512', '顺丰速运', '2025-04-19 15:46:26', NULL, NULL, 0, 0, '2025-04-19 15:44:38', '2025-04-19 15:46:26', 1);
INSERT INTO `order` VALUES (83, 'ORDER17450499740802107', 9, 2799.30, 2769.30, '已完成', 143, '2025-04-19 16:06:19', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-19 16:09:58', '1745050197505754', '韵达快递', NULL, NULL, NULL, 0, 0, '2025-04-19 16:06:14', '2025-04-19 16:10:25', 1);
INSERT INTO `order` VALUES (84, 'ORDER17450503412792999', 9, 298.00, 268.00, '已完成', 146, '2025-04-19 16:15:06', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-19 16:15:25', '1745050524832409', '中通快递', NULL, NULL, NULL, 0, 0, '2025-04-19 16:12:21', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (85, 'ORDER17450548263311761', 9, 89990.00, 89960.00, '已完成', 147, '2025-04-19 17:27:09', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-19 17:28:21', '1745054900921268', '顺丰速运', NULL, NULL, NULL, 0, 0, '2025-04-19 17:27:06', '2025-04-19 17:30:00', 1);
INSERT INTO `order` VALUES (86, 'ORDER17450703688316173', 9, 1948.50, 1918.50, 'PENDING_SHIPMENT', 148, '2025-04-19 22:50:31', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-19 21:46:09', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (87, 'ORDER17450713988864410', 9, 1199.60, 1169.60, 'PENDING_SHIPMENT', 149, '2025-04-19 22:03:26', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-19 22:03:19', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (88, 'ORDER17450718197018013', 9, 1999.50, 1969.50, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-19 22:11:28', '超时未支付自动取消', 0, 0, '2025-04-19 22:10:20', '2025-04-19 22:11:28', 1);
INSERT INTO `order` VALUES (89, 'ORDER17450722127227683', 9, 1999.50, 1969.50, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-19 22:18:28', '超时未支付自动取消', 0, 0, '2025-04-19 22:16:53', '2025-04-19 22:18:28', 1);
INSERT INTO `order` VALUES (90, 'ORDER17450723620775825', 9, 774.00, 744.00, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-19 22:20:28', '超时未支付自动取消', 0, 0, '2025-04-19 22:19:22', '2025-04-19 22:20:28', 1);
INSERT INTO `order` VALUES (91, 'ORDER17450724127713058', 9, 774.00, 744.00, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-19 22:21:28', '超时未支付自动取消', 0, 0, '2025-04-19 22:20:13', '2025-04-19 22:21:28', 1);
INSERT INTO `order` VALUES (92, 'ORDER17450727037494755', 9, 903.00, 873.00, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-19 22:26:28', '超时未支付自动取消', 0, 0, '2025-04-19 22:25:04', '2025-04-19 22:26:28', 1);
INSERT INTO `order` VALUES (93, 'ORDER17450727385853230', 9, 903.00, 873.00, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-19 22:27:28', '超时未支付自动取消', 0, 0, '2025-04-19 22:25:39', '2025-04-19 22:27:28', 1);
INSERT INTO `order` VALUES (94, 'ORDER17451129283889471', 9, 949.50, 919.50, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-20 09:37:03', '超时未支付自动取消', 0, 0, '2025-04-20 09:35:28', '2025-04-20 09:37:03', 1);
INSERT INTO `order` VALUES (95, 'ORDER17451132439942397', 9, 949.50, 919.50, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-20 09:42:03', '超时未支付自动取消', 0, 0, '2025-04-20 09:40:44', '2025-04-20 09:42:03', 1);
INSERT INTO `order` VALUES (96, 'ORDER17451140395427141', 9, 2208.30, 2178.30, 'PENDING_SHIPMENT', 150, '2025-04-20 09:54:32', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-20 09:54:00', '2025-04-20 09:54:32', 1);
INSERT INTO `order` VALUES (97, 'ORDER17451144733955838', 9, 2399.40, 2369.40, 'PENDING_SHIPMENT', 151, '2025-04-20 10:01:47', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-20 10:01:13', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (98, 'ORDER17451145292332064', 9, 399.90, 369.90, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-20 12:59:35', '超时未支付自动取消', 0, 0, '2025-04-20 10:02:09', '2025-04-20 12:59:35', 1);
INSERT INTO `order` VALUES (99, 'ORDER17451278084316405', 9, 899.10, 869.10, 'PENDING_SHIPMENT', 153, '2025-04-20 13:43:46', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-20 13:43:28', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (100, 'ORDER17451301068395898', 9, 359.40, 329.40, 'PENDING_SHIPMENT', 154, '2025-04-20 14:28:56', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-20 14:21:47', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (101, 'ORDER17451304374482107', 9, 359.40, 329.40, 'PENDING_SHIPMENT', 155, '2025-04-20 14:28:54', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-20 14:27:17', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (102, 'ORDER17451305201237152', 9, 1139.40, 1109.40, 'PENDING_SHIPMENT', 156, '2025-04-20 14:28:51', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-20 14:28:40', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (103, 'ORDER17451305469165686', 9, 3576.00, 3576.00, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-20 15:06:07', '超时未支付自动取消', 0, 0, '2025-04-20 14:29:07', '2025-04-20 15:06:07', 1);
INSERT INTO `order` VALUES (104, 'ORDER17451407073845313', 9, 1029.40, 999.40, '已完成', 157, '2025-04-20 17:18:32', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-20 17:37:02', '1745141821611516', '申通快递', NULL, NULL, NULL, 0, 0, '2025-04-20 17:18:27', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (105, 'ORDER17451448487674395', 9, 189.90, 169.90, 'PENDING_SHIPMENT', 158, '2025-04-20 18:27:33', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 20.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-20 18:27:29', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (106, 'ORDER17451457560899513', 9, 299.70, 269.70, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-20 18:43:52', '超时未支付自动取消', 0, 0, '2025-04-20 18:42:36', '2025-04-20 18:43:52', 1);
INSERT INTO `order` VALUES (107, 'ORDER17451458509872621', 9, 569.70, 539.70, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-20 18:45:52', '超时未支付自动取消', 0, 0, '2025-04-20 18:44:11', '2025-04-20 18:45:52', 1);
INSERT INTO `order` VALUES (108, 'ORDER17451460141667503', 9, 1157.80, 1127.80, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-20 18:48:34', '超时未支付自动取消', 0, 0, '2025-04-20 18:46:54', '2025-04-20 18:48:34', 1);
INSERT INTO `order` VALUES (109, 'ORDER17451462811964198', 9, 189.90, 169.90, '已完成', 162, '2025-04-20 18:51:30', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 20.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-20 18:52:03', '1745146322530764', '中通快递', NULL, NULL, NULL, 0, 0, '2025-04-20 18:51:21', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (110, 'ORDER17451528919724899', 9, 799.80, 769.80, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-20 20:43:15', '超时未支付自动取消', 0, 0, '2025-04-20 20:41:32', '2025-04-20 20:43:15', 1);
INSERT INTO `order` VALUES (111, 'ORDER17452114489074352', 9, 59.90, 59.90, 'PENDING_SHIPMENT', 163, '2025-04-21 12:57:44', 'alipay', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 12:57:29', '2025-04-21 12:57:44', 1);
INSERT INTO `order` VALUES (112, 'ORDER17452132971645998', 9, 319.60, 289.60, 'PENDING_SHIPMENT', 164, '2025-04-21 13:28:23', 'wechat', 'standard', 0.00, 6, '彭伟株', '13555622458', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 13:28:17', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (113, 'ORDER17452135226618130', 9, 239.70, 209.70, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-21 13:33:28', '超时未支付自动取消', 0, 0, '2025-04-21 13:32:03', '2025-04-21 13:33:28', 1);
INSERT INTO `order` VALUES (114, 'ORDER17452137537325147', 9, 79.90, 79.90, '已取消', NULL, NULL, 'alipay', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-21 13:37:28', '超时未支付自动取消', 0, 0, '2025-04-21 13:35:54', '2025-04-21 13:37:28', 1);
INSERT INTO `order` VALUES (115, 'ORDER17452140968006383', 9, 149.00, 139.00, 'PENDING_SHIPMENT', 167, '2025-04-21 13:42:23', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 10.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 13:41:37', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (116, 'ORDER17452142961221645', 9, 399.90, 369.90, 'PENDING_SHIPMENT', 168, '2025-04-21 13:45:08', 'alipay', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 13:44:56', '2025-04-21 13:45:08', 1);
INSERT INTO `order` VALUES (117, 'ORDER17452145246841901', 9, 569.70, 539.70, 'PENDING_SHIPMENT', 169, '2025-04-21 13:48:49', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 13:48:45', '2025-04-21 14:57:04', 1);
INSERT INTO `order` VALUES (118, 'ORDER17452148490214231', 9, 569.70, 539.70, 'PENDING_SHIPMENT', 170, '2025-04-21 13:54:12', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 13:54:09', '2025-04-21 14:57:05', 1);
INSERT INTO `order` VALUES (119, 'ORDER17452150243901075', 9, 189.90, 169.90, 'PENDING_SHIPMENT', 171, '2025-04-21 13:57:14', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 20.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 13:57:04', '2025-04-21 14:57:06', 1);
INSERT INTO `order` VALUES (120, 'ORDER17452154110214171', 9, 399.90, 369.90, 'PENDING_SHIPMENT', 172, '2025-04-21 14:03:36', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 14:03:31', '2025-04-21 14:57:07', 1);
INSERT INTO `order` VALUES (121, 'ORDER17452160264701642', 9, 1599.60, 1569.60, 'PENDING_SHIPMENT', 173, '2025-04-21 14:14:12', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 14:13:46', '2025-04-21 14:56:55', 1);
INSERT INTO `order` VALUES (122, 'ORDER17452167814059647', 9, 189.90, 169.90, 'PENDING_SHIPMENT', 174, '2025-04-21 14:26:26', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 20.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 14:26:21', '2025-04-21 14:56:54', 1);
INSERT INTO `order` VALUES (123, 'ORDER17452172041521840', 9, 419.30, 389.30, 'PENDING_SHIPMENT', 175, '2025-04-21 14:33:34', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 14:33:24', '2025-04-21 14:56:51', 1);
INSERT INTO `order` VALUES (124, 'ORDER17452186371897636', 9, 949.50, 919.50, 'PENDING_SHIPMENT', 176, '2025-04-21 14:57:21', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 14:57:17', '2025-04-21 15:08:06', 1);
INSERT INTO `order` VALUES (125, 'ORDER17452193667296638', 9, 299.70, 269.70, 'PENDING_SHIPMENT', 177, '2025-04-21 15:09:30', 'alipay', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 15:09:27', '2025-04-21 15:09:30', 1);
INSERT INTO `order` VALUES (126, 'ORDER17452193970978567', 9, 1199.60, 1169.60, 'PENDING_SHIPMENT', 178, '2025-04-21 15:10:10', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 15:09:57', '2025-04-21 16:00:00', 1);
INSERT INTO `order` VALUES (127, 'ORDER17452201961333527', 9, 1799.40, 1769.40, 'PENDING_SHIPMENT', 179, '2025-04-21 15:23:19', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 15:23:16', '2025-04-21 16:00:00', 1);
INSERT INTO `order` VALUES (128, 'ORDER17452202440941328', 9, 539.40, 509.40, 'PENDING_SHIPMENT', 180, '2025-04-21 15:24:12', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 15:24:04', '2025-04-21 15:30:15', 1);
INSERT INTO `order` VALUES (129, 'ORDER17452207018446413', 9, 449.10, 419.10, 'PENDING_SHIPMENT', 181, '2025-04-21 15:31:55', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 15:31:42', '2025-04-21 16:00:00', 1);
INSERT INTO `order` VALUES (130, 'ORDER17452212896086413', 9, 899.00, 869.00, 'PENDING_SHIPMENT', 182, '2025-04-21 15:41:37', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 15:41:30', '2025-04-21 15:50:50', 1);
INSERT INTO `order` VALUES (131, 'ORDER17452215674805396', 9, 272.00, 242.00, 'PENDING_SHIPMENT', 183, '2025-04-21 15:46:20', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 15:46:07', '2025-04-21 15:50:45', 1);
INSERT INTO `order` VALUES (132, 'ORDER17452218697879314', 9, 649.50, 619.50, '待收货', 184, '2025-04-21 15:51:35', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-24 16:42:39', '1745484159139862', '圆通速递', NULL, NULL, NULL, 0, 0, '2025-04-21 15:51:10', '2025-04-24 16:42:39', 1);
INSERT INTO `order` VALUES (133, 'ORDER17452371392251432', 9, 189.90, 169.90, '已取消', NULL, NULL, NULL, 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 20.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-04-21 20:07:32', '超时未支付自动取消', 0, 0, '2025-04-21 20:05:39', '2025-04-21 20:07:32', 1);
INSERT INTO `order` VALUES (134, 'ORDER17452396871972804', 9, 189.90, 169.90, 'PENDING_SHIPMENT', 185, '2025-04-21 20:48:10', 'alipay', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 20.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 20:48:07', '2025-04-21 21:00:00', 1);
INSERT INTO `order` VALUES (135, 'ORDER17452402212747378', 9, 596.00, 566.00, 'PENDING_SHIPMENT', 186, '2025-04-21 20:57:12', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 20:57:01', '2025-04-21 21:00:00', 1);
INSERT INTO `order` VALUES (164, 'ORDER17452428483553410', 9, 1999.50, 1969.50, 'PENDING_SHIPMENT', 187, '2025-04-21 21:40:54', NULL, 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-21 21:40:48', '2025-04-21 21:40:54', 1);
INSERT INTO `order` VALUES (165, 'ORDER17453246643974732', 9, 239.60, 209.60, 'PENDING_SHIPMENT', 188, '2025-04-22 20:24:27', 'alipay', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-22 20:24:24', '2025-04-22 21:00:00', 1);
INSERT INTO `order` VALUES (166, 'ORDER17453248963623581', 9, 139.00, 129.00, 'PENDING_SHIPMENT', 189, '2025-04-22 20:28:19', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 10.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-22 20:28:16', '2025-04-22 20:34:04', 1);
INSERT INTO `order` VALUES (167, 'ORDER17453255706674318', 9, 499.50, 469.50, 'PENDING_SHIPMENT', 190, '2025-04-22 20:39:35', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-22 20:39:31', '2025-04-22 21:00:00', 1);
INSERT INTO `order` VALUES (168, 'ORDER17453264196027857', 9, 759.60, 759.60, 'PENDING_SHIPMENT', 191, '2025-04-22 20:53:43', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-22 20:53:40', '2025-04-22 21:00:00', 1);
INSERT INTO `order` VALUES (169, 'ORDER17453294836071112', 9, 1999.50, 1969.50, 'PENDING_SHIPMENT', 192, '2025-04-22 21:44:55', 'alipay', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-22 21:44:44', '2025-04-22 22:00:00', 1);
INSERT INTO `order` VALUES (170, 'ORDER17453295353832425', 9, 399.60, 369.60, 'PENDING_SHIPMENT', 193, '2025-04-22 21:45:39', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-22 21:45:35', '2025-04-22 22:00:00', 1);
INSERT INTO `order` VALUES (171, 'ORDER17453297779016486', 9, 1599.60, 1569.60, '已完成', 194, '2025-04-22 21:49:41', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, 2, 0.00, 0.00, 0, 0.00, NULL, '2025-04-23 14:10:35', '1745388634950233', '顺丰速运', NULL, NULL, NULL, 0, 0, '2025-04-22 21:49:38', '2025-04-23 20:03:34', 1);
INSERT INTO `order` VALUES (172, 'ORDER17453886695259124', 9, 99.90, 99.90, '已完成', 195, '2025-04-23 14:11:21', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-04-23 14:56:34', '1745391393955311', '圆通速递', NULL, NULL, NULL, 0, 0, '2025-04-23 14:11:10', '2025-04-23 20:29:27', 1);
INSERT INTO `order` VALUES (173, 'ORDER17454113357745166', 9, 759.60, 729.60, 'PENDING_SHIPMENT', 196, '2025-04-23 20:29:02', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, 2, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-23 20:28:56', '2025-04-23 21:00:00', 1);
INSERT INTO `order` VALUES (174, 'ORDER17454133754631831', 9, 199.90, 179.90, '待收货', 197, '2025-04-23 21:02:58', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 20.00, 3, 0.00, 0.00, 0, 0.00, NULL, '2025-04-24 19:53:35', '1745495615421985', '邮政EMS', NULL, NULL, NULL, 0, 0, '2025-04-23 21:02:55', '2025-04-24 19:53:35', 1);
INSERT INTO `order` VALUES (175, 'ORDER17454143481457218', 9, 799.60, 769.60, 'PENDING_SHIPMENT', 198, '2025-04-23 21:19:14', 'alipay', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, 2, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-23 21:19:08', '2025-04-24 13:00:00', 1);
INSERT INTO `order` VALUES (176, 'ORDER17454145288759162', 9, 399.20, 369.20, 'PENDING_SHIPMENT', 199, '2025-04-23 21:22:55', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 30.00, 2, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-23 21:22:09', '2025-04-24 13:00:00', 1);
INSERT INTO `order` VALUES (177, 'ORDER17454153673359147', 9, 519.60, 469.60, '待收货', 200, '2025-04-23 21:36:15', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 50.00, 10, 0.00, 0.00, 0, 0.00, NULL, '2025-04-25 09:09:50', '1745543389690786', '京东物流', NULL, NULL, NULL, 0, 0, '2025-04-23 21:36:07', '2025-04-25 09:09:50', 1);
INSERT INTO `order` VALUES (178, 'ORDER17454712732296454', 9, 99.90, 49.90, '待收货', 201, '2025-04-24 13:08:03', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 50.00, 9, 0.00, 0.00, 0, 0.00, NULL, '2025-04-24 19:42:32', '1745494951873789', '申通快递', NULL, NULL, NULL, 0, 0, '2025-04-24 13:07:53', '2025-04-24 19:42:32', 1);
INSERT INTO `order` VALUES (179, 'ORDER17454721117056277', 9, 2399.40, 2384.40, '待收货', 202, '2025-04-24 13:21:59', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 15.00, 14, 0.00, 0.00, 0, 0.00, NULL, '2025-04-24 17:21:32', '1745486492168386', '韵达快递', NULL, NULL, NULL, 0, 0, '2025-04-24 13:21:52', '2025-04-24 17:21:32', 1);
INSERT INTO `order` VALUES (180, 'ORDER17454725349573471', 9, 1199.60, 1149.60, '已完成', 203, '2025-04-24 13:29:00', 'wechat', 'standard', 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 50.00, 12, 0.00, 0.00, 0, 0.00, NULL, '2025-04-24 15:17:10', '1745479030017388', '中通快递', '2025-04-26 15:13:44', NULL, NULL, 0, 0, '2025-04-24 13:28:55', '2025-04-26 15:13:44', 1);
INSERT INTO `order` VALUES (181, 'ORDER17456516991175745', 9, 984.00, 984.00, '待发货', 204, '2025-04-26 15:15:02', 'alipay', 'standard', 0.00, 7, '王五', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-26 15:14:59', '2025-04-26 15:15:09', 1);
INSERT INTO `order` VALUES (182, 'ORDER17456796217205119', 9, 99.90, 99.90, '待发货', 205, '2025-04-26 23:00:25', 'alipay', 'standard', 0.00, 7, '王五', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-26 23:00:22', '2025-04-26 23:00:37', 1);
INSERT INTO `order` VALUES (183, 'ORDER17456797301057465', 9, 189.90, 189.90, 'PENDING_SHIPMENT', 206, '2025-04-26 23:02:15', 'alipay', 'standard', 0.00, 7, '王五', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-04-26 23:02:10', '2025-04-26 23:02:15', 1);
INSERT INTO `order` VALUES (195, 'OD1746540784960366edc', 8, 328.00, 278.00, 'cancelled', 228, NULL, 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, 19, 50.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-11 09:53:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-06 22:13:05', '2025-05-11 09:53:00', 3);
INSERT INTO `order` VALUES (196, 'OD17465414906845dbb63', 8, 159.90, 109.90, 'cancelled', 229, NULL, 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, 19, 50.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-11 09:53:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-06 22:24:51', '2025-05-11 09:53:00', 3);
INSERT INTO `order` VALUES (197, 'OD174654189790984570d', 8, 229.90, 229.90, 'cancelled', 230, NULL, 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-11 09:53:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-06 22:31:38', '2025-05-11 09:53:00', 3);
INSERT INTO `order` VALUES (198, 'OD17465432495356e4092', 8, 49.90, 49.90, 'cancelled', 231, NULL, 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-11 09:53:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-06 22:54:10', '2025-05-11 09:53:00', 3);
INSERT INTO `order` VALUES (199, 'OD174654347401056de4b', 8, 699.90, 649.90, 'cancelled', 232, NULL, 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, 19, 50.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-11 09:53:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-06 22:57:54', '2025-05-11 09:53:00', 3);
INSERT INTO `order` VALUES (200, 'OD1746623366746e117e8', 8, 59.99, 59.99, 'cancelled', NULL, NULL, 'wallet', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-11 09:53:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-07 21:09:27', '2025-05-11 09:53:00', 2);
INSERT INTO `order` VALUES (201, 'OD1746625330343300015', 8, 129.90, 129.90, 'cancelled', 233, NULL, 'wechat', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-07 22:03:35', NULL, 0, 0, '2025-05-07 21:42:10', '2025-05-07 22:03:35', 3);
INSERT INTO `order` VALUES (202, 'OD17466276982781ac330', 8, 189.90, 139.90, 'completed', 245, '2025-05-08 13:43:00', 'wechat', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, 19, 50.00, 0.00, 0, 0.00, NULL, '2025-05-09 20:44:13', 'EMS2505092044137839', 'EMS快递', '2025-05-09 20:45:06', NULL, NULL, 0, 0, '2025-05-07 22:21:38', '2025-05-09 20:45:06', 6);
INSERT INTO `order` VALUES (203, 'OD17466803173112b1180', 9, 79.90, 29.90, 'completed', 249, '2025-05-10 22:54:26', 'wechat', NULL, 0.00, 9, '李四', '13666522356', '上海', '上海市', '徐汇区', '地中海10086号', '', '', 0.00, 18, 50.00, 0.00, 0, 0.00, NULL, '2025-05-10 22:55:36', 'BEST2505102255353745', '百世快递', '2025-05-10 22:55:49', NULL, NULL, 0, 0, '2025-05-08 12:58:37', '2025-05-10 22:55:49', 7);
INSERT INTO `order` VALUES (204, 'OD1746681070145d9652e', 9, 399.90, 399.90, 'completed', 238, NULL, 'wechat', NULL, 0.00, 7, '王五', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-05-09 20:43:16', 'YD2505092043151360', '韵达速递', '2025-05-10 22:47:01', NULL, NULL, 0, 0, '2025-05-08 13:11:10', '2025-05-10 22:47:01', 4);
INSERT INTO `order` VALUES (205, 'OD1746681443285b6ea27', 8, 129.90, 79.90, 'completed', 244, NULL, 'wechat', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, 19, 50.00, 0.00, 0, 0.00, NULL, '2025-05-09 13:22:33', 'SF2505091322338066', '顺丰速运', '2025-05-09 13:45:26', NULL, NULL, 1, 0, '2025-05-08 13:17:23', '2025-05-27 22:53:00', 8);
INSERT INTO `order` VALUES (206, 'OD1746796766856f06ea9', 8, 299.70, 299.70, 'completed', 246, '2025-05-09 21:20:18', 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-05-09 21:20:44', 'ZT2505092120432303', '中通快递', '2025-05-09 21:21:31', NULL, NULL, 1, 0, '2025-05-09 21:19:27', '2025-05-18 09:31:01', 4);
INSERT INTO `order` VALUES (207, 'OD17467996633588ae4d9', 8, 259.80, 209.80, 'completed', 247, '2025-05-09 22:07:56', 'wechat', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, 19, 50.00, 0.00, 0, 0.00, NULL, '2025-05-09 22:08:25', 'STO2505092208250563', '申通快递', '2025-05-09 22:09:10', NULL, NULL, 1, 0, '2025-05-09 22:07:43', '2025-05-30 09:34:50', 4);
INSERT INTO `order` VALUES (208, 'OD17468018813539a9556', 8, 589.80, 539.80, 'completed', 248, '2025-05-09 22:44:53', 'wechat', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, 19, 50.00, 0.00, 0, 0.00, NULL, '2025-05-09 22:45:14', 'JT2505092245138415', '极兔速递', '2025-05-09 22:45:42', NULL, NULL, 1, 0, '2025-05-09 22:44:41', '2025-05-17 19:28:10', 4);
INSERT INTO `order` VALUES (209, 'OD17468890668821ab08d', 9, 429.80, 379.80, 'completed', 250, '2025-05-10 22:57:57', 'wechat', NULL, 0.00, 16, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', '', 0.00, 18, 50.00, 0.00, 0, 0.00, NULL, '2025-05-10 23:01:10', 'YT2505102301099841', '圆通速递', '2025-05-10 23:01:22', NULL, NULL, 0, 0, '2025-05-10 22:57:47', '2025-05-10 23:01:22', 4);
INSERT INTO `order` VALUES (210, 'OD17468895285965ff711', 9, 159.90, 109.90, 'completed', 251, '2025-05-10 23:05:49', 'wechat', NULL, 0.00, 9, '李四', '13666522356', '上海', '上海市', '徐汇区', '地中海10086号', '', '', 0.00, 20, 50.00, 0.00, 0, 0.00, NULL, '2025-05-10 23:06:06', 'EMS2505102306050130', 'EMS快递', '2025-05-10 23:06:18', NULL, NULL, 0, 0, '2025-05-10 23:05:29', '2025-05-10 23:06:18', 4);
INSERT INTO `order` VALUES (211, 'OD17469289178677b3311', 8, 259.80, 259.80, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-11 11:19:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-11 10:01:58', '2025-05-11 11:19:00', 2);
INSERT INTO `order` VALUES (212, 'OD1746936244945e237b5', 8, 79.90, 79.90, 'pending_shipment', 252, '2025-05-11 12:04:14', 'wechat', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-05-11 12:04:05', '2025-05-11 12:04:14', 2);
INSERT INTO `order` VALUES (213, 'OD17469398067101837dd', 8, 399.90, 399.90, 'pending_shipment', 253, '2025-05-11 13:04:38', 'wechat', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-05-11 13:03:27', '2025-05-11 13:04:37', 2);
INSERT INTO `order` VALUES (214, 'OD1746940786288d4fce4', 8, 189.90, 189.90, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-11 13:25:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-11 13:19:46', '2025-05-11 13:25:00', 2);
INSERT INTO `order` VALUES (215, 'OD17469425530540505e9', 8, 39.90, 49.90, 'cancelled', NULL, NULL, 'alipay', NULL, 10.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-11 13:55:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-11 13:49:13', '2025-05-11 14:03:14', 2);
INSERT INTO `order` VALUES (216, 'OD1746943484132202f5d', 8, 49.90, 59.90, 'cancelled', NULL, NULL, 'alipay', NULL, 10.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-11 14:10:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-11 14:04:44', '2025-05-11 14:10:00', 2);
INSERT INTO `order` VALUES (217, 'OD1746944159254ef56e4', 8, 59.90, 69.90, 'pending_shipment', 254, '2025-05-11 14:16:07', 'wechat', NULL, 10.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-05-11 14:15:59', '2025-05-11 14:16:06', 2);
INSERT INTO `order` VALUES (218, 'OD1747060259777612246', 8, 129.90, 79.90, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, 19, 50.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-12 22:33:03', NULL, 0, 0, '2025-05-12 22:31:00', '2025-05-12 22:33:03', 2);
INSERT INTO `order` VALUES (219, 'OD17470605148463a1ef5', 8, 399.90, 399.90, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-12 22:41:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-12 22:35:15', '2025-05-12 22:41:00', 2);
INSERT INTO `order` VALUES (220, 'OD17470608003180c957b', 8, 49.90, 49.90, 'cancelled', NULL, NULL, 'alipay', NULL, 10.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 1000, 10.00, NULL, NULL, NULL, NULL, NULL, '2025-05-12 22:45:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-12 22:40:00', '2025-05-12 22:45:00', 2);
INSERT INTO `order` VALUES (221, 'OD174728514628658bee0', 8, 129.90, 129.90, 'pending_shipment', 255, '2025-05-15 12:59:48', 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-05-15 12:59:06', '2025-05-15 12:59:48', 2);
INSERT INTO `order` VALUES (222, 'OD1747320522379daa9d8', 8, 69.90, 69.90, 'completed', 260, '2025-05-15 22:49:20', 'alipay', NULL, 10.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 1000, 10.00, NULL, '2025-05-20 22:54:01', 'YZPY2505202254008936', '邮政快递包裹', '2025-05-22 21:35:26', NULL, NULL, 1, 0, '2025-05-15 22:48:42', '2025-05-27 20:10:00', 8);
INSERT INTO `order` VALUES (225, 'OD17478392282642a46ae', 8, 399.90, 399.90, 'refunded', 261, '2025-05-21 22:54:59', 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-05-21 22:53:48', '2025-05-21 22:54:59', 4);
INSERT INTO `order` VALUES (226, 'OD17478984664046d3f2b', 8, 25.80, 35.80, 'cancelled', NULL, NULL, 'alipay', NULL, 10.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-22 15:27:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-22 15:21:06', '2025-05-22 15:27:00', 2);
INSERT INTO `order` VALUES (227, 'OD17479041992960ab1fc', 8, 199.00, 199.00, 'cancelled', 263, '2025-05-22 17:01:49', 'wechat', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-22 18:09:35', NULL, 0, 0, '2025-05-22 16:56:39', '2025-05-22 18:09:35', 4);
INSERT INTO `order` VALUES (228, 'OD1747922100745f8639c', 8, 716.00, 716.00, 'refunding', 264, '2025-05-22 21:56:21', 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-05-22 21:55:01', '2025-05-22 21:56:21', 3);
INSERT INTO `order` VALUES (229, 'OD1748085552938f9acc0', 8, 129.00, 129.00, 'shipped', 265, '2025-05-24 19:19:43', 'wechat', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-05-29 23:05:02', 'YD2505292305010709', '韵达速递', NULL, NULL, NULL, 0, 0, '2025-05-24 19:19:13', '2025-05-29 23:05:02', 3);
INSERT INTO `order` VALUES (230, 'OD1748086365510710a7f', 8, 199.00, 199.00, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-05-24 19:38:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-24 19:32:46', '2025-05-24 19:38:00', 2);
INSERT INTO `order` VALUES (231, 'OD174844173195788d6c7', 8, 756.00, 756.00, 'completed', 266, '2025-05-28 22:16:10', 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-05-28 22:16:57', 'YT2505282216561622', '圆通速递', '2025-05-28 22:17:15', NULL, NULL, 1, 0, '2025-05-28 22:15:32', '2025-05-28 22:17:44', 4);
INSERT INTO `order` VALUES (232, 'OD1748532233832df04d1', 8, 299.90, 269.90, 'cancelled', 267, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 3000, 30.00, NULL, NULL, NULL, NULL, NULL, '2025-05-30 09:06:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-29 23:23:54', '2025-05-30 09:06:00', 3);
INSERT INTO `order` VALUES (233, 'OD174853231755256c661', 8, 736.00, 716.00, 'cancelled', 268, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 2000, 20.00, NULL, NULL, NULL, NULL, NULL, '2025-05-30 09:06:01', '订单超时未支付，系统自动取消', 0, 0, '2025-05-29 23:25:18', '2025-05-30 09:06:01', 3);
INSERT INTO `order` VALUES (234, 'OD1748577051762df19cb', 8, 179.00, 129.00, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 5000, 50.00, NULL, NULL, NULL, NULL, NULL, '2025-06-03 16:34:00', '订单超时未支付，系统自动取消', 0, 0, '2025-05-30 11:50:52', '2025-06-03 16:34:00', 2);
INSERT INTO `order` VALUES (235, 'OD1749172272479c582e6', 8, 169.00, 119.00, 'shipped', 270, '2025-06-06 09:12:29', 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, 19, 50.00, 0.00, 0, 0.00, NULL, '2025-06-06 13:32:09', 'EMS2506061332085716', 'EMS快递', NULL, NULL, NULL, 0, 0, '2025-06-06 09:11:12', '2025-06-06 13:32:09', 4);
INSERT INTO `order` VALUES (236, 'OD17491728740800729c7', 8, 799.80, 749.80, 'completed', 272, '2025-06-06 09:22:39', 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, 22, 50.00, 0.00, 0, 0.00, NULL, '2025-06-06 09:39:56', 'YT2506060939567253', '圆通速递', '2025-06-06 09:46:48', NULL, NULL, 1, 0, '2025-06-06 09:21:14', '2025-06-06 09:47:15', 5);
INSERT INTO `order` VALUES (237, 'OD1749188154352415971', 8, 399.90, 399.90, 'completed', 273, '2025-06-06 13:36:36', 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, 27, 50.00, 0.00, 0, 0.00, NULL, '2025-06-06 13:36:58', 'BEST2506061336586498', '百世快递', '2025-06-06 13:37:09', NULL, NULL, 1, 0, '2025-06-06 13:35:54', '2025-06-06 13:37:45', 4);
INSERT INTO `order` VALUES (238, 'OD1749556103940f7657c', 8, 169.00, 169.00, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-10 19:54:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-10 19:48:24', '2025-06-10 19:54:00', 2);
INSERT INTO `order` VALUES (239, 'OD1749622926447f21f6f', 8, 328.00, 328.00, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-11 14:28:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-11 14:22:06', '2025-06-11 14:28:00', 2);
INSERT INTO `order` VALUES (240, 'OD174962995000653b73f', 8, 2580.00, 2580.00, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-11 16:25:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-11 16:19:10', '2025-06-11 16:25:00', 2);
INSERT INTO `order` VALUES (241, 'OD174963042817199e343', 8, 358.00, 358.00, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-11 16:33:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-11 16:27:08', '2025-06-11 16:33:00', 2);
INSERT INTO `order` VALUES (242, 'OD17496310719225d7722', 8, 129.90, 129.90, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-11 16:43:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-11 16:37:52', '2025-06-11 16:43:00', 2);
INSERT INTO `order` VALUES (243, 'OD1749631472233a41073', 8, 25.80, 35.80, 'cancelled', NULL, NULL, 'alipay', NULL, 10.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '用户删除', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-11 16:50:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-11 16:44:32', '2025-06-11 16:52:25', 5);
INSERT INTO `order` VALUES (244, 'OD174963196772928ba63', 8, 358.00, 358.00, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-11 16:58:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-11 16:52:48', '2025-06-11 16:58:00', 2);
INSERT INTO `order` VALUES (245, 'OD1749632308025fbdbf2', 8, 189.00, 189.00, 'cancelled', 274, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-11 17:04:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-11 16:58:28', '2025-06-11 17:04:00', 3);
INSERT INTO `order` VALUES (246, 'OD1749632756130cc4d79', 8, 179.00, 179.00, 'pending_shipment', 275, '2025-06-11 17:06:37', 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-06-11 17:05:56', '2025-06-11 17:06:37', 2);
INSERT INTO `order` VALUES (247, 'OD174963284047489364a', 8, 109.00, 109.00, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-11 17:13:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-11 17:07:20', '2025-06-11 17:13:00', 2);
INSERT INTO `order` VALUES (248, 'OD1749633399671293c3f', 8, 29.80, 39.80, 'pending_shipment', 276, '2025-06-11 17:16:49', 'wechat', NULL, 10.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-06-11 17:16:40', '2025-06-11 17:16:49', 2);
INSERT INTO `order` VALUES (249, 'OD1749633587969adf18d', 8, 388.00, 388.00, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-11 17:25:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-11 17:19:48', '2025-06-11 17:25:00', 2);
INSERT INTO `order` VALUES (250, 'OD1749634467342a29476', 8, 358.00, 358.00, 'cancelled', NULL, NULL, 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-11 17:40:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-11 17:34:27', '2025-06-11 17:40:00', 2);
INSERT INTO `order` VALUES (251, 'OD1749634872421a79c8d', 8, 29.80, 39.80, 'cancelled', NULL, NULL, 'alipay', NULL, 10.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '用户删除', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-06-11 17:47:00', '订单超时未支付，系统自动取消', 0, 0, '2025-06-11 17:41:12', '2025-06-13 22:57:30', 3);
INSERT INTO `order` VALUES (252, 'OD1749635425602acc22f', 8, 179.00, 179.00, 'pending_shipment', 277, '2025-06-11 17:54:43', 'wallet', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-06-11 17:50:26', '2025-06-11 17:54:43', 2);
INSERT INTO `order` VALUES (253, 'OD1749726408755f094d7', 8, 378.00, 378.00, 'refunded', 278, '2025-06-12 19:08:17', 'alipay', NULL, 0.00, 5, '彭伟株', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-06-12 19:06:49', '2025-06-12 19:08:17', 4);
INSERT INTO `order` VALUES (254, 'OD175099804314016fc06', 8, 388.00, 398.00, 'refunded', 279, '2025-06-27 12:20:49', 'wallet', NULL, 10.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-06-27 12:20:43', '2025-06-27 12:20:49', 4);
INSERT INTO `order` VALUES (255, 'OD175204518724432e360', 8, 899.00, 899.00, 'refunded', 280, '2025-07-09 15:13:12', 'wallet', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-07-09 15:13:07', '2025-07-09 15:13:12', 4);
INSERT INTO `order` VALUES (256, 'OD1752064333335be2d08', 9, 2580.00, 2580.00, 'completed', 281, '2025-07-09 20:32:19', 'wallet', NULL, 0.00, 7, '王五', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-07-09 20:32:49', 'YT2507092032483667', '圆通速递', '2025-07-09 20:32:56', NULL, NULL, 0, 0, '2025-07-09 20:32:13', '2025-07-09 20:32:56', 4);
INSERT INTO `order` VALUES (257, 'OD1752065959966ad2742', 9, 179.00, 179.00, 'pending_shipment', 282, '2025-07-09 20:59:27', 'wallet', NULL, 0.00, 9, '李四', '13666522356', '上海', '上海市', '徐汇区', '地中海10086号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-07-09 20:59:20', '2025-07-09 20:59:26', 2);
INSERT INTO `order` VALUES (258, 'OD17521295188585571b8', 59, 1880.00, 1880.00, 'cancelled', 284, NULL, 'alipay', NULL, 0.00, 18, 'pwz', '15222566568', '安徽省', '合肥市', '包河区', '西湖区10086号', '', '未支付已自动取消', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, '2025-07-10 14:44:00', '订单超时未支付，系统自动取消', 0, 0, '2025-07-10 14:38:39', '2025-07-10 14:44:00', 4);
INSERT INTO `order` VALUES (259, 'OD175213011582428c5fe', 59, 119.00, 119.00, 'completed', 285, '2025-07-10 14:49:11', 'alipay', NULL, 0.00, 18, 'pwz', '15222566568', '安徽省', '合肥市', '包河区', '西湖区10086号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-07-10 14:49:41', 'EMS2507101449400205', 'EMS快递', '2025-07-10 16:57:54', NULL, NULL, 0, 0, '2025-07-10 14:48:36', '2025-07-10 16:57:54', 4);
INSERT INTO `order` VALUES (260, 'OD1752210905878845ae7', 8, 79.90, 89.90, 'refunded', 286, '2025-07-11 13:15:46', 'alipay', NULL, 10.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, '2025-07-11 13:16:11', 'ZT2507111316109731', '中通快递', '2025-07-11 13:16:38', NULL, NULL, 1, 0, '2025-07-11 13:15:06', '2025-07-11 13:17:03', 6);
INSERT INTO `order` VALUES (261, 'OD1752217759425d955cc', 9, 378.00, 378.00, 'pending_shipment', 287, '2025-07-11 15:09:56', 'alipay', NULL, 0.00, 9, '李四', '13666522356', '上海', '上海市', '徐汇区', '地中海10086号', '', '', 0.00, 18, 50.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-07-11 15:09:19', '2025-07-11 15:09:55', 2);
INSERT INTO `order` VALUES (262, 'OD1752218035911b8d529', 9, 180.00, 130.00, 'pending_shipment', 288, '2025-07-11 15:14:40', 'alipay', NULL, 0.00, 7, '王五', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, 20, 50.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-07-11 15:13:56', '2025-07-11 15:14:40', 2);
INSERT INTO `order` VALUES (263, 'OD175221820097485ae4b', 9, 1880.00, 1830.00, 'refunded', 290, '2025-07-11 15:17:49', 'alipay', NULL, 0.00, 7, '王五', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', '', 0.00, 21, 50.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-07-11 15:16:41', '2025-07-11 15:17:49', 5);
INSERT INTO `order` VALUES (264, 'OD1752661153782d5f325', 8, 399.90, 399.90, 'refunded', 291, '2025-07-16 18:19:23', 'wechat', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-07-16 18:19:14', '2025-07-16 18:19:22', 4);
INSERT INTO `order` VALUES (265, 'OD1752663366489bad395', 8, 179.00, 179.00, 'refunding', 292, '2025-07-16 18:56:11', 'wallet', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-07-16 18:56:06', '2025-07-16 18:56:10', 3);
INSERT INTO `order` VALUES (266, 'OD1758185044441fc2a2a', 8, 189.90, 189.90, 'pending_shipment', 294, '2025-09-18 16:45:31', 'alipay', NULL, 0.00, 5, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-09-18 16:44:04', '2025-09-18 16:45:31', 3);
INSERT INTO `order` VALUES (267, 'OD17582551185296f4f05', 8, 1074.00, 1074.00, 'pending_shipment', 295, '2025-09-19 12:12:17', 'wechat', NULL, 0.00, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', '', 0.00, NULL, 0.00, 0.00, 0, 0.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '2025-09-19 12:11:59', '2025-09-19 12:12:19', 2);

-- ----------------------------
-- Table structure for order_product
-- ----------------------------
DROP TABLE IF EXISTS `order_product`;
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
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 108 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_product
-- ----------------------------
INSERT INTO `order_product` VALUES (1, 1, 10, '惠氏启赋有机婴儿配方奶粉1段（0-6个月）', 'goods10.jpg', 298.00, 2, '{\"规格\": \"400g\", \"礼盒装\": \"是\"}', '2025-03-05 21:58:47', '2025-03-11 14:22:04');
INSERT INTO `order_product` VALUES (2, 2, 11, '帮宝适超薄干爽纸尿裤M码（6-11kg）', 'goods11.jpg', 139.00, 1, '{\"包装\": \"中包装（56片）\"}', '2025-03-05 21:58:47', '2025-03-11 14:22:10');
INSERT INTO `order_product` VALUES (3, 3, 12, '花王妙而舒纸尿裤L码（9-14kg）', 'goods12.jpg', 149.00, 1, '{\"包装\": \"大包装（76片）\"}', '2025-03-06 13:58:47', '2025-03-11 14:22:11');
INSERT INTO `order_product` VALUES (4, 4, 13, '惠氏启赋有机婴儿配方奶粉1段（0-6个月）', 'goods13.jpg', 298.00, 1, '{\"规格\": \"400g\", \"礼盒装\": \"否\"}', '2025-03-05 14:58:47', '2025-03-11 14:22:13');
INSERT INTO `order_product` VALUES (5, 71, 49, '儿童绘本套装', 'goods49.jpg', 159.90, 1, NULL, '2025-04-18 22:08:06', '2025-04-18 22:08:06');
INSERT INTO `order_product` VALUES (6, 75, 49, '儿童绘本套装', 'goods49.jpg', 159.90, 1, NULL, '2025-04-19 14:28:49', '2025-04-19 14:28:49');
INSERT INTO `order_product` VALUES (7, 85, 59, '孕妇维生素', 'goods59.jpg', 189.90, 2, '\"规格:默认\"', '2025-04-19 17:27:06', '2025-04-19 17:27:06');
INSERT INTO `order_product` VALUES (8, 115, 49, '儿童绘本套装', 'goods49.jpg', 159.90, 1, NULL, '2025-04-21 13:41:36', '2025-04-21 13:41:36');
INSERT INTO `order_product` VALUES (9, 131, 44, '婴儿指甲剪', 'goods44.jpg', 29.90, 1, NULL, '2025-04-21 15:46:07', '2025-04-21 15:46:07');
INSERT INTO `order_product` VALUES (10, 135, 49, '儿童绘本套装', 'goods49.jpg', 159.90, 1, NULL, '2025-04-21 20:57:01', '2025-04-21 20:57:01');
INSERT INTO `order_product` VALUES (11, 166, 50, '儿童识字卡片', 'goods50.jpg', 59.99, 1, NULL, '2025-04-22 20:28:16', '2025-04-22 20:28:16');
INSERT INTO `order_product` VALUES (12, 184, 56, '婴儿音乐床铃', 'goods56.jpg', 129.90, 1, NULL, '2025-05-05 18:35:34', '2025-05-05 18:35:34');
INSERT INTO `order_product` VALUES (13, 184, 57, '儿童智能手表', 'goods57.jpg', 299.90, 1, NULL, '2025-05-05 18:35:34', '2025-05-05 18:35:34');
INSERT INTO `order_product` VALUES (14, 184, 59, '孕妇维生素', 'goods59.jpg', 189.90, 1, NULL, '2025-05-05 18:35:34', '2025-05-05 18:35:34');
INSERT INTO `order_product` VALUES (15, 185, 58, '儿童防蓝光眼镜', 'goods48.jpg', 99.90, 1, NULL, '2025-05-05 18:41:12', '2025-05-05 18:41:12');
INSERT INTO `order_product` VALUES (16, 186, 53, '儿童户外玩具', 'goods53.jpg', 199.90, 1, NULL, '2025-05-05 20:40:53', '2025-05-05 20:40:53');
INSERT INTO `order_product` VALUES (17, 186, 54, '儿童电子学习机', 'goods54.jpg', 399.90, 1, NULL, '2025-05-05 20:40:53', '2025-05-05 20:40:53');
INSERT INTO `order_product` VALUES (18, 187, 49, '儿童绘本套装', 'goods49.jpg', 159.90, 1, NULL, '2025-05-05 21:26:43', '2025-05-05 21:26:43');
INSERT INTO `order_product` VALUES (19, 187, 50, '儿童识字卡片', 'goods50.jpg', 59.99, 1, NULL, '2025-05-05 21:26:43', '2025-05-05 21:26:43');
INSERT INTO `order_product` VALUES (20, 187, 53, '儿童户外玩具', 'goods53.jpg', 199.90, 1, NULL, '2025-05-05 21:26:43', '2025-05-05 21:26:43');
INSERT INTO `order_product` VALUES (21, 187, 55, '婴儿摇铃玩具', 'goods55.jpg', 49.90, 1, NULL, '2025-05-05 21:26:43', '2025-05-05 21:26:43');
INSERT INTO `order_product` VALUES (22, 187, 56, '婴儿音乐床铃', 'goods56.jpg', 129.90, 1, NULL, '2025-05-05 21:26:43', '2025-05-05 21:26:43');
INSERT INTO `order_product` VALUES (23, 187, 58, '儿童防蓝光眼镜', 'goods48.jpg', 99.90, 2, NULL, '2025-05-05 21:26:43', '2025-05-05 21:26:43');
INSERT INTO `order_product` VALUES (24, 187, 59, '孕妇维生素', 'goods59.jpg', 189.90, 1, NULL, '2025-05-05 21:26:43', '2025-05-05 21:26:43');
INSERT INTO `order_product` VALUES (25, 187, 60, '产后修复套装', 'goods60.jpg', 399.90, 1, NULL, '2025-05-05 21:26:43', '2025-05-05 21:26:43');
INSERT INTO `order_product` VALUES (26, 188, 42, '婴儿润肤乳', 'goods42.jpg', 79.90, 1, NULL, '2025-05-06 10:06:39', '2025-05-06 10:06:39');
INSERT INTO `order_product` VALUES (27, 189, 47, '儿童玩具车', 'goods47.jpg', 129.90, 1, NULL, '2025-05-06 17:52:59', '2025-05-06 17:52:59');
INSERT INTO `order_product` VALUES (28, 189, 43, '儿童牙膏', 'goods43.jpg', 39.90, 1, NULL, '2025-05-06 17:52:59', '2025-05-06 17:52:59');
INSERT INTO `order_product` VALUES (29, 190, 55, '婴儿摇铃玩具', 'goods55.jpg', 49.90, 1, NULL, '2025-05-06 18:08:27', '2025-05-06 18:08:27');
INSERT INTO `order_product` VALUES (30, 191, 7, '费雪声光安抚海马', 'goods7.jpg', 129.00, 1, NULL, '2025-05-06 18:22:25', '2025-05-06 18:22:25');
INSERT INTO `order_product` VALUES (31, 191, 5, '花王妙而舒纸尿裤L码（9-14kg）', 'goods5.jpg', 149.00, 1, NULL, '2025-05-06 18:22:25', '2025-05-06 18:22:25');
INSERT INTO `order_product` VALUES (32, 192, 60, '产后修复套装', 'goods60.jpg', 399.90, 1, NULL, '2025-05-06 20:29:27', '2025-05-06 20:29:27');
INSERT INTO `order_product` VALUES (33, 193, 9, '舒儿适婴儿抚触油', 'goods9.jpg', 89.90, 1, NULL, '2025-05-06 21:28:47', '2025-05-06 21:28:47');
INSERT INTO `order_product` VALUES (34, 194, 53, '儿童户外玩具', 'goods53.jpg', 199.90, 1, NULL, '2025-05-06 21:39:57', '2025-05-06 21:39:57');
INSERT INTO `order_product` VALUES (35, 195, 2, '婴儿奶粉）', 'goods2.jpg', 328.00, 1, NULL, '2025-05-06 22:13:05', '2025-05-06 22:13:05');
INSERT INTO `order_product` VALUES (36, 196, 19, '孕妇护肤品', 'goods19.jpg', 159.90, 1, NULL, '2025-05-06 22:24:51', '2025-05-06 22:24:51');
INSERT INTO `order_product` VALUES (37, 197, 23, '孕妇奶粉', 'goods23.jpg', 229.90, 1, NULL, '2025-05-06 22:31:38', '2025-05-06 22:31:38');
INSERT INTO `order_product` VALUES (38, 198, 16, '儿童玩具', 'goods16.jpg', 49.90, 1, NULL, '2025-05-06 22:54:10', '2025-05-06 22:54:10');
INSERT INTO `order_product` VALUES (39, 199, 15, '婴儿推车', 'goods15.jpg', 699.90, 1, NULL, '2025-05-06 22:57:54', '2025-05-06 22:57:54');
INSERT INTO `order_product` VALUES (40, 200, 50, '儿童识字卡片', 'goods50.jpg', 59.99, 1, NULL, '2025-05-07 21:09:27', '2025-05-07 21:09:27');
INSERT INTO `order_product` VALUES (41, 201, 56, '婴儿音乐床铃', 'goods56.jpg', 129.90, 1, NULL, '2025-05-07 21:42:10', '2025-05-07 21:42:10');
INSERT INTO `order_product` VALUES (42, 202, 59, '孕妇维生素', 'goods59.jpg', 189.90, 1, NULL, '2025-05-07 22:21:38', '2025-05-07 22:21:38');
INSERT INTO `order_product` VALUES (43, 203, 52, '儿童拼图游戏', 'goods52.jpg', 79.90, 1, NULL, '2025-05-08 12:58:37', '2025-05-08 12:58:37');
INSERT INTO `order_product` VALUES (44, 204, 60, '产后修复套装', 'goods60.jpg', 399.90, 1, NULL, '2025-05-08 13:11:10', '2025-05-08 13:11:10');
INSERT INTO `order_product` VALUES (45, 205, 56, '婴儿音乐床铃', 'goods56.jpg', 129.90, 1, NULL, '2025-05-08 13:17:23', '2025-05-08 13:17:23');
INSERT INTO `order_product` VALUES (46, 206, 58, '儿童防蓝光眼镜', 'goods48.jpg', 99.90, 3, NULL, '2025-05-09 21:19:27', '2025-05-09 21:19:27');
INSERT INTO `order_product` VALUES (47, 207, 56, '婴儿音乐床铃', 'goods56.jpg', 129.90, 2, NULL, '2025-05-09 22:07:43', '2025-05-09 22:07:43');
INSERT INTO `order_product` VALUES (48, 208, 54, '儿童电子学习机', 'goods54.jpg', 399.90, 1, NULL, '2025-05-09 22:44:41', '2025-05-09 22:44:41');
INSERT INTO `order_product` VALUES (49, 208, 59, '孕妇维生素', 'goods59.jpg', 189.90, 1, NULL, '2025-05-09 22:44:41', '2025-05-09 22:44:41');
INSERT INTO `order_product` VALUES (50, 209, 23, '孕妇奶粉', 'goods23.jpg', 229.90, 1, NULL, '2025-05-10 22:57:47', '2025-05-10 22:57:47');
INSERT INTO `order_product` VALUES (51, 209, 21, '孕妇装', 'goods21.jpg', 199.90, 1, NULL, '2025-05-10 22:57:47', '2025-05-10 22:57:47');
INSERT INTO `order_product` VALUES (52, 210, 49, '儿童绘本套装', 'goods49.jpg', 159.90, 1, NULL, '2025-05-10 23:05:29', '2025-05-10 23:05:29');
INSERT INTO `order_product` VALUES (53, 211, 56, '婴儿音乐床铃', 'goods56.jpg', 129.90, 2, NULL, '2025-05-11 10:01:58', '2025-05-11 10:01:58');
INSERT INTO `order_product` VALUES (54, 212, 52, '儿童拼图游戏', 'goods52.jpg', 79.90, 1, NULL, '2025-05-11 12:04:05', '2025-05-11 12:04:05');
INSERT INTO `order_product` VALUES (55, 213, 54, '儿童电子学习机', 'goods54.jpg', 399.90, 1, NULL, '2025-05-11 13:03:27', '2025-05-11 13:03:27');
INSERT INTO `order_product` VALUES (56, 214, 59, '孕妇维生素', 'goods59.jpg', 189.90, 1, NULL, '2025-05-11 13:19:46', '2025-05-11 13:19:46');
INSERT INTO `order_product` VALUES (57, 215, 43, '儿童牙膏', 'goods43.jpg', 39.90, 1, NULL, '2025-05-11 13:49:13', '2025-05-11 13:49:13');
INSERT INTO `order_product` VALUES (58, 216, 55, '婴儿摇铃玩具', 'goods55.jpg', 49.90, 1, NULL, '2025-05-11 14:04:44', '2025-05-11 14:04:44');
INSERT INTO `order_product` VALUES (59, 217, 46, '婴儿护臀膏', 'goods46.jpg', 59.90, 1, NULL, '2025-05-11 14:15:59', '2025-05-11 14:15:59');
INSERT INTO `order_product` VALUES (60, 218, 47, '儿童玩具车', 'goods47.jpg', 129.90, 1, NULL, '2025-05-12 22:31:00', '2025-05-12 22:31:00');
INSERT INTO `order_product` VALUES (61, 219, 60, '产后修复套装', 'goods60.jpg', 399.90, 1, NULL, '2025-05-12 22:35:15', '2025-05-12 22:35:15');
INSERT INTO `order_product` VALUES (62, 220, 36, '儿童牙刷套装', 'goods36.jpg', 49.90, 1, NULL, '2025-05-12 22:40:00', '2025-05-12 22:40:00');
INSERT INTO `order_product` VALUES (63, 221, 24, '孕妇内衣', 'goods24.jpg', 129.90, 1, NULL, '2025-05-15 12:59:06', '2025-05-15 12:59:06');
INSERT INTO `order_product` VALUES (64, 222, 65, '贝亲婴儿柔湿巾80抽x6包', 'goods65.jpg', 69.90, 1, NULL, '2025-05-15 22:48:42', '2025-05-15 22:48:42');
INSERT INTO `order_product` VALUES (65, 225, 60, '产后修复套装', 'goods60.jpg', 399.90, 1, '{\"类型\": \"孕中\"}', '2025-05-21 22:53:48', '2025-05-21 22:53:48');
INSERT INTO `order_product` VALUES (66, 226, 66, '嘉宝星星泡芙香蕉味', 'goods66.jpg', 25.80, 1, NULL, '2025-05-22 15:21:06', '2025-05-22 15:21:06');
INSERT INTO `order_product` VALUES (67, 227, 119, '帮宝适拉拉裤L号', 'goods119.jpg', 199.00, 1, '{\"规格\": \"默认\"}', '2025-05-22 16:56:39', '2025-05-22 16:56:39');
INSERT INTO `order_product` VALUES (68, 228, 61, '爱他美卓萃有机婴儿配方奶粉1段', 'goods61.jpg', 358.00, 2, '{\"规格\": \"默认\"}', '2025-05-22 21:55:01', '2025-05-22 21:55:01');
INSERT INTO `order_product` VALUES (69, 229, 63, '飞利浦新安怡宽口径玻璃奶瓶', 'goods63.jpg', 129.00, 1, NULL, '2025-05-24 19:19:13', '2025-05-24 19:19:13');
INSERT INTO `order_product` VALUES (70, 230, 119, '帮宝适拉拉裤L号', 'goods119.jpg', 199.00, 1, '{\"规格\": \"默认\"}', '2025-05-24 19:32:46', '2025-05-24 19:32:46');
INSERT INTO `order_product` VALUES (71, 231, 77, '惠氏S-26铂臻3段幼儿配方奶粉', 'goods77.jpg', 378.00, 2, NULL, '2025-05-28 22:15:32', '2025-05-28 22:15:32');
INSERT INTO `order_product` VALUES (72, 232, 57, '儿童智能手表', 'goods57.jpg', 299.90, 1, '{\"颜色\": \"粉色;尺寸\"}', '2025-05-29 23:23:54', '2025-05-29 23:23:54');
INSERT INTO `order_product` VALUES (73, 233, 68, '美素佳儿皇家美素佳儿3段幼儿配方奶粉', 'goods68.jpg', 368.00, 2, NULL, '2025-05-29 23:25:18', '2025-05-29 23:25:18');
INSERT INTO `order_product` VALUES (74, 234, 89, '帮宝适特级棉柔纸尿裤M号', 'goods89.jpg', 179.00, 1, '{\"规格\": \"默认\"}', '2025-05-30 11:50:52', '2025-05-30 11:50:52');
INSERT INTO `order_product` VALUES (75, 235, 80, '花王纸尿裤NB号', 'goods80.jpg', 169.00, 1, '{\"规格\": \"默认\"}', '2025-06-06 09:11:13', '2025-06-06 09:11:13');
INSERT INTO `order_product` VALUES (76, 236, 60, '产后修复套装', 'goods60.jpg', 399.90, 2, '{\"类型\": \"孕中\"}', '2025-06-06 09:21:14', '2025-06-06 09:21:14');
INSERT INTO `order_product` VALUES (77, 237, 60, '产后修复套装', 'goods60.jpg', 399.90, 1, NULL, '2025-06-06 13:35:54', '2025-06-06 13:35:54');
INSERT INTO `order_product` VALUES (78, 238, 80, '花王纸尿裤NB号', 'goods80.jpg', 169.00, 1, '{}', '2025-06-10 19:48:24', '2025-06-10 19:48:24');
INSERT INTO `order_product` VALUES (79, 239, 2, '婴儿奶粉）', 'goods2.jpg', 328.00, 1, NULL, '2025-06-11 14:22:06', '2025-06-11 14:22:06');
INSERT INTO `order_product` VALUES (80, 240, 64, '美德乐丝韵翼双边电动吸奶器', 'goods64.jpg', 2580.00, 1, NULL, '2025-06-11 16:19:10', '2025-06-11 16:19:10');
INSERT INTO `order_product` VALUES (81, 241, 3, '有机婴儿配方奶粉3段（1-3岁）', 'goods3.jpg', 358.00, 1, NULL, '2025-06-11 16:27:08', '2025-06-11 16:27:08');
INSERT INTO `order_product` VALUES (82, 242, 12, '儿童套装', 'goods12.jpg', 129.90, 1, NULL, '2025-06-11 16:37:52', '2025-06-11 16:37:52');
INSERT INTO `order_product` VALUES (83, 243, 66, '嘉宝星星泡芙香蕉味', 'goods66.jpg', 25.80, 1, '{}', '2025-06-11 16:44:32', '2025-06-11 16:44:32');
INSERT INTO `order_product` VALUES (84, 244, 98, '美素佳儿金装较大婴儿配方奶粉2段', 'goods98.jpg', 358.00, 1, '{}', '2025-06-11 16:52:48', '2025-06-11 16:52:48');
INSERT INTO `order_product` VALUES (85, 245, 69, '帮宝适一级帮纸尿裤M号', 'goods69.jpg', 189.00, 1, '{}', '2025-06-11 16:58:28', '2025-06-11 16:58:28');
INSERT INTO `order_product` VALUES (86, 246, 100, '花王纸尿裤S号', 'goods100.jpg', 179.00, 1, '{}', '2025-06-11 17:05:56', '2025-06-11 17:05:56');
INSERT INTO `order_product` VALUES (87, 247, 93, '飞利浦新安怡自然原生系列玻璃奶瓶', 'goods93.jpg', 109.00, 1, '{}', '2025-06-11 17:07:20', '2025-06-11 17:07:20');
INSERT INTO `order_product` VALUES (88, 248, 96, '嘉宝磨牙饼干香蕉味', 'goods96.jpg', 29.80, 1, '{}', '2025-06-11 17:16:40', '2025-06-11 17:16:40');
INSERT INTO `order_product` VALUES (89, 249, 97, '惠氏S-26铂臻2段较大婴儿配方奶粉', 'goods97.jpg', 388.00, 1, '{}', '2025-06-11 17:19:48', '2025-06-11 17:19:48');
INSERT INTO `order_product` VALUES (90, 250, 98, '美素佳儿金装较大婴儿配方奶粉2段', 'goods98.jpg', 358.00, 1, '{}', '2025-06-11 17:34:27', '2025-06-11 17:34:27');
INSERT INTO `order_product` VALUES (91, 251, 96, '嘉宝磨牙饼干香蕉味', 'goods96.jpg', 29.80, 1, '{}', '2025-06-11 17:41:12', '2025-06-11 17:41:12');
INSERT INTO `order_product` VALUES (92, 252, 89, '帮宝适特级棉柔纸尿裤M号', 'goods89.jpg', 179.00, 1, '{}', '2025-06-11 17:50:26', '2025-06-11 17:50:26');
INSERT INTO `order_product` VALUES (93, 253, 77, '惠氏S-26铂臻3段幼儿配方奶粉', 'goods77.jpg', 378.00, 1, '{}', '2025-06-12 19:06:49', '2025-06-12 19:06:49');
INSERT INTO `order_product` VALUES (94, 254, 91, '爱他美卓萃婴儿配方奶粉2段', 'goods91.jpg', 388.00, 1, '{\"规格\": \"默认\"}', '2025-06-27 12:20:43', '2025-06-27 12:20:43');
INSERT INTO `order_product` VALUES (95, 255, 83, '飞利浦新安怡婴儿辅食机', 'goods83.jpg', 899.00, 1, NULL, '2025-07-09 15:13:07', '2025-07-09 15:13:07');
INSERT INTO `order_product` VALUES (96, 256, 94, '美德乐丝韵翼双边电动吸奶器', 'goods94.jpg', 2580.00, 1, '{\"规格\": \"默认\"}', '2025-07-09 20:32:13', '2025-07-09 20:32:13');
INSERT INTO `order_product` VALUES (97, 257, 70, '花王妙而舒纸尿裤L号', 'goods70.jpg', 179.00, 1, NULL, '2025-07-09 20:59:20', '2025-07-09 20:59:20');
INSERT INTO `order_product` VALUES (98, 258, 84, '美德乐丝韵单边电动吸奶器', 'goods84.jpg', 1880.00, 1, '{\"规格\": \"默认\"}', '2025-07-10 14:38:39', '2025-07-10 14:38:39');
INSERT INTO `order_product` VALUES (99, 259, 85, '贝亲宽口径PPSU奶瓶', 'goods85.jpg', 119.00, 1, '{\"规格\": \"默认\"}', '2025-07-10 14:48:36', '2025-07-10 14:48:36');
INSERT INTO `order_product` VALUES (100, 260, 95, '贝亲婴儿洗发沐浴露二合一', 'goods95.jpg', 79.90, 1, NULL, '2025-07-11 13:15:06', '2025-07-11 13:15:06');
INSERT INTO `order_product` VALUES (101, 261, 77, '惠氏S-26铂臻3段幼儿配方奶粉', 'goods77.jpg', 378.00, 1, NULL, '2025-07-11 15:09:19', '2025-07-11 15:09:19');
INSERT INTO `order_product` VALUES (102, 262, 104, '美德乐吸奶器配件', 'goods104.jpg', 180.00, 1, '{\"规格\": \"默认\"}', '2025-07-11 15:13:56', '2025-07-11 15:13:56');
INSERT INTO `order_product` VALUES (103, 263, 84, '美德乐丝韵单边电动吸奶器', 'goods84.jpg', 1880.00, 1, '{\"规格\": \"默认\"}', '2025-07-11 15:16:41', '2025-07-11 15:16:41');
INSERT INTO `order_product` VALUES (104, 264, 60, '产后修复套装', 'goods60.jpg', 399.90, 1, '{\"类型\": \"孕中\"}', '2025-07-16 18:19:14', '2025-07-16 18:19:14');
INSERT INTO `order_product` VALUES (105, 265, 100, '花王纸尿裤S号', 'goods100.jpg', 179.00, 1, '{\"规格\": \"默认\"}', '2025-07-16 18:56:06', '2025-07-16 18:56:06');
INSERT INTO `order_product` VALUES (106, 266, 59, '孕妇维生素', 'goods59.jpg', 189.90, 1, '{\"规格\": \"标准型\"}', '2025-09-18 16:44:04', '2025-09-18 16:44:04');
INSERT INTO `order_product` VALUES (107, 267, 98, '美素佳儿金装较大婴儿配方奶粉2段', 'goods98.jpg', 358.00, 3, NULL, '2025-09-19 12:11:59', '2025-09-19 12:11:59');

-- ----------------------------
-- Table structure for order_state_log
-- ----------------------------
DROP TABLE IF EXISTS `order_state_log`;
CREATE TABLE `order_state_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `order_id` int NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `old_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原状态',
  `new_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '新状态',
  `event` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发事件',
  `operator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作者',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单状态变更日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_state_log
-- ----------------------------
INSERT INTO `order_state_log` VALUES (1, 195, 'OD1746540784960366edc', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-11 09:53:00');
INSERT INTO `order_state_log` VALUES (2, 196, 'OD17465414906845dbb63', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-11 09:53:00');
INSERT INTO `order_state_log` VALUES (3, 197, 'OD174654189790984570d', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-11 09:53:00');
INSERT INTO `order_state_log` VALUES (4, 198, 'OD17465432495356e4092', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-11 09:53:00');
INSERT INTO `order_state_log` VALUES (5, 199, 'OD174654347401056de4b', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-11 09:53:00');
INSERT INTO `order_state_log` VALUES (6, 200, 'OD1746623366746e117e8', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-11 09:53:00');
INSERT INTO `order_state_log` VALUES (7, 211, 'OD17469289178677b3311', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-11 11:19:00');
INSERT INTO `order_state_log` VALUES (8, 214, 'OD1746940786288d4fce4', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-11 13:25:00');
INSERT INTO `order_state_log` VALUES (9, 215, 'OD17469425530540505e9', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-11 13:55:00');
INSERT INTO `order_state_log` VALUES (10, 216, 'OD1746943484132202f5d', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-11 14:10:00');
INSERT INTO `order_state_log` VALUES (11, 219, 'OD17470605148463a1ef5', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-12 22:41:00');
INSERT INTO `order_state_log` VALUES (12, 220, 'OD17470608003180c957b', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-12 22:45:00');
INSERT INTO `order_state_log` VALUES (13, 226, 'OD17478984664046d3f2b', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-22 15:27:00');
INSERT INTO `order_state_log` VALUES (14, 230, 'OD1748086365510710a7f', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-24 19:38:00');
INSERT INTO `order_state_log` VALUES (15, 232, 'OD1748532233832df04d1', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-30 09:06:00');
INSERT INTO `order_state_log` VALUES (16, 233, 'OD174853231755256c661', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-05-30 09:06:01');
INSERT INTO `order_state_log` VALUES (17, 234, 'OD1748577051762df19cb', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-03 16:34:00');
INSERT INTO `order_state_log` VALUES (18, 238, 'OD1749556103940f7657c', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-10 19:54:00');
INSERT INTO `order_state_log` VALUES (19, 239, 'OD1749622926447f21f6f', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-11 14:28:00');
INSERT INTO `order_state_log` VALUES (20, 240, 'OD174962995000653b73f', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-11 16:25:00');
INSERT INTO `order_state_log` VALUES (21, 241, 'OD174963042817199e343', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-11 16:33:00');
INSERT INTO `order_state_log` VALUES (22, 242, 'OD17496310719225d7722', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-11 16:43:00');
INSERT INTO `order_state_log` VALUES (23, 243, 'OD1749631472233a41073', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-11 16:50:00');
INSERT INTO `order_state_log` VALUES (24, 244, 'OD174963196772928ba63', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-11 16:58:00');
INSERT INTO `order_state_log` VALUES (25, 245, 'OD1749632308025fbdbf2', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-11 17:04:00');
INSERT INTO `order_state_log` VALUES (26, 247, 'OD174963284047489364a', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-11 17:13:00');
INSERT INTO `order_state_log` VALUES (27, 249, 'OD1749633587969adf18d', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-11 17:25:00');
INSERT INTO `order_state_log` VALUES (28, 250, 'OD1749634467342a29476', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-11 17:40:00');
INSERT INTO `order_state_log` VALUES (29, 251, 'OD1749634872421a79c8d', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-06-11 17:47:00');
INSERT INTO `order_state_log` VALUES (30, 258, 'OD17521295188585571b8', 'pending_payment', 'cancelled', 'TIMEOUT', 'system', '订单超过5分钟未支付，系统自动取消', '2025-07-10 14:44:00');

-- ----------------------------
-- Table structure for payment
-- ----------------------------
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '支付ID',
  `payment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付单号',
  `order_id` int UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付方式: alipay, wechat, bank',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态: 0-待支付 1-支付中 2-支付成功 3-支付失败 4-已关闭',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `transaction_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '第三方支付流水号',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付成功时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '支付超时时间',
  `notify_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '异步通知地址',
  `return_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '同步返回地址',
  `extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '支付附加信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁控制',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_payment_no`(`payment_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status_method`(`status` ASC, `payment_method` ASC) USING BTREE,
  INDEX `idx_version`(`version` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 296 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payment
-- ----------------------------
INSERT INTO `payment` VALUES (1, 'PAY1646547896123ABC', 1, 'MO202503051234', 8, 735.00, 'alipay', 2, NULL, 'ALI16465478961234567', '2025-03-05 22:00:00', '2025-03-05 23:58:00', 'https://api.muyingmall.com/payment/notify/alipay', 'https://www.muyingmall.com/orders/pay/return', NULL, '2025-03-05 21:59:00', '2025-03-05 22:00:00', 1);
INSERT INTO `payment` VALUES (2, 'PAY1646556096456DEF', 2, 'MO202503051235', 8, 149.00, 'wechat', 2, NULL, 'WX16465560961234567', '2025-03-06 14:00:00', '2025-03-06 15:58:00', 'https://api.muyingmall.com/payment/notify/wechat', 'https://www.muyingmall.com/orders/pay/return', NULL, '2025-03-06 13:58:47', '2025-03-06 14:00:00', 1);
INSERT INTO `payment` VALUES (3, 'PAY1646547296789GHI', 3, 'MO202503051236', 3, 298.00, 'alipay', 2, NULL, 'ALI16465472961234567', '2025-03-05 15:00:00', '2025-03-05 16:58:00', 'https://api.muyingmall.com/payment/notify/alipay', 'https://www.muyingmall.com/orders/pay/return', NULL, '2025-03-05 14:58:47', '2025-03-05 15:00:00', 1);
INSERT INTO `payment` VALUES (4, 'PAY1678083896123JKL', 4, 'MO202503051237', 5, 199.00, 'wechat', 4, NULL, NULL, NULL, '2025-03-05 15:58:00', 'https://api.muyingmall.com/payment/notify/wechat', 'https://www.muyingmall.com/orders/pay/return', NULL, '2025-03-05 13:58:00', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (5, 'PAY1678084096456MNO', 5, 'MO202503051238', 6, 499.00, 'alipay', 3, NULL, 'ALI16780840961234567', NULL, '2025-03-05 16:58:00', 'https://api.muyingmall.com/payment/notify/alipay', 'https://www.muyingmall.com/orders/pay/return', NULL, '2025-03-05 14:58:00', '2025-03-05 15:10:00', 1);
INSERT INTO `payment` VALUES (6, 'PAY2025032116182430AD7AC9', 8, 'ORDER17425450978307155', 8, 709.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 18:18:24', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 16:18:24', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (7, 'PAY20250321161841494B3335', 8, 'ORDER17425450978307155', 8, 709.00, 'wechat', 4, NULL, NULL, NULL, '2025-03-21 18:18:41', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 16:18:41', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (8, 'PAY20250321161843CEBB7E18', 8, 'ORDER17425450978307155', 8, 709.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 18:18:43', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 16:18:43', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (9, 'PAY2025032116184615E6E041', 8, 'ORDER17425450978307155', 8, 709.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 18:18:47', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 16:18:47', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (35, 'PAY2025032119045594876929', 10, 'ORDER17425550941242842', 8, 2858.00, 'alipay', 2, '2025-04-15 14:10:31', NULL, NULL, '2025-03-21 21:04:56', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:04:56', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (36, 'PAY202503211904554FA413D9', 10, 'ORDER17425550941242842', 8, 2858.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:04:56', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:04:56', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (37, 'PAY202503211913578CD1FB15', 11, 'ORDER17425555347319298', 8, 379.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:13:58', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:13:58', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (38, 'PAY202503211913581B172DEE', 11, 'ORDER17425555347319298', 8, 379.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:13:58', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:13:58', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (39, 'PAY20250321191447353F7206', 11, 'ORDER17425555347319298', 8, 379.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:14:48', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:14:48', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (40, 'PAY20250321191447B37B4C59', 11, 'ORDER17425555347319298', 8, 379.00, 'alipay', 2, '2025-04-15 14:10:31', NULL, NULL, '2025-03-21 21:14:48', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:14:48', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (41, 'PAY20250321191941AE73DF4E', 12, 'ORDER17425559766269918', 8, 938.00, 'alipay', 2, '2025-04-15 14:10:31', NULL, NULL, '2025-03-21 21:19:41', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:19:41', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (42, 'PAY2025032119194150D4E06C', 12, 'ORDER17425559766269918', 8, 938.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:19:41', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:19:41', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (43, 'PAY2025032119201187C12A31', 12, 'ORDER17425559766269918', 8, 938.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:20:12', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:20:12', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (44, 'PAY20250321192012F20629ED', 12, 'ORDER17425559766269918', 8, 938.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:20:12', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:20:12', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (45, 'PAY20250321192221B0C6CBA6', 13, 'ORDER17425560745329614', 8, 1519.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:22:22', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:22:22', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (46, 'PAY202503211922227154709C', 13, 'ORDER17425560745329614', 8, 1519.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:22:23', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:22:23', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (47, 'PAY202503211926518A3ADBFD', 14, 'ORDER17425564085578043', 8, 439.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:26:51', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:26:51', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (48, 'PAY202503211926512952793F', 14, 'ORDER17425564085578043', 8, 439.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:26:51', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:26:51', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (49, 'PAY20250321192740F6D32EBA', 14, 'ORDER17425564085578043', 8, 439.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:27:40', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:27:40', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (50, 'PAY20250321192740A6462486', 14, 'ORDER17425564085578043', 8, 439.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:27:40', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:27:40', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (51, 'PAY202503211927582C577ADF', 14, 'ORDER17425564085578043', 8, 439.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:27:58', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:27:58', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (52, 'PAY20250321192758321DE450', 14, 'ORDER17425564085578043', 8, 439.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-21 21:27:58', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-21 19:27:58', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (53, 'PAY20250324143421D31FBC19', 15, 'ORDER17427980593769997', 8, 1699.00, 'alipay', 2, '2025-04-15 14:10:31', NULL, NULL, '2025-03-24 16:34:22', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-24 14:34:22', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (54, 'PAY2025032414342174958C4F', 15, 'ORDER17427980593769997', 8, 1699.00, 'alipay', 2, '2025-04-15 14:10:31', NULL, NULL, '2025-03-24 16:34:22', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-24 14:34:22', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (55, 'PAY202503241946328B85A58E', 16, 'ORDER17428167891174429', 8, 1669.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-24 21:46:32', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-24 19:46:32', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (56, 'PAY2025032513302686A0936A', 17, 'ORDER17428806201698642', 8, 349.70, 'alipay', 4, NULL, NULL, NULL, '2025-03-25 15:30:26', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 13:30:26', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (57, 'PAY20250325133426C1A9DF12', 18, 'ORDER17428808625887930', 8, 99.90, 'alipay', 4, NULL, NULL, NULL, '2025-03-25 15:34:27', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 13:34:27', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (58, 'PAY202503251340353F60D89F', 19, 'ORDER17428812299722595', 8, 69.90, 'alipay', 4, NULL, NULL, NULL, '2025-03-25 15:40:36', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 13:40:36', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (59, 'PAY202503251343079E34BCE4', 20, 'ORDER17428813832589104', 8, 358.00, 'alipay', 4, NULL, NULL, NULL, '2025-03-25 15:43:08', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 13:43:08', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (60, 'PAY20250325184024283165D9', 1, 'ORDER17428992193116059', 8, 99.90, 'alipay', 2, NULL, NULL, '2025-03-25 20:23:15', '2025-03-25 20:40:25', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 18:40:25', '2025-03-25 20:23:15', 1);
INSERT INTO `payment` VALUES (61, 'PAY20250325202636A57532FC', 2, 'ORDER17429055908353061', 8, 259.80, 'alipay', 2, NULL, NULL, '2025-03-25 20:29:15', '2025-03-25 22:26:36', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 20:26:36', '2025-03-25 20:29:15', 1);
INSERT INTO `payment` VALUES (62, 'PAY2025032520331992D448BD', 3, 'ORDER17429059946469017', 8, 29.90, 'alipay', 2, NULL, NULL, '2025-03-25 20:33:19', '2025-03-25 22:33:19', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 20:33:19', '2025-03-25 20:33:19', 1);
INSERT INTO `payment` VALUES (63, 'PAY20250325203702CAD0550A', 4, 'ORDER17429062182848188', 8, 99.90, 'alipay', 2, NULL, NULL, '2025-03-25 20:37:03', '2025-03-25 22:37:03', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 20:37:03', '2025-03-25 20:37:03', 1);
INSERT INTO `payment` VALUES (64, 'PAY20250325203919ECE6CDD9', 5, 'ORDER17429063563726994', 8, 358.00, 'alipay', 2, NULL, NULL, '2025-03-25 20:39:20', '2025-03-25 22:39:20', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 20:39:20', '2025-03-25 20:39:20', 1);
INSERT INTO `payment` VALUES (65, 'PAY20250325204200D5605C4D', 6, 'ORDER17429065185254784', 8, 69.90, 'alipay', 2, NULL, NULL, '2025-03-25 20:42:12', '2025-03-25 22:42:00', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 20:42:00', '2025-03-25 20:42:12', 1);
INSERT INTO `payment` VALUES (66, 'PAY20250325204537B40DA2CE', 7, 'ORDER17429067366344955', 8, 179.80, 'alipay', 2, NULL, NULL, '2025-03-25 20:45:38', '2025-03-25 22:45:38', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 20:45:38', '2025-03-25 20:45:38', 1);
INSERT INTO `payment` VALUES (67, 'PAY2025032520485647BD7F5D', 8, 'ORDER17429069358481384', 8, 99.90, 'alipay', 2, NULL, NULL, '2025-03-25 20:51:03', '2025-03-25 22:48:57', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-25 20:48:57', '2025-03-25 20:51:03', 1);
INSERT INTO `payment` VALUES (68, 'PAY2025032619455633F71ACA', 9, 'ORDER17429895540598177', 8, 3349.00, 'wechat', 2, NULL, NULL, '2025-03-26 19:46:01', '2025-03-26 21:45:57', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-26 19:45:57', '2025-03-26 19:46:01', 1);
INSERT INTO `payment` VALUES (69, 'PAY202503281509089C988A29', 10, 'ORDER17431449048035508', 9, 189.90, 'alipay', 2, NULL, NULL, '2025-03-28 15:09:09', '2025-03-28 17:09:08', NULL, NULL, NULL, '2025-03-28 15:09:08', '2025-03-28 15:09:09', 1);
INSERT INTO `payment` VALUES (70, 'PAY20250330124807B51EC3F0', 11, 'ORDER17433100841148442', 9, 379.80, 'wechat', 2, NULL, NULL, '2025-03-30 12:48:12', '2025-03-30 14:48:07', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-30 12:48:07', '2025-03-30 12:48:12', 1);
INSERT INTO `payment` VALUES (71, 'PAY20250330215827976096E1', 12, 'ORDER17433427340697529', 9, 59.90, 'wechat', 2, '2025-03-30 22:04:09', 'mock_prepay_PAY20250330215827976096E1', NULL, '2025-03-30 23:58:27', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-30 21:58:27', '2025-03-30 22:04:09', 1);
INSERT INTO `payment` VALUES (72, 'PAY20250330220602059B0B1B', 13, 'ORDER17433435585039867', 9, 159.90, 'wechat', 2, '2025-03-30 22:17:54', 'mock_prepay_PAY20250330220602059B0B1B', NULL, '2025-03-31 00:06:02', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-30 22:06:02', '2025-03-30 22:17:54', 1);
INSERT INTO `payment` VALUES (73, 'PAY202503311942041B3C57AC', 14, 'ORDER17434213216393884', 9, 2239.20, 'alipay', 2, '2025-03-31 19:42:04', NULL, NULL, '2025-03-31 21:42:04', NULL, 'http://localhost:5173/payment/result', NULL, '2025-03-31 19:42:04', '2025-03-31 19:42:04', 1);
INSERT INTO `payment` VALUES (74, 'PAY20250411093124680C193B', 15, 'ORDER17443350818553567', 9, 10.00, 'alipay', 2, '2025-04-11 09:31:25', NULL, NULL, '2025-04-11 11:31:25', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-11 09:31:25', '2025-04-11 09:31:25', 1);
INSERT INTO `payment` VALUES (75, 'PAY20250411100121E286958D', 16, 'ORDER17443368784063579', 9, 189.90, 'wechat', 2, '2025-04-11 10:01:39', 'mock_prepay_PAY20250411100121E286958D', NULL, '2025-04-11 12:01:21', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-11 10:01:21', '2025-04-11 10:01:39', 1);
INSERT INTO `payment` VALUES (76, 'PAY20250411152340F5CCCB7A', 21, 'ORDER17443551411937606', 8, 139.00, 'alipay', 2, '2025-04-11 15:23:40', NULL, NULL, '2025-04-11 17:23:40', NULL, NULL, NULL, '2025-04-11 15:23:40', '2025-04-11 15:23:40', 1);
INSERT INTO `payment` VALUES (77, 'PAY202504111526355E18B07C', 23, 'ORDER17443563929058771', 8, 298.00, 'alipay', 2, '2025-04-11 15:26:36', NULL, NULL, '2025-04-11 17:26:36', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-11 15:26:36', '2025-04-11 15:26:36', 1);
INSERT INTO `payment` VALUES (78, 'PAY20250411213231C9EE3F14', 24, 'ORDER17443783487882645', 9, 189.90, 'wechat', 2, '2025-04-11 21:33:14', 'mock_prepay_PAY20250411213231C9EE3F14', NULL, '2025-04-11 23:32:31', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-11 21:32:31', '2025-04-11 21:33:14', 1);
INSERT INTO `payment` VALUES (79, 'PAY202504122052089A37CF2F', 29, 'ORDER17444623264241201', 9, 99.00, 'alipay', 4, NULL, NULL, NULL, '2025-04-12 22:52:09', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-12 20:52:09', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (80, 'PAY202504122058046CCFEE36', 30, 'ORDER17444626814173636', 9, 49.90, 'alipay', 4, NULL, NULL, NULL, '2025-04-12 22:58:05', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-12 20:58:05', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (81, 'PAY2025041221063813D937C0', 31, 'ORDER17444631922854686', 9, 119.00, 'alipay', 4, NULL, NULL, NULL, '2025-04-12 23:06:38', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-12 21:06:38', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (82, 'PAY20250412211301165744E4', 32, 'ORDER17444635781204469', 9, 9.90, 'alipay', 2, '2025-04-15 14:10:31', NULL, NULL, '2025-04-12 23:13:02', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-12 21:13:02', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (83, 'PAY20250412211805C7452EC7', 33, 'ORDER17444638836098885', 9, 9.90, 'alipay', 4, NULL, NULL, NULL, '2025-04-12 23:18:06', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-12 21:18:06', '2025-04-15 14:10:31', 1);
INSERT INTO `payment` VALUES (84, 'PAY20250412213548AEDAF4A9', 34, 'ORDER17444649461924919', 9, 269.90, 'alipay', 2, '2025-04-12 21:35:49', NULL, NULL, '2025-04-12 23:35:48', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-12 21:35:48', '2025-04-12 21:35:49', 1);
INSERT INTO `payment` VALUES (85, 'PAY202504132114248F2088D7', 35, 'ORDER17445500621193029', 9, 649.90, 'alipay', 2, '2025-04-13 21:23:37', NULL, NULL, '2025-04-13 23:14:24', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-13 21:14:24', '2025-04-13 21:23:37', 1);
INSERT INTO `payment` VALUES (86, 'PAY20250413211840BB254603', 36, 'ORDER17445503174596522', 9, 649.90, 'alipay', 2, '2025-04-13 21:18:41', NULL, NULL, '2025-04-13 23:18:40', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-13 21:18:40', '2025-04-13 21:18:41', 1);
INSERT INTO `payment` VALUES (87, 'PAY2025041321291792B571ED', 37, 'ORDER17445509559397100', 9, 649.90, 'alipay', 2, '2025-04-13 21:29:19', NULL, NULL, '2025-04-13 23:29:18', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-13 21:29:18', '2025-04-13 21:29:19', 1);
INSERT INTO `payment` VALUES (88, 'PAY202504132142450FB06892', 38, 'ORDER17445517632626037', 9, 248.00, 'alipay', 2, '2025-04-13 22:34:35', NULL, NULL, '2025-04-13 23:42:45', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-13 21:42:45', '2025-04-13 22:34:35', 1);
INSERT INTO `payment` VALUES (89, 'PAY2025041322360703DE50A4', 41, 'ORDER17445549654586614', 9, 379.90, 'alipay', 2, '2025-04-13 23:01:36', NULL, NULL, '2025-04-14 00:36:07', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-13 22:36:07', '2025-04-13 23:01:36', 1);
INSERT INTO `payment` VALUES (90, 'PAY20250414123856F7A69FA3', 42, 'ORDER17446055257039754', 9, 39.90, 'alipay', 2, '2025-04-14 13:02:04', NULL, NULL, '2025-04-14 14:38:57', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-14 12:38:57', '2025-04-14 13:02:04', 1);
INSERT INTO `payment` VALUES (91, 'PAY1744609410934a1a769', 43, 'ORDER17446089940392244', 9, 149.00, 'alipay', 2, '2025-04-14 13:43:33', NULL, NULL, '2025-04-14 15:43:31', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-14 13:43:31', '2025-04-14 13:43:33', 1);
INSERT INTO `payment` VALUES (92, 'PAY1744609853135c2f96a', 43, 'ORDER17446089940392244', 9, 149.00, 'alipay', 2, '2025-04-14 13:50:55', 'AP1744609853849', NULL, '2025-04-14 15:50:53', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-14 13:50:53', '2025-04-14 13:50:55', 1);
INSERT INTO `payment` VALUES (93, 'PAY1744609864814e115a2', 43, 'ORDER17446089940392244', 9, 149.00, 'alipay', 2, '2025-04-14 13:51:10', 'AP1744609864862', NULL, '2025-04-14 15:51:05', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-14 13:51:05', '2025-04-14 13:51:10', 1);
INSERT INTO `payment` VALUES (94, 'PAY17446103791794e3909', 44, 'ORDER17446103742268284', 9, 99.90, 'alipay', 2, '2025-04-14 13:59:45', 'AP1744610379322', NULL, '2025-04-14 15:59:39', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-14 13:59:39', '2025-04-14 13:59:45', 1);
INSERT INTO `payment` VALUES (95, 'PAY1744610445591e2f67a', 43, 'ORDER17446089940392244', 9, 149.00, 'alipay', 2, '2025-04-14 14:00:47', 'AP1744610446211', NULL, '2025-04-14 16:00:46', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-14 14:00:46', '2025-04-14 14:00:47', 1);
INSERT INTO `payment` VALUES (96, 'PAY17446104747886eb727', 42, 'ORDER17446055257039754', 9, 39.90, 'alipay', 2, '2025-04-14 14:01:16', 'AP1744610475402', NULL, '2025-04-14 16:01:15', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-14 14:01:15', '2025-04-14 14:01:16', 1);
INSERT INTO `payment` VALUES (97, 'PAY1744635140541479a0e', 45, 'ORDER17446351380987716', 9, 349.90, 'alipay', 2, '2025-04-14 20:52:22', 'AP1744635140789', NULL, '2025-04-14 22:52:21', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-14 20:52:21', '2025-04-14 20:52:22', 1);
INSERT INTO `payment` VALUES (98, 'PAY1744639563355a4120a', 46, 'ORDER17446395616614757', 9, 9.90, 'alipay', 2, '2025-04-14 22:06:04', 'AP1744639563405', NULL, '2025-04-15 00:06:03', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-14 22:06:03', '2025-04-14 22:06:04', 1);
INSERT INTO `payment` VALUES (99, 'PAY1744695552013ccc4bb', 47, 'ORDER17446955490614819', 9, 49.90, 'alipay', 2, '2025-04-15 13:39:19', 'AP1744695552056', NULL, '2025-04-15 15:39:12', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-15 13:39:12', '2025-04-15 13:39:19', 1);
INSERT INTO `payment` VALUES (100, 'PAY17448059050017e867e', 50, 'ORDER17448059023224799', 9, 1019.40, 'alipay', 2, '2025-04-16 20:18:27', 'AP1744805905110', NULL, '2025-04-16 22:18:25', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-16 20:18:25', '2025-04-16 20:18:27', 1);
INSERT INTO `payment` VALUES (101, 'PAY174480761376125633c', 52, 'ORDER17448076037709411', 9, 379.80, 'alipay', 2, '2025-04-16 20:46:55', 'AP1744807613797', NULL, '2025-04-16 22:46:54', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-16 20:46:54', '2025-04-16 20:46:55', 1);
INSERT INTO `payment` VALUES (102, 'PAY17448083223639d2a90', 53, 'ORDER17448083186517324', 9, 239.60, 'alipay', 2, '2025-04-16 20:58:43', NULL, NULL, '2025-04-16 22:58:42', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-16 20:58:42', '2025-04-16 20:58:43', 1);
INSERT INTO `payment` VALUES (103, 'PAY1744810677155d75bd3', 54, 'ORDER17448106746512253', 9, 379.80, 'wechat', 2, '2025-04-16 21:37:57', 'WX1744810677211', NULL, '2025-04-16 23:37:57', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-16 21:37:57', '2025-04-16 21:37:57', 1);
INSERT INTO `payment` VALUES (104, 'PAY1744939892940b89081', 55, 'ORDER17449398848975895', 9, 889.70, 'alipay', 2, '2025-04-18 09:31:51', 'AP1744939892989', NULL, '2025-04-18 11:31:33', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 09:31:33', '2025-04-18 09:31:51', 1);
INSERT INTO `payment` VALUES (105, 'PAY17449528562833edefb', 56, 'ORDER17449528547744283', 9, 59.90, 'alipay', 2, '2025-04-18 13:07:36', 'AP1744952856326', NULL, '2025-04-18 15:07:36', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 13:07:36', '2025-04-18 13:07:36', 1);
INSERT INTO `payment` VALUES (106, 'PAY1744979567121561240', 57, 'ORDER17449795558219373', 9, 0.01, 'alipay', 2, '2025-04-18 20:32:52', NULL, NULL, '2025-04-18 22:32:47', NULL, 'http://localhost:5173/test/alipay-result', NULL, '2025-04-18 20:32:47', '2025-04-18 20:32:52', 1);
INSERT INTO `payment` VALUES (107, 'PAY1744980322379f7025f', 58, 'ORDER17449803210071190', 9, 179.90, 'alipay', 2, '2025-04-18 20:45:22', 'AP1744980322420', NULL, '2025-04-18 22:45:22', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 20:45:22', '2025-04-18 20:45:22', 1);
INSERT INTO `payment` VALUES (108, 'PAY1744980744396044764', 59, 'ORDER17449807430696568', 9, 139.00, 'alipay', 4, NULL, NULL, NULL, '2025-04-18 22:52:24', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 20:52:24', '2025-04-19 14:28:22', 1);
INSERT INTO `payment` VALUES (109, 'PAY1744980752638916696', 59, 'ORDER17449807430696568', 9, 139.00, 'alipay', 4, NULL, NULL, NULL, '2025-04-18 22:52:33', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 20:52:33', '2025-04-19 14:28:22', 1);
INSERT INTO `payment` VALUES (110, 'PAY1744981347288ff2b51', 60, 'ORDER17449813436552135', 9, 89.90, 'alipay', 2, '2025-04-18 21:02:42', NULL, NULL, '2025-04-18 23:02:27', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:02:27', '2025-04-18 21:02:42', 1);
INSERT INTO `payment` VALUES (111, 'PAY174498161527571b44d', 61, 'ORDER17449816129176380', 9, 369.90, 'alipay', 2, '2025-04-19 14:28:22', NULL, NULL, '2025-04-18 23:06:55', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:06:55', '2025-04-19 14:28:22', 1);
INSERT INTO `payment` VALUES (112, 'PAY1744981733097fd9691', 61, 'ORDER17449816129176380', 9, 369.90, 'alipay', 2, '2025-04-18 21:08:53', 'AP1744981733146', NULL, '2025-04-18 23:08:53', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:08:53', '2025-04-18 21:08:53', 1);
INSERT INTO `payment` VALUES (113, 'PAY1744981956944b75f46', 62, 'ORDER17449819551205775', 9, 179.90, 'alipay', 2, '2025-04-18 21:12:37', 'AP1744981956976', NULL, '2025-04-18 23:12:37', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:12:37', '2025-04-18 21:12:37', 1);
INSERT INTO `payment` VALUES (114, 'PAY1744982012855ef24e6', 61, 'ORDER17449816129176380', 9, 369.90, 'alipay', 2, '2025-04-18 21:13:36', 'AP1744982015922', NULL, '2025-04-18 23:13:33', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:13:33', '2025-04-18 21:13:36', 1);
INSERT INTO `payment` VALUES (115, 'PAY17449820635839b643e', 63, 'ORDER17449820595288082', 9, 129.00, 'alipay', 4, NULL, NULL, NULL, '2025-04-18 23:14:24', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:14:24', '2025-04-19 14:28:22', 1);
INSERT INTO `payment` VALUES (116, 'PAY1744982937708ef03ee', 64, 'ORDER17449829297755677', 9, 179.90, 'alipay', 2, '2025-04-18 21:28:58', 'AP1744982938379', NULL, '2025-04-18 23:28:58', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:28:58', '2025-04-18 21:28:58', 1);
INSERT INTO `payment` VALUES (117, 'PAY17449834095758b776c', 65, 'ORDER17449833960601450', 9, 369.90, 'alipay', 2, '2025-04-19 14:28:22', NULL, NULL, '2025-04-18 23:36:50', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:36:50', '2025-04-19 14:28:22', 1);
INSERT INTO `payment` VALUES (118, 'PAY17449834434196082cd', 65, 'ORDER17449833960601450', 9, 369.90, 'alipay', 4, NULL, NULL, NULL, '2025-04-18 23:37:23', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:37:23', '2025-04-19 14:28:22', 1);
INSERT INTO `payment` VALUES (119, 'PAY1744983462190d3d422', 65, 'ORDER17449833960601450', 9, 369.90, 'alipay', 4, NULL, NULL, NULL, '2025-04-18 23:37:42', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:37:42', '2025-04-19 14:28:22', 1);
INSERT INTO `payment` VALUES (120, 'PAY1744983473494536b74', 65, 'ORDER17449833960601450', 9, 369.90, 'alipay', 2, '2025-04-18 21:37:54', 'AP1744983473711', NULL, '2025-04-18 23:37:53', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:37:53', '2025-04-18 21:37:54', 1);
INSERT INTO `payment` VALUES (121, 'PAY1744983969673736f1c', 66, 'ORDER17449839678137295', 9, 389.90, 'alipay', 2, '2025-04-18 21:46:10', 'AP1744983969782', NULL, '2025-04-18 23:46:10', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:46:10', '2025-04-18 21:46:10', 1);
INSERT INTO `payment` VALUES (122, 'PAY1744984044784bb32e2', 67, 'ORDER17449840414779475', 9, 1009.60, 'alipay', 2, '2025-04-18 21:47:28', 'AP1744984047840', NULL, '2025-04-18 23:47:25', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:47:25', '2025-04-18 21:47:28', 1);
INSERT INTO `payment` VALUES (123, 'PAY174498441793216b4c2', 68, 'ORDER17449844147359468', 9, 2969.00, 'alipay', 2, '2025-04-18 21:53:44', 'AP1744984424058', NULL, '2025-04-18 23:53:38', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:53:38', '2025-04-18 21:53:44', 1);
INSERT INTO `payment` VALUES (124, 'PAY17449844359306a2458', 68, 'ORDER17449844147359468', 9, 2969.00, 'alipay', 4, NULL, NULL, NULL, '2025-04-18 23:53:56', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:53:56', '2025-04-19 14:28:22', 1);
INSERT INTO `payment` VALUES (125, 'PAY1744984564672a81706', 69, 'ORDER17449845632983598', 9, 268.00, 'alipay', 2, '2025-04-18 21:56:05', 'AP1744984564705', NULL, '2025-04-18 23:56:05', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 21:56:05', '2025-04-18 21:56:05', 1);
INSERT INTO `payment` VALUES (126, 'PAY174498523613835d52a', 70, 'ORDER17449852325764091', 9, 2058.90, 'alipay', 2, '2025-04-18 22:07:16', 'AP1744985236261', NULL, '2025-04-19 00:07:16', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 22:07:16', '2025-04-18 22:07:16', 1);
INSERT INTO `payment` VALUES (127, 'PAY17449852939819ff906', 71, 'ORDER17449852869362755', 9, 864.00, 'alipay', 2, '2025-04-18 22:08:14', 'AP1744985294031', NULL, '2025-04-19 00:08:14', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 22:08:14', '2025-04-18 22:08:14', 1);
INSERT INTO `payment` VALUES (128, 'PAY1744985298648a248d4', 71, 'ORDER17449852869362755', 9, 864.00, 'alipay', 2, '2025-04-18 22:08:22', 'AP1744985301719', NULL, '2025-04-19 00:08:19', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 22:08:19', '2025-04-18 22:08:22', 1);
INSERT INTO `payment` VALUES (129, 'PAY1744987200523d38354', 72, 'ORDER17449871938104796', 9, 748.70, 'alipay', 2, '2025-04-18 22:40:01', 'AP1744987200618', NULL, '2025-04-19 00:40:01', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 22:40:01', '2025-04-18 22:40:01', 1);
INSERT INTO `payment` VALUES (130, 'PAY174498793621586d131', 73, 'ORDER17449879317155084', 9, 2286.00, 'alipay', 2, '2025-04-18 22:52:16', 'AP1744987936309', NULL, '2025-04-19 00:52:16', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 22:52:16', '2025-04-18 22:52:16', 1);
INSERT INTO `payment` VALUES (131, 'PAY17449879685316afafa', 73, 'ORDER17449879317155084', 9, 2286.00, 'wechat', 2, '2025-04-18 22:52:49', 'WX1744987969136', NULL, '2025-04-19 00:52:49', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 22:52:49', '2025-04-18 22:52:49', 1);
INSERT INTO `payment` VALUES (132, 'PAY17449879815082054b7', 74, 'ORDER17449879795469034', 9, 2266.00, 'wechat', 2, '2025-04-18 22:53:02', 'WX1744987981529', NULL, '2025-04-19 00:53:02', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-18 22:53:02', '2025-04-18 22:53:02', 1);
INSERT INTO `payment` VALUES (133, 'PAY17450441393432122c6', 75, 'ORDER17450441292649624', 9, 417.00, 'alipay', 2, '2025-04-19 14:29:03', 'AP1745044143198', NULL, '2025-04-19 16:28:59', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 14:28:59', '2025-04-19 14:29:03', 1);
INSERT INTO `payment` VALUES (134, 'PAY17450450901467434df', 76, 'ORDER17450450886945756', 9, 269.70, 'alipay', 2, '2025-04-19 14:44:50', 'AP1745045090208', NULL, '2025-04-19 16:44:50', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 14:44:50', '2025-04-19 14:44:50', 1);
INSERT INTO `payment` VALUES (135, 'PAY1745045188834d7bfe8', 77, 'ORDER17450451873077695', 9, 289.60, 'alipay', 2, '2025-04-19 14:46:29', 'AP1745045188932', NULL, '2025-04-19 16:46:29', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 14:46:29', '2025-04-19 14:46:29', 1);
INSERT INTO `payment` VALUES (136, 'PAY1745045204285f0893b', 77, 'ORDER17450451873077695', 9, 289.60, 'alipay', 4, '2025-04-19 14:46:44', 'AP1745045204338', NULL, '2025-04-19 16:46:44', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 14:46:44', '2025-04-19 14:46:48', 1);
INSERT INTO `payment` VALUES (137, 'PAY17450452185792175b8', 77, 'ORDER17450451873077695', 9, 289.60, 'alipay', 2, '2025-04-19 14:46:59', 'AP1745045218627', NULL, '2025-04-19 16:46:59', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 14:46:59', '2025-04-19 14:46:59', 1);
INSERT INTO `payment` VALUES (138, 'PAY1745045359377a4c438', 78, 'ORDER17450453576811598', 9, 99.90, 'alipay', 2, '2025-04-19 14:49:23', 'AP1745045363190', NULL, '2025-04-19 16:49:19', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 14:49:19', '2025-04-19 14:49:23', 1);
INSERT INTO `payment` VALUES (139, 'PAY174504598998811404c', 79, 'ORDER17450459875746430', 9, 399.90, 'alipay', 2, '2025-04-19 14:59:50', 'AP1745045990089', NULL, '2025-04-19 16:59:50', NULL, NULL, NULL, '2025-04-19 14:59:50', '2025-04-19 14:59:50', 1);
INSERT INTO `payment` VALUES (140, 'PAY17450473756587524be', 80, 'ORDER17450473725228396', 9, 59.90, 'wechat', 2, '2025-04-19 15:22:56', 'WX1745047375695', NULL, '2025-04-19 17:22:56', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 15:22:56', '2025-04-19 15:22:56', 1);
INSERT INTO `payment` VALUES (141, 'PAY1745047863669cb9bc2', 81, 'ORDER17450478614373007', 9, 369.90, 'alipay', 4, '2025-04-19 15:31:07', 'AP1745047867191', NULL, '2025-04-19 17:31:04', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 15:31:04', '2025-04-19 15:31:40', 1);
INSERT INTO `payment` VALUES (142, 'PAY17450486811494f496f', 82, 'ORDER17450486780378791', 9, 209.60, 'alipay', 2, '2025-04-19 15:44:41', 'AP1745048681247', NULL, '2025-04-19 17:44:41', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 15:44:41', '2025-04-19 15:44:41', 1);
INSERT INTO `payment` VALUES (143, 'PAY1745049978875e1d57b', 83, 'ORDER17450499740802107', 9, 2769.30, 'alipay', 2, '2025-04-19 16:06:19', 'AP1745049978977', NULL, '2025-04-19 18:06:19', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 16:06:19', '2025-04-19 16:06:19', 1);
INSERT INTO `payment` VALUES (144, 'PAY174505034289306fe38', 84, 'ORDER17450503412792999', 9, 268.00, 'alipay', 2, '2025-04-19 16:12:26', 'AP1745050346185', NULL, '2025-04-19 18:12:23', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 16:12:23', '2025-04-19 16:12:26', 1);
INSERT INTO `payment` VALUES (145, 'PAY1745050493001189c45', 84, 'ORDER17450503412792999', 9, 268.00, 'wallet', 2, '2025-04-19 16:14:54', 'WALLET1745050493589', NULL, '2025-04-19 18:14:53', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 16:14:53', '2025-04-19 16:14:54', 1);
INSERT INTO `payment` VALUES (146, 'PAY17450505018790ccbf0', 84, 'ORDER17450503412792999', 9, 268.00, 'wechat', 2, '2025-04-19 16:15:05', 'WX1745050504912', NULL, '2025-04-19 18:15:02', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 16:15:02', '2025-04-19 16:15:05', 1);
INSERT INTO `payment` VALUES (147, 'PAY1745054829148f64df6', 85, 'ORDER17450548263311761', 9, 89960.00, 'alipay', 2, '2025-04-19 17:27:09', 'AP1745054829236', NULL, '2025-04-19 19:27:09', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 17:27:09', '2025-04-19 17:27:09', 1);
INSERT INTO `payment` VALUES (148, 'PAY1745070371296d35ba4', 86, 'ORDER17450703688316173', 9, 1918.50, 'wechat', 2, '2025-04-19 21:46:11', 'WX1745070371322', NULL, '2025-04-19 23:46:11', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 21:46:11', '2025-04-19 21:46:11', 1);
INSERT INTO `payment` VALUES (149, 'PAY17450714015527a5b58', 87, 'ORDER17450713988864410', 9, 1169.60, 'wechat', 2, '2025-04-19 22:03:25', 'WX1745071404613', NULL, '2025-04-20 00:03:22', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-19 22:03:22', '2025-04-19 22:03:25', 1);
INSERT INTO `payment` VALUES (150, 'PAY174511407270902f42e', 96, 'ORDER17451140395427141', 9, 2178.30, 'alipay', 2, '2025-04-20 09:54:33', 'AP1745114072827', NULL, '2025-04-20 11:54:33', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 09:54:33', '2025-04-20 09:54:33', 1);
INSERT INTO `payment` VALUES (151, 'PAY1745114503571f54e7c', 97, 'ORDER17451144733955838', 9, 2369.40, 'wechat', 2, '2025-04-20 10:01:47', 'WX1745114507190', NULL, '2025-04-20 12:01:44', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 10:01:44', '2025-04-20 10:01:47', 1);
INSERT INTO `payment` VALUES (152, 'PAY174512781527969f0c0', 99, 'ORDER17451278084316405', 9, 869.10, 'alipay', 2, '2025-04-20 13:43:39', 'AP1745127818706', NULL, '2025-04-20 15:43:35', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 13:43:35', '2025-04-20 13:43:39', 1);
INSERT INTO `payment` VALUES (153, 'PAY1745127825237d87627', 99, 'ORDER17451278084316405', 9, 869.10, 'wechat', 2, '2025-04-20 13:43:45', 'WX1745127825269', NULL, '2025-04-20 15:43:45', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 13:43:45', '2025-04-20 13:43:45', 1);
INSERT INTO `payment` VALUES (154, 'PAY1745130111130cf7d1e', 100, 'ORDER17451301068395898', 9, 329.40, 'wechat', 2, '2025-04-20 14:21:51', 'WX1745130111166', NULL, '2025-04-20 16:21:51', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 14:21:51', '2025-04-20 14:21:51', 1);
INSERT INTO `payment` VALUES (155, 'PAY174513044020692fbd6', 101, 'ORDER17451304374482107', 9, 329.40, 'wechat', 2, '2025-04-20 14:27:20', 'WX1745130440226', NULL, '2025-04-20 16:27:20', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 14:27:20', '2025-04-20 14:27:20', 1);
INSERT INTO `payment` VALUES (156, 'PAY1745130522923819eb8', 102, 'ORDER17451305201237152', 9, 1109.40, 'wechat', 2, '2025-04-20 14:28:43', 'WX1745130522942', NULL, '2025-04-20 16:28:43', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 14:28:43', '2025-04-20 14:28:43', 1);
INSERT INTO `payment` VALUES (157, 'PAY17451407108169af73e', 104, 'ORDER17451407073845313', 9, 999.40, 'wechat', 2, '2025-04-20 17:18:31', 'WX1745140710835', NULL, '2025-04-20 19:18:31', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 17:18:31', '2025-04-20 17:18:31', 1);
INSERT INTO `payment` VALUES (158, 'PAY1745144851768b0bbba', 105, 'ORDER17451448487674395', 9, 169.90, 'wechat', 2, '2025-04-20 18:27:32', 'WX1745144851797', NULL, '2025-04-20 20:27:32', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 18:27:32', '2025-04-20 18:27:32', 1);
INSERT INTO `payment` VALUES (159, 'PAY1745145759222f4bf4d', 106, 'ORDER17451457560899513', 9, 269.70, 'wechat', 2, '2025-04-20 18:42:45', 'WX1745145765322', NULL, '2025-04-20 20:42:39', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 18:42:39', '2025-04-20 18:42:45', 1);
INSERT INTO `payment` VALUES (160, 'PAY17451458564662d042b', 107, 'ORDER17451458509872621', 9, 539.70, 'wechat', 2, '2025-04-20 18:44:20', 'WX1745145859504', NULL, '2025-04-20 20:44:16', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 18:44:16', '2025-04-20 18:44:20', 1);
INSERT INTO `payment` VALUES (161, 'PAY174514601719068659f', 108, 'ORDER17451460141667503', 9, 1127.80, 'alipay', 2, '2025-04-20 18:47:01', 'AP1745146020669', NULL, '2025-04-20 20:46:57', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 18:46:57', '2025-04-20 18:47:01', 1);
INSERT INTO `payment` VALUES (162, 'PAY17451462862128ae6b9', 109, 'ORDER17451462811964198', 9, 169.90, 'wechat', 2, '2025-04-20 18:51:29', 'WX1745146289272', NULL, '2025-04-20 20:51:26', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-20 18:51:26', '2025-04-20 18:51:29', 1);
INSERT INTO `payment` VALUES (163, 'PAY1745211464719a134ae', 111, 'ORDER17452114489074352', 9, 59.90, 'alipay', 2, '2025-04-21 12:57:45', 'AP1745211464831', NULL, '2025-04-21 14:57:45', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 12:57:45', '2025-04-21 12:57:45', 1);
INSERT INTO `payment` VALUES (164, 'PAY1745213300574947371', 112, 'ORDER17452132971645998', 9, 289.60, 'wechat', 2, '2025-04-21 13:28:24', 'WX1745213303624', NULL, '2025-04-21 15:28:21', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 13:28:21', '2025-04-21 13:28:24', 1);
INSERT INTO `payment` VALUES (165, 'PAY17452141350242bd300', 115, 'ORDER17452140968006383', 9, 139.00, 'wallet', 2, '2025-04-21 13:42:16', 'WALLET1745214135610', NULL, '2025-04-21 15:42:15', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 13:42:15', '2025-04-21 13:42:16', 1);
INSERT INTO `payment` VALUES (166, 'PAY1745214142076aeb456', 115, 'ORDER17452140968006383', 9, 139.00, 'wechat', 2, '2025-04-21 13:42:22', 'WX1745214142094', NULL, '2025-04-21 15:42:22', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 13:42:22', '2025-04-21 13:42:22', 1);
INSERT INTO `payment` VALUES (167, 'PAY17452141433401545df', 115, 'ORDER17452140968006383', 9, 139.00, 'wechat', 2, '2025-04-21 13:42:23', 'WX1745214143356', NULL, '2025-04-21 15:42:23', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 13:42:23', '2025-04-21 13:42:23', 1);
INSERT INTO `payment` VALUES (168, 'PAY1745214299656932019', 116, 'ORDER17452142961221645', 9, 369.90, 'alipay', 2, '2025-04-21 13:45:09', 'AP1745214308732', NULL, '2025-04-21 15:45:00', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 13:45:00', '2025-04-21 13:45:09', 1);
INSERT INTO `payment` VALUES (169, 'PAY1745214529577cf2af9', 117, 'ORDER17452145246841901', 9, 539.70, 'wechat', 2, '2025-04-21 13:48:50', 'WX1745214529600', NULL, '2025-04-21 15:48:50', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 13:48:50', '2025-04-21 13:48:50', 1);
INSERT INTO `payment` VALUES (170, 'PAY1745214852767d3e391', 118, 'ORDER17452148490214231', 9, 539.70, 'wechat', 2, '2025-04-21 13:54:13', 'WX1745214852786', NULL, '2025-04-21 15:54:13', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 13:54:13', '2025-04-21 13:54:13', 1);
INSERT INTO `payment` VALUES (171, 'PAY1745215034104f99850', 119, 'ORDER17452150243901075', 9, 169.90, 'wechat', 2, '2025-04-21 13:57:14', 'WX1745215034133', NULL, '2025-04-21 15:57:14', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 13:57:14', '2025-04-21 13:57:14', 1);
INSERT INTO `payment` VALUES (172, 'PAY1745215416904a4587f', 120, 'ORDER17452154110214171', 9, 369.90, 'wechat', 2, '2025-04-21 14:03:37', 'WX1745215416925', NULL, '2025-04-21 16:03:37', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 14:03:37', '2025-04-21 14:03:37', 1);
INSERT INTO `payment` VALUES (173, 'PAY1745216052119895bae', 121, 'ORDER17452160264701642', 9, 1569.60, 'wechat', 2, '2025-04-21 14:14:12', 'WX1745216052145', NULL, '2025-04-21 16:14:12', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 14:14:12', '2025-04-21 14:14:12', 1);
INSERT INTO `payment` VALUES (174, 'PAY17452167868979ba2f0', 122, 'ORDER17452167814059647', 9, 169.90, 'wechat', 2, '2025-04-21 14:26:27', 'WX1745216786941', NULL, '2025-04-21 16:26:27', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 14:26:27', '2025-04-21 14:26:27', 1);
INSERT INTO `payment` VALUES (175, 'PAY174521721167621e441', 123, 'ORDER17452172041521840', 9, 389.30, 'wechat', 2, '2025-04-21 14:33:35', 'WX1745217214738', NULL, '2025-04-21 16:33:32', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 14:33:32', '2025-04-21 14:33:35', 1);
INSERT INTO `payment` VALUES (176, 'PAY1745218641040dacb8c', 124, 'ORDER17452186371897636', 9, 919.50, 'wechat', 2, '2025-04-21 14:57:21', 'WX1745218641080', NULL, '2025-04-21 16:57:21', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 14:57:21', '2025-04-21 14:57:21', 1);
INSERT INTO `payment` VALUES (177, 'PAY174521937034270b977', 125, 'ORDER17452193667296638', 9, 269.70, 'alipay', 2, '2025-04-21 15:09:30', 'AP1745219370464', NULL, '2025-04-21 17:09:30', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 15:09:30', '2025-04-21 15:09:30', 1);
INSERT INTO `payment` VALUES (178, 'PAY1745219407720b96ac0', 126, 'ORDER17452193970978567', 9, 1169.60, 'wechat', 2, '2025-04-21 15:10:11', 'WX1745219410774', NULL, '2025-04-21 17:10:08', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 15:10:08', '2025-04-21 15:10:11', 1);
INSERT INTO `payment` VALUES (179, 'PAY17452201996364d1342', 127, 'ORDER17452201961333527', 9, 1769.40, 'wechat', 2, '2025-04-21 15:23:20', 'WX1745220199670', NULL, '2025-04-21 17:23:20', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 15:23:20', '2025-04-21 15:23:20', 1);
INSERT INTO `payment` VALUES (180, 'PAY17452202466482520c6', 128, 'ORDER17452202440941328', 9, 509.40, 'wechat', 2, '2025-04-21 15:24:13', 'WX1745220252704', NULL, '2025-04-21 17:24:07', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 15:24:07', '2025-04-21 15:24:13', 1);
INSERT INTO `payment` VALUES (181, 'PAY1745220709188b17f3a', 129, 'ORDER17452207018446413', 9, 419.10, 'wechat', 2, '2025-04-21 15:31:55', 'WX1745220715256', NULL, '2025-04-21 17:31:49', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 15:31:49', '2025-04-21 15:31:55', 1);
INSERT INTO `payment` VALUES (182, 'PAY1745221297141a49b46', 130, 'ORDER17452212896086413', 9, 869.00, 'wechat', 2, '2025-04-21 15:41:37', 'WX1745221297180', NULL, '2025-04-21 17:41:37', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 15:41:37', '2025-04-21 15:41:37', 1);
INSERT INTO `payment` VALUES (183, 'PAY17452215808890fdb52', 131, 'ORDER17452215674805396', 9, 242.00, 'wechat', 2, '2025-04-21 15:46:21', 'WX1745221580920', NULL, '2025-04-21 17:46:21', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 15:46:21', '2025-04-21 15:46:21', 1);
INSERT INTO `payment` VALUES (184, 'PAY1745221894682aea9b3', 132, 'ORDER17452218697879314', 9, 619.50, 'wechat', 2, '2025-04-21 15:51:35', 'WX1745221895299', NULL, '2025-04-21 17:51:35', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 15:51:35', '2025-04-21 15:51:35', 1);
INSERT INTO `payment` VALUES (185, 'PAY17452396907186c594c', 134, 'ORDER17452396871972804', 9, 169.90, 'alipay', 2, '2025-04-21 20:48:11', 'AP1745239690823', NULL, '2025-04-21 22:48:11', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 20:48:11', '2025-04-21 20:48:11', 1);
INSERT INTO `payment` VALUES (186, 'PAY174524023208279b175', 135, 'ORDER17452402212747378', 9, 566.00, 'wechat', 2, '2025-04-21 20:57:12', 'WX1745240232121', NULL, '2025-04-21 22:57:12', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-21 20:57:12', '2025-04-21 20:57:12', 1);
INSERT INTO `payment` VALUES (188, 'PAY1745324667673b96e7e', 165, 'ORDER17453246643974732', 9, 209.60, 'alipay', 2, '2025-04-22 20:24:28', 'AP1745324667815', NULL, '2025-04-22 22:24:28', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-22 20:24:28', '2025-04-22 20:24:28', 1);
INSERT INTO `payment` VALUES (189, 'PAY1745324899036cecae2', 166, 'ORDER17453248963623581', 9, 129.00, 'wechat', 2, '2025-04-22 20:28:19', 'WX1745324899058', NULL, '2025-04-22 22:28:19', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-22 20:28:19', '2025-04-22 20:28:19', 1);
INSERT INTO `payment` VALUES (190, 'PAY17453255752578dcbb0', 167, 'ORDER17453255706674318', 9, 469.50, 'wechat', 2, '2025-04-22 20:39:35', 'WX1745325575300', NULL, '2025-04-22 22:39:35', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-22 20:39:35', '2025-04-22 20:39:35', 1);
INSERT INTO `payment` VALUES (191, 'PAY1745326423081494255', 168, 'ORDER17453264196027857', 9, 759.60, 'wechat', 2, '2025-04-22 20:53:43', 'WX1745326423123', NULL, '2025-04-22 22:53:43', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-22 20:53:43', '2025-04-22 20:53:43', 1);
INSERT INTO `payment` VALUES (192, 'PAY174532948779162a32f', 169, 'ORDER17453294836071112', 9, 1969.50, 'alipay', 2, '2025-04-22 21:44:55', 'AP1745329495167', NULL, '2025-04-22 23:44:48', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-22 21:44:48', '2025-04-22 21:44:55', 1);
INSERT INTO `payment` VALUES (193, 'PAY1745329539594e580d0', 170, 'ORDER17453295353832425', 9, 369.60, 'wechat', 2, '2025-04-22 21:45:40', 'WX1745329539621', NULL, '2025-04-22 23:45:40', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-22 21:45:40', '2025-04-22 21:45:40', 1);
INSERT INTO `payment` VALUES (194, 'PAY17453297815574ed46d', 171, 'ORDER17453297779016486', 9, 1569.60, 'wechat', 2, '2025-04-22 21:49:42', 'WX1745329781603', NULL, '2025-04-22 23:49:42', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-22 21:49:42', '2025-04-22 21:49:42', 1);
INSERT INTO `payment` VALUES (195, 'PAY1745388681720e6c424', 172, 'ORDER17453886695259124', 9, 99.90, 'wechat', 2, '2025-04-23 14:11:22', 'WX1745388681750', NULL, '2025-04-23 16:11:22', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-23 14:11:22', '2025-04-23 14:11:22', 1);
INSERT INTO `payment` VALUES (196, 'PAY17454113396328b0cf8', 173, 'ORDER17454113357745166', 9, 729.60, 'wechat', 2, '2025-04-23 20:29:03', 'WX1745411342677', NULL, '2025-04-23 22:29:00', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-23 20:29:00', '2025-04-23 20:29:03', 1);
INSERT INTO `payment` VALUES (197, 'PAY17454133781947b3880', 174, 'ORDER17454133754631831', 9, 179.90, 'wechat', 2, '2025-04-23 21:02:58', 'WX1745413378217', NULL, '2025-04-23 23:02:58', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-23 21:02:58', '2025-04-23 21:02:58', 1);
INSERT INTO `payment` VALUES (198, 'PAY174541435149050d980', 175, 'ORDER17454143481457218', 9, 769.60, 'alipay', 2, '2025-04-23 21:19:15', 'AP1745414354690', NULL, '2025-04-23 23:19:11', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-23 21:19:11', '2025-04-23 21:19:15', 1);
INSERT INTO `payment` VALUES (199, 'PAY1745414571933bfa9d8', 176, 'ORDER17454145288759162', 9, 369.20, 'wechat', 2, '2025-04-23 21:22:56', 'WX1745414575575', NULL, '2025-04-23 23:22:52', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-23 21:22:52', '2025-04-23 21:22:56', 1);
INSERT INTO `payment` VALUES (200, 'PAY174541537253300aa94', 177, 'ORDER17454153673359147', 9, 469.60, 'wechat', 2, '2025-04-23 21:36:16', 'WX1745415375794', NULL, '2025-04-23 23:36:13', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-23 21:36:13', '2025-04-23 21:36:16', 1);
INSERT INTO `payment` VALUES (201, 'PAY17454712839087b6da6', 178, 'ORDER17454712732296454', 9, 49.90, 'wechat', 2, '2025-04-24 13:08:04', 'WX1745471283944', NULL, '2025-04-24 15:08:04', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-24 13:08:04', '2025-04-24 13:08:04', 1);
INSERT INTO `payment` VALUES (202, 'PAY17454721166551ded83', 179, 'ORDER17454721117056277', 9, 2384.40, 'wechat', 2, '2025-04-24 13:22:00', 'WX1745472119724', NULL, '2025-04-24 15:21:57', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-24 13:21:57', '2025-04-24 13:22:00', 1);
INSERT INTO `payment` VALUES (203, 'PAY17454725407407be93e', 180, 'ORDER17454725349573471', 9, 1149.60, 'wechat', 2, '2025-04-24 13:29:01', 'WX1745472540791', NULL, '2025-04-24 15:29:01', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-24 13:29:01', '2025-04-24 13:29:01', 1);
INSERT INTO `payment` VALUES (204, 'PAY1745651702717075307', 181, 'ORDER17456516991175745', 9, 984.00, 'alipay', 2, '2025-04-26 15:15:10', 'AP1745651702780', NULL, '2025-04-26 17:15:03', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-26 15:15:03', '2025-04-26 15:15:10', 1);
INSERT INTO `payment` VALUES (205, 'PAY1745679625227dd0439', 182, 'ORDER17456796217205119', 9, 99.90, 'alipay', 2, '2025-04-26 23:00:37', 'AP1745679625287', NULL, '2025-04-27 01:00:25', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-26 23:00:25', '2025-04-26 23:00:37', 1);
INSERT INTO `payment` VALUES (206, 'PAY1745679734997798417', 183, 'ORDER17456797301057465', 9, 189.90, 'alipay', 2, '2025-04-26 23:02:15', 'AP1745679735032', NULL, '2025-04-27 01:02:15', NULL, 'http://localhost:5173/payment/result', NULL, '2025-04-26 23:02:15', '2025-04-26 23:02:15', 1);
INSERT INTO `payment` VALUES (212, 'PAY17464574405223e9f', 187, 'OD1746451602504591a5e', 8, 1339.19, 'alipay', 2, '2025-05-05 23:04:34', 'AP-2025050522001434870506399441', NULL, '2025-05-06 01:04:01', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-05 23:04:01', '2025-05-05 23:04:34', 1);
INSERT INTO `payment` VALUES (213, 'PAY1746457684516365c', 187, 'OD1746451602504591a5e', 8, 1339.19, 'alipay', 0, NULL, NULL, NULL, '2025-05-06 01:08:05', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-05 23:08:05', '2025-05-05 23:08:05', 1);
INSERT INTO `payment` VALUES (218, 'PAY17465249353407d82', 188, 'OD17464971994169a1e75', 8, 79.90, 'alipay', 2, '2025-05-06 17:49:45', 'AP-2025050622001434870506405529', NULL, '2025-05-06 19:48:55', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-06 17:48:55', '2025-05-06 17:49:45', 1);
INSERT INTO `payment` VALUES (228, 'PAY174654079027428ab', 195, 'OD1746540784960366edc', 8, 278.00, 'alipay', 2, '2025-05-06 22:13:42', 'AP-2025050622001434870506418372', NULL, '2025-05-07 00:13:10', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-06 22:13:10', '2025-05-06 22:13:42', 2);
INSERT INTO `payment` VALUES (229, 'PAY1746541495245ed7a', 196, 'OD17465414906845dbb63', 8, 109.90, 'alipay', 2, '2025-05-06 22:25:27', 'AP-2025050622001434870506417127', NULL, '2025-05-07 00:24:55', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-06 22:24:55', '2025-05-06 22:25:27', 2);
INSERT INTO `payment` VALUES (230, 'PAY1746541903233ab08', 197, 'OD174654189790984570d', 8, 229.90, 'alipay', 2, '2025-05-06 22:32:17', 'AP-2025050622001434870506420307', NULL, '2025-05-07 00:31:43', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-06 22:31:43', '2025-05-06 22:32:17', 2);
INSERT INTO `payment` VALUES (231, 'PAY17465432545426867', 198, 'OD17465432495356e4092', 8, 49.90, 'alipay', 2, '2025-05-06 22:55:01', 'AP-2025050622001434870506418373', NULL, '2025-05-07 00:54:15', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-06 22:54:15', '2025-05-06 22:55:01', 2);
INSERT INTO `payment` VALUES (232, 'PAY1746543478630becd', 199, 'OD174654347401056de4b', 8, 649.90, 'alipay', 2, '2025-05-06 22:58:27', 'AP-2025050622001434870506422119', NULL, '2025-05-07 00:57:59', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-06 22:57:59', '2025-05-06 22:58:27', 2);
INSERT INTO `payment` VALUES (233, 'PAY1746626373267d425', 201, 'OD1746625330343300015', 8, 129.90, 'wechat', 2, '2025-05-07 21:59:33', 'AP-WX_SANDBOX_02f9b888162640049b5179e963ae5b94', NULL, '2025-05-07 23:59:33', NULL, NULL, NULL, '2025-05-07 21:59:33', '2025-05-07 21:59:33', 2);
INSERT INTO `payment` VALUES (234, 'PAY17466277061719862', 202, 'OD17466276982781ac330', 8, 139.90, 'wechat', 2, '2025-05-07 22:21:46', 'WX052c9a5630e54b06bd0f33fb4807f443', NULL, '2025-05-08 00:21:46', NULL, NULL, NULL, '2025-05-07 22:21:46', '2025-05-07 22:21:46', 2);
INSERT INTO `payment` VALUES (235, 'PAY174668033414079f5', 203, 'OD17466803173112b1180', 9, 29.90, 'wechat', 2, '2025-05-08 12:58:54', 'WX0c83d50e5d9044fb9fc11833c25955f9', NULL, '2025-05-08 14:58:54', NULL, NULL, NULL, '2025-05-08 12:58:54', '2025-05-08 12:58:54', 2);
INSERT INTO `payment` VALUES (236, 'PAY1746680614334600e', 203, 'OD17466803173112b1180', 9, 29.90, 'wechat', 2, '2025-05-08 13:03:34', 'WXd69896794a8a4052a7d12a3e68879e4c', NULL, '2025-05-08 15:03:34', NULL, NULL, NULL, '2025-05-08 13:03:34', '2025-05-08 13:03:34', 2);
INSERT INTO `payment` VALUES (237, 'PAY1746680843854ef3b', 203, 'OD17466803173112b1180', 9, 29.90, 'wechat', 2, '2025-05-08 13:07:24', 'WX361383c3ba0a4cd5bf58c8dac2ceba68', NULL, '2025-05-08 15:07:24', NULL, NULL, NULL, '2025-05-08 13:07:24', '2025-05-08 13:07:24', 2);
INSERT INTO `payment` VALUES (238, 'PAY17466810857943ab5', 204, 'OD1746681070145d9652e', 9, 399.90, 'wechat', 2, '2025-05-08 13:11:26', 'WX38080cc9371b4bf2bb1a0f27bef728ed', NULL, '2025-05-08 15:11:26', NULL, NULL, NULL, '2025-05-08 13:11:26', '2025-05-08 13:11:26', 2);
INSERT INTO `payment` VALUES (239, 'PAY174668120689500b0', 202, 'OD17466276982781ac330', 8, 139.90, 'wechat', 2, '2025-05-08 13:13:27', 'WX586ce04bb05543e0a1cfc4e76dd0b326', NULL, '2025-05-08 15:13:27', NULL, NULL, NULL, '2025-05-08 13:13:27', '2025-05-08 13:13:27', 2);
INSERT INTO `payment` VALUES (240, 'PAY1746681459971d9aa', 205, 'OD1746681443285b6ea27', 8, 79.90, 'wechat', 2, '2025-05-08 13:17:40', 'WXf2044893e08648aabe52fdbfc984d823', NULL, '2025-05-08 15:17:40', NULL, NULL, NULL, '2025-05-08 13:17:40', '2025-05-08 13:17:40', 2);
INSERT INTO `payment` VALUES (241, 'PAY17466817829147d96', 205, 'OD1746681443285b6ea27', 8, 79.90, 'wechat', 2, '2025-05-08 13:23:03', 'WX0830455745fb45ce8b05330894a6eb39', NULL, '2025-05-08 15:23:03', NULL, NULL, NULL, '2025-05-08 13:23:03', '2025-05-08 13:23:03', 2);
INSERT INTO `payment` VALUES (242, 'PAY1746681998437e5f0', 205, 'OD1746681443285b6ea27', 8, 79.90, 'wechat', 2, '2025-05-08 13:26:38', 'WX0bf565a4a2a94751b65fdba0a3035e9c', NULL, '2025-05-08 15:26:38', NULL, NULL, NULL, '2025-05-08 13:26:38', '2025-05-08 13:26:38', 2);
INSERT INTO `payment` VALUES (243, 'PAY174668220985375e2', 205, 'OD1746681443285b6ea27', 8, 79.90, 'wechat', 2, '2025-05-08 13:30:10', 'WX3b5164de5bb54400b9d89358fffabf3c', NULL, '2025-05-08 15:30:10', NULL, NULL, NULL, '2025-05-08 13:30:10', '2025-05-08 13:30:10', 2);
INSERT INTO `payment` VALUES (244, 'PAY1746682452857943b', 205, 'OD1746681443285b6ea27', 8, 79.90, 'wechat', 2, '2025-05-08 13:34:13', 'WX4faf3f302bb349328627b00caedf6c64', NULL, '2025-05-08 15:34:13', NULL, NULL, NULL, '2025-05-08 13:34:13', '2025-05-08 13:34:13', 2);
INSERT INTO `payment` VALUES (245, 'PAY174668298021509c8', 202, 'OD17466276982781ac330', 8, 139.90, 'wechat', 2, '2025-05-08 13:43:00', 'WXb48ec43e1f054b3190bf6f5e53f6c77d', NULL, '2025-05-08 15:43:00', NULL, NULL, NULL, '2025-05-08 13:43:00', '2025-05-08 13:43:00', 2);
INSERT INTO `payment` VALUES (246, 'PAY17467967728918dde', 206, 'OD1746796766856f06ea9', 8, 299.70, 'alipay', 2, '2025-05-09 21:20:18', 'AP2025050922001434870506456063', NULL, '2025-05-09 23:19:33', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-09 21:19:33', '2025-05-09 21:20:18', 2);
INSERT INTO `payment` VALUES (247, 'PAY1746799676432fe9e', 207, 'OD17467996633588ae4d9', 8, 209.80, 'wechat', 2, '2025-05-09 22:07:56', 'WXac403c62471c418b982a7d6d6cbceaa2', NULL, '2025-05-10 00:07:56', NULL, NULL, NULL, '2025-05-09 22:07:56', '2025-05-09 22:07:56', 2);
INSERT INTO `payment` VALUES (248, 'PAY1746801893114518a', 208, 'OD17468018813539a9556', 8, 539.80, 'wechat', 2, '2025-05-09 22:44:53', 'WX423c3732250a4f3ca1a3b2a776abde86', NULL, '2025-05-10 00:44:53', NULL, NULL, NULL, '2025-05-09 22:44:53', '2025-05-09 22:44:53', 2);
INSERT INTO `payment` VALUES (249, 'PAY17468888664864869', 203, 'OD17466803173112b1180', 9, 29.90, 'wechat', 2, '2025-05-10 22:54:26', 'WX7a998b29e23f46f7bbfbac6bdd7ef959', NULL, '2025-05-11 00:54:26', NULL, NULL, NULL, '2025-05-10 22:54:26', '2025-05-10 22:54:26', 2);
INSERT INTO `payment` VALUES (250, 'PAY1746889077130fffd', 209, 'OD17468890668821ab08d', 9, 379.80, 'wechat', 2, '2025-05-10 22:57:57', 'WXfef89e273d0c4ac686707cd71fa2fc57', NULL, '2025-05-11 00:57:57', NULL, NULL, NULL, '2025-05-10 22:57:57', '2025-05-10 22:57:57', 2);
INSERT INTO `payment` VALUES (251, 'PAY174688954934456b6', 210, 'OD17468895285965ff711', 9, 109.90, 'wechat', 2, '2025-05-10 23:05:49', 'WX55dd1bf854734a31bece1b763f381827', NULL, '2025-05-11 01:05:49', NULL, NULL, NULL, '2025-05-10 23:05:49', '2025-05-10 23:05:49', 2);
INSERT INTO `payment` VALUES (252, 'PAY1746936254032f676', 212, 'OD1746936244945e237b5', 8, 79.90, 'wechat', 2, '2025-05-11 12:04:14', 'WX77a1f608a06942a3a3567c5cbdde6fd9', NULL, '2025-05-11 14:04:14', NULL, NULL, NULL, '2025-05-11 12:04:14', '2025-05-11 12:04:14', 2);
INSERT INTO `payment` VALUES (253, 'PAY174693987770874c4', 213, 'OD17469398067101837dd', 8, 399.90, 'wechat', 2, '2025-05-11 13:04:38', 'WXc633b00dcae1486ba35f6ae919997407', NULL, '2025-05-11 15:04:38', NULL, NULL, NULL, '2025-05-11 13:04:38', '2025-05-11 13:04:38', 2);
INSERT INTO `payment` VALUES (254, 'PAY1746944166593b8a2', 217, 'OD1746944159254ef56e4', 8, 69.90, 'wechat', 2, '2025-05-11 14:16:07', 'WXbc61b33a1fce46cd88eb407d39cd28a1', NULL, '2025-05-11 16:16:07', NULL, NULL, NULL, '2025-05-11 14:16:07', '2025-05-11 14:16:07', 2);
INSERT INTO `payment` VALUES (255, 'PAY17472851528960bf1', 221, 'OD174728514628658bee0', 8, 129.90, 'alipay', 2, '2025-05-15 12:59:48', 'AP2025051522001434870506524724', NULL, '2025-05-15 14:59:13', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-15 12:59:13', '2025-05-15 12:59:48', 2);
INSERT INTO `payment` VALUES (256, 'PAY1747320526702f283', 222, 'OD1747320522379daa9d8', 8, 69.90, 'alipay', 0, NULL, NULL, NULL, '2025-05-16 00:48:47', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-15 22:48:47', '2025-05-15 22:48:47', 1);
INSERT INTO `payment` VALUES (257, 'PAY17473205294688508', 222, 'OD1747320522379daa9d8', 8, 69.90, 'alipay', 0, NULL, NULL, NULL, '2025-05-16 00:48:49', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-15 22:48:49', '2025-05-15 22:48:49', 1);
INSERT INTO `payment` VALUES (258, 'PAY1747320530207fe75', 222, 'OD1747320522379daa9d8', 8, 69.90, 'alipay', 0, NULL, NULL, NULL, '2025-05-16 00:48:50', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-15 22:48:50', '2025-05-15 22:48:50', 1);
INSERT INTO `payment` VALUES (259, 'PAY17473205306616718', 222, 'OD1747320522379daa9d8', 8, 69.90, 'alipay', 0, NULL, NULL, NULL, '2025-05-16 00:48:51', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-15 22:48:51', '2025-05-15 22:48:51', 1);
INSERT INTO `payment` VALUES (260, 'PAY17473205310099bb4', 222, 'OD1747320522379daa9d8', 8, 69.90, 'alipay', 2, '2025-05-15 22:49:20', 'AP2025051522001434870506532443', NULL, '2025-05-16 00:48:51', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-15 22:48:51', '2025-05-15 22:49:20', 2);
INSERT INTO `payment` VALUES (261, 'PAY17478392335246a08', 225, 'OD17478392282642a46ae', 8, 399.90, 'alipay', 2, '2025-05-21 22:54:59', 'AP2025052122001434870506595532', NULL, '2025-05-22 00:53:54', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-21 22:53:54', '2025-05-21 22:54:59', 2);
INSERT INTO `payment` VALUES (262, 'PAY1747904258160b1f5', 227, 'OD17479041992960ab1fc', 8, 199.00, 'alipay', 0, NULL, NULL, NULL, '2025-05-22 18:57:38', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-22 16:57:38', '2025-05-22 16:57:38', 1);
INSERT INTO `payment` VALUES (263, 'PAY1747904508617ed4d', 227, 'OD17479041992960ab1fc', 8, 199.00, 'wechat', 2, '2025-05-22 17:01:49', 'WX36a1792a3cac49f5a4ac27d1a7710083', NULL, '2025-05-22 19:01:49', NULL, NULL, NULL, '2025-05-22 17:01:49', '2025-05-22 17:01:49', 2);
INSERT INTO `payment` VALUES (264, 'PAY17479221037659725', 228, 'OD1747922100745f8639c', 8, 716.00, 'alipay', 2, '2025-05-22 21:56:21', 'AP2025052222001434870506601167', NULL, '2025-05-22 23:55:04', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-22 21:55:04', '2025-05-22 21:56:21', 2);
INSERT INTO `payment` VALUES (265, 'PAY17480855830260159', 229, 'OD1748085552938f9acc0', 8, 129.00, 'wechat', 2, '2025-05-24 19:19:43', 'WX822ce0a5afff4cae9a47de74452b0955', NULL, '2025-05-24 21:19:43', NULL, NULL, NULL, '2025-05-24 19:19:43', '2025-05-24 19:19:43', 2);
INSERT INTO `payment` VALUES (266, 'PAY1748441735569f9d8', 231, 'OD174844173195788d6c7', 8, 756.00, 'alipay', 2, '2025-05-28 22:16:10', 'AP2025052822001434870506664276', NULL, '2025-05-29 00:15:36', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-28 22:15:36', '2025-05-28 22:16:10', 2);
INSERT INTO `payment` VALUES (267, 'PAY1748532237688a84b', 232, 'OD1748532233832df04d1', 8, 269.90, 'alipay', 0, NULL, NULL, NULL, '2025-05-30 01:23:58', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-29 23:23:58', '2025-05-29 23:23:58', 1);
INSERT INTO `payment` VALUES (268, 'PAY174853232075972cf', 233, 'OD174853231755256c661', 8, 716.00, 'alipay', 0, NULL, NULL, NULL, '2025-05-30 01:25:21', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-05-29 23:25:21', '2025-05-29 23:25:21', 1);
INSERT INTO `payment` VALUES (269, 'PAY17491722765131f1f', 235, 'OD1749172272479c582e6', 8, 119.00, 'alipay', 0, NULL, NULL, NULL, '2025-06-06 11:11:17', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-06-06 09:11:17', '2025-06-06 09:11:17', 1);
INSERT INTO `payment` VALUES (270, 'PAY174917231204806df', 235, 'OD1749172272479c582e6', 8, 119.00, 'alipay', 2, '2025-06-06 09:12:29', 'AP2025060622001434870506742202', NULL, '2025-06-06 11:11:52', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-06-06 09:11:52', '2025-06-06 09:12:29', 2);
INSERT INTO `payment` VALUES (271, 'PAY17491728919034412', 236, 'OD17491728740800729c7', 8, 749.80, 'alipay', 0, NULL, NULL, NULL, '2025-06-06 11:21:32', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-06-06 09:21:32', '2025-06-06 09:21:32', 1);
INSERT INTO `payment` VALUES (272, 'PAY1749172928840592b', 236, 'OD17491728740800729c7', 8, 749.80, 'alipay', 2, '2025-06-06 09:22:39', 'AP2025060622001434870506743307', NULL, '2025-06-06 11:22:09', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-06-06 09:22:09', '2025-06-06 09:22:39', 2);
INSERT INTO `payment` VALUES (273, 'PAY1749188160746ee8a', 237, 'OD1749188154352415971', 8, 399.90, 'alipay', 2, '2025-06-06 13:36:36', 'AP2025060622001434870506740866', NULL, '2025-06-06 15:36:01', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-06-06 13:36:01', '2025-06-06 13:36:36', 2);
INSERT INTO `payment` VALUES (274, 'PAY1749632568651a7b9', 245, 'OD1749632308025fbdbf2', 8, 189.00, 'alipay', 0, NULL, NULL, NULL, '2025-06-11 19:02:49', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-06-11 17:02:49', '2025-06-11 17:02:49', 1);
INSERT INTO `payment` VALUES (275, 'PAY1749632758619287a', 246, 'OD1749632756130cc4d79', 8, 179.00, 'alipay', 2, '2025-06-11 17:06:37', 'AP2025061122001434870506779727', NULL, '2025-06-11 19:05:59', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-06-11 17:05:59', '2025-06-11 17:06:37', 2);
INSERT INTO `payment` VALUES (276, 'PAY17496334090014bbf', 248, 'OD1749633399671293c3f', 8, 39.80, 'wechat', 2, '2025-06-11 17:16:49', 'WXa9062bc0815642ffa9d23815cc298acc', NULL, '2025-06-11 19:16:49', NULL, NULL, NULL, '2025-06-11 17:16:49', '2025-06-11 17:16:49', 2);
INSERT INTO `payment` VALUES (277, 'PAY17496356831872b83', 252, 'OD1749635425602acc22f', 8, 179.00, 'wallet', 2, '2025-06-11 17:54:43', 'WALLET7ddae63fda304ed0a39a9ff989e504a2', NULL, NULL, NULL, NULL, NULL, '2025-06-11 17:54:43', '2025-06-11 17:54:43', 1);
INSERT INTO `payment` VALUES (278, 'PAY1749726440445eb5a', 253, 'OD1749726408755f094d7', 8, 378.00, 'alipay', 2, '2025-06-12 19:08:17', 'AP2025061222001434870506785214', NULL, '2025-06-12 21:07:20', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-06-12 19:07:20', '2025-06-12 19:08:17', 2);
INSERT INTO `payment` VALUES (279, 'PAY1750998049110de21', 254, 'OD175099804314016fc06', 8, 398.00, 'wallet', 2, '2025-06-27 12:20:49', 'WALLETe935b69449224723bd90428e2859ecc2', NULL, NULL, NULL, NULL, NULL, '2025-06-27 12:20:49', '2025-06-27 12:20:49', 1);
INSERT INTO `payment` VALUES (280, 'PAY1752045192006b270', 255, 'OD175204518724432e360', 8, 899.00, 'wallet', 2, '2025-07-09 15:13:12', 'WALLET4636296ad5bb42748b8c5006074b4590', NULL, NULL, NULL, NULL, NULL, '2025-07-09 15:13:12', '2025-07-09 15:13:12', 1);
INSERT INTO `payment` VALUES (281, 'PAY17520643385658abf', 256, 'OD1752064333335be2d08', 9, 2580.00, 'wallet', 2, '2025-07-09 20:32:19', 'WALLET7292a1e490464584a330a66c6fb5c694', NULL, NULL, NULL, NULL, NULL, '2025-07-09 20:32:19', '2025-07-09 20:32:19', 1);
INSERT INTO `payment` VALUES (282, 'PAY175206596662641d7', 257, 'OD1752065959966ad2742', 9, 179.00, 'wallet', 2, '2025-07-09 20:59:27', 'WALLETb909a69f5cf64f81a0449d53913fce68', NULL, NULL, NULL, NULL, NULL, '2025-07-09 20:59:27', '2025-07-09 20:59:27', 1);
INSERT INTO `payment` VALUES (283, 'PAY17521295224322a2a', 258, 'OD17521295188585571b8', 59, 1880.00, 'alipay', 0, NULL, NULL, NULL, '2025-07-10 16:38:42', 'http://localhost:8080/api/payment/wallet/alipay/notify', 'http://localhost:8080/api/payment/wallet/alipay/return', NULL, '2025-07-10 14:38:42', '2025-07-10 14:38:42', 1);
INSERT INTO `payment` VALUES (284, 'PAY1752129621526ea74', 258, 'OD17521295188585571b8', 59, 1880.00, 'alipay', 0, NULL, NULL, NULL, '2025-07-10 16:40:22', 'http://localhost:8080/api/payment/wallet/alipay/notify', 'http://localhost:8080/api/payment/wallet/alipay/return', NULL, '2025-07-10 14:40:22', '2025-07-10 14:40:22', 1);
INSERT INTO `payment` VALUES (285, 'PAY1752130119515d000', 259, 'OD175213011582428c5fe', 59, 119.00, 'alipay', 2, '2025-07-10 14:49:11', 'AP2025071022001434870507077803', NULL, '2025-07-10 16:48:40', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-07-10 14:48:40', '2025-07-10 14:49:11', 2);
INSERT INTO `payment` VALUES (286, 'PAY17522109089929187', 260, 'OD1752210905878845ae7', 8, 89.90, 'alipay', 2, '2025-07-11 13:15:46', 'AP2025071122001434870507086968', NULL, '2025-07-11 15:15:09', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-07-11 13:15:09', '2025-07-11 13:15:46', 2);
INSERT INTO `payment` VALUES (287, 'PAY17522177662451968', 261, 'OD1752217759425d955cc', 9, 378.00, 'alipay', 2, '2025-07-11 15:09:56', 'AP2025071122001434870507089412', NULL, '2025-07-11 17:09:26', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-07-11 15:09:26', '2025-07-11 15:09:56', 2);
INSERT INTO `payment` VALUES (288, 'PAY1752218049239b2b7', 262, 'OD1752218035911b8d529', 9, 130.00, 'alipay', 2, '2025-07-11 15:14:40', 'AP2025071122001434870507083237', NULL, '2025-07-11 17:14:09', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-07-11 15:14:09', '2025-07-11 15:14:40', 2);
INSERT INTO `payment` VALUES (289, 'PAY175221820465019dd', 263, 'OD175221820097485ae4b', 9, 1830.00, 'alipay', 0, NULL, NULL, NULL, '2025-07-11 17:16:45', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-07-11 15:16:45', '2025-07-11 15:16:45', 1);
INSERT INTO `payment` VALUES (290, 'PAY175221823585266a2', 263, 'OD175221820097485ae4b', 9, 1830.00, 'alipay', 2, '2025-07-11 15:17:49', 'AP2025071122001434870507097880', NULL, '2025-07-11 17:17:16', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-07-11 15:17:16', '2025-07-11 15:17:49', 2);
INSERT INTO `payment` VALUES (291, 'PAY17526611627801fad', 264, 'OD1752661153782d5f325', 8, 399.90, 'wechat', 2, '2025-07-16 18:19:23', 'WX649ac87523f349dab462128f8ab4e013', NULL, '2025-07-16 20:19:23', NULL, NULL, NULL, '2025-07-16 18:19:23', '2025-07-16 18:19:23', 2);
INSERT INTO `payment` VALUES (292, 'PAY17526633706918cd8', 265, 'OD1752663366489bad395', 8, 179.00, 'wallet', 2, '2025-07-16 18:56:11', 'WALLET58ec7e13c47b4484881f07888e1c5de9', NULL, NULL, NULL, NULL, NULL, '2025-07-16 18:56:11', '2025-07-16 18:56:11', 1);
INSERT INTO `payment` VALUES (293, 'PAY175818504760855e7', 266, 'OD1758185044441fc2a2a', 8, 189.90, 'alipay', 0, NULL, NULL, NULL, '2025-09-18 18:44:08', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-09-18 16:44:08', '2025-09-18 16:44:08', 1);
INSERT INTO `payment` VALUES (294, 'PAY17581850978919aef', 266, 'OD1758185044441fc2a2a', 8, 189.90, 'alipay', 2, '2025-09-18 16:45:31', 'AP2025091822001434870507763800', NULL, '2025-09-18 18:44:58', 'http://localhost:8080/api/payment/alipay/notify', 'http://localhost:8080/api/payment/alipay/return', NULL, '2025-09-18 16:44:58', '2025-09-18 16:45:31', 2);
INSERT INTO `payment` VALUES (295, 'PAY17582551336424ecd', 267, 'OD17582551185296f4f05', 8, 1074.00, 'wechat', 2, '2025-09-19 12:12:17', 'WX47ea26cda1204db695bc123b706013db', NULL, '2025-09-19 14:12:14', NULL, NULL, NULL, '2025-09-19 12:12:14', '2025-09-19 12:12:17', 2);

-- ----------------------------
-- Table structure for payment_log
-- ----------------------------
DROP TABLE IF EXISTS `payment_log`;
CREATE TABLE `payment_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `payment_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单号',
  `event` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件类型',
  `status` int NULL DEFAULT NULL COMMENT '当前状态',
  `old_status` int NULL DEFAULT NULL COMMENT '旧状态',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '支付金额',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `operator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人',
  `operation_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_payment_id`(`payment_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_operation_time`(`operation_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payment_log
-- ----------------------------
INSERT INTO `payment_log` VALUES (1, '98', 'ORDER17446395616614757', 'PAYMENT_CREATED', 0, NULL, 9.90, '创建支付请求', '9', '2025-04-14 22:06:03', '0:0:0:0:0:0:0:1', '2025-04-14 22:06:03');
INSERT INTO `payment_log` VALUES (2, '99', 'ORDER17446955490614819', 'PAYMENT_CREATED', 0, NULL, 49.90, '创建支付请求', '9', '2025-04-15 13:39:12', '0:0:0:0:0:0:0:1', '2025-04-15 13:39:12');
INSERT INTO `payment_log` VALUES (3, '100', 'ORDER17448059023224799', 'PAYMENT_CREATED', 0, NULL, 1019.40, '创建支付请求', '9', '2025-04-16 20:18:25', '0:0:0:0:0:0:0:1', '2025-04-16 20:18:25');
INSERT INTO `payment_log` VALUES (4, '101', 'ORDER17448076037709411', 'PAYMENT_CREATED', 0, NULL, 379.80, '创建支付请求', '9', '2025-04-16 20:46:54', '0:0:0:0:0:0:0:1', '2025-04-16 20:46:53');
INSERT INTO `payment_log` VALUES (5, '102', 'ORDER17448083186517324', 'PAYMENT_CREATED', 0, NULL, 239.60, '创建支付请求', '9', '2025-04-16 20:58:42', '0:0:0:0:0:0:0:1', '2025-04-16 20:58:42');
INSERT INTO `payment_log` VALUES (6, '103', 'ORDER17448106746512253', 'PAYMENT_CREATED', 0, NULL, 379.80, '创建支付请求', '9', '2025-04-16 21:37:57', '0:0:0:0:0:0:0:1', '2025-04-16 21:37:57');
INSERT INTO `payment_log` VALUES (7, '104', 'ORDER17449398848975895', 'PAYMENT_CREATED', 0, NULL, 889.70, '创建支付请求', '9', '2025-04-18 09:31:33', '0:0:0:0:0:0:0:1', '2025-04-18 09:31:32');
INSERT INTO `payment_log` VALUES (8, '105', 'ORDER17449528547744283', 'PAYMENT_CREATED', 0, NULL, 59.90, '创建支付请求', '9', '2025-04-18 13:07:36', '0:0:0:0:0:0:0:1', '2025-04-18 13:07:36');
INSERT INTO `payment_log` VALUES (9, '106', 'ORDER17449795558219373', 'PAYMENT_CREATED', 0, NULL, 0.01, '创建支付请求', '9', '2025-04-18 20:32:47', '0:0:0:0:0:0:0:1', '2025-04-18 20:32:47');
INSERT INTO `payment_log` VALUES (10, '107', 'ORDER17449803210071190', 'PAYMENT_CREATED', 0, NULL, 179.90, '创建支付请求', '9', '2025-04-18 20:45:22', '0:0:0:0:0:0:0:1', '2025-04-18 20:45:22');
INSERT INTO `payment_log` VALUES (11, '108', 'ORDER17449807430696568', 'PAYMENT_CREATED', 0, NULL, 139.00, '创建支付请求', '9', '2025-04-18 20:52:24', '0:0:0:0:0:0:0:1', '2025-04-18 20:52:24');
INSERT INTO `payment_log` VALUES (12, '109', 'ORDER17449807430696568', 'PAYMENT_CREATED', 0, NULL, 139.00, '创建支付请求', '9', '2025-04-18 20:52:33', '0:0:0:0:0:0:0:1', '2025-04-18 20:52:32');
INSERT INTO `payment_log` VALUES (13, '110', 'ORDER17449813436552135', 'PAYMENT_CREATED', 0, NULL, 89.90, '创建支付请求', '9', '2025-04-18 21:02:27', '0:0:0:0:0:0:0:1', '2025-04-18 21:02:27');
INSERT INTO `payment_log` VALUES (14, '111', 'ORDER17449816129176380', 'PAYMENT_CREATED', 0, NULL, 369.90, '创建支付请求', '9', '2025-04-18 21:06:55', '0:0:0:0:0:0:0:1', '2025-04-18 21:06:55');
INSERT INTO `payment_log` VALUES (15, '112', 'ORDER17449816129176380', 'PAYMENT_CREATED', 0, NULL, 369.90, '创建支付请求', '9', '2025-04-18 21:08:53', '0:0:0:0:0:0:0:1', '2025-04-18 21:08:53');
INSERT INTO `payment_log` VALUES (16, '113', 'ORDER17449819551205775', 'PAYMENT_CREATED', 0, NULL, 179.90, '创建支付请求', '9', '2025-04-18 21:12:37', '0:0:0:0:0:0:0:1', '2025-04-18 21:12:36');
INSERT INTO `payment_log` VALUES (17, '114', 'ORDER17449816129176380', 'PAYMENT_CREATED', 0, NULL, 369.90, '创建支付请求', '9', '2025-04-18 21:13:33', '0:0:0:0:0:0:0:1', '2025-04-18 21:13:32');
INSERT INTO `payment_log` VALUES (18, '115', 'ORDER17449820595288082', 'PAYMENT_CREATED', 0, NULL, 129.00, '创建支付请求', '9', '2025-04-18 21:14:24', '0:0:0:0:0:0:0:1', '2025-04-18 21:14:23');
INSERT INTO `payment_log` VALUES (19, '116', 'ORDER17449829297755677', 'PAYMENT_CREATED', 0, NULL, 179.90, '创建支付请求', '9', '2025-04-18 21:28:58', '0:0:0:0:0:0:0:1', '2025-04-18 21:28:57');
INSERT INTO `payment_log` VALUES (20, '117', 'ORDER17449833960601450', 'PAYMENT_CREATED', 0, NULL, 369.90, '创建支付请求', '9', '2025-04-18 21:36:50', '0:0:0:0:0:0:0:1', '2025-04-18 21:36:49');
INSERT INTO `payment_log` VALUES (21, '118', 'ORDER17449833960601450', 'PAYMENT_CREATED', 0, NULL, 369.90, '创建支付请求', '9', '2025-04-18 21:37:23', '0:0:0:0:0:0:0:1', '2025-04-18 21:37:23');
INSERT INTO `payment_log` VALUES (22, '119', 'ORDER17449833960601450', 'PAYMENT_CREATED', 0, NULL, 369.90, '创建支付请求', '9', '2025-04-18 21:37:42', '0:0:0:0:0:0:0:1', '2025-04-18 21:37:42');
INSERT INTO `payment_log` VALUES (23, '120', 'ORDER17449833960601450', 'PAYMENT_CREATED', 0, NULL, 369.90, '创建支付请求', '9', '2025-04-18 21:37:53', '0:0:0:0:0:0:0:1', '2025-04-18 21:37:53');
INSERT INTO `payment_log` VALUES (24, '121', 'ORDER17449839678137295', 'PAYMENT_CREATED', 0, NULL, 389.90, '创建支付请求', '9', '2025-04-18 21:46:10', '0:0:0:0:0:0:0:1', '2025-04-18 21:46:09');
INSERT INTO `payment_log` VALUES (25, '122', 'ORDER17449840414779475', 'PAYMENT_CREATED', 0, NULL, 1009.60, '创建支付请求', '9', '2025-04-18 21:47:25', '0:0:0:0:0:0:0:1', '2025-04-18 21:47:24');
INSERT INTO `payment_log` VALUES (26, '123', 'ORDER17449844147359468', 'PAYMENT_CREATED', 0, NULL, 2969.00, '创建支付请求', '9', '2025-04-18 21:53:38', '0:0:0:0:0:0:0:1', '2025-04-18 21:53:37');
INSERT INTO `payment_log` VALUES (27, '124', 'ORDER17449844147359468', 'PAYMENT_CREATED', 0, NULL, 2969.00, '创建支付请求', '9', '2025-04-18 21:53:56', '0:0:0:0:0:0:0:1', '2025-04-18 21:53:55');
INSERT INTO `payment_log` VALUES (28, '125', 'ORDER17449845632983598', 'PAYMENT_CREATED', 0, NULL, 268.00, '创建支付请求', '9', '2025-04-18 21:56:05', '0:0:0:0:0:0:0:1', '2025-04-18 21:56:04');
INSERT INTO `payment_log` VALUES (29, '126', 'ORDER17449852325764091', 'PAYMENT_CREATED', 0, NULL, 2058.90, '创建支付请求', '9', '2025-04-18 22:07:16', '0:0:0:0:0:0:0:1', '2025-04-18 22:07:16');
INSERT INTO `payment_log` VALUES (30, '127', 'ORDER17449852869362755', 'PAYMENT_CREATED', 0, NULL, 864.00, '创建支付请求', '9', '2025-04-18 22:08:14', '0:0:0:0:0:0:0:1', '2025-04-18 22:08:13');
INSERT INTO `payment_log` VALUES (31, '128', 'ORDER17449852869362755', 'PAYMENT_CREATED', 0, NULL, 864.00, '创建支付请求', '9', '2025-04-18 22:08:19', '0:0:0:0:0:0:0:1', '2025-04-18 22:08:18');
INSERT INTO `payment_log` VALUES (32, '129', 'ORDER17449871938104796', 'PAYMENT_CREATED', 0, NULL, 748.70, '创建支付请求', '9', '2025-04-18 22:40:01', '0:0:0:0:0:0:0:1', '2025-04-18 22:40:00');
INSERT INTO `payment_log` VALUES (33, '130', 'ORDER17449879317155084', 'PAYMENT_CREATED', 0, NULL, 2286.00, '创建支付请求', '9', '2025-04-18 22:52:16', '0:0:0:0:0:0:0:1', '2025-04-18 22:52:16');
INSERT INTO `payment_log` VALUES (34, '131', 'ORDER17449879317155084', 'PAYMENT_CREATED', 0, NULL, 2286.00, '创建支付请求', '9', '2025-04-18 22:52:49', '0:0:0:0:0:0:0:1', '2025-04-18 22:52:48');
INSERT INTO `payment_log` VALUES (35, '132', 'ORDER17449879795469034', 'PAYMENT_CREATED', 0, NULL, 2266.00, '创建支付请求', '9', '2025-04-18 22:53:02', '0:0:0:0:0:0:0:1', '2025-04-18 22:53:01');
INSERT INTO `payment_log` VALUES (36, '133', 'ORDER17450441292649624', 'PAYMENT_CREATED', 0, NULL, 417.00, '创建支付请求', '9', '2025-04-19 14:28:59', '0:0:0:0:0:0:0:1', '2025-04-19 14:28:59');
INSERT INTO `payment_log` VALUES (37, '134', 'ORDER17450450886945756', 'PAYMENT_CREATED', 0, NULL, 269.70, '创建支付请求', '9', '2025-04-19 14:44:50', '0:0:0:0:0:0:0:1', '2025-04-19 14:44:50');
INSERT INTO `payment_log` VALUES (38, '134', 'ORDER17450450886945756', 'GENERATE_FORM', 1, NULL, 269.70, '生成支付宝沙盒表单', 'SYSTEM', '2025-04-19 14:44:50', '0:0:0:0:0:0:0:1', '2025-04-19 14:44:50');
INSERT INTO `payment_log` VALUES (39, '135', 'ORDER17450451873077695', 'PAYMENT_CREATED', 0, NULL, 289.60, '创建支付请求', '9', '2025-04-19 14:46:29', '0:0:0:0:0:0:0:1', '2025-04-19 14:46:28');
INSERT INTO `payment_log` VALUES (40, '136', 'ORDER17450451873077695', 'PAYMENT_CREATED', 0, NULL, 289.60, '创建支付请求', '9', '2025-04-19 14:46:44', '0:0:0:0:0:0:0:1', '2025-04-19 14:46:44');
INSERT INTO `payment_log` VALUES (41, '137', 'ORDER17450451873077695', 'PAYMENT_CREATED', 0, NULL, 289.60, '创建支付请求', '9', '2025-04-19 14:46:59', '0:0:0:0:0:0:0:1', '2025-04-19 14:46:58');
INSERT INTO `payment_log` VALUES (42, '138', 'ORDER17450453576811598', 'PAYMENT_CREATED', 0, NULL, 99.90, '创建支付请求', '9', '2025-04-19 14:49:19', '0:0:0:0:0:0:0:1', '2025-04-19 14:49:19');
INSERT INTO `payment_log` VALUES (43, '139', 'ORDER17450459875746430', 'CREATE', 0, NULL, 399.90, '创建支付记录', '9', '2025-04-19 14:59:50', '0:0:0:0:0:0:0:1', '2025-04-19 14:59:49');
INSERT INTO `payment_log` VALUES (44, '140', 'ORDER17450473725228396', 'PAYMENT_CREATED', 0, NULL, 59.90, '创建支付请求', '9', '2025-04-19 15:22:56', '0:0:0:0:0:0:0:1', '2025-04-19 15:22:55');
INSERT INTO `payment_log` VALUES (45, '141', 'ORDER17450478614373007', 'PAYMENT_CREATED', 0, NULL, 369.90, '创建支付请求', '9', '2025-04-19 15:31:04', '0:0:0:0:0:0:0:1', '2025-04-19 15:31:03');
INSERT INTO `payment_log` VALUES (46, '142', 'ORDER17450486780378791', 'PAYMENT_CREATED', 0, NULL, 209.60, '创建支付请求', '9', '2025-04-19 15:44:41', '0:0:0:0:0:0:0:1', '2025-04-19 15:44:41');
INSERT INTO `payment_log` VALUES (47, '143', 'ORDER17450499740802107', 'PAYMENT_CREATED', 0, NULL, 2769.30, '创建支付请求', '9', '2025-04-19 16:06:19', '0:0:0:0:0:0:0:1', '2025-04-19 16:06:18');
INSERT INTO `payment_log` VALUES (48, '144', 'ORDER17450503412792999', 'PAYMENT_CREATED', 0, NULL, 268.00, '创建支付请求', '9', '2025-04-19 16:12:23', '0:0:0:0:0:0:0:1', '2025-04-19 16:12:22');
INSERT INTO `payment_log` VALUES (49, '145', 'ORDER17450503412792999', 'PAYMENT_CREATED', 0, NULL, 268.00, '创建支付请求', '9', '2025-04-19 16:14:53', '0:0:0:0:0:0:0:1', '2025-04-19 16:14:53');
INSERT INTO `payment_log` VALUES (50, '146', 'ORDER17450503412792999', 'PAYMENT_CREATED', 0, NULL, 268.00, '创建支付请求', '9', '2025-04-19 16:15:02', '0:0:0:0:0:0:0:1', '2025-04-19 16:15:01');
INSERT INTO `payment_log` VALUES (51, '147', 'ORDER17450548263311761', 'PAYMENT_CREATED', 0, NULL, 89960.00, '创建支付请求', '9', '2025-04-19 17:27:09', '0:0:0:0:0:0:0:1', '2025-04-19 17:27:09');
INSERT INTO `payment_log` VALUES (52, '148', 'ORDER17450703688316173', 'PAYMENT_CREATED', 0, NULL, 1918.50, '创建支付请求', '9', '2025-04-19 21:46:11', '0:0:0:0:0:0:0:1', '2025-04-19 21:46:11');
INSERT INTO `payment_log` VALUES (53, '149', 'ORDER17450713988864410', 'PAYMENT_CREATED', 0, NULL, 1169.60, '创建支付请求', '9', '2025-04-19 22:03:22', '0:0:0:0:0:0:0:1', '2025-04-19 22:03:21');
INSERT INTO `payment_log` VALUES (54, '150', 'ORDER17451140395427141', 'PAYMENT_CREATED', 0, NULL, 2178.30, '创建支付请求', '9', '2025-04-20 09:54:33', '0:0:0:0:0:0:0:1', '2025-04-20 09:54:32');
INSERT INTO `payment_log` VALUES (55, '151', 'ORDER17451144733955838', 'PAYMENT_CREATED', 0, NULL, 2369.40, '创建支付请求', '9', '2025-04-20 10:01:44', '0:0:0:0:0:0:0:1', '2025-04-20 10:01:43');
INSERT INTO `payment_log` VALUES (56, '152', 'ORDER17451278084316405', 'PAYMENT_CREATED', 0, NULL, 869.10, '创建支付请求', '9', '2025-04-20 13:43:35', '0:0:0:0:0:0:0:1', '2025-04-20 13:43:35');
INSERT INTO `payment_log` VALUES (57, '153', 'ORDER17451278084316405', 'PAYMENT_CREATED', 0, NULL, 869.10, '创建支付请求', '9', '2025-04-20 13:43:45', '0:0:0:0:0:0:0:1', '2025-04-20 13:43:45');
INSERT INTO `payment_log` VALUES (58, '154', 'ORDER17451301068395898', 'PAYMENT_CREATED', 0, NULL, 329.40, '创建支付请求', '9', '2025-04-20 14:21:51', '0:0:0:0:0:0:0:1', '2025-04-20 14:21:51');
INSERT INTO `payment_log` VALUES (59, '155', 'ORDER17451304374482107', 'PAYMENT_CREATED', 0, NULL, 329.40, '创建支付请求', '9', '2025-04-20 14:27:20', '0:0:0:0:0:0:0:1', '2025-04-20 14:27:20');
INSERT INTO `payment_log` VALUES (60, '156', 'ORDER17451305201237152', 'PAYMENT_CREATED', 0, NULL, 1109.40, '创建支付请求', '9', '2025-04-20 14:28:43', '0:0:0:0:0:0:0:1', '2025-04-20 14:28:42');
INSERT INTO `payment_log` VALUES (61, '157', 'ORDER17451407073845313', 'PAYMENT_CREATED', 0, NULL, 999.40, '创建支付请求', '9', '2025-04-20 17:18:31', '0:0:0:0:0:0:0:1', '2025-04-20 17:18:30');
INSERT INTO `payment_log` VALUES (62, '158', 'ORDER17451448487674395', 'PAYMENT_CREATED', 0, NULL, 169.90, '创建支付请求', '9', '2025-04-20 18:27:32', '0:0:0:0:0:0:0:1', '2025-04-20 18:27:31');
INSERT INTO `payment_log` VALUES (63, '159', 'ORDER17451457560899513', 'PAYMENT_CREATED', 0, NULL, 269.70, '创建支付请求', '9', '2025-04-20 18:42:39', '0:0:0:0:0:0:0:1', '2025-04-20 18:42:39');
INSERT INTO `payment_log` VALUES (64, '160', 'ORDER17451458509872621', 'PAYMENT_CREATED', 0, NULL, 539.70, '创建支付请求', '9', '2025-04-20 18:44:16', '0:0:0:0:0:0:0:1', '2025-04-20 18:44:16');
INSERT INTO `payment_log` VALUES (65, '161', 'ORDER17451460141667503', 'PAYMENT_CREATED', 0, NULL, 1127.80, '创建支付请求', '9', '2025-04-20 18:46:57', '0:0:0:0:0:0:0:1', '2025-04-20 18:46:57');
INSERT INTO `payment_log` VALUES (66, '162', 'ORDER17451462811964198', 'PAYMENT_CREATED', 0, NULL, 169.90, '创建支付请求', '9', '2025-04-20 18:51:26', '0:0:0:0:0:0:0:1', '2025-04-20 18:51:26');
INSERT INTO `payment_log` VALUES (67, '163', 'ORDER17452114489074352', 'PAYMENT_CREATED', 0, NULL, 59.90, '创建支付请求', '9', '2025-04-21 12:57:45', '0:0:0:0:0:0:0:1', '2025-04-21 12:57:44');
INSERT INTO `payment_log` VALUES (68, '164', 'ORDER17452132971645998', 'PAYMENT_CREATED', 0, NULL, 289.60, '创建支付请求', '9', '2025-04-21 13:28:21', '0:0:0:0:0:0:0:1', '2025-04-21 13:28:20');
INSERT INTO `payment_log` VALUES (69, '165', 'ORDER17452140968006383', 'PAYMENT_CREATED', 0, NULL, 139.00, '创建支付请求', '9', '2025-04-21 13:42:15', '0:0:0:0:0:0:0:1', '2025-04-21 13:42:15');
INSERT INTO `payment_log` VALUES (70, '166', 'ORDER17452140968006383', 'PAYMENT_CREATED', 0, NULL, 139.00, '创建支付请求', '9', '2025-04-21 13:42:22', '0:0:0:0:0:0:0:1', '2025-04-21 13:42:22');
INSERT INTO `payment_log` VALUES (71, '167', 'ORDER17452140968006383', 'PAYMENT_CREATED', 0, NULL, 139.00, '创建支付请求', '9', '2025-04-21 13:42:23', '0:0:0:0:0:0:0:1', '2025-04-21 13:42:23');
INSERT INTO `payment_log` VALUES (72, '168', 'ORDER17452142961221645', 'PAYMENT_CREATED', 0, NULL, 369.90, '创建支付请求', '9', '2025-04-21 13:45:00', '0:0:0:0:0:0:0:1', '2025-04-21 13:44:59');
INSERT INTO `payment_log` VALUES (73, '169', 'ORDER17452145246841901', 'PAYMENT_CREATED', 0, NULL, 539.70, '创建支付请求', '9', '2025-04-21 13:48:50', '0:0:0:0:0:0:0:1', '2025-04-21 13:48:49');
INSERT INTO `payment_log` VALUES (74, '170', 'ORDER17452148490214231', 'PAYMENT_CREATED', 0, NULL, 539.70, '创建支付请求', '9', '2025-04-21 13:54:13', '0:0:0:0:0:0:0:1', '2025-04-21 13:54:12');
INSERT INTO `payment_log` VALUES (75, '171', 'ORDER17452150243901075', 'PAYMENT_CREATED', 0, NULL, 169.90, '创建支付请求', '9', '2025-04-21 13:57:14', '0:0:0:0:0:0:0:1', '2025-04-21 13:57:14');
INSERT INTO `payment_log` VALUES (76, '172', 'ORDER17452154110214171', 'PAYMENT_CREATED', 0, NULL, 369.90, '创建支付请求', '9', '2025-04-21 14:03:37', '0:0:0:0:0:0:0:1', '2025-04-21 14:03:36');
INSERT INTO `payment_log` VALUES (77, '173', 'ORDER17452160264701642', 'PAYMENT_CREATED', 0, NULL, 1569.60, '创建支付请求', '9', '2025-04-21 14:14:12', '0:0:0:0:0:0:0:1', '2025-04-21 14:14:12');
INSERT INTO `payment_log` VALUES (78, '174', 'ORDER17452167814059647', 'PAYMENT_CREATED', 0, NULL, 169.90, '创建支付请求', '9', '2025-04-21 14:26:27', '0:0:0:0:0:0:0:1', '2025-04-21 14:26:26');
INSERT INTO `payment_log` VALUES (79, '175', 'ORDER17452172041521840', 'PAYMENT_CREATED', 0, NULL, 389.30, '创建支付请求', '9', '2025-04-21 14:33:32', '0:0:0:0:0:0:0:1', '2025-04-21 14:33:31');
INSERT INTO `payment_log` VALUES (80, '176', 'ORDER17452186371897636', 'PAYMENT_CREATED', 0, NULL, 919.50, '创建支付请求', '9', '2025-04-21 14:57:21', '0:0:0:0:0:0:0:1', '2025-04-21 14:57:21');
INSERT INTO `payment_log` VALUES (81, '177', 'ORDER17452193667296638', 'PAYMENT_CREATED', 0, NULL, 269.70, '创建支付请求', '9', '2025-04-21 15:09:30', '0:0:0:0:0:0:0:1', '2025-04-21 15:09:30');
INSERT INTO `payment_log` VALUES (82, '178', 'ORDER17452193970978567', 'PAYMENT_CREATED', 0, NULL, 1169.60, '创建支付请求', '9', '2025-04-21 15:10:08', '0:0:0:0:0:0:0:1', '2025-04-21 15:10:07');
INSERT INTO `payment_log` VALUES (83, '179', 'ORDER17452201961333527', 'PAYMENT_CREATED', 0, NULL, 1769.40, '创建支付请求', '9', '2025-04-21 15:23:20', '0:0:0:0:0:0:0:1', '2025-04-21 15:23:19');
INSERT INTO `payment_log` VALUES (84, '180', 'ORDER17452202440941328', 'PAYMENT_CREATED', 0, NULL, 509.40, '创建支付请求', '9', '2025-04-21 15:24:07', '0:0:0:0:0:0:0:1', '2025-04-21 15:24:06');
INSERT INTO `payment_log` VALUES (85, '181', 'ORDER17452207018446413', 'PAYMENT_CREATED', 0, NULL, 419.10, '创建支付请求', '9', '2025-04-21 15:31:49', '0:0:0:0:0:0:0:1', '2025-04-21 15:31:49');
INSERT INTO `payment_log` VALUES (86, '182', 'ORDER17452212896086413', 'PAYMENT_CREATED', 0, NULL, 869.00, '创建支付请求', '9', '2025-04-21 15:41:37', '0:0:0:0:0:0:0:1', '2025-04-21 15:41:37');
INSERT INTO `payment_log` VALUES (87, '183', 'ORDER17452215674805396', 'PAYMENT_CREATED', 0, NULL, 242.00, '创建支付请求', '9', '2025-04-21 15:46:21', '0:0:0:0:0:0:0:1', '2025-04-21 15:46:20');
INSERT INTO `payment_log` VALUES (88, '184', 'ORDER17452218697879314', 'PAYMENT_CREATED', 0, NULL, 619.50, '创建支付请求', '9', '2025-04-21 15:51:35', '0:0:0:0:0:0:0:1', '2025-04-21 15:51:34');
INSERT INTO `payment_log` VALUES (89, '185', 'ORDER17452396871972804', 'PAYMENT_CREATED', 0, NULL, 169.90, '创建支付请求', '9', '2025-04-21 20:48:11', '0:0:0:0:0:0:0:1', '2025-04-21 20:48:10');
INSERT INTO `payment_log` VALUES (90, '186', 'ORDER17452402212747378', 'PAYMENT_CREATED', 0, NULL, 566.00, '创建支付请求', '9', '2025-04-21 20:57:12', '0:0:0:0:0:0:0:1', '2025-04-21 20:57:12');
INSERT INTO `payment_log` VALUES (91, '187', 'ORDER17452428483553410', 'PAYMENT_CREATED', 0, NULL, 1969.50, '创建支付请求', '9', '2025-04-21 21:40:54', '0:0:0:0:0:0:0:1', '2025-04-21 21:40:54');
INSERT INTO `payment_log` VALUES (92, '188', 'ORDER17453246643974732', 'PAYMENT_CREATED', 0, NULL, 209.60, '创建支付请求', '9', '2025-04-22 20:24:28', '0:0:0:0:0:0:0:1', '2025-04-22 20:24:27');
INSERT INTO `payment_log` VALUES (93, '189', 'ORDER17453248963623581', 'PAYMENT_CREATED', 0, NULL, 129.00, '创建支付请求', '9', '2025-04-22 20:28:19', '0:0:0:0:0:0:0:1', '2025-04-22 20:28:19');
INSERT INTO `payment_log` VALUES (94, '190', 'ORDER17453255706674318', 'PAYMENT_CREATED', 0, NULL, 469.50, '创建支付请求', '9', '2025-04-22 20:39:35', '0:0:0:0:0:0:0:1', '2025-04-22 20:39:35');
INSERT INTO `payment_log` VALUES (95, '191', 'ORDER17453264196027857', 'PAYMENT_CREATED', 0, NULL, 759.60, '创建支付请求', '9', '2025-04-22 20:53:43', '0:0:0:0:0:0:0:1', '2025-04-22 20:53:43');
INSERT INTO `payment_log` VALUES (96, '192', 'ORDER17453294836071112', 'PAYMENT_CREATED', 0, NULL, 1969.50, '创建支付请求', '9', '2025-04-22 21:44:48', '0:0:0:0:0:0:0:1', '2025-04-22 21:44:47');
INSERT INTO `payment_log` VALUES (97, '193', 'ORDER17453295353832425', 'PAYMENT_CREATED', 0, NULL, 369.60, '创建支付请求', '9', '2025-04-22 21:45:40', '0:0:0:0:0:0:0:1', '2025-04-22 21:45:39');
INSERT INTO `payment_log` VALUES (98, '194', 'ORDER17453297779016486', 'PAYMENT_CREATED', 0, NULL, 1569.60, '创建支付请求', '9', '2025-04-22 21:49:42', '0:0:0:0:0:0:0:1', '2025-04-22 21:49:41');
INSERT INTO `payment_log` VALUES (99, '195', 'ORDER17453886695259124', 'PAYMENT_CREATED', 0, NULL, 99.90, '创建支付请求', '9', '2025-04-23 14:11:22', '0:0:0:0:0:0:0:1', '2025-04-23 14:11:21');
INSERT INTO `payment_log` VALUES (100, '196', 'ORDER17454113357745166', 'PAYMENT_CREATED', 0, NULL, 729.60, '创建支付请求', '9', '2025-04-23 20:29:00', '0:0:0:0:0:0:0:1', '2025-04-23 20:28:59');
INSERT INTO `payment_log` VALUES (101, '197', 'ORDER17454133754631831', 'PAYMENT_CREATED', 0, NULL, 179.90, '创建支付请求', '9', '2025-04-23 21:02:58', '0:0:0:0:0:0:0:1', '2025-04-23 21:02:58');
INSERT INTO `payment_log` VALUES (102, '198', 'ORDER17454143481457218', 'PAYMENT_CREATED', 0, NULL, 769.60, '创建支付请求', '9', '2025-04-23 21:19:11', '0:0:0:0:0:0:0:1', '2025-04-23 21:19:11');
INSERT INTO `payment_log` VALUES (103, '199', 'ORDER17454145288759162', 'PAYMENT_CREATED', 0, NULL, 369.20, '创建支付请求', '9', '2025-04-23 21:22:52', '0:0:0:0:0:0:0:1', '2025-04-23 21:22:51');
INSERT INTO `payment_log` VALUES (104, '200', 'ORDER17454153673359147', 'PAYMENT_CREATED', 0, NULL, 469.60, '创建支付请求', '9', '2025-04-23 21:36:13', '0:0:0:0:0:0:0:1', '2025-04-23 21:36:12');
INSERT INTO `payment_log` VALUES (105, '201', 'ORDER17454712732296454', 'PAYMENT_CREATED', 0, NULL, 49.90, '创建支付请求', '9', '2025-04-24 13:08:04', '0:0:0:0:0:0:0:1', '2025-04-24 13:08:03');
INSERT INTO `payment_log` VALUES (106, '202', 'ORDER17454721117056277', 'PAYMENT_CREATED', 0, NULL, 2384.40, '创建支付请求', '9', '2025-04-24 13:21:57', '0:0:0:0:0:0:0:1', '2025-04-24 13:21:56');
INSERT INTO `payment_log` VALUES (107, '203', 'ORDER17454725349573471', 'PAYMENT_CREATED', 0, NULL, 1149.60, '创建支付请求', '9', '2025-04-24 13:29:01', '0:0:0:0:0:0:0:1', '2025-04-24 13:29:00');
INSERT INTO `payment_log` VALUES (108, '204', 'ORDER17456516991175745', 'PAYMENT_CREATED', 0, NULL, 984.00, '创建支付请求', '9', '2025-04-26 15:15:03', '0:0:0:0:0:0:0:1', '2025-04-26 15:15:02');
INSERT INTO `payment_log` VALUES (109, '205', 'ORDER17456796217205119', 'PAYMENT_CREATED', 0, NULL, 99.90, '创建支付请求', '9', '2025-04-26 23:00:25', '0:0:0:0:0:0:0:1', '2025-04-26 23:00:25');
INSERT INTO `payment_log` VALUES (110, '206', 'ORDER17456797301057465', 'PAYMENT_CREATED', 0, NULL, 189.90, '创建支付请求', '9', '2025-04-26 23:02:15', '0:0:0:0:0:0:0:1', '2025-04-26 23:02:14');

-- ----------------------------
-- Table structure for payment_state_log
-- ----------------------------
DROP TABLE IF EXISTS `payment_state_log`;
CREATE TABLE `payment_state_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `payment_id` bigint NOT NULL COMMENT '支付ID',
  `payment_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付单号',
  `order_id` int NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `old_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原状态',
  `new_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '新状态',
  `event` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发事件',
  `operator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作者',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_payment_id`(`payment_id` ASC) USING BTREE,
  INDEX `idx_payment_no`(`payment_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付状态变更日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of payment_state_log
-- ----------------------------

-- ----------------------------
-- Table structure for points_exchange
-- ----------------------------
DROP TABLE IF EXISTS `points_exchange`;
CREATE TABLE `points_exchange`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '兑换单号',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '兑换数量',
  `points` int NOT NULL COMMENT '消耗积分',
  `address_id` int UNSIGNED NULL DEFAULT NULL COMMENT '收货地址ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号码',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态(0:待发货,1:已发货,2:已完成,3:已取消)',
  `tracking_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流单号',
  `tracking_company` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `fk_exchange_address`(`address_id` ASC) USING BTREE,
  CONSTRAINT `fk_exchange_address` FOREIGN KEY (`address_id`) REFERENCES `user_address` (`address_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_exchange_goods` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_exchange_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分兑换记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of points_exchange
-- ----------------------------
INSERT INTO `points_exchange` VALUES (1, 'PE20250320001', 3, 1, 1, 2000, 3, '13800000002', 2, 'SF1234567890', '顺丰速运', '工作日送货', '2025-03-20 14:30:00', '2025-03-20 16:20:00');
INSERT INTO `points_exchange` VALUES (2, 'PE20250320002', 4, 2, 1, 500, NULL, '15222366958', 2, NULL, NULL, NULL, '2025-03-20 10:15:00', '2025-03-20 10:30:00');
INSERT INTO `points_exchange` VALUES (3, 'PE20250320003', 8, 6, 1, 2500, 5, '13888144526', 1, 'YT9876543210', '圆通速递', '周末送货', '2025-03-20 09:45:00', '2025-03-20 14:20:00');
INSERT INTO `points_exchange` VALUES (4, 'PE20250320004', 3, 3, 1, 1000, NULL, NULL, 2, NULL, NULL, NULL, '2025-03-20 16:30:00', '2025-03-20 16:35:00');
INSERT INTO `points_exchange` VALUES (5, 'PE20250320005', 2, 4, 1, 3500, 1, '13800000001', 0, NULL, NULL, '请尽快发货', '2025-03-20 11:20:00', '2025-03-20 11:20:00');
INSERT INTO `points_exchange` VALUES (6, 'PE20250320006', 8, 5, 2, 200, NULL, NULL, 2, NULL, NULL, NULL, '2025-03-20 15:10:00', '2025-03-20 15:20:00');
INSERT INTO `points_exchange` VALUES (7, 'PE20250320007', 4, 8, 1, 1500, NULL, '15222366958', 3, NULL, NULL, '用户申请取消', '2025-03-20 13:45:00', '2025-03-20 14:30:00');
INSERT INTO `points_exchange` VALUES (8, 'PE20250320008', 8, 7, 1, 300, NULL, '13888144526', 2, NULL, NULL, NULL, '2025-03-20 17:25:00', '2025-03-20 17:35:00');
INSERT INTO `points_exchange` VALUES (9, 'PE20250320009', 2, 11, 1, 10000, NULL, NULL, 2, NULL, NULL, '系统自动处理', '2025-03-20 10:50:00', '2025-03-20 11:00:00');
INSERT INTO `points_exchange` VALUES (10, 'PE20250320010', 9, 13, 3, 600, NULL, NULL, 0, NULL, NULL, '多优惠券兑换', '2025-03-20 14:15:00', '2025-03-20 14:15:00');
INSERT INTO `points_exchange` VALUES (12, 'PE20250324507124', 8, 5, 1, 100, NULL, NULL, 0, NULL, NULL, '', '2025-03-24 13:06:45', '2025-03-24 13:06:45');

-- ----------------------------
-- Table structure for points_history
-- ----------------------------
DROP TABLE IF EXISTS `points_history`;
CREATE TABLE `points_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points` int NOT NULL COMMENT '积分变动数量（正为增加，负为减少）',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：earn(获得), spend(消费)',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源：order(订单), signin(签到), review(评论), register(注册), exchange(兑换)等',
  `reference_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联ID（如订单ID等）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 153 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分历史记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of points_history
-- ----------------------------
INSERT INTO `points_history` VALUES (1, 3, 100, 'earn', 'register', NULL, '注册奖励', '2025-01-15 10:30:00');
INSERT INTO `points_history` VALUES (2, 3, 200, 'earn', 'order', '102', '购物奖励', '2025-03-01 10:48:22');
INSERT INTO `points_history` VALUES (3, 3, 50, 'earn', 'review', '2', '评价奖励', '2025-03-01 10:52:15');
INSERT INTO `points_history` VALUES (4, 3, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-10 09:15:22');
INSERT INTO `points_history` VALUES (5, 3, -300, 'spend', 'exchange', 'E100', '兑换优惠券', '2025-03-05 16:30:45');
INSERT INTO `points_history` VALUES (6, 4, 100, 'earn', 'register', NULL, '注册奖励', '2025-01-10 09:15:22');
INSERT INTO `points_history` VALUES (7, 4, 500, 'earn', 'order', '103', '购物奖励', '2025-03-02 08:40:15');
INSERT INTO `points_history` VALUES (8, 4, 50, 'earn', 'review', '3', '评价奖励', '2025-03-02 08:45:30');
INSERT INTO `points_history` VALUES (9, 4, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-12 10:22:35');
INSERT INTO `points_history` VALUES (10, 4, 200, 'earn', 'invite', 'U10', '邀请好友', '2025-03-08 14:36:47');
INSERT INTO `points_history` VALUES (11, 5, 100, 'earn', 'register', NULL, '注册奖励', '2025-02-20 14:30:45');
INSERT INTO `points_history` VALUES (12, 5, 1000, 'earn', 'order', '104', '购物奖励', '2025-03-03 16:35:27');
INSERT INTO `points_history` VALUES (13, 5, 50, 'earn', 'review', '4', '评价奖励', '2025-03-03 16:40:12');
INSERT INTO `points_history` VALUES (14, 5, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-15 09:28:45');
INSERT INTO `points_history` VALUES (15, 5, -500, 'spend', 'exchange', 'E101', '兑换礼品', '2025-03-10 11:36:29');
INSERT INTO `points_history` VALUES (16, 6, 100, 'earn', 'register', NULL, '注册奖励', '2025-01-05 16:45:30');
INSERT INTO `points_history` VALUES (17, 6, 800, 'earn', 'order', '105', '购物奖励', '2025-03-04 12:18:42');
INSERT INTO `points_history` VALUES (18, 6, 50, 'earn', 'review', '5', '评价奖励', '2025-03-04 12:25:30');
INSERT INTO `points_history` VALUES (19, 6, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-18 08:32:15');
INSERT INTO `points_history` VALUES (20, 6, 1000, 'earn', 'promotion', 'P10', '促销活动奖励', '2025-03-15 15:42:38');
INSERT INTO `points_history` VALUES (21, 8, 100, 'earn', 'register', NULL, '注册奖励', '2025-02-20 11:20:15');
INSERT INTO `points_history` VALUES (22, 8, 2000, 'earn', 'order', '101', '购物奖励', '2025-02-28 15:30:27');
INSERT INTO `points_history` VALUES (23, 8, 50, 'earn', 'review', '1', '评价奖励', '2025-02-28 15:35:42');
INSERT INTO `points_history` VALUES (24, 8, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-20 09:26:18');
INSERT INTO `points_history` VALUES (25, 8, 1000, 'earn', 'promotion', 'P10', '促销活动奖励', '2025-03-20 15:42:38');
INSERT INTO `points_history` VALUES (26, 8, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-19 13:39:44');
INSERT INTO `points_history` VALUES (27, 8, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-21 09:05:00');
INSERT INTO `points_history` VALUES (28, 8, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-24 13:06:34');
INSERT INTO `points_history` VALUES (29, 8, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-25 18:08:45');
INSERT INTO `points_history` VALUES (30, 8, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-26 19:44:23');
INSERT INTO `points_history` VALUES (31, 8, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-27 15:01:12');
INSERT INTO `points_history` VALUES (32, 8, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-28 09:51:36');
INSERT INTO `points_history` VALUES (33, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-28 14:53:57');
INSERT INTO `points_history` VALUES (34, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-29 15:36:24');
INSERT INTO `points_history` VALUES (35, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-30 12:44:56');
INSERT INTO `points_history` VALUES (36, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-03-31 19:30:53');
INSERT INTO `points_history` VALUES (37, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-07 21:26:12');
INSERT INTO `points_history` VALUES (38, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-08 12:40:00');
INSERT INTO `points_history` VALUES (39, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-09 14:22:46');
INSERT INTO `points_history` VALUES (40, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-11 09:27:23');
INSERT INTO `points_history` VALUES (41, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-12 21:06:14');
INSERT INTO `points_history` VALUES (42, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-13 22:08:37');
INSERT INTO `points_history` VALUES (43, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-14 20:37:30');
INSERT INTO `points_history` VALUES (44, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-16 19:48:48');
INSERT INTO `points_history` VALUES (45, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-17 14:25:08');
INSERT INTO `points_history` VALUES (46, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-18 09:23:15');
INSERT INTO `points_history` VALUES (47, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-19 15:18:36');
INSERT INTO `points_history` VALUES (48, 9, 36, 'earn', 'order', '81', '购物消费奖励', '2025-04-19 15:32:09');
INSERT INTO `points_history` VALUES (49, 9, 20, 'earn', 'order', '82', '购物消费奖励', '2025-04-19 15:46:26');
INSERT INTO `points_history` VALUES (50, 9, 276, 'earn', 'ORDER_COMPLETE', 'ORDER17450499740802107', '订单完成奖励积分', '2025-04-19 16:10:25');
INSERT INTO `points_history` VALUES (51, 9, 26, 'earn', 'ORDER_COMPLETE', 'ORDER17450503412792999', '订单完成奖励积分', '2025-04-19 17:18:27');
INSERT INTO `points_history` VALUES (52, 9, 8996, 'earn', 'ORDER_COMPLETE', 'ORDER17450548263311761', '订单完成奖励积分', '2025-04-19 17:30:00');
INSERT INTO `points_history` VALUES (53, 9, 21, 'earn', 'signin', NULL, '每日签到', '2025-04-20 09:31:58');
INSERT INTO `points_history` VALUES (54, 9, 99, 'earn', 'ORDER_COMPLETE', 'ORDER17451407073845313', '订单完成奖励积分', '2025-04-20 17:40:51');
INSERT INTO `points_history` VALUES (55, 9, 16, 'earn', 'ORDER_COMPLETE', 'ORDER17451462811964198', '订单完成奖励积分', '2025-04-20 18:52:12');
INSERT INTO `points_history` VALUES (56, 9, 21, 'earn', 'signin', NULL, '每日签到', '2025-04-21 13:38:39');
INSERT INTO `points_history` VALUES (57, 9, 21, 'earn', 'signin', NULL, '每日签到', '2025-04-22 22:46:39');
INSERT INTO `points_history` VALUES (58, 9, 21, 'earn', 'signin', NULL, '每日签到', '2025-04-23 14:10:14');
INSERT INTO `points_history` VALUES (59, 9, 156, 'earn', 'ORDER_COMPLETE', 'ORDER17453297779016486', '订单完成奖励积分', '2025-04-23 14:11:37');
INSERT INTO `points_history` VALUES (60, 9, 9, 'earn', 'ORDER_COMPLETE', 'ORDER17453886695259124', '订单完成奖励积分', '2025-04-23 20:29:27');
INSERT INTO `points_history` VALUES (61, 9, 21, 'earn', 'signin', NULL, '每日签到', '2025-04-24 12:43:29');
INSERT INTO `points_history` VALUES (62, 9, 114, 'earn', 'ORDER_COMPLETE', 'ORDER17454725349573471', '订单完成奖励积分', '2025-04-24 16:43:29');
INSERT INTO `points_history` VALUES (63, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-25 22:58:24');
INSERT INTO `points_history` VALUES (64, 9, 20, 'earn', 'signin', NULL, '每日签到', '2025-04-26 15:17:26');
INSERT INTO `points_history` VALUES (65, 9, 30, 'earn', 'signin', '', '每日签到，连续签到12天', '2025-04-27 20:25:17');
INSERT INTO `points_history` VALUES (66, 57, 20, 'earn', 'signin', '2025-04-28', '每日签到', '2025-04-28 12:58:09');
INSERT INTO `points_history` VALUES (67, 9, 30, 'earn', 'signin', '2025-04-28', '每日签到，连续签到13天', '2025-04-28 13:02:54');
INSERT INTO `points_history` VALUES (68, 9, 30, 'earn', 'signin', '2025-04-29', '每日签到，连续签到14天', '2025-04-29 09:52:09');
INSERT INTO `points_history` VALUES (69, 1, 20, 'earn', 'signin', '2025-04-29', '每日签到', '2025-04-29 09:57:41');
INSERT INTO `points_history` VALUES (70, 8, 20, 'earn', 'signin', '2025-04-29', '每日签到', '2025-04-29 10:07:28');
INSERT INTO `points_history` VALUES (71, 9, 20, 'earn', 'signin', '2025-05-05', '每日签到', '2025-05-05 15:11:31');
INSERT INTO `points_history` VALUES (72, 8, 20, 'earn', 'signin', '2025-05-05', '每日签到', '2025-05-05 21:26:23');
INSERT INTO `points_history` VALUES (73, 8, 20, 'earn', 'signin', '2025-05-06', '每日签到', '2025-05-06 10:14:51');
INSERT INTO `points_history` VALUES (74, 9, 20, 'earn', 'signin', '2025-05-06', '每日签到', '2025-05-06 21:47:39');
INSERT INTO `points_history` VALUES (75, 8, 20, 'earn', 'signin', '2025-05-07', '每日签到', '2025-05-07 14:49:35');
INSERT INTO `points_history` VALUES (76, 9, 20, 'earn', 'signin', '2025-05-08', '每日签到', '2025-05-08 12:57:45');
INSERT INTO `points_history` VALUES (77, 8, 20, 'earn', 'signin', '2025-05-10', '每日签到', '2025-05-10 21:13:39');
INSERT INTO `points_history` VALUES (78, 9, 20, 'earn', 'signin', '2025-05-10', '每日签到', '2025-05-10 22:45:55');
INSERT INTO `points_history` VALUES (81, 9, 11, 'earn', 'ORDER_REWARD', '210', '订单完成奖励，订单号: 210', '2025-05-10 23:06:18');
INSERT INTO `points_history` VALUES (82, 9, 20, 'earn', 'signin', '2025-05-11', '每日签到', '2025-05-11 09:23:07');
INSERT INTO `points_history` VALUES (83, 8, 20, 'earn', 'signin', '2025-05-11', '每日签到', '2025-05-11 09:43:39');
INSERT INTO `points_history` VALUES (84, 8, 200, 'earn', 'signin_continuous', '2025-05-12', '连续签到3天额外奖励', '2025-05-12 21:32:41');
INSERT INTO `points_history` VALUES (85, 8, 20, 'earn', 'signin', '2025-05-12', '每日签到', '2025-05-12 21:32:41');
INSERT INTO `points_history` VALUES (86, 8, -1000, 'spend', 'order', 'OD17470608003180c957b', '订单抵扣', '2025-05-12 22:40:00');
INSERT INTO `points_history` VALUES (87, 8, 100, 'earn', 'admin', NULL, '管理员增加', '2025-05-13 20:33:53');
INSERT INTO `points_history` VALUES (88, 8, 25, 'earn', 'signin', '2025-05-13', '每日签到，连续签到4天', '2025-05-13 20:34:52');
INSERT INTO `points_history` VALUES (89, 8, 25, 'earn', 'signin', '2025-05-14', '每日签到，连续签到5天', '2025-05-14 18:41:29');
INSERT INTO `points_history` VALUES (90, 9, 20, 'earn', 'signin', '2025-05-14', '每日签到', '2025-05-14 22:59:22');
INSERT INTO `points_history` VALUES (91, 9, 20, 'earn', 'signin', '2025-05-14', '每日签到', '2025-05-14 22:59:35');
INSERT INTO `points_history` VALUES (92, 9, 20, 'earn', 'signin', '2025-05-15', '每日签到', '2025-05-15 12:47:35');
INSERT INTO `points_history` VALUES (93, 8, 25, 'earn', 'signin', '2025-05-15', '每日签到，连续签到6天', '2025-05-15 12:47:58');
INSERT INTO `points_history` VALUES (95, 8, -1000, 'spend', 'order', 'OD1747320522379daa9d8', '订单抵扣', '2025-05-15 22:48:42');
INSERT INTO `points_history` VALUES (96, 8, 500, 'earn', 'signin_continuous', '2025-05-16', '连续签到7天额外奖励', '2025-05-16 11:34:30');
INSERT INTO `points_history` VALUES (97, 8, 40, 'earn', 'signin', '2025-05-16', '每日签到，连续签到7天', '2025-05-16 11:34:30');
INSERT INTO `points_history` VALUES (98, 8, 50, 'earn', 'signin', '2025-05-17', '每日签到，连续签到8天', '2025-05-17 18:40:12');
INSERT INTO `points_history` VALUES (99, 8, 60, 'earn', 'signin', '2025-05-18', '每日签到，连续签到9天', '2025-05-18 22:43:55');
INSERT INTO `points_history` VALUES (100, 8, 20, 'earn', 'signin', '2025-05-20', '每日签到', '2025-05-20 15:49:13');
INSERT INTO `points_history` VALUES (101, 8, 20, 'earn', 'signin', '2025-05-21', '每日签到', '2025-05-21 13:32:22');
INSERT INTO `points_history` VALUES (102, 8, 200, 'earn', 'signin_continuous', '2025-05-22', '连续签到3天额外奖励', '2025-05-22 14:40:08');
INSERT INTO `points_history` VALUES (103, 8, 20, 'earn', 'signin', '2025-05-22', '每日签到', '2025-05-22 14:40:08');
INSERT INTO `points_history` VALUES (104, 8, 25, 'earn', 'signin', '2025-05-23', '每日签到，连续签到4天', '2025-05-23 09:33:17');
INSERT INTO `points_history` VALUES (105, 9, 20, 'earn', 'signin', '2025-05-23', '每日签到', '2025-05-23 15:35:45');
INSERT INTO `points_history` VALUES (106, 8, 30, 'earn', 'signin', '2025-05-24', '每日签到，连续签到5天', '2025-05-24 18:47:34');
INSERT INTO `points_history` VALUES (107, 8, 20, 'earn', 'signin', '2025-05-26', '每日签到', '2025-05-26 15:40:27');
INSERT INTO `points_history` VALUES (108, 8, 20, 'earn', 'signin', '2025-05-27', '每日签到', '2025-05-27 19:19:05');
INSERT INTO `points_history` VALUES (109, 8, 200, 'earn', 'signin_continuous', '2025-05-28', '连续签到3天额外奖励', '2025-05-28 19:11:18');
INSERT INTO `points_history` VALUES (110, 8, 20, 'earn', 'signin', '2025-05-28', '每日签到', '2025-05-28 19:11:18');
INSERT INTO `points_history` VALUES (111, 8, 75, 'earn', 'order_completed', '231', '订单完成奖励', '2025-05-28 22:17:15');
INSERT INTO `points_history` VALUES (112, 8, 23, 'earn', 'comment_reward', '210', '评价奖励', '2025-05-28 22:17:44');
INSERT INTO `points_history` VALUES (113, 8, -3000, 'spend', 'order', 'OD1748532233832df04d1', '订单抵扣', '2025-05-29 23:23:54');
INSERT INTO `points_history` VALUES (114, 8, -2000, 'spend', 'order', 'OD174853231755256c661', '订单抵扣', '2025-05-29 23:25:18');
INSERT INTO `points_history` VALUES (115, 8, 20, 'earn', 'signin', '2025-05-30', '每日签到', '2025-05-30 09:34:18');
INSERT INTO `points_history` VALUES (116, 8, 23, 'earn', 'comment_reward', '211', '评价奖励', '2025-05-30 09:34:51');
INSERT INTO `points_history` VALUES (117, 8, 2000, 'earn', 'admin', NULL, '管理员补偿', '2025-05-30 11:48:49');
INSERT INTO `points_history` VALUES (118, 8, 3000, 'earn', 'admin', NULL, '管理员补偿', '2025-05-30 11:49:56');
INSERT INTO `points_history` VALUES (119, 8, -5000, 'spend', 'order', 'OD1748577051762df19cb', '订单抵扣', '2025-05-30 11:50:52');
INSERT INTO `points_history` VALUES (120, 8, 20, 'earn', 'signin', '2025-06-03', '每日签到', '2025-06-03 16:33:55');
INSERT INTO `points_history` VALUES (121, 8, 5000, 'earn', 'order_cancel', 'OD1748577051762df19cb', '订单取消返还积分', '2025-06-03 16:34:00');
INSERT INTO `points_history` VALUES (122, 8, 20, 'earn', 'signin', '2025-06-05', '每日签到', '2025-06-05 14:39:18');
INSERT INTO `points_history` VALUES (123, 8, 20, 'earn', 'signin', '2025-06-06', '每日签到', '2025-06-06 09:05:47');
INSERT INTO `points_history` VALUES (124, 8, 74, 'earn', 'order_completed', '236', '订单完成奖励', '2025-06-06 09:46:48');
INSERT INTO `points_history` VALUES (125, 8, 23, 'earn', 'comment_reward', '212', '评价奖励', '2025-06-06 09:47:15');
INSERT INTO `points_history` VALUES (126, 8, 39, 'earn', 'order_completed', '237', '订单完成奖励', '2025-06-06 13:37:09');
INSERT INTO `points_history` VALUES (127, 8, 23, 'earn', 'comment_reward', '213', '评价奖励', '2025-06-06 13:37:46');
INSERT INTO `points_history` VALUES (128, 8, 200, 'earn', 'signin_continuous', '2025-06-07', '连续签到3天额外奖励', '2025-06-07 14:55:38');
INSERT INTO `points_history` VALUES (129, 8, 20, 'earn', 'signin', '2025-06-07', '每日签到', '2025-06-07 14:55:38');
INSERT INTO `points_history` VALUES (130, 58, 20, 'earn', 'signin', '2025-06-09', '每日签到', '2025-06-09 22:23:21');
INSERT INTO `points_history` VALUES (131, 8, 20, 'earn', 'signin', '2025-06-10', '每日签到', '2025-06-10 09:36:45');
INSERT INTO `points_history` VALUES (132, 8, 20, 'earn', 'signin', '2025-06-11', '每日签到', '2025-06-11 12:46:12');
INSERT INTO `points_history` VALUES (133, 8, 200, 'earn', 'signin_continuous', '2025-06-12', '连续签到3天额外奖励', '2025-06-12 19:02:22');
INSERT INTO `points_history` VALUES (134, 8, 20, 'earn', 'signin', '2025-06-12', '每日签到', '2025-06-12 19:02:22');
INSERT INTO `points_history` VALUES (135, 8, 25, 'earn', 'signin', '2025-06-13', '每日签到，连续签到4天', '2025-06-13 09:39:11');
INSERT INTO `points_history` VALUES (136, 8, 20, 'earn', 'signin', '2025-06-17', '每日签到', '2025-06-17 18:33:25');
INSERT INTO `points_history` VALUES (137, 8, 20, 'earn', 'signin', '2025-06-18', '每日签到', '2025-06-18 15:17:15');
INSERT INTO `points_history` VALUES (138, 8, 20, 'earn', 'signin', '2025-06-27', '每日签到', '2025-06-27 11:51:07');
INSERT INTO `points_history` VALUES (139, 8, 20, 'earn', 'signin', '2025-07-08', '每日签到', '2025-07-08 10:00:20');
INSERT INTO `points_history` VALUES (140, 8, 20, 'earn', 'signin', '2025-07-09', '每日签到', '2025-07-09 12:07:53');
INSERT INTO `points_history` VALUES (141, 9, 20, 'earn', 'signin', '2025-07-09', '每日签到', '2025-07-09 15:38:24');
INSERT INTO `points_history` VALUES (142, 9, 258, 'earn', 'order_completed', '256', '订单完成奖励', '2025-07-09 20:32:56');
INSERT INTO `points_history` VALUES (143, 9, 20, 'earn', 'signin', '2025-07-10', '每日签到', '2025-07-10 10:25:22');
INSERT INTO `points_history` VALUES (144, 59, 11, 'earn', 'order_completed', '259', '订单完成奖励', '2025-07-10 16:57:55');
INSERT INTO `points_history` VALUES (145, 8, 8, 'earn', 'order_completed', '260', '订单完成奖励', '2025-07-11 13:16:38');
INSERT INTO `points_history` VALUES (146, 8, 23, 'earn', 'comment_reward', '214', '评价奖励', '2025-07-11 13:17:04');
INSERT INTO `points_history` VALUES (147, 8, 20, 'earn', 'signin', '2025-07-11', '每日签到', '2025-07-11 13:17:36');
INSERT INTO `points_history` VALUES (148, 9, 200, 'earn', 'signin_continuous', '2025-07-11', '连续签到3天额外奖励', '2025-07-11 15:19:53');
INSERT INTO `points_history` VALUES (149, 9, 20, 'earn', 'signin', '2025-07-11', '每日签到', '2025-07-11 15:19:54');
INSERT INTO `points_history` VALUES (150, 8, 20, 'earn', 'signin', '2025-09-13', '每日签到', '2025-09-13 19:05:35');
INSERT INTO `points_history` VALUES (151, 8, 20, 'earn', 'signin', '2025-09-18', '每日签到', '2025-09-18 16:43:12');
INSERT INTO `points_history` VALUES (152, 8, 20, 'earn', 'signin', '2025-09-19', '每日签到', '2025-09-19 12:11:15');

-- ----------------------------
-- Table structure for points_product
-- ----------------------------
DROP TABLE IF EXISTS `points_product`;
CREATE TABLE `points_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品图片',
  `points` int NOT NULL COMMENT '所需积分',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品分类(virtual:虚拟商品,physical:实物商品,coupon:优惠券,vip:会员特权)',
  `need_address` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否需要收货地址(0:否,1:是)',
  `need_phone` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否需要手机号(0:否,1:是)',
  `is_hot` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否热门(0:否,1:是)',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态(0:下架,1:上架)',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分商城商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of points_product
-- ----------------------------
INSERT INTO `points_product` VALUES (1, '婴儿奶瓶消毒器', '智能蒸汽消毒，一键操作，可消毒6个奶瓶', 'points/sterilizer.jpg', 2000, 100, 'physical', 1, 1, 1, 1, 10, '2025-03-20 10:00:00', '2025-05-15 14:56:02');
INSERT INTO `points_product` VALUES (2, '婴儿纸尿裤', '满200元减50元优惠券，适用于所有品牌纸尿裤', 'points/diaper.webp', 5000, 200, 'coupon', 0, 0, 1, 1, 20, '2025-03-20 10:00:00', '2025-05-15 14:56:08');
INSERT INTO `points_product` VALUES (3, '母婴会员月卡', '享受母婴商品9折优惠，专属客服，免费育儿咨询', 'points/vip_month.jpg', 1000, 999, 'vip', 0, 0, 1, 1, 5, '2025-03-20 10:00:00', '2025-05-15 14:56:11');
INSERT INTO `points_product` VALUES (4, '婴儿辅食机', '多功能辅食机，一键制作营养辅食', 'points/baby_maker.webp', 3500, 50, 'physical', 1, 1, 1, 1, 8, '2025-03-20 10:00:00', '2025-05-15 14:56:14');
INSERT INTO `points_product` VALUES (5, '婴儿玩具优惠券', '满100元减20元优惠券，适用于所有玩具类商品', 'points/toy_coupon.jpg', 1000, 500, 'coupon', 0, 0, 0, 1, 30, '2025-03-20 10:00:00', '2025-05-15 14:56:16');
INSERT INTO `points_product` VALUES (6, '婴儿体温计', '智能体温计，快速测温，带夜光显示', 'points/thermometer.webp', 2500, 80, 'physical', 1, 1, 1, 1, 15, '2025-03-20 10:00:00', '2025-05-15 14:56:18');
INSERT INTO `points_product` VALUES (7, '育儿课程7天会员', '专业育儿课程7天体验，包含新生儿护理、辅食制作等课程', 'points/parenting_course.jpg', 3000, 1000, 'virtual', 0, 1, 0, 1, 25, '2025-03-20 10:00:00', '2025-05-15 14:56:20');
INSERT INTO `points_product` VALUES (8, '婴儿推车', '轻便折叠推车，一键收车，适合0-3岁', 'points/stroller.webp', 1500, 100, 'physical', 1, 1, 0, 1, 18, '2025-03-20 10:00:00', '2025-05-15 14:56:22');
INSERT INTO `points_product` VALUES (9, '婴儿奶粉优惠券', '满300元减50元优惠券，适用于所有品牌奶粉', 'points/milk_coupon.jpg', 2000, 300, 'coupon', 0, 0, 1, 1, 28, '2025-03-20 10:00:00', '2025-05-15 14:56:24');
INSERT INTO `points_product` VALUES (10, '母婴年度会员', '享受母婴商品8.5折优惠，专属客服，免费育儿咨询，生日礼包', 'points/vip_year.jpg', 10000, 100, 'vip', 0, 0, 1, 1, 1, '2025-03-20 10:00:00', '2025-05-15 14:56:26');

-- ----------------------------
-- Table structure for points_rule
-- ----------------------------
DROP TABLE IF EXISTS `points_rule`;
CREATE TABLE `points_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则标题',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则描述',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则类型',
  `points_value` int NOT NULL DEFAULT 0 COMMENT '规则值',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of points_rule
-- ----------------------------
INSERT INTO `points_rule` VALUES (1, '购物奖励', 1, '购物可获得订单金额10%的积分', 'order', 1, '2025-03-19 13:37:48', '2025-03-19 13:37:48', 1);
INSERT INTO `points_rule` VALUES (2, '评价奖励', 2, '评价商品可获得10-30积分', 'review', 20, '2025-03-19 13:37:48', '2025-03-19 13:37:48', 1);
INSERT INTO `points_rule` VALUES (3, '每日签到', 3, '每日签到可获得20积分', 'signin', 20, '2025-03-19 13:37:48', '2025-03-19 13:37:48', 1);
INSERT INTO `points_rule` VALUES (4, '邀请好友', 4, '成功邀请新用户注册可获得100积分', 'invite', 100, '2025-03-19 13:37:48', '2025-03-19 13:37:48', 1);
INSERT INTO `points_rule` VALUES (5, '完善资料', 5, '首次完善个人资料可获得50积分', 'profile', 50, '2025-03-19 13:37:48', '2025-03-19 13:37:48', 1);
INSERT INTO `points_rule` VALUES (6, '购物返积分', 6, '每消费1元返1积分', 'shopping', 0, '2025-05-09 22:24:36', '2025-05-09 22:24:36', 1);
INSERT INTO `points_rule` VALUES (7, '连续签到3天奖励', 7, '连续签到满3天额外奖励200积分', 'signin_continuous_3', 200, '2025-05-10 22:40:50', '2025-05-10 22:40:50', 1);
INSERT INTO `points_rule` VALUES (8, '连续签到7天奖励', 8, '连续签到满7天额外奖励500积分', 'signin_continuous_7', 500, '2025-05-10 22:40:50', '2025-05-10 22:40:50', 1);

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
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
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁控制',
  PRIMARY KEY (`product_id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_brand_id`(`brand_id` ASC) USING BTREE,
  INDEX `idx_category_brand_price`(`category_id` ASC, `brand_id` ASC, `price_new` ASC) USING BTREE,
  INDEX `idx_version`(`version` ASC) USING BTREE,
  FULLTEXT INDEX `ft_goods_name_keywords`(`product_name`)
) ENGINE = InnoDB AUTO_INCREMENT = 121 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (1, 5, 1, '惠氏启赋有机婴儿配方奶粉1段（0-6个月）', 'WY001', 'goods1.jpg', '惠氏启赋有机婴儿配方奶粉，源自有机奶源，为宝宝提供全面营养', 298.00, 358.00, 100, 1024, 980, 4.8, 256, '上架', 1, 1, 1, '2025-03-05 21:58:47', '2025-05-08 19:03:54', 1);
INSERT INTO `product` VALUES (2, 5, 1, '婴儿奶粉）', 'WY002', 'goods2.jpg', '惠氏启赋有机婴儿配方奶粉，源自有机奶源，为宝宝提供全面营养', 328.00, 388.00, 100, 896, 850, 4.7, 220, '上架', 1, 0, 1, '2025-03-05 21:58:47', '2025-06-11 14:28:00', 1);
INSERT INTO `product` VALUES (3, 4, 1, '有机婴儿配方奶粉3段（1-3岁）', 'WY003', 'goods3.jpg', '惠氏启赋有机婴儿配方奶粉，源自有机奶源，为宝宝提供全面营养', 358.00, 418.00, 1000, 768, 720, 4.6, 180, '上架', 0, 0, 1, '2025-03-05 21:58:47', '2025-06-11 16:33:00', 1);
INSERT INTO `product` VALUES (4, 2, 1, '帮宝适超薄干爽纸尿裤M码（6-11kg）', 'PM001', 'goods4.jpg', '帮宝适超薄干爽纸尿裤，瞬吸干爽，让宝宝整夜安睡', 139.00, 169.00, 200, 2048, 1900, 4.9, 512, '上架', 1, 0, 1, '2025-03-05 21:58:47', '2025-05-08 19:04:53', 1);
INSERT INTO `product` VALUES (5, 2, 1, '花王妙而舒纸尿裤L码（9-14kg）', 'HW001', 'goods5.jpg', '花王妙而舒纸尿裤，透气干爽，减少红屁屁', 149.00, 179.00, 199, 1536, 1400, 4.8, 384, '上架', 1, 1, 1, '2025-03-05 21:58:47', '2025-05-08 19:04:52', 1);
INSERT INTO `product` VALUES (6, 3, 1, '美素佳儿有机米粉（6个月以上）', 'FR001', 'goods6.jpg', '美素佳儿有机米粉，100%有机原料，易消化吸收', 68.00, 88.00, 150, 896, 820, 4.7, 224, '上架', 0, 1, 1, '2025-03-05 21:58:47', '2025-05-08 19:04:51', 1);
INSERT INTO `product` VALUES (7, 4, 1, '费雪声光安抚海马', 'FS001', 'goods7.jpg', '费雪声光安抚海马，多种音乐和声效，帮助宝宝安抚入睡', 129.00, 159.00, 79, 768, 720, 4.8, 192, '上架', 0, 0, 1, '2025-03-05 21:58:47', '2025-05-08 19:04:51', 1);
INSERT INTO `product` VALUES (8, 5, 1, '强生婴儿洗发沐浴露二合一', 'JS001', 'goods8.jpg', '强生婴儿洗发沐浴露二合一，温和无泪配方，呵护宝宝娇嫩肌肤', 59.90, 79.90, 300, 1280, 1200, 4.6, 320, '上架', 0, 0, 1, '2025-03-05 21:58:47', '2025-05-08 19:04:49', 1);
INSERT INTO `product` VALUES (9, 5, 1, '舒儿适婴儿抚触油', 'SE001', 'goods9.jpg', '舒儿适婴儿抚触油，100%天然植物油，温和滋养，促进亲子关系', 89.90, 119.90, 199, 650, 600, 4.7, 156, '上架', 1, 0, 1, '2025-03-05 21:58:47', '2025-05-08 19:04:49', 1);
INSERT INTO `product` VALUES (10, 6, 1, '贝亲宽口径玻璃奶瓶', 'BQ001', 'goods10.jpg', '贝亲宽口径玻璃奶瓶，防胀气设计，耐高温消毒，适合0-18个月婴儿使用', 79.90, 99.90, 180, 850, 780, 4.8, 210, '上架', 1, 1, 1, '2025-03-05 21:58:47', '2025-05-08 19:04:48', 1);
INSERT INTO `product` VALUES (11, 2, 1, '婴儿连体衣', 'BY001', 'goods11.jpg', '纯棉材质，柔软舒适，适合0-12个月婴儿穿着，多种颜色可选。', 89.90, 129.90, 500, 320, 280, 4.8, 260, '上架', 1, 0, 1, '2025-03-05 10:00:00', '2025-05-08 19:04:04', 1);
INSERT INTO `product` VALUES (12, 2, 1, '儿童套装', 'BY002', 'goods12.jpg', '优质面料，透气舒适，适合1-3岁儿童，多种款式可选。', 129.90, 169.90, 400, 280, 240, 4.7, 220, '上架', 1, 0, 1, '2025-03-08 10:05:00', '2025-06-11 16:43:00', 1);
INSERT INTO `product` VALUES (13, 1, 1, '婴儿奶瓶', 'BY003', 'goods13.jpg', '医用级硅胶奶嘴，宽口径设计，防胀气，适合0-18个月婴儿。', 69.90, 99.90, 600, 450, 380, 4.9, 350, '上架', 1, 1, 1, '2025-03-12 10:10:00', '2025-05-08 19:04:47', 1);
INSERT INTO `product` VALUES (14, 1, 1, '婴儿床', 'BY004', 'goods14.jpg', '实木材质，环保漆，可调节高度，适合0-3岁婴幼儿。', 899.90, 1299.90, 100, 65, 58, 4.6, 50, '上架', 0, 1, 1, '2025-03-15 10:15:00', '2025-05-08 19:04:45', 1);
INSERT INTO `product` VALUES (15, 1, 1, '婴儿推车', 'BY005', 'goods15.jpg', '轻便折叠，双向推行，减震设计，适合0-3岁婴幼儿。', 699.90, 999.90, 150, 95, 85, 4.7, 80, '上架', 0, 1, 1, '2025-03-18 10:20:00', '2025-05-11 09:53:00', 1);
INSERT INTO `product` VALUES (16, 1, 2, '儿童玩具', 'BY006', 'goods16.jpg', '益智拼图，锻炼手眼协调，适合2-4岁儿童。', 49.90, 79.90, 800, 650, 580, 4.8, 520, '上架', 1, 0, 1, '2025-03-22 10:25:00', '2025-05-11 09:53:00', 1);
INSERT INTO `product` VALUES (17, 1, 2, '婴儿湿巾', 'BY007', 'goods17.jpg', '无酒精，无香料，温和不刺激，适合新生儿使用。', 19.90, 29.90, 1000, 850, 750, 4.9, 700, '上架', 1, 0, 1, '2025-03-25 10:30:00', '2025-05-08 19:05:01', 1);
INSERT INTO `product` VALUES (18, 1, 2, '婴儿洗发水', 'BY008', 'goods18.jpg', '温和配方，无泪配方，适合0-3岁婴幼儿。', 39.90, 59.90, 700, 580, 520, 4.8, 480, '上架', 1, 0, 1, '2025-03-28 10:35:00', '2025-05-08 19:05:02', 1);
INSERT INTO `product` VALUES (19, 2, 2, '孕妇护肤品', 'BY009', 'goods19.jpg', '天然成分，无添加，孕期安全使用，滋润保湿。', 159.90, 229.90, 300, 220, 190, 4.7, 170, '上架', 0, 1, 1, '2025-04-01 10:40:00', '2025-05-11 09:53:00', 1);
INSERT INTO `product` VALUES (20, 2, 2, '孕妇洗发水', 'BY010', 'goods20.jpg', '温和配方，无硅油，孕期安全使用，滋养发丝。', 89.90, 129.90, 400, 320, 280, 4.8, 250, '上架', 0, 1, 1, '2025-04-05 10:45:00', '2025-05-08 19:05:03', 1);
INSERT INTO `product` VALUES (21, 2, 2, '孕妇装', 'BY011', 'goods21.jpg', '舒适面料，宽松设计，适合孕中晚期穿着。', 199.90, 299.90, 249, 180, 160, 4.6, 140, '上架', 1, 0, 1, '2025-04-08 10:50:00', '2025-05-10 22:57:46', 1);
INSERT INTO `product` VALUES (22, 2, 2, '孕妇枕', 'BY012', 'goods22.jpg', 'U型设计，支撑腰部和腹部，改善孕期睡眠质量。', 159.90, 229.90, 200, 150, 130, 4.7, 120, '上架', 1, 0, 1, '2025-04-12 10:55:00', '2025-04-12 10:55:00', 1);
INSERT INTO `product` VALUES (23, 2, 2, '孕妇奶粉', 'BY013', 'goods23.jpg', '富含叶酸和钙，满足孕期营养需求，促进胎儿发育。', 229.90, 329.90, 299, 240, 210, 4.8, 190, '上架', 1, 0, 1, '2025-04-15 11:00:00', '2025-05-11 09:53:00', 1);
INSERT INTO `product` VALUES (24, 2, 2, '孕妇内衣', 'BY014', 'goods24.jpg', '无钢圈，棉质面料，舒适透气，适合孕期穿着。', 129.90, 189.90, 349, 280, 250, 4.7, 230, '上架', 1, 0, 1, '2025-04-18 11:05:00', '2025-05-15 12:59:06', 1);
INSERT INTO `product` VALUES (25, 5, 2, '孕妇袜', 'BY015', 'goods25.jpg', '防滑设计，舒适透气，减轻孕期腿部疲劳。', 39.90, 59.90, 500, 420, 380, 4.8, 350, '上架', 1, 0, 1, '2025-04-22 11:10:00', '2025-05-08 19:05:06', 1);
INSERT INTO `product` VALUES (26, 2, 2, '孕妇裤', 'BY016', 'goods26.jpg', '高腰设计，弹力面料，舒适贴合，适合孕中晚期穿着。', 159.90, 229.90, 300, 240, 210, 4.7, 190, '上架', 1, 0, 1, '2025-04-25 11:15:00', '2025-05-08 19:05:07', 1);
INSERT INTO `product` VALUES (27, 2, 2, '婴儿奶粉', 'BY017', 'goods27.jpg', '优质奶源，配方科学，易消化吸收，适合0-6个月婴儿。', 299.90, 399.90, 200, 160, 140, 4.9, 130, '上架', 1, 0, 1, '2025-04-28 11:20:00', '2025-05-08 19:05:09', 1);
INSERT INTO `product` VALUES (28, 2, 2, '婴儿纸尿裤', 'BY018', 'goods28.jpg', '超薄透气，瞬吸干爽，防漏设计，适合0-12个月婴儿。', 99.90, 149.90, 400, 350, 320, 4.8, 300, '上架', 1, 0, 1, '2025-04-30 11:25:00', '2025-04-30 11:25:00', 1);
INSERT INTO `product` VALUES (29, 2, 2, '婴儿辅食机', 'BY019', 'goods29.jpg', '一机多用，蒸煮搅拌一体，智能预约，适合制作6个月以上婴儿辅食。', 399.90, 499.90, 150, 125, 98, 4.7, 85, '上架', 1, 0, 1, '2025-05-03 11:30:00', '2025-05-08 19:05:10', 1);
INSERT INTO `product` VALUES (30, 2, 2, '儿童保温杯', 'BY020', 'goods30.jpg', '316不锈钢内胆，12小时保温，防漏设计，适合3岁以上儿童。', 129.90, 169.90, 300, 245, 210, 4.8, 185, '上架', 1, 0, 1, '2025-05-05 11:35:00', '2025-05-08 19:05:12', 1);
INSERT INTO `product` VALUES (31, 3, 3, '婴儿学步车', 'BY021', 'goods31.jpg', '多功能设计，音乐灯光，锻炼宝宝平衡能力，适合6-18个月婴儿。', 259.90, 359.90, 200, 160, 140, 4.7, 120, '上架', 1, 0, 1, '2025-05-08 11:40:00', '2025-05-08 19:05:14', 1);
INSERT INTO `product` VALUES (32, 3, 3, '儿童餐椅', 'BY022', 'goods32.jpg', '可调节高度，安全带设计，易清洁，适合6个月-4岁儿童。', 299.90, 399.90, 150, 120, 100, 4.6, 90, '上架', 1, 0, 1, '2025-05-10 11:45:00', '2025-05-08 19:05:14', 1);
INSERT INTO `product` VALUES (33, 3, 3, '婴儿洗澡盆', 'BY023', 'goods33.jpg', '大容量设计，防滑底座，温度感应，适合0-3岁婴幼儿。', 159.90, 219.90, 250, 200, 180, 4.8, 160, '上架', 1, 0, 1, '2025-05-12 11:50:00', '2025-05-08 19:05:15', 1);
INSERT INTO `product` VALUES (34, 3, 3, '婴儿防晒霜', 'BY024', 'goods34.jpg', 'SPF50+，物理防晒，温和不刺激，适合6个月以上婴幼儿。', 89.90, 129.90, 300, 250, 220, 4.7, 200, '上架', 1, 0, 1, '2025-05-15 11:55:00', '2025-05-08 19:05:22', 1);
INSERT INTO `product` VALUES (35, 3, 3, '婴儿理发器', 'BY025', 'goods35.jpg', '低噪音设计，多档位调节，安全防水，适合0-6岁儿童。', 159.90, 219.90, 200, 160, 140, 4.6, 120, '上架', 0, 1, 1, '2025-05-18 12:00:00', '2025-05-18 12:00:00', 1);
INSERT INTO `product` VALUES (36, 3, 3, '儿童牙刷套装', 'BY026', 'goods36.jpg', '软毛设计，卡通造型，培养刷牙习惯，适合1-6岁儿童。', 49.90, 69.90, 400, 350, 320, 4.8, 300, '上架', 1, 0, 1, '2025-05-20 12:05:00', '2025-05-12 22:45:00', 1);
INSERT INTO `product` VALUES (37, 3, 3, '婴儿背带', 'BY027', 'goods37.jpg', '人体工学设计，透气网面，多种背法，适合0-36个月婴儿。', 199.90, 259.90, 200, 160, 140, 4.7, 120, '上架', 1, 0, 1, '2025-05-22 12:10:00', '2025-05-08 19:05:23', 1);
INSERT INTO `product` VALUES (38, 3, 3, '儿童安全座椅', 'BY028', 'goods38.jpg', '五点式安全带，侧面防撞，可调节角度，适合9个月-12岁儿童。', 899.90, 1199.90, 100, 80, 70, 4.9, 60, '上架', 0, 1, 1, '2025-05-24 12:15:00', '2025-05-08 19:05:23', 1);
INSERT INTO `product` VALUES (39, 4, 3, '婴儿纸尿裤', 'BY029', 'goods39.jpg', '超薄透气，瞬吸干爽，防红臀，S/M/L/XL码可选。', 99.90, 129.90, 500, 450, 400, 4.8, 380, '上架', 1, 0, 1, '2025-05-26 12:20:00', '2025-05-08 19:05:24', 1);
INSERT INTO `product` VALUES (40, 4, 3, '婴儿湿巾', 'BY030', 'goods40.jpg', '无香型，99%纯水配方，温和无刺激，80抽/包，10包/箱。', 59.90, 79.90, 600, 550, 500, 4.9, 480, '上架', 1, 0, 1, '2025-05-28 12:25:00', '2025-05-08 19:05:24', 1);
INSERT INTO `product` VALUES (41, 4, 3, '儿童洗发沐浴露', 'BY031', 'goods41.jpg', '温和无泪配方，天然植物萃取，适合0-12岁儿童。', 69.90, 89.90, 400, 350, 320, 4.7, 300, '上架', 1, 0, 1, '2025-05-29 12:30:00', '2025-05-08 19:05:28', 1);
INSERT INTO `product` VALUES (42, 4, 3, '婴儿润肤乳', 'BY032', 'goods42.jpg', '无香型，滋润保湿，舒缓敏感，适合0-3岁婴儿。', 79.90, 99.90, 349, 300, 270, 4.8, 250, '上架', 1, 0, 1, '2025-05-30 12:35:00', '2025-05-08 19:05:30', 1);
INSERT INTO `product` VALUES (43, 4, 3, '儿童牙膏', 'BY033', 'goods43.jpg', '可吞咽配方，水果味，不含氟，适合1-6岁儿童。', 39.90, 49.90, 499, 450, 420, 4.8, 400, '上架', 1, 0, 1, '2025-05-30 12:40:00', '2025-05-11 13:55:00', 1);
INSERT INTO `product` VALUES (44, 4, 3, '婴儿指甲剪', 'BY034', 'goods44.jpg', '圆头设计，安全防夹肉，带放大镜，适合0-3岁婴幼儿。', 29.90, 39.90, 600, 550, 520, 4.7, 500, '上架', 1, 0, 1, '2025-05-30 12:45:00', '2025-05-30 12:45:00', 1);
INSERT INTO `product` VALUES (45, 4, 3, '儿童防蚊液', 'BY035', 'goods45.jpg', '天然植物配方，温和不刺激，长效驱蚊，适合1岁以上儿童。', 49.90, 69.90, 400, 350, 320, 4.6, 300, '上架', 1, 0, 1, '2025-05-31 12:50:00', '2025-05-08 19:05:32', 1);
INSERT INTO `product` VALUES (46, 4, 4, '婴儿护臀膏', 'BY036', 'goods46.jpg', '天然成分，舒缓红臀，形成保护膜，适合0-3岁婴幼儿。', 59.90, 79.90, 349, 300, 280, 4.8, 260, '上架', 1, 0, 1, '2025-05-31 12:55:00', '2025-05-11 14:15:59', 1);
INSERT INTO `product` VALUES (47, 5, 4, '儿童玩具车', 'BY037', 'goods47.jpg', '仿真设计，安全环保材质，耐摔耐用，适合3-8岁儿童。', 129.90, 169.90, 299, 250, 220, 4.7, 200, '上架', 0, 1, 1, '2025-05-31 13:00:00', '2025-05-12 22:33:03', 1);
INSERT INTO `product` VALUES (48, 5, 4, '益智积木套装', 'BY038', 'goods48.jpg', '多彩积木，环保材质，锻炼创造力和动手能力，适合3-12岁儿童。', 199.90, 249.90, 250, 200, 180, 4.8, 160, '上架', 1, 0, 1, '2025-05-31 13:05:00', '2025-05-08 19:05:37', 1);
INSERT INTO `product` VALUES (49, 5, 4, '儿童绘本套装', 'BY039', 'goods49.jpg', '经典故事，精美插图，培养阅读习惯，适合3-8岁儿童。', 159.90, 199.90, 198, 170, 150, 4.9, 130, '上架', 1, 0, 1, '2025-05-31 13:10:00', '2025-05-10 23:05:28', 1);
INSERT INTO `product` VALUES (50, 5, 4, '儿童识字卡片', 'BY040', 'goods50.jpg', '双面设计，中英双语，图文结合，适合2-6岁儿童。', 59.99, 79.90, 399, 350, 320, 4.7, 300, '上架', 0, 1, 0, '2025-05-31 13:15:00', '2025-05-11 09:53:00', 1);
INSERT INTO `product` VALUES (51, 5, 4, '儿童绘画套装', 'BY041', 'goods51.jpg', '安全无毒，易清洗，培养艺术兴趣，适合3-12岁儿童。', 89.90, 119.90, 300, 250, 230, 4.8, 220, '上架', 1, 0, 1, '2025-05-31 13:20:00', '2025-05-08 19:05:38', 1);
INSERT INTO `product` VALUES (52, 5, 4, '儿童拼图游戏', 'BY042', 'goods52.jpg', '多种难度，锻炼观察力和逻辑思维能力，适合3-12岁儿童。', 79.90, 99.90, 198, 170, 150, 4.9, 140, '上架', 1, 0, 1, '2025-05-31 13:25:00', '2025-05-11 12:04:04', 1);
INSERT INTO `product` VALUES (53, 5, 4, '儿童户外玩具', 'BY043', 'goods53.jpg', '安全环保，耐玩耐摔，锻炼身体，适合3-12岁儿童。', 199.90, 249.90, 97, 80, 70, 4.7, 60, '上架', 1, 0, 1, '2025-05-31 13:30:00', '2025-05-06 21:39:56', 1);
INSERT INTO `product` VALUES (54, 5, 4, '儿童电子学习机', 'BY044', 'goods54.jpg', '智能语音，互动教学，培养学习兴趣，适合3-12岁儿童。', 399.90, 499.90, 147, 120, 110, 4.8, 100, '上架', 1, 0, 1, '2025-05-31 13:35:00', '2025-05-11 13:03:26', 1);
INSERT INTO `product` VALUES (55, 6, 4, '婴儿摇铃玩具', 'BY045', 'goods55.jpg', '多彩设计，声音柔和，锻炼宝宝抓握能力，适合0-12个月婴儿。', 49.90, 69.90, 398, 350, 320, 4.8, 300, '上架', 1, 0, 1, '2025-05-31 13:40:00', '2025-05-11 14:10:00', 1);
INSERT INTO `product` VALUES (56, 6, 4, '婴儿音乐床铃', 'BY046', 'goods56.jpg', '旋转设计，多种音乐，吸引宝宝注意力，适合0-24个月婴儿。', 129.90, 159.90, 195, 160, 140, 4.7, 120, '上架', 0, 1, 1, '2025-05-31 13:45:00', '2025-05-11 11:19:00', 1);
INSERT INTO `product` VALUES (57, 6, 4, '儿童智能手表', 'BY047', 'goods57.jpg', 'GPS定位，防水设计，双向通话，适合3-12岁儿童。', 299.90, 399.90, 149, 121, 100, 4.9, 90, '上架', 1, 1, 1, '2025-05-31 13:50:00', '2025-05-30 09:06:00', 1);
INSERT INTO `product` VALUES (58, 6, 4, '儿童防蓝光眼镜', 'BY048', 'goods48.jpg', '轻盈舒适，有效阻隔蓝光，保护视力，适合3-15岁儿童。', 99.90, 129.90, 294, 250, 220, 4.6, 200, '上架', 0, 1, 0, '2025-05-31 13:55:00', '2025-05-09 21:19:26', 1);
INSERT INTO `product` VALUES (59, 6, 4, '孕妇维生素', 'BY049', 'goods59.jpg', '全面营养配方，补充叶酸和铁质，适合孕期全程使用。', 189.90, 239.90, 245, 201, 180, 4.8, 160, '上架', 1, 0, 1, '2025-05-31 14:00:00', '2025-09-18 16:44:04', 1);
INSERT INTO `product` VALUES (60, 6, 4, '产后修复套装', 'BY050', 'goods60.jpg', '天然成分，促进产后恢复，包含收腹带和修复霜。', 399.90, 499.90, 95, 84, 70, 4.7, 60, '上架', 1, 1, 1, '2025-05-31 14:05:00', '2025-07-16 18:19:13', 1);
INSERT INTO `product` VALUES (61, 1, 5, '爱他美卓萃有机婴儿配方奶粉1段', 'BY061', 'goods61.jpg', '德国原装进口，有机奶源，科学配方，适合0-6个月婴儿。', 358.00, 428.00, 198, 152, 130, 4.8, 120, '上架', 1, 1, 1, '2025-06-01 10:00:00', '2025-05-22 21:55:00', 1);
INSERT INTO `product` VALUES (62, 1, 6, '费雪多功能婴儿摇椅', 'BY062', 'goods62.jpg', '多档位调节，震动安抚，音乐播放，适合0-36个月婴儿。', 499.90, 599.90, 150, 100, 90, 4.7, 80, '上架', 1, 1, 1, '2025-06-01 10:05:00', '2025-06-01 10:05:00', 1);
INSERT INTO `product` VALUES (63, 2, 7, '飞利浦新安怡宽口径玻璃奶瓶', 'BY063', 'goods63.jpg', '天然原生玻璃材质，易清洗，防胀气设计，适合0-12个月婴儿。', 129.00, 159.00, 179, 130, 110, 4.9, 100, '上架', 0, 1, 0, '2025-06-01 10:10:00', '2025-05-24 19:19:12', 1);
INSERT INTO `product` VALUES (64, 2, 8, '美德乐丝韵翼双边电动吸奶器', 'BY064', 'goods64.jpg', '双韵律吸乳模式，高效舒适，静音设计，附带便携包。', 2580.00, 3280.00, 80, 50, 45, 4.9, 40, '上架', 1, 0, 1, '2025-06-01 10:15:00', '2025-06-11 16:25:00', 1);
INSERT INTO `product` VALUES (65, 3, 9, '贝亲婴儿柔湿巾80抽x6包', 'BY065', 'goods65.jpg', '纯水配方，无酒精无香料，温和清洁，适合婴儿娇嫩肌肤。', 69.90, 89.90, 299, 250, 220, 4.8, 200, '上架', 0, 1, 0, '2025-06-01 10:20:00', '2025-05-15 22:48:42', 1);
INSERT INTO `product` VALUES (66, 3, 10, '嘉宝星星泡芙香蕉味', 'BY066', 'goods66.jpg', '非油炸，入口即化，富含铁锌，适合8个月以上宝宝。', 25.80, 32.00, 400, 351, 300, 4.7, 280, '上架', 1, 1, 1, '2025-06-01 10:25:00', '2025-06-11 16:50:00', 1);
INSERT INTO `product` VALUES (67, 4, 1, '惠氏启赋未来3段幼儿配方奶粉', 'BY067', 'goods67.jpg', '爱尔兰原装进口，突破性结构脂OPO，助力宝宝吸收。', 388.00, 458.00, 220, 160, 140, 4.9, 130, '上架', 1, 0, 1, '2025-06-01 10:30:00', '2025-06-01 10:30:00', 1);
INSERT INTO `product` VALUES (68, 4, 2, '美素佳儿皇家美素佳儿3段幼儿配方奶粉', 'BY068', 'goods68.jpg', '荷兰原装进口，全脂牛奶一次入料，锁留天然营养。', 368.00, 438.00, 210, 155, 135, 4.8, 125, '上架', 1, 0, 1, '2025-06-01 10:35:00', '2025-05-30 09:06:00', 1);
INSERT INTO `product` VALUES (69, 5, 3, '帮宝适一级帮纸尿裤M号', 'BY069', 'goods69.jpg', '日本原装进口，羽柔材质，瞬吸干爽，给宝宝极致呵护。', 189.00, 229.00, 280, 231, 200, 4.9, 180, '上架', 1, 1, 1, '2025-06-01 10:40:00', '2025-06-11 17:04:00', 1);
INSERT INTO `product` VALUES (70, 5, 4, '花王妙而舒纸尿裤L号', 'BY070', 'goods70.jpg', '日本原装进口，柔软透气，立体防漏，长时间保持干爽。', 179.00, 219.00, 269, 220, 190, 4.8, 170, '上架', 1, 1, 1, '2025-06-01 10:45:00', '2025-07-09 20:59:19', 1);
INSERT INTO `product` VALUES (71, 6, 5, '爱他美白金版婴儿配方奶粉2段', 'BY071', 'goods71.jpg', '德国原装进口，特有Pronutra+配方，支持宝宝免疫系统。', 398.00, 468.00, 190, 140, 120, 4.9, 110, '上架', 1, 0, 1, '2025-06-01 10:50:00', '2025-06-01 10:50:00', 1);
INSERT INTO `product` VALUES (72, 6, 6, '费雪海马声光安抚玩具', 'BY072', 'goods72.jpg', '柔和灯光和音乐，安抚宝宝情绪，帮助入睡。', 159.00, 199.00, 160, 110, 100, 4.7, 90, '上架', 0, 1, 0, '2025-06-01 10:55:00', '2025-06-01 10:55:00', 1);
INSERT INTO `product` VALUES (73, 1, 7, '飞利浦新安怡自然原生系列奶瓶', 'BY073', 'goods73.jpg', '宽口径设计，易于冲调和清洗，仿生奶嘴，宝宝易接受。', 109.00, 139.00, 170, 120, 105, 4.8, 95, '上架', 0, 1, 0, '2025-06-01 11:00:00', '2025-06-01 11:00:00', 1);
INSERT INTO `product` VALUES (74, 1, 8, '美德乐韵律吸乳器单边电动', 'BY074', 'goods74.jpg', '模拟宝宝自然吸吮节奏，高效舒适，轻巧便携。', 1880.00, 2380.00, 90, 60, 55, 4.9, 50, '上架', 1, 0, 1, '2025-06-01 11:05:00', '2025-06-01 11:05:00', 1);
INSERT INTO `product` VALUES (75, 2, 9, '贝亲婴儿洗衣液', 'BY075', 'goods75.jpg', '植物配方，温和无刺激，有效去除污渍，易漂洗。', 59.90, 79.90, 250, 200, 180, 4.7, 160, '上架', 0, 1, 0, '2025-06-01 11:10:00', '2025-06-01 11:10:00', 1);
INSERT INTO `product` VALUES (76, 2, 10, '嘉宝米粉原味', 'BY076', 'goods76.jpg', '高铁高钙，细腻易消化，不添加蔗糖和食用盐，适合6个月以上宝宝。', 35.80, 45.00, 350, 300, 280, 4.8, 260, '上架', 1, 1, 1, '2025-06-01 11:15:00', '2025-06-01 11:15:00', 1);
INSERT INTO `product` VALUES (77, 3, 1, '惠氏S-26铂臻3段幼儿配方奶粉', 'BY077', 'goods77.jpg', '瑞士原装进口，含神经鞘磷脂，助力宝宝认知发展。', 378.00, 448.00, 196, 151, 130, 4.9, 120, '上架', 1, 0, 1, '2025-06-01 11:20:00', '2025-07-11 15:09:19', 1);
INSERT INTO `product` VALUES (78, 3, 2, '美素佳儿金装3段幼儿配方奶粉', 'BY078', 'goods78.jpg', '荷兰原装进口，含益生元，支持宝宝肠道健康。', 348.00, 418.00, 190, 145, 125, 4.8, 115, '上架', 1, 0, 1, '2025-06-01 11:25:00', '2025-06-01 11:25:00', 1);
INSERT INTO `product` VALUES (79, 4, 3, '帮宝适拉拉裤L号', 'BY079', 'goods79.jpg', '日本原装进口，一拉就穿，一撕即脱，方便好动宝宝。', 199.00, 239.00, 260, 210, 190, 4.9, 170, '上架', 1, 1, 1, '2025-06-01 11:30:00', '2025-06-01 11:30:00', 1);
INSERT INTO `product` VALUES (80, 3, 4, '花王纸尿裤NB号', 'BY080', 'goods80.jpg', '日本原装进口，专为新生儿设计，柔软贴合，呵护脐部。', 169.00, 209.00, 299, 207, 185, 4.8, 165, '上架', 1, 1, 1, '2025-06-01 11:35:00', '2025-06-10 19:54:00', 1);
INSERT INTO `product` VALUES (81, 5, 5, '爱他美卓萃婴儿配方奶粉3段', 'BY081', 'goods81.jpg', '德国原装进口，有机奶源，科学配方，适合1-3岁幼儿。', 368.00, 438.00, 180, 130, 110, 4.9, 100, '上架', 1, 0, 1, '2025-06-01 11:40:00', '2025-06-01 11:40:00', 1);
INSERT INTO `product` VALUES (82, 5, 6, '费雪踢踢乐钢琴健身器', 'BY082', 'goods82.jpg', '多种玩法，锻炼宝宝运动能力和感官发育。', 299.00, 359.00, 140, 90, 80, 4.7, 70, '上架', 0, 1, 0, '2025-06-01 11:45:00', '2025-06-01 11:45:00', 1);
INSERT INTO `product` VALUES (83, 6, 7, '飞利浦新安怡婴儿辅食机', 'BY083', 'goods83.jpg', '蒸煮搅拌一体，快速制作健康辅食，易清洗。', 899.00, 1099.00, 119, 80, 70, 4.8, 60, '上架', 1, 1, 1, '2025-06-01 11:50:00', '2025-07-09 15:13:07', 1);
INSERT INTO `product` VALUES (84, 6, 8, '美德乐丝韵单边电动吸奶器', 'BY084', 'goods84.jpg', '模拟宝宝自然吸吮节奏，高效舒适，轻巧便携。', 1880.00, 2380.00, 89, 62, 55, 4.9, 50, '上架', 1, 0, 1, '2025-06-01 11:55:00', '2025-07-11 15:16:40', 1);
INSERT INTO `product` VALUES (85, 1, 9, '贝亲宽口径PPSU奶瓶', 'BY085', 'goods85.jpg', 'PPSU材质，轻巧耐摔，耐高温，易清洗。', 119.00, 149.00, 169, 121, 105, 4.8, 95, '上架', 0, 1, 0, '2025-06-01 12:00:00', '2025-07-10 14:48:35', 1);
INSERT INTO `product` VALUES (86, 1, 10, '嘉宝泡芙草莓苹果味', 'BY086', 'goods86.jpg', '非油炸，入口即化，富含铁锌，适合8个月以上宝宝。', 25.80, 32.00, 400, 350, 300, 4.7, 280, '上架', 1, 1, 1, '2025-06-01 12:05:00', '2025-06-01 12:05:00', 1);
INSERT INTO `product` VALUES (87, 2, 1, '惠氏启赋蓝钻3段幼儿配方奶粉', 'BY087', 'goods87.jpg', '爱尔兰原装进口，含活性益生菌，支持宝宝肠道健康。', 398.00, 468.00, 220, 160, 140, 4.9, 130, '上架', 1, 0, 1, '2025-06-01 12:10:00', '2025-06-01 12:10:00', 1);
INSERT INTO `product` VALUES (88, 2, 2, '美素佳儿金装较大婴儿配方奶粉2段', 'BY088', 'goods88.jpg', '荷兰原装进口，含益生元，支持宝宝肠道健康。', 358.00, 428.00, 210, 155, 135, 4.8, 125, '上架', 1, 0, 1, '2025-06-01 12:15:00', '2025-06-01 12:15:00', 1);
INSERT INTO `product` VALUES (89, 3, 3, '帮宝适特级棉柔纸尿裤M号', 'BY089', 'goods89.jpg', '超柔软材质，瞬吸干爽，给宝宝极致呵护。', 179.00, 219.00, 279, 232, 200, 4.9, 180, '上架', 1, 1, 1, '2025-06-01 12:20:00', '2025-06-11 17:50:25', 1);
INSERT INTO `product` VALUES (90, 3, 4, '花王妙而舒纸尿裤NB号', 'BY090', 'goods90.jpg', '日本原装进口，专为新生儿设计，柔软贴合，呵护脐部。', 169.00, 210.00, 250, 205, 185, 4.8, 165, '上架', 1, 1, 1, '2025-06-01 12:25:00', '2025-06-06 13:31:12', 1);
INSERT INTO `product` VALUES (91, 4, 5, '爱他美卓萃婴儿配方奶粉2段', 'BY091', 'goods91.jpg', '德国原装进口，有机奶源，科学配方，适合6-12个月婴儿。', 388.00, 458.00, 189, 141, 120, 4.9, 110, '上架', 1, 0, 1, '2025-06-01 12:30:00', '2025-06-27 12:20:43', 1);
INSERT INTO `product` VALUES (92, 4, 6, '费雪声光安抚海马', 'BY092', 'goods92.jpg', '柔和灯光和音乐，安抚宝宝情绪，帮助入睡。', 159.00, 199.00, 160, 110, 100, 4.7, 90, '上架', 0, 1, 0, '2025-06-01 12:35:00', '2025-06-01 12:35:00', 1);
INSERT INTO `product` VALUES (93, 5, 7, '飞利浦新安怡自然原生系列玻璃奶瓶', 'BY093', 'goods93.jpg', '宽口径设计，易于冲调和清洗，仿生奶嘴，宝宝易接受。', 109.00, 139.00, 170, 121, 105, 4.8, 95, '上架', 0, 1, 0, '2025-06-01 12:40:00', '2025-06-11 17:13:00', 1);
INSERT INTO `product` VALUES (94, 5, 8, '美德乐丝韵翼双边电动吸奶器', 'BY094', 'goods94.jpg', '双韵律吸乳模式，高效舒适，静音设计，附带便携包。', 2580.00, 3280.00, 79, 51, 45, 4.9, 40, '上架', 1, 0, 1, '2025-06-01 12:45:00', '2025-07-09 20:32:13', 1);
INSERT INTO `product` VALUES (95, 6, 9, '贝亲婴儿洗发沐浴露二合一', 'BY095', 'goods95.jpg', '弱酸性配方，温和清洁，无泪配方，适合婴儿娇嫩肌肤。', 79.90, 89.90, 249, 200, 180, 4.7, 160, '上架', 0, 1, 0, '2025-06-01 12:50:00', '2025-07-11 13:15:05', 1);
INSERT INTO `product` VALUES (96, 6, 10, '嘉宝磨牙饼干香蕉味', 'BY096', 'goods96.jpg', '帮助宝宝缓解出牙不适，富含铁锌，适合10个月以上宝宝。', 29.80, 36.00, 349, 302, 280, 4.8, 260, '上架', 1, 1, 1, '2025-06-01 12:55:00', '2025-06-11 17:47:00', 1);
INSERT INTO `product` VALUES (97, 1, 1, '惠氏S-26铂臻2段较大婴儿配方奶粉', 'BY097', 'goods97.jpg', '瑞士原装进口，含神经鞘磷脂，助力宝宝认知发展。', 388.00, 458.00, 200, 151, 130, 4.9, 120, '上架', 1, 0, 1, '2025-06-01 13:00:00', '2025-06-11 17:25:00', 1);
INSERT INTO `product` VALUES (98, 1, 2, '美素佳儿金装较大婴儿配方奶粉2段', 'BY098', 'goods98.jpg', '荷兰原装进口，含益生元，支持宝宝肠道健康。', 358.00, 428.00, 187, 147, 125, 4.8, 115, '上架', 1, 0, 1, '2025-06-01 13:05:00', '2025-09-19 12:11:58', 1);
INSERT INTO `product` VALUES (99, 2, 3, '帮宝适拉拉裤M号', 'BY099', 'goods99.jpg', '日本原装进口，一拉就穿，一撕即脱，方便好动宝宝。', 189.00, 229.00, 260, 210, 190, 4.9, 170, '上架', 1, 1, 1, '2025-06-01 13:10:00', '2025-06-01 13:10:00', 1);
INSERT INTO `product` VALUES (100, 2, 4, '花王纸尿裤S号', 'BY100', 'goods100.jpg', '日本原装进口，柔软透气，立体防漏，长时间保持干爽。', 179.00, 219.00, 268, 222, 190, 4.8, 170, '上架', 1, 1, 1, '2025-06-01 13:15:00', '2025-07-16 18:56:06', 1);
INSERT INTO `product` VALUES (101, 3, 5, '爱他美卓萃婴儿配方奶粉1段', 'BY101', 'goods101.jpg', '德国原装进口，有机奶源，科学配方，适合0-6个月婴儿。', 358.00, 428.00, 200, 150, 130, 4.8, 120, '上架', 1, 1, 1, '2025-06-01 13:20:00', '2025-06-01 13:20:00', 1);
INSERT INTO `product` VALUES (102, 3, 6, '费雪多功能婴儿摇椅', 'BY102', 'goods102.jpg', '多档位调节，震动安抚，音乐播放，适合0-36个月婴儿。', 499.90, 599.90, 150, 100, 90, 4.7, 80, '上架', 1, 1, 1, '2025-06-01 13:25:00', '2025-06-01 13:25:00', 1);
INSERT INTO `product` VALUES (103, 4, 7, '飞利浦新安怡婴儿奶嘴', 'BY103', 'goods103.jpg', '硅胶材质，柔软舒适，防胀气设计，适合不同月龄宝宝。', 59.00, 79.00, 200, 150, 130, 4.8, 120, '上架', 0, 1, 0, '2025-06-01 13:30:00', '2025-06-01 13:30:00', 1);
INSERT INTO `product` VALUES (104, 4, 8, '美德乐吸奶器配件', 'BY104', 'goods104.jpg', '原装配件，确保吸奶器正常使用和卫生。', 180.00, 220.00, 99, 71, 60, 4.9, 55, '上架', 0, 0, 0, '2025-06-01 13:35:00', '2025-07-11 15:13:55', 1);
INSERT INTO `product` VALUES (105, 5, 9, '贝亲婴儿润肤露', 'BY105', 'goods105.jpg', '天然植物成分，温和滋润，呵护宝宝肌肤。', 79.90, 99.90, 220, 180, 160, 4.7, 150, '上架', 0, 1, 0, '2025-06-01 13:40:00', '2025-06-01 13:40:00', 1);
INSERT INTO `product` VALUES (106, 5, 10, '嘉宝米粉混合水果味', 'BY106', 'goods106.jpg', '高铁高钙，添加多种水果，营养丰富，适合6个月以上宝宝。', 38.80, 48.00, 300, 250, 230, 4.8, 210, '上架', 1, 1, 1, '2025-06-01 13:45:00', '2025-06-01 13:45:00', 1);
INSERT INTO `product` VALUES (107, 6, 1, '惠氏启赋未来2段较大婴儿配方奶粉', 'BY107', 'goods107.jpg', '爱尔兰原装进口，突破性结构脂OPO，助力宝宝吸收。', 398.00, 468.00, 200, 150, 130, 4.9, 120, '上架', 1, 0, 1, '2025-06-01 13:50:00', '2025-06-01 13:50:00', 1);
INSERT INTO `product` VALUES (108, 6, 2, '美素佳儿皇家美素佳儿2段较大婴儿配方奶粉', 'BY108', 'goods108.jpg', '荷兰原装进口，全脂牛奶一次入料，锁留天然营养。', 378.00, 448.00, 190, 145, 125, 4.8, 115, '上架', 1, 0, 1, '2025-06-01 13:55:00', '2025-06-01 13:55:00', 1);
INSERT INTO `product` VALUES (109, 1, 3, '帮宝适一级帮纸尿裤L号', 'BY109', 'goods109.jpg', '日本原装进口，羽柔材质，瞬吸干爽，给宝宝极致呵护。', 199.00, 239.00, 250, 200, 180, 4.9, 170, '上架', 1, 1, 1, '2025-06-01 14:00:00', '2025-06-01 14:00:00', 1);
INSERT INTO `product` VALUES (110, 1, 4, '花王妙而舒纸尿裤XL号', 'BY110', 'goods110.jpg', '日本原装进口，柔软透气，立体防漏，长时间保持干爽。', 189.00, 229.00, 240, 190, 170, 4.8, 160, '上架', 1, 1, 1, '2025-06-01 14:05:00', '2025-06-01 14:05:00', 1);
INSERT INTO `product` VALUES (111, 2, 5, '爱他美白金版婴儿配方奶粉3段', 'BY111', 'goods111.jpg', '德国原装进口，特有Pronutra+配方，支持宝宝免疫系统。', 408.00, 478.00, 180, 130, 110, 4.9, 100, '上架', 1, 0, 1, '2025-06-01 14:10:00', '2025-06-01 14:10:00', 1);
INSERT INTO `product` VALUES (112, 2, 6, '费雪声光安抚小海马', 'BY112', 'goods112.jpg', '柔和灯光和音乐，安抚宝宝情绪，帮助入睡。', 159.00, 199.00, 150, 100, 90, 4.7, 80, '上架', 0, 1, 0, '2025-06-01 14:15:00', '2025-06-01 14:15:00', 1);
INSERT INTO `product` VALUES (113, 3, 7, '飞利浦新安怡自然原生系列玻璃奶瓶', 'BY113', 'goods113.jpg', '宽口径设计，易于冲调和清洗，仿生奶嘴，宝宝易接受。', 109.00, 139.00, 160, 110, 100, 4.8, 90, '上架', 0, 1, 0, '2025-06-01 14:20:00', '2025-06-01 14:20:00', 1);
INSERT INTO `product` VALUES (114, 3, 8, '美德乐韵律吸乳器单边电动', 'BY114', 'goods114.jpg', '模拟宝宝自然吸吮节奏，高效舒适，轻巧便携。', 1880.00, 2380.00, 80, 50, 45, 4.9, 40, '上架', 1, 0, 1, '2025-06-01 14:25:00', '2025-06-01 14:25:00', 1);
INSERT INTO `product` VALUES (115, 4, 9, '贝亲婴儿洗衣液', 'BY115', 'goods115.jpg', '植物配方，温和无刺激，有效去除污渍，易漂洗。', 59.90, 79.90, 200, 150, 130, 4.7, 120, '上架', 0, 1, 0, '2025-06-01 14:30:00', '2025-06-01 14:30:00', 1);
INSERT INTO `product` VALUES (116, 4, 10, '嘉宝米粉原味', 'BY116', 'goods116.jpg', '高铁高钙，细腻易消化，不添加蔗糖和食用盐，适合6个月以上宝宝。', 35.80, 45.00, 300, 250, 230, 4.8, 210, '上架', 1, 1, 1, '2025-06-01 14:35:00', '2025-06-01 14:35:00', 1);
INSERT INTO `product` VALUES (117, 5, 1, '惠氏S-26铂臻3段幼儿配方奶粉', 'BY117', 'goods117.jpg', '瑞士原装进口，含神经鞘磷脂，助力宝宝认知发展。', 378.00, 448.00, 180, 130, 110, 4.9, 100, '上架', 1, 0, 1, '2025-06-01 14:40:00', '2025-06-01 14:40:00', 1);
INSERT INTO `product` VALUES (118, 5, 2, '美素佳儿金装3段幼儿配方奶粉', 'BY118', 'goods118.jpg', '荷兰原装进口，含益生元，支持宝宝肠道健康。', 348.00, 418.00, 170, 125, 105, 4.8, 95, '上架', 1, 0, 1, '2025-06-01 14:45:00', '2025-06-01 14:45:00', 1);
INSERT INTO `product` VALUES (119, 6, 3, '帮宝适拉拉裤L号', 'BY119', 'goods119.jpg', '日本原装进口，一拉就穿，一撕即脱，方便好动宝宝。', 199.00, 239.00, 220, 172, 150, 4.9, 140, '上架', 1, 1, 1, '2025-06-01 14:50:00', '2025-05-24 19:38:00', 1);
INSERT INTO `product` VALUES (120, 6, 4, '花王纸尿裤NB号', 'BY120', 'goods120.jpg', '日本原装进口，专为新生儿设计，柔软贴合，呵护脐部。', 169.00, 209.00, 200, 155, 135, 4.8, 125, '上架', 1, 1, 1, '2025-06-01 14:55:00', '2025-06-01 14:55:00', 1);

-- ----------------------------
-- Table structure for product_image
-- ----------------------------
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image`  (
  `image_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `type` enum('main','detail','desc') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'main' COMMENT '图片类型：main-主图，detail-详情图，desc-描述图',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`image_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_type_product`(`type` ASC, `product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 441 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品图片表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_image
-- ----------------------------
INSERT INTO `product_image` VALUES (1, 1, 'goods1.jpg', 'main', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (2, 1, 'desc_01_01.jpeg', 'desc', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (3, 1, 'desc_01_02.jpeg', 'desc', 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (4, 1, 'desc_01_03.jpeg', 'desc', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (5, 1, 'desc_01_04.jpeg', 'desc', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (6, 1, 'desc_01_05.jpeg', 'desc', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (7, 1, 'desc_01_06.jpeg', 'desc', 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (8, 2, 'goods2.jpg', 'main', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (9, 2, 'desc_02_01.jpeg', 'desc', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (10, 2, 'desc_02_02.jpeg', 'desc', 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (11, 2, 'desc_02_03.jpeg', 'desc', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (12, 2, 'desc_02_04.jpeg', 'desc', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (13, 2, 'desc_02_05.jpeg', 'desc', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (14, 2, 'desc_02_06.jpeg', 'desc', 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (15, 3, 'goods3.jpg', 'main', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (16, 3, 'desc_03_01.jpeg', 'desc', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (17, 3, 'desc_03_02.jpeg', 'desc', 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (18, 3, 'desc_03_03.jpeg', 'desc', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (19, 3, 'desc_03_04.jpeg', 'desc', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (20, 3, 'desc_03_05.jpeg', 'desc', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (21, 3, 'desc_03_06.jpeg', 'desc', 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (22, 4, 'goods4.jpg', 'main', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (23, 4, 'desc_04_01.jpeg', 'desc', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (24, 4, 'desc_04_02.jpeg', 'desc', 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (25, 4, 'desc_04_03.jpeg', 'desc', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (26, 4, 'desc_04_04.jpeg', 'desc', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (27, 4, 'desc_04_05.jpeg', 'desc', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (28, 4, 'desc_04_06.jpeg', 'desc', 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (29, 5, 'goods5.jpg', 'main', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (30, 5, 'desc_05_01.jpeg', 'desc', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (31, 5, 'desc_05_02.jpeg', 'desc', 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (32, 5, 'desc_05_03.jpeg', 'desc', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (33, 5, 'desc_05_04.jpeg', 'desc', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (34, 5, 'desc_05_05.jpeg', 'desc', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (35, 5, 'desc_05_06.jpeg', 'desc', 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (36, 6, 'goods6.jpg', 'main', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (37, 6, 'desc_06_01.jpeg', 'desc', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (38, 6, 'desc_06_02.jpeg', 'desc', 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (39, 6, 'desc_06_03.jpeg', 'desc', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (40, 6, 'desc_06_04.jpeg', 'desc', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (41, 6, 'desc_06_05.jpeg', 'desc', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (42, 6, 'desc_06_06.jpeg', 'desc', 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (43, 7, 'goods7.jpg', 'main', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (44, 7, 'desc_07_01.jpeg', 'desc', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (45, 7, 'desc_07_02.jpeg', 'desc', 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (46, 7, 'desc_07_03.jpeg', 'desc', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (47, 7, 'desc_07_04.jpeg', 'desc', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (48, 7, 'desc_07_05.jpeg', 'desc', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (49, 7, 'desc_07_06.jpeg', 'desc', 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (50, 8, 'goods8.jpg', 'main', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (51, 8, 'desc_08_01.jpeg', 'desc', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (52, 8, 'desc_08_02.jpeg', 'desc', 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (53, 8, 'desc_08_03.jpeg', 'desc', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (54, 8, 'desc_08_04.jpeg', 'desc', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (55, 8, 'desc_08_05.jpeg', 'desc', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (56, 8, 'desc_08_06.jpeg', 'desc', 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (57, 9, 'goods9.jpg', 'main', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (58, 9, 'desc_09_01.jpeg', 'desc', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (59, 9, 'desc_09_02.jpeg', 'desc', 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (60, 9, 'desc_09_03.jpeg', 'desc', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (61, 9, 'desc_09_04.jpeg', 'desc', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (62, 9, 'desc_09_05.jpeg', 'desc', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (63, 9, 'desc_09_06.jpeg', 'desc', 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (64, 10, 'goods10.jpg', 'main', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (65, 10, 'desc_10_01.jpeg', 'desc', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (66, 10, 'desc_10_02.jpeg', 'desc', 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (67, 10, 'desc_10_03.jpeg', 'desc', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (68, 10, 'desc_10_04.jpeg', 'desc', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (69, 10, 'desc_10_05.jpeg', 'desc', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (70, 10, 'desc_10_06.jpeg', 'desc', 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_image` VALUES (71, 11, 'goods11.jpg', 'main', 1, '2025-03-12 10:00:00', '2025-03-12 10:00:00');
INSERT INTO `product_image` VALUES (72, 11, 'desc_11_01.jpeg', 'desc', 1, '2025-03-12 10:00:00', '2025-03-12 10:00:00');
INSERT INTO `product_image` VALUES (73, 11, 'desc_11_02.jpeg', 'desc', 2, '2025-03-12 10:00:00', '2025-03-12 10:00:00');
INSERT INTO `product_image` VALUES (74, 11, 'desc_11_03.jpeg', 'desc', 3, '2025-03-12 10:00:00', '2025-03-12 10:00:00');
INSERT INTO `product_image` VALUES (75, 11, 'desc_11_04.jpeg', 'desc', 4, '2025-03-12 10:00:00', '2025-03-12 10:00:00');
INSERT INTO `product_image` VALUES (76, 11, 'desc_11_05.jpeg', 'desc', 5, '2025-03-12 10:00:00', '2025-03-12 10:00:00');
INSERT INTO `product_image` VALUES (77, 11, 'desc_11_06.jpeg', 'desc', 6, '2025-03-12 10:00:00', '2025-03-12 10:00:00');
INSERT INTO `product_image` VALUES (78, 12, 'goods12.jpg', 'main', 1, '2025-03-15 10:05:00', '2025-03-15 10:05:00');
INSERT INTO `product_image` VALUES (79, 12, 'desc_12_01.jpeg', 'desc', 1, '2025-03-15 10:05:00', '2025-03-15 10:05:00');
INSERT INTO `product_image` VALUES (80, 12, 'desc_12_02.jpeg', 'desc', 2, '2025-03-15 10:05:00', '2025-03-15 10:05:00');
INSERT INTO `product_image` VALUES (81, 12, 'desc_12_03.jpeg', 'desc', 3, '2025-03-15 10:05:00', '2025-03-15 10:05:00');
INSERT INTO `product_image` VALUES (82, 12, 'desc_12_04.jpeg', 'desc', 4, '2025-03-15 10:05:00', '2025-03-15 10:05:00');
INSERT INTO `product_image` VALUES (83, 12, 'desc_12_05.jpeg', 'desc', 5, '2025-03-15 10:05:00', '2025-03-15 10:05:00');
INSERT INTO `product_image` VALUES (84, 12, 'desc_12_06.jpeg', 'desc', 6, '2025-03-15 10:05:00', '2025-03-15 10:05:00');
INSERT INTO `product_image` VALUES (85, 13, 'goods13.jpg', 'main', 1, '2025-03-18 10:10:00', '2025-03-18 10:10:00');
INSERT INTO `product_image` VALUES (86, 13, 'desc_13_01.jpeg', 'desc', 1, '2025-03-18 10:10:00', '2025-03-18 10:10:00');
INSERT INTO `product_image` VALUES (87, 13, 'desc_13_02.jpeg', 'desc', 2, '2025-03-18 10:10:00', '2025-03-18 10:10:00');
INSERT INTO `product_image` VALUES (88, 13, 'desc_13_03.jpeg', 'desc', 3, '2025-03-18 10:10:00', '2025-03-18 10:10:00');
INSERT INTO `product_image` VALUES (89, 13, 'desc_13_04.jpeg', 'desc', 4, '2025-03-18 10:10:00', '2025-03-18 10:10:00');
INSERT INTO `product_image` VALUES (90, 13, 'desc_13_05.jpeg', 'desc', 5, '2025-03-18 10:10:00', '2025-03-18 10:10:00');
INSERT INTO `product_image` VALUES (91, 13, 'desc_13_06.jpeg', 'desc', 6, '2025-03-18 10:10:00', '2025-03-18 10:10:00');
INSERT INTO `product_image` VALUES (92, 14, 'goods14.jpg', 'main', 1, '2025-03-20 10:15:00', '2025-03-20 10:15:00');
INSERT INTO `product_image` VALUES (93, 14, 'desc_14_01.jpeg', 'desc', 1, '2025-03-20 10:15:00', '2025-03-20 10:15:00');
INSERT INTO `product_image` VALUES (94, 14, 'desc_14_02.jpeg', 'desc', 2, '2025-03-20 10:15:00', '2025-03-20 10:15:00');
INSERT INTO `product_image` VALUES (95, 14, 'desc_14_03.jpeg', 'desc', 3, '2025-03-20 10:15:00', '2025-03-20 10:15:00');
INSERT INTO `product_image` VALUES (96, 14, 'desc_14_04.jpeg', 'desc', 4, '2025-03-20 10:15:00', '2025-03-20 10:15:00');
INSERT INTO `product_image` VALUES (97, 14, 'desc_14_05.jpeg', 'desc', 5, '2025-03-20 10:15:00', '2025-03-20 10:15:00');
INSERT INTO `product_image` VALUES (98, 14, 'desc_14_06.jpeg', 'desc', 6, '2025-03-20 10:15:00', '2025-03-20 10:15:00');
INSERT INTO `product_image` VALUES (99, 15, 'goods15.jpg', 'main', 1, '2025-03-22 10:20:00', '2025-03-22 10:20:00');
INSERT INTO `product_image` VALUES (100, 15, 'desc_15_01.jpeg', 'desc', 1, '2025-03-22 10:20:00', '2025-03-22 10:20:00');
INSERT INTO `product_image` VALUES (101, 15, 'desc_15_02.jpeg', 'desc', 2, '2025-03-22 10:20:00', '2025-03-22 10:20:00');
INSERT INTO `product_image` VALUES (102, 15, 'desc_15_03.jpeg', 'desc', 3, '2025-03-22 10:20:00', '2025-03-22 10:20:00');
INSERT INTO `product_image` VALUES (103, 15, 'desc_15_04.jpeg', 'desc', 4, '2025-03-22 10:20:00', '2025-03-22 10:20:00');
INSERT INTO `product_image` VALUES (104, 15, 'desc_15_05.jpeg', 'desc', 5, '2025-03-22 10:20:00', '2025-03-22 10:20:00');
INSERT INTO `product_image` VALUES (105, 15, 'desc_15_06.jpeg', 'desc', 6, '2025-03-22 10:20:00', '2025-03-22 10:20:00');
INSERT INTO `product_image` VALUES (106, 16, 'goods16.jpg', 'main', 1, '2025-03-25 10:25:00', '2025-03-25 10:25:00');
INSERT INTO `product_image` VALUES (107, 16, 'desc_16_01.jpeg', 'desc', 1, '2025-03-25 10:25:00', '2025-03-25 10:25:00');
INSERT INTO `product_image` VALUES (108, 16, 'desc_16_02.jpeg', 'desc', 2, '2025-03-25 10:25:00', '2025-03-25 10:25:00');
INSERT INTO `product_image` VALUES (109, 16, 'desc_16_03.jpeg', 'desc', 3, '2025-03-25 10:25:00', '2025-03-25 10:25:00');
INSERT INTO `product_image` VALUES (110, 16, 'desc_16_04.jpeg', 'desc', 4, '2025-03-25 10:25:00', '2025-03-25 10:25:00');
INSERT INTO `product_image` VALUES (111, 16, 'desc_16_05.jpeg', 'desc', 5, '2025-03-25 10:25:00', '2025-03-25 10:25:00');
INSERT INTO `product_image` VALUES (112, 16, 'desc_16_06.jpeg', 'desc', 6, '2025-03-25 10:25:00', '2025-03-25 10:25:00');
INSERT INTO `product_image` VALUES (113, 17, 'goods17.jpg', 'main', 1, '2025-03-27 10:30:00', '2025-03-27 10:30:00');
INSERT INTO `product_image` VALUES (114, 17, 'desc_17_01.jpeg', 'desc', 1, '2025-03-27 10:30:00', '2025-03-27 10:30:00');
INSERT INTO `product_image` VALUES (115, 17, 'desc_17_02.jpeg', 'desc', 2, '2025-03-27 10:30:00', '2025-03-27 10:30:00');
INSERT INTO `product_image` VALUES (116, 17, 'desc_17_03.jpeg', 'desc', 3, '2025-03-27 10:30:00', '2025-03-27 10:30:00');
INSERT INTO `product_image` VALUES (117, 17, 'desc_17_04.jpeg', 'desc', 4, '2025-03-27 10:30:00', '2025-03-27 10:30:00');
INSERT INTO `product_image` VALUES (118, 17, 'desc_17_05.jpeg', 'desc', 5, '2025-03-27 10:30:00', '2025-03-27 10:30:00');
INSERT INTO `product_image` VALUES (119, 17, 'desc_17_06.jpeg', 'desc', 6, '2025-03-27 10:30:00', '2025-03-27 10:30:00');
INSERT INTO `product_image` VALUES (120, 18, 'goods18.jpg', 'main', 1, '2025-03-29 10:35:00', '2025-03-29 10:35:00');
INSERT INTO `product_image` VALUES (121, 18, 'desc_18_01.jpeg', 'desc', 1, '2025-03-29 10:35:00', '2025-03-29 10:35:00');
INSERT INTO `product_image` VALUES (122, 18, 'desc_18_02.jpeg', 'desc', 2, '2025-03-29 10:35:00', '2025-03-29 10:35:00');
INSERT INTO `product_image` VALUES (123, 18, 'desc_18_03.jpeg', 'desc', 3, '2025-03-29 10:35:00', '2025-03-29 10:35:00');
INSERT INTO `product_image` VALUES (124, 18, 'desc_18_04.jpeg', 'desc', 4, '2025-03-29 10:35:00', '2025-03-29 10:35:00');
INSERT INTO `product_image` VALUES (125, 18, 'desc_18_05.jpeg', 'desc', 5, '2025-03-29 10:35:00', '2025-03-29 10:35:00');
INSERT INTO `product_image` VALUES (126, 18, 'desc_18_06.jpeg', 'desc', 6, '2025-03-29 10:35:00', '2025-03-29 10:35:00');
INSERT INTO `product_image` VALUES (127, 19, 'goods19.jpg', 'main', 1, '2025-04-01 10:40:00', '2025-04-01 10:40:00');
INSERT INTO `product_image` VALUES (128, 19, 'desc_19_01.jpeg', 'desc', 1, '2025-04-01 10:40:00', '2025-04-01 10:40:00');
INSERT INTO `product_image` VALUES (129, 19, 'desc_19_02.jpeg', 'desc', 2, '2025-04-01 10:40:00', '2025-04-01 10:40:00');
INSERT INTO `product_image` VALUES (130, 19, 'desc_19_03.jpeg', 'desc', 3, '2025-04-01 10:40:00', '2025-04-01 10:40:00');
INSERT INTO `product_image` VALUES (131, 19, 'desc_19_04.jpeg', 'desc', 4, '2025-04-01 10:40:00', '2025-04-01 10:40:00');
INSERT INTO `product_image` VALUES (132, 19, 'desc_19_05.jpeg', 'desc', 5, '2025-04-01 10:40:00', '2025-04-01 10:40:00');
INSERT INTO `product_image` VALUES (133, 19, 'desc_19_06.jpeg', 'desc', 6, '2025-04-01 10:40:00', '2025-04-01 10:40:00');
INSERT INTO `product_image` VALUES (134, 20, 'goods20.jpg', 'main', 1, '2025-04-03 10:45:00', '2025-04-03 10:45:00');
INSERT INTO `product_image` VALUES (135, 20, 'desc_20_01.jpeg', 'desc', 1, '2025-04-03 10:45:00', '2025-04-03 10:45:00');
INSERT INTO `product_image` VALUES (136, 20, 'desc_20_02.jpeg', 'desc', 2, '2025-04-03 10:45:00', '2025-04-03 10:45:00');
INSERT INTO `product_image` VALUES (137, 20, 'desc_20_03.jpeg', 'desc', 3, '2025-04-03 10:45:00', '2025-04-03 10:45:00');
INSERT INTO `product_image` VALUES (138, 20, 'desc_20_04.jpeg', 'desc', 4, '2025-04-03 10:45:00', '2025-04-03 10:45:00');
INSERT INTO `product_image` VALUES (139, 20, 'desc_20_05.jpeg', 'desc', 5, '2025-04-03 10:45:00', '2025-04-03 10:45:00');
INSERT INTO `product_image` VALUES (140, 20, 'desc_20_06.jpeg', 'desc', 6, '2025-04-03 10:45:00', '2025-04-03 10:45:00');
INSERT INTO `product_image` VALUES (141, 21, 'goods21.jpg', 'main', 1, '2025-04-05 10:50:00', '2025-04-05 10:50:00');
INSERT INTO `product_image` VALUES (142, 21, 'desc_21_01.jpeg', 'desc', 1, '2025-04-05 10:50:00', '2025-04-05 10:50:00');
INSERT INTO `product_image` VALUES (143, 21, 'desc_21_02.jpeg', 'desc', 2, '2025-04-05 10:50:00', '2025-04-05 10:50:00');
INSERT INTO `product_image` VALUES (144, 21, 'desc_21_03.jpeg', 'desc', 3, '2025-04-05 10:50:00', '2025-04-05 10:50:00');
INSERT INTO `product_image` VALUES (145, 21, 'desc_21_04.jpeg', 'desc', 4, '2025-04-05 10:50:00', '2025-04-05 10:50:00');
INSERT INTO `product_image` VALUES (146, 21, 'desc_21_05.jpeg', 'desc', 5, '2025-04-05 10:50:00', '2025-04-05 10:50:00');
INSERT INTO `product_image` VALUES (147, 21, 'desc_21_06.jpeg', 'desc', 6, '2025-04-05 10:50:00', '2025-04-05 10:50:00');
INSERT INTO `product_image` VALUES (148, 22, 'goods22.jpg', 'main', 1, '2025-04-07 10:55:00', '2025-04-07 10:55:00');
INSERT INTO `product_image` VALUES (149, 22, 'desc_22_01.jpeg', 'desc', 1, '2025-04-07 10:55:00', '2025-04-07 10:55:00');
INSERT INTO `product_image` VALUES (150, 22, 'desc_22_02.jpeg', 'desc', 2, '2025-04-07 10:55:00', '2025-04-07 10:55:00');
INSERT INTO `product_image` VALUES (151, 22, 'desc_22_03.jpeg', 'desc', 3, '2025-04-07 10:55:00', '2025-04-07 10:55:00');
INSERT INTO `product_image` VALUES (152, 22, 'desc_22_04.jpeg', 'desc', 4, '2025-04-07 10:55:00', '2025-04-07 10:55:00');
INSERT INTO `product_image` VALUES (153, 22, 'desc_22_05.jpeg', 'desc', 5, '2025-04-07 10:55:00', '2025-04-07 10:55:00');
INSERT INTO `product_image` VALUES (154, 22, 'desc_22_06.jpeg', 'desc', 6, '2025-04-07 10:55:00', '2025-04-07 10:55:00');
INSERT INTO `product_image` VALUES (155, 23, 'goods23.jpg', 'main', 1, '2025-04-09 11:00:00', '2025-04-09 11:00:00');
INSERT INTO `product_image` VALUES (156, 23, 'desc_23_01.jpeg', 'desc', 1, '2025-04-09 11:00:00', '2025-04-09 11:00:00');
INSERT INTO `product_image` VALUES (157, 23, 'desc_23_02.jpeg', 'desc', 2, '2025-04-09 11:00:00', '2025-04-09 11:00:00');
INSERT INTO `product_image` VALUES (158, 23, 'desc_23_03.jpeg', 'desc', 3, '2025-04-09 11:00:00', '2025-04-09 11:00:00');
INSERT INTO `product_image` VALUES (159, 23, 'desc_23_04.jpeg', 'desc', 4, '2025-04-09 11:00:00', '2025-04-09 11:00:00');
INSERT INTO `product_image` VALUES (160, 23, 'desc_23_05.jpeg', 'desc', 5, '2025-04-09 11:00:00', '2025-04-09 11:00:00');
INSERT INTO `product_image` VALUES (161, 23, 'desc_23_06.jpeg', 'desc', 6, '2025-04-09 11:00:00', '2025-04-09 11:00:00');
INSERT INTO `product_image` VALUES (162, 24, 'goods24.jpg', 'main', 1, '2025-04-12 11:05:00', '2025-04-12 11:05:00');
INSERT INTO `product_image` VALUES (163, 24, 'desc_24_01.jpeg', 'desc', 1, '2025-04-12 11:05:00', '2025-04-12 11:05:00');
INSERT INTO `product_image` VALUES (164, 24, 'desc_24_02.jpeg', 'desc', 2, '2025-04-12 11:05:00', '2025-04-12 11:05:00');
INSERT INTO `product_image` VALUES (165, 24, 'desc_24_03.jpeg', 'desc', 3, '2025-04-12 11:05:00', '2025-04-12 11:05:00');
INSERT INTO `product_image` VALUES (166, 24, 'desc_24_04.jpeg', 'desc', 4, '2025-04-12 11:05:00', '2025-04-12 11:05:00');
INSERT INTO `product_image` VALUES (167, 24, 'desc_24_05.jpeg', 'desc', 5, '2025-04-12 11:05:00', '2025-04-12 11:05:00');
INSERT INTO `product_image` VALUES (168, 24, 'desc_24_06.jpeg', 'desc', 6, '2025-04-12 11:05:00', '2025-04-12 11:05:00');
INSERT INTO `product_image` VALUES (169, 25, 'goods25.jpg', 'main', 1, '2025-04-15 11:10:00', '2025-04-15 11:10:00');
INSERT INTO `product_image` VALUES (170, 25, 'desc_25_01.jpeg', 'desc', 1, '2025-04-15 11:10:00', '2025-04-15 11:10:00');
INSERT INTO `product_image` VALUES (171, 25, 'desc_25_02.jpeg', 'desc', 2, '2025-04-15 11:10:00', '2025-04-15 11:10:00');
INSERT INTO `product_image` VALUES (172, 25, 'desc_25_03.jpeg', 'desc', 3, '2025-04-15 11:10:00', '2025-04-15 11:10:00');
INSERT INTO `product_image` VALUES (173, 25, 'desc_25_04.jpeg', 'desc', 4, '2025-04-15 11:10:00', '2025-04-15 11:10:00');
INSERT INTO `product_image` VALUES (174, 25, 'desc_25_05.jpeg', 'desc', 5, '2025-04-15 11:10:00', '2025-04-15 11:10:00');
INSERT INTO `product_image` VALUES (175, 25, 'desc_25_06.jpeg', 'desc', 6, '2025-04-15 11:10:00', '2025-04-15 11:10:00');
INSERT INTO `product_image` VALUES (176, 26, 'goods26.jpg', 'main', 1, '2025-04-18 11:15:00', '2025-04-18 11:15:00');
INSERT INTO `product_image` VALUES (177, 26, 'desc_26_01.jpeg', 'desc', 1, '2025-04-18 11:15:00', '2025-04-18 11:15:00');
INSERT INTO `product_image` VALUES (178, 26, 'desc_26_02.jpeg', 'desc', 2, '2025-04-18 11:15:00', '2025-04-18 11:15:00');
INSERT INTO `product_image` VALUES (179, 26, 'desc_26_03.jpeg', 'desc', 3, '2025-04-18 11:15:00', '2025-04-18 11:15:00');
INSERT INTO `product_image` VALUES (180, 26, 'desc_26_04.jpeg', 'desc', 4, '2025-04-18 11:15:00', '2025-04-18 11:15:00');
INSERT INTO `product_image` VALUES (181, 26, 'desc_26_05.jpeg', 'desc', 5, '2025-04-18 11:15:00', '2025-04-18 11:15:00');
INSERT INTO `product_image` VALUES (182, 26, 'desc_26_06.jpeg', 'desc', 6, '2025-04-18 11:15:00', '2025-04-18 11:15:00');
INSERT INTO `product_image` VALUES (183, 27, 'goods27.jpg', 'main', 1, '2025-04-20 11:20:00', '2025-04-20 11:20:00');
INSERT INTO `product_image` VALUES (184, 27, 'desc_27_01.jpeg', 'desc', 1, '2025-04-20 11:20:00', '2025-04-20 11:20:00');
INSERT INTO `product_image` VALUES (185, 27, 'desc_27_02.jpeg', 'desc', 2, '2025-04-20 11:20:00', '2025-04-20 11:20:00');
INSERT INTO `product_image` VALUES (186, 27, 'desc_27_03.jpeg', 'desc', 3, '2025-04-20 11:20:00', '2025-04-20 11:20:00');
INSERT INTO `product_image` VALUES (187, 27, 'desc_27_04.jpeg', 'desc', 4, '2025-04-20 11:20:00', '2025-04-20 11:20:00');
INSERT INTO `product_image` VALUES (188, 27, 'desc_27_05.jpeg', 'desc', 5, '2025-04-20 11:20:00', '2025-04-20 11:20:00');
INSERT INTO `product_image` VALUES (189, 27, 'desc_27_06.jpeg', 'desc', 6, '2025-04-20 11:20:00', '2025-04-20 11:20:00');
INSERT INTO `product_image` VALUES (190, 28, 'goods28.jpg', 'main', 1, '2025-04-22 11:25:00', '2025-04-22 11:25:00');
INSERT INTO `product_image` VALUES (191, 28, 'desc_28_01.jpeg', 'desc', 1, '2025-04-22 11:25:00', '2025-04-22 11:25:00');
INSERT INTO `product_image` VALUES (192, 28, 'desc_28_02.jpeg', 'desc', 2, '2025-04-22 11:25:00', '2025-04-22 11:25:00');
INSERT INTO `product_image` VALUES (193, 28, 'desc_28_03.jpeg', 'desc', 3, '2025-04-22 11:25:00', '2025-04-22 11:25:00');
INSERT INTO `product_image` VALUES (194, 28, 'desc_28_04.jpeg', 'desc', 4, '2025-04-22 11:25:00', '2025-04-22 11:25:00');
INSERT INTO `product_image` VALUES (195, 28, 'desc_28_05.jpeg', 'desc', 5, '2025-04-22 11:25:00', '2025-04-22 11:25:00');
INSERT INTO `product_image` VALUES (196, 28, 'desc_28_06.jpeg', 'desc', 6, '2025-04-22 11:25:00', '2025-04-22 11:25:00');
INSERT INTO `product_image` VALUES (197, 29, 'goods29.jpg', 'main', 1, '2025-04-25 11:30:00', '2025-04-25 11:30:00');
INSERT INTO `product_image` VALUES (198, 29, 'desc_29_01.jpeg', 'desc', 1, '2025-04-25 11:30:00', '2025-04-25 11:30:00');
INSERT INTO `product_image` VALUES (199, 29, 'desc_29_02.jpeg', 'desc', 2, '2025-04-25 11:30:00', '2025-04-25 11:30:00');
INSERT INTO `product_image` VALUES (200, 29, 'desc_29_03.jpeg', 'desc', 3, '2025-04-25 11:30:00', '2025-04-25 11:30:00');
INSERT INTO `product_image` VALUES (201, 29, 'desc_29_04.jpeg', 'desc', 4, '2025-04-25 11:30:00', '2025-04-25 11:30:00');
INSERT INTO `product_image` VALUES (202, 29, 'desc_29_05.jpeg', 'desc', 5, '2025-04-25 11:30:00', '2025-04-25 11:30:00');
INSERT INTO `product_image` VALUES (203, 29, 'desc_29_06.jpeg', 'desc', 6, '2025-04-25 11:30:00', '2025-04-25 11:30:00');
INSERT INTO `product_image` VALUES (204, 30, 'goods30.jpg', 'main', 1, '2025-04-28 11:35:00', '2025-04-28 11:35:00');
INSERT INTO `product_image` VALUES (205, 30, 'desc_30_01.jpeg', 'desc', 1, '2025-04-28 11:35:00', '2025-04-28 11:35:00');
INSERT INTO `product_image` VALUES (206, 30, 'desc_30_02.jpeg', 'desc', 2, '2025-04-28 11:35:00', '2025-04-28 11:35:00');
INSERT INTO `product_image` VALUES (207, 30, 'desc_30_03.jpeg', 'desc', 3, '2025-04-28 11:35:00', '2025-04-28 11:35:00');
INSERT INTO `product_image` VALUES (208, 30, 'desc_30_04.jpeg', 'desc', 4, '2025-04-28 11:35:00', '2025-04-28 11:35:00');
INSERT INTO `product_image` VALUES (209, 30, 'desc_30_05.jpeg', 'desc', 5, '2025-04-28 11:35:00', '2025-04-28 11:35:00');
INSERT INTO `product_image` VALUES (210, 30, 'desc_30_06.jpeg', 'desc', 6, '2025-04-28 11:35:00', '2025-04-28 11:35:00');
INSERT INTO `product_image` VALUES (211, 31, 'goods31.jpg', 'main', 1, '2025-05-01 10:00:00', '2025-05-01 10:00:00');
INSERT INTO `product_image` VALUES (212, 31, 'desc_31_01.jpeg', 'desc', 1, '2025-05-01 10:00:00', '2025-05-01 10:00:00');
INSERT INTO `product_image` VALUES (213, 31, 'desc_31_02.jpeg', 'desc', 2, '2025-05-01 10:00:00', '2025-05-01 10:00:00');
INSERT INTO `product_image` VALUES (214, 31, 'desc_31_03.jpeg', 'desc', 3, '2025-05-01 10:00:00', '2025-05-01 10:00:00');
INSERT INTO `product_image` VALUES (215, 31, 'desc_31_04.jpeg', 'desc', 4, '2025-05-01 10:00:00', '2025-05-01 10:00:00');
INSERT INTO `product_image` VALUES (216, 31, 'desc_31_05.jpeg', 'desc', 5, '2025-05-01 10:00:00', '2025-05-01 10:00:00');
INSERT INTO `product_image` VALUES (217, 31, 'desc_31_06.jpeg', 'desc', 6, '2025-05-01 10:00:00', '2025-05-01 10:00:00');
INSERT INTO `product_image` VALUES (218, 32, 'goods32.jpg', 'main', 1, '2025-05-03 10:05:00', '2025-05-03 10:05:00');
INSERT INTO `product_image` VALUES (219, 32, 'desc_32_01.jpeg', 'desc', 1, '2025-05-03 10:05:00', '2025-05-03 10:05:00');
INSERT INTO `product_image` VALUES (220, 32, 'desc_32_02.jpeg', 'desc', 2, '2025-05-03 10:05:00', '2025-05-03 10:05:00');
INSERT INTO `product_image` VALUES (221, 32, 'desc_32_03.jpeg', 'desc', 3, '2025-05-03 10:05:00', '2025-05-03 10:05:00');
INSERT INTO `product_image` VALUES (222, 32, 'desc_32_04.jpeg', 'desc', 4, '2025-05-03 10:05:00', '2025-05-03 10:05:00');
INSERT INTO `product_image` VALUES (223, 32, 'desc_32_05.jpeg', 'desc', 5, '2025-05-03 10:05:00', '2025-05-03 10:05:00');
INSERT INTO `product_image` VALUES (224, 32, 'desc_32_06.jpeg', 'desc', 6, '2025-05-03 10:05:00', '2025-05-03 10:05:00');
INSERT INTO `product_image` VALUES (225, 33, 'goods33.jpg', 'main', 1, '2025-05-05 10:10:00', '2025-05-05 10:10:00');
INSERT INTO `product_image` VALUES (226, 33, 'desc_33_01.jpeg', 'desc', 1, '2025-05-05 10:10:00', '2025-05-05 10:10:00');
INSERT INTO `product_image` VALUES (227, 33, 'desc_33_02.jpeg', 'desc', 2, '2025-05-05 10:10:00', '2025-05-05 10:10:00');
INSERT INTO `product_image` VALUES (228, 33, 'desc_33_03.jpeg', 'desc', 3, '2025-05-05 10:10:00', '2025-05-05 10:10:00');
INSERT INTO `product_image` VALUES (229, 33, 'desc_33_04.jpeg', 'desc', 4, '2025-05-05 10:10:00', '2025-05-05 10:10:00');
INSERT INTO `product_image` VALUES (230, 33, 'desc_33_05.jpeg', 'desc', 5, '2025-05-05 10:10:00', '2025-05-05 10:10:00');
INSERT INTO `product_image` VALUES (231, 33, 'desc_33_06.jpeg', 'desc', 6, '2025-05-05 10:10:00', '2025-05-05 10:10:00');
INSERT INTO `product_image` VALUES (232, 34, 'goods34.jpg', 'main', 1, '2025-05-07 10:15:00', '2025-05-07 10:15:00');
INSERT INTO `product_image` VALUES (233, 34, 'desc_34_01.jpeg', 'desc', 1, '2025-05-07 10:15:00', '2025-05-07 10:15:00');
INSERT INTO `product_image` VALUES (234, 34, 'desc_34_02.jpeg', 'desc', 2, '2025-05-07 10:15:00', '2025-05-07 10:15:00');
INSERT INTO `product_image` VALUES (235, 34, 'desc_34_03.jpeg', 'desc', 3, '2025-05-07 10:15:00', '2025-05-07 10:15:00');
INSERT INTO `product_image` VALUES (236, 34, 'desc_34_04.jpeg', 'desc', 4, '2025-05-07 10:15:00', '2025-05-07 10:15:00');
INSERT INTO `product_image` VALUES (237, 34, 'desc_34_05.jpeg', 'desc', 5, '2025-05-07 10:15:00', '2025-05-07 10:15:00');
INSERT INTO `product_image` VALUES (238, 34, 'desc_34_06.jpeg', 'desc', 6, '2025-05-07 10:15:00', '2025-05-07 10:15:00');
INSERT INTO `product_image` VALUES (239, 35, 'goods35.jpg', 'main', 1, '2025-05-09 10:20:00', '2025-05-09 10:20:00');
INSERT INTO `product_image` VALUES (240, 35, 'desc_35_01.jpeg', 'desc', 1, '2025-05-09 10:20:00', '2025-05-09 10:20:00');
INSERT INTO `product_image` VALUES (241, 35, 'desc_35_02.jpeg', 'desc', 2, '2025-05-09 10:20:00', '2025-05-09 10:20:00');
INSERT INTO `product_image` VALUES (242, 35, 'desc_35_03.jpeg', 'desc', 3, '2025-05-09 10:20:00', '2025-05-09 10:20:00');
INSERT INTO `product_image` VALUES (243, 35, 'desc_35_04.jpeg', 'desc', 4, '2025-05-09 10:20:00', '2025-05-09 10:20:00');
INSERT INTO `product_image` VALUES (244, 35, 'desc_35_05.jpeg', 'desc', 5, '2025-05-09 10:20:00', '2025-05-09 10:20:00');
INSERT INTO `product_image` VALUES (245, 35, 'desc_35_06.jpeg', 'desc', 6, '2025-05-09 10:20:00', '2025-05-09 10:20:00');
INSERT INTO `product_image` VALUES (246, 36, 'goods36.jpg', 'main', 1, '2025-05-11 10:25:00', '2025-05-11 10:25:00');
INSERT INTO `product_image` VALUES (247, 36, 'desc_36_01.jpeg', 'desc', 1, '2025-05-11 10:25:00', '2025-05-11 10:25:00');
INSERT INTO `product_image` VALUES (248, 36, 'desc_36_02.jpeg', 'desc', 2, '2025-05-11 10:25:00', '2025-05-11 10:25:00');
INSERT INTO `product_image` VALUES (249, 36, 'desc_36_03.jpeg', 'desc', 3, '2025-05-11 10:25:00', '2025-05-11 10:25:00');
INSERT INTO `product_image` VALUES (250, 36, 'desc_36_04.jpeg', 'desc', 4, '2025-05-11 10:25:00', '2025-05-11 10:25:00');
INSERT INTO `product_image` VALUES (251, 36, 'desc_36_05.jpeg', 'desc', 5, '2025-05-11 10:25:00', '2025-05-11 10:25:00');
INSERT INTO `product_image` VALUES (252, 36, 'desc_36_06.jpeg', 'desc', 6, '2025-05-11 10:25:00', '2025-05-11 10:25:00');
INSERT INTO `product_image` VALUES (253, 37, 'goods37.jpg', 'main', 1, '2025-05-13 10:30:00', '2025-05-13 10:30:00');
INSERT INTO `product_image` VALUES (254, 37, 'desc_37_01.jpeg', 'desc', 1, '2025-05-13 10:30:00', '2025-05-13 10:30:00');
INSERT INTO `product_image` VALUES (255, 37, 'desc_37_02.jpeg', 'desc', 2, '2025-05-13 10:30:00', '2025-05-13 10:30:00');
INSERT INTO `product_image` VALUES (256, 37, 'desc_37_03.jpeg', 'desc', 3, '2025-05-13 10:30:00', '2025-05-13 10:30:00');
INSERT INTO `product_image` VALUES (257, 37, 'desc_37_04.jpeg', 'desc', 4, '2025-05-13 10:30:00', '2025-05-13 10:30:00');
INSERT INTO `product_image` VALUES (258, 37, 'desc_37_05.jpeg', 'desc', 5, '2025-05-13 10:30:00', '2025-05-13 10:30:00');
INSERT INTO `product_image` VALUES (259, 37, 'desc_37_06.jpeg', 'desc', 6, '2025-05-13 10:30:00', '2025-05-13 10:30:00');
INSERT INTO `product_image` VALUES (260, 38, 'goods38.jpg', 'main', 1, '2025-05-15 10:35:00', '2025-05-15 10:35:00');
INSERT INTO `product_image` VALUES (261, 38, 'desc_38_01.jpeg', 'desc', 1, '2025-05-15 10:35:00', '2025-05-15 10:35:00');
INSERT INTO `product_image` VALUES (262, 38, 'desc_38_02.jpeg', 'desc', 2, '2025-05-15 10:35:00', '2025-05-15 10:35:00');
INSERT INTO `product_image` VALUES (263, 38, 'desc_38_03.jpeg', 'desc', 3, '2025-05-15 10:35:00', '2025-05-15 10:35:00');
INSERT INTO `product_image` VALUES (264, 38, 'desc_38_04.jpeg', 'desc', 4, '2025-05-15 10:35:00', '2025-05-15 10:35:00');
INSERT INTO `product_image` VALUES (265, 38, 'desc_38_05.jpeg', 'desc', 5, '2025-05-15 10:35:00', '2025-05-15 10:35:00');
INSERT INTO `product_image` VALUES (266, 38, 'desc_38_06.jpeg', 'desc', 6, '2025-05-15 10:35:00', '2025-05-15 10:35:00');
INSERT INTO `product_image` VALUES (267, 39, 'goods39.jpg', 'main', 1, '2025-05-17 10:40:00', '2025-05-17 10:40:00');
INSERT INTO `product_image` VALUES (268, 39, 'desc_39_01.jpeg', 'desc', 1, '2025-05-17 10:40:00', '2025-05-17 10:40:00');
INSERT INTO `product_image` VALUES (269, 39, 'desc_39_02.jpeg', 'desc', 2, '2025-05-17 10:40:00', '2025-05-17 10:40:00');
INSERT INTO `product_image` VALUES (270, 39, 'desc_39_03.jpeg', 'desc', 3, '2025-05-17 10:40:00', '2025-05-17 10:40:00');
INSERT INTO `product_image` VALUES (271, 39, 'desc_39_04.jpeg', 'desc', 4, '2025-05-17 10:40:00', '2025-05-17 10:40:00');
INSERT INTO `product_image` VALUES (272, 39, 'desc_39_05.jpeg', 'desc', 5, '2025-05-17 10:40:00', '2025-05-17 10:40:00');
INSERT INTO `product_image` VALUES (273, 39, 'desc_39_06.jpeg', 'desc', 6, '2025-05-17 10:40:00', '2025-05-17 10:40:00');
INSERT INTO `product_image` VALUES (274, 40, 'goods40.jpg', 'main', 1, '2025-05-19 10:45:00', '2025-05-19 10:45:00');
INSERT INTO `product_image` VALUES (275, 40, 'desc_40_01.jpeg', 'desc', 1, '2025-05-19 10:45:00', '2025-05-19 10:45:00');
INSERT INTO `product_image` VALUES (276, 40, 'desc_40_02.jpeg', 'desc', 2, '2025-05-19 10:45:00', '2025-05-19 10:45:00');
INSERT INTO `product_image` VALUES (277, 40, 'desc_40_03.jpeg', 'desc', 3, '2025-05-19 10:45:00', '2025-05-19 10:45:00');
INSERT INTO `product_image` VALUES (278, 40, 'desc_40_04.jpeg', 'desc', 4, '2025-05-19 10:45:00', '2025-05-19 10:45:00');
INSERT INTO `product_image` VALUES (279, 40, 'desc_40_05.jpeg', 'desc', 5, '2025-05-19 10:45:00', '2025-05-19 10:45:00');
INSERT INTO `product_image` VALUES (280, 40, 'desc_40_06.jpeg', 'desc', 6, '2025-05-19 10:45:00', '2025-05-19 10:45:00');
INSERT INTO `product_image` VALUES (281, 41, 'goods41.jpg', 'main', 1, '2025-05-21 10:50:00', '2025-05-21 10:50:00');
INSERT INTO `product_image` VALUES (282, 41, 'desc_41_01.jpeg', 'desc', 1, '2025-05-21 10:50:00', '2025-05-21 10:50:00');
INSERT INTO `product_image` VALUES (283, 41, 'desc_41_02.jpeg', 'desc', 2, '2025-05-21 10:50:00', '2025-05-21 10:50:00');
INSERT INTO `product_image` VALUES (284, 41, 'desc_41_03.jpeg', 'desc', 3, '2025-05-21 10:50:00', '2025-05-21 10:50:00');
INSERT INTO `product_image` VALUES (285, 41, 'desc_41_04.jpeg', 'desc', 4, '2025-05-21 10:50:00', '2025-05-21 10:50:00');
INSERT INTO `product_image` VALUES (286, 41, 'desc_41_05.jpeg', 'desc', 5, '2025-05-21 10:50:00', '2025-05-21 10:50:00');
INSERT INTO `product_image` VALUES (287, 41, 'desc_41_06.jpeg', 'desc', 6, '2025-05-21 10:50:00', '2025-05-21 10:50:00');
INSERT INTO `product_image` VALUES (288, 42, 'goods42.jpg', 'main', 1, '2025-05-23 10:55:00', '2025-05-23 10:55:00');
INSERT INTO `product_image` VALUES (289, 42, 'desc_42_01.jpeg', 'desc', 1, '2025-05-23 10:55:00', '2025-05-23 10:55:00');
INSERT INTO `product_image` VALUES (290, 42, 'desc_42_02.jpeg', 'desc', 2, '2025-05-23 10:55:00', '2025-05-23 10:55:00');
INSERT INTO `product_image` VALUES (291, 42, 'desc_42_03.jpeg', 'desc', 3, '2025-05-23 10:55:00', '2025-05-23 10:55:00');
INSERT INTO `product_image` VALUES (292, 42, 'desc_42_04.jpeg', 'desc', 4, '2025-05-23 10:55:00', '2025-05-23 10:55:00');
INSERT INTO `product_image` VALUES (293, 42, 'desc_42_05.jpeg', 'desc', 5, '2025-05-23 10:55:00', '2025-05-23 10:55:00');
INSERT INTO `product_image` VALUES (294, 42, 'desc_42_06.jpeg', 'desc', 6, '2025-05-23 10:55:00', '2025-05-23 10:55:00');
INSERT INTO `product_image` VALUES (295, 43, 'goods43.jpg', 'main', 1, '2025-05-25 11:00:00', '2025-05-25 11:00:00');
INSERT INTO `product_image` VALUES (296, 43, 'desc_43_01.jpeg', 'desc', 1, '2025-05-25 11:00:00', '2025-05-25 11:00:00');
INSERT INTO `product_image` VALUES (297, 43, 'desc_43_02.jpeg', 'desc', 2, '2025-05-25 11:00:00', '2025-05-25 11:00:00');
INSERT INTO `product_image` VALUES (298, 43, 'desc_43_03.jpeg', 'desc', 3, '2025-05-25 11:00:00', '2025-05-25 11:00:00');
INSERT INTO `product_image` VALUES (299, 43, 'desc_43_04.jpeg', 'desc', 4, '2025-05-25 11:00:00', '2025-05-25 11:00:00');
INSERT INTO `product_image` VALUES (300, 43, 'desc_43_05.jpeg', 'desc', 5, '2025-05-25 11:00:00', '2025-05-25 11:00:00');
INSERT INTO `product_image` VALUES (301, 43, 'desc_43_06.jpeg', 'desc', 6, '2025-05-25 11:00:00', '2025-05-25 11:00:00');
INSERT INTO `product_image` VALUES (302, 44, 'goods44.jpg', 'main', 1, '2025-05-27 11:05:00', '2025-05-27 11:05:00');
INSERT INTO `product_image` VALUES (303, 44, 'desc_44_01.jpeg', 'desc', 1, '2025-05-27 11:05:00', '2025-05-27 11:05:00');
INSERT INTO `product_image` VALUES (304, 44, 'desc_44_02.jpeg', 'desc', 2, '2025-05-27 11:05:00', '2025-05-27 11:05:00');
INSERT INTO `product_image` VALUES (305, 44, 'desc_44_03.jpeg', 'desc', 3, '2025-05-27 11:05:00', '2025-05-27 11:05:00');
INSERT INTO `product_image` VALUES (306, 44, 'desc_44_04.jpeg', 'desc', 4, '2025-05-27 11:05:00', '2025-05-27 11:05:00');
INSERT INTO `product_image` VALUES (307, 44, 'desc_44_05.jpeg', 'desc', 5, '2025-05-27 11:05:00', '2025-05-27 11:05:00');
INSERT INTO `product_image` VALUES (308, 44, 'desc_44_06.jpeg', 'desc', 6, '2025-05-27 11:05:00', '2025-05-27 11:05:00');
INSERT INTO `product_image` VALUES (309, 45, 'goods45.jpg', 'main', 1, '2025-05-29 11:10:00', '2025-05-29 11:10:00');
INSERT INTO `product_image` VALUES (310, 45, 'desc_45_01.jpeg', 'desc', 1, '2025-05-29 11:10:00', '2025-05-29 11:10:00');
INSERT INTO `product_image` VALUES (311, 45, 'desc_45_02.jpeg', 'desc', 2, '2025-05-29 11:10:00', '2025-05-29 11:10:00');
INSERT INTO `product_image` VALUES (312, 45, 'desc_45_03.jpeg', 'desc', 3, '2025-05-29 11:10:00', '2025-05-29 11:10:00');
INSERT INTO `product_image` VALUES (313, 45, 'desc_45_04.jpeg', 'desc', 4, '2025-05-29 11:10:00', '2025-05-29 11:10:00');
INSERT INTO `product_image` VALUES (314, 45, 'desc_45_05.jpeg', 'desc', 5, '2025-05-29 11:10:00', '2025-05-29 11:10:00');
INSERT INTO `product_image` VALUES (315, 45, 'desc_45_06.jpeg', 'desc', 6, '2025-05-29 11:10:00', '2025-05-29 11:10:00');
INSERT INTO `product_image` VALUES (316, 46, 'goods46.jpg', 'main', 1, '2025-05-31 11:15:00', '2025-05-31 11:15:00');
INSERT INTO `product_image` VALUES (317, 46, 'desc_46_01.jpeg', 'desc', 1, '2025-05-31 11:15:00', '2025-05-31 11:15:00');
INSERT INTO `product_image` VALUES (318, 46, 'desc_46_02.jpeg', 'desc', 2, '2025-05-31 11:15:00', '2025-05-31 11:15:00');
INSERT INTO `product_image` VALUES (319, 46, 'desc_46_03.jpeg', 'desc', 3, '2025-05-31 11:15:00', '2025-05-31 11:15:00');
INSERT INTO `product_image` VALUES (320, 46, 'desc_46_04.jpeg', 'desc', 4, '2025-05-31 11:15:00', '2025-05-31 11:15:00');
INSERT INTO `product_image` VALUES (321, 46, 'desc_46_05.jpeg', 'desc', 5, '2025-05-31 11:15:00', '2025-05-31 11:15:00');
INSERT INTO `product_image` VALUES (322, 46, 'desc_46_06.jpeg', 'desc', 6, '2025-05-31 11:15:00', '2025-05-31 11:15:00');
INSERT INTO `product_image` VALUES (323, 47, 'goods47.jpg', 'main', 1, '2025-06-02 11:20:00', '2025-06-02 11:20:00');
INSERT INTO `product_image` VALUES (324, 47, 'desc_47_01.jpeg', 'desc', 1, '2025-06-02 11:20:00', '2025-06-02 11:20:00');
INSERT INTO `product_image` VALUES (325, 47, 'desc_47_02.jpeg', 'desc', 2, '2025-06-02 11:20:00', '2025-06-02 11:20:00');
INSERT INTO `product_image` VALUES (326, 47, 'desc_47_03.jpeg', 'desc', 3, '2025-06-02 11:20:00', '2025-06-02 11:20:00');
INSERT INTO `product_image` VALUES (327, 47, 'desc_47_04.jpeg', 'desc', 4, '2025-06-02 11:20:00', '2025-06-02 11:20:00');
INSERT INTO `product_image` VALUES (328, 47, 'desc_47_05.jpeg', 'desc', 5, '2025-06-02 11:20:00', '2025-06-02 11:20:00');
INSERT INTO `product_image` VALUES (329, 47, 'desc_47_06.jpeg', 'desc', 6, '2025-06-02 11:20:00', '2025-06-02 11:20:00');
INSERT INTO `product_image` VALUES (330, 48, 'goods48.jpg', 'main', 1, '2025-06-04 11:25:00', '2025-06-04 11:25:00');
INSERT INTO `product_image` VALUES (331, 48, 'desc_48_01.jpeg', 'desc', 1, '2025-06-04 11:25:00', '2025-06-04 11:25:00');
INSERT INTO `product_image` VALUES (332, 48, 'desc_48_02.jpeg', 'desc', 2, '2025-06-04 11:25:00', '2025-06-04 11:25:00');
INSERT INTO `product_image` VALUES (333, 48, 'desc_48_03.jpeg', 'desc', 3, '2025-06-04 11:25:00', '2025-06-04 11:25:00');
INSERT INTO `product_image` VALUES (334, 48, 'desc_48_04.jpeg', 'desc', 4, '2025-06-04 11:25:00', '2025-06-04 11:25:00');
INSERT INTO `product_image` VALUES (335, 48, 'desc_48_05.jpeg', 'desc', 5, '2025-06-04 11:25:00', '2025-06-04 11:25:00');
INSERT INTO `product_image` VALUES (336, 48, 'desc_48_06.jpeg', 'desc', 6, '2025-06-04 11:25:00', '2025-06-04 11:25:00');
INSERT INTO `product_image` VALUES (337, 49, 'goods49.jpg', 'main', 1, '2025-06-06 11:30:00', '2025-06-06 11:30:00');
INSERT INTO `product_image` VALUES (338, 49, 'desc_49_01.jpeg', 'desc', 1, '2025-06-06 11:30:00', '2025-06-06 11:30:00');
INSERT INTO `product_image` VALUES (339, 49, 'desc_49_02.jpeg', 'desc', 2, '2025-06-06 11:30:00', '2025-06-06 11:30:00');
INSERT INTO `product_image` VALUES (340, 49, 'desc_49_03.jpeg', 'desc', 3, '2025-06-06 11:30:00', '2025-06-06 11:30:00');
INSERT INTO `product_image` VALUES (341, 49, 'desc_49_04.jpeg', 'desc', 4, '2025-06-06 11:30:00', '2025-06-06 11:30:00');
INSERT INTO `product_image` VALUES (342, 49, 'desc_49_05.jpeg', 'desc', 5, '2025-06-06 11:30:00', '2025-06-06 11:30:00');
INSERT INTO `product_image` VALUES (343, 49, 'desc_49_06.jpeg', 'desc', 6, '2025-06-06 11:30:00', '2025-06-06 11:30:00');
INSERT INTO `product_image` VALUES (344, 50, 'goods50.jpg', 'main', 1, '2025-06-08 11:35:00', '2025-06-08 11:35:00');
INSERT INTO `product_image` VALUES (345, 50, 'desc_50_01.jpeg', 'desc', 1, '2025-06-08 11:35:00', '2025-06-08 11:35:00');
INSERT INTO `product_image` VALUES (346, 50, 'desc_50_02.jpeg', 'desc', 2, '2025-06-08 11:35:00', '2025-06-08 11:35:00');
INSERT INTO `product_image` VALUES (347, 50, 'desc_50_03.jpeg', 'desc', 3, '2025-06-08 11:35:00', '2025-06-08 11:35:00');
INSERT INTO `product_image` VALUES (348, 50, 'desc_50_04.jpeg', 'desc', 4, '2025-06-08 11:35:00', '2025-06-08 11:35:00');
INSERT INTO `product_image` VALUES (349, 50, 'desc_50_05.jpeg', 'desc', 5, '2025-06-08 11:35:00', '2025-06-08 11:35:00');
INSERT INTO `product_image` VALUES (350, 50, 'desc_50_06.jpeg', 'desc', 6, '2025-06-08 11:35:00', '2025-06-08 11:35:00');

-- ----------------------------
-- Table structure for product_param
-- ----------------------------
DROP TABLE IF EXISTS `product_param`;
CREATE TABLE `product_param`  (
  `param_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '参数ID',
  `product_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `param_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '参数名称',
  `param_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '参数值',
  `param_unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数单位',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`param_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 281 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品参数表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_param
-- ----------------------------
INSERT INTO `product_param` VALUES (1, 1, '品牌', '惠氏', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (2, 1, '产地', '爱尔兰', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (3, 1, '净含量', '400', 'g', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (4, 1, '适用年龄', '0-6个月', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (5, 1, '保质期', '24', '个月', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (6, 1, '配料表', '有机脱脂奶粉、乳清蛋白粉、植物油、乳糖等', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (7, 1, '储存方法', '密封保存，置于阴凉干燥处', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (8, 2, '品牌', '惠氏', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (9, 2, '产地', '爱尔兰', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (10, 2, '净含量', '800', 'g', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (11, 2, '适用年龄', '6-12个月', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (12, 2, '保质期', '24', '个月', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (13, 2, '配料表', '有机脱脂奶粉、乳清蛋白粉、植物油、乳糖等', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (14, 2, '储存方法', '密封保存，置于阴凉干燥处', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (15, 3, '品牌', '惠氏', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (16, 3, '产地', '爱尔兰', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (17, 3, '净含量', '1200', 'g', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (18, 3, '适用年龄', '1-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (19, 3, '保质期', '24', '个月', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (20, 3, '配料表', '有机脱脂奶粉、乳清蛋白粉、植物油、乳糖等', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (21, 3, '储存方法', '密封保存，置于阴凉干燥处', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (22, 4, '品牌', '帮宝适', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (23, 4, '产地', '中国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (24, 4, '尺码', 'M', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (25, 4, '适用体重', '6-11', 'kg', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (26, 4, '片数', '56', '片/包', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (27, 4, '材质', '无纺布、吸水纸、高分子吸收体等', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (28, 4, '产品特点', '瞬吸干爽，超薄透气，舒适贴合', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (29, 5, '品牌', '花王', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (30, 5, '产地', '日本', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (31, 5, '尺码', 'L', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (32, 5, '适用体重', '9-14', 'kg', 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (33, 5, '片数', '76', '片/包', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (34, 5, '材质', '无纺布、吸水纸、高分子吸收体等', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (35, 5, '产品特点', '透气干爽，减少红屁屁，贴合无漏尿', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (36, 6, '品牌', '美素佳儿', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (37, 6, '产地', '荷兰', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (38, 6, '净含量', '225', 'g', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (39, 6, '适用年龄', '6个月以上', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (40, 6, '保质期', '18', '个月', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (41, 6, '配料表', '有机大米粉、维生素、矿物质等', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (42, 6, '储存方法', '密封保存，置于阴凉干燥处', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (43, 7, '品牌', '费雪', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (44, 7, '产地', '中国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (45, 7, '材质', '绒布、塑料', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (46, 7, '适用年龄', '0-36个月', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (47, 7, '电池规格', '3节AAA电池（不含）', NULL, 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (48, 7, '功能', '声光安抚、音乐播放、助眠', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (49, 7, '尺寸', '30cm', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (50, 8, '品牌', '强生', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (51, 8, '产地', '美国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (52, 8, '净含量', '500', 'ml', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (53, 8, '适用年龄', '0岁以上', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (54, 8, '保质期', '36', '个月', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (55, 8, '配料表', '水、月桂醇硫酸酯钠、椰油酰胺丙基甜菜碱等', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (56, 8, '产品特点', '温和无泪配方，呵护宝宝娇嫩肌肤', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (57, 9, '品牌', '舒儿适', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (58, 9, '产地', '法国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (59, 9, '净含量', '200', 'ml', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (60, 9, '适用年龄', '0岁以上', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (61, 9, '保质期', '36', '个月', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (62, 9, '配料表', '葵花籽油、甜杏仁油、橄榄油等', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (63, 9, '产品特点', '100%天然植物油，温和滋养，促进亲子关系', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (64, 10, '品牌', '贝亲', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (65, 10, '产地', '日本', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (66, 10, '容量', '240', 'ml', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (67, 10, '适用年龄', '0-18个月', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (68, 10, '材质', '高硼硅玻璃、硅胶', NULL, 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (69, 10, '耐热温度', '120', '℃', 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (70, 10, '产品特点', '防胀气设计，耐高温消毒，宽口径易清洗', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (71, 11, '品牌', '卡特', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (72, 11, '产地', '中国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (73, 11, '材质', '100%纯棉', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (74, 11, '适用年龄', '0-12个月', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (75, 11, '尺码', '66/73/80/90', 'cm', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (76, 11, '洗涤说明', '40℃以下水温手洗，不可漂白', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (77, 11, '产品特点', '纯棉材质，柔软舒适，多种颜色可选', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (78, 12, '品牌', '巴拉巴拉', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (79, 12, '产地', '中国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (80, 12, '材质', '95%棉，5%氨纶', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (81, 12, '适用年龄', '1-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (82, 12, '尺码', '90/100/110/120', 'cm', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (83, 12, '洗涤说明', '30℃以下水温手洗，不可漂白', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (84, 12, '产品特点', '优质面料，透气舒适，多种款式可选', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (85, 13, '品牌', '好孩子', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (86, 13, '产地', '中国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (87, 13, '材质', '铝合金、布料', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (88, 13, '适用年龄', '0-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (89, 13, '重量', '7.5', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (90, 13, '功能', '可折叠，可调节靠背，五点式安全带', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (91, 13, '产品特点', '轻便易携，安全舒适，多种颜色可选', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (92, 14, '品牌', '宜家', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (93, 14, '产地', '瑞典', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (94, 14, '材质', '松木', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (95, 14, '适用年龄', '0-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (96, 14, '尺寸', '120*60', 'cm', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (97, 14, '功能', '可调节床板高度，带护栏', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (98, 14, '产品特点', '环保材质，安全稳固，易于组装', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (99, 15, '品牌', 'Pouch', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (100, 15, '产地', '中国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (101, 15, '材质', 'PP塑料、钢管', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (102, 15, '适用年龄', '6个月-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (103, 15, '重量', '8.5', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (104, 15, '功能', '可调节高度，可折叠，带餐盘', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (105, 15, '产品特点', '安全稳固，易于清洁，多种颜色可选', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (106, 16, '品牌', 'ergobaby', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (107, 16, '产地', '美国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (108, 16, '材质', '棉质、尼龙', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (109, 16, '适用年龄', '0-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (110, 16, '承重', '20', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (111, 16, '功能', '多姿势背法，可调节肩带', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (112, 16, '产品特点', '符合人体工学设计，舒适透气，多种颜色可选', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (113, 17, '品牌', '费雪', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (114, 17, '产地', '中国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (115, 17, '材质', '塑料', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (116, 17, '适用年龄', '6-18个月', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (117, 17, '重量', '3.5', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (118, 17, '功能', '带音乐和灯光，可调节高度', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (119, 17, '产品特点', '安全稳固，促进宝宝学步，多种颜色可选', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (120, 18, '品牌', 'Nuna', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (121, 18, '产地', '荷兰', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (122, 18, '材质', '铝合金、布料', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (123, 18, '适用年龄', '0-2岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (124, 18, '重量', '5.5', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (125, 18, '功能', '可调节角度，带震动功能', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (126, 18, '产品特点', '舒适安全，促进宝宝睡眠，多种颜色可选', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (127, 19, '品牌', 'OKBABY', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (128, 19, '产地', '意大利', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (129, 19, '材质', 'PP塑料', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (130, 19, '适用年龄', '0-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (131, 19, '尺寸', '90*50', 'cm', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (132, 19, '功能', '带温度显示，防滑设计', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (133, 19, '产品特点', '安全舒适，易于清洁，多种颜色可选', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (134, 20, '品牌', '好奇', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (135, 20, '产地', '韩国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (136, 20, '材质', '无纺布', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (137, 20, '适用年龄', '0岁以上', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (138, 20, '重量', '50', 'g', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (139, 20, '功能', '防沾，防汗', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (140, 20, '产品特点', '透气舒适，易于清洁，多种颜色可选', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (141, 21, '品牌', '好孩子', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (142, 21, '产地', '中国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (143, 21, '材质', '铝合金', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (144, 21, '适用年龄', '0-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (145, 21, '重量', '8.5', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (146, 21, '功能', '可折叠，可调节靠背，带遮阳篷', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (147, 21, '产品特点', '轻便易携，安全舒适，多种颜色可选', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (148, 22, '品牌', 'ergobaby', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (149, 22, '产地', '美国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (150, 22, '材质', '棉质', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (151, 22, '适用年龄', '0-2岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (152, 22, '承重', '20', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (153, 22, '功能', '多姿势背法，可调节肩带', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (154, 22, '产品特点', '符合人体工学设计，舒适透气', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (155, 23, '品牌', '宜家', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (156, 23, '产地', '瑞典', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (157, 23, '材质', '松木', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (158, 23, '适用年龄', '0-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (159, 23, '尺寸', '120*60', 'cm', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (160, 23, '功能', '可调节床板高度，可转换为幼儿床', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (161, 23, '产品特点', '环保安全，稳固耐用', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (162, 24, '品牌', 'pouch', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (163, 24, '产地', '德国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (164, 24, '材质', 'PP塑料', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (165, 24, '适用年龄', '6个月-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (166, 24, '承重', '15', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (167, 24, '功能', '可调节高度，可折叠', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (168, 24, '产品特点', '安全稳固，易于清洁', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (169, 25, '品牌', 'okbaby', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (170, 25, '产地', '意大利', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (171, 25, '材质', 'PP塑料', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (172, 25, '适用年龄', '0-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (173, 25, '尺寸', '85*50', 'cm', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (174, 25, '功能', '带温度显示，防滑设计', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (175, 25, '产品特点', '安全舒适，易于清洁，多种颜色可选', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (176, 26, '品牌', '好孩子', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (177, 26, '产地', '中国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (178, 26, '材质', '铝合金', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (179, 26, '适用年龄', '0-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (180, 26, '重量', '8.5', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (181, 26, '功能', '可折叠，360度旋转', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (182, 26, '产品特点', '轻便易携，避震设计', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (183, 27, '品牌', 'ergobaby', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (184, 27, '产地', '美国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (185, 27, '材质', '纯棉', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (186, 27, '适用年龄', '0-12个月', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (187, 27, '承重', '15', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (188, 27, '功能', '多种背法，可调节', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (189, 27, '产品特点', '符合人体工学设计，保护宝宝脊椎', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (190, 28, '品牌', 'Peg Perego', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (191, 28, '产地', '意大利', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (192, 28, '材质', 'PP塑料', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (193, 28, '适用年龄', '6个月-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (194, 28, '重量', '8', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (195, 28, '功能', '可调节高度，可折叠', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (196, 28, '产品特点', '安全舒适，易于清洁', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (197, 29, '品牌', '宜家', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (198, 29, '产地', '瑞典', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (199, 29, '材质', '实木', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (200, 29, '适用年龄', '0-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (201, 29, '尺寸', '120*60', 'cm', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (202, 29, '功能', '可调节高度，可转换为儿童床', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (203, 29, '产品特点', '环保材料，安全无毒', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (204, 30, '品牌', 'Britax', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (205, 30, '产地', '德国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (206, 30, '材质', 'EPP', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (207, 30, '适用年龄', '0-4岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (208, 30, '重量', '10', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (209, 30, '功能', '360度旋转，可调节倾斜角度', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (210, 30, '产品特点', '符合国际安全标准，舒适透气', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (211, 31, '品牌', 'Britax', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (212, 31, '产地', '德国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (213, 31, '材质', 'EPP', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (214, 31, '适用年龄', '0-4岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (215, 31, '重量', '10', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (216, 31, '功能', '360度旋转，可调节倾斜角度', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (217, 31, '产品特点', '符合国际安全标准，舒适透气', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (218, 32, '品牌', 'Britax', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (219, 32, '产地', '德国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (220, 32, '材质', 'EPP', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (221, 32, '适用年龄', '0-4岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (222, 32, '重量', '10', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (223, 32, '功能', '360度旋转，可调节倾斜角度', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (224, 32, '产品特点', '符合国际安全标准，舒适透气', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (225, 33, '品牌', 'Britax', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (226, 33, '产地', '德国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (227, 33, '材质', 'EPP', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (228, 33, '适用年龄', '0-4岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (229, 33, '重量', '10', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (230, 33, '功能', '360度旋转，可调节倾斜角度', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (231, 33, '产品特点', '符合国际安全标准，舒适透气', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (232, 34, '品牌', 'Britax', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (233, 34, '产地', '德国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (234, 34, '材质', 'EPP', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (235, 34, '适用年龄', '0-4岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (236, 34, '重量', '10', 'kg', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (237, 34, '功能', '360度旋转，可调节倾斜角度', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (238, 34, '产品特点', '符合国际安全标准，舒适透气', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (239, 35, '品牌', '强生', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (240, 35, '产地', '美国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (241, 35, '净含量', '400', 'ml', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (242, 35, '适用年龄', '0岁以上', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (243, 35, '保质期', '36', '个月', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (244, 35, '配料表', '水、月桂醇硫酸酯钠、椰油酰胺丙基甜菜碱等', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (245, 35, '产品特点', '温和无刺激，不含皂基，不刺激眼睛', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (246, 36, '品牌', '日本狮王', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (247, 36, '产地', '日本', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (248, 36, '材质', '食品级PP材质、超细软毛', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (249, 36, '适用年龄', '6个月-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (250, 36, '规格', '1', '支', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (251, 36, '功能', '防滑手柄，超细软毛', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (252, 36, '产品特点', '安全卫生，柔软不伤牙龈', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (253, 37, '品牌', '好孩子', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (254, 37, '产地', '中国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (255, 37, '材质', 'PP环保材质', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (256, 37, '适用年龄', '0-3岁', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (257, 37, '尺寸', '80*45*23', 'cm', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (258, 37, '功能', '带温度计，防滑设计', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (259, 37, '产品特点', '大容量设计，排水方便，带浴网', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (260, 38, '品牌', '贝德玛', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (261, 38, '产地', '法国', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (262, 38, '净含量', '200', 'ml', 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (263, 38, '适用年龄', '0岁以上', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (264, 38, '保质期', '36', '个月', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (265, 38, '功效', '保湿滋润，舒缓修护', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (266, 38, '产品特点', '无香精，无酒精，低敏配方', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (267, 39, '品牌', '飞利浦', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (268, 39, '产地', '荷兰', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (269, 39, '材质', '不锈钢刀头，ABS外壳', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (270, 39, '适用年龄', '0岁以上', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (271, 39, '电池规格', '2节AA电池（不含）', NULL, 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (272, 39, '功能', '静音设计，多档位调节', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (273, 39, '产品特点', '防水设计，安全圆润刀头', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (274, 40, '品牌', '贝亲', NULL, 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (275, 40, '产地', '日本', NULL, 2, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (276, 40, '材质', '不锈钢', NULL, 3, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (277, 40, '适用年龄', '0岁以上', NULL, 4, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (278, 40, '规格', '1', '套', 5, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (279, 40, '功能', '带放大镜，防夹肉设计', NULL, 6, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `product_param` VALUES (280, 40, '产品特点', '安全圆头设计，不伤宝宝', NULL, 7, '2025-03-05 21:58:47', '2025-03-05 21:58:47');

-- ----------------------------
-- Table structure for product_specs
-- ----------------------------
DROP TABLE IF EXISTS `product_specs`;
CREATE TABLE `product_specs`  (
  `spec_id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `product_id` int UNSIGNED NOT NULL,
  `spec_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规格名称，如颜色、尺寸',
  `spec_values` json NOT NULL COMMENT '规格值列表',
  `sort_order` int NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`spec_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `fk_product_specs_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 76 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品规格表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_specs
-- ----------------------------
INSERT INTO `product_specs` VALUES (1, 1, '规格', '[{\"id\": 1, \"name\": \"1段(0-6个月)\"}, {\"id\": 2, \"name\": \"2段(6-12个月)\"}, {\"id\": 3, \"name\": \"3段(1-3岁)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (2, 1, '重量', '[{\"id\": 1, \"name\": \"900g\"}, {\"id\": 2, \"name\": \"1.8kg\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (3, 2, '规格', '[{\"id\": 1, \"name\": \"1段(0-6个月)\"}, {\"id\": 2, \"name\": \"2段(6-12个月)\"}, {\"id\": 3, \"name\": \"3段(1-3岁)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (4, 2, '重量', '[{\"id\": 1, \"name\": \"800g\"}, {\"id\": 2, \"name\": \"1.6kg\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (5, 3, '规格', '[{\"id\": 1, \"name\": \"1段(0-6个月)\"}, {\"id\": 2, \"name\": \"2段(6-12个月)\"}, {\"id\": 3, \"name\": \"3段(1-3岁)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (6, 4, '规格', '[{\"id\": 1, \"name\": \"2段(6-12个月)\"}, {\"id\": 2, \"name\": \"3段(1-3岁)\"}, {\"id\": 3, \"name\": \"4段(3-6岁)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (7, 4, '重量', '[{\"id\": 1, \"name\": \"400g\"}, {\"id\": 2, \"name\": \"800g\"}, {\"id\": 3, \"name\": \"1.6kg\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (8, 5, '规格', '[{\"id\": 1, \"name\": \"1段(0-6个月)\"}, {\"id\": 2, \"name\": \"2段(6-12个月)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (9, 6, '规格', '[{\"id\": 1, \"name\": \"婴儿配方奶粉\"}, {\"id\": 2, \"name\": \"较大婴儿配方奶粉\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (10, 6, '重量', '[{\"id\": 1, \"name\": \"700g\"}, {\"id\": 2, \"name\": \"1.5kg\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (11, 7, '规格', '[{\"id\": 1, \"name\": \"1段(0-6个月)\"}, {\"id\": 2, \"name\": \"2段(6-12个月)\"}, {\"id\": 3, \"name\": \"3段(1-3岁)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (12, 8, '重量', '[{\"id\": 1, \"name\": \"800g\"}, {\"id\": 2, \"name\": \"1.6kg\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (13, 9, '规格', '[{\"id\": 1, \"name\": \"有机奶粉\"}, {\"id\": 2, \"name\": \"普通奶粉\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (14, 10, '适用年龄', '[{\"id\": 1, \"name\": \"1-3岁\"}, {\"id\": 2, \"name\": \"3-7岁\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (15, 11, '尺寸', '[{\"id\": 1, \"name\": \"S(4-8kg)\"}, {\"id\": 2, \"name\": \"M(6-11kg)\"}, {\"id\": 3, \"name\": \"L(9-14kg)\"}, {\"id\": 4, \"name\": \"XL(12kg以上)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (16, 11, '数量', '[{\"id\": 1, \"name\": \"36片\"}, {\"id\": 2, \"name\": \"72片\"}, {\"id\": 3, \"name\": \"108片\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (17, 12, '尺寸', '[{\"id\": 1, \"name\": \"S(4-8kg)\"}, {\"id\": 2, \"name\": \"M(6-11kg)\"}, {\"id\": 3, \"name\": \"L(9-14kg)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (18, 12, '数量', '[{\"id\": 1, \"name\": \"40片\"}, {\"id\": 2, \"name\": \"80片\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (19, 13, '尺寸', '[{\"id\": 1, \"name\": \"NB(0-5kg)\"}, {\"id\": 2, \"name\": \"S(4-8kg)\"}, {\"id\": 3, \"name\": \"M(6-11kg)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (20, 14, '尺寸', '[{\"id\": 1, \"name\": \"M(6-11kg)\"}, {\"id\": 2, \"name\": \"L(9-14kg)\"}, {\"id\": 3, \"name\": \"XL(12kg以上)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (21, 15, '类型', '[{\"id\": 1, \"name\": \"纸尿裤\"}, {\"id\": 2, \"name\": \"拉拉裤\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (22, 15, '数量', '[{\"id\": 1, \"name\": \"50片\"}, {\"id\": 2, \"name\": \"100片\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (23, 16, '尺寸', '[{\"id\": 1, \"name\": \"S(4-8kg)\"}, {\"id\": 2, \"name\": \"M(6-11kg)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (24, 17, '类型', '[{\"id\": 1, \"name\": \"日用\"}, {\"id\": 2, \"name\": \"夜用加量版\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (25, 18, '尺寸', '[{\"id\": 1, \"name\": \"L(9-14kg)\"}, {\"id\": 2, \"name\": \"XL(12kg以上)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (26, 19, '尺寸', '[{\"id\": 1, \"name\": \"59cm(0-3个月)\"}, {\"id\": 2, \"name\": \"66cm(3-6个月)\"}, {\"id\": 3, \"name\": \"73cm(6-12个月)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (27, 19, '颜色', '[{\"id\": 1, \"name\": \"粉色\"}, {\"id\": 2, \"name\": \"蓝色\"}, {\"id\": 3, \"name\": \"黄色\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (28, 20, '尺寸', '[{\"id\": 1, \"name\": \"66cm(3-6个月)\"}, {\"id\": 2, \"name\": \"73cm(6-12个月)\"}, {\"id\": 3, \"name\": \"80cm(1-2岁)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (29, 20, '颜色', '[{\"id\": 1, \"name\": \"蓝白条\"}, {\"id\": 2, \"name\": \"灰色\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (30, 21, '尺寸', '[{\"id\": 1, \"name\": \"73cm(6-12个月)\"}, {\"id\": 2, \"name\": \"80cm(1-2岁)\"}, {\"id\": 3, \"name\": \"90cm(2-3岁)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (31, 22, '尺寸', '[{\"id\": 1, \"name\": \"80cm(1-2岁)\"}, {\"id\": 2, \"name\": \"90cm(2-3岁)\"}, {\"id\": 3, \"name\": \"100cm(3-4岁)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (32, 22, '颜色', '[{\"id\": 1, \"name\": \"白色\"}, {\"id\": 2, \"name\": \"粉色\"}, {\"id\": 3, \"name\": \"蓝色\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (33, 23, '尺寸', '[{\"id\": 1, \"name\": \"小号(0-6个月)\"}, {\"id\": 2, \"name\": \"中号(6-12个月)\"}, {\"id\": 3, \"name\": \"大号(1-3岁)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (34, 24, '尺寸', '[{\"id\": 1, \"name\": \"90cm(2-3岁)\"}, {\"id\": 2, \"name\": \"100cm(3-4岁)\"}, {\"id\": 3, \"name\": \"110cm(4-5岁)\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (35, 25, '尺寸', '[{\"id\": 1, \"name\": \"6-12个月\"}, {\"id\": 2, \"name\": \"1-3岁\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (36, 26, '颜色', '[{\"id\": 1, \"name\": \"红色\"}, {\"id\": 2, \"name\": \"蓝色\"}, {\"id\": 3, \"name\": \"粉色\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (37, 27, '尺寸', '[{\"id\": 1, \"name\": \"0-6个月\"}, {\"id\": 2, \"name\": \"6-12个月\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (38, 28, '款式', '[{\"id\": 1, \"name\": \"春秋款\"}, {\"id\": 2, \"name\": \"冬季款\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (39, 29, '适用年龄', '[{\"id\": 1, \"name\": \"0-1岁\"}, {\"id\": 2, \"name\": \"1-3岁\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (40, 30, '类型', '[{\"id\": 1, \"name\": \"积木套装\"}, {\"id\": 2, \"name\": \"单个积木\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (41, 30, '数量', '[{\"id\": 1, \"name\": \"40块\"}, {\"id\": 2, \"name\": \"80块\"}, {\"id\": 3, \"name\": \"120块\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (42, 31, '尺寸', '[{\"id\": 1, \"name\": \"小号\"}, {\"id\": 2, \"name\": \"大号\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (43, 32, '类型', '[{\"id\": 1, \"name\": \"动物系列\"}, {\"id\": 2, \"name\": \"交通工具系列\"}, {\"id\": 3, \"name\": \"食物系列\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (44, 33, '适用年龄', '[{\"id\": 1, \"name\": \"3-6岁\"}, {\"id\": 2, \"name\": \"6岁以上\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (45, 34, '颜色', '[{\"id\": 1, \"name\": \"红色\"}, {\"id\": 2, \"name\": \"蓝色\"}, {\"id\": 3, \"name\": \"黄色\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (46, 35, '材质', '[{\"id\": 1, \"name\": \"塑料\"}, {\"id\": 2, \"name\": \"木质\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (47, 36, '类型', '[{\"id\": 1, \"name\": \"拼图\"}, {\"id\": 2, \"name\": \"游戏套装\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (48, 37, '容量', '[{\"id\": 1, \"name\": \"200ml\"}, {\"id\": 2, \"name\": \"400ml\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (49, 38, '功效', '[{\"id\": 1, \"name\": \"滋润型\"}, {\"id\": 2, \"name\": \"修复型\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (50, 39, '容量', '[{\"id\": 1, \"name\": \"250ml\"}, {\"id\": 2, \"name\": \"500ml\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (51, 40, '类型', '[{\"id\": 1, \"name\": \"杀菌型\"}, {\"id\": 2, \"name\": \"护肤型\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (52, 40, '容量', '[{\"id\": 1, \"name\": \"250ml\"}, {\"id\": 2, \"name\": \"500ml\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (53, 41, '功效', '[{\"id\": 1, \"name\": \"温和型\"}, {\"id\": 2, \"name\": \"防敏感型\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (54, 42, '规格', '[{\"id\": 1, \"name\": \"小包装\"}, {\"id\": 2, \"name\": \"大包装\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (55, 43, '容量', '[{\"id\": 1, \"name\": \"100ml\"}, {\"id\": 2, \"name\": \"200ml\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (56, 44, '类型', '[{\"id\": 1, \"name\": \"婴儿专用\"}, {\"id\": 2, \"name\": \"儿童专用\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (57, 45, '容量', '[{\"id\": 1, \"name\": \"160ml\"}, {\"id\": 2, \"name\": \"240ml\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (58, 45, '材质', '[{\"id\": 1, \"name\": \"PP材质\"}, {\"id\": 2, \"name\": \"玻璃\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (59, 46, '类型', '[{\"id\": 1, \"name\": \"奶瓶\"}, {\"id\": 2, \"name\": \"奶瓶套装\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (60, 47, '适用年龄', '[{\"id\": 1, \"name\": \"0-6个月\"}, {\"id\": 2, \"name\": \"6个月以上\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (61, 48, '尺寸', '[{\"id\": 1, \"name\": \"小号\"}, {\"id\": 2, \"name\": \"中号\"}, {\"id\": 3, \"name\": \"大号\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (62, 49, '材质', '[{\"id\": 1, \"name\": \"硅胶\"}, {\"id\": 2, \"name\": \"塑料\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (63, 50, '类型', '[{\"id\": 1, \"name\": \"固体辅食碗\"}, {\"id\": 2, \"name\": \"液体辅食碗\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (64, 51, '颜色', '[{\"id\": 1, \"name\": \"粉色\"}, {\"id\": 2, \"name\": \"蓝色\"}, {\"id\": 3, \"name\": \"绿色\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (65, 52, '材质', '[{\"id\": 1, \"name\": \"不锈钢\"}, {\"id\": 2, \"name\": \"塑料\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (66, 53, '规格', '[{\"id\": 1, \"name\": \"基础款\"}, {\"id\": 2, \"name\": \"高级款\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (67, 54, '尺寸', '[{\"id\": 1, \"name\": \"M\"}, {\"id\": 2, \"name\": \"L\"}, {\"id\": 3, \"name\": \"XL\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (68, 54, '颜色', '[{\"id\": 1, \"name\": \"肤色\"}, {\"id\": 2, \"name\": \"黑色\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (69, 55, '规格', '[{\"id\": 1, \"name\": \"单瓶装\"}, {\"id\": 2, \"name\": \"3瓶装\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (70, 56, '功效', '[{\"id\": 1, \"name\": \"预防\"}, {\"id\": 2, \"name\": \"修复\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (71, 57, '尺寸', '[{\"id\": 1, \"name\": \"均码\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (72, 57, '颜色', '[{\"id\": 1, \"name\": \"白色\"}, {\"id\": 2, \"name\": \"粉色\"}, {\"id\": 3, \"name\": \"蓝色\"}]', 2, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (73, 58, '类型', '[{\"id\": 1, \"name\": \"DHA\"}, {\"id\": 2, \"name\": \"钙铁锌\"}, {\"id\": 3, \"name\": \"叶酸\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (74, 59, '规格', '[{\"id\": 1, \"name\": \"标准型\"}, {\"id\": 2, \"name\": \"加大型\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');
INSERT INTO `product_specs` VALUES (75, 60, '类型', '[{\"id\": 1, \"name\": \"孕前\"}, {\"id\": 2, \"name\": \"孕中\"}, {\"id\": 3, \"name\": \"孕后\"}]', 1, '2025-04-28 10:00:00', '2025-04-28 10:00:00');

-- ----------------------------
-- Table structure for refund
-- ----------------------------
DROP TABLE IF EXISTS `refund`;
CREATE TABLE `refund`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '退款ID',
  `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退款单号',
  `order_id` int UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `payment_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '支付ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '退款金额',
  `refund_reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退款原因',
  `refund_reason_detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款原因详情',
  `evidence_images` json NULL COMMENT '凭证图片',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '退款状态：PENDING-待处理, APPROVED-已批准, REJECTED-已拒绝, PROCESSING-处理中, COMPLETED-已完成, FAILED-退款失败',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `refund_account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款账户',
  `refund_channel` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款渠道：ALIPAY-支付宝, WECHAT-微信, BANK-银行卡',
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款交易号',
  `admin_id` int UNSIGNED NULL DEFAULT NULL COMMENT '处理人ID',
  `admin_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理人姓名',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `version` int NOT NULL DEFAULT 0 COMMENT '版本号，用于乐观锁',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_refund_no`(`refund_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_payment_id`(`payment_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_refund_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_refund_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_refund_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退款申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of refund
-- ----------------------------
INSERT INTO `refund` VALUES (2, 'R1747964709693D075A0', 228, 'OD1747922100745f8639c', 8, 264, 716.00, '不想要了', '', '[]', 'PROCESSING', NULL, NULL, NULL, 'WECHAT', NULL, 1, 'admin', 0, 3, '2025-05-23 09:45:10', '2025-05-23 09:45:10');
INSERT INTO `refund` VALUES (3, 'R1747965703901991F4D', 225, 'OD17478392282642a46ae', 8, 261, 399.90, '发货太慢', '', '[]', 'COMPLETED', NULL, '2025-05-23 11:16:25', NULL, 'ALIPAY', 'ALIPAY', 1, 'admin', 0, 5, '2025-05-23 10:01:44', '2025-05-23 10:01:44');
INSERT INTO `refund` VALUES (4, 'R17501675810529B76EB', 253, 'OD1749726408755f094d7', 8, 278, 378.00, 'unwanted', '', '[]', 'COMPLETED', NULL, '2025-06-27 11:52:23', NULL, 'ALIPAY', 'OD1749726408755f094d7', 1, 'admin', 0, 5, '2025-06-17 21:39:41', '2025-06-17 21:39:41');
INSERT INTO `refund` VALUES (5, 'R1750999409482F6DA75', 254, 'OD175099804314016fc06', 8, 279, 398.00, '收到商品破损', '', '[]', 'COMPLETED', NULL, '2025-06-27 12:49:47', NULL, 'WALLET', 'OD175099804314016fc06', 1, 'admin', 0, 5, '2025-06-27 12:43:29', '2025-06-27 12:43:29');
INSERT INTO `refund` VALUES (6, 'R175204525835868B750', 255, 'OD175204518724432e360', 8, 280, 899.00, '不想要了', '', '[]', 'COMPLETED', NULL, '2025-07-09 15:15:22', NULL, 'WALLET', 'OD175204518724432e360', 1, 'admin', 0, 5, '2025-07-09 15:14:18', '2025-07-09 15:14:18');
INSERT INTO `refund` VALUES (7, 'R175221112681766C869', 260, 'OD1752210905878845ae7', 8, 286, 89.90, '不想要了', '', '[]', 'COMPLETED', NULL, '2025-07-11 13:19:52', NULL, 'ALIPAY', 'OD1752210905878845ae7', 1, 'admin', 0, 5, '2025-07-11 13:18:47', '2025-07-11 13:18:47');
INSERT INTO `refund` VALUES (8, 'R175221830584853CA2C', 263, 'OD175221820097485ae4b', 9, 290, 1830.00, '商品质量问题', '', '[]', 'COMPLETED', NULL, '2025-07-11 15:19:19', NULL, 'ALIPAY', 'OD175221820097485ae4b', 1, 'admin', 0, 5, '2025-07-11 15:18:26', '2025-07-11 15:18:26');
INSERT INTO `refund` VALUES (9, 'R1752661171343E90907', 264, 'OD1752661153782d5f325', 8, 291, 399.90, '商品与描述不符', '', '[]', 'COMPLETED', NULL, '2025-07-16 18:51:14', NULL, 'WECHAT', 'OD1752661153782d5f325', 1, 'admin', 0, 5, '2025-07-16 18:19:31', '2025-07-16 18:19:31');
INSERT INTO `refund` VALUES (10, 'R1752663378457377ED9', 265, 'OD1752663366489bad395', 8, 292, 179.00, '不想要了', '', '[]', 'PROCESSING', NULL, NULL, NULL, 'WECHAT', NULL, 1, 'admin', 0, 3, '2025-07-16 18:56:18', '2025-07-16 18:56:18');

-- ----------------------------
-- Table structure for refund_log
-- ----------------------------
DROP TABLE IF EXISTS `refund_log`;
CREATE TABLE `refund_log`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `refund_id` bigint UNSIGNED NOT NULL COMMENT '退款ID',
  `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退款单号',
  `old_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '旧状态',
  `new_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '新状态',
  `operator_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作者类型：USER-用户, ADMIN-管理员, SYSTEM-系统',
  `operator_id` int UNSIGNED NULL DEFAULT NULL COMMENT '操作者ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作者名称',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_refund_id`(`refund_id` ASC) USING BTREE,
  INDEX `idx_refund_no`(`refund_no` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_log_refund` FOREIGN KEY (`refund_id`) REFERENCES `refund` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退款处理日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of refund_log
-- ----------------------------
INSERT INTO `refund_log` VALUES (1, 3, 'R1747965703901991F4D', 'PENDING', 'PENDING', 'USER', 8, '用户8', '用户申请退款：发货太慢', '2025-05-23 10:01:44');
INSERT INTO `refund_log` VALUES (2, 3, 'R1747965703901991F4D', 'PENDING', 'APPROVED', 'ADMIN', 1, 'admin', '管理员批准退款', '2025-05-23 10:59:25');
INSERT INTO `refund_log` VALUES (3, 3, 'R1747965703901991F4D', 'APPROVED', 'APPROVED', 'SYSTEM', NULL, '系统', '调用退款接口失败: 支付未完成，无法退款', '2025-05-23 11:03:03');
INSERT INTO `refund_log` VALUES (4, 3, 'R1747965703901991F4D', 'APPROVED', 'PROCESSING', 'ADMIN', 1, 'admin', '管理员开始处理退款，渠道：ALIPAY', '2025-05-23 11:03:03');
INSERT INTO `refund_log` VALUES (5, 3, 'R1747965703901991F4D', 'PROCESSING', 'PROCESSING', 'SYSTEM', NULL, '系统', '查询支付宝退款状态失败: 支付宝退款查询失败：交易不存在', '2025-05-23 11:16:25');
INSERT INTO `refund_log` VALUES (6, 3, 'R1747965703901991F4D', 'PROCESSING', 'COMPLETED', 'ADMIN', 1, 'admin', '管理员完成退款，交易号：ALIPAY', '2025-05-23 11:16:25');
INSERT INTO `refund_log` VALUES (7, 2, 'R1747964709693D075A0', 'PENDING', 'APPROVED', 'ADMIN', 1, 'admin', '管理员批准退款', '2025-05-29 23:05:33');
INSERT INTO `refund_log` VALUES (8, 2, 'R1747964709693D075A0', 'APPROVED', 'PROCESSING', 'ADMIN', 1, 'admin', '管理员开始处理退款，渠道：WECHAT', '2025-05-29 23:05:47');
INSERT INTO `refund_log` VALUES (9, 4, 'R17501675810529B76EB', 'PENDING', 'PENDING', 'USER', 8, '用户8', '用户申请退款：unwanted', '2025-06-17 21:39:41');
INSERT INTO `refund_log` VALUES (10, 4, 'R17501675810529B76EB', 'PENDING', 'APPROVED', 'ADMIN', 1, 'admin', '管理员批准退款', '2025-06-27 11:51:48');
INSERT INTO `refund_log` VALUES (11, 4, 'R17501675810529B76EB', 'APPROVED', 'APPROVED', 'SYSTEM', NULL, '系统', '调用退款接口失败: 支付未完成，无法退款', '2025-06-27 11:51:58');
INSERT INTO `refund_log` VALUES (12, 4, 'R17501675810529B76EB', 'APPROVED', 'PROCESSING', 'ADMIN', 1, 'admin', '管理员开始处理退款，渠道：ALIPAY', '2025-06-27 11:51:58');
INSERT INTO `refund_log` VALUES (13, 4, 'R17501675810529B76EB', 'PROCESSING', 'PROCESSING', 'SYSTEM', NULL, '系统', '查询支付宝退款状态失败: 支付宝退款查询失败：交易不存在', '2025-06-27 11:52:23');
INSERT INTO `refund_log` VALUES (14, 4, 'R17501675810529B76EB', 'PROCESSING', 'COMPLETED', 'ADMIN', 1, 'admin', '管理员完成退款，交易号：OD1749726408755f094d7', '2025-06-27 11:52:23');
INSERT INTO `refund_log` VALUES (15, 5, 'R1750999409482F6DA75', 'PENDING', 'PENDING', 'USER', 8, '用户8', '用户申请退款：收到商品破损', '2025-06-27 12:43:29');
INSERT INTO `refund_log` VALUES (16, 5, 'R1750999409482F6DA75', 'PENDING', 'APPROVED', 'ADMIN', 1, 'admin', '管理员批准退款', '2025-06-27 12:49:25');
INSERT INTO `refund_log` VALUES (17, 5, 'R1750999409482F6DA75', 'APPROVED', 'PROCESSING', 'ADMIN', 1, 'admin', '管理员开始处理退款，渠道：WALLET', '2025-06-27 12:49:34');
INSERT INTO `refund_log` VALUES (18, 5, 'R1750999409482F6DA75', 'PROCESSING', 'COMPLETED', 'ADMIN', 1, 'admin', '管理员完成退款，交易号：OD175099804314016fc06', '2025-06-27 12:49:47');
INSERT INTO `refund_log` VALUES (19, 6, 'R175204525835868B750', 'PENDING', 'PENDING', 'USER', 8, '用户8', '用户申请退款：不想要了', '2025-07-09 15:14:18');
INSERT INTO `refund_log` VALUES (20, 6, 'R175204525835868B750', 'PENDING', 'APPROVED', 'ADMIN', 1, 'admin', '管理员批准退款', '2025-07-09 15:14:47');
INSERT INTO `refund_log` VALUES (21, 6, 'R175204525835868B750', 'APPROVED', 'PROCESSING', 'ADMIN', 1, 'admin', '管理员开始处理退款，渠道：WALLET', '2025-07-09 15:14:56');
INSERT INTO `refund_log` VALUES (22, 6, 'R175204525835868B750', 'PROCESSING', 'COMPLETED', 'ADMIN', 1, 'admin', '管理员完成退款，交易号：OD175204518724432e360', '2025-07-09 15:15:22');
INSERT INTO `refund_log` VALUES (23, 7, 'R175221112681766C869', 'PENDING', 'PENDING', 'USER', 8, '用户8', '用户申请退款：不想要了', '2025-07-11 13:18:47');
INSERT INTO `refund_log` VALUES (24, 7, 'R175221112681766C869', 'PENDING', 'APPROVED', 'ADMIN', 1, 'admin', '管理员批准退款', '2025-07-11 13:19:07');
INSERT INTO `refund_log` VALUES (25, 7, 'R175221112681766C869', 'APPROVED', 'APPROVED', 'SYSTEM', NULL, '系统', '调用退款接口失败: 支付未完成，无法退款', '2025-07-11 13:19:17');
INSERT INTO `refund_log` VALUES (26, 7, 'R175221112681766C869', 'APPROVED', 'PROCESSING', 'ADMIN', 1, 'admin', '管理员开始处理退款，渠道：ALIPAY', '2025-07-11 13:19:17');
INSERT INTO `refund_log` VALUES (27, 7, 'R175221112681766C869', 'PROCESSING', 'PROCESSING', 'SYSTEM', NULL, '系统', '查询支付宝退款状态失败: 支付宝退款查询失败：交易不存在', '2025-07-11 13:19:52');
INSERT INTO `refund_log` VALUES (28, 7, 'R175221112681766C869', 'PROCESSING', 'COMPLETED', 'ADMIN', 1, 'admin', '管理员完成退款，交易号：OD1752210905878845ae7', '2025-07-11 13:19:52');
INSERT INTO `refund_log` VALUES (29, 8, 'R175221830584853CA2C', 'PENDING', 'PENDING', 'USER', 9, '用户9', '用户申请退款：商品质量问题', '2025-07-11 15:18:26');
INSERT INTO `refund_log` VALUES (30, 8, 'R175221830584853CA2C', 'PENDING', 'APPROVED', 'ADMIN', 1, 'admin', '管理员批准退款', '2025-07-11 15:18:41');
INSERT INTO `refund_log` VALUES (31, 8, 'R175221830584853CA2C', 'APPROVED', 'APPROVED', 'SYSTEM', NULL, '系统', '调用退款接口失败: 支付未完成，无法退款', '2025-07-11 15:18:54');
INSERT INTO `refund_log` VALUES (32, 8, 'R175221830584853CA2C', 'APPROVED', 'PROCESSING', 'ADMIN', 1, 'admin', '管理员开始处理退款，渠道：ALIPAY', '2025-07-11 15:18:54');
INSERT INTO `refund_log` VALUES (33, 8, 'R175221830584853CA2C', 'PROCESSING', 'PROCESSING', 'SYSTEM', NULL, '系统', '查询支付宝退款状态失败: 支付宝退款查询失败：交易不存在', '2025-07-11 15:19:19');
INSERT INTO `refund_log` VALUES (34, 8, 'R175221830584853CA2C', 'PROCESSING', 'COMPLETED', 'ADMIN', 1, 'admin', '管理员完成退款，交易号：OD175221820097485ae4b', '2025-07-11 15:19:19');
INSERT INTO `refund_log` VALUES (35, 9, 'R1752661171343E90907', 'PENDING', 'PENDING', 'USER', 8, '用户8', '用户申请退款：商品与描述不符', '2025-07-16 18:19:31');
INSERT INTO `refund_log` VALUES (36, 9, 'R1752661171343E90907', 'PENDING', 'APPROVED', 'ADMIN', 1, 'admin', '管理员批准退款', '2025-07-16 18:39:49');
INSERT INTO `refund_log` VALUES (37, 9, 'R1752661171343E90907', 'APPROVED', 'PROCESSING', 'ADMIN', 1, 'admin', '管理员开始处理退款，渠道：WECHAT', '2025-07-16 18:44:18');
INSERT INTO `refund_log` VALUES (38, 9, 'R1752661171343E90907', 'PROCESSING', 'COMPLETED', 'ADMIN', 1, 'admin', '管理员完成退款，交易号：OD1752661153782d5f325', '2025-07-16 18:51:14');
INSERT INTO `refund_log` VALUES (39, 10, 'R1752663378457377ED9', 'PENDING', 'PENDING', 'USER', 8, '用户8', '用户申请退款：不想要了', '2025-07-16 18:56:18');
INSERT INTO `refund_log` VALUES (40, 10, 'R1752663378457377ED9', 'PENDING', 'APPROVED', 'ADMIN', 1, 'admin', '管理员批准退款', '2025-07-16 18:56:32');
INSERT INTO `refund_log` VALUES (41, 10, 'R1752663378457377ED9', 'APPROVED', 'PROCESSING', 'ADMIN', 1, 'admin', '管理员开始处理退款，渠道：WECHAT', '2025-07-16 19:23:29');

-- ----------------------------
-- Table structure for search_statistics
-- ----------------------------
DROP TABLE IF EXISTS `search_statistics`;
CREATE TABLE `search_statistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `keyword` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '搜索关键词',
  `search_count` int NOT NULL DEFAULT 1 COMMENT '搜索次数',
  `result_count` bigint NOT NULL DEFAULT 0 COMMENT '搜索结果数量',
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID',
  `source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'web' COMMENT '搜索来源：web, mobile, api等',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '搜索IP地址',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户代理',
  `search_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
  `response_time` bigint NULL DEFAULT NULL COMMENT '响应时间（毫秒）',
  `has_click` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否有点击结果',
  `clicked_product_id` int NULL DEFAULT NULL COMMENT '点击的商品ID',
  `session_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '搜索会话ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_keyword`(`keyword` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_search_time`(`search_time` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_keyword_user_time`(`keyword` ASC, `user_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_result_count`(`result_count` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '搜索统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of search_statistics
-- ----------------------------
INSERT INTO `search_statistics` VALUES (1, '奶粉', 156, 45, NULL, 'web', NULL, NULL, '2024-01-15 10:30:00', NULL, 0, NULL, NULL, '2025-07-14 11:34:36', '2025-07-14 11:34:36');
INSERT INTO `search_statistics` VALUES (2, '纸尿裤', 134, 38, NULL, 'web', NULL, NULL, '2024-01-15 11:15:00', NULL, 0, NULL, NULL, '2025-07-14 11:34:36', '2025-07-14 11:34:36');
INSERT INTO `search_statistics` VALUES (3, '婴儿车', 98, 22, NULL, 'web', NULL, NULL, '2024-01-15 14:20:00', NULL, 0, NULL, NULL, '2025-07-14 11:34:36', '2025-07-14 11:34:36');
INSERT INTO `search_statistics` VALUES (4, '奶瓶', 87, 31, NULL, 'web', NULL, NULL, '2024-01-15 16:45:00', NULL, 0, NULL, NULL, '2025-07-14 11:34:36', '2025-07-14 11:34:36');
INSERT INTO `search_statistics` VALUES (5, '玩具', 76, 89, NULL, 'web', NULL, NULL, '2024-01-15 18:30:00', NULL, 0, NULL, NULL, '2025-07-14 11:34:36', '2025-07-14 11:34:36');
INSERT INTO `search_statistics` VALUES (6, '辅食', 65, 27, NULL, 'web', NULL, NULL, '2024-01-16 09:15:00', NULL, 0, NULL, NULL, '2025-07-14 11:34:36', '2025-07-14 11:34:36');
INSERT INTO `search_statistics` VALUES (7, '童装', 54, 156, NULL, 'web', NULL, NULL, '2024-01-16 13:20:00', NULL, 0, NULL, NULL, '2025-07-14 11:34:36', '2025-07-14 11:34:36');
INSERT INTO `search_statistics` VALUES (8, '安全座椅', 43, 12, NULL, 'web', NULL, NULL, '2024-01-16 15:45:00', NULL, 0, NULL, NULL, '2025-07-14 11:34:36', '2025-07-14 11:34:36');
INSERT INTO `search_statistics` VALUES (9, '洗护用品', 38, 67, NULL, 'web', NULL, NULL, '2024-01-16 17:30:00', NULL, 0, NULL, NULL, '2025-07-14 11:34:36', '2025-07-14 11:34:36');
INSERT INTO `search_statistics` VALUES (10, '益智玩具', 32, 45, NULL, 'web', NULL, NULL, '2024-01-16 19:15:00', NULL, 0, NULL, NULL, '2025-07-14 11:34:36', '2025-07-14 11:34:36');

-- ----------------------------
-- Table structure for spec_value
-- ----------------------------
DROP TABLE IF EXISTS `spec_value`;
CREATE TABLE `spec_value`  (
  `value_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '规格值ID',
  `spec_id` int UNSIGNED NOT NULL COMMENT '规格ID',
  `value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规格值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`value_id`) USING BTREE,
  INDEX `idx_spec_id`(`spec_id` ASC) USING BTREE,
  INDEX `idx_value`(`value`(20) ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '规格值表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of spec_value
-- ----------------------------
INSERT INTO `spec_value` VALUES (1, 1, '400g', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (2, 1, '800g', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (3, 1, '1200g', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (4, 2, '是', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (5, 2, '否', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (6, 3, '400g', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (7, 3, '800g', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (8, 3, '1200g', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (9, 4, '是', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (10, 4, '否', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (11, 5, '400g', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (12, 5, '800g', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (13, 5, '1200g', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (14, 6, '是', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (15, 6, '否', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (16, 7, '小包装（28片）', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (17, 7, '中包装（56片）', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (18, 7, '大包装（84片）', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (19, 8, '小包装（24片）', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (20, 8, '中包装（50片）', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (21, 8, '大包装（76片）', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (22, 9, '原味', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (23, 9, '苹果味', '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `spec_value` VALUES (24, 9, '香蕉味', '2025-03-05 21:58:47', '2025-03-05 21:58:47');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
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
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁控制',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_version`(`version` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$99ZE4lyxIxU0elrvrkWzZuGkNAHmewaZX2GMTr//rRb8A/M6sPo7q', 'Administrator', 'admin@example.com', '13777788866', 'http://localhost:5173/avatars/1/cce1bec0becb473980db13633e734130.gif', 'unknown', '1990-01-01', 1, 'admin', '2025-03-05 21:58:47', '2025-06-06 13:33:58', 1);
INSERT INTO `user` VALUES (2, '123', '$2a$10$n9nJqYeP1UJ1YB8wJIP.1.q9RowPIWJ/c6v1z5NjsHYQkHJDCfKSi', '张三', 'user1@example.com', '13800000001', 'avatars/user1.jpg', 'female', '1992-05-20', 0, 'user', '2025-03-05 21:58:47', '2025-03-10 22:31:26', 1);
INSERT INTO `user` VALUES (3, 'user', '/wbJNy.uYxIgMwR.q2rUW6Y4Xi3YXDWqueMgGRW', '李四', 'user2@qq.com', '13800000002', 'avatars/user2.jpg', 'male', '1988-11-15', 0, 'user', '2025-03-05 21:58:47', '2025-03-10 22:23:02', 1);
INSERT INTO `user` VALUES (4, '157', '123456', '小明', '125645@qq.com', '15222366958', NULL, 'unknown', NULL, 0, 'user', '2025-03-05 22:43:49', '2025-03-11 21:46:01', 1);
INSERT INTO `user` VALUES (8, 'test2', '$2a$10$UbDKxWBFG5B2wA.wIi/fJuO4PizzMZZ/p9atEIBzuJhNnIi2YPhSe', '张三', 'test2@qq.com', '13888144527', 'http://localhost:5173/avatars/8/f9d97f2d8ea8470eb20cbf93bcef6158.png', 'male', '2025-03-27', 1, 'user', '2025-03-06 18:11:00', '2025-07-14 11:31:53', 1);
INSERT INTO `user` VALUES (9, 'pwz', '$2a$10$q11Omyzc1zBghScC.GZP7O.kmWH2TpfSrIF3rh5XWaP62HhBPgX2q', '彭伟株', '2898191344@qq.com', '13444755888', 'http://localhost:5173/avatars/9/b15f49fa72b6496f8c769b5df4b442ad.png', 'male', '2025-03-16', 1, 'user', '2025-03-10 22:26:02', '2025-05-23 13:13:23', 1);
INSERT INTO `user` VALUES (10, 'test', '$2a$10$AdJGyc3uxy1fwxCWcH/QjO1DitbSxzipkMXUTS8X9gSYOa2SXQE1q', '测试用户', 'test@157.com', '18555622565', NULL, 'male', '2025-03-15', 0, 'user', '2025-03-13 18:18:48', '2025-03-18 20:20:39', 1);
INSERT INTO `user` VALUES (11, '133', '123456', '特殊测试用户', '133@qq.com', '15774858562', NULL, NULL, NULL, 1, 'user', '2025-03-14 23:11:49', '2025-03-15 22:45:54', 1);
INSERT INTO `user` VALUES (48, 'test_special', 'special123', '特殊测试用户', 'test_special@example.com', '13444322343', NULL, NULL, NULL, 1, 'user', '2025-03-17 15:17:07', '2025-03-17 15:17:07', 1);
INSERT INTO `user` VALUES (49, 'wx_440cae9953', '$2a$10$oCQQGawYzoalFG8qSVDoJOm6aoqNxxHSnBIwkFg4IwtE/fGaBErw2', '微信登录', 'wx_440cae9953@wx.muyingmall.com', NULL, NULL, NULL, NULL, 1, 'user', '2025-04-16 14:29:38', '2025-05-29 23:07:15', 1);
INSERT INTO `user` VALUES (50, 'wx_94f2bcaff8', '$2a$10$mG68CrUgrlvtL14KZv2GyuO.SUgPA8XB4NYGm3SFS0ZfUKCiU1y7y', 'wx_94f2bcaff8', 'wx_94f2bcaff8@wx.muyingmall.com', NULL, NULL, NULL, NULL, 0, 'user', '2025-04-16 14:32:42', '2025-04-16 14:32:42', 1);
INSERT INTO `user` VALUES (57, 'ttttt1', '$2a$10$B7g.yx17JFrPHAH/2w1Sme0P/IgsdHrUiP85ENnd//OQd8TIt3pOO', 'ttttt', '25365245@qq.com', '13555622356', NULL, 'unknown', NULL, 1, 'user', '2025-04-28 12:57:21', '2025-06-06 13:32:44', 1);
INSERT INTO `user` VALUES (58, 'demo', '$2a$10$XSJB8Yyhk7D5mTkkUhUt5.mREvHYJMjR6YRHGjLBySiFoBOnfDVgy', 'demo', '33@qq.com', '13555622356', 'http://localhost:5173/avatars/58/ee0c38cd173b4978a69387f9d455fd40.png', 'unknown', NULL, 1, 'user', '2025-06-09 22:21:35', '2025-06-09 22:21:35', 1);
INSERT INTO `user` VALUES (59, 'qqqq', '$2a$10$APRywN7vxR.mieLfglA5y.088ZGsCJGkdsWJ8rxeNSk7n2Pf5iwxe', 'qqqq', '56@qq.com', '13555266589', NULL, 'unknown', NULL, 1, 'user', '2025-07-10 14:31:30', '2025-07-10 14:31:30', 1);
INSERT INTO `user` VALUES (60, 'testuser', '$2a$10$4vyzRG4N6p85g.HM6GZKcuXEurN0AlbLpEnh3DFYv5YfDsUs8.5ue', 'testuser', 'test@example.com', '13800138000', NULL, 'unknown', NULL, 1, 'user', '2025-09-13 19:06:20', '2025-09-13 19:06:20', 1);

-- ----------------------------
-- Table structure for user_account
-- ----------------------------
DROP TABLE IF EXISTS `user_account`;
CREATE TABLE `user_account`  (
  `account_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `frozen_balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结余额',
  `points` int NOT NULL DEFAULT 0 COMMENT '账户积分',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '账户状态：0-冻结，1-正常',
  `pay_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付密码（加密存储）',
  `security_level` tinyint NOT NULL DEFAULT 1 COMMENT '安全等级：1-低，2-中，3-高',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`account_id`) USING BTREE,
  UNIQUE INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_account_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户账户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_account
-- ----------------------------
INSERT INTO `user_account` VALUES (1, 1, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-06-06 17:48:02');
INSERT INTO `user_account` VALUES (2, 2, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-06-06 17:48:02');
INSERT INTO `user_account` VALUES (3, 3, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-06-06 17:48:02');
INSERT INTO `user_account` VALUES (4, 4, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-06-06 17:48:02');
INSERT INTO `user_account` VALUES (5, 8, 18944.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-09-18 16:43:29');
INSERT INTO `user_account` VALUES (6, 9, 4241.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-07-11 15:20:38');
INSERT INTO `user_account` VALUES (7, 10, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-06-06 17:48:02');
INSERT INTO `user_account` VALUES (8, 11, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-06-06 17:48:02');
INSERT INTO `user_account` VALUES (9, 48, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-06-06 17:48:02');
INSERT INTO `user_account` VALUES (10, 49, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-06-06 17:48:02');
INSERT INTO `user_account` VALUES (11, 50, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-06-06 17:48:02');
INSERT INTO `user_account` VALUES (12, 57, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-06 17:48:02', '2025-06-06 17:48:02');
INSERT INTO `user_account` VALUES (16, 58, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-06-09 22:21:35', '2025-06-09 22:21:35');
INSERT INTO `user_account` VALUES (17, 59, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-07-10 14:31:30', '2025-07-10 14:31:30');
INSERT INTO `user_account` VALUES (18, 60, 0.00, 0.00, 0, 1, NULL, 1, NULL, NULL, '2025-09-13 19:06:20', '2025-09-13 19:06:20');

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
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
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户地址表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_address
-- ----------------------------
INSERT INTO `user_address` VALUES (1, 2, '张三', '13800000001', '广东省', '深圳市', '南山区', '科技园南路XX号', '518000', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `user_address` VALUES (2, 2, '张三', '13800000001', '广东省', '广州市', '天河区', '天河路XX号', '510000', 0, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `user_address` VALUES (3, 3, '李四', '13800000002', '北京市', '北京市', '朝阳区', '朝阳路XX号', '100000', 1, '2025-03-05 21:58:47', '2025-03-05 21:58:47');
INSERT INTO `user_address` VALUES (4, 8, '青柠檬', '13444588765', '广东省', '深圳市', '罗湖区', '三墩157号', '310000', 0, '2025-03-07 22:27:31', '2025-06-06 13:38:51');
INSERT INTO `user_address` VALUES (5, 8, '李狗蛋', '15777455256', '北京市', '北京市', '朝阳区', '胜利街道1887号', '', 1, '2025-03-09 13:11:01', '2025-06-13 20:18:47');
INSERT INTO `user_address` VALUES (6, 9, '彭伟株', '13555622466', '浙江省', '杭州市', '西湖区', '三墩路1555号', '', 0, '2025-03-16 21:28:59', '2025-04-27 22:44:31');
INSERT INTO `user_address` VALUES (7, 9, '王五', '13444522365', '上海市', '上海市', '静安区', '胜利达到157号', '', 1, '2025-03-16 21:30:38', '2025-05-05 17:17:48');
INSERT INTO `user_address` VALUES (8, 8, '王大锤', '13666522545', '广东省', '深圳市', '福田区', '紫阳大道1555号', '', 0, '2025-03-17 15:46:09', '2025-06-06 13:38:52');
INSERT INTO `user_address` VALUES (9, 9, '李四', '13666522356', '上海', '上海市', '徐汇区', '地中海10086号', '', 0, '2025-03-17 18:27:14', '2025-04-28 13:21:46');
INSERT INTO `user_address` VALUES (14, 10, 'pwz', '13555656565', '浙江省', '杭州市', '江干区', '钱江新城5幢6单元', NULL, 0, '2025-03-18 19:27:43', '2025-03-18 19:27:43');
INSERT INTO `user_address` VALUES (15, 10, 'qqqq', '13666522565', '浙江省', '杭州市', '江干区', 'asdadasdasd', NULL, 0, '2025-03-18 19:52:03', '2025-03-18 19:52:03');
INSERT INTO `user_address` VALUES (16, 9, '张三', '13666544577', '江苏省', '苏州市', '吴江区', '太湖公园18888号', '', 0, '2025-04-14 13:36:17', '2025-05-05 17:17:47');
INSERT INTO `user_address` VALUES (17, 59, 'qqqq', '13444555666', '山西省', '太原市', '尖草坪区', '西湖区15869号', '', 0, '2025-07-10 14:35:33', '2025-07-10 14:48:31');
INSERT INTO `user_address` VALUES (18, 59, 'pwz', '15222566568', '安徽省', '合肥市', '包河区', '西湖区10086号', '', 1, '2025-07-10 14:38:22', '2025-07-10 14:48:32');

-- ----------------------------
-- Table structure for user_coupon
-- ----------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `coupon_id` bigint UNSIGNED NOT NULL COMMENT '优惠券ID',
  `batch_id` int UNSIGNED NULL DEFAULT NULL COMMENT '批次ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UNUSED' COMMENT '状态：UNUSED-未使用, USED-已使用, EXPIRED-已过期, FROZEN-已冻结',
  `use_time` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `order_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联订单ID',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户优惠券表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_coupon
-- ----------------------------
INSERT INTO `user_coupon` VALUES (1, 1, 1, 1, 'EXPIRED', NULL, NULL, '2025-04-23 19:56:18', '2025-05-23 19:56:18', '2025-04-23 19:56:18', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (2, 1, 2, 2, 'USED', '2025-04-23 20:28:55', 173, '2025-04-23 19:56:18', '2025-05-23 19:56:18', '2025-04-23 19:56:18', '2025-04-23 20:28:55');
INSERT INTO `user_coupon` VALUES (3, 2, 1, 1, 'USED', '2025-04-23 21:02:55', 174, '2025-04-23 19:56:18', '2025-05-23 19:56:18', '2025-04-23 19:56:18', '2025-04-23 21:02:55');
INSERT INTO `user_coupon` VALUES (4, 2, 3, 3, 'EXPIRED', NULL, NULL, '2025-04-23 19:56:18', '2025-05-23 19:56:18', '2025-04-23 19:56:18', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (5, 3, 2, 2, 'EXPIRED', NULL, NULL, '2025-04-23 19:56:18', '2025-05-23 19:56:18', '2025-04-23 19:56:18', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (6, 3, 4, 4, 'EXPIRED', NULL, NULL, '2025-04-23 19:56:18', '2025-05-23 19:56:18', '2025-04-23 19:56:18', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (7, 4, 3, 3, 'EXPIRED', NULL, NULL, '2025-04-23 19:56:18', '2025-05-23 19:56:18', '2025-04-23 19:56:18', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (8, 9, 5, 5, 'EXPIRED', NULL, NULL, '2025-04-23 19:56:18', '2025-05-23 19:56:18', '2025-04-23 19:56:18', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (9, 9, 1, 1, 'USED', '2025-04-24 13:07:53', 178, '2025-04-23 19:56:18', '2025-05-23 19:56:18', '2025-04-23 19:56:18', '2025-04-24 13:07:53');
INSERT INTO `user_coupon` VALUES (10, 9, 4, NULL, 'USED', '2025-04-23 21:36:07', 177, '2025-04-23 19:56:18', '2025-05-23 19:56:18', '2025-04-23 19:56:18', '2025-04-23 21:36:07');
INSERT INTO `user_coupon` VALUES (11, 9, 6, NULL, 'EXPIRED', NULL, NULL, '2025-04-23 20:22:34', '2025-05-13 19:54:13', '2025-04-23 20:22:34', '2025-05-14 09:59:05');
INSERT INTO `user_coupon` VALUES (12, 9, 7, NULL, 'USED', '2025-04-24 13:28:54', 180, '2025-04-23 20:22:36', '2025-05-18 19:54:13', '2025-04-23 20:22:36', '2025-04-24 13:28:54');
INSERT INTO `user_coupon` VALUES (13, 9, 8, NULL, 'EXPIRED', NULL, NULL, '2025-04-23 20:22:37', '2025-05-08 19:54:13', '2025-04-23 20:22:37', '2025-05-09 09:45:32');
INSERT INTO `user_coupon` VALUES (14, 9, 2, NULL, 'USED', '2025-04-24 13:21:51', 179, '2025-04-23 20:33:20', '2025-05-23 19:54:13', '2025-04-23 20:33:20', '2025-04-24 13:21:51');
INSERT INTO `user_coupon` VALUES (15, 9, 3, NULL, 'EXPIRED', NULL, NULL, '2025-04-23 20:33:22', '2025-05-23 19:54:13', '2025-04-23 20:33:22', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (16, 9, 10, NULL, 'EXPIRED', NULL, NULL, '2025-04-24 13:45:58', '2025-05-23 19:54:13', '2025-04-24 13:45:58', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (17, 9, 9, NULL, 'EXPIRED', NULL, NULL, '2025-04-24 13:46:01', '2025-05-13 19:54:13', '2025-04-24 13:46:01', '2025-05-14 09:59:05');
INSERT INTO `user_coupon` VALUES (18, 9, 1, NULL, 'USED', '2025-07-11 15:09:19', 261, '2025-04-24 14:12:15', '2025-09-23 00:00:00', '2025-04-24 14:12:15', '2025-04-24 14:12:15');
INSERT INTO `user_coupon` VALUES (19, 8, 1, 1, 'USED', '2025-06-06 09:11:13', 235, '2025-04-29 10:08:42', '2025-09-23 00:00:00', '2025-04-29 10:08:42', '2025-04-29 10:08:42');
INSERT INTO `user_coupon` VALUES (20, 9, 1, 1, 'USED', '2025-07-11 15:13:56', 262, '2025-05-05 17:28:11', '2025-09-23 00:00:00', '2025-05-05 17:28:11', '2025-05-05 17:28:11');
INSERT INTO `user_coupon` VALUES (21, 9, 1, 1, 'USED', '2025-07-11 15:16:41', 263, '2025-05-05 17:43:08', '2025-09-23 00:00:00', '2025-05-05 17:43:08', '2025-05-05 17:43:08');
INSERT INTO `user_coupon` VALUES (22, 8, 1, 1, 'USED', '2025-06-06 09:21:14', 236, '2025-05-05 21:26:53', '2025-09-23 00:00:00', '2025-05-05 21:26:53', '2025-05-05 21:26:53');
INSERT INTO `user_coupon` VALUES (23, 8, 2, 2, 'EXPIRED', NULL, NULL, '2025-05-05 21:26:55', '2025-05-23 19:54:13', '2025-05-05 21:26:55', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (24, 8, 3, 2, 'EXPIRED', NULL, NULL, '2025-05-14 21:54:08', '2025-05-23 19:54:13', '2025-05-14 21:54:08', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (25, 8, 10, 5, 'EXPIRED', NULL, NULL, '2025-05-14 21:54:13', '2025-05-23 19:54:13', '2025-05-14 21:54:13', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (26, 8, 4, 2, 'EXPIRED', NULL, NULL, '2025-05-14 21:54:15', '2025-05-23 19:54:13', '2025-05-14 21:54:15', '2025-05-26 13:37:51');
INSERT INTO `user_coupon` VALUES (27, 8, 1, 1, 'USED', '2025-06-06 13:35:54', 237, '2025-05-26 17:45:23', '2025-09-23 00:00:00', '2025-05-26 17:45:23', '2025-05-26 17:45:23');
INSERT INTO `user_coupon` VALUES (28, 8, 1, 1, 'UNUSED', NULL, NULL, '2025-06-11 22:53:28', '2025-09-23 00:00:00', '2025-06-11 22:53:28', '2025-06-11 22:53:28');

-- ----------------------------
-- Table structure for user_message
-- ----------------------------
DROP TABLE IF EXISTS `user_message`;
CREATE TABLE `user_message`  (
  `message_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型：ORDER-订单消息，SYSTEM-系统消息，REMIND-提醒消息',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `extra` json NULL COMMENT '额外信息，如关联的订单ID等',
  `is_admin_message` tinyint(1) NULL DEFAULT 0 COMMENT '是否是管理员消息 0-普通用户消息 1-管理员消息',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_is_admin_message`(`is_admin_message` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_message
-- ----------------------------
INSERT INTO `user_message` VALUES ('076764cb-c20c-432a-a4d9-6b3eecb58695', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17443783487882645 ', 0, '2025-04-12 20:56:35', NULL, 1, '{\"orderNo\": \"ORDER17443783487882645\"}', 1);
INSERT INTO `user_message` VALUES ('0893579c-1adf-48a2-9869-1fab3bec5888', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17443350818553567 ', 0, '2025-04-11 09:31:39', NULL, 1, '{\"orderNo\": \"ORDER17443350818553567\"}', 1);
INSERT INTO `user_message` VALUES ('0b86ca38-d555-4c7e-b205-c396fb4d08e9', 9, '2', '提醒发货已收到', '您对订单 ORDER17443350818553567 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-11 09:31:39', '2025-04-13 22:13:38', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('0df21ec90d5840a692abebe8e0ae2b95', 8, 'REMIND', '您的催发货申请已收到', '您对订单 OD17491728740800729c7 的催发货申请已收到，我们将尽快为您发货。', 0, '2025-06-06 09:39:40', NULL, 1, '{\"orderId\": 236}', 0);
INSERT INTO `user_message` VALUES ('106cc3feef804a23a476c5077178b992', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-06-05 14:39:18', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('1264b473668947978ce9d32d30fd02f7', 9, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-05-14 22:59:22', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('180e98adb7594777a2bf93650f5b0cb7', 9, 'ORDER', '订单状态已更新 - 35be2d08', '您的订单 OD1752064333335be2d08 状态已从 [shipped] 更新为 [completed]。', 0, '2025-07-09 20:32:56', NULL, 1, '{\"orderId\": 256, \"newStatus\": \"completed\", \"oldStatus\": \"shipped\"}', 0);
INSERT INTO `user_message` VALUES ('18c9b5dbc33d40d4928272c79905f64a', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-06-10 09:36:45', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('1bac59aaf7b44391a558399b696fa031', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-07-11 13:17:36', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('2009380a-3af6-4e3e-b3b6-c4573e8844bd', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17446351380987716 ', 0, '2025-04-14 21:33:56', NULL, 1, '{\"orderNo\": \"ORDER17446351380987716\"}', 1);
INSERT INTO `user_message` VALUES ('244f8b205cc84c1596d7ee119c964f9a', 8, 'SHIPPING_REMINDER', '用户催发货提醒 - 55f094d7', '用户对订单 OD1749726408755f094d7 进行了催发货。\n下单时间：2025-06-12 19:06:49\n用户留言：对订单 ORDEROD1749726408755f094d7 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-06-17 21:36:15', NULL, 1, '{\"orderId\": 253}', 0);
INSERT INTO `user_message` VALUES ('24c0c5e089a543b4a08e3138f22b2edb', 8, 'REMIND', '您的催发货申请已收到', '您对订单 OD1749172272479c582e6 的催发货申请已收到，我们将尽快为您发货。', 0, '2025-06-06 09:14:04', NULL, 1, '{\"orderId\": 235}', 0);
INSERT INTO `user_message` VALUES ('285ca4f9d14447fcb3679160c2b990c1', 59, 'SHIPPING_REMINDER', '用户催发货提醒 - 2428c5fe', '用户对订单 OD175213011582428c5fe 进行了催发货。\n下单时间：2025-07-10 14:48:36\n用户留言：对订单 ORDEROD175213011582428c5fe 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-07-10 14:49:23', NULL, 1, '{\"orderId\": 259}', 0);
INSERT INTO `user_message` VALUES ('28f76ca7-b311-487f-9c07-3e783d731c0a', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17433427340697529 ', 0, '2025-03-30 22:05:38', NULL, 1, '{\"orderNo\": \"ORDER17433427340697529\"}', 1);
INSERT INTO `user_message` VALUES ('2a56b9f9-ab90-40e0-86f7-2f189f2bd0a6', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17450703688316173 ', 0, '2025-04-19 22:01:01', NULL, 1, '{\"orderNo\": \"ORDER17450703688316173\"}', 1);
INSERT INTO `user_message` VALUES ('2bac9a2ec46c4cbb9e9d6626c945cb9e', 9, 'REMIND', '您的催发货申请已收到', '您对订单 OD175221820097485ae4b 的催发货申请已收到，我们将尽快为您发货。', 0, '2025-07-11 15:17:56', NULL, 1, '{\"orderId\": 263}', 0);
INSERT INTO `user_message` VALUES ('2cc2da69-e929-4252-832f-7335beae7b4b', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17448106746512253 ', 0, '2025-04-17 15:40:12', NULL, 1, '{\"orderNo\": \"ORDER17448106746512253\"}', 1);
INSERT INTO `user_message` VALUES ('2f2ade01f8c64a0ba1b8b32a9fe51270', 8, 'COMMENT_REWARD', '评价奖励 +23积分', '感谢您对\"产后修复套装\"的评价！\n\n系统已为您发放以下奖励：\n\n· 基础评价奖励: +5积分\n· 好评奖励: +8积分\n· 基础评价奖励: +10积分\n\n总计: +23积分', 1, '2025-06-06 09:47:15', '2025-06-06 09:56:26', 1, '{\"rewards\": [{\"rewardName\": \"基础评价奖励\", \"rewardType\": \"points\", \"rewardValue\": 5, \"rewardDescription\": \"完成订单评价获得基础积分\"}, {\"rewardName\": \"好评奖励\", \"rewardType\": \"points\", \"rewardValue\": 8, \"rewardDescription\": \"给出4-5星好评\"}, {\"rewardName\": \"基础评价奖励\", \"rewardType\": \"points\", \"rewardValue\": 10, \"rewardDescription\": \"完成商品评价获得奖励\"}], \"commentId\": 212, \"productId\": 60, \"totalReward\": 23}', 0);
INSERT INTO `user_message` VALUES ('301940d2-f063-4d1a-b9ec-a9b55aa86b27', 9, '4', '签到成功', '恭喜您完成今日签到，获得 21 积分。您已连续签到 2 天，继续保持！', 0, '2025-04-22 22:46:38', NULL, 1, '{\"days\": 2, \"points\": 21, \"source\": \"signin\"}', 0);
INSERT INTO `user_message` VALUES ('347757ab-b9d0-4feb-b4b7-aca42f2aeba9', 9, '2', '提醒发货已收到', '您对订单 ORDER17443783487882645 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-12 20:56:35', '2025-04-13 22:13:38', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('3536928817a34594b514633c2b933597', 8, 'SHIPPING_REMINDER', '用户催发货提醒 - 8658bee0', '用户对订单 OD174728514628658bee0 进行了催发货。\n下单时间：2025-05-15 12:59:06\n用户留言：对订单 ORDEROD174728514628658bee0 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-05-15 13:29:57', NULL, 1, '{\"orderId\": 221}', 0);
INSERT INTO `user_message` VALUES ('36fe9c41-399d-4428-b0f4-1be46b3a1c88', 9, '2', '提醒发货已收到', '您对订单 ORDER17433100841148442 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-03-30 12:55:56', '2025-04-13 22:13:38', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('37a7f509-4119-4d87-8a9f-291e2ffdd3ff', 9, '2', '提醒发货已收到', '您对订单 ORDER17433100841148442 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-03-30 12:56:40', '2025-04-13 22:13:38', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('38d40bf0-db01-444d-a4bc-5b6f243b5726', 9, '2', '提醒发货已收到', '您对订单 ORDER17450453576811598 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-04-19 21:34:31', NULL, 1, NULL, 0);
INSERT INTO `user_message` VALUES ('3e741fa093524a43b371f4b15c6fbc4f', 8, 'ORDER', '订单状态已更新 - 78845ae7', '您的订单 OD1752210905878845ae7 状态已从 [pending_shipment] 更新为 [shipped]。', 0, '2025-07-11 13:16:11', NULL, 1, '{\"orderId\": 260, \"newStatus\": \"shipped\", \"oldStatus\": \"pending_shipment\"}', 0);
INSERT INTO `user_message` VALUES ('40f5dc82-1e5d-471e-8cee-518bf6295032', 9, '2', '提醒发货已收到', '您对订单 ORDER17433435585039867 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-03-31 19:42:41', '2025-04-13 22:13:38', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('46e0f407dc084b47a0280647e59a4fc4', 8, 'CHECKIN', '恭喜您，已连续签到2天', '您今日签到获得了20积分。您已连续签到2天，继续保持可获得更多奖励！', 0, '2025-05-27 19:19:05', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 2}', 0);
INSERT INTO `user_message` VALUES ('47884c884720464ea435c4ef430a8f54', 59, 'REMIND', '您的催发货申请已收到', '您对订单 OD175213011582428c5fe 的催发货申请已收到，我们将尽快为您发货。', 0, '2025-07-10 14:49:23', NULL, 1, '{\"orderId\": 259}', 0);
INSERT INTO `user_message` VALUES ('4c277e0ef68645f982b2e041c60cc895', 8, 'CHECKIN', '恭喜您，已连续签到2天', '您今日签到获得了20积分。您已连续签到2天，继续保持可获得更多奖励！', 0, '2025-06-18 15:17:15', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 2}', 0);
INSERT INTO `user_message` VALUES ('4cf9d5c822f74aaf9ac6e8fc34994703', 8, 'CHECKIN', '恭喜您，已连续签到2天', '您今日签到获得了20积分。您已连续签到2天，继续保持可获得更多奖励！', 0, '2025-06-06 09:05:47', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 2}', 0);
INSERT INTO `user_message` VALUES ('4e5657c915d143ec851aa8b5597dea6a', 8, 'REMIND', '您的催发货申请已收到', '您对订单 OD1749726408755f094d7 的催发货申请已收到，我们将尽快为您发货。', 0, '2025-06-17 21:36:15', NULL, 1, '{\"orderId\": 253}', 0);
INSERT INTO `user_message` VALUES ('4ee417ac0c7549819f8b0fdde370fd2a', 8, 'CHECKIN', '恭喜您，已连续签到4天', '您今日签到获得了25积分。您已连续签到4天，继续保持可获得更多奖励！', 0, '2025-05-23 09:33:18', NULL, 1, '{\"earnedPoints\": 25, \"continuousDays\": 4}', 0);
INSERT INTO `user_message` VALUES ('50275eba-ad35-4b17-a988-dd0d3f798c11', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17446395616614757 ', 0, '2025-04-14 22:06:06', NULL, 1, '{\"orderNo\": \"ORDER17446395616614757\"}', 1);
INSERT INTO `user_message` VALUES ('5038f8d1-4905-4905-9afa-4cd895fdf61d', 9, '4', '恭喜获得积分奖励', '您的订单 ORDER17450503412792999 已完成，获得 26 积分奖励（订单金额的10%）。', 1, '2025-04-19 17:18:41', '2025-04-19 17:27:57', 1, '{\"points\": 26, \"source\": \"order\", \"orderNo\": \"ORDER17450503412792999\", \"actualAmount\": \"268.00\"}', 0);
INSERT INTO `user_message` VALUES ('513e5805229348d9bcbfc010959a1d47', 8, 'ORDER', '订单状态已更新 - 79c582e6', '您的订单 OD1749172272479c582e6 状态已从 [pending_shipment] 更新为 [shipped]。', 0, '2025-06-06 13:32:09', NULL, 1, '{\"orderId\": 235, \"newStatus\": \"shipped\", \"oldStatus\": \"pending_shipment\"}', 0);
INSERT INTO `user_message` VALUES ('51f423a9-896e-4d85-904f-8609e21430fa', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17446351380987716 ', 0, '2025-04-14 20:57:00', NULL, 1, '{\"orderNo\": \"ORDER17446351380987716\"}', 1);
INSERT INTO `user_message` VALUES ('5275d42903bd44afa2eb909a9b76bab3', 8, 'REMIND', '您的催发货申请已收到', '您对订单 OD1749726408755f094d7 的催发货申请已收到，我们将尽快为您发货。', 0, '2025-06-17 21:36:59', NULL, 1, '{\"orderId\": 253}', 0);
INSERT INTO `user_message` VALUES ('539e5914-cfe0-4d76-b06a-8764dd3a8243', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17456516991175745 ', 0, '2025-04-26 15:15:17', NULL, 1, '{\"orderNo\": \"ORDER17456516991175745\"}', 0);
INSERT INTO `user_message` VALUES ('581b853686354322a927d1b19698d2b9', 8, 'ORDER', '订单状态已更新 - 79daa9d8', '您的订单 OD1747320522379daa9d8 状态已从 [pending_shipment] 更新为 [shipped]。', 0, '2025-05-20 22:54:01', NULL, 1, '{\"orderId\": 222, \"newStatus\": \"shipped\", \"oldStatus\": \"pending_shipment\"}', 0);
INSERT INTO `user_message` VALUES ('5a5f3f85c91047c79932808ca5f0d9ba', 8, 'ORDER', '订单状态已更新 - 21a79c8d', '您的订单 OD1749634872421a79c8d 状态已从 [cancelled] 更新为 [deleted]。', 0, '2025-06-13 22:57:31', NULL, 1, '{\"orderId\": 251, \"newStatus\": \"deleted\", \"oldStatus\": \"cancelled\"}', 0);
INSERT INTO `user_message` VALUES ('5b3e4482e29b45adbd0e408053fbe084', 8, 'ORDER', '订单状态已更新 - 52415971', '您的订单 OD1749188154352415971 状态已从 [pending_shipment] 更新为 [shipped]。', 0, '2025-06-06 13:36:58', NULL, 1, '{\"orderId\": 237, \"newStatus\": \"shipped\", \"oldStatus\": \"pending_shipment\"}', 0);
INSERT INTO `user_message` VALUES ('5bcb1aa492ed45979a775a62baf222c0', 9, 'CHECKIN', '恭喜您，已连续签到2天', '您今日签到获得了20积分。您已连续签到2天，继续保持可获得更多奖励！', 0, '2025-05-15 12:47:36', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 2}', 0);
INSERT INTO `user_message` VALUES ('5c1cb2f5d7284f11803772cce418ef59', 8, 'CHECKIN', '恭喜您，已连续签到8天', '您今日签到获得了50积分。您已连续签到8天，继续保持可获得更多奖励！', 0, '2025-05-17 18:40:12', NULL, 1, '{\"earnedPoints\": 50, \"continuousDays\": 8}', 0);
INSERT INTO `user_message` VALUES ('5d4643f46e24422c9e717f1b67f91f45', 8, 'ORDER', '订单状态已更新 - 78845ae7', '您的订单 OD1752210905878845ae7 状态已从 [shipped] 更新为 [completed]。', 0, '2025-07-11 13:16:38', NULL, 1, '{\"orderId\": 260, \"newStatus\": \"completed\", \"oldStatus\": \"shipped\"}', 0);
INSERT INTO `user_message` VALUES ('5ea4aaa03fa1498dab78950718604001', 59, 'ORDER', '订单状态已更新 - 2428c5fe', '您的订单 OD175213011582428c5fe 状态已从 [pending_shipment] 更新为 [shipped]。', 0, '2025-07-10 14:49:41', NULL, 1, '{\"orderId\": 259, \"newStatus\": \"shipped\", \"oldStatus\": \"pending_shipment\"}', 0);
INSERT INTO `user_message` VALUES ('62ac6d55be49402facb30090b6c6bfd3', 8, 'REMIND', '您的催发货申请已收到', '您对订单 OD1752210905878845ae7 的催发货申请已收到，我们将尽快为您发货。', 0, '2025-07-11 13:15:49', NULL, 1, '{\"orderId\": 260}', 0);
INSERT INTO `user_message` VALUES ('634e308ab65b4070ae5b5a6b2ca26f54', 8, 'CHECKIN', '恭喜您，已连续签到4天', '您今日签到获得了25积分。您已连续签到4天，继续保持可获得更多奖励！', 0, '2025-06-13 09:39:11', NULL, 1, '{\"earnedPoints\": 25, \"continuousDays\": 4}', 0);
INSERT INTO `user_message` VALUES ('6800803a411b435f89dfa0dbfbba590b', 8, 'ORDER', '订单状态已更新 - 52415971', '您的订单 OD1749188154352415971 状态已从 [shipped] 更新为 [completed]。', 0, '2025-06-06 13:37:09', NULL, 1, '{\"orderId\": 237, \"newStatus\": \"completed\", \"oldStatus\": \"shipped\"}', 0);
INSERT INTO `user_message` VALUES ('6a9a930a-80e1-416f-b9fc-b7da73bdee68', 9, '2', '提醒发货已收到', '您对订单 ORDER17450703688316173 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-04-19 22:01:01', NULL, 1, NULL, 0);
INSERT INTO `user_message` VALUES ('6dc63b2ef303480685df69e834f7dc65', 8, 'CHECKIN', '恭喜您，已连续签到3天', '您今日签到获得了20积分。您已连续签到3天，继续保持可获得更多奖励！', 0, '2025-05-28 19:11:18', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 3}', 0);
INSERT INTO `user_message` VALUES ('71cbe1fd-542f-41d1-8007-e0973feb96e5', 9, '4', '签到成功', '恭喜您完成今日签到，获得 21 积分。您已连续签到 2 天，继续保持！', 0, '2025-04-20 09:31:58', NULL, 1, '{\"days\": 2, \"points\": 21, \"source\": \"signin\"}', 0);
INSERT INTO `user_message` VALUES ('749e8ba1663845be8ed7a34f9cab8080', 8, 'SHIPPING_REMINDER', '用户催发货提醒 - 55f094d7', '用户对订单 OD1749726408755f094d7 进行了催发货。\n下单时间：2025-06-12 19:06:49\n用户留言：对订单 ORDEROD1749726408755f094d7 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-06-17 21:36:59', NULL, 1, '{\"orderId\": 253}', 0);
INSERT INTO `user_message` VALUES ('7557ac560e98420aaf831d7282d0ca66', 8, 'ORDER', '订单状态已更新 - 33a41073', '您的订单 OD1749631472233a41073 状态已从 [cancelled] 更新为 [deleted]。', 0, '2025-06-11 16:52:14', NULL, 1, '{\"orderId\": 243, \"newStatus\": \"deleted\", \"oldStatus\": \"cancelled\"}', 0);
INSERT INTO `user_message` VALUES ('784652968a954470b1e9567340d02f2f', 8, 'ORDER', '订单状态已更新 - 33a41073', '您的订单 OD1749631472233a41073 状态已从 [cancelled] 更新为 [deleted]。', 0, '2025-06-11 16:52:25', NULL, 1, '{\"orderId\": 243, \"newStatus\": \"deleted\", \"oldStatus\": \"cancelled\"}', 0);
INSERT INTO `user_message` VALUES ('78698d3a-b924-435a-89b6-3f8011120369', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17433100841148442 ', 0, '2025-03-30 12:55:56', NULL, 1, '{\"orderNo\": \"ORDER17433100841148442\"}', 1);
INSERT INTO `user_message` VALUES ('7a6933bb-8b0a-4d30-8d62-27b5de1df6ac', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17433435585039867 ', 0, '2025-03-31 19:42:41', NULL, 1, '{\"orderNo\": \"ORDER17433435585039867\"}', 1);
INSERT INTO `user_message` VALUES ('7aa9ba99-c56a-4416-9436-05639d904ef1', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17443551411937606 ', 0, '2025-04-11 15:23:45', NULL, 1, '{\"orderNo\": \"ORDER17443551411937606\"}', 1);
INSERT INTO `user_message` VALUES ('7dafabf9-609c-4156-992e-1a15caf39d0e', 9, '4', '恭喜获得积分奖励', '您的订单 ORDER17453886695259124 已完成，获得 9 积分奖励（订单金额的10%）。', 0, '2025-04-23 20:29:42', NULL, 1, '{\"points\": 9, \"source\": \"order\", \"orderNo\": \"ORDER17453886695259124\", \"actualAmount\": \"99.90\"}', 0);
INSERT INTO `user_message` VALUES ('7f920d3dabcf4fddb3095a7d81543358', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-06-27 11:51:07', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('7fc97faa-e853-4136-ad9d-eef3abb47594', 9, '4', '签到成功', '恭喜您完成今日签到，获得 21 积分。您已连续签到 2 天，继续保持！', 0, '2025-04-21 13:38:39', NULL, 1, '{\"days\": 2, \"points\": 21, \"source\": \"signin\"}', 0);
INSERT INTO `user_message` VALUES ('82b0230bcd2f4988a5db38d1ddbdd514', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-06-03 16:33:55', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('83b2ca8c5fad44349ff0bea429d745b1', 8, 'CHECKIN', '恭喜您，已连续签到9天', '您今日签到获得了60积分。您已连续签到9天，继续保持可获得更多奖励！', 0, '2025-05-18 22:43:55', NULL, 1, '{\"earnedPoints\": 60, \"continuousDays\": 9}', 0);
INSERT INTO `user_message` VALUES ('861a2dbdedbd465298a566cdc757ae5a', 9, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-05-14 22:59:35', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('8bcd415f1c964a76bf00658ae0b9c8a7', 8, 'CHECKIN', '恭喜您，已连续签到7天', '您今日签到获得了40积分。您已连续签到7天，继续保持可获得更多奖励！', 0, '2025-05-16 11:34:30', NULL, 1, '{\"earnedPoints\": 40, \"continuousDays\": 7}', 0);
INSERT INTO `user_message` VALUES ('8d2dd994-8ce5-4bac-a4d7-d6d9b921d93b', 9, '2', '提醒发货已收到', '您对订单 ORDER17446395616614757 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-14 22:06:06', '2025-04-19 17:27:57', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('8e1d1562-ed9d-4648-829f-d4481d8ff6e8', 9, '2', '提醒发货已收到', '您对订单 ORDER17446351380987716 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-14 21:33:56', '2025-04-19 17:27:57', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('8e8edbac88ae44f9bae9e9197139bfcd', 8, 'REMIND', '您的催发货申请已收到', '您对订单 OD1746944159254ef56e4 的催发货申请已收到，我们将尽快为您发货。', 0, '2025-05-13 21:28:49', NULL, 1, '{\"orderId\": 217}', 0);
INSERT INTO `user_message` VALUES ('8f5e7b9969814624a1736b59e3143166', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-09-18 16:43:12', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('8f735558ca5844f3a956bba44a87f68b', 59, 'ORDER', '订单状态已更新 - 2428c5fe', '您的订单 OD175213011582428c5fe 状态已从 [shipped] 更新为 [completed]。', 0, '2025-07-10 16:57:54', NULL, 1, '{\"orderId\": 259, \"newStatus\": \"completed\", \"oldStatus\": \"shipped\"}', 0);
INSERT INTO `user_message` VALUES ('8ff12890-057f-4d54-9bef-2a01ac70650c', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17450453576811598 ', 0, '2025-04-19 21:34:31', NULL, 1, '{\"orderNo\": \"ORDER17450453576811598\"}', 1);
INSERT INTO `user_message` VALUES ('9175cbcf-ba23-420a-bd38-3819ffb05961', 9, '4', '恭喜获得积分奖励', '您的订单 ORDER17453297779016486 已完成，获得 156 积分奖励（订单金额的10%）。', 0, '2025-04-23 14:11:51', NULL, 1, '{\"points\": 156, \"source\": \"order\", \"orderNo\": \"ORDER17453297779016486\", \"actualAmount\": \"1569.60\"}', 0);
INSERT INTO `user_message` VALUES ('91992e4df4d14d4e9d836cb3393a721e', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-05-26 15:40:27', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('92e96dc37234406b89a5583b08bdf67d', 8, 'CHECKIN', '恭喜您，已连续签到2天', '您今日签到获得了20积分。您已连续签到2天，继续保持可获得更多奖励！', 0, '2025-06-11 12:46:12', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 2}', 0);
INSERT INTO `user_message` VALUES ('9322a87a32e347cfb5b88915f76d4a6c', 8, 'ORDER', '订单状态已更新 - 5788d6c7', '您的订单 OD174844173195788d6c7 状态已从 [pending_shipment] 更新为 [shipped]。', 0, '2025-05-28 22:16:57', NULL, 1, '{\"orderId\": 231, \"newStatus\": \"shipped\", \"oldStatus\": \"pending_shipment\"}', 0);
INSERT INTO `user_message` VALUES ('93cdc8fa99914fc5aae106ff6952571b', 8, 'CHECKIN', '恭喜您，已连续签到2天', '您今日签到获得了20积分。您已连续签到2天，继续保持可获得更多奖励！', 0, '2025-07-09 12:07:53', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 2}', 0);
INSERT INTO `user_message` VALUES ('93e91b6f-65ca-4a1f-8f57-1992757ea9c8', 9, '4', '恭喜获得积分奖励', '您的订单 ORDER17454725349573471 已完成，获得 114 积分奖励（订单金额的10%）。', 0, '2025-04-24 16:43:44', NULL, 1, '{\"points\": 114, \"source\": \"order\", \"orderNo\": \"ORDER17454725349573471\", \"actualAmount\": \"1149.60\"}', 0);
INSERT INTO `user_message` VALUES ('99564d99bf944177ba28c7dd80feaa89', 9, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-07-09 15:38:25', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('9a8c869fbd774427ae34f5733a513c4d', 8, 'CHECKIN', '恭喜您，已连续签到2天', '您今日签到获得了20积分。您已连续签到2天，继续保持可获得更多奖励！', 0, '2025-09-19 12:11:15', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 2}', 0);
INSERT INTO `user_message` VALUES ('9b7b1fad-7c5f-4e51-a32f-542d5979f68d', 9, '2', '提醒发货已收到', '您对订单 ORDER17433100841148442 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-03-30 12:56:42', '2025-04-13 22:13:38', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('9c38e8db8c234cdb900d468e132027ea', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-09-13 19:05:36', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('9d8332ce-e84c-4155-9231-f9fc21f690e4', 9, '4', '恭喜获得积分奖励', '您的订单 ORDER17451407073845313 已完成，获得 99 积分奖励（订单金额的10%）。', 0, '2025-04-20 17:41:06', NULL, 1, '{\"points\": 99, \"source\": \"order\", \"orderNo\": \"ORDER17451407073845313\", \"actualAmount\": \"999.40\"}', 0);
INSERT INTO `user_message` VALUES ('a50da281-dca2-4574-b315-a01dd83bc9ae', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17433100841148442 ', 0, '2025-03-30 12:56:40', NULL, 1, '{\"orderNo\": \"ORDER17433100841148442\"}', 1);
INSERT INTO `user_message` VALUES ('a5ad2b33-d04e-4f70-8d35-37b00af29315', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17450486780378791 ', 0, '2025-04-19 15:45:25', NULL, 1, '{\"orderNo\": \"ORDER17450486780378791\"}', 1);
INSERT INTO `user_message` VALUES ('a6273d141596485198551f6495060df6', 8, 'ORDER', '订单状态已更新 - 33a41073', '您的订单 OD1749631472233a41073 状态已从 [cancelled] 更新为 [deleted]。', 0, '2025-06-11 16:52:20', NULL, 1, '{\"orderId\": 243, \"newStatus\": \"deleted\", \"oldStatus\": \"cancelled\"}', 0);
INSERT INTO `user_message` VALUES ('a7cf8d7867554e5bbb2cc0c8c07aae11', 9, 'CHECKIN', '恭喜您，已连续签到2天', '您今日签到获得了20积分。您已连续签到2天，继续保持可获得更多奖励！', 0, '2025-07-10 10:25:23', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 2}', 0);
INSERT INTO `user_message` VALUES ('aad59a9e3ff34af1abd0b44fae4d66b4', 8, 'ORDER', '订单状态已更新 - 960ab1fc', '您的订单 OD17479041992960ab1fc 状态已从 [pending_shipment] 更新为 [cancelled]。', 0, '2025-05-22 18:09:35', NULL, 1, '{\"orderId\": 227, \"newStatus\": \"cancelled\", \"oldStatus\": \"pending_shipment\"}', 0);
INSERT INTO `user_message` VALUES ('ae58cf0285184354a2dc98c5e711c7dd', 8, 'CHECKIN', '恭喜您，已连续签到6天', '您今日签到获得了25积分。您已连续签到6天，继续保持可获得更多奖励！', 0, '2025-05-15 12:47:59', NULL, 1, '{\"earnedPoints\": 25, \"continuousDays\": 6}', 0);
INSERT INTO `user_message` VALUES ('b076c6528cee45748418e426d995067e', 8, 'CHECKIN', '恭喜您，已连续签到3天', '您今日签到获得了20积分。您已连续签到3天，继续保持可获得更多奖励！', 0, '2025-05-22 14:40:08', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 3}', 0);
INSERT INTO `user_message` VALUES ('b1862330-65a3-4545-ba26-d245c019161b', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17443783487882645 ', 0, '2025-04-11 21:37:24', NULL, 1, '{\"orderNo\": \"ORDER17443783487882645\"}', 1);
INSERT INTO `user_message` VALUES ('b1a0d9e6945645a091d6fe55d6643e47', 9, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-05-23 15:35:45', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('b1b0386a4b8942f19ee94e3708a877d3', 8, 'ORDER', '订单状态已更新 - 5788d6c7', '您的订单 OD174844173195788d6c7 状态已从 [shipped] 更新为 [completed]。', 0, '2025-05-28 22:17:15', NULL, 1, '{\"orderId\": 231, \"newStatus\": \"completed\", \"oldStatus\": \"shipped\"}', 0);
INSERT INTO `user_message` VALUES ('b32c996dd161468699c02ab9b04a2d04', 8, 'ORDER', '订单状态已更新 - 79daa9d8', '您的订单 OD1747320522379daa9d8 状态已从 [shipped] 更新为 [completed]。', 0, '2025-05-22 21:35:26', NULL, 1, '{\"orderId\": 222, \"newStatus\": \"completed\", \"oldStatus\": \"shipped\"}', 0);
INSERT INTO `user_message` VALUES ('b349d79446d6403fb1f3e3ff7ec894b9', 8, 'SHIPPING_REMINDER', '用户催发货提醒 - 79c582e6', '用户对订单 OD1749172272479c582e6 进行了催发货。\n下单时间：2025-06-06 09:11:12\n用户留言：对订单 ORDEROD1749172272479c582e6 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-06-06 09:14:05', NULL, 1, '{\"orderId\": 235}', 0);
INSERT INTO `user_message` VALUES ('b608465b-bf78-4ebe-81d0-1df246812fdc', 9, '2', '提醒发货已收到', '您对订单 ORDER17446103742268284 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-14 14:37:05', '2025-04-19 17:27:57', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('b6f81aa9-5161-420f-a7be-9b6f42c1fd4d', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17449398848975895 ', 0, '2025-04-18 10:13:54', NULL, 1, '{\"orderNo\": \"ORDER17449398848975895\"}', 1);
INSERT INTO `user_message` VALUES ('b8d5115cd1a84d5cb7cd2fbc25377080', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-06-17 18:33:25', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('b928eeda371145ab97e4a8f03d8ef650', 8, 'REMIND', '您的催发货申请已收到', '您对订单 OD1747922100745f8639c 的催发货申请已收到，我们将尽快为您发货。', 0, '2025-05-22 22:01:25', NULL, 1, '{\"orderId\": 228}', 0);
INSERT INTO `user_message` VALUES ('bd8cd7babf75415a9e1284e54d292379', 8, 'ORDER', '订单状态已更新 - 38f9acc0', '您的订单 OD1748085552938f9acc0 状态已从 [pending_shipment] 更新为 [shipped]。', 0, '2025-05-29 23:05:02', NULL, 1, '{\"orderId\": 229, \"newStatus\": \"shipped\", \"oldStatus\": \"pending_shipment\"}', 0);
INSERT INTO `user_message` VALUES ('be56133a-6ed6-4595-871e-ce54067cf7d7', 9, '2', '提醒发货已收到', '您对订单 ORDER17446351380987716 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-14 20:57:00', '2025-04-19 17:27:57', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('c35df59a7ecd4076a715945ded1720cd', 8, 'ORDER', '订单状态已更新 - 800729c7', '您的订单 OD17491728740800729c7 状态已从 [pending_shipment] 更新为 [shipped]。', 0, '2025-06-06 09:39:56', NULL, 1, '{\"orderId\": 236, \"newStatus\": \"shipped\", \"oldStatus\": \"pending_shipment\"}', 0);
INSERT INTO `user_message` VALUES ('c9795922-c02f-4ce2-a557-5a4723ed87fd', 9, '2', '提醒发货已收到', '您对订单 ORDER17448106746512253 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-17 15:37:51', '2025-04-19 17:27:57', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('cae56201-6e0f-49cc-958a-7176aa309049', 9, '2', '提醒发货已收到', '您对订单 ORDER17433427340697529 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-03-30 22:05:37', '2025-04-13 22:13:38', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('cb84d3ae-cec1-47e5-bf5b-f180538800b2', 9, '4', '恭喜获得积分奖励', '您的订单 ORDER17450499740802107 已完成，获得 276 积分奖励（订单金额的10%）。', 1, '2025-04-19 16:10:40', '2025-04-19 17:27:57', 1, '{\"points\": 276, \"source\": \"order\", \"orderNo\": \"ORDER17450499740802107\", \"actualAmount\": \"2769.30\"}', 0);
INSERT INTO `user_message` VALUES ('ccba01e91e0e42a38ece77420bf00d79', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-07-08 10:00:20', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('cd60e84c-e070-494e-89cd-b1fd52c2df06', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17448106746512253 ', 0, '2025-04-17 15:37:51', NULL, 1, '{\"orderNo\": \"ORDER17448106746512253\"}', 1);
INSERT INTO `user_message` VALUES ('cda1d8e3fb72432196ca6d234d8d3265', 8, 'COMMENT_REWARD', '评价奖励 +23积分', '感谢您对\"婴儿音乐床铃\"的评价！\n\n系统已为您发放以下奖励：\n\n· 基础评价奖励: +5积分\n· 好评奖励: +8积分\n· 基础评价奖励: +10积分\n\n总计: +23积分', 0, '2025-05-30 09:34:51', NULL, 1, '{\"rewards\": [{\"rewardName\": \"基础评价奖励\", \"rewardType\": \"points\", \"rewardValue\": 5, \"rewardDescription\": \"完成订单评价获得基础积分\"}, {\"rewardName\": \"好评奖励\", \"rewardType\": \"points\", \"rewardValue\": 8, \"rewardDescription\": \"给出4-5星好评\"}, {\"rewardName\": \"基础评价奖励\", \"rewardType\": \"points\", \"rewardValue\": 10, \"rewardDescription\": \"完成商品评价获得奖励\"}], \"commentId\": 211, \"productId\": 56, \"totalReward\": 23}', 0);
INSERT INTO `user_message` VALUES ('d048ce39-9989-4816-aa50-0395222e5b4d', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17446103742268284 ', 0, '2025-04-14 14:37:05', NULL, 1, '{\"orderNo\": \"ORDER17446103742268284\"}', 1);
INSERT INTO `user_message` VALUES ('d0740494-e065-4800-b23b-b3eebaa53443', 9, '4', '恭喜获得积分奖励', '您的订单 ORDER17450548263311761 已完成，获得 8996 积分奖励（订单金额的10%）。', 0, '2025-04-19 17:30:15', NULL, 1, '{\"points\": 8996, \"source\": \"order\", \"orderNo\": \"ORDER17450548263311761\", \"actualAmount\": \"89960.00\"}', 0);
INSERT INTO `user_message` VALUES ('d3c86b1042da4c17b327d586e298457e', 8, 'COMMENT_REWARD', '评价奖励 +23积分', '感谢您对\"产后修复套装\"的评价！\n\n系统已为您发放以下奖励：\n\n· 基础评价奖励: +5积分\n· 好评奖励: +8积分\n· 基础评价奖励: +10积分\n\n总计: +23积分', 1, '2025-06-06 13:37:46', '2025-06-06 13:38:20', 1, '{\"rewards\": [{\"rewardName\": \"基础评价奖励\", \"rewardType\": \"points\", \"rewardValue\": 5, \"rewardDescription\": \"完成订单评价获得基础积分\"}, {\"rewardName\": \"好评奖励\", \"rewardType\": \"points\", \"rewardValue\": 8, \"rewardDescription\": \"给出4-5星好评\"}, {\"rewardName\": \"基础评价奖励\", \"rewardType\": \"points\", \"rewardValue\": 10, \"rewardDescription\": \"完成商品评价获得奖励\"}], \"commentId\": 213, \"productId\": 60, \"totalReward\": 23}', 0);
INSERT INTO `user_message` VALUES ('d441ff87-1165-4447-b85c-73c447c0a956', 9, '2', '提醒发货已收到', '您对订单 ORDER17456516991175745 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-04-26 15:15:17', NULL, 1, NULL, 0);
INSERT INTO `user_message` VALUES ('d8041bfdf191458bbb57f0e24b05399c', 8, 'SHIPPING_REMINDER', '用户催发货提醒 - 54ef56e4', '用户对订单 OD1746944159254ef56e4 进行了催发货。\n下单时间：2025-05-11 14:15:59\n用户留言：对订单 ORDEROD1746944159254ef56e4 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-05-13 21:28:49', NULL, 1, '{\"orderId\": 217}', 0);
INSERT INTO `user_message` VALUES ('d8a9f9680d3e44a8889721c8fcc1b6b0', 8, 'CHECKIN', '恭喜您，已连续签到3天', '您今日签到获得了20积分。您已连续签到3天，继续保持可获得更多奖励！', 0, '2025-06-12 19:02:22', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 3}', 0);
INSERT INTO `user_message` VALUES ('d9781ba79f584a1aa7759fd905e486c7', 9, 'ORDER', '订单状态已更新 - 35be2d08', '您的订单 OD1752064333335be2d08 状态已从 [pending_shipment] 更新为 [shipped]。', 0, '2025-07-09 20:32:49', NULL, 1, '{\"orderId\": 256, \"newStatus\": \"shipped\", \"oldStatus\": \"pending_shipment\"}', 0);
INSERT INTO `user_message` VALUES ('db2f6017f08a4dce9c66715eb69a7b00', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-05-20 15:49:13', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('dd2923e88004448ca775dd9df19718d8', 8, 'SHIPPING_REMINDER', '用户催发货提醒 - 45f8639c', '用户对订单 OD1747922100745f8639c 进行了催发货。\n下单时间：2025-05-22 21:55:01\n用户留言：对订单 ORDEROD1747922100745f8639c 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-05-22 22:01:25', NULL, 1, '{\"orderId\": 228}', 0);
INSERT INTO `user_message` VALUES ('ddeed8a9-5396-4e13-a8ab-8972af7092d5', 8, '2', '提醒发货已收到', '您对订单 ORDER17443551411937606 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-04-11 15:23:45', NULL, 1, NULL, 0);
INSERT INTO `user_message` VALUES ('de70430be43f43aa922fd3ca8c59e17a', 8, 'REMIND', '您的催发货申请已收到', '您对订单 OD174728514628658bee0 的催发货申请已收到，我们将尽快为您发货。', 0, '2025-05-15 13:29:57', NULL, 1, '{\"orderId\": 221}', 0);
INSERT INTO `user_message` VALUES ('defeb8611a27433c93ff52352aef3669', 9, 'SHIPPING_REMINDER', '用户催发货提醒 - 7485ae4b', '用户对订单 OD175221820097485ae4b 进行了催发货。\n下单时间：2025-07-11 15:16:41\n用户留言：对订单 ORDEROD175221820097485ae4b 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-07-11 15:17:56', NULL, 1, '{\"orderId\": 263}', 0);
INSERT INTO `user_message` VALUES ('e00a55601b52483792d71e4f85adcf7f', 8, 'ORDER', '订单状态已更新 - 800729c7', '您的订单 OD17491728740800729c7 状态已从 [shipped] 更新为 [completed]。', 0, '2025-06-06 09:46:48', NULL, 1, '{\"orderId\": 236, \"newStatus\": \"completed\", \"oldStatus\": \"shipped\"}', 0);
INSERT INTO `user_message` VALUES ('e274e6ef-41f5-4f7c-ac4e-ebaa237f45c0', 9, '2', '提醒发货已收到', '您对订单 ORDER17449398848975895 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-18 10:13:54', '2025-04-19 17:27:57', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('e36c19e9cf2549b3ba4babcd9d3b0f6d', 8, 'CHECKIN', '恭喜您，已连续签到5天', '您今日签到获得了30积分。您已连续签到5天，继续保持可获得更多奖励！', 0, '2025-05-24 18:47:35', NULL, 1, '{\"earnedPoints\": 30, \"continuousDays\": 5}', 0);
INSERT INTO `user_message` VALUES ('e5d8ba7680f54a0c9b8762c88575bf46', 8, 'COMMENT_REWARD', '评价奖励 +23积分', '感谢您对\"惠氏S-26铂臻3段幼儿...\"的评价！\n\n系统已为您发放以下奖励：\n\n· 基础评价奖励: +5积分\n· 好评奖励: +8积分\n· 基础评价奖励: +10积分\n\n总计: +23积分', 0, '2025-05-28 22:17:44', NULL, 1, '{\"rewards\": [{\"rewardName\": \"基础评价奖励\", \"rewardType\": \"points\", \"rewardValue\": 5, \"rewardDescription\": \"完成订单评价获得基础积分\"}, {\"rewardName\": \"好评奖励\", \"rewardType\": \"points\", \"rewardValue\": 8, \"rewardDescription\": \"给出4-5星好评\"}, {\"rewardName\": \"基础评价奖励\", \"rewardType\": \"points\", \"rewardValue\": 10, \"rewardDescription\": \"完成商品评价获得奖励\"}], \"commentId\": 210, \"productId\": 77, \"totalReward\": 23}', 0);
INSERT INTO `user_message` VALUES ('e6653bf3f2414255b8c9daf17bca1760', 9, 'CHECKIN', '恭喜您，已连续签到3天', '您今日签到获得了20积分。您已连续签到3天，继续保持可获得更多奖励！', 0, '2025-07-11 15:19:54', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 3}', 0);
INSERT INTO `user_message` VALUES ('e6666451-c566-4b66-bc14-781b8787654f', 9, '2', '提醒发货已收到', '您对订单 ORDER17448059023224799 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-16 20:18:47', '2025-04-19 17:27:57', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('e76f0be7-20c6-4a7b-9c72-5a4d31f0c0d7', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17448059023224799 ', 0, '2025-04-16 20:18:47', NULL, 1, '{\"orderNo\": \"ORDER17448059023224799\"}', 1);
INSERT INTO `user_message` VALUES ('e9d92d41-4c49-462d-82e0-af051f06847d', 1, '2', '订单发货提醒', '用户提醒发货: 订单号 ORDER17433100841148442 ', 0, '2025-03-30 12:56:42', NULL, 1, '{\"orderNo\": \"ORDER17433100841148442\"}', 1);
INSERT INTO `user_message` VALUES ('eac4a2d9-df97-4aa5-ab5c-2e67281fa4b8', 9, '4', '签到成功', '恭喜您完成今日签到，获得 21 积分。您已连续签到 2 天，继续保持！', 0, '2025-04-23 14:10:13', NULL, 1, '{\"days\": 2, \"points\": 21, \"source\": \"signin\"}', 0);
INSERT INTO `user_message` VALUES ('eb4bebe059ea4c5e9a04a3583641a116', 8, 'SHIPPING_REMINDER', '用户催发货提醒 - 78845ae7', '用户对订单 OD1752210905878845ae7 进行了催发货。\n下单时间：2025-07-11 13:15:06\n用户留言：对订单 ORDEROD1752210905878845ae7 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-07-11 13:15:49', NULL, 1, '{\"orderId\": 260}', 0);
INSERT INTO `user_message` VALUES ('eccc0f12-025a-4278-93ef-859eb09402d0', 9, '4', '恭喜获得积分奖励', '您的订单 ORDER17451462811964198 已完成，获得 16 积分奖励（订单金额的10%）。', 0, '2025-04-20 18:52:26', NULL, 1, '{\"points\": 16, \"source\": \"order\", \"orderNo\": \"ORDER17451462811964198\", \"actualAmount\": \"169.90\"}', 0);
INSERT INTO `user_message` VALUES ('ef6d7437-e49a-4cad-9921-519a93644cf9', 9, '2', '提醒发货已收到', '您对订单 ORDER17443783487882645 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-11 21:37:24', '2025-04-13 22:13:38', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('f25f27a172cd48c388aa7f5c214b5492', 8, 'CHECKIN', '恭喜您，已连续签到3天', '您今日签到获得了20积分。您已连续签到3天，继续保持可获得更多奖励！', 1, '2025-06-07 14:55:38', '2025-06-07 14:55:49', 1, '{\"earnedPoints\": 20, \"continuousDays\": 3}', 0);
INSERT INTO `user_message` VALUES ('f39ef12db14f47f589b3fbf5798e9ef7', 8, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-05-30 09:34:19', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('f3e99cdb2f724fa5b651b3345787f4a8', 8, 'CHECKIN', '恭喜您，已连续签到6天', '您今日签到获得了25积分。您已连续签到6天，继续保持可获得更多奖励！', 0, '2025-05-15 12:48:21', NULL, 1, '{\"earnedPoints\": 25, \"continuousDays\": 6}', 0);
INSERT INTO `user_message` VALUES ('f545e702b0324038aace0a546e6cb998', 8, 'SHIPPING_REMINDER', '用户催发货提醒 - 800729c7', '用户对订单 OD17491728740800729c7 进行了催发货。\n下单时间：2025-06-06 09:21:14\n用户留言：对订单 ORDEROD17491728740800729c7 的提醒发货请求已收到，商家将尽快处理。', 0, '2025-06-06 09:39:40', NULL, 1, '{\"orderId\": 236}', 0);
INSERT INTO `user_message` VALUES ('f5d0429243cc429eaaae6f57ddf967b4', 8, 'COMMENT_REWARD', '评价奖励 +23积分', '感谢您对\"贝亲婴儿洗发沐浴露二合一\"的评价！\n\n系统已为您发放以下奖励：\n\n· 基础评价奖励: +5积分\n· 好评奖励: +8积分\n· 基础评价奖励: +10积分\n\n总计: +23积分', 0, '2025-07-11 13:17:04', NULL, 1, '{\"rewards\": [{\"rewardName\": \"基础评价奖励\", \"rewardType\": \"points\", \"rewardValue\": 5, \"rewardDescription\": \"完成订单评价获得基础积分\"}, {\"rewardName\": \"好评奖励\", \"rewardType\": \"points\", \"rewardValue\": 8, \"rewardDescription\": \"给出4-5星好评\"}, {\"rewardName\": \"基础评价奖励\", \"rewardType\": \"points\", \"rewardValue\": 10, \"rewardDescription\": \"完成商品评价获得奖励\"}], \"commentId\": 214, \"productId\": 95, \"totalReward\": 23}', 0);
INSERT INTO `user_message` VALUES ('fb199376ab35472c8bac36c95397b0aa', 8, 'CHECKIN', '恭喜您，已连续签到2天', '您今日签到获得了20积分。您已连续签到2天，继续保持可获得更多奖励！', 0, '2025-05-21 13:32:22', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 2}', 0);
INSERT INTO `user_message` VALUES ('fc4b3a5b-7ec1-4995-ab0a-49441ba41fd2', 9, '4', '签到成功', '恭喜您完成今日签到，获得 21 积分。您已连续签到 2 天，继续保持！', 0, '2025-04-24 12:43:28', NULL, 1, '{\"days\": 2, \"points\": 21, \"source\": \"signin\"}', 0);
INSERT INTO `user_message` VALUES ('fd1ce4eb-d020-479a-95e0-3a9c0b99afb4', 9, '2', '提醒发货已收到', '您对订单 ORDER17448106746512253 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-17 15:40:12', '2025-04-19 17:27:57', 1, NULL, 0);
INSERT INTO `user_message` VALUES ('fd25ee3133bd4cfd8aad5ae6a5bd13ac', 58, 'CHECKIN', '签到成功，请继续保持', '您今日签到获得了20积分。开始您的签到之旅，连续签到可以获得更多奖励哦！', 0, '2025-06-09 22:23:21', NULL, 1, '{\"earnedPoints\": 20, \"continuousDays\": 1}', 0);
INSERT INTO `user_message` VALUES ('ff485660-9fcf-493a-b26f-1584f2185a01', 9, '2', '提醒发货已收到', '您对订单 ORDER17450486780378791 的提醒发货请求已收到，商家将尽快处理。', 1, '2025-04-19 15:45:25', '2025-04-19 17:27:57', 1, NULL, 0);

-- ----------------------------
-- Table structure for user_points
-- ----------------------------
DROP TABLE IF EXISTS `user_points`;
CREATE TABLE `user_points`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '积分ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points` int NULL DEFAULT 0 COMMENT '积分总数',
  `level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '普通会员' COMMENT '会员等级',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_points`(`user_id` ASC, `points` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户积分表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_points
-- ----------------------------
INSERT INTO `user_points` VALUES (1, 3, 2500, '普通会员', '2025-01-15 10:30:00', '2025-03-10 14:22:35');
INSERT INTO `user_points` VALUES (2, 4, 7800, '银牌会员', '2025-01-10 09:15:22', '2025-03-12 16:45:18');
INSERT INTO `user_points` VALUES (3, 5, 12600, '金牌会员', '2024-12-28 14:30:45', '2025-03-15 11:28:37');
INSERT INTO `user_points` VALUES (4, 6, 5400, '银牌会员', '2025-01-05 16:45:30', '2025-03-18 09:36:52');
INSERT INTO `user_points` VALUES (5, 8, 33401, '钻石会员', '2025-02-28 11:20:15', '2025-09-19 12:11:15');
INSERT INTO `user_points` VALUES (6, 1, 0, '普通会员', '2025-03-27 19:41:28', '2025-03-27 19:41:28');
INSERT INTO `user_points` VALUES (7, 2, 0, '普通会员', '2025-03-27 19:41:28', '2025-03-27 19:41:28');
INSERT INTO `user_points` VALUES (8, 9, 10842, '普通会员', '2025-03-27 19:41:28', '2025-07-11 15:19:54');
INSERT INTO `user_points` VALUES (9, 10, 0, '普通会员', '2025-03-27 19:41:28', '2025-03-27 19:41:28');
INSERT INTO `user_points` VALUES (10, 11, 0, '普通会员', '2025-03-27 19:41:28', '2025-03-27 19:41:28');
INSERT INTO `user_points` VALUES (11, 48, 0, '普通会员', '2025-03-27 19:41:28', '2025-03-27 19:41:28');
INSERT INTO `user_points` VALUES (12, 58, 20, '普通会员', '2025-06-09 22:23:12', '2025-06-09 22:23:21');
INSERT INTO `user_points` VALUES (14, 59, 11, '普通会员', '2025-07-10 14:31:45', '2025-07-10 16:57:55');

-- ----------------------------
-- View structure for v_hot_search_keywords
-- ----------------------------
DROP VIEW IF EXISTS `v_hot_search_keywords`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_hot_search_keywords` AS select `search_statistics`.`keyword` AS `keyword`,sum(`search_statistics`.`search_count`) AS `total_searches`,avg(`search_statistics`.`result_count`) AS `avg_results`,count(distinct `search_statistics`.`user_id`) AS `unique_users`,max(`search_statistics`.`search_time`) AS `last_search_time` from `search_statistics` where (`search_statistics`.`create_time` >= (now() - interval 7 day)) group by `search_statistics`.`keyword` order by `total_searches` desc;

-- ----------------------------
-- View structure for v_search_overview
-- ----------------------------
DROP VIEW IF EXISTS `v_search_overview`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_search_overview` AS select cast(`search_statistics`.`search_time` as date) AS `search_date`,count(distinct `search_statistics`.`keyword`) AS `unique_keywords`,sum(`search_statistics`.`search_count`) AS `total_searches`,avg(`search_statistics`.`result_count`) AS `avg_results`,avg(`search_statistics`.`response_time`) AS `avg_response_time`,sum((case when (`search_statistics`.`has_click` = 1) then 1 else 0 end)) AS `click_count`,sum((case when (`search_statistics`.`result_count` = 0) then 1 else 0 end)) AS `no_result_count` from `search_statistics` where (`search_statistics`.`search_time` >= (now() - interval 30 day)) group by cast(`search_statistics`.`search_time` as date) order by `search_date` desc;

-- ----------------------------
-- Procedure structure for CleanExpiredSearchStatistics
-- ----------------------------
DROP PROCEDURE IF EXISTS `CleanExpiredSearchStatistics`;
delimiter ;;
CREATE PROCEDURE `CleanExpiredSearchStatistics`(IN days_to_keep INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE affected_rows INT DEFAULT 0;
    
    -- 删除过期数据
    DELETE FROM `search_statistics` 
    WHERE `create_time` < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
    
    SET affected_rows = ROW_COUNT();
    
    -- 记录清理日志
    SELECT CONCAT('清理了 ', affected_rows, ' 条过期搜索统计记录') AS result;
    
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for get_user_info
-- ----------------------------
DROP PROCEDURE IF EXISTS `get_user_info`;
delimiter ;;
CREATE PROCEDURE `get_user_info`(IN p_user_id INT)
BEGIN
    SELECT 
        `user_id`,
        `username`,
        `nickname`,
        `email`,
        `phone`,
        `avatar`,
        `gender`,
        `birthday`,
        `status`,
        `role`,
        `create_time`,
        `update_time`
    FROM `user`
    WHERE `user_id` = p_user_id;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for proc_clean_expired_cart_items
-- ----------------------------
DROP PROCEDURE IF EXISTS `proc_clean_expired_cart_items`;
delimiter ;;
CREATE PROCEDURE `proc_clean_expired_cart_items`()
BEGIN
  -- 删除超过30天未更新的购物车项
  DELETE FROM `cart` WHERE `update_time` < DATE_SUB(NOW(), INTERVAL 30 DAY);
  
  -- 删除已经标记为过期的购物车项
  DELETE FROM `cart` WHERE `expire_time` IS NOT NULL AND `expire_time` < NOW();
  
  -- 将已下单的购物车项标记为已下单状态
  UPDATE `cart` SET `status` = 2 
  WHERE `cart_id` IN (
    SELECT c.cart_id FROM `cart` c
    JOIN `order_goods` og ON c.goods_id = og.goods_id 
    JOIN `order` o ON og.order_id = o.order_id
    WHERE c.user_id = o.user_id AND o.create_time > c.create_time
  );
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for proc_user_consume
-- ----------------------------
DROP PROCEDURE IF EXISTS `proc_user_consume`;
delimiter ;;
CREATE PROCEDURE `proc_user_consume`(IN p_user_id INT,
  IN p_amount DECIMAL(10, 2),
  IN p_payment_method VARCHAR(20),
  IN p_order_id VARCHAR(64),
  IN p_description VARCHAR(255),
  OUT p_transaction_id BIGINT,
  OUT p_result INT)
BEGIN
  CALL proc_user_transaction(
    p_user_id, p_amount, 2, p_payment_method, p_order_id, 
    IFNULL(p_description, '商品购买'), NULL, p_transaction_id, p_result
  );
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for proc_user_recharge
-- ----------------------------
DROP PROCEDURE IF EXISTS `proc_user_recharge`;
delimiter ;;
CREATE PROCEDURE `proc_user_recharge`(IN p_user_id INT,
  IN p_amount DECIMAL(10, 2),
  IN p_payment_method VARCHAR(20),
  IN p_related_id VARCHAR(64),
  IN p_description VARCHAR(255),
  OUT p_transaction_id BIGINT,
  OUT p_result INT)
BEGIN
  CALL proc_user_transaction(
    p_user_id, p_amount, 1, p_payment_method, p_related_id, 
    IFNULL(p_description, '账户充值'), NULL, p_transaction_id, p_result
  );
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for proc_user_transaction
-- ----------------------------
DROP PROCEDURE IF EXISTS `proc_user_transaction`;
delimiter ;;
CREATE PROCEDURE `proc_user_transaction`(IN p_user_id INT,
  IN p_amount DECIMAL(10, 2),
  IN p_type TINYINT,
  IN p_payment_method VARCHAR(20),
  IN p_related_id VARCHAR(64),
  IN p_description VARCHAR(255),
  IN p_remark VARCHAR(255),
  OUT p_transaction_id BIGINT,
  OUT p_result INT)
BEGIN
  DECLARE v_account_id INT;
  DECLARE v_balance DECIMAL(10, 2);
  DECLARE v_status TINYINT;
  DECLARE v_transaction_no VARCHAR(64);
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    SET p_result = 0; -- 失败
  END;
  
  START TRANSACTION;
  
  -- 检查用户账户是否存在且正常
  SELECT account_id, balance, status INTO v_account_id, v_balance, v_status
  FROM user_account WHERE user_id = p_user_id FOR UPDATE;
  
  IF v_account_id IS NULL THEN
    SET p_result = 0; -- 账户不存在
    ROLLBACK;
  ELSEIF v_status = 0 THEN
    SET p_result = 0; -- 账户已冻结
    ROLLBACK;
  ELSE
    -- 消费、提现、转账需要检查余额是否充足
    IF (p_type IN (2, 4, 5) AND v_balance < p_amount) THEN
      SET p_result = 0; -- 余额不足
      ROLLBACK;
    ELSE
      -- 生成交易单号
      SET v_transaction_no = CONCAT('TXN', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), LPAD(FLOOR(RAND() * 1000000), 6, '0'));
      
      -- 更新账户余额
      IF p_type IN (1, 3, 6) THEN -- 充值、退款、收入
        UPDATE user_account SET balance = balance + p_amount WHERE account_id = v_account_id;
      ELSEIF p_type IN (2, 4, 5) THEN -- 消费、提现、转账
        UPDATE user_account SET balance = balance - p_amount WHERE account_id = v_account_id;
      END IF;
      
      -- 获取更新后的余额
      SELECT balance INTO v_balance FROM user_account WHERE account_id = v_account_id;
      
      -- 记录交易流水
      INSERT INTO account_transaction (
        transaction_no, account_id, user_id, amount, balance, type, 
        payment_method, status, related_id, description, remark
      ) VALUES (
        v_transaction_no, v_account_id, p_user_id, p_amount, v_balance, p_type,
        p_payment_method, 1, p_related_id, p_description, p_remark
      );
      
      -- 获取交易ID
      SET p_transaction_id = LAST_INSERT_ID();
      SET p_result = 1; -- 成功
      COMMIT;
    END IF;
  END IF;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for update_order_status
-- ----------------------------
DROP PROCEDURE IF EXISTS `update_order_status`;
delimiter ;;
CREATE PROCEDURE `update_order_status`(IN p_order_id INT,    IN p_status VARCHAR(50),    IN p_pay_time DATETIME)
BEGIN    UPDATE `order` SET        `status` = p_status,        `pay_time` = p_pay_time,        `update_time` = NOW()    WHERE `order_id` = p_order_id; END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for update_user_info
-- ----------------------------
DROP PROCEDURE IF EXISTS `update_user_info`;
delimiter ;;
CREATE PROCEDURE `update_user_info`(IN p_user_id INT,
    IN p_nickname VARCHAR(50),
    IN p_gender VARCHAR(20),
    IN p_phone VARCHAR(20),
    IN p_email VARCHAR(100),
    IN p_birthday DATE,
    IN p_avatar VARCHAR(255))
BEGIN
    DECLARE email_exists INT DEFAULT 0;
    
    -- 检查email是否被其他用户使用
    IF p_email IS NOT NULL THEN
        SELECT COUNT(*) INTO email_exists FROM `user` 
        WHERE `email` = p_email AND `user_id` != p_user_id;
    END IF;
    
    IF email_exists = 0 THEN
        UPDATE `user` SET
            `nickname` = CASE WHEN p_nickname IS NOT NULL THEN p_nickname ELSE `nickname` END,
            `gender` = CASE WHEN p_gender IS NOT NULL THEN p_gender ELSE `gender` END,
            `phone` = CASE WHEN p_phone IS NOT NULL THEN p_phone ELSE `phone` END,
            `email` = CASE WHEN p_email IS NOT NULL THEN p_email ELSE `email` END,
            `birthday` = CASE WHEN p_birthday IS NOT NULL THEN p_birthday ELSE `birthday` END,
            `avatar` = CASE WHEN p_avatar IS NOT NULL THEN p_avatar ELSE `avatar` END,
            `update_time` = CURRENT_TIMESTAMP
        WHERE `user_id` = p_user_id;
        
        SELECT 'success' AS result, '' AS message;
    ELSE
        SELECT 'error' AS result, '邮箱已被使用' AS message;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Event structure for evt_daily_clean_cart
-- ----------------------------
DROP EVENT IF EXISTS `evt_daily_clean_cart`;
delimiter ;;
CREATE EVENT `evt_daily_clean_cart`
ON SCHEDULE
EVERY '1' DAY STARTS '2025-03-25 00:00:00'
DO CALL proc_clean_expired_cart_items()
;;
delimiter ;

-- ----------------------------
-- Event structure for evt_daily_expire_coupon_check
-- ----------------------------
DROP EVENT IF EXISTS `evt_daily_expire_coupon_check`;
delimiter ;;
CREATE EVENT `evt_daily_expire_coupon_check`
ON SCHEDULE
EVERY '1' DAY STARTS '2025-04-24 00:00:00'
DO BEGIN
  UPDATE `user_coupon` 
  SET `status` = 'EXPIRED' 
  WHERE `status` = 'UNUSED' AND `expire_time` < NOW();
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table cart
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_cart_before_insert`;
delimiter ;;
CREATE TRIGGER `trg_cart_before_insert` BEFORE INSERT ON `cart` FOR EACH ROW BEGIN
  -- 生成规格哈希值
  IF NEW.specs IS NOT NULL THEN
    SET NEW.specs_hash = MD5(JSON_UNQUOTE(JSON_EXTRACT(NEW.specs, '$')));
  ELSE
    SET NEW.specs_hash = MD5('');
  END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table payment
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_payment_update_sync_order`;
delimiter ;;
CREATE TRIGGER `trg_payment_update_sync_order` AFTER UPDATE ON `payment` FOR EACH ROW BEGIN
    -- 当payment_method被修改，且订单ID不为空时
    IF NEW.payment_method != OLD.payment_method AND NEW.order_id IS NOT NULL THEN
        -- 更新关联的订单记录
        UPDATE `order` 
        SET payment_method = NEW.payment_method,
            update_time = NOW()
        WHERE order_id = NEW.order_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table user
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_user_after_insert`;
delimiter ;;
CREATE TRIGGER `trg_user_after_insert` AFTER INSERT ON `user` FOR EACH ROW BEGIN
  INSERT INTO `user_account` (`user_id`, `balance`, `frozen_balance`, `points`, `status`)
  VALUES (NEW.user_id, 0.00, 0.00, 0, 1);
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table user_coupon
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_user_coupon_expire_check`;
delimiter ;;
CREATE TRIGGER `trg_user_coupon_expire_check` BEFORE UPDATE ON `user_coupon` FOR EACH ROW BEGIN
  -- 如果过期时间已到且状态为未使用，则修改状态为已过期
  IF NEW.expire_time < NOW() AND NEW.status = 'UNUSED' THEN
    SET NEW.status = 'EXPIRED';
  END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
