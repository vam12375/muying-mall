# &#x1F37C; 母婴商城系统 - 后端服务

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=flat-square&logo=spring)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7.4.0-red?style=flat-square&logo=redis)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-4.1.4-orange?style=flat-square&logo=rabbitmq)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-9.2.1-yellow?style=flat-square&logo=elasticsearch)

基于 Spring Boot 3.2.0 + Java 21 构建的现代化母婴电商后端服务

</div>

---

## &#x1F3AC; 项目演示

<div align="center">

### &#x1F3AF; 后台管理系统演示
> 完整展示后台管理系统的核心功能和操作流程

https://github.com/user-attachments/assets/f37a9f50-1511-4000-a56b-74d1357d25c9

---

### &#x1F504; 完整业务流程演示
> 商品购买 - 支付 - 发货 - 退款全流程演示

https://github.com/user-attachments/assets/3aa40165-337a-4e13-8262-ca470a6a9a47

---

### &#x2B50; 商品评价与回复流程演示
> 商品评价 - 评价回复 - 互动交流完整流程

https://github.com/user-attachments/assets/bb7d9c68-591b-4fe4-b3d1-6558a964e4b4

</div>

---

## &#x1F3D7; 系统架构

```
+-------------------------------------------------------------------+
|                          客户端层                                  |
|       muying-web (Vue 3)          muying-admin (Next.js 16)       |
+-------------------------------------------------------------------+
                                  |
                                  v
+-------------------------------------------------------------------+
|                      muying-mall 后端服务                          |
+-------------------+-----------------------------------------------+
|  Controller 层    |  RESTful API 接口，统一响应格式                |
+-------------------+-----------------------------------------------+
|  Service 层       |  业务逻辑处理，事务管理                         |
+-------------------+-----------------------------------------------+
|  核心组件         |  状态机 | TCC事务 | 分布式锁 | 消息队列        |
+-------------------+-----------------------------------------------+
|  Mapper 层        |  MyBatis-Plus 数据访问                         |
+-------------------------------------------------------------------+
                                  |
        +-----------+-----------+-----------+-----------+
        v           v           v           v           v
   +---------+ +---------+ +---------+ +---------+ +---------+
   |  MySQL  | |  Redis  | |RabbitMQ | |   ES    | | Alipay  |
   |  8.0+   | |  7.4.0  | |  4.1.4  | |  9.2.1  | |   SDK   |
   +---------+ +---------+ +---------+ +---------+ +---------+
```

---

## &#x1F4E6; 后端功能模块

```
src/main/java/com/muyingmall/
|-- controller/          # API 接口层
|   |-- admin/           # 管理端接口
|   |-- user/            # 用户端接口
|   +-- common/          # 公共接口
|-- service/             # 业务逻辑层
|-- mapper/              # 数据访问层
|-- entity/              # 实体类
|-- dto/                 # 数据传输对象
|-- enums/               # 枚举定义
|-- config/              # 配置类
|-- security/            # 安全认证 (JWT + Spring Security)
|-- statemachine/        # 状态机 (订单/支付/退款状态流转)
|-- tcc/                 # TCC 分布式事务
|-- lock/                # 分布式锁
|-- consumer/            # RabbitMQ 消息消费者
|-- handler/             # 消息处理器
|-- event/               # 事件驱动
|-- listener/            # 事件监听器
|-- monitor/             # 系统监控
|-- websocket/           # WebSocket 实时通信
|-- task/                # 定时任务
|-- aspect/              # AOP 切面
|-- filter/              # 过滤器
|-- interceptor/         # 拦截器
|-- exception/           # 异常处理
|-- annotation/          # 自定义注解
|-- document/            # ES 文档
|-- repository/          # ES 仓库
|-- common/              # 通用工具
+-- util/                # 工具类
```

---

## &#x26A1; 核心技术特性

| 特性 | 实现方案 | 说明 |
|------|---------|------|
| 状态机模式 | Spring Statemachine | 订单、支付、退款状态流转管理 |
| 分布式事务 | TCC 模式 | 保证跨服务数据最终一致性 |
| 分布式锁 | Redis + Redisson | 库存扣减、防重复下单 |
| 消息队列 | RabbitMQ | 异步处理、延迟队列、死信队列 |
| 全文搜索 | Elasticsearch 9.2.1 | 商品搜索、搜索建议 |
| 缓存策略 | Redis 多级缓存 | 热点数据缓存、Session 管理 |
| 认证授权 | JWT + Spring Security | 无状态认证、权限控制 |
| 实时通信 | WebSocket | 监控数据推送、物流更新 |
| API 文档 | Knife4j | 自动生成接口文档 |

---

## &#x1F527; 核心业务模块

| 模块 | 功能 | 技术要点 |
|------|------|---------|
| 用户模块 | 注册登录、个人中心、地址管理、钱包积分 | JWT认证、BCrypt加密 |
| 商品模块 | 商品管理、SKU规格、分类品牌、库存管理 | ES搜索、Redis缓存 |
| 订单模块 | 购物车、下单、支付、发货、退款 | 状态机、TCC事务、分布式锁 |
| 支付模块 | 支付宝/微信/余额支付、退款处理 | 异步回调、幂等性保证 |
| 营销模块 | 优惠券、积分体系、会员等级 | 分布式锁防超领 |
| 消息模块 | 订单/支付/库存消息处理 | RabbitMQ、死信队列 |
| 监控模块 | 系统监控、API统计、日志记录 | Actuator、AOP切面 |

---

## &#x1F680; 快速开始

### 环境要求
- Java 21
- Maven 3.9.9
- MySQL 8.0
- Redis 7.4.0
- RabbitMQ 4.1.4 (可选)
- Elasticsearch 9.2.1 (可选)

### 启动步骤

```bash
# 1. 克隆项目
git clone https://github.com/vam12375/muying-mall.git
cd muying-mall

# 2. 初始化数据库
mysql -u root -p -e "CREATE DATABASE muying_mall CHARACTER SET utf8mb4;"
mysql -u root -p muying_mall < muying_mall.sql

# 3. 配置私有信息
cp src/main/resources/application-private.yml.example src/main/resources/application-private.yml
# 编辑 application-private.yml 填入数据库密码、JWT密钥等

# 4. 启动服务
mvn spring-boot:run

# 5. 访问 Knife4j API 文档
# http://localhost:8080/api/doc.html
```

### Docker 启动

```bash
docker-compose up -d
```

---

## &#x1F310; API 接口

| 模块 | 路径 | 说明 |
|------|------|------|
| 用户认证 | `/user/auth/*` | 注册、登录、登出 |
| 用户中心 | `/user/*` | 个人信息、地址、钱包、积分 |
| 购物流程 | `/user/cart/*`, `/user/order/*` | 购物车、订单管理 |
| 商品服务 | `/common/product/*` | 商品列表、详情、搜索 |
| 支付服务 | `/payment/*` | 支付宝/微信支付、退款 |
| 管理后台 | `/admin/*` | 商品/订单/用户/营销管理 |

响应格式:
```json
{ "code": 200, "message": "操作成功", "data": {} }
```

---

## &#x1F6E0; 技术栈

| 类型 | 技术 |
|------|------|
| 核心框架 | Spring Boot 3.2.0, Spring Security 6.x |
| 数据层 | MyBatis-Plus 3.5.9, MySQL 8.0 |
| 缓存 | Redis 7.4.0 |
| 消息队列 | RabbitMQ 4.1.4 |
| 搜索引擎 | Elasticsearch 9.2.1 |
| 认证 | JWT 0.11.5 |
| 支付 | Alipay SDK 4.38.0 |
| 文档 | Knife4j |

---

## &#x1F517; 相关项目

| 项目 | 技术栈 | 链接 |
|------|--------|------|
| muying-mall | Spring Boot 3.2 (本项目) | [GitHub](https://github.com/vam12375/muying-mall) |
| muying-web | Vue 3 + Vite | [GitHub](https://github.com/vam12375/muying-web) |
| muying-admin | Next.js 16 + React | [GitHub](https://github.com/vam12375/muying-admin) |

---

## &#x1F4DA; 文档

| 类型 | 文档 | 说明 |
|------|------|------|
| 架构设计 | [系统架构](docs/architecture/system-architecture.md) | 整体架构设计 |
| 架构设计 | [组件交互](docs/architecture/component-interaction.md) | 组件间交互关系 |
| 架构设计 | [部署架构](docs/architecture/deployment-architecture.md) | 部署方案设计 |
| 业务流程 | [订单流程](docs/business/order-process.md) | 订单业务流程 |
| 业务流程 | [支付流程](docs/business/payment-process.md) | 支付业务流程 |
| 业务流程 | [时序图](docs/business/sequence-diagrams.md) | 核心业务时序图 |
| 数据库 | [ER图](docs/database/er-diagram.md) | 数据库实体关系图 |
| 数据库 | [数据字典](docs/database/data-dictionary.md) | 表结构说明 |
| 开发指南 | [环境搭建](docs/development/setup-guide.md) | 开发环境配置 |
| 开发指南 | [编码规范](docs/development/coding-standards.md) | 代码规范 |
| 开发指南 | [测试指南](docs/development/testing-guide.md) | 测试说明 |

---

## &#x1F4C4; 许可证

MIT License

---

<div align="center">

&#x2B50; 如果这个项目对你有帮助，请给一个 Star

</div>
