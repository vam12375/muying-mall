-- 修复商品120的SKU数据

-- 删除格式错误的SKU
DELETE FROM product_sku WHERE product_id = 120;

-- 重新插入正确格式的SKU数据
INSERT INTO product_sku (product_id, sku_name, sku_code, spec_values, price, stock, sku_image, weight, status, sort_order, create_time, update_time)
VALUES
(120, 'NB码', 'BY120-NB', '{"规格":"NB码"}', 189.00, 198, 'goods120.jpg', 2.00, 1, 1, NOW(), NOW());
