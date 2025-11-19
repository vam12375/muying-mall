-- 性能优化：数据库索引（安全版本）
-- Source: 性能优化 - 数据库索引优化
-- 遵循协议: AURA-X-KYS (KISS/YAGNI/SOLID)
-- 说明：此脚本会先删除已存在的索引，然后重新创建

-- 用户表索引
DROP INDEX IF EXISTS idx_user_username ON user;
DROP INDEX IF EXISTS idx_user_email ON user;
DROP INDEX IF EXISTS idx_user_status ON user;
DROP INDEX IF EXISTS idx_user_role ON user;
DROP INDEX IF EXISTS idx_user_create_time ON user;

CREATE INDEX idx_user_username ON user(username);
CREATE INDEX idx_user_email ON user(email);
CREATE INDEX idx_user_status ON user(status);
CREATE INDEX idx_user_role ON user(role);
CREATE INDEX idx_user_create_time ON user(create_time);

-- 商品表索引
DROP INDEX IF EXISTS idx_product_category ON product;
DROP INDEX IF EXISTS idx_product_brand ON product;
DROP INDEX IF EXISTS idx_product_status ON product;
DROP INDEX IF EXISTS idx_product_hot ON product;
DROP INDEX IF EXISTS idx_product_new ON product;
DROP INDEX IF EXISTS idx_product_recommend ON product;
DROP INDEX IF EXISTS idx_product_create_time ON product;
DROP INDEX IF EXISTS idx_product_name ON product;
DROP INDEX IF EXISTS idx_product_category_status ON product;
DROP INDEX IF EXISTS idx_product_brand_status ON product;

CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_brand ON product(brand_id);
CREATE INDEX idx_product_status ON product(product_status);
CREATE INDEX idx_product_hot ON product(is_hot);
CREATE INDEX idx_product_new ON product(is_new);
CREATE INDEX idx_product_recommend ON product(is_recommend);
CREATE INDEX idx_product_create_time ON product(create_time);
CREATE INDEX idx_product_name ON product(product_name);
CREATE INDEX idx_product_category_status ON product(category_id, product_status);
CREATE INDEX idx_product_brand_status ON product(brand_id, product_status);

-- 订单表索引
DROP INDEX IF EXISTS idx_order_user ON `order`;
DROP INDEX IF EXISTS idx_order_status ON `order`;
DROP INDEX IF EXISTS idx_order_create_time ON `order`;
DROP INDEX IF EXISTS idx_order_number ON `order`;
DROP INDEX IF EXISTS idx_order_user_status ON `order`;
DROP INDEX IF EXISTS idx_order_user_time ON `order`;

CREATE INDEX idx_order_user ON `order`(user_id);
CREATE INDEX idx_order_status ON `order`(order_status);
CREATE INDEX idx_order_create_time ON `order`(create_time);
CREATE INDEX idx_order_number ON `order`(order_number);
CREATE INDEX idx_order_user_status ON `order`(user_id, order_status);
CREATE INDEX idx_order_user_time ON `order`(user_id, create_time);

-- 品牌表索引
DROP INDEX IF EXISTS idx_brand_status ON brand;
DROP INDEX IF EXISTS idx_brand_name ON brand;
DROP INDEX IF EXISTS idx_brand_sort ON brand;

CREATE INDEX idx_brand_status ON brand(status);
CREATE INDEX idx_brand_name ON brand(name);
CREATE INDEX idx_brand_sort ON brand(sort_order);

-- 分类表索引
DROP INDEX IF EXISTS idx_category_parent ON category;
DROP INDEX IF EXISTS idx_category_status ON category;
DROP INDEX IF EXISTS idx_category_sort ON category;

CREATE INDEX idx_category_parent ON category(parent_id);
CREATE INDEX idx_category_status ON category(status);
CREATE INDEX idx_category_sort ON category(sort_order);

-- 积分历史表索引
DROP INDEX IF EXISTS idx_points_history_user ON points_history;
DROP INDEX IF EXISTS idx_points_history_type ON points_history;
DROP INDEX IF EXISTS idx_points_history_time ON points_history;

CREATE INDEX idx_points_history_user ON points_history(user_id);
CREATE INDEX idx_points_history_type ON points_history(operation_type);
CREATE INDEX idx_points_history_time ON points_history(create_time);

-- 用户账户表索引
DROP INDEX IF EXISTS idx_user_account_user ON user_account;
DROP INDEX IF EXISTS idx_user_account_status ON user_account;

CREATE INDEX idx_user_account_user ON user_account(user_id);
CREATE INDEX idx_user_account_status ON user_account(status);

-- 完成提示
SELECT '索引创建完成！' AS message;
