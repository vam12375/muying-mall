# Muying Mall - 母婴商城后端API服务

母婴商城系统的后端API服务，基于Spring Boot构建的现代Java应用，为前台商城和后台管理系统提供强大的API支持。

## 技术栈

- **核心框架**: Spring Boot 3.2.0
- **Java版本**: Java 21
- **ORM框架**: MyBatis-Plus 3.5.9
- **数据库**: MySQL
- **缓存**: Redis (Spring Data Redis)
- **会话管理**: Spring Session
- **安全框架**: Spring Security
- **API文档**: SpringDoc OpenAPI 2.2.0
- **搜索引擎**: Elasticsearch 8.11.0
- **JSON处理**: 
  - FastJSON2 2.0.40
  - Jackson (jackson-datatype-jsr310)
- **支付集成**: 
  - 支付宝 SDK 4.38.0
  - 微信支付 SDK 0.0.3
- **认证授权**: JWT (JJWT 0.11.5)
- **构建工具**: Maven 3.8+

## 功能特性

- 用户认证与授权
  - 基于JWT的登录认证
  - 基于角色的权限控制
- 商品服务
  - 商品信息管理
  - 商品分类管理
  - 品牌管理
  - 库存管理
- 搜索服务
  - 基于Elasticsearch的全文搜索
  - 商品搜索与过滤
- 订单服务
  - 订单创建与管理
  - 订单状态流转
  - 订单事件处理
- 支付服务
  - 支付宝支付集成
  - 微信支付集成
  - 支付回调处理
- 用户服务
  - 用户注册与信息管理
  - 会员等级系统
- 促销服务
  - 优惠券系统
  - 活动管理
- 统计服务
  - 销售数据统计
  - 用户行为分析

## 项目设置

### 前置条件

- JDK 21
- Maven 3.8+
- MySQL 8+
- Redis 6+
- Elasticsearch 8.11 (可选，用于搜索功能)

### 本地开发

```bash
# 使用Maven运行应用
mvn spring-boot:run
```

### 构建

```bash
# 生成可部署的JAR包
mvn clean package
```

生成的JAR文件位于`target/muying-mall-0.0.1-SNAPSHOT.jar`

### 运行JAR包

```bash
java -jar target/muying-mall-0.0.1-SNAPSHOT.jar
```

## 项目结构

```
src/main/java/com/muyingmall/
├── api/           # API接口定义
├── common/        # 通用工具和常量
├── config/        # 应用配置
├── controller/    # 控制器
├── dto/           # 数据传输对象
├── entity/        # 实体类
├── enums/         # 枚举类型
├── event/         # 事件处理
├── exception/     # 异常处理
├── filter/        # 过滤器
├── listener/      # 事件监听器
├── lock/          # 分布式锁
├── mapper/        # MyBatis映射
├── security/      # 安全相关
├── service/       # 业务服务
├── statemachine/  # 状态机
├── task/          # 定时任务
├── tcc/           # TCC事务
├── util/          # 工具类
└── MuyingMallApplication.java # 应用入口
```

## 环境配置

应用使用Spring Profile进行环境配置管理：

- `dev`: 开发环境
- `test`: 测试环境
- `prod`: 生产环境

配置文件位于`src/main/resources/application-{profile}.yml`

## API文档

应用集成了SpringDoc OpenAPI，提供自动化的API文档：

- 访问地址: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 数据库迁移

应用包含主要的数据库脚本：`main.sql`，用于初始化数据库结构和基础数据。

## 安全设计

- 使用Spring Security进行认证授权
- 基于JWT的Token认证机制
- 请求签名验证
- 敏感数据加密存储
- XSS防护
- CSRF防护
- 频率限制

## 性能优化

- Redis缓存常用数据
- 使用Elasticsearch提升搜索性能
- MyBatis-Plus针对复杂查询的性能优化
- 分布式锁避免并发问题
- 异步处理长耗时操作

## 部署建议

- 使用Docker容器化部署
- 配合Nginx作为反向代理
- Redis集群提高缓存可用性
- MySQL主从复制保障数据安全
- 定期备份数据库

## 开发规范

- RESTful API设计规范
- 统一的异常处理机制
- 规范的代码格式
- 完善的日志记录
- 单元测试覆盖关键业务逻辑

## 接口安全

所有API端点都需要适当的认证，除了：
- `/api/auth/login` - 用户登录
- `/api/auth/register` - 用户注册
- `/api/products/**` - 产品浏览(GET请求) 