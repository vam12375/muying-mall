# 👶🏻 母婴商城系统 (Muying Mall)

> ✨ **基于 Spring Boot 3.3.0 + Java 21 的现代化、高并发母婴电商平台** ✨

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.4-red)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## 📖 项目简介

**母婴商城系统** 是一个功能完善的B2C电商平台，深入聚焦母婴用品的在线零售。它不仅提供了常规的电商购物流程（商品浏览、购物车、下单、支付、物流），还融入了**育儿知识圈子**、**积分商城**、**优惠券营销**、**高并发秒杀**等丰富功能。

本系统采用了当前最前沿的 Java 技术栈：**Java 21 的虚拟线程（Virtual Threads）** 搭配 **Spring Boot 3.3**，以极低的资源消耗实现了极高的并发处理能力，完美解决了传统电商在高并发场景下的线程瓶颈。

---

## 🌟 核心亮点

本项目融合了众多企业级核心解决方案：

1. 🚀 **Java 21 虚拟线程体验**
   全面弃用传统 Tomcat 线程池，拥抱 Java 21 虚拟线程机制。不仅将单机 Tomcat 的 `max-connections` 压榨到了 20,000 以上，还有效避免了高并发下的阻塞等待。
2. 🔄 **优雅的状态机 (State Machine)**
   彻底告别订单、支付状态的 `if-else` 意大利面条代码。基于硬核状态机设计模式设计了 `OrderStateMachine` 和 `PaymentStateMachine`，确保所有状态流转绝对合法，防篡改。
3. 🛡️ **TCC 分布式事务实现**
   在面对库存扣减、优惠券使用、积分抵用等跨模块复杂业务时，采用手写轻量级 TCC（Try-Confirm-Cancel）分布式事务框架，保障核心资金和库存的数据一致性。
4. ⚡ **千万级秒杀与超卖防护**
   针对突发流量的秒杀场景，采取了 **Redis Lua 脚本预扣库存** + **RabbitMQ 削峰填谷** + **异步下单** 的三把斧策略，利用极致的压榨彻底解决超卖难题。
5. 🔍 **Elasticsearch 全文搜索支持**
   告别低效的 `LIKE` 查询，商品搜索引入 Elasticsearch 作为搜索引擎，支持复杂的分词、聚合以及高亮显示，并实现了 MySQL 到 ES 的平滑数据同步。
6. 📍 **高德地图聚合物流轨迹**
   集成真实经纬度计算，自动对接高德地图（AMap）完成物流追踪管理与距离计算。

---

## 🏗️ 系统架构

> 📋 详细架构图请查看 [docs/diagrams/system-architecture.drawio](docs/diagrams/system-architecture.drawio)

```text
┌─────────────────────────────────────────────────────────────┐
│                      客户端层                                │
│    移动端App  │  Web前端  │  管理后台  │  小程序             │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Nginx 负载均衡                            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot 应用层                        │
│  Controller → Security → Service → Mapper                   │
│  状态机 │ 事件总线 │ TCC事务 │ 分布式锁 │ 缓存管理            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      数据存储层                              │
│   MySQL 8.0  │  Redis 7.4  │  Elasticsearch  │  RabbitMQ   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ 技术栈概览

系统采用非常标准的流行主流框架构建。

| 中间件 / 框架 | 技术方案 | 版本 | 作用说明 |
| :--- | :--- | :--- | :--- |
| **核心框架** | Spring Boot | 3.3.0 | 强悍的底层IoC、AOP基础 |
| **运行时环境**| Java (Virtual Threads) | 21 | 虚拟线程让多线程开发变回最初的单线程体验 |
| **安全框架** | Spring Security + JWT | 6.x | 把守每一次HTTP请求，进行安全的RBAC认证与授权 |
| **持久层框架**| MyBatis-Plus | 3.5.9 | 数据操作的利器，无脑支持各种复杂查询 |
| **关系型数据库**| MySQL | 8.0+ | 主业务数据的可靠安家之所 |
| **NoSQL / 缓存**| Redis + Lettuce | 7.4 | 抗下绝大多数热点数据的查询；支持分布式锁 |
| **搜索引擎** | Elasticsearch | 9.x | 为海量商品提供了毫秒级的搜索支撑 |
| **消息队列** | RabbitMQ | 4.1 | 订单超时取消、秒杀队列排队、事件通知异步解耦 |
| **三方支付** | 支付宝 / 微信 SDK | 4.x / 0.x | 商城的绝对核心：资金收款 |

---

## 📂 项目结构导读

了解以下目录结构，帮你轻松找到改代码的地方：

```text
muying-mall/
├── src/main/java/com/muyingmall/
│   ├── config/          # ⚙️ 各种中间件的配置（安全、Redis、MQ、线程池、拦截器等）
│   ├── controller/      # 🌍 API入口
│   │   ├── admin/       # ↪ 后台管理API层
│   │   ├── user/        # ↪ 移动端/前端商城API层
│   │   └── common/      # ↪ 公共开放API（支付回调、上传等）
│   ├── service/         # 🧠 最核心的业务逻辑层（高达140+个服务接口）
│   ├── mapper/          # 💾 Mybatis 操作数据库的接口
│   ├── entity/          # 📦 数据库映射实体类（多达68+个表）
│   ├── dto/             # 📨 数据传输对象，接口参数接收入口
│   ├── statemachine/    # 🚦 状态机设计模式的详细实现（订单/支付/退款引擎）
│   ├── consumer/        # 🐰 RabbitMQ 消息消费者（例如秒杀队列处理）
│   ├── tcc/             # ⚖️ TCC 分布式事务引擎相关注解与拦截处理
│   ├── security/        # 🔒 Spring Security 用户鉴权和拦截层
│   └── util/            # 🛠️ 各类通用的工具方法大全
├── src/main/resources/
│   ├── db/migration/    # 🗄️ SQL 文件以及基础测试数据
│   ├── lua/ & scripts/  # 📜 Redis 原生 Lua 脚本（用于库存扣减/锁操作）
│   ├── mapper/          # 📝 xml 后缀的 Mybatis 自定义 SQL 内容
│   └── application.yml  # 📑 主配置文件
├── docs/                # 📚 最全面的官方文档库（强烈建议阅读）
└── docker-compose.yml   # 🐳 极其简单的 Docker 编排配置
```

---

## 🎯 业务功能清单

### 🏪 C端 - 商城核心功能

- **🛒 购物全链路**：商品浏览 → 加入购物车 → 结算提交订单 → 支付宝/微信付款 → 填写/选择收货地址。
- **💥 营销体系**：每日签到获取积分、抽奖和积分兑换商品；多级分类优惠券的发放、核销。
- **⚡ 秒杀专区**：定期自动上架热门商品抢购配置，全异步化下单体验流。
- **👶🏻 育儿互动圈**：不只是买买买，还能发布育儿动态帖子、评论点赞，获取每日育儿小知识（Parenting Tip）。
- **👩‍🔧 售后服务**：查看高德物流聚合轨迹，售后申请退款与退款进度查询，发表附带多图的商品复杂评价（评论树结构）。

### 💻 B端 - 强大的运营后台

- **🛍️ 商品与订单控制台**：上下架商品，全盘实时掌控平台交易金额与订单状态。
- **🎟️ 营销策略管理**：一键生成带验证规则的优惠券（Coupon Batch）、积分发放策略控制。
- **📊 监控与数据图表**：聚合首页数据分析，支持强大的 `sys_log` 审计监控和管理员登录追踪。
- **🔧 基础设施调配**：RBAC基于角色的系统菜单动态渲染、字典常量动态管理等。

---

## 🚀 极速部署运行指南

本项目环境极度友好，通过少量的步骤即可在您的本地机器跑起来。

### 1. 环境必备条件

- **JDK环境**：必须安装 **Java 21** 及以上版本 (因为使用了虚拟线程特性)。
- **构建工具**：Maven 3.8+。
- **环境软件**：Docker & Docker-Compose (仅用于非常简单的依赖调起)。

### 2. 服务器 / 基础环境一键启动

我们为你准备了傻瓜式配置的完整中间件环境，打开命令行输入：

```bash
# 自动拉起 MySQL(3306), Redis(6379), RabbitMQ(5672/15672)
docker-compose up -d mysql redis rabbitmq
```

*(注意：第一次启动 MySQL 会自动读取根目录的 `muying_mall.sql` 并执行全量导入，请稍安勿躁等待 1-2 分钟。)*

### 3. 配置密钥与启动程序

将 `src/main/resources/application-private.yml.example` 重命名为 `application-private.yml`。这其中存放了数据库密码与密钥（如果你没有修改 docker 的配置，默认即可直接连接）：

```yaml
spring:
  datasource:
    username: root
    password: muying123456
  data:
    redis:
      password: 
jwt:
  secret: your-secret-key-change-this-in-production
```

接着，直接利用 IDE (如 IntelliJ IDEA) 运行 `MuyingMallApplication.java` 或通过 Maven 命令：

```bash
mvn spring-boot:run
```

如果您看到控制台打印出如下可爱的图案，恭喜！跑起来了！

```text
(♥◠‿◠)ﾉﾞ  母婴商城启动成功   ლ(´ڡ`ლ)ﾞ  
    ███╗   ███╗    ██╗   ██╗    
    ████╗ ████║    ╚██╗ ██╔╝    
    ██╔████╔██║     ╚████╔╝     
    ██║╚██╔╝██║      ╚██╔╝      
    ██║ ╚═╝ ██║       ██║       
    ╚═╝     ╚═╝       ╚═╝       
    ♡ Muying Mall Started ♡    
```

### 4. API 访问与调试

项目集成了酷炫的 `Knife4j`（Swagger UI 升级版），启动后可以随时访问并查阅 API：
👉🏻 **Swagger 调试地址**： `http://localhost:8080/api/doc.html`

---

## 📖 原创架构深度剖析文档

如果您想要通过这个项目学习真正的企业级架构思维，而不是只停留在增删改查上，请**务必移步到项目的 `docs/` 目录下学习**，我们撰写了极其详尽的设计思路：

- 🛠 **业务与状态流设计**： [订单状态机流转](docs/diagrams/order-state-machine.drawio) 、 [支付与退款时序图](docs/diagrams/payment-sequence.drawio)
- 🚀 **高并发场景**： [秒杀架构剖析](docs/SECKILL_ARCHITECTURE.md) 、 [Elasticsearch 搜索解析](docs/5.4搜索.md)
- 📐 **持久层与实体**： [系统核心 E-R图 与 字典](docs/diagrams/er-diagram.drawio)
- 🌱 **基础部署知识**： [手把手运维与 Docker 编排](docs/deployment/deployment-architecture.md)

---

## 👨‍💻 贡献建议与反馈

如果你对项目有任何优化建议，非常欢迎提交 PR：

1. Fork 本仓库
2. 新建您的一项重大改变 feature 分支 (`git checkout -b feature/SomeAmazingFeature`)
3. 给代码加上适当的提交 `Commit` (`git commit -m 'Add some feature'`)
4. 推送到刚才的分支 (`git push origin feature/SomeAmazingFeature`)
5. 新建一个对我们主分支的 Pull Request

如果遇到麻烦或者疑问，别忘了提 `Issues` 告诉我们！

## 📄 许可协议

本项目使用 [MIT License](LICENSE) 许可协议，放心研究、改造与商业使用。

---

*Final Updated: 2026-3-11*
