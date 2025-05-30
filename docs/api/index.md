# 母婴商城API文档概览

## API概述

母婴商城系统提供了一套完整的RESTful API，用于支持前端应用与后端服务的通信。API采用标准化设计，提供了丰富的功能支持，包括用户管理、商品浏览、订单处理、支付集成等核心功能。

## API设计原则

母婴商城API设计遵循以下核心原则：

1. **RESTful架构**：基于资源的设计，使用标准HTTP方法表示操作
2. **版本化管理**：所有API都包含版本信息，确保向后兼容
3. **标准化响应**：统一的返回结构和状态码
4. **安全性**：接口认证与授权，数据加密传输
5. **性能优化**：支持缓存、分页、字段过滤等机制
6. **文档完善**：详细且保持更新的API文档

## API访问地址

- **测试环境**：https://api-test.muyingmall.com/api/v1
- **预发环境**：https://api-pre.muyingmall.com/api/v1
- **生产环境**：https://api.muyingmall.com/api/v1

## API版本管理

母婴商城API采用URI中的版本号标识API版本，例如`/api/v1/users`表示用户相关API的v1版本。

现有版本说明：
- **v1**：当前稳定版本，包含所有基础功能
- **v2**：开发中，增强版本，包含高级搜索和个性化推荐功能

## 认证与授权

### 认证方式

母婴商城API支持以下认证方式：

1. **JWT令牌认证（主要方式）**
   - 客户端通过登录接口获取JWT令牌
   - 后续请求通过Authorization头部传递令牌: `Authorization: Bearer <token>`
   - 令牌默认有效期为2小时

2. **刷新令牌机制**
   - 登录时同时获取refresh_token
   - 当访问令牌过期时，可使用refresh_token获取新的访问令牌
   - refresh_token默认有效期为30天

### 权限控制

系统采用基于角色的访问控制(RBAC)：

- **游客**：可访问公开信息，如商品列表、商品详情
- **注册用户**：可管理个人信息、下单、支付等
- **商户**：可管理自己的商品、订单等
- **管理员**：可访问管理后台，进行系统配置

## 通用请求格式

### 通用请求头

| 头部名称 | 是否必须 | 说明 |
|---------|--------|------|
| Content-Type | 是 | 请求体格式，通常为application/json |
| Authorization | 否* | 身份验证令牌，格式为"Bearer {token}"，*需要认证的接口必须提供 |
| User-Agent | 是 | 客户端信息 |
| Accept-Language | 否 | 期望的响应语言，默认zh-CN |
| X-Request-ID | 否 | 请求唯一标识，用于跟踪和调试 |

### 通用查询参数

对于列表类API，支持以下通用查询参数：

| 参数名 | 类型 | 说明 | 示例 |
|-------|------|-----|------|
| page | Integer | 页码，默认1 | ?page=2 |
| size | Integer | 每页条数，默认20，最大100 | ?size=50 |
| sort | String | 排序字段和方向，格式为"字段名,asc\|desc" | ?sort=createTime,desc |
| fields | String | 需要返回的字段，多个字段用逗号分隔 | ?fields=id,name,price |

## 通用响应格式

所有API响应均使用统一的JSON格式：

```json
{
  "code": 200,          // 业务状态码
  "message": "success", // 状态描述
  "data": { ... },      // 业务数据
  "timestamp": 1673356800123, // 响应时间戳
  "requestId": "req-abc123" // 请求唯一标识
}
```

### 业务状态码

| 状态码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或认证过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

## 分页响应格式

列表类API的分页响应格式如下：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      { ... }, // 数据项
      { ... }
    ],
    "pageable": {
      "pageNumber": 1,   // 当前页码
      "pageSize": 20,    // 每页条数
      "sort": [
        {
          "direction": "DESC",
          "property": "createTime"
        }
      ]
    },
    "totalElements": 85, // 总记录数
    "totalPages": 5      // 总页数
  },
  "timestamp": 1673356800123,
  "requestId": "req-abc123"
}
```

## 错误响应格式

当出现错误时，API返回统一的错误响应格式：

```json
{
  "code": 400,
  "message": "参数验证失败",
  "data": {
    "errors": [
      {
        "field": "email",
        "message": "邮箱格式不正确"
      },
      {
        "field": "password",
        "message": "密码长度不能小于6位"
      }
    ]
  },
  "timestamp": 1673356800123,
  "requestId": "req-abc123"
}
```

## API模块

母婴商城API分为以下几个主要模块，详情请查看对应文档：

1. [认证API](./auth.md) - 用户注册、登录、密码重置等
2. [用户API](./user.md) - 用户信息管理、地址管理等
3. [商品API](./product.md) - 商品列表、详情、搜索等
4. [分类API](./category.md) - 分类列表、详情等
5. [购物车API](./cart.md) - 购物车管理
6. [订单API](./order.md) - 订单创建、查询、取消等
7. [支付API](./payment.md) - 支付创建、回调等
8. [优惠券API](./coupon.md) - 优惠券获取、使用等
9. [评价API](./comment.md) - 商品评价管理
10. [搜索API](./search.md) - 高级搜索功能
11. [积分API](./points.md) - 积分获取、兑换等

## API使用示例

### 示例1：登录

**请求**:
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "yourpassword"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200,
    "tokenType": "Bearer",
    "user": {
      "id": 10001,
      "username": "user@example.com",
      "nickname": "测试用户",
      "avatar": "https://cdn.muyingmall.com/avatars/default.png",
      "roles": ["USER"]
    }
  },
  "timestamp": 1673356800123,
  "requestId": "req-abc123"
}
```

### 示例2：获取商品列表

**请求**:
```http
GET /api/v1/products?category=1&page=1&size=20&sort=createTime,desc
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1001,
        "name": "爱他美卓萃幼儿配方奶粉3段",
        "brief": "德国原装进口，适合1-3岁宝宝",
        "price": 335.0,
        "originalPrice": 365.0,
        "thumbnail": "https://cdn.muyingmall.com/products/1001/thumb.jpg",
        "sales": 3280,
        "rating": 4.9
      },
      // 更多商品...
    ],
    "pageable": {
      "pageNumber": 1,
      "pageSize": 20,
      "sort": [
        {
          "direction": "DESC",
          "property": "createTime"
        }
      ]
    },
    "totalElements": 85,
    "totalPages": 5
  },
  "timestamp": 1673356800123,
  "requestId": "req-abc123"
}
```

## API限流策略

为保障系统稳定性，API实施了以下限流策略：

- **未登录用户**：60次/分钟/IP
- **登录用户**：300次/分钟/用户
- **特殊接口**：如登录接口限制为10次/分钟/IP

超过限制将返回429状态码，请合理控制请求频率。

## 开发者工具

1. **Swagger UI**：访问 https://api-test.muyingmall.com/api/swagger-ui.html 获取交互式API文档
2. **Postman集合**：[下载Postman集合](../assets/muyingmall-api.postman_collection.json)

## 变更日志

| 日期 | 版本 | 变更内容 |
|-----|------|---------|
| 2023-01-10 | v1.0.0 | 初始版本发布 |
| 2023-03-15 | v1.0.1 | 新增积分API |
| 2023-05-20 | v1.1.0 | 优化商品搜索API，增加筛选功能 |
| 2023-08-05 | v1.2.0 | 新增支付宝小程序支付方式 |

## 注意事项

1. 生产环境的API必须通过HTTPS访问
2. 请妥善保管你的API访问凭证，不要在客户端存储敏感信息
3. 建议实现Token自动刷新机制，提升用户体验
4. API请求中包含敏感数据时，建议进行加密处理 