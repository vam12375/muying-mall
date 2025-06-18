# 母婴商城支付处理流程

## 概述

本文档详细描述了母婴商城系统的支付处理流程，包括支付宝支付、微信支付、余额支付等多种支付方式的完整业务流程，以及支付回调、退款处理等关键环节。

## 支付状态机

### 支付状态流转图

```mermaid
stateDiagram-v2
    [*] --> 待支付: 创建支付
    待支付 --> 支付中: 发起支付
    待支付 --> 已取消: 取消支付/超时
    支付中 --> 支付成功: 支付完成
    支付中 --> 支付失败: 支付失败
    支付中 --> 已取消: 用户取消
    支付成功 --> 退款中: 申请退款
    支付失败 --> 待支付: 重新支付
    退款中 --> 已退款: 退款成功
    退款中 --> 支付成功: 退款失败
    已取消 --> [*]
    已退款 --> [*]
    支付成功 --> [*]: 交易完成
    
    待支付: 等待用户支付\n15分钟超时
    支付中: 支付处理中\n等待第三方响应
    支付成功: 支付完成\n可申请退款
    支付失败: 支付失败\n可重新支付
    已取消: 支付已取消
    退款中: 退款处理中
    已退款: 退款已完成
```

## 支付方式集成

### 支付宝支付流程

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端
    participant PC as PaymentController
    participant PS as PaymentService
    participant AP as 支付宝API
    participant DB as 数据库
    participant OS as OrderService
    
    U->>F: 选择支付宝支付
    F->>PC: 创建支付请求
    PC->>PS: 调用支付宝支付服务
    PS->>DB: 创建支付记录
    DB->>PS: 返回支付ID
    
    PS->>AP: 调用支付宝统一下单API
    AP->>PS: 返回支付参数
    PS->>PC: 返回支付信息
    PC->>F: 返回支付参数
    F->>U: 跳转支付宝支付页面
    
    Note over U,AP: 用户在支付宝完成支付
    
    AP->>PS: 异步通知支付结果
    PS->>DB: 更新支付状态
    PS->>OS: 通知订单支付成功
    OS->>DB: 更新订单状态
    PS->>AP: 返回通知确认
    
    U->>F: 支付完成返回
    F->>PC: 查询支付结果
    PC->>PS: 获取支付状态
    PS->>F: 返回支付成功
    F->>U: 显示支付成功页面
```

### 微信支付流程

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端
    participant PC as PaymentController
    participant PS as PaymentService
    participant WP as 微信支付API
    participant DB as 数据库
    participant OS as OrderService
    
    U->>F: 选择微信支付
    F->>PC: 创建支付请求
    PC->>PS: 调用微信支付服务
    PS->>DB: 创建支付记录
    DB->>PS: 返回支付ID
    
    PS->>WP: 调用微信统一下单API
    WP->>PS: 返回预支付交易会话标识
    PS->>PS: 生成支付签名
    PS->>PC: 返回支付参数
    PC->>F: 返回支付信息
    F->>U: 调起微信支付
    
    Note over U,WP: 用户在微信完成支付
    
    WP->>PS: 异步通知支付结果
    PS->>DB: 更新支付状态
    PS->>OS: 通知订单支付成功
    OS->>DB: 更新订单状态
    PS->>WP: 返回通知确认
    
    U->>F: 支付完成返回
    F->>PC: 查询支付结果
    PC->>PS: 获取支付状态
    PS->>F: 返回支付成功
    F->>U: 显示支付成功页面
```

### 余额支付流程

```mermaid
flowchart TD
    A[用户选择余额支付] --> B[验证用户身份]
    B --> C[检查账户余额]
    C --> D{余额是否充足}
    D -->|不足| E[显示余额不足]
    E --> F[跳转充值页面]
    D -->|充足| G[验证支付密码]
    G --> H{密码是否正确}
    H -->|错误| I[显示密码错误]
    I --> G
    H -->|正确| J[扣减账户余额]
    J --> K[创建支付记录]
    K --> L[更新订单状态]
    L --> M[发送支付成功通知]
    M --> N[支付完成]
    
    style A fill:#e1f5fe
    style N fill:#c8e6c9
```

## 支付创建流程

### 支付创建流程图

```mermaid
flowchart TD
    A[用户确认订单] --> B[选择支付方式]
    B --> C[验证订单状态]
    C --> D{订单状态检查}
    D -->|非待付款状态| E[显示订单状态错误]
    E --> F[返回订单页面]
    D -->|待付款状态| G[验证订单金额]
    G --> H[创建支付记录]
    H --> I[生成支付编号]
    I --> J{支付方式}
    J -->|第三方支付| K[调用第三方API]
    J -->|余额支付| L[执行余额支付]
    K --> M[返回支付参数]
    L --> N[直接完成支付]
    M --> O[跳转支付页面]
    N --> P[更新支付状态]
    P --> Q[通知订单服务]
    Q --> R[支付流程完成]
    
    style A fill:#e1f5fe
    style R fill:#c8e6c9
```

### 支付参数生成

```mermaid
flowchart TD
    A[接收支付请求] --> B[验证请求参数]
    B --> C[生成支付订单号]
    C --> D[计算支付金额]
    D --> E[设置支付超时时间]
    E --> F[生成回调URL]
    F --> G{支付方式}
    G -->|支付宝| H[生成支付宝参数]
    G -->|微信支付| I[生成微信支付参数]
    G -->|余额支付| J[生成余额支付参数]
    
    H --> K[添加支付宝签名]
    I --> L[添加微信支付签名]
    J --> M[验证账户余额]
    
    K --> N[返回支付参数]
    L --> N
    M --> N
    
    style A fill:#e1f5fe
    style N fill:#c8e6c9
```

## 支付回调处理

### 异步通知处理流程

```mermaid
flowchart TD
    A[接收第三方通知] --> B[验证通知签名]
    B --> C{签名验证}
    C -->|验证失败| D[记录异常日志]
    D --> E[返回失败响应]
    C -->|验证成功| F[解析通知参数]
    F --> G[查询支付记录]
    G --> H{支付记录存在}
    H -->|不存在| I[记录异常日志]
    I --> E
    H -->|存在| J[检查支付状态]
    J --> K{当前状态}
    K -->|已成功| L[返回成功响应]
    K -->|待支付/支付中| M[更新支付状态]
    M --> N[通知订单服务]
    N --> O[发送支付成功事件]
    O --> P[返回成功响应]
    
    style A fill:#e1f5fe
    style P fill:#c8e6c9
    style L fill:#c8e6c9
```

### 支付结果同步查询

```mermaid
sequenceDiagram
    participant F as 前端
    participant PC as PaymentController
    participant PS as PaymentService
    participant AP as 第三方API
    participant DB as 数据库
    
    F->>PC: 查询支付结果
    PC->>PS: 获取支付状态
    PS->>DB: 查询本地支付记录
    DB->>PS: 返回支付状态
    
    alt 本地状态为待支付
        PS->>AP: 主动查询第三方支付状态
        AP->>PS: 返回最新支付状态
        alt 第三方状态已成功
            PS->>DB: 更新本地支付状态
            PS->>PS: 触发支付成功处理
        end
    end
    
    PS->>PC: 返回最终支付状态
    PC->>F: 返回支付结果
```

## 退款处理流程

### 退款申请流程

```mermaid
flowchart TD
    A[用户申请退款] --> B[验证退款条件]
    B --> C{退款条件检查}
    C -->|不满足条件| D[显示退款条件不满足]
    D --> E[返回订单页面]
    C -->|满足条件| F[创建退款申请]
    F --> G[商家审核]
    G --> H{审核结果}
    H -->|拒绝| I[更新退款状态为已拒绝]
    I --> J[发送拒绝通知]
    H -->|同意| K[调用第三方退款API]
    K --> L{退款结果}
    L -->|成功| M[更新退款状态为已退款]
    L -->|失败| N[更新退款状态为退款失败]
    M --> O[发送退款成功通知]
    N --> P[人工处理退款]
    
    style A fill:#e1f5fe
    style O fill:#c8e6c9
```

### 退款时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端
    participant RC as RefundController
    participant RS as RefundService
    participant PS as PaymentService
    participant AP as 第三方API
    participant DB as 数据库
    participant NS as NotificationService
    
    U->>F: 申请退款
    F->>RC: 提交退款申请
    RC->>RS: 创建退款记录
    RS->>DB: 保存退款申请
    DB->>RS: 返回退款ID
    
    Note over RS: 商家审核通过
    
    RS->>PS: 调用退款服务
    PS->>AP: 调用第三方退款API
    AP->>PS: 返回退款结果
    
    alt 退款成功
        PS->>DB: 更新支付状态为已退款
        PS->>RS: 返回退款成功
        RS->>DB: 更新退款状态
        RS->>NS: 发送退款成功通知
        NS->>U: 推送退款成功消息
    else 退款失败
        PS->>DB: 记录退款失败原因
        PS->>RS: 返回退款失败
        RS->>NS: 发送退款失败通知
        NS->>U: 推送退款失败消息
    end
```

## 支付安全机制

### 防重复支付

```mermaid
flowchart TD
    A[接收支付请求] --> B[生成请求唯一标识]
    B --> C[检查Redis缓存]
    C --> D{是否存在相同请求}
    D -->|存在| E[返回重复请求错误]
    D -->|不存在| F[将请求标识存入Redis]
    F --> G[设置过期时间]
    G --> H[执行支付逻辑]
    H --> I[支付处理完成]
    I --> J[清除Redis标识]
    
    style A fill:#e1f5fe
    style I fill:#c8e6c9
```

### 支付金额验证

```mermaid
flowchart TD
    A[接收支付回调] --> B[获取订单信息]
    B --> C[计算订单应付金额]
    C --> D[获取实际支付金额]
    D --> E{金额是否一致}
    E -->|不一致| F[记录金额异常]
    F --> G[人工审核处理]
    E -->|一致| H[继续支付流程]
    H --> I[更新支付状态]
    
    style A fill:#e1f5fe
    style I fill:#c8e6c9
```

### 签名验证机制

```mermaid
flowchart TD
    A[接收第三方通知] --> B[提取通知参数]
    B --> C[按规则排序参数]
    C --> D[拼接参数字符串]
    D --> E[使用密钥生成签名]
    E --> F[与通知中的签名对比]
    F --> G{签名是否一致}
    G -->|不一致| H[拒绝处理通知]
    H --> I[记录安全日志]
    G -->|一致| J[继续处理通知]
    J --> K[更新支付状态]
    
    style A fill:#e1f5fe
    style K fill:#c8e6c9
```

## 支付监控和对账

### 支付状态监控

```mermaid
flowchart TD
    A[定时任务启动] --> B[查询待支付订单]
    B --> C[检查支付超时]
    C --> D{是否超时}
    D -->|未超时| E[继续监控]
    D -->|已超时| F[主动查询第三方状态]
    F --> G{第三方状态}
    G -->|已支付| H[更新本地状态]
    G -->|未支付| I[取消订单]
    H --> J[通知订单服务]
    I --> K[释放库存]
    J --> L[发送状态变更通知]
    K --> L
    
    style A fill:#e1f5fe
    style L fill:#c8e6c9
```

### 对账流程

```mermaid
flowchart TD
    A[每日凌晨对账] --> B[下载第三方对账单]
    B --> C[解析对账单数据]
    C --> D[与本地交易记录比对]
    D --> E{数据是否一致}
    E -->|一致| F[对账成功]
    E -->|不一致| G[生成差异报告]
    G --> H[人工核实差异]
    H --> I[调整账务数据]
    I --> J[记录调整日志]
    F --> K[生成对账报告]
    J --> K
    
    style A fill:#e1f5fe
    style K fill:#c8e6c9
```

## 业务规则说明

### 支付创建规则
1. **订单状态**: 只有待付款状态的订单才能创建支付
2. **金额验证**: 支付金额必须与订单金额完全一致
3. **支付超时**: 支付创建后15分钟内必须完成支付
4. **重复支付**: 同一订单不能重复创建支付
5. **支付方式**: 根据订单金额限制可用支付方式

### 支付回调规则
1. **签名验证**: 所有回调通知必须验证签名
2. **幂等处理**: 支持重复通知的幂等处理
3. **状态检查**: 只有待支付状态才能更新为成功
4. **异常处理**: 回调处理异常时记录详细日志
5. **响应格式**: 按第三方要求返回标准响应

### 退款规则
1. **退款时限**: 支付成功后180天内可申请退款
2. **退款金额**: 退款金额不能超过原支付金额
3. **退款次数**: 同一笔支付最多申请3次退款
4. **退款状态**: 只有支付成功状态才能申请退款
5. **审核机制**: 大额退款需要人工审核

### 安全规则
1. **防重放**: 使用时间戳和随机数防止重放攻击
2. **IP白名单**: 限制回调通知的来源IP
3. **频率限制**: 限制支付接口的调用频率
4. **敏感信息**: 支付密码等敏感信息加密存储
5. **日志记录**: 记录所有支付相关操作的详细日志

---
*最后更新时间: 2025-06-18*
*维护者: 青柠檬*
