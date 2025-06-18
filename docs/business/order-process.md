# 母婴商城订单处理流程

## 概述

本文档详细描述了母婴商城系统中订单的完整生命周期，包括订单创建、支付、发货、配送、完成等各个环节的业务流程和状态管理。

## 订单状态机

### 订单状态流转图

```mermaid
stateDiagram-v2
    [*] --> 待付款: 创建订单
    待付款 --> 待发货: 支付成功
    待付款 --> 已取消: 取消订单/超时未付款
    待发货 --> 已发货: 商家发货
    待发货 --> 已取消: 取消订单
    已发货 --> 已完成: 确认收货
    已发货 --> 退款中: 申请退款
    已完成 --> 退款中: 申请退款
    退款中 --> 已退款: 退款成功
    退款中 --> 已完成: 拒绝退款
    已取消 --> [*]
    已退款 --> [*]
    已完成 --> [*]
    
    待付款: 等待用户付款\n超时自动取消
    待发货: 等待商家发货\n可申请取消
    已发货: 商品在配送中\n可申请退款
    已完成: 交易完成\n可申请退款
    已取消: 订单已取消
    退款中: 退款处理中
    已退款: 退款已完成
```

## 订单创建流程

### 订单创建流程图

```mermaid
flowchart TD
    A[用户点击结算] --> B[选择收货地址]
    B --> C[选择配送方式]
    C --> D[选择支付方式]
    D --> E[使用优惠券]
    E --> F[确认订单信息]
    F --> G{库存检查}
    G -->|库存不足| H[显示库存不足]
    H --> I[返回购物车]
    G -->|库存充足| J[锁定商品库存]
    J --> K[计算订单金额]
    K --> L[生成订单编号]
    L --> M[保存订单信息]
    M --> N[清空购物车]
    N --> O[跳转支付页面]
    
    style A fill:#e1f5fe
    style O fill:#c8e6c9
```

### 订单创建时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端
    participant OC as OrderController
    participant OS as OrderService
    participant PS as ProductService
    participant CS as CouponService
    participant DL as DistributedLock
    participant OM as OrderMapper
    participant DB as 数据库
    participant EB as EventBus
    
    U->>F: 点击结算
    F->>OC: 提交订单创建请求
    OC->>OS: 调用创建订单服务
    OS->>DL: 获取分布式锁
    DL->>OS: 锁获取成功
    
    OS->>PS: 检查商品库存
    PS->>OS: 库存检查结果
    
    alt 库存不足
        OS->>DL: 释放锁
        OS->>OC: 返回库存不足错误
        OC->>F: 返回错误响应
        F->>U: 显示库存不足
    else 库存充足
        OS->>PS: 锁定商品库存
        PS->>OS: 库存锁定成功
        
        OS->>CS: 验证优惠券
        CS->>OS: 优惠券验证结果
        
        OS->>OS: 计算订单金额
        OS->>OM: 保存订单信息
        OM->>DB: 插入订单记录
        DB->>OM: 插入成功
        OM->>OS: 返回订单ID
        
        OS->>EB: 发布订单创建事件
        EB->>OS: 事件发布成功
        
        OS->>DL: 释放锁
        OS->>OC: 返回订单创建成功
        OC->>F: 返回订单信息
        F->>U: 跳转支付页面
    end
```

## 订单支付流程

### 支付流程图

```mermaid
flowchart TD
    A[用户进入支付页面] --> B[选择支付方式]
    B --> C{支付方式}
    C -->|支付宝| D[调用支付宝API]
    C -->|微信支付| E[调用微信支付API]
    C -->|余额支付| F[验证账户余额]
    
    D --> G[获取支付参数]
    E --> G
    F --> H{余额是否充足}
    H -->|不足| I[显示余额不足]
    I --> B
    H -->|充足| J[扣减账户余额]
    
    G --> K[用户完成支付]
    J --> L[支付成功]
    K --> M{支付结果}
    M -->|支付成功| L
    M -->|支付失败| N[显示支付失败]
    N --> B
    
    L --> O[更新订单状态]
    O --> P[释放库存锁定]
    P --> Q[扣减实际库存]
    Q --> R[发送支付成功通知]
    R --> S[跳转订单详情]
    
    style A fill:#e1f5fe
    style L fill:#c8e6c9
    style S fill:#c8e6c9
```

## 订单发货流程

### 发货流程图

```mermaid
flowchart TD
    A[商家查看待发货订单] --> B[准备商品]
    B --> C[选择物流公司]
    C --> D[填写物流单号]
    D --> E[确认发货]
    E --> F[更新订单状态为已发货]
    F --> G[创建物流跟踪记录]
    G --> H[发送发货通知给用户]
    H --> I[同步物流信息]
    I --> J[订单进入配送状态]
    
    style A fill:#e1f5fe
    style J fill:#c8e6c9
```

### 发货时序图

```mermaid
sequenceDiagram
    participant M as 商家
    participant AC as AdminController
    participant OS as OrderService
    participant LS as LogisticsService
    participant NS as NotificationService
    participant OM as OrderMapper
    participant DB as 数据库
    participant SMS as 短信服务
    
    M->>AC: 确认发货
    AC->>OS: 调用发货服务
    OS->>OM: 更新订单状态
    OM->>DB: 更新订单为已发货
    DB->>OM: 更新成功
    OM->>OS: 返回更新结果
    
    OS->>LS: 创建物流记录
    LS->>DB: 插入物流信息
    DB->>LS: 插入成功
    LS->>OS: 返回物流ID
    
    OS->>NS: 发送发货通知
    NS->>SMS: 发送短信通知
    SMS->>NS: 发送成功
    NS->>OS: 通知发送完成
    
    OS->>AC: 返回发货成功
    AC->>M: 显示发货成功
```

## 订单配送流程

### 配送状态跟踪

```mermaid
flowchart TD
    A[商品已发货] --> B[物流公司揽收]
    B --> C[运输中]
    C --> D[到达配送中心]
    D --> E[派送中]
    E --> F[配送成功]
    F --> G[用户确认收货]
    G --> H[订单完成]
    
    C --> I[运输异常]
    I --> J[异常处理]
    J --> C
    
    E --> K[配送失败]
    K --> L[重新派送]
    L --> E
    
    style A fill:#e1f5fe
    style H fill:#c8e6c9
```

### 物流信息同步

```mermaid
sequenceDiagram
    participant LS as 物流系统
    participant API as 物流API
    participant SYS as 商城系统
    participant U as 用户
    
    loop 定时同步
        SYS->>API: 查询物流状态
        API->>LS: 获取最新状态
        LS->>API: 返回物流信息
        API->>SYS: 返回状态更新
        SYS->>SYS: 更新本地物流状态
        
        alt 状态有变化
            SYS->>U: 推送状态更新通知
        end
    end
```

## 订单完成流程

### 确认收货流程

```mermaid
flowchart TD
    A[用户收到商品] --> B{商品检查}
    B -->|商品正常| C[点击确认收货]
    B -->|商品异常| D[申请退换货]
    
    C --> E[更新订单状态为已完成]
    E --> F[释放交易资金]
    F --> G[增加用户积分]
    G --> H[发送评价邀请]
    H --> I[订单流程结束]
    
    D --> J[提交退换货申请]
    J --> K[等待商家处理]
    
    style A fill:#e1f5fe
    style I fill:#c8e6c9
```

### 自动确认收货

```mermaid
flowchart TD
    A[订单已发货] --> B[开始计时]
    B --> C{是否超过7天}
    C -->|否| D[继续等待]
    D --> C
    C -->|是| E[系统自动确认收货]
    E --> F[更新订单状态]
    F --> G[释放交易资金]
    G --> H[发送确认通知]
    H --> I[订单自动完成]
    
    style A fill:#e1f5fe
    style I fill:#c8e6c9
```

## 订单取消流程

### 取消订单流程图

```mermaid
flowchart TD
    A[用户申请取消订单] --> B{订单状态检查}
    B -->|待付款| C[直接取消订单]
    B -->|待发货| D[申请取消审核]
    B -->|已发货| E[不允许取消]
    
    C --> F[更新订单状态为已取消]
    F --> G[释放库存锁定]
    G --> H[取消成功]
    
    D --> I{商家审核}
    I -->|同意| J[取消订单]
    I -->|拒绝| K[拒绝取消]
    
    J --> L[退还支付金额]
    L --> M[更新订单状态]
    M --> N[发送取消通知]
    N --> O[取消完成]
    
    E --> P[提示不能取消]
    K --> P
    
    style A fill:#e1f5fe
    style H fill:#c8e6c9
    style O fill:#c8e6c9
```

### 超时自动取消

```mermaid
flowchart TD
    A[订单创建] --> B[设置支付超时时间]
    B --> C[开始倒计时]
    C --> D{是否已支付}
    D -->|已支付| E[停止倒计时]
    D -->|未支付| F{是否超时}
    F -->|未超时| G[继续倒计时]
    G --> D
    F -->|已超时| H[自动取消订单]
    H --> I[释放库存锁定]
    I --> J[发送取消通知]
    J --> K[订单自动取消完成]
    
    style A fill:#e1f5fe
    style E fill:#c8e6c9
    style K fill:#ffcdd2
```

## 退款处理流程

### 退款申请流程

```mermaid
flowchart TD
    A[用户申请退款] --> B[填写退款原因]
    B --> C[上传凭证图片]
    C --> D[提交退款申请]
    D --> E[创建退款记录]
    E --> F[商家审核]
    F --> G{审核结果}
    G -->|同意退款| H[处理退款]
    G -->|拒绝退款| I[拒绝退款]
    
    H --> J[调用支付平台退款API]
    J --> K{退款结果}
    K -->|成功| L[更新退款状态]
    K -->|失败| M[退款失败处理]
    
    L --> N[发送退款成功通知]
    N --> O[退款完成]
    
    I --> P[发送拒绝通知]
    M --> Q[人工处理]
    
    style A fill:#e1f5fe
    style O fill:#c8e6c9
```

## 订单查询和管理

### 订单列表查询

```mermaid
flowchart TD
    A[用户进入订单页面] --> B[选择订单状态筛选]
    B --> C[设置时间范围]
    C --> D[输入搜索关键词]
    D --> E[提交查询请求]
    E --> F[后端处理查询]
    F --> G[分页返回订单列表]
    G --> H[前端展示订单]
    H --> I[用户操作订单]
    
    I --> J{操作类型}
    J -->|查看详情| K[跳转订单详情]
    J -->|取消订单| L[执行取消流程]
    J -->|申请退款| M[执行退款流程]
    J -->|确认收货| N[执行确认收货]
    J -->|评价商品| O[跳转评价页面]
    
    style A fill:#e1f5fe
```

## 业务规则说明

### 订单创建规则
1. **库存检查**: 创建订单前必须检查商品库存
2. **价格锁定**: 订单创建时锁定商品价格
3. **地址验证**: 收货地址必须完整有效
4. **优惠券限制**: 每个订单最多使用一张优惠券
5. **最小金额**: 订单金额不能低于配送费

### 支付规则
1. **支付超时**: 订单创建后30分钟内必须完成支付
2. **重复支付**: 防止订单重复支付
3. **金额验证**: 支付金额必须与订单金额一致
4. **状态检查**: 只有待付款状态的订单才能支付

### 发货规则
1. **发货时限**: 支付成功后48小时内必须发货
2. **物流信息**: 发货时必须填写完整物流信息
3. **状态限制**: 只有待发货状态的订单才能发货
4. **库存扣减**: 发货时扣减实际库存

### 取消规则
1. **取消时限**: 待付款状态可随时取消，待发货状态需商家同意
2. **退款处理**: 已支付订单取消后自动退款
3. **库存恢复**: 取消订单后恢复库存
4. **取消次数**: 限制用户恶意取消订单的次数

### 退款规则
1. **退款时限**: 收货后7天内可申请退款
2. **退款原因**: 必须提供合理的退款原因
3. **商品状态**: 退款商品必须保持原有状态
4. **退款金额**: 退款金额不超过实际支付金额

---
*最后更新时间: 2025-06-18*
*维护者: 青柠檬*
