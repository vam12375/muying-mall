# 母婴商城系统文档中心

> 本目录包含母婴商城系统的完整技术文档和架构图

## 文档导航

### 架构文档

| 文档 | 说明 | 更新时间 |
|------|------|----------|
| [系统概览](architecture/system-overview.md) | 系统整体介绍、技术栈、功能概述 | 2025-06 |
| [系统架构](architecture/system-architecture.md) | 分层架构设计、组件说明 | 2025-06 |
| [部署架构](architecture/deployment-architecture.md) | 部署方案、环境配置 | 2025-06 |
| [组件交互](architecture/component-interaction.md) | 组件间交互流程 | 2025-06 |

### 业务流程文档

| 文档 | 说明 | 更新时间 |
|------|------|----------|
| [订单流程](business/order-process.md) | 订单创建、状态流转、取消退款 | 2025-06 |
| [支付流程](business/payment-process.md) | 支付发起、回调处理、退款 | 2025-06 |
| [用户流程](business/user-flows.md) | 注册登录、信息管理 | 2025-06 |
| [时序图](business/sequence-diagrams.md) | 核心业务时序图 | 2025-06 |

### 数据库文档

| 文档 | 说明 | 更新时间 |
|------|------|----------|
| [数据库设计](database/schema-design.md) | 表结构设计、索引策略 | 2025-06 |
| [数据字典](database/data-dictionary.md) | 字段说明、枚举值 | 2025-06 |
| [E-R图](database/er-diagram.md) | 实体关系图说明 | 2025-06 |

### 开发文档

| 文档 | 说明 | 更新时间 |
|------|------|----------|
| [环境搭建](development/setup-guide.md) | 开发环境配置指南 | 2025-06 |
| [编码规范](development/coding-standards.md) | 代码风格、命名规范 | 2025-06 |
| [测试指南](development/testing-guide.md) | 单元测试、集成测试 | 2025-06 |

## 架构图 (DrawIO)

所有架构图使用 DrawIO 格式，可以使用 [draw.io](https://app.diagrams.net/) 在线编辑或 VS Code DrawIO 插件打开。

### 图表列表

| 图表名称 | 文件路径 | 说明 |
|----------|----------|------|
| 系统架构图 | [diagrams/system-architecture.drawio](diagrams/system-architecture.drawio) | 系统分层架构、技术组件 |
| E-R实体关系图 | [diagrams/er-diagram.drawio](diagrams/er-diagram.drawio) | 核心实体及关系 |
| 订单流程时序图 | [diagrams/order-sequence.drawio](diagrams/order-sequence.drawio) | 用户下单完整流程 |
| 支付流程时序图 | [diagrams/payment-sequence.drawio](diagrams/payment-sequence.drawio) | 支付及异步回调流程 |
| 订单状态机图 | [diagrams/order-state-machine.drawio](diagrams/order-state-machine.drawio) | 订单状态流转 |
| 部署架构图 | [diagrams/deployment-architecture.drawio](diagrams/deployment-architecture.drawio) | Docker部署方案 |

### 图表预览

#### 1. 系统架构图
展示系统的整体分层架构，包括：
- 客户端层（移动端、Web、小程序、管理后台）
- 网关层（Nginx、API Gateway、负载均衡）
- 应用层（Controller、Security、异常处理）
- 业务服务层（用户、商品、订单、支付等服务）
- 数据存储层（MySQL、Redis、Elasticsearch、RabbitMQ）
- 外部服务（支付宝、微信支付）

#### 2. E-R实体关系图
展示核心业务实体及其关系：
- User（用户）↔ Order（订单）：一对多
- Order（订单）↔ OrderItem（订单项）：一对多
- Product（商品）↔ Category（分类）：多对一
- Order（订单）↔ Payment（支付）：一对一
- User（用户）↔ Cart（购物车）：一对多

#### 3. 订单流程时序图
展示用户下单的完整时序：
1. 用户点击结算
2. 获取分布式锁
3. 检查库存
4. 锁定库存
5. 创建订单
6. 发送延迟消息（订单超时处理）
7. 返回订单信息

#### 4. 支付流程时序图
展示支付处理流程：
1. 选择支付方式
2. 调用支付宝/微信预下单
3. 用户完成支付
4. 异步回调处理
5. 更新订单状态
6. 发送通知

#### 5. 订单状态机图
展示订单状态流转：
```
待付款 → 待发货 → 已发货 → 已完成
  ↓         ↓        ↓         ↓
已取消   已取消   退款中 → 已退款
```

#### 6. 部署架构图
展示Docker容器化部署方案：
- Nginx 反向代理
- Spring Boot 应用容器（可水平扩展）
- MySQL 8.0 数据库容器
- Redis 7.4 缓存容器
- RabbitMQ 4.1 消息队列容器
- Elasticsearch 搜索引擎容器

## 如何查看图表

### 方式一：draw.io 在线编辑器
1. 访问 https://app.diagrams.net/
2. 选择 "打开现有图表"
3. 选择本地 .drawio 文件

### 方式二：VS Code 插件
1. 安装 "Draw.io Integration" 插件
2. 直接在 VS Code 中打开 .drawio 文件

### 方式三：导出为图片
1. 在 draw.io 中打开文件
2. 文件 → 导出为 → PNG/SVG/PDF

## 文档维护

### 更新日志

| 日期 | 更新内容 |
|------|----------|
| 2025-12-31 | 添加DrawIO架构图（系统架构、E-R图、时序图、状态机、部署图） |
| 2025-06-18 | 初始文档创建 |

### 贡献指南

1. 文档使用 Markdown 格式
2. 架构图使用 DrawIO 格式（.drawio）
3. 图表文件统一存放在 `diagrams/` 目录
4. 更新文档后请同步更新本索引页

---

*维护者: 青柠檬*
*最后更新: 2025-12-31*
