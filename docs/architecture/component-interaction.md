# 母婴商城组件交互设计

## 概述

本文档详细描述了母婴商城系统中各个组件之间的交互关系，包括组件依赖、数据流向、接口定义等，为系统的开发、维护和扩展提供指导。

## 组件交互总览

```mermaid
graph TB
    subgraph "前端组件"
        FE1[用户端App]
        FE2[Web前端]
        FE3[管理后台]
        FE4[小程序]
    end
    
    subgraph "网关组件"
        GW1[API Gateway]
        GW2[Load Balancer]
        GW3[Rate Limiter]
    end
    
    subgraph "控制器组件"
        CT1[UserController]
        CT2[ProductController]
        CT3[OrderController]
        CT4[PaymentController]
        CT5[CartController]
        CT6[SearchController]
    end
    
    subgraph "服务组件"
        SV1[UserService]
        SV2[ProductService]
        SV3[OrderService]
        SV4[PaymentService]
        SV5[CartService]
        SV6[SearchService]
        SV7[CouponService]
        SV8[PointsService]
        SV9[NotificationService]
    end
    
    subgraph "支撑组件"
        SP1[StateMachine]
        SP2[EventBus]
        SP3[CacheManager]
        SP4[DistributedLock]
        SP5[SecurityManager]
    end
    
    subgraph "数据组件"
        DA1[UserMapper]
        DA2[ProductMapper]
        DA3[OrderMapper]
        DA4[PaymentMapper]
        DA5[CacheAdapter]
    end
    
    subgraph "存储组件"
        ST1[MySQL]
        ST2[Redis]
        ST3[Elasticsearch]
        ST4[OSS]
    end
    
    subgraph "外部组件"
        EX1[Alipay API]
        EX2[WeChat Pay API]
        EX3[SMS Service]
        EX4[CDN Service]
    end
    
    %% 前端到网关
    FE1 --> GW1
    FE2 --> GW1
    FE3 --> GW1
    FE4 --> GW1
    
    %% 网关到控制器
    GW1 --> CT1
    GW1 --> CT2
    GW1 --> CT3
    GW1 --> CT4
    GW1 --> CT5
    GW1 --> CT6
    
    %% 控制器到服务
    CT1 --> SV1
    CT2 --> SV2
    CT3 --> SV3
    CT4 --> SV4
    CT5 --> SV5
    CT6 --> SV6
    
    %% 服务间交互
    SV3 --> SV1
    SV3 --> SV2
    SV3 --> SV4
    SV4 --> SV1
    SV5 --> SV2
    SV6 --> SV2
    SV7 --> SV1
    SV8 --> SV1
    
    %% 服务到支撑组件
    SV3 --> SP1
    SV4 --> SP1
    SV1 --> SP2
    SV3 --> SP2
    SV4 --> SP2
    SV2 --> SP3
    SV6 --> SP3
    SV3 --> SP4
    SV4 --> SP4
    
    %% 服务到数据组件
    SV1 --> DA1
    SV2 --> DA2
    SV3 --> DA3
    SV4 --> DA4
    SV2 --> DA5
    SV6 --> DA5
    
    %% 数据组件到存储
    DA1 --> ST1
    DA2 --> ST1
    DA3 --> ST1
    DA4 --> ST1
    DA5 --> ST2
    SV6 --> ST3
    SV2 --> ST4
    
    %% 服务到外部组件
    SV4 --> EX1
    SV4 --> EX2
    SV9 --> EX3
    FE1 --> EX4
    FE2 --> EX4
```

## 核心交互流程

### 1. 用户认证流程

```mermaid
sequenceDiagram
    participant U as 用户端
    participant G as API Gateway
    participant UC as UserController
    participant US as UserService
    participant SM as SecurityManager
    participant UM as UserMapper
    participant DB as MySQL
    participant R as Redis
    
    U->>G: 登录请求
    G->>UC: 路由转发
    UC->>US: 用户登录
    US->>UM: 查询用户信息
    UM->>DB: 执行SQL查询
    DB->>UM: 返回用户数据
    UM->>US: 返回用户信息
    US->>SM: 生成JWT令牌
    SM->>US: 返回令牌
    US->>R: 缓存用户会话
    US->>UC: 返回登录结果
    UC->>G: 响应数据
    G->>U: 登录成功响应
```

### 2. 商品浏览流程

```mermaid
sequenceDiagram
    participant U as 用户端
    participant G as API Gateway
    participant PC as ProductController
    participant PS as ProductService
    participant CM as CacheManager
    participant PM as ProductMapper
    participant R as Redis
    participant DB as MySQL
    
    U->>G: 商品列表请求
    G->>PC: 路由转发
    PC->>PS: 获取商品列表
    PS->>CM: 查询缓存
    CM->>R: 检查Redis缓存
    
    alt 缓存命中
        R->>CM: 返回缓存数据
        CM->>PS: 返回商品数据
    else 缓存未命中
        PS->>PM: 查询数据库
        PM->>DB: 执行SQL查询
        DB->>PM: 返回商品数据
        PM->>PS: 返回查询结果
        PS->>CM: 更新缓存
        CM->>R: 存储到Redis
    end
    
    PS->>PC: 返回商品列表
    PC->>G: 响应数据
    G->>U: 商品列表响应
```

### 3. 订单创建流程

```mermaid
sequenceDiagram
    participant U as 用户端
    participant G as API Gateway
    participant OC as OrderController
    participant OS as OrderService
    participant PS as ProductService
    participant US as UserService
    participant DL as DistributedLock
    participant SM as StateMachine
    participant EB as EventBus
    participant OM as OrderMapper
    participant DB as MySQL
    
    U->>G: 创建订单请求
    G->>OC: 路由转发
    OC->>OS: 创建订单
    OS->>DL: 获取分布式锁
    DL->>OS: 锁获取成功
    
    OS->>US: 验证用户信息
    US->>OS: 用户验证通过
    
    OS->>PS: 检查商品库存
    PS->>OS: 库存充足
    
    OS->>SM: 初始化订单状态
    SM->>OS: 状态设置完成
    
    OS->>OM: 保存订单数据
    OM->>DB: 执行插入操作
    DB->>OM: 插入成功
    OM->>OS: 返回订单ID
    
    OS->>PS: 锁定商品库存
    PS->>OS: 库存锁定成功
    
    OS->>EB: 发布订单创建事件
    EB->>OS: 事件发布成功
    
    OS->>DL: 释放分布式锁
    OS->>OC: 返回订单信息
    OC->>G: 响应数据
    G->>U: 订单创建成功
```

### 4. 支付处理流程

```mermaid
sequenceDiagram
    participant U as 用户端
    participant G as API Gateway
    participant PC as PaymentController
    participant PS as PaymentService
    participant OS as OrderService
    participant SM as StateMachine
    participant AP as Alipay API
    participant PM as PaymentMapper
    participant DB as MySQL
    participant EB as EventBus
    
    U->>G: 发起支付请求
    G->>PC: 路由转发
    PC->>PS: 创建支付
    PS->>OS: 验证订单状态
    OS->>PS: 订单状态有效
    
    PS->>PM: 创建支付记录
    PM->>DB: 插入支付数据
    DB->>PM: 插入成功
    PM->>PS: 返回支付ID
    
    PS->>AP: 调用支付宝API
    AP->>PS: 返回支付参数
    PS->>PC: 返回支付信息
    PC->>G: 响应支付参数
    G->>U: 支付参数响应
    
    Note over U,AP: 用户在支付宝完成支付
    
    AP->>PS: 支付回调通知
    PS->>PM: 更新支付状态
    PM->>DB: 更新支付记录
    PS->>SM: 触发订单状态变更
    SM->>OS: 订单状态更新
    PS->>EB: 发布支付成功事件
    PS->>AP: 返回回调确认
```

## 组件依赖关系

### Controller层依赖
```mermaid
graph TB
    subgraph "Controller层"
        UC[UserController]
        PC[ProductController]
        OC[OrderController]
        PayC[PaymentController]
        CC[CartController]
        SC[SearchController]
    end
    
    subgraph "Service层"
        US[UserService]
        PS[ProductService]
        OS[OrderService]
        PayS[PaymentService]
        CS[CartService]
        SS[SearchService]
    end
    
    UC --> US
    PC --> PS
    OC --> OS
    OC --> US
    OC --> PS
    PayC --> PayS
    PayC --> OS
    CC --> CS
    CC --> PS
    SC --> SS
    SC --> PS
```

### Service层依赖
```mermaid
graph TB
    subgraph "Service层"
        US[UserService]
        PS[ProductService]
        OS[OrderService]
        PayS[PaymentService]
        CS[CartService]
        SS[SearchService]
        CouS[CouponService]
        PoS[PointsService]
    end
    
    subgraph "Mapper层"
        UM[UserMapper]
        PM[ProductMapper]
        OM[OrderMapper]
        PayM[PaymentMapper]
        CM[CartMapper]
    end
    
    subgraph "支撑组件"
        Cache[CacheManager]
        Lock[DistributedLock]
        SM[StateMachine]
        EB[EventBus]
    end
    
    US --> UM
    US --> Cache
    PS --> PM
    PS --> Cache
    OS --> OM
    OS --> Lock
    OS --> SM
    OS --> EB
    PayS --> PayM
    PayS --> SM
    CS --> CM
    SS --> Cache
    CouS --> US
    PoS --> US
```

## 接口定义规范

### RESTful API设计
```yaml
用户相关接口:
  - POST /api/v1/users/register     # 用户注册
  - POST /api/v1/users/login        # 用户登录
  - GET  /api/v1/users/profile      # 获取用户信息
  - PUT  /api/v1/users/profile      # 更新用户信息

商品相关接口:
  - GET  /api/v1/products           # 商品列表
  - GET  /api/v1/products/{id}      # 商品详情
  - GET  /api/v1/categories         # 分类列表
  - GET  /api/v1/brands             # 品牌列表

订单相关接口:
  - POST /api/v1/orders             # 创建订单
  - GET  /api/v1/orders             # 订单列表
  - GET  /api/v1/orders/{id}        # 订单详情
  - PUT  /api/v1/orders/{id}/cancel # 取消订单

支付相关接口:
  - POST /api/v1/payments           # 创建支付
  - GET  /api/v1/payments/{id}      # 支付详情
  - POST /api/v1/payments/callback  # 支付回调
```

### 内部服务接口
```java
// 用户服务接口
public interface UserService {
    User getUserById(Integer userId);
    User getUserByUsername(String username);
    boolean validateUser(String username, String password);
    String generateToken(User user);
}

// 商品服务接口
public interface ProductService {
    List<Product> getProductsByCategory(Integer categoryId);
    Product getProductById(Integer productId);
    boolean checkStock(Integer productId, Integer quantity);
    void lockStock(Integer productId, Integer quantity);
}

// 订单服务接口
public interface OrderService {
    Order createOrder(OrderCreateRequest request);
    Order getOrderById(Integer orderId);
    void updateOrderStatus(Integer orderId, OrderStatus status);
    void cancelOrder(Integer orderId);
}
```

## 事件驱动架构

### 事件定义
```java
// 订单事件
public class OrderCreatedEvent {
    private Integer orderId;
    private Integer userId;
    private BigDecimal amount;
    private LocalDateTime createTime;
}

public class OrderPaidEvent {
    private Integer orderId;
    private Long paymentId;
    private LocalDateTime payTime;
}

// 支付事件
public class PaymentSuccessEvent {
    private Long paymentId;
    private Integer orderId;
    private BigDecimal amount;
    private String paymentMethod;
}
```

### 事件处理器
```java
@Component
public class OrderEventHandler {
    
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 发送订单确认通知
        // 更新用户积分
        // 记录操作日志
    }
    
    @EventListener
    public void handleOrderPaid(OrderPaidEvent event) {
        // 更新库存
        // 发送发货通知
        // 更新订单状态
    }
}
```

## 缓存交互策略

### 缓存层次结构
```mermaid
graph TB
    A[应用请求] --> B[本地缓存 L1]
    B --> C{命中?}
    C -->|是| D[返回数据]
    C -->|否| E[Redis缓存 L2]
    E --> F{命中?}
    F -->|是| G[更新L1缓存]
    G --> D
    F -->|否| H[数据库查询]
    H --> I[更新L2缓存]
    I --> G
```

### 缓存更新策略
```java
@Service
public class ProductService {
    
    @Cacheable(value = "products", key = "#productId")
    public Product getProductById(Integer productId) {
        return productMapper.selectById(productId);
    }
    
    @CacheEvict(value = "products", key = "#product.productId")
    public void updateProduct(Product product) {
        productMapper.updateById(product);
        // 发送缓存失效事件
        eventBus.publish(new CacheInvalidateEvent("products", product.getProductId()));
    }
}
```

## 异常处理机制

### 全局异常处理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.failed(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(ValidationException.class)
    public ApiResponse<Void> handleValidationException(ValidationException e) {
        return ApiResponse.failed(ApiErrorCodes.PARAM_ERROR, e.getMessage());
    }
}
```

### 服务间异常传播
```java
public class ServiceException extends RuntimeException {
    private String serviceCode;
    private String errorCode;
    private String errorMessage;
    
    // 异常链传播
    public ServiceException(String serviceCode, String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.serviceCode = serviceCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
```

---
*最后更新时间: 2025-06-18*
*维护者: 青柠檬*
