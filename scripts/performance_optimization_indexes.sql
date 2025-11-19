-- 性能优化：数据库索引
-- Source: 性能优化 - 数据库索引优化
-- 遵循协议: AURA-X-KYS (KISS/YAGNI/SOLID)
-- 注意：如果索引已存在会报错，可以忽略或先删除再创建

-- 用户表索引
CREATE INDEX idx_user_username ON user(username);
CREATE INDEX idx_user_email ON user(email);
CREATE INDEX idx_user_status ON user(status);
CREATE INDEX idx_user_role ON user(role);
CREATE INDEX idx_user_create_time ON user(create_time);

-- 商品表索引
CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_brand ON product(brand_id);
CREATE INDEX idx_product_status ON product(product_status);
CREATE INDEX idx_product_hot ON product(is_hot);
CREATE INDEX idx_product_new ON product(is_new);
CREATE INDEX idx_product_recommend ON product(is_recommend);
CREATE INDEX idx_product_create_time ON product(create_time);
CREATE INDEX idx_product_name ON product(product_name);

-- 订单表索引
CREATE INDEX idx_order_user ON `order`(user_id);
CREATE INDEX idx_order_status ON `order`(order_status);
CREATE INDEX idx_order_create_time ON `order`(create_time);
CREATE INDEX idx_order_number ON `order`(order_number);

-- 品牌表索引
CREATE INDEX idx_brand_status ON brand(status);
CREATE INDEX idx_brand_name ON brand(name);
CREATE INDEX idx_brand_sort ON brand(sort_order);

-- 分类表索引
CREATE INDEX idx_category_parent ON category(parent_id);
CREATE INDEX idx_category_status ON category(status);
CREATE INDEX idx_category_sort ON category(sort_order);

-- 积分历史表索引
CREATE INDEX idx_points_history_user ON points_history(user_id);
CREATE INDEX idx_points_history_type ON points_history(operation_type);
CREATE INDEX idx_points_history_time ON points_history(create_time);

-- 用户账户表索引
CREATE INDEX idx_user_account_user ON user_account(user_id);
CREATE INDEX idx_user_account_status ON user_account(status);

-- 复合索引（高频查询组合）
CREATE INDEX idx_product_category_status ON product(category_id, product_status);
CREATE INDEX idx_product_brand_status ON product(brand_id, product_status);
CREATE INDEX idx_order_user_status ON `order`(user_id, order_status);
CREATE INDEX idx_order_user_time ON `order`(user_id, create_time);
