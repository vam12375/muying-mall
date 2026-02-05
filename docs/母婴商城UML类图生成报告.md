# 母婴商城核心 UML 类图生成报告

## 概述

本文档记录了母婴商城系统核心 UML 类图的生成过程和设计说明。该类图采用标准的三格形式（类名、属性、方法），字体大小为 18px，清晰展示了系统核心实体及其关系。

## 类图文件

- **文件路径**: `muying-mall/docs/diagrams/core-uml-class-diagram.drawio`
- **生成时间**: 2026-01-19
- **工具**: DrawIO MCP Server

## 核心实体类

### 1. 用户模块 (蓝色/绿色)

#### User (用户)
- **主键**: user_id: Long
- **核心属性**: 
  - username, password, nickname
  - email, phone, avatar
  - gender, birthday
  - status, role
- **关系**: 
  - 1:1 → UserAccount (账户)
  - 1:n → UserAddress (地址)
  - 1:n → Cart (购物车)
  - 1:n → Order (订单)
  - 1:n → UserCoupon (用户优惠券)

#### UserAccount (用户账户)
- **主键**: account_id: Long
- **外键**: user_id: Long
- **核心属性**: 
  - balance, frozen_balance (余额管理)
  - points (积分)
  - pay_password (支付密码)
  - status (账户状态)

#### UserAddress (用户地址)
- **主键**: address_id: Long
- **外键**: user_id: Long
- **核心属性**: 
  - receiver, phone (收货人信息)
  - province, city, district, detail (地址信息)
  - is_default (默认地址标识)

### 2. 商品模块 (黄色)

#### Product (商品)
- **主键**: product_id: Long
- **外键**: category_id, brand_id
- **核心属性**: 
  - product_name, product_img
  - price_new, price_old (价格)
  - stock, sales (库存和销量)
  - rating (评分)
  - status, has_sku (状态和SKU标识)
- **关系**: 
  - n:1 → Category (分类)
  - n:1 → Brand (品牌)
  - 1:n → ProductSku (SKU)

#### ProductSku (商品SKU)
- **主键**: sku_id: Long
- **外键**: product_id: Long
- **核心属性**: 
  - sku_code, sku_name
  - spec_values (规格值JSON)
  - price, stock
  - status

#### Category (分类)
- **主键**: category_id: Long
- **核心属性**: 
  - parent_id (父分类)
  - name, icon
  - sort_order, status

#### Brand (品牌)
- **主键**: brand_id: Long
- **核心属性**: 
  - name, logo
  - description
  - status

### 3. 购物车模块 (紫色)

#### Cart (购物车)
- **主键**: cart_id: Long
- **外键**: user_id, product_id, sku_id
- **核心属性**: 
  - quantity (数量)
  - selected (是否选中)
  - specs (规格JSON)
  - status
- **关系**: 
  - n:1 → User (用户)
  - n:1 → Product (商品)

### 4. 订单模块 (红色)

#### Order (订单)
- **主键**: order_id: Long
- **外键**: user_id, payment_id, address_id, coupon_id
- **核心属性**: 
  - order_no (订单号)
  - total_amount, actual_amount (金额)
  - status (订单状态)
  - receiver_name, receiver_phone, receiver_address (收货信息)
  - points_used (使用积分)
- **关系**: 
  - n:1 → User (用户)
  - 1:n → OrderProduct (订单商品)
  - 1:0..1 → Payment (支付)
  - 1:0..1 → Logistics (物流)

#### OrderProduct (订单商品)
- **主键**: id: Long
- **外键**: order_id, product_id
- **核心属性**: 
  - product_name, product_img
  - price, quantity
  - specs (规格JSON)
  - sku_id

#### Payment (支付)
- **主键**: id: Long
- **外键**: order_id, user_id
- **核心属性**: 
  - payment_no (支付单号)
  - amount (金额)
  - payment_method (支付方式)
  - status (支付状态)
  - transaction_id (第三方交易号)

#### Logistics (物流)
- **主键**: id: Long
- **外键**: order_id, company_id
- **核心属性**: 
  - tracking_no (物流单号)
  - status (物流状态)
  - receiver_name, receiver_phone, receiver_address (收货信息)

### 5. 营销模块 (橙色)

#### Coupon (优惠券)
- **主键**: id: Long
- **核心属性**: 
  - name, type
  - value (面值)
  - min_spend (最低消费)
  - status
  - start_time, end_time (有效期)
- **关系**: 
  - 1:n → UserCoupon (用户优惠券)

#### UserCoupon (用户优惠券)
- **主键**: id: Long
- **外键**: user_id, coupon_id
- **核心属性**: 
  - status (使用状态)
  - use_time (使用时间)
  - order_id (关联订单)
  - receive_time, expire_time (领取和过期时间)

## 关系说明

### 一对一关系 (1:1)
- User ↔ UserAccount: 每个用户有且仅有一个账户

### 一对多关系 (1:n)
- User → UserAddress: 一个用户可以有多个收货地址
- User → Cart: 一个用户可以有多个购物车项
- User → Order: 一个用户可以有多个订单
- User → UserCoupon: 一个用户可以领取多个优惠券
- Product → ProductSku: 一个商品可以有多个SKU
- Category → Product: 一个分类下可以有多个商品
- Brand → Product: 一个品牌下可以有多个商品
- Order → OrderProduct: 一个订单包含多个商品
- Coupon → UserCoupon: 一个优惠券可以被多个用户领取

### 多对一关系 (n:1)
- Cart → Product: 多个购物车项可以指向同一个商品
- Cart → User: 多个购物车项属于同一个用户

### 可选关系 (0..1)
- Order → Payment: 订单可能有支付记录（待付款订单没有）
- Order → Logistics: 订单可能有物流记录（未发货订单没有）

## 设计特点

### 1. 颜色编码
- **蓝色/绿色**: 用户相关模块
- **黄色**: 商品相关模块
- **紫色**: 购物车模块
- **红色**: 订单相关模块
- **橙色**: 营销模块

### 2. 标准三格形式
每个类都包含：
- **第一格**: 类名（中英文）
- **第二格**: 属性列表（字段名: 类型 [约束]）
- **第三格**: 方法（实体类通常省略 getter/setter）

### 3. 关系表示
- 使用 UML 标准关系线
- 明确标注基数（1, 0..1, 0..*, 1..*）
- 使用外键（FK）和主键（PK）标注

### 4. 字体规范
- 统一使用 18px 字体大小
- 确保可读性和专业性

## 技术实现

### 数据来源
- 基于 `muying_mall.sql` 数据库表结构
- 提取核心业务实体
- 保留关键字段和关系

### 生成工具
- DrawIO MCP Server
- 自动化生成 XML 格式
- 支持实时预览和编辑

## 使用说明

### 查看类图
1. 使用 DrawIO 桌面版或在线版打开 `core-uml-class-diagram.drawio`
2. 或使用 VSCode 的 DrawIO 插件查看

### 编辑类图
1. 在 DrawIO 中打开文件
2. 可以调整布局、添加注释
3. 保存后可继续使用

### 导出格式
- 支持导出为 PNG、SVG、PDF 等格式
- 适用于文档、演示等场景

## 后续优化建议

1. **添加方法层**: 可以为关键类添加业务方法
2. **细化关系**: 添加关联类的详细说明
3. **分模块展示**: 可以按业务模块拆分为多个子图
4. **添加注释**: 为复杂关系添加说明文字
5. **状态图补充**: 为订单、支付等添加状态机图

## 总结

本 UML 类图完整展示了母婴商城系统的核心实体结构，包括：
- 14 个核心实体类
- 清晰的关系定义
- 标准的 UML 表示法
- 专业的视觉呈现

该类图可作为系统设计文档的重要组成部分，帮助开发团队理解系统架构和数据模型。

---

**生成日期**: 2026-01-19  
**工具版本**: DrawIO MCP Server  
**文档版本**: v1.0
