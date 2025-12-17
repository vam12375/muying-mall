#!/bin/bash

# ============================================
# Elasticsearch 索引初始化和优化脚本
# 用途：创建优化的商品搜索索引
# ============================================

ES_HOST="${ES_HOST:-localhost:9200}"
INDEX_NAME="products"

echo "=========================================="
echo "Elasticsearch 索引优化脚本"
echo "=========================================="
echo "ES 地址: $ES_HOST"
echo "索引名称: $INDEX_NAME"
echo ""

# 检查 ES 是否可用
echo "[1/5] 检查 Elasticsearch 连接..."
if ! curl -s "$ES_HOST" > /dev/null; then
    echo "✗ 无法连接到 Elasticsearch: $ES_HOST"
    exit 1
fi
echo "✓ Elasticsearch 连接正常"
echo ""

# 检查索引是否存在
echo "[2/5] 检查索引是否存在..."
if curl -s -o /dev/null -w "%{http_code}" "$ES_HOST/$INDEX_NAME" | grep -q "200"; then
    echo "⚠ 索引已存在，是否删除并重建？(y/n)"
    read -r response
    if [[ "$response" == "y" ]]; then
        echo "删除现有索引..."
        curl -X DELETE "$ES_HOST/$INDEX_NAME"
        echo ""
    else
        echo "跳过索引创建"
        exit 0
    fi
fi
echo ""

# 创建优化的索引
echo "[3/5] 创建优化的索引配置..."
curl -X PUT "$ES_HOST/$INDEX_NAME" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "refresh_interval": "30s",
    "max_result_window": 10000,
    "analysis": {
      "analyzer": {
        "ik_smart_pinyin": {
          "type": "custom",
          "tokenizer": "ik_smart",
          "filter": ["lowercase"]
        },
        "ik_max_word_pinyin": {
          "type": "custom",
          "tokenizer": "ik_max_word",
          "filter": ["lowercase"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "productId": {
        "type": "integer"
      },
      "productName": {
        "type": "text",
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "productDetail": {
        "type": "text",
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart"
      },
      "productPrice": {
        "type": "scaled_float",
        "scaling_factor": 100
      },
      "categoryId": {
        "type": "integer"
      },
      "categoryName": {
        "type": "text",
        "analyzer": "ik_smart",
        "fields": {
          "keyword": {
            "type": "keyword"
          }
        }
      },
      "brandId": {
        "type": "integer"
      },
      "brandName": {
        "type": "text",
        "analyzer": "ik_smart",
        "fields": {
          "keyword": {
            "type": "keyword"
          }
        }
      },
      "keywords": {
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "productStatus": {
        "type": "keyword"
      },
      "salesCount": {
        "type": "integer"
      },
      "rating": {
        "type": "half_float"
      },
      "searchWeight": {
        "type": "integer"
      },
      "createTime": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      },
      "updateTime": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      }
    }
  }
}'
echo ""
echo "✓ 索引创建成功"
echo ""

# 验证索引配置
echo "[4/5] 验证索引配置..."
curl -X GET "$ES_HOST/$INDEX_NAME/_settings?pretty"
echo ""

# 显示索引映射
echo "[5/5] 显示索引映射..."
curl -X GET "$ES_HOST/$INDEX_NAME/_mapping?pretty"
echo ""

echo "=========================================="
echo "✓ 索引优化完成！"
echo "=========================================="
echo ""
echo "下一步操作："
echo "1. 在应用中调用 rebuildSearchIndex() 方法同步数据"
echo "2. 或使用 API: POST /api/admin/search/rebuild"
echo ""
