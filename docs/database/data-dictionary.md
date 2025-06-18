# 母婴商城数据字典

## 概述

本文档详细描述了母婴商城系统中所有数据表的字段定义、数据类型、约束条件和业务规则。

## 表结构概览

| 表名 | 中文名称 | 记录数估算 | 主要用途 |
|------|----------|------------|----------|
| user | 用户表 | 10万+ | 存储用户基本信息 |
| user_address | 用户地址表 | 50万+ | 存储用户收货地址 |
| user_account | 用户账户表 | 10万+ | 存储用户账户信息 |
| category | 商品分类表 | 100+ | 存储商品分类信息 |
| brand | 品牌表 | 500+ | 存储品牌信息 |
| product | 商品表 | 10万+ | 存储商品基本信息 |
| product_image | 商品图片表 | 50万+ | 存储商品图片 |
| product_specs | 商品规格表 | 30万+ | 存储商品规格信息 |
| order | 订单表 | 100万+ | 存储订单信息 |
| order_product | 订单商品表 | 300万+ | 存储订单商品详情 |
| payment | 支付表 | 100万+ | 存储支付记录 |
| cart | 购物车表 | 50万+ | 存储购物车信息 |
| coupon | 优惠券表 | 1万+ | 存储优惠券信息 |
| user_coupon | 用户优惠券表 | 100万+ | 存储用户优惠券 |
| comment | 评价表 | 50万+ | 存储商品评价 |
| logistics | 物流表 | 100万+ | 存储物流信息 |

## 详细字段说明

### 1. 用户表 (user)

| 字段名 | 数据类型 | 长度 | 是否为空 | 默认值 | 说明 | 业务规则 |
|--------|----------|------|----------|--------|------|----------|
| user_id | int | - | NO | AUTO_INCREMENT | 用户ID，主键 | 自增，唯一标识 |
| username | varchar | 50 | NO | - | 用户名 | 3-50字符，支持字母数字下划线 |
| password | varchar | 100 | NO | - | 密码(加密) | BCrypt加密存储 |
| email | varchar | 100 | YES | NULL | 邮箱 | 邮箱格式验证，唯一 |
| phone | varchar | 20 | YES | NULL | 手机号 | 11位数字，唯一 |
| avatar | varchar | 255 | YES | NULL | 头像URL | 支持相对路径和绝对路径 |
| nickname | varchar | 50 | YES | NULL | 昵称 | 1-50字符 |
| gender | enum | - | YES | 'U' | 性别 | M-男，F-女，U-未知 |
| birthday | date | - | YES | NULL | 生日 | YYYY-MM-DD格式 |
| status | tinyint | 1 | YES | 1 | 状态 | 0-禁用，1-正常 |
| create_time | datetime | - | NO | CURRENT_TIMESTAMP | 创建时间 | 自动设置 |
| update_time | datetime | - | NO | CURRENT_TIMESTAMP | 更新时间 | 自动更新 |

### 2. 商品表 (product)

| 字段名 | 数据类型 | 长度 | 是否为空 | 默认值 | 说明 | 业务规则 |
|--------|----------|------|----------|--------|------|----------|
| product_id | int | - | NO | AUTO_INCREMENT | 商品ID，主键 | 自增，唯一标识 |
| category_id | int | - | NO | - | 分类ID | 外键，关联category表 |
| brand_id | int | - | YES | NULL | 品牌ID | 外键，关联brand表 |
| product_name | varchar | 100 | NO | - | 商品名称 | 1-100字符 |
| product_sn | varchar | 50 | YES | NULL | 商品编号 | 唯一，支持SKU管理 |
| product_img | varchar | 255 | YES | NULL | 商品主图 | 图片URL |
| product_detail | text | - | YES | NULL | 商品详情 | HTML格式 |
| price_new | decimal | 10,2 | NO | - | 现价 | 非负数，精确到分 |
| price_old | decimal | 10,2 | YES | NULL | 原价 | 非负数，用于显示折扣 |
| stock | int | - | NO | 0 | 库存 | 非负整数 |
| sales | int | - | NO | 0 | 销量 | 非负整数 |
| rating | decimal | 2,1 | YES | 5.0 | 评分 | 1.0-5.0范围 |
| review_count | int | - | YES | 0 | 评价数量 | 非负整数 |
| product_status | enum | - | YES | '上架' | 商品状态 | 上架、下架 |
| is_hot | tinyint | 1 | YES | 0 | 是否热门 | 0-否，1-是 |
| is_new | tinyint | 1 | YES | 0 | 是否新品 | 0-否，1-是 |
| is_recommend | tinyint | 1 | YES | 0 | 是否推荐 | 0-否，1-是 |

### 3. 订单表 (order)

| 字段名 | 数据类型 | 长度 | 是否为空 | 默认值 | 说明 | 业务规则 |
|--------|----------|------|----------|--------|------|----------|
| order_id | int | - | NO | AUTO_INCREMENT | 订单ID，主键 | 自增，唯一标识 |
| order_no | varchar | 32 | NO | - | 订单编号 | 唯一，格式：时间戳+随机数 |
| user_id | int | - | NO | - | 用户ID | 外键，关联user表 |
| total_amount | decimal | 10,2 | NO | - | 订单总金额 | 非负数，精确到分 |
| actual_amount | decimal | 10,2 | NO | - | 实付金额 | 非负数，扣除优惠后金额 |
| status | varchar | 20 | NO | 'pending_payment' | 订单状态 | 状态机管理 |
| payment_id | bigint | - | YES | NULL | 支付ID | 外键，关联payment表 |
| pay_time | datetime | - | YES | NULL | 支付时间 | 支付成功时设置 |
| receiver_name | varchar | 50 | NO | - | 收货人姓名 | 1-50字符 |
| receiver_phone | varchar | 20 | NO | - | 收货人电话 | 手机号格式 |
| receiver_address | varchar | 500 | NO | - | 收货地址 | 完整地址信息 |
| remark | text | - | YES | NULL | 备注 | 用户备注信息 |

### 4. 支付表 (payment)

| 字段名 | 数据类型 | 长度 | 是否为空 | 默认值 | 说明 | 业务规则 |
|--------|----------|------|----------|--------|------|----------|
| payment_id | bigint | - | NO | AUTO_INCREMENT | 支付ID，主键 | 自增，唯一标识 |
| payment_no | varchar | 32 | NO | - | 支付编号 | 唯一，内部支付流水号 |
| order_id | int | - | NO | - | 订单ID | 外键，关联order表 |
| user_id | int | - | NO | - | 用户ID | 外键，关联user表 |
| amount | decimal | 10,2 | NO | - | 支付金额 | 非负数，精确到分 |
| payment_method | enum | - | NO | - | 支付方式 | ALIPAY-支付宝，WECHAT-微信 |
| status | enum | - | NO | 'PENDING' | 支付状态 | 状态机管理 |
| transaction_id | varchar | 64 | YES | NULL | 第三方交易号 | 支付平台返回的交易号 |
| pay_time | datetime | - | YES | NULL | 支付时间 | 支付成功时设置 |

## 枚举值定义

### 订单状态 (order.status)
- `pending_payment`: 待付款
- `pending_shipment`: 待发货  
- `shipped`: 已发货
- `completed`: 已完成
- `cancelled`: 已取消

### 支付方式 (payment.payment_method)
- `ALIPAY`: 支付宝
- `WECHAT`: 微信支付
- `BALANCE`: 余额支付

### 支付状态 (payment.status)
- `PENDING`: 待支付
- `SUCCESS`: 支付成功
- `FAILED`: 支付失败
- `CANCELLED`: 已取消

### 用户性别 (user.gender)
- `M`: 男性
- `F`: 女性
- `U`: 未知

### 商品状态 (product.product_status)
- `上架`: 正常销售
- `下架`: 停止销售

## 业务规则说明

### 1. 用户相关规则
- 用户名、邮箱、手机号必须唯一
- 密码必须经过BCrypt加密存储
- 用户状态为禁用时，无法登录和下单

### 2. 商品相关规则
- 商品必须关联有效的分类
- 商品编号如果设置，必须唯一
- 库存为0时，前端应显示缺货状态
- 评分范围1.0-5.0，默认5.0

### 3. 订单相关规则
- 订单编号格式：年月日时分秒+6位随机数
- 订单状态按状态机流转，不可逆转
- 实付金额不能大于订单总金额
- 收货信息在订单创建时冗余存储

### 4. 支付相关规则
- 支付金额必须与订单实付金额一致
- 支付成功后，订单状态自动更新
- 第三方交易号用于对账和退款

## 索引策略

### 高频查询索引
- user表：username, email, phone（唯一索引）
- product表：category_id, brand_id, product_status
- order表：user_id, status, create_time
- payment表：order_id, status

### 组合索引
- product表：(category_id, product_status, price_new)
- order表：(user_id, status, create_time)
- user_address表：(user_id, is_default)

## 数据完整性约束

### 外键约束
- 所有关联表都设置外键约束
- 支持级联删除和更新
- 高并发场景可考虑移除，通过应用层保证

### 检查约束
- 价格字段非负约束
- 评分范围约束
- 库存非负约束
- 数量非负约束

---
*最后更新时间: 2025-06-18*
*维护者: 青柠檬*
