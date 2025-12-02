-- =====================================================
-- 更新商品的has_sku字段
-- 说明: 将所有在product_sku表中有SKU记录的商品的has_sku字段设置为1
-- =====================================================

-- 更新所有有SKU的商品
UPDATE product p
SET p.has_sku = 1
WHERE p.product_id IN (
    SELECT DISTINCT ps.product_id 
    FROM product_sku ps 
    WHERE ps.status = 1
);

-- 验证更新结果
SELECT p.product_id, p.product_name, p.has_sku, 
       (SELECT COUNT(*) FROM product_sku ps WHERE ps.product_id = p.product_id AND ps.status = 1) as sku_count
FROM product p
WHERE p.has_sku = 1;
