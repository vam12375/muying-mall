-- 检查商品120的基本信息
SELECT 
    product_id,
    product_name,
    has_sku,
    min_price,
    max_price,
    stock,
    product_status
FROM product
WHERE product_id = 120;

-- 检查商品120的SKU列表
SELECT 
    sku_id,
    product_id,
    sku_code,
    sku_name,
    spec_values,
    price,
    stock,
    status,
    sku_image
FROM product_sku
WHERE product_id = 120
ORDER BY sku_id;

-- 统计商品120的SKU数量
SELECT 
    COUNT(*) as total_sku_count,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as enabled_sku_count,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as disabled_sku_count
FROM product_sku
WHERE product_id = 120;
