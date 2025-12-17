-- ============================================
-- 母婴商城性能优化索引脚本
-- 执行前请先备份数据库
-- 使用存储过程安全创建索引（如果已存在则跳过）
-- ============================================

USE muying_mall;

-- 创建安全添加索引的存储过程
DROP PROCEDURE IF EXISTS safe_add_index;
DELIMITER //
CREATE PROCEDURE safe_add_index(
    IN table_name VARCHAR(64),
    IN index_name VARCHAR(64),
    IN column_name VARCHAR(64)
)
BEGIN
    DECLARE index_exists INT DEFAULT 0;
    
    SELECT COUNT(*) INTO index_exists
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = table_name
      AND index_name = index_name;
    
    IF index_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', table_name, '` ADD INDEX ', index_name, ' (', column_name, ') USING BTREE');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('Created index: ', index_name, ' on ', table_name) AS result;
    ELSE
        SELECT CONCAT('Index already exists: ', index_name, ' on ', table_name) AS result;
    END IF;
END //
DELIMITER ;

-- ============================================
-- 1. 用户表索引 (user)
-- ============================================
SELECT '正在创建用户表索引...' AS status;
CALL safe_add_index('user', 'idx_user_username', 'username');
CALL safe_add_index('user', 'idx_user_email', 'email');
CALL safe_add_index('user', 'idx_user_status', 'status');
CALL safe_add_index('user', 'idx_user_role', 'role');

-- ============================================
-- 2. 商品表索引 (product)
-- ============================================
SELECT '正在创建商品表索引...' AS status;
CALL safe_add_index('product', 'idx_product_status', 'product_status');
CALL safe_add_index('product', 'idx_product_category', 'category_id');
CALL safe_add_index('product', 'idx_product_brand', 'brand_id');
CALL safe_add_index('product', 'idx_product_hot', 'is_hot');
CALL safe_add_index('product', 'idx_product_new', 'is_new');
CALL safe_add_index('product', 'idx_product_recommend', 'is_recommend');
CALL safe_add_index('product', 'idx_product_create_time', 'create_time');

-- ============================================
-- 3. 评论表索引 (comment)
-- ============================================
SELECT '正在创建评论表索引...' AS status;
CALL safe_add_index('comment', 'idx_comment_product', 'product_id');
CALL safe_add_index('comment', 'idx_comment_user', 'user_id');
CALL safe_add_index('comment', 'idx_comment_order', 'order_id');
CALL safe_add_index('comment', 'idx_comment_status', 'status');
CALL safe_add_index('comment', 'idx_comment_create_time', 'create_time');

-- ============================================
-- 4. 购物车表索引 (cart)
-- ============================================
SELECT '正在创建购物车表索引...' AS status;
CALL safe_add_index('cart', 'idx_cart_user', 'user_id');
CALL safe_add_index('cart', 'idx_cart_product', 'product_id');
CALL safe_add_index('cart', 'idx_cart_status', 'status');

-- ============================================
-- 5. 订单表索引 (order)
-- ============================================
SELECT '正在创建订单表索引...' AS status;
CALL safe_add_index('order', 'idx_order_user', 'user_id');
CALL safe_add_index('order', 'idx_order_status', 'status');
CALL safe_add_index('order', 'idx_order_no', 'order_no');
CALL safe_add_index('order', 'idx_order_create_time', 'create_time');

-- ============================================
-- 6. 收藏表索引 (favorite)
-- ============================================
SELECT '正在创建收藏表索引...' AS status;
CALL safe_add_index('favorite', 'idx_favorite_user', 'user_id');
CALL safe_add_index('favorite', 'idx_favorite_product', 'product_id');

-- ============================================
-- 7. 用户积分表索引 (user_points)
-- ============================================
SELECT '正在创建用户积分表索引...' AS status;
CALL safe_add_index('user_points', 'idx_user_points_user', 'user_id');

-- ============================================
-- 8. 用户优惠券表索引 (user_coupon)
-- ============================================
SELECT '正在创建用户优惠券表索引...' AS status;
CALL safe_add_index('user_coupon', 'idx_user_coupon_user', 'user_id');
CALL safe_add_index('user_coupon', 'idx_user_coupon_status', 'status');

-- ============================================
-- 9. 用户账户表索引 (user_account)
-- ============================================
SELECT '正在创建用户账户表索引...' AS status;
CALL safe_add_index('user_account', 'idx_user_account_user', 'user_id');

-- 清理存储过程
DROP PROCEDURE IF EXISTS safe_add_index;

-- ============================================
-- 完成提示
-- ============================================
SELECT '所有索引创建完成！' AS status;
