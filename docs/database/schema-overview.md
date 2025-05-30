# 母婴商城数据库概览

## 数据库设计理念

母婴商城系统数据库设计遵循以下核心原则：

1. **规范化设计**：采用第三范式(3NF)设计，减少数据冗余，避免更新异常
2. **业务模块化**：根据业务领域划分表格，提高模块内聚性
3. **性能优化**：为高频查询设计适当索引，关键表使用合理的分区策略
4. **数据安全**：敏感数据加密存储，应用审计机制
5. **可扩展性**：预留扩展字段，支持未来业务变化

## 数据库版本

- MySQL 8.0+
- 字符集：UTF-8MB4
- 排序规则：utf8mb4_0900_ai_ci
- 存储引擎：InnoDB（支持事务、外键约束）

## 数据库主要模块

数据库设计按照以下业务模块进行划分：

1. **用户模块**：用户账户、地址、积分等用户相关信息
2. **商品模块**：商品信息、分类、品牌、规格等
3. **订单模块**：订单信息、订单商品、支付记录等
4. **购物车模块**：用户购物车记录
5. **促销模块**：优惠券、活动规则等
6. **评价模块**：商品评价、回复等
7. **物流模块**：物流公司、配送信息等
8. **退款模块**：退款申请、退款记录等

## 核心表结构

### 用户相关表

| 表名 | 说明 | 主要字段 | 关联关系 |
|------|------|---------|---------|
| `user` | 用户基本信息表 | user_id, username, password(加密), email, mobile, status... | 主表 |
| `user_address` | 用户地址表 | address_id, user_id, receiver, phone, province, city... | 关联user表 |
| `user_points` | 用户积分表 | id, user_id, points_balance, update_time... | 关联user表 |
| `user_coupon` | 用户优惠券表 | id, user_id, coupon_id, status, expire_time... | 关联user和coupon表 |
| `user_message` | 用户消息表 | id, user_id, message_type, title, content, is_read... | 关联user表 |

### 商品相关表

| 表名 | 说明 | 主要字段 | 关联关系 |
|------|------|---------|---------|
| `product` | 商品表 | product_id, name, description, price, stock, status... | 主表 |
| `product_image` | 商品图片表 | id, product_id, image_url, sort_order... | 关联product表 |
| `product_specs` | 商品规格表 | id, product_id, specs_json, stock, price... | 关联product表 |
| `category` | 分类表 | category_id, parent_id, name, sort_order... | 自关联(parent_id) |
| `brand` | 品牌表 | brand_id, name, logo, description... | 被product表关联 |

### 订单相关表

| 表名 | 说明 | 主要字段 | 关联关系 |
|------|------|---------|---------|
| `order` | 订单表 | order_id, user_id, order_no, total_amount, status... | 关联user表 |
| `order_product` | 订单商品表 | id, order_id, product_id, quantity, price... | 关联order和product表 |
| `payment` | 支付记录表 | payment_id, order_id, payment_type, amount, status... | 关联order表 |
| `logistics` | 物流信息表 | logistics_id, order_id, logistics_company, logistics_no... | 关联order表 |
| `refund` | 退款表 | refund_id, order_id, amount, status, reason... | 关联order表 |

### 购物车相关表

| 表名 | 说明 | 主要字段 | 关联关系 |
|------|------|---------|---------|
| `cart` | 购物车表 | cart_id, user_id, product_id, quantity, selected, specs... | 关联user和product表 |

### 促销相关表

| 表名 | 说明 | 主要字段 | 关联关系 |
|------|------|---------|---------|
| `coupon` | 优惠券表 | id, name, type, value, min_spend, status... | 主表 |
| `coupon_rule` | 优惠券规则表 | rule_id, name, type, rule_content... | 被coupon关联 |
| `coupon_batch` | 优惠券批次表 | batch_id, coupon_name, rule_id, total_count... | 关联coupon_rule表 |
| `points_rule` | 积分规则表 | rule_id, name, type, points, rule_content... | 主表 |
| `points_product` | 积分商品表 | id, product_name, image, points_price, stock... | 主表 |
| `points_exchange` | 积分兑换记录表 | id, user_id, points_cost, exchange_type... | 关联user表 |
| `points_history` | 积分历史表 | id, user_id, points, change_type, description... | 关联user表 |

### 评价相关表

| 表名 | 说明 | 主要字段 | 关联关系 |
|------|------|---------|---------|
| `comment` | 评价表 | comment_id, user_id, product_id, order_id, content, rating... | 关联user、product和order表 |

### 物流相关表

| 表名 | 说明 | 主要字段 | 关联关系 |
|------|------|---------|---------|
| `logistics_company` | 物流公司表 | company_id, name, code, website, status... | 被logistics表关联 |
| `logistics_track` | 物流轨迹表 | id, logistics_id, status, location, track_time... | 关联logistics表 |

## 数据库关系图

ER图展示了表之间的关联关系，详见：[ER图](./er-diagram.png)

## 索引设计

系统对高频查询字段设置了索引，主要包括：

1. **主键索引**：所有表的主键
2. **外键索引**：关联字段（如user_id, product_id, order_id等）
3. **复合索引**：常用筛选条件组合（如status+create_time）
4. **全文索引**：商品名称和描述字段

## 触发器和存储过程

系统利用触发器和存储过程实现部分业务逻辑：

1. **购物车清理存储过程**：`proc_clean_expired_cart_items`
2. **购物车规格哈希触发器**：在插入和更新购物车时自动生成规格哈希值
3. **优惠券过期自动更新触发器**：自动更新过期的优惠券状态

## 数据库安全设计

1. **密码加密**：用户密码采用BCrypt算法加密存储
2. **敏感数据脱敏**：手机号、邮箱等敏感信息查询时自动脱敏
3. **操作审计**：关键操作记录用户、时间、操作内容等信息
4. **权限控制**：应用使用最小权限原则的数据库账号

## 性能优化策略

1. **表分区**：大表（如订单表）按时间范围分区
2. **冷热数据分离**：历史订单等冷数据单独存储
3. **索引优化**：根据查询模式优化索引结构
4. **定期维护**：定期更新统计信息，优化表结构

## 数据库维护计划

1. **备份策略**：
   - 每日全量备份
   - 实时binlog备份
   - 定期备份验证

2. **清理策略**：
   - 购物车数据：30天未更新自动清理
   - 日志数据：保留90天
   - 订单数据：永久保存，但冷数据归档

3. **扩展计划**：
   - 读写分离：主库写入，从库读取
   - 分库分表：当数据量增长到阈值时实施 