-- ============================================
-- 修复超时订单未恢复的SKU库存
-- 执行前请先备份数据库！
-- ============================================

-- 1. 查看需要恢复的库存（订单307的扣减记录）
SELECT 
    pssl.log_id,
    pssl.sku_id,
    pssl.sku_code,
    pssl.order_id,
    pssl.change_type,
    pssl.change_quantity,
    pssl.before_stock,
    pssl.after_stock,
    pssl.remark,
    pssl.create_time,
    ps.stock AS current_stock,
    ps.sku_name
FROM product_sku_stock_log pssl
LEFT JOIN product_sku ps ON pssl.sku_id = ps.sku_id
WHERE pssl.order_id = 307
  AND pssl.change_type = 'DEDUCT'
  AND NOT EXISTS (
    -- 检查是否已有对应的RESTORE记录
    SELECT 1 FROM product_sku_stock_log pssl2
    WHERE pssl2.sku_id = pssl.sku_id
      AND pssl2.order_id = pssl.order_id
      AND pssl2.change_type = 'RESTORE'
  )
ORDER BY pssl.create_time;

-- 2. 恢复订单307的SKU库存
-- 根据上面查询结果，逐个恢复SKU库存

-- 恢复 SKU ID 374 (BY119-M-X) 的库存 +10
UPDATE product_sku 
SET stock = stock + 10,
    version = version + 1,
    update_time = NOW()
WHERE sku_id = 374;

-- 记录恢复日志
INSERT INTO product_sku_stock_log (
    sku_id, 
    sku_code, 
    order_id, 
    change_type, 
    change_quantity, 
    before_stock, 
    after_stock, 
    operator, 
    remark, 
    create_time
)
SELECT 
    374,
    'BY119-M-X',
    307,
    'RESTORE',
    10,
    stock - 10,
    stock,
    'admin',
    '手动修复：订单超时未恢复库存',
    NOW()
FROM product_sku
WHERE sku_id = 374;

-- 恢复 SKU ID 480 (BY120-S-X) 的库存 +10
UPDATE product_sku 
SET stock = stock + 10,
    version = version + 1,
    update_time = NOW()
WHERE sku_id = 480;

-- 记录恢复日志
INSERT INTO product_sku_stock_log (
    sku_id, 
    sku_code, 
    order_id, 
    change_type, 
    change_quantity, 
    before_stock, 
    after_stock, 
    operator, 
    remark, 
    create_time
)
SELECT 
    480,
    'BY120-S-X',
    307,
    'RESTORE',
    10,
    stock - 10,
    stock,
    'admin',
    '手动修复：订单超时未恢复库存',
    NOW()
FROM product_sku
WHERE sku_id = 480;

-- 3. 更新对应商品的总库存
-- 根据SKU所属的商品ID更新商品总库存

-- 更新商品总库存（假设SKU 374和480属于不同商品）
UPDATE product p
SET p.stock = (
    SELECT COALESCE(SUM(ps.stock), 0)
    FROM product_sku ps
    WHERE ps.product_id = p.product_id
      AND ps.status = 1
),
p.update_time = NOW()
WHERE p.product_id IN (
    SELECT DISTINCT ps.product_id
    FROM product_sku ps
    WHERE ps.sku_id IN (374, 480)
);

-- 4. 验证恢复结果
SELECT 
    ps.sku_id,
    ps.sku_code,
    ps.sku_name,
    ps.stock AS sku_stock,
    ps.product_id,
    p.product_name,
    p.stock AS product_total_stock
FROM product_sku ps
LEFT JOIN product p ON ps.product_id = p.product_id
WHERE ps.sku_id IN (374, 480);

-- 5. 查看恢复后的库存日志
SELECT 
    log_id,
    sku_id,
    sku_code,
    order_id,
    change_type,
    change_quantity,
    before_stock,
    after_stock,
    operator,
    remark,
    create_time
FROM product_sku_stock_log
WHERE order_id = 307
ORDER BY create_time DESC;

-- ============================================
-- 通用脚本：批量恢复所有超时订单的未恢复库存
-- ============================================

-- 查找所有超时取消但未恢复库存的订单
SELECT DISTINCT
    o.order_id,
    o.order_no,
    o.status,
    o.cancel_reason,
    COUNT(DISTINCT pssl.sku_id) AS affected_sku_count
FROM `order` o
INNER JOIN product_sku_stock_log pssl ON o.order_id = pssl.order_id
WHERE o.status = 'cancelled'
  AND o.cancel_reason = 'TIMEOUT'
  AND pssl.change_type = 'DEDUCT'
  AND NOT EXISTS (
    SELECT 1 FROM product_sku_stock_log pssl2
    WHERE pssl2.sku_id = pssl.sku_id
      AND pssl2.order_id = pssl.order_id
      AND pssl2.change_type = 'RESTORE'
  )
GROUP BY o.order_id, o.order_no, o.status, o.cancel_reason;

-- ============================================
-- 注意事项：
-- 1. 执行前务必备份数据库
-- 2. 先执行查询语句确认数据正确
-- 3. 根据实际的SKU ID和数量调整UPDATE语句
-- 4. 执行后验证库存是否正确恢复
-- 5. 检查商品总库存是否同步更新
-- ============================================
