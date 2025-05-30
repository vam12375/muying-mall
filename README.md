# Muying Mall - 母婴商城后端API服务

母婴商城系统的后端API服务，基于Spring Boot构建的现代Java应用，为前台商城和后台管理系统提供强大的API支持。

## 技术栈详解

### 1. 核心框架与环境
- **Spring Boot 3.2.0**
  - 基于Spring框架的简化开发方案
  - 采用自动配置和约定优于配置原则
  - 利用Spring Boot Actuator实现系统健康监控和指标收集
  - 集成了丰富的Web开发功能，如内嵌式容器、请求映射等
  
- **Java 21**
  - 采用长期支持版本(LTS)
  - 利用Java 21的新特性，如增强的switch表达式、记录类(Records)等
  - 使用虚拟线程(Virtual Threads)提高并发处理能力

- **构建工具**
  - Maven 3.8+，用于依赖管理和项目构建
  - 项目基于Spring Boot Starter父项目进行版本管理
  - 使用Maven插件体系构建可执行JAR包

### 2. 数据层技术
- **ORM框架: MyBatis-Plus 3.5.9**
  - 增强版MyBatis，提供了丰富的CRUD操作封装
  - 自动化SQL构建，减少手动SQL编写
  - 内置分页插件，简化分页查询操作
  - 字段自动填充功能，自动处理创建时间、更新时间等公共字段
  - 乐观锁插件，实现并发控制
  - 逻辑删除功能，实现数据软删除

- **数据库: MySQL 8.0+**
  - 使用InnoDB存储引擎，支持事务和外键约束
  - 优化的表结构设计，合理使用索引
  - 使用存储过程、触发器优化特定业务场景
  - 采用主从复制架构，提高读写性能和系统可用性
  - 实现了定期数据清理和归档

### 3. 缓存与会话管理
- **Redis 缓存**
  - Spring Data Redis 提供Redis操作抽象
  - 多级缓存设计，包括本地缓存和分布式缓存
  - 实现商品信息、分类信息等热点数据的缓存
  - 使用Redis实现分布式锁，解决高并发下的数据一致性问题
  - 缓存预热机制，提高系统启动后的响应速度

- **会话管理: Spring Session**
  - 基于Redis的分布式会话管理
  - 实现跨节点的会话共享
  - 自定义会话过期策略
  - 会话数据安全存储

### 4. 安全框架
- **Spring Security**
  - 基于角色的权限控制(RBAC)
  - 自定义认证流程
  - 防止CSRF攻击
  - 实现请求过滤和安全拦截
  - 密码加密存储(BCrypt)

- **认证授权: JWT (JJWT 0.11.5)**
  - 基于Token的无状态认证机制
  - 自定义Token生成策略
  - Token有效期管理
  - 刷新Token机制
  - 支持黑名单机制，实现Token即时失效

### 5. API文档
- **SpringDoc OpenAPI 2.2.0**
  - 自动生成API文档
  - 基于注解的API描述
  - 支持Swagger UI界面展示
  - API分组与标签管理
  - 文档版本控制
  - 支持认证信息配置

### 6. 搜索引擎
- **Elasticsearch 8.11.0**
  - 高性能全文搜索
  - 与Spring Boot集成的Elasticsearch客户端
  - 商品搜索、智能推荐
  - 搜索结果排序与过滤
  - 搜索热词统计
  - 数据库与搜索引擎数据同步策略

### 7. JSON处理
- **FastJSON2 2.0.40**
  - 高性能JSON解析和生成
  - 支持复杂对象序列化
  - 自定义序列化和反序列化

- **Jackson (jackson-datatype-jsr310)**
  - Spring Boot默认的JSON库
  - 支持Java 8日期时间API (JSR-310)
  - 自定义日期格式处理

### 8. 支付集成
- **支付宝 SDK 4.38.0**
  - 集成支付宝支付功能
  - 支持多种支付方式
  - 实现异步通知处理
  - 退款流程支持
  - 支付状态查询

- **微信支付 SDK 0.0.3**
  - 微信支付接口集成
  - 支持扫码支付、H5支付等
  - 支付结果异步通知
  - 退款和查询功能

### 9. 业务逻辑实现
- **状态机模式**
  - 实现订单状态流转
  - 实现支付状态管理
  - 实现退款状态管理
  - 基于事件驱动的状态转换

- **事件系统**
  - 基于Spring Event实现业务解耦
  - 异步事件处理
  - 事件驱动的业务流程

- **分布式锁**
  - 基于Redis实现分布式锁
  - 解决并发操作的数据一致性问题
  - 支持可重入锁、公平锁等多种锁类型

- **TCC事务**
  - Try-Confirm-Cancel模式
  - 实现分布式事务
  - 保证跨服务操作的一致性

### 10. 性能优化与监控
- **性能优化**
  - 多级缓存策略
  - SQL查询优化
  - 异步处理长耗时操作
  - 连接池优化

- **监控与指标**
  - Micrometer 集成
  - 性能指标收集
  - 自定义监控指标
  - 与Prometheus、Grafana等监控工具集成

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

- 访问地址: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs

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

## 详细文档

本项目提供了详细的开发和使用文档：

### API文档
- [API文档概览](docs/api/index.md)
- 在线Swagger文档：http://localhost:8080/api/swagger-ui.html

### 系统架构
- [系统整体架构](docs/architecture/system-architecture.md)
- [组件交互图](docs/architecture/component-diagram.md)

### 数据库设计
- [数据库概览](docs/database/schema-overview.md)
- [ER图](docs/database/er-diagram.png)

### 开发指南
- [环境搭建](docs/development/setup.md)
- [开发最佳实践](docs/development/best-practices.md)
- [代码示例](docs/development/code-examples.md)

### 部署文档
- [Docker部署指南](docs/deployment/docker-deployment.md)
- [Kubernetes配置](docs/deployment/kubernetes-config.md)
- [Nginx配置](docs/deployment/nginx-config.md) 