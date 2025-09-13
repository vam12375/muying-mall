# Elasticsearch 搜索功能文档

## 概述

本项目集成了 Elasticsearch 8.11.0 作为商品搜索引擎，提供高性能的全文搜索、智能推荐、搜索建议等功能。

## 功能特性

### 🔍 核心搜索功能
- **全文搜索**：支持中文分词，智能匹配商品名称、描述、分类、品牌等字段
- **多条件筛选**：支持分类、品牌、价格范围等多维度筛选
- **智能排序**：支持相关性、价格、销量、评分、时间等多种排序方式
- **搜索高亮**：搜索结果关键词高亮显示
- **分页查询**：高效的分页查询支持

### 🎯 智能推荐
- **搜索建议**：实时搜索建议和自动补全
- **热门搜索**：基于搜索统计的热门关键词推荐
- **相似商品**：基于商品属性的相似商品推荐
- **搜索聚合**：提供分类、品牌、价格区间等聚合统计

### 📊 搜索分析
- **搜索统计**：记录搜索关键词、结果数量、用户行为等
- **热门分析**：分析热门搜索词和搜索趋势
- **性能监控**：搜索响应时间和成功率监控
- **用户行为**：搜索点击率和转化率分析

## 技术架构

### 核心组件
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端搜索界面   │────│   搜索控制器     │────│   搜索服务层     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Redis缓存     │────│  Elasticsearch  │
                       └─────────────────┘    └─────────────────┘
                                                        │
                       ┌─────────────────┐    ┌─────────────────┐
                       │   MySQL数据库   │────│   搜索统计服务   │
                       └─────────────────┘    └─────────────────┘
```

### 数据流程
1. **索引同步**：商品数据从 MySQL 同步到 Elasticsearch
2. **搜索请求**：前端发送搜索请求到后端 API
3. **缓存查询**：优先从 Redis 缓存获取热门数据
4. **ES查询**：执行 Elasticsearch 复杂查询
5. **结果处理**：处理搜索结果并返回给前端
6. **统计记录**：异步记录搜索统计数据

## API 接口

### 用户搜索接口

#### 1. 搜索商品
```http
GET /search/products
```

**参数：**
- `keyword` (string, optional): 搜索关键词
- `categoryId` (integer, optional): 分类ID
- `brandId` (integer, optional): 品牌ID
- `minPrice` (decimal, optional): 最低价格
- `maxPrice` (decimal, optional): 最高价格
- `sortBy` (string, optional): 排序字段 (relevance, price, sales, rating, createTime)
- `sortOrder` (string, optional): 排序方向 (asc, desc)
- `page` (integer, optional): 页码，从0开始
- `size` (integer, optional): 每页大小

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "productId": 1,
        "productName": "优质婴儿奶粉",
        "productPrice": 158.00,
        "productImage": "/images/product1.jpg",
        "salesCount": 1250,
        "rating": 4.8
      }
    ],
    "totalElements": 156,
    "totalPages": 16,
    "size": 10,
    "number": 0
  }
}
```

#### 2. 获取搜索建议
```http
GET /search/suggestions?keyword=奶&limit=10
```

#### 3. 获取热门搜索词
```http
GET /search/hot-keywords?limit=10
```

#### 4. 获取相似商品
```http
GET /search/similar/{productId}?limit=10
```

### 管理员接口

#### 1. 重建搜索索引
```http
POST /admin/search/index/rebuild
```

#### 2. 同步商品到索引
```http
POST /admin/search/sync/product/{productId}
```

#### 3. 批量同步商品
```http
POST /admin/search/sync/products/batch
Content-Type: application/json

[1, 2, 3, 4, 5]
```

#### 4. 检查索引健康状态
```http
GET /admin/search/health
```

## 配置说明

### application.yml 配置
```yaml
spring:
  data:
    elasticsearch:
      uris: localhost:9200
      connection-timeout: 5s
      socket-timeout: 30s
      username: # 可选
      password: # 可选

management:
  health:
    elasticsearch:
      enabled: true
```

### 索引配置
- **索引名称**：products
- **分片数量**：1 (单机部署)
- **副本数量**：0 (单机部署)
- **分词器**：ik_max_word (索引), ik_smart (搜索)

## 部署指南

### 1. 安装 Elasticsearch
```bash
# 使用 Docker 部署
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  elasticsearch:8.11.0
```

### 2. 安装 IK 分词器 (可选)
```bash
# 进入容器
docker exec -it elasticsearch bash

# 安装 IK 分词器
./bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v8.11.0/elasticsearch-analysis-ik-8.11.0.zip

# 重启容器
docker restart elasticsearch
```

### 3. 初始化索引
```bash
# 启动应用后，调用管理接口创建索引
curl -X POST "http://localhost:8080/admin/search/index/create"

# 同步商品数据
curl -X POST "http://localhost:8080/admin/search/index/rebuild"
```

## 性能优化

### 1. 索引优化
- 合理设置分片和副本数量
- 定期执行索引优化操作
- 使用索引模板管理映射

### 2. 查询优化
- 使用缓存减少重复查询
- 合理设置查询超时时间
- 优化查询条件和排序

### 3. 监控告警
- 监控 Elasticsearch 集群状态
- 监控搜索响应时间
- 设置异常告警机制

## 故障排除

### 常见问题

1. **Elasticsearch 连接失败**
   - 检查 ES 服务是否启动
   - 验证网络连接和端口
   - 查看应用日志错误信息

2. **搜索结果为空**
   - 检查索引是否存在
   - 验证商品数据是否同步
   - 检查查询条件是否正确

3. **搜索性能慢**
   - 检查 ES 集群资源使用情况
   - 优化查询语句
   - 增加缓存策略

### 日志查看
```bash
# 查看应用日志
tail -f logs/muying-mall.log | grep -i elasticsearch

# 查看 ES 日志
docker logs elasticsearch
```

## 扩展功能

### 1. 搜索推荐算法
- 基于用户行为的个性化推荐
- 协同过滤推荐算法
- 机器学习模型集成

### 2. 搜索分析
- 搜索漏斗分析
- A/B 测试支持
- 实时搜索监控

### 3. 多语言支持
- 国际化搜索
- 多语言分词器
- 跨语言搜索

## 联系支持

如有问题或建议，请联系开发团队或提交 Issue。
