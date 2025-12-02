#!/bin/bash
# 测试SKU API

echo "=== 测试商品120的SKU接口 ==="
echo ""

# 获取token（需要先登录）
echo "1. 测试获取商品120的SKU列表"
curl -X GET "http://localhost:8080/products/120/skus" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "=== 测试完成 ==="
