# API 路径配置指南

## 配置原则

### 后端配置
```yaml
# application.yml
server:
  servlet:
    context-path: /api
```

### 控制器路由规则
**❌ 错误写法**：
```java
@RestController
@RequestMapping("/api/brands")  // ❌ 不要在路由中包含 /api
public class BrandController {
}
```

**✅ 正确写法**：
```java
@RestController
@RequestMapping("/brands")  // ✅ 直接写资源路径
public class BrandController {
}
```

### 前端配置
```javascript
// request.js
const request = axios.create({
  baseURL: '/api',  // 统一前缀
  timeout: 10000
})
```

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
      // 不需要 rewrite
    }
  }
}
```

## 路径流程

### 示例：品牌列表接口

```
前端调用:
  getBrands() -> request.get('/brands/all')

前端实际请求:
  baseURL + path = /api/brands/all

Vite 代理:
  匹配 /api -> 转发到 http://localhost:8080/api/brands/all

后端接收:
  context-path: /api
  实际处理路径: /brands/all

Spring Boot 路由匹配:
  @RequestMapping("/brands") + @GetMapping("/all")
  = /brands/all ✅
```

## 常见错误

### 错误 1：路径重复
```
❌ 后端: @RequestMapping("/api/brands")
   context-path: /api
   实际路径: /api/api/brands
```

### 错误 2：前端路径错误
```
❌ 前端: request.get('/api/brands')
   baseURL: /api
   实际请求: /api/api/brands
```

### 错误 3：代理配置错误
```
❌ Vite 代理:
   rewrite: (path) => path.replace(/^\/api/, '/api')
   结果: /api/api/brands
```

## 检查清单

### 后端检查
- [ ] 所有控制器路由不包含 `/api` 前缀
- [ ] `application.yml` 中 `context-path: /api`
- [ ] 接口文档路径正确

### 前端检查
- [ ] `request.js` 中 `baseURL: '/api'`
- [ ] API 调用不包含 `/api` 前缀
- [ ] Vite 代理配置正确

## 控制器路由规范

### 用户端接口
```java
@RestController
@RequestMapping("/products")    // 商品
@RequestMapping("/orders")      // 订单
@RequestMapping("/cart")        // 购物车
@RequestMapping("/user")        // 用户
@RequestMapping("/brands")      // 品牌
@RequestMapping("/categories")  // 分类
```

### 管理端接口
```java
@RestController
@RequestMapping("/admin/products")   // 商品管理
@RequestMapping("/admin/orders")     // 订单管理
@RequestMapping("/admin/users")      // 用户管理
@RequestMapping("/admin/brands")     // 品牌管理
```

## 测试验证

### 1. 检查后端路由
```bash
# 启动后端，访问 Swagger
http://localhost:8080/api/swagger-ui.html

# 检查接口路径是否正确
# 应该是: /api/brands/all
# 而不是: /api/api/brands/all
```

### 2. 检查前端请求
```javascript
// 浏览器控制台
console.log('请求路径:', request.defaults.baseURL)
// 应该输出: /api

// Network 面板查看实际请求
// 应该是: /api/brands/all
```

### 3. 端到端测试
```bash
# 前端发起请求
curl http://localhost:5173/api/brands/all

# 应该成功返回品牌列表
```

---

**创建时间**: 2024-11-25  
**适用版本**: Spring Boot 3.x + Vue 3 + Vite  
**核心原则**: 统一路径管理，避免重复前缀
