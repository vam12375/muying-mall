-- ============================================
-- 用户统计测试数据插入脚本
-- 用途：为测试用户创建订单、收藏、评价数据
-- ============================================

-- 1. 查询测试用户的 ID
-- SELECT user_id, username FROM user WHERE username = 'test2';
-- 假设查询结果 user_id = 2

-- 2. 插入订单测试数据
INSERT INTO `order` (user_id, order_no, total_amount, order_status, payment_method, create_time, update_time) 
VALUES 
(2, 'TEST20241124001', 358.00, '已完成', '支付宝', NOW(), NOW()),
(2, 'TEST20241124002', 298.00, '已完成', '微信支付', NOW(), NOW()),
(2, 'TEST20241124003', 688.00, '待发货', '支付宝', NOW(), NOW()),
(2, 'TEST20241124004', 368.00, '已完成', '微信支付', NOW(), NOW()),
(2, 'TEST20241124005', 348.00, '待支付', '支付宝', NOW(), NOW());

-- 3. 插入收藏测试数据
INSERT INTO favorite (user_id, product_id, create_time) 
VALUES 
(2, 1, NOW()),
(2, 2, NOW()),
(2, 3, NOW()),
(2, 4, NOW()),
(2, 5, NOW()),
(2, 6, NOW()),
(2, 7, NOW()),
(2, 8, NOW());

-- 4. 插入评价测试数据
INSERT INTO comment (user_id, product_id, order_id, rating, content, is_anonymous, create_time, update_time) 
VALUES 
(2, 1, 1, 5, '惠氏启赋有机奶粉非常好，宝宝很喜欢喝，溶解度高，不上火。', 0, NOW(), NOW()),
(2, 2, 2, 5, '爱他美白金版奶粉品质很好，宝宝喝了消化很好，会继续购买。', 0, NOW(), NOW()),
(2, 3, 3, 4, '美赞臣蓝臻奶粉不错，就是价格有点贵，但是为了宝宝值得。', 0, NOW(), NOW()),
(2, 4, 4, 5, '诺优能奶粉很好，宝宝喝了长得很好，推荐购买。', 0, NOW(), NOW()),
(2, 5, 5, 4, '雅培菁智奶粉质量可以，宝宝接受度高。', 0, NOW(), NOW());

-- 5. 验证插入结果
SELECT 
    (SELECT COUNT(*) FROM `order` WHERE user_id = 2) as order_count,
    (SELECT COUNT(*) FROM favorite WHERE user_id = 2) as favorite_count,
    (SELECT COUNT(*) FROM comment WHERE user_id = 2) as comment_count;

-- 预期结果：
-- order_count: 5
-- favorite_count: 8
-- comment_count: 5

-- ============================================
-- 清理测试数据（如需要）
-- ============================================

-- DELETE FROM `order` WHERE user_id = 2 AND order_no LIKE 'TEST%';
-- DELETE FROM favorite WHERE user_id = 2;
-- DELETE FROM comment WHERE user_id = 2;
