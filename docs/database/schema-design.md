# 母婴商城数据库设计详解

## 设计理念

母婴商城数据库设计遵循以下核心原则：

1. **规范化设计**: 采用第三范式(3NF)，减少数据冗余，避免更新异常
2. **业务模块化**: 根据业务领域划分表格，提高模块内聚性  
3. **性能优化**: 为高频查询设计适当索引，关键表使用合理的分区策略
4. **数据安全**: 敏感数据加密存储，应用审计机制
5. **可扩展性**: 预留扩展字段，支持未来业务变化

## 技术规范

- **数据库版本**: MySQL 8.0+
- **字符集**: UTF8MB4
- **排序规则**: utf8mb4_0900_ai_ci  
- **存储引擎**: InnoDB（支持事务、外键约束）
- **时区**: Asia/Shanghai

## 核心表结构设计

### 1. 用户相关表

#### 用户主表 (user)
```sql
CREATE TABLE `user` (
  `user_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(加密)',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `gender` enum('M','F','U') DEFAULT 'U' COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `idx_username` (`username`),
  UNIQUE KEY `idx_email` (`email`),
  UNIQUE KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

**设计要点**:
- 用户名、邮箱、手机号设置唯一索引，支持多种登录方式
- 密码字段长度100，支持各种加密算法
- 性别使用枚举类型，节省存储空间
- 状态字段支持用户禁用功能

#### 用户地址表 (user_address)
```sql
CREATE TABLE `user_address` (
  `address_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否默认地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`address_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`),
  CONSTRAINT `fk_user_address_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';
```

**设计要点**:
- 支持一个用户多个收货地址
- 默认地址标识，便于快速选择
- 级联删除，用户删除时自动删除相关地址

### 2. 商品相关表

#### 商品分类表 (category)
```sql
CREATE TABLE `category` (
  `category_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` int UNSIGNED DEFAULT 0 COMMENT '父分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '分类图标',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';
```

**设计要点**:
- 支持无限级分类结构
- parent_id=0表示顶级分类
- 状态和排序组合索引，优化分类查询

#### 商品主表 (product)
```sql
CREATE TABLE `product` (
  `product_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `category_id` int UNSIGNED NOT NULL COMMENT '分类ID',
  `brand_id` int UNSIGNED DEFAULT NULL COMMENT '品牌ID',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `product_sn` varchar(50) DEFAULT NULL COMMENT '商品编号',
  `product_img` varchar(255) DEFAULT NULL COMMENT '商品主图',
  `product_detail` text COMMENT '商品详情',
  `price_new` decimal(10,2) NOT NULL COMMENT '现价',
  `price_old` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `sales` int NOT NULL DEFAULT 0 COMMENT '销量',
  `rating` decimal(2,1) DEFAULT 5.0 COMMENT '评分',
  `review_count` int DEFAULT 0 COMMENT '评价数量',
  `product_status` enum('上架','下架') DEFAULT '上架' COMMENT '商品状态',
  `is_hot` tinyint(1) DEFAULT 0 COMMENT '是否热门',
  `is_new` tinyint(1) DEFAULT 0 COMMENT '是否新品',
  `is_recommend` tinyint(1) DEFAULT 0 COMMENT '是否推荐',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `idx_product_sn` (`product_sn`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_brand_id` (`brand_id`),
  KEY `idx_category_status_price` (`category_id`, `product_status`, `price_new`),
  KEY `idx_hot_new_recommend` (`is_hot`, `is_new`, `is_recommend`),
  CONSTRAINT `chk_price_positive` CHECK (`price_new` >= 0 AND `price_old` >= 0),
  CONSTRAINT `chk_rating_range` CHECK (`rating` >= 1.0 AND `rating` <= 5.0`),
  CONSTRAINT `chk_stock_positive` CHECK (`stock` >= 0`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
```

**设计要点**:
- 商品编号唯一索引，支持SKU管理
- 价格使用decimal类型，避免浮点数精度问题
- 多个标识字段支持商品推荐算法
- 添加约束检查，保证数据合法性
- 组合索引优化商品查询性能

### 3. 订单相关表

#### 订单主表 (order)
```sql
CREATE TABLE `order` (
  `order_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `status` varchar(20) NOT NULL DEFAULT 'pending_payment' COMMENT '订单状态',
  `payment_id` bigint UNSIGNED DEFAULT NULL COMMENT '支付ID',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `receiver_address` varchar(500) NOT NULL COMMENT '收货地址',
  `remark` text COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_pay_time` (`pay_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

**设计要点**:
- 订单编号唯一，支持订单查询
- 订单状态使用字符串，便于扩展
- 收货信息冗余存储，避免地址变更影响历史订单
- 多个时间索引支持订单统计分析

## 索引设计策略

### 1. 主键索引
所有表都使用自增整数作为主键，保证查询性能和数据唯一性。

### 2. 唯一索引
- 用户表：username, email, phone
- 商品表：product_sn
- 订单表：order_no

### 3. 普通索引
- 外键字段：提高关联查询性能
- 状态字段：支持状态筛选
- 时间字段：支持时间范围查询

### 4. 组合索引
- 商品表：(category_id, product_status, price_new) - 支持分类商品查询
- 用户地址表：(user_id, is_default) - 支持默认地址查询

## 约束设计

### 1. 外键约束
- 保证数据引用完整性
- 支持级联删除和更新
- 在高并发场景下可考虑移除，通过应用层保证

### 2. 检查约束
- 价格非负约束
- 评分范围约束（1.0-5.0）
- 库存非负约束

### 3. 唯一约束
- 防止重复数据
- 支持业务唯一性要求

## 性能优化策略

### 1. 分区策略
- 订单表按时间分区，提高历史数据查询性能
- 日志表按月分区，便于数据归档

### 2. 缓存策略
- 商品信息缓存：Redis存储热门商品
- 分类信息缓存：树形结构缓存
- 用户会话缓存：Session存储

### 3. 读写分离
- 主库处理写操作
- 从库处理读操作
- 通过中间件实现自动路由

## 数据安全

### 1. 敏感数据加密
- 用户密码：BCrypt加密
- 支付密码：单独加密存储
- 个人信息：可选择性加密

### 2. 审计日志
- 关键操作记录
- 数据变更追踪
- 异常访问监控

---
*最后更新时间: 2025-06-18*
*维护者: 青柠檬*
