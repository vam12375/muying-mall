-- 修复购物车表中的 specs_hash 字段
-- 问题：当有 sku_id 时，specs_hash 应该是 'sku_' + sku_id，而不是空字符串的 MD5

-- 1. 查看当前有问题的数据
SELECT cart_id, user_id, product_id, sku_id, sku_name, specs_hash 
FROM cart 
WHERE sku_id IS NOT NULL 
  AND (specs_hash IS NULL OR specs_hash = 'd41d8cd98f00b204e9800998ecf8427e' OR specs_hash NOT LIKE 'sku_%');

-- 2. 更新有 sku_id 但 specs_hash 不正确的记录
UPDATE cart 
SET specs_hash = CONCAT('sku_', sku_id)
WHERE sku_id IS NOT NULL 
  AND (specs_hash IS NULL OR specs_hash = 'd41d8cd98f00b204e9800998ecf8427e' OR specs_hash NOT LIKE 'sku_%');

-- 3. 验证修复结果
SELECT cart_id, user_id, product_id, sku_id, sku_name, specs_hash 
FROM cart 
WHERE sku_id IS NOT NULL;

-- 4. 如果有重复的记录（同一用户、同一商品、同一SKU），需要合并
-- 先查看是否有重复
SELECT user_id, product_id, sku_id, COUNT(*) as cnt
FROM cart
WHERE sku_id IS NOT NULL
GROUP BY user_id, product_id, sku_id
HAVING COUNT(*) > 1;

-- 5. 如果有重复，保留最新的一条，删除其他的
-- DELETE c1 FROM cart c1
-- INNER JOIN cart c2 
-- WHERE c1.user_id = c2.user_id 
--   AND c1.product_id = c2.product_id 
--   AND c1.sku_id = c2.sku_id 
--   AND c1.cart_id < c2.cart_id;
