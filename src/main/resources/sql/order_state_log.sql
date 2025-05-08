-- 订单状态变更日志表
CREATE TABLE IF NOT EXISTS `order_state_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `order_id` int(11) NOT NULL COMMENT '订单ID',
    `order_no` varchar(64) NOT NULL COMMENT '订单编号',
    `old_status` varchar(32) NOT NULL COMMENT '原状态',
    `new_status` varchar(32) NOT NULL COMMENT '新状态',
    `event` varchar(32) NOT NULL COMMENT '触发事件',
    `operator` varchar(64) NOT NULL COMMENT '操作者',
    `reason` varchar(255) DEFAULT NULL COMMENT '变更原因',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态变更日志表'; 