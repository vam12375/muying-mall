#!/bin/bash
# 清理Redis缓存脚本

echo "正在连接Redis并清理缓存..."

# 清理商品相关缓存
redis-cli DEL "product:detail:120"
redis-cli DEL "product:skus:120"

# 清理所有商品详情缓存（可选）
redis-cli --scan --pattern "product:detail:*" | xargs -L 1 redis-cli DEL
redis-cli --scan --pattern "product:skus:*" | xargs -L 1 redis-cli DEL

echo "Redis缓存清理完成！"
