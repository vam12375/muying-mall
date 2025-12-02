-- 验证数据库中的has_sku字段
SELECT 
    product_id,
    product_name,
    has_sku,
    min_price,
    max_price,
    product_status
FROM product
WHERE product_id IN (120, 1, 2, 3, 4, 5)
ORDER BY product_id;

-- 统计has_sku的分布
SELECT 
    has_sku,
    COUNT(*) as count,
    GROUP_CONCAT(product_id) as product_ids
FROM product
GROUP BY has_sku;
