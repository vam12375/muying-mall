#!/bin/bash
# 清理商品详情缓存脚本

echo "正在清理Redis中的商品详情缓存..."

# 连接Redis并删除所有商品详情缓存
redis-cli KEYS "product:detail:*" | xargs redis-cli DEL

echo "商品详情缓存已清理完成！"
