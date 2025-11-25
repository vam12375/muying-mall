# IP地址和地理位置查询功能指南

## 功能概述

本系统实现了完整的IP地址获取和地理位置查询功能，支持：
- 真实IP地址获取（考虑代理和负载均衡）
- 自动地理位置查询（使用免费API）
- 地理位置缓存（24小时）
- 内网和本地地址识别

## 技术实现

### 1. IP地址获取

系统会按以下优先级获取真实IP地址：
1. `X-Forwarded-For` 请求头
2. `Proxy-Client-IP` 请求头
3. `WL-Proxy-Client-IP` 请求头
4. `HTTP_X_FORWARDED_FOR` 请求头
5. `X-Real-IP` 请求头
6. `request.getRemoteAddr()`

**特殊处理：**
- IPv6本地回环地址（`0:0:0:0:0:0:0:1` 或 `::1`）会被转换为 `127.0.0.1`
- 如果检测到多个IP（逗号分隔），取第一个

### 2. 地理位置查询

系统使用两个免费API进行地理位置查询：

#### 主API：ip-api.com
- **限制**：45次/分钟
- **优点**：免费、无需API key、支持中文
- **URL**：`http://ip-api.com/json/{ip}?lang=zh-CN`

#### 备用API：ipapi.co
- **限制**：1000次/天
- **优点**：免费、HTTPS支持
- **URL**：`https://ipapi.co/{ip}/json/`

### 3. 缓存机制

- 查询结果会缓存24小时
- 使用 `ConcurrentHashMap` 存储
- 自动过期清理

## 使用方法

### 1. 在代码中使用

```java
import com.muyingmall.util.IpUtil;

// 获取真实IP
String ip = IpUtil.getRealIp(request);

// 获取地理位置
String location = IpUtil.getIpLocation(ip);

// 清理过期缓存
IpUtil.cleanExpiredCache();
```

### 2. 测试接口

#### 获取当前IP信息
```bash
GET /admin/ip-test/current
```

响应示例：
```json
{
  "success": true,
  "data": {
    "ip": "117.147.112.29",
    "location": "中国 浙江 杭州",
    "userAgent": "Mozilla/5.0...",
    "headers": {
      "X-Forwarded-For": "117.147.112.29",
      "User-Agent": "Mozilla/5.0..."
    }
  }
}
```

#### 查询指定IP地理位置
```bash
GET /admin/ip-test/query?ip=8.8.8.8
```

响应示例：
```json
{
  "success": true,
  "data": {
    "ip": "8.8.8.8",
    "location": "美国 加利福尼亚 山景城"
  }
}
```

#### 清理缓存
```bash
POST /admin/ip-test/clear-cache
```

## 地理位置格式

系统返回的地理位置格式为：`国家 省份/州 城市`

**示例：**
- 公网IP：`中国 浙江 杭州`
- 本地地址：`本地`
- 内网地址：`内网`
- 查询失败：`未知`

## 更新现有数据

如果数据库中已有登录记录，可以执行以下SQL更新地理位置：

```bash
# 连接到数据库
mysql -u root -p muying_mall

# 执行更新脚本
source src/main/resources/db/update_ip_locations.sql
```

或者直接执行：
```sql
-- 更新IPv6本地地址
UPDATE admin_login_records 
SET ip_address = '127.0.0.1' 
WHERE ip_address IN ('0:0:0:0:0:0:0:1', '::1');

-- 更新本地地址的地理位置
UPDATE admin_login_records 
SET location = '本地' 
WHERE ip_address = '127.0.0.1';

-- 更新内网地址的地理位置
UPDATE admin_login_records 
SET location = '内网' 
WHERE ip_address LIKE '192.168.%' OR ip_address LIKE '10.%';
```

## 性能优化

### 1. 缓存策略
- 地理位置查询结果缓存24小时
- 避免频繁调用外部API
- 减少网络延迟

### 2. 超时设置
- 连接超时：3秒
- 读取超时：3秒
- 避免长时间等待

### 3. 降级策略
- 主API失败时自动切换到备用API
- 所有API失败时返回"未知"
- 不影响主业务流程

## 常见问题

### Q1: 为什么显示 `0:0:0:0:0:0:0:1`？
**A:** 这是IPv6的本地回环地址，表示请求来自本机。系统会自动转换为 `127.0.0.1`。

### Q2: 为什么地理位置显示"未知"？
**A:** 可能的原因：
1. IP是内网地址或本地地址
2. API查询失败（网络问题）
3. API限流（超过请求限制）
4. IP地址格式不正确

### Q3: 如何提高查询成功率？
**A:** 
1. 确保服务器可以访问外网
2. 检查防火墙设置
3. 考虑使用付费API（更高限额）
4. 部署本地IP数据库（如GeoIP2）

### Q4: 如何使用本地IP数据库？
**A:** 可以集成以下库：
- **MaxMind GeoIP2**：需要下载数据库文件
- **IP2Location**：提供Java SDK
- **纯真IP数据库**：国内IP查询准确

## 生产环境建议

### 1. 使用本地IP数据库
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.maxmind.geoip2</groupId>
    <artifactId>geoip2</artifactId>
    <version>4.0.0</version>
</dependency>
```

### 2. 配置API密钥
如果使用付费API，在 `application.yml` 中配置：
```yaml
ip:
  api:
    key: your-api-key
    provider: ipapi  # 或 maxmind
```

### 3. 监控和告警
- 监控API调用成功率
- 设置缓存命中率告警
- 记录查询失败日志

## 相关文件

### 后端
- `IpUtil.java` - IP工具类
- `IpTestController.java` - 测试控制器
- `AdminLoginInterceptor.java` - 登录拦截器

### 数据库
- `update_ip_locations.sql` - 数据更新脚本

### 前端
- `LoginRecords.tsx` - 登录记录组件
- `OperationRecords.tsx` - 操作记录组件

## 参考资源

- [ip-api.com 文档](https://ip-api.com/docs)
- [ipapi.co 文档](https://ipapi.co/api/)
- [MaxMind GeoIP2](https://dev.maxmind.com/geoip/geoip2/downloadable/)
- [IP2Location](https://www.ip2location.com/)
