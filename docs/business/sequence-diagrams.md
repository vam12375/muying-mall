# 母婴商城关键业务时序图

## 概述

本文档展示了母婴商城系统中关键业务场景的时序图，详细描述了各个组件之间的交互时序和数据流向，为系统开发和维护提供参考。

## 用户认证时序图

### 用户注册时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端应用
    participant G as API网关
    participant UC as UserController
    participant US as UserService
    participant UM as UserMapper
    participant DB as MySQL数据库
    participant R as Redis缓存
    participant SMS as 短信服务
    participant E as 事件总线
    
    U->>F: 填写注册信息
    F->>F: 前端表单验证
    F->>G: POST /api/v1/users/register
    G->>G: 路由转发和限流检查
    G->>UC: 转发注册请求
    
    UC->>UC: 参数验证和格式检查
    UC->>US: 调用用户注册服务
    
    US->>UM: 检查用户名唯一性
    UM->>DB: SELECT * FROM user WHERE username = ?
    DB->>UM: 返回查询结果
    UM->>US: 返回唯一性检查结果
    
    alt 用户名已存在
        US->>UC: 返回用户名已存在错误
        UC->>G: 返回400错误响应
        G->>F: 返回错误信息
        F->>U: 显示用户名已存在提示
    else 用户名不存在
        US->>US: BCrypt加密密码
        US->>UM: 保存用户信息
        UM->>DB: INSERT INTO user VALUES(...)
        DB->>UM: 返回插入结果
        UM->>US: 返回用户ID
        
        US->>R: 缓存用户基本信息
        R->>US: 缓存成功
        
        US->>SMS: 发送注册成功短信
        SMS->>US: 短信发送成功
        
        US->>E: 发布用户注册事件
        E->>E: 异步处理注册后续任务
        
        US->>UC: 返回注册成功
        UC->>G: 返回201成功响应
        G->>F: 返回成功信息
        F->>U: 显示注册成功，跳转登录
    end
```

### 用户登录时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端应用
    participant G as API网关
    participant UC as UserController
    participant US as UserService
    participant UM as UserMapper
    participant DB as MySQL数据库
    participant R as Redis缓存
    participant JWT as JWT工具类
    participant SEC as SecurityManager
    
    U->>F: 输入用户名和密码
    F->>G: POST /api/v1/users/login
    G->>SEC: 安全过滤器预处理
    SEC->>G: 通过安全检查
    G->>UC: 转发登录请求
    
    UC->>US: 调用用户登录服务
    US->>R: 检查用户缓存
    R->>US: 返回缓存结果
    
    alt 缓存命中
        US->>US: 验证密码
    else 缓存未命中
        US->>UM: 查询用户信息
        UM->>DB: SELECT * FROM user WHERE username = ?
        DB->>UM: 返回用户数据
        UM->>US: 返回用户信息
        US->>R: 更新用户缓存
        US->>US: 验证密码
    end
    
    alt 密码验证失败
        US->>R: 记录登录失败次数
        US->>UC: 返回密码错误
        UC->>G: 返回401错误响应
        G->>F: 返回认证失败
        F->>U: 显示密码错误提示
    else 密码验证成功
        US->>JWT: 生成JWT访问令牌
        JWT->>US: 返回JWT令牌
        
        US->>R: 保存用户会话信息
        R->>US: 会话保存成功
        
        US->>UM: 更新最后登录时间
        UM->>DB: UPDATE user SET last_login_time = NOW()
        
        US->>UC: 返回登录成功和令牌
        UC->>G: 返回200成功响应
        G->>F: 返回令牌和用户信息
        F->>F: 保存令牌到本地存储
        F->>U: 跳转到首页
    end
```

## 商品浏览时序图

### 商品列表查询时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端应用
    participant G as API网关
    participant PC as ProductController
    participant PS as ProductService
    participant CM as CacheManager
    participant PM as ProductMapper
    participant R as Redis缓存
    participant DB as MySQL数据库
    participant ES as Elasticsearch
    
    U->>F: 浏览商品分类
    F->>G: GET /api/v1/products?categoryId=1&page=1
    G->>PC: 转发商品查询请求
    
    PC->>PS: 调用商品列表服务
    PS->>CM: 检查商品列表缓存
    CM->>R: 查询Redis缓存
    R->>CM: 返回缓存结果
    
    alt 缓存命中
        CM->>PS: 返回缓存的商品列表
        PS->>PC: 返回商品数据
    else 缓存未命中
        PS->>PM: 查询数据库商品列表
        PM->>DB: SELECT * FROM product WHERE category_id = ? LIMIT ?, ?
        DB->>PM: 返回商品数据
        PM->>PS: 返回查询结果
        
        PS->>CM: 更新商品列表缓存
        CM->>R: 存储到Redis，设置过期时间
        R->>CM: 缓存更新成功
        
        PS->>PC: 返回商品数据
    end
    
    PC->>G: 返回商品列表响应
    G->>F: 返回商品数据
    F->>U: 展示商品列表页面
    
    Note over U,F: 用户点击商品进入详情
    
    U->>F: 点击商品详情
    F->>G: GET /api/v1/products/123
    G->>PC: 转发商品详情请求
    PC->>PS: 调用商品详情服务
    
    PS->>CM: 检查商品详情缓存
    CM->>R: 查询Redis缓存
    
    alt 缓存命中
        R->>CM: 返回缓存的商品详情
        CM->>PS: 返回商品详情
    else 缓存未命中
        PS->>PM: 查询商品详情
        PM->>DB: SELECT * FROM product WHERE product_id = ?
        DB->>PM: 返回商品详情
        PM->>PS: 返回查询结果
        
        PS->>CM: 更新商品详情缓存
        CM->>R: 存储详情到Redis
    end
    
    PS->>PC: 返回商品详情
    PC->>G: 返回详情响应
    G->>F: 返回商品详情
    F->>U: 展示商品详情页面
```

### 商品搜索时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端应用
    participant G as API网关
    participant SC as SearchController
    participant SS as SearchService
    participant ES as Elasticsearch
    participant PS as ProductService
    participant R as Redis缓存
    
    U->>F: 输入搜索关键词
    F->>G: GET /api/v1/search?keyword=奶粉&page=1
    G->>SC: 转发搜索请求
    
    SC->>SS: 调用商品搜索服务
    SS->>SS: 构建Elasticsearch查询条件
    SS->>ES: 执行全文搜索查询
    ES->>SS: 返回搜索结果
    
    SS->>SS: 解析搜索结果，提取商品ID列表
    SS->>PS: 批量获取商品详细信息
    PS->>R: 批量查询商品缓存
    R->>PS: 返回部分缓存数据
    
    alt 部分商品未缓存
        PS->>PS: 查询未缓存的商品信息
        PS->>R: 更新商品缓存
    end
    
    PS->>SS: 返回完整商品信息
    SS->>SS: 合并搜索结果和商品信息
    SS->>SC: 返回搜索结果
    
    SC->>G: 返回搜索响应
    G->>F: 返回搜索结果
    F->>U: 展示搜索结果页面
```

## 购物车操作时序图

### 添加商品到购物车时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端应用
    participant G as API网关
    participant CC as CartController
    participant CS as CartService
    participant PS as ProductService
    participant CM as CartMapper
    participant DB as MySQL数据库
    participant R as Redis缓存
    
    U->>F: 点击加入购物车
    F->>G: POST /api/v1/cart/items
    G->>G: JWT令牌验证
    G->>CC: 转发购物车请求
    
    CC->>CS: 调用添加购物车服务
    CS->>PS: 验证商品信息和库存
    PS->>CS: 返回商品验证结果
    
    alt 商品不存在或库存不足
        CS->>CC: 返回商品不可用错误
        CC->>G: 返回400错误响应
        G->>F: 返回错误信息
        F->>U: 显示商品不可用提示
    else 商品可用
        CS->>CM: 检查购物车中是否已存在该商品
        CM->>DB: SELECT * FROM cart WHERE user_id = ? AND product_id = ?
        DB->>CM: 返回查询结果
        CM->>CS: 返回检查结果
        
        alt 商品已存在
            CS->>CM: 更新商品数量
            CM->>DB: UPDATE cart SET quantity = quantity + ?
        else 商品不存在
            CS->>CM: 新增购物车记录
            CM->>DB: INSERT INTO cart VALUES(...)
        end
        
        DB->>CM: 返回操作结果
        CM->>CS: 返回成功
        
        CS->>R: 清除用户购物车缓存
        R->>CS: 缓存清除成功
        
        CS->>CC: 返回添加成功
        CC->>G: 返回201成功响应
        G->>F: 返回成功信息
        F->>U: 显示添加成功提示
    end
```

## 订单处理时序图

### 订单创建时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端应用
    participant G as API网关
    participant OC as OrderController
    participant OS as OrderService
    participant PS as ProductService
    participant US as UserService
    participant CS as CouponService
    participant DL as DistributedLock
    participant OM as OrderMapper
    participant DB as MySQL数据库
    participant EB as EventBus
    participant R as Redis缓存
    
    U->>F: 点击结算
    F->>G: POST /api/v1/orders
    G->>OC: 转发订单创建请求
    
    OC->>OS: 调用订单创建服务
    OS->>DL: 获取分布式锁(用户ID)
    DL->>OS: 锁获取成功
    
    OS->>US: 验证用户信息和收货地址
    US->>OS: 用户信息验证通过
    
    OS->>PS: 批量检查商品库存
    PS->>PS: 逐个验证商品库存
    PS->>OS: 返回库存检查结果
    
    alt 库存不足
        OS->>DL: 释放分布式锁
        OS->>OC: 返回库存不足错误
        OC->>F: 返回错误响应
        F->>U: 显示库存不足提示
    else 库存充足
        OS->>PS: 锁定商品库存
        PS->>R: 更新库存锁定缓存
        PS->>OS: 库存锁定成功
        
        OS->>CS: 验证和使用优惠券
        CS->>OS: 优惠券处理结果
        
        OS->>OS: 计算订单总金额
        OS->>OM: 保存订单主信息
        OM->>DB: INSERT INTO `order` VALUES(...)
        DB->>OM: 返回订单ID
        
        OS->>OM: 保存订单商品信息
        OM->>DB: INSERT INTO order_product VALUES(...)
        DB->>OM: 保存成功
        
        OS->>EB: 发布订单创建事件
        EB->>EB: 异步处理后续任务
        
        OS->>DL: 释放分布式锁
        OS->>OC: 返回订单创建成功
        OC->>G: 返回201成功响应
        G->>F: 返回订单信息
        F->>U: 跳转支付页面
    end
```

### 支付处理时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端应用
    participant G as API网关
    participant PC as PaymentController
    participant PS as PaymentService
    participant OS as OrderService
    participant AP as 支付宝API
    participant PM as PaymentMapper
    participant DB as MySQL数据库
    participant EB as EventBus
    participant NS as NotificationService
    
    U->>F: 选择支付方式并确认支付
    F->>G: POST /api/v1/payments
    G->>PC: 转发支付创建请求
    
    PC->>PS: 调用支付创建服务
    PS->>OS: 验证订单状态
    OS->>PS: 订单状态验证通过
    
    PS->>PM: 创建支付记录
    PM->>DB: INSERT INTO payment VALUES(...)
    DB->>PM: 返回支付ID
    PM->>PS: 返回支付记录
    
    PS->>AP: 调用支付宝统一下单API
    AP->>PS: 返回支付参数
    PS->>PC: 返回支付信息
    PC->>G: 返回支付参数
    G->>F: 返回支付数据
    F->>U: 跳转支付宝支付页面
    
    Note over U,AP: 用户在支付宝完成支付
    
    AP->>PS: 异步通知支付结果
    PS->>PS: 验证通知签名
    PS->>PM: 更新支付状态
    PM->>DB: UPDATE payment SET status = 'SUCCESS'
    DB->>PM: 更新成功
    
    PS->>OS: 通知订单支付成功
    OS->>DB: UPDATE `order` SET status = 'pending_shipment'
    OS->>PS: 订单状态更新成功
    
    PS->>EB: 发布支付成功事件
    EB->>NS: 异步发送支付成功通知
    NS->>U: 推送支付成功消息
    
    PS->>AP: 返回通知确认
    
    Note over U,F: 用户返回商城查看支付结果
    
    U->>F: 查看支付结果
    F->>G: GET /api/v1/payments/{paymentId}
    G->>PC: 转发查询请求
    PC->>PS: 查询支付状态
    PS->>PC: 返回支付成功状态
    PC->>F: 返回支付结果
    F->>U: 显示支付成功页面
```

---
*最后更新时间: 2025-06-18*
*维护者: 青柠檬*
