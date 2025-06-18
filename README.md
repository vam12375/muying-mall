# 🍼 母婴商城系统

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=flat-square&logo=spring)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7.4.0-red?style=flat-square&logo=redis)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.11-yellow?style=flat-square&logo=elasticsearch)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

**现代化的母婴用品电商平台，基于Spring Boot 3.2.0构建**

[快速开始](#-快速开始) • [功能特性](#-功能特性) • [技术架构](#-技术架构) • [文档](#-文档) • [贡献指南](#-贡献指南)

</div>

---

## 📋 项目简介

母婴商城系统是一个功能完整的现代化电商平台，专注于母婴用品的在线销售。系统采用前后端分离架构，基于Spring Boot 3.2.0和Java 21构建，提供完整的电商功能，包括用户管理、商品管理、订单处理、支付集成、营销活动等核心模块。

### 🎯 核心特性

- **🔐 用户体系**: 完整的用户注册、登录、信息管理体系
- **📦 商品管理**: 支持多级分类、多规格、多图片的商品管理
- **🛒 购物体验**: 购物车、收藏、商品搜索、评价系统
- **📋 订单处理**: 完整的订单生命周期管理，支持状态机流转
- **💳 支付集成**: 集成支付宝、微信支付等主流支付方式
- **🎁 营销功能**: 优惠券系统、积分体系、会员等级
- **🚚 物流管理**: 物流跟踪、配送管理

### 🚀 技术特性

- **现代架构**: 基于Spring Boot 3.2.0，采用最新Java 21特性
- **高性能**: Redis缓存、Elasticsearch搜索、数据库优化
- **高可用**: 分布式架构设计，支持水平扩展
- **安全可靠**: Spring Security + JWT认证，数据加密存储
- **开发友好**: 完整的API文档、规范的代码结构

## 🛠 技术栈

### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.2.0 | 核心框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis-Plus | 3.5.9 | ORM框架 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 7.4.0 | 缓存和会话存储 |
| Elasticsearch | 8.11.0 | 全文搜索引擎 |
| JWT | 0.11.5 | 令牌认证 |
| SpringDoc | 2.2.0 | API文档生成 |

### 开发工具
- **构建工具**: Maven 3.8+
- **IDE**: IntelliJ IDEA (推荐)
- **版本控制**: Git
- **容器化**: Docker & Docker Compose
- **API测试**: Postman, Swagger UI

## 🚀 快速开始

### 环境要求

- **Java**: 21+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Redis**: 7.4.0+
- **Elasticsearch**: 8.11+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/vam12375/muying-mall.git
   cd muying-mall
   ```

2. **配置数据库**
   ```sql
   CREATE DATABASE muying_mall CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
   mysql -u root -p muying_mall < main.sql
   ```

3. **配置应用**
   ```bash
   cp src/main/resources/application-private.yml.example src/main/resources/application-private.yml
   # 编辑配置文件，填入数据库密码等私有配置
   ```

4. **启动服务**
   ```bash
   # 启动基础服务
   docker-compose up -d mysql redis elasticsearch

   # 启动应用
   mvn spring-boot:run
   ```

5. **验证安装**
   ```bash
   curl http://localhost:8080/api/actuator/health
   # 访问 http://localhost:8080/api/swagger-ui.html 查看API文档
   ```

### Docker快速启动

```bash
# 使用Docker Compose一键启动
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f app
```

## 📚 文档

### 📖 技术文档

| 文档类型 | 链接 | 说明 |
|----------|------|------|
| 🏗️ 系统架构 | [架构文档](docs/architecture/) | 系统整体架构设计 |
| 🗄️ 数据库设计 | [数据库文档](docs/database/) | ER图、表结构、数据字典 |
| 🔄 业务流程 | [业务文档](docs/business/) | 核心业务流程和时序图 |
| 💻 开发指南 | [开发文档](docs/development/) | 环境搭建、编码规范、测试指南 |
| 🔌 API接口 | [API文档](docs/api/) | RESTful API接口文档 |

### 🎯 核心文档

- **[系统概览](docs/architecture/system-overview.md)** - 系统整体介绍和技术栈
- **[数据库ER图](docs/database/er-diagram.md)** - 完整的数据库实体关系图
- **[开发环境搭建](docs/development/setup-guide.md)** - 详细的环境配置指南
- **[编码规范](docs/development/coding-standards.md)** - 代码规范和最佳实践

## 🏗️ 项目结构

```
muying-mall/
├── docs/                          # 📚 项目文档
│   ├── architecture/              # 🏗️ 架构设计文档
│   ├── database/                  # 🗄️ 数据库设计文档
│   ├── business/                  # 🔄 业务流程文档
│   ├── development/               # 💻 开发相关文档
│   └── api/                       # 🔌 API接口文档
├── src/
│   ├── main/
│   │   ├── java/com/muyingmall/
│   │   │   ├── controller/        # 🎮 控制器层
│   │   │   ├── service/           # 🔧 业务服务层
│   │   │   ├── mapper/            # 🗃️ 数据访问层
│   │   │   ├── entity/            # 📊 实体类
│   │   │   ├── dto/               # 📦 数据传输对象
│   │   │   ├── config/            # ⚙️ 配置类
│   │   │   └── common/            # 🛠️ 通用工具类
│   │   └── resources/
│   │       ├── mapper/            # 📋 MyBatis映射文件
│   │       └── application.yml    # ⚙️ 应用配置
│   └── test/                      # 🧪 测试代码
├── main.sql                       # 🗄️ 数据库初始化脚本
├── docker-compose.yml             # 🐳 Docker编排文件
├── pom.xml                        # 📦 Maven配置文件
└── README.md                      # 📖 项目说明文档
```

## 🔧 核心功能模块

### 用户管理模块
- 用户注册/登录
- 个人信息管理
- 收货地址管理
- 积分账户管理

### 商品管理模块
- 商品信息管理
- 多级分类管理
- 品牌管理
- 库存管理

### 订单管理模块
- 购物车管理
- 订单创建与管理
- 订单状态流转
- 物流跟踪

### 支付管理模块
- 多种支付方式
- 支付状态管理
- 支付回调处理
- 退款管理

### 营销管理模块
- 优惠券系统
- 积分体系
- 商品评价
- 收藏功能

## 🌐 API接口

系统提供完整的RESTful API接口，支持：

- **用户相关**: 注册、登录、信息管理
- **商品相关**: 商品查询、分类浏览、搜索
- **订单相关**: 订单创建、查询、状态管理
- **支付相关**: 支付创建、回调处理、退款
- **营销相关**: 优惠券、积分、评价

### API文档访问

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs

## 🧪 测试

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行单元测试
mvn test -Dtest="*Test"

# 运行集成测试
mvn test -Dtest="*IntegrationTest"

# 生成测试报告
mvn test jacoco:report
```

### 测试覆盖率

- **单元测试覆盖率**: 目标 80%+
- **集成测试覆盖率**: 目标 60%+
- **API测试覆盖率**: 目标 90%+

## 🚀 部署

### 开发环境
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 测试环境
```bash
docker-compose -f docker-compose.test.yml up -d
```

### 生产环境
```bash
# 构建镜像
docker build -t muying-mall:latest .

# 部署到Kubernetes
kubectl apply -f k8s/
```

## 🤝 贡献指南

我们欢迎所有形式的贡献！请阅读 [贡献指南](CONTRIBUTING.md) 了解如何参与项目开发。

### 开发流程

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

### 代码规范

- 遵循 [编码规范](docs/development/coding-standards.md)
- 编写单元测试
- 更新相关文档
- 通过所有CI检查

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👥 团队

- **项目负责人**: [@your-name](https://github.com/vam12375)
- **技术负责人**: [@tech-lead](https://github.com/vam12375)
- **开发团队**: [@dev-team](https://github.com/vam12375)

## 📞 联系我们

- **项目主页**: https://github.com/vam12375/muying-mall
- **问题反馈**: https://github.com/vam12375/muying-mall/issues
- **邮箱**: 2898191344@qq.com
- **文档**: https://docs.muyingmall.com

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！

---

<div align="center">

**如果这个项目对你有帮助，请给我们一个 ⭐️**

Made with ❤️ by [母婴商城团队](https://github.com/vam12375)

</div>