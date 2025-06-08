-- ----------------------------
-- 用户账户表相关（用于记录用户支付金额、支付方式等）
-- ----------------------------
DROP TABLE IF EXISTS `user_account`;
CREATE TABLE `user_account` (
  `account_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `frozen_balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结余额',
  `points` int NOT NULL DEFAULT 0 COMMENT '账户积分',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '账户状态：0-冻结，1-正常',
  `pay_password` varchar(100) NULL COMMENT '支付密码（加密存储）',
  `security_level` tinyint NOT NULL DEFAULT 1 COMMENT '安全等级：1-低，2-中，3-高',
  `last_login_time` datetime NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) NULL COMMENT '最后登录IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`account_id`) USING BTREE,
  UNIQUE INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_user_account_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户账户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for account_transaction 账户交易记录表
-- ----------------------------
DROP TABLE IF EXISTS `account_transaction`;
CREATE TABLE `account_transaction` (
  `transaction_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `transaction_no` varchar(64) NOT NULL COMMENT '交易单号',
  `account_id` int UNSIGNED NOT NULL COMMENT '账户ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '交易金额',
  `balance` decimal(10, 2) NOT NULL COMMENT '交易后余额',
  `type` tinyint NOT NULL COMMENT '交易类型：1-充值，2-消费，3-退款，4-提现，5-转账，6-收入，7-其他',
  `payment_method` varchar(20) NULL COMMENT '支付方式：alipay-支付宝，wechat-微信支付，bank-银行卡，balance-余额，other-其他',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '交易状态：0-失败，1-成功，2-处理中，3-已取消',
  `related_id` varchar(64) NULL COMMENT '关联ID（如订单ID、退款ID等）',
  `description` varchar(255) NULL COMMENT '交易描述',
  `remark` varchar(255) NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`transaction_id`) USING BTREE,
  UNIQUE INDEX `idx_transaction_no`(`transaction_no`) USING BTREE,
  INDEX `idx_account_id`(`account_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  CONSTRAINT `fk_transaction_account` FOREIGN KEY (`account_id`) REFERENCES `user_account` (`account_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账户交易记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 移除user表中的balance字段（因为现在会使用user_account表管理余额）
-- ----------------------------
-- ALTER TABLE `user` DROP COLUMN `balance`;

-- ----------------------------
-- 账户初始化触发器：用户注册后自动创建账户
-- ----------------------------
DELIMITER //
CREATE TRIGGER IF NOT EXISTS `trg_user_after_insert`
AFTER INSERT ON `user`
FOR EACH ROW
BEGIN
  INSERT INTO `user_account` (`user_id`, `balance`, `frozen_balance`, `points`, `status`)
  VALUES (NEW.user_id, 0.00, 0.00, 0, 1);
END //
DELIMITER ;

-- ----------------------------
-- 账户交易处理存储过程
-- ----------------------------
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `proc_user_transaction`(
  IN p_user_id INT,
  IN p_amount DECIMAL(10, 2),
  IN p_type TINYINT,
  IN p_payment_method VARCHAR(20),
  IN p_related_id VARCHAR(64),
  IN p_description VARCHAR(255),
  IN p_remark VARCHAR(255),
  OUT p_transaction_id BIGINT,
  OUT p_result INT
)
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
END //
DELIMITER ;

-- ----------------------------
-- 余额充值存储过程
-- ----------------------------
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `proc_user_recharge`(
  IN p_user_id INT,
  IN p_amount DECIMAL(10, 2),
  IN p_payment_method VARCHAR(20),
  IN p_related_id VARCHAR(64),
  IN p_description VARCHAR(255),
  OUT p_transaction_id BIGINT,
  OUT p_result INT
)
BEGIN
  CALL proc_user_transaction(
    p_user_id, p_amount, 1, p_payment_method, p_related_id, 
    IFNULL(p_description, '账户充值'), NULL, p_transaction_id, p_result
  );
END //
DELIMITER ;

-- ----------------------------
-- 余额消费存储过程
-- ----------------------------
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `proc_user_consume`(
  IN p_user_id INT,
  IN p_amount DECIMAL(10, 2),
  IN p_payment_method VARCHAR(20),
  IN p_order_id VARCHAR(64),
  IN p_description VARCHAR(255),
  OUT p_transaction_id BIGINT,
  OUT p_result INT
)
BEGIN
  CALL proc_user_transaction(
    p_user_id, p_amount, 2, p_payment_method, p_order_id, 
    IFNULL(p_description, '商品购买'), NULL, p_transaction_id, p_result
  );
END //
DELIMITER ;

-- ----------------------------
-- 为现有用户初始化账户数据 
-- ----------------------------
INSERT INTO `user_account` (`user_id`, `balance`, `frozen_balance`, `points`, `status`)
SELECT `user_id`, 0.00, 0.00, 0, 1 
FROM `user` u
WHERE NOT EXISTS (SELECT 1 FROM `user_account` ua WHERE ua.user_id = u.user_id);