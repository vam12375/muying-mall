-- =====================================================
-- 系统设置模块数据库表
-- 包含：菜单管理、通知公告、字典管理
-- =====================================================

-- -----------------------------------------------------
-- 1. 系统菜单表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id` INT DEFAULT 0 COMMENT '父菜单ID，0表示顶级菜单',
    `name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '菜单图标',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    `sort` INT DEFAULT 0 COMMENT '排序值，越小越靠前',
    `visible` TINYINT DEFAULT 1 COMMENT '是否可见：1-可见，0-隐藏',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';

-- 初始化菜单数据
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `icon`, `path`, `sort`, `visible`, `status`) VALUES
(1, 0, '仪表盘', 'Home', '/dashboard', 1, 1, 1),
(2, 0, '商品管理', 'Package', '/products', 2, 1, 1),
(21, 2, '商品列表', 'LayoutGrid', '/products/list', 1, 1, 1),
(22, 2, '商品分类', 'FolderTree', '/products/category', 2, 1, 1),
(23, 2, '品牌管理', 'Tag', '/products/brands', 3, 1, 1),
(3, 0, '订单管理', 'ShoppingCart', '/orders', 3, 1, 1),
(4, 0, '用户管理', 'Users', '/users', 4, 1, 1),
(5, 0, '系统设置', 'Settings', '/settings', 5, 1, 1),
(51, 5, '系统配置', 'Sliders', '/settings/config', 1, 1, 1),
(52, 5, '菜单管理', 'Menu', '/settings/menu', 2, 1, 1),
(53, 5, '通知公告', 'Megaphone', '/settings/notice', 3, 1, 1),
(54, 5, '字典管理', 'BookOpen', '/settings/dict', 4, 1, 1);

-- -----------------------------------------------------
-- 2. 系统通知公告表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_notice` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT COMMENT '公告内容',
    `type` VARCHAR(20) DEFAULT 'notice' COMMENT '公告类型：system-系统通知，notice-公告，feature-功能更新，activity-活动',
    `status` VARCHAR(20) DEFAULT 'draft' COMMENT '状态：published-已发布，draft-草稿',
    `is_pinned` TINYINT DEFAULT 0 COMMENT '是否置顶：1-置顶，0-不置顶',
    `author` VARCHAR(50) DEFAULT NULL COMMENT '发布者',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_is_pinned` (`is_pinned`),
    KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知公告表';

-- 初始化公告数据
INSERT INTO `sys_notice` (`title`, `content`, `type`, `status`, `is_pinned`, `author`, `publish_time`, `view_count`) VALUES
('系统升级通知', '系统将于近期进行升级维护，届时系统将暂停服务，请提前做好准备。', 'system', 'published', 1, 'admin', NOW(), 100),
('新功能上线：积分商城', '积分商城功能已正式上线，用户可使用积分兑换精美礼品。', 'feature', 'published', 0, 'admin', NOW(), 200),
('会员日活动预告', '每月8日为会员日，全场商品享受会员专属折扣。', 'activity', 'draft', 0, 'admin', NULL, 0);

-- -----------------------------------------------------
-- 3. 字典类型表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_dict_type` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
    `code` VARCHAR(100) NOT NULL COMMENT '字典类型编码（唯一标识）',
    `name` VARCHAR(100) NOT NULL COMMENT '字典类型名称',
    `status` VARCHAR(20) DEFAULT 'enabled' COMMENT '状态：enabled-启用，disabled-禁用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注说明',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 初始化字典类型数据
INSERT INTO `sys_dict_type` (`code`, `name`, `status`, `remark`) VALUES
('order_status', '订单状态', 'enabled', '订单流转状态'),
('payment_method', '支付方式', 'enabled', '系统支持的支付方式'),
('user_status', '用户状态', 'enabled', '用户账号状态'),
('logistics_status', '物流状态', 'enabled', '物流配送状态'),
('refund_reason', '退款原因', 'enabled', '用户申请退款的原因选项'),
('gender', '性别', 'enabled', '用户性别选项');

-- -----------------------------------------------------
-- 4. 字典项表
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_dict_item` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '字典项ID',
    `dict_code` VARCHAR(100) NOT NULL COMMENT '所属字典类型编码',
    `label` VARCHAR(100) NOT NULL COMMENT '字典项标签（显示名称）',
    `value` VARCHAR(100) NOT NULL COMMENT '字典项值',
    `sort` INT DEFAULT 0 COMMENT '排序值，越小越靠前',
    `status` VARCHAR(20) DEFAULT 'enabled' COMMENT '状态：enabled-启用，disabled-禁用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注说明',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_dict_code` (`dict_code`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项表';

-- 初始化字典项数据
-- 订单状态
INSERT INTO `sys_dict_item` (`dict_code`, `label`, `value`, `sort`, `status`, `remark`) VALUES
('order_status', '待支付', 'pending', 1, 'enabled', '订单已创建，等待支付'),
('order_status', '已支付', 'paid', 2, 'enabled', '订单已支付成功'),
('order_status', '待发货', 'processing', 3, 'enabled', '订单正在处理中'),
('order_status', '已发货', 'shipped', 4, 'enabled', '订单已发货'),
('order_status', '已完成', 'completed', 5, 'enabled', '订单已完成'),
('order_status', '已取消', 'cancelled', 6, 'enabled', '订单已取消');

-- 支付方式
INSERT INTO `sys_dict_item` (`dict_code`, `label`, `value`, `sort`, `status`, `remark`) VALUES
('payment_method', '支付宝', 'alipay', 1, 'enabled', '支付宝在线支付'),
('payment_method', '微信支付', 'wechat', 2, 'enabled', '微信在线支付'),
('payment_method', '余额支付', 'balance', 3, 'enabled', '账户余额支付'),
('payment_method', '货到付款', 'cod', 4, 'disabled', '货到付款（暂不支持）');

-- 用户状态
INSERT INTO `sys_dict_item` (`dict_code`, `label`, `value`, `sort`, `status`, `remark`) VALUES
('user_status', '正常', 'active', 1, 'enabled', '账号正常使用'),
('user_status', '禁用', 'disabled', 2, 'enabled', '账号已被禁用'),
('user_status', '待激活', 'pending', 3, 'enabled', '账号待激活');

-- 物流状态
INSERT INTO `sys_dict_item` (`dict_code`, `label`, `value`, `sort`, `status`, `remark`) VALUES
('logistics_status', '待揽收', 'pending', 1, 'enabled', '快递员待揽收'),
('logistics_status', '运输中', 'transit', 2, 'enabled', '包裹运输中'),
('logistics_status', '派送中', 'delivering', 3, 'enabled', '快递员派送中'),
('logistics_status', '已签收', 'signed', 4, 'enabled', '已签收'),
('logistics_status', '异常', 'exception', 5, 'enabled', '物流异常');

-- 退款原因
INSERT INTO `sys_dict_item` (`dict_code`, `label`, `value`, `sort`, `status`, `remark`) VALUES
('refund_reason', '不想要了', 'unwanted', 1, 'enabled', '用户不想要了'),
('refund_reason', '商品质量问题', 'quality', 2, 'enabled', '商品存在质量问题'),
('refund_reason', '发错货', 'wrong_item', 3, 'enabled', '商家发错商品'),
('refund_reason', '商品与描述不符', 'mismatch', 4, 'enabled', '商品与描述不符'),
('refund_reason', '物流太慢', 'slow_delivery', 5, 'enabled', '物流配送太慢'),
('refund_reason', '其他原因', 'other', 6, 'enabled', '其他原因');

-- 性别
INSERT INTO `sys_dict_item` (`dict_code`, `label`, `value`, `sort`, `status`, `remark`) VALUES
('gender', '男', 'male', 1, 'enabled', '男性'),
('gender', '女', 'female', 2, 'enabled', '女性'),
('gender', '保密', 'unknown', 3, 'enabled', '不公开');
