# 母婴商城用户业务流程

## 概述

本文档详细描述了母婴商城系统中用户相关的各种业务流程，包括用户注册、登录、信息管理、地址管理等核心用户操作流程。

## 用户注册流程

### 注册流程图

```mermaid
flowchart TD
    A[用户访问注册页面] --> B[填写注册信息]
    B --> C{信息格式验证}
    C -->|验证失败| D[显示错误信息]
    D --> B
    C -->|验证通过| E[提交注册请求]
    E --> F{用户名/邮箱/手机号唯一性检查}
    F -->|已存在| G[提示用户已存在]
    G --> B
    F -->|不存在| H[密码加密处理]
    H --> I[保存用户信息到数据库]
    I --> J[创建用户账户]
    J --> K[发送欢迎邮件/短信]
    K --> L[注册成功]
    L --> M[自动登录]
    M --> N[跳转到首页]
    
    style A fill:#e1f5fe
    style L fill:#c8e6c9
    style N fill:#c8e6c9
```

### 注册时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端
    participant G as API网关
    participant UC as UserController
    participant US as UserService
    participant UM as UserMapper
    participant DB as 数据库
    participant SMS as 短信服务
    
    U->>F: 填写注册信息
    F->>F: 前端表单验证
    F->>G: 提交注册请求
    G->>UC: 路由到用户控制器
    UC->>UC: 参数验证
    UC->>US: 调用注册服务
    US->>UM: 检查用户名唯一性
    UM->>DB: 查询用户表
    DB->>UM: 返回查询结果
    UM->>US: 返回检查结果
    
    alt 用户名已存在
        US->>UC: 返回错误信息
        UC->>G: 返回错误响应
        G->>F: 返回错误
        F->>U: 显示错误提示
    else 用户名不存在
        US->>US: 密码加密
        US->>UM: 保存用户信息
        UM->>DB: 插入用户记录
        DB->>UM: 返回插入结果
        UM->>US: 返回保存结果
        US->>SMS: 发送欢迎短信
        SMS->>US: 发送成功
        US->>UC: 返回注册成功
        UC->>G: 返回成功响应
        G->>F: 返回成功
        F->>U: 显示注册成功
    end
```

### 注册业务规则

| 字段 | 验证规则 | 错误提示 |
|------|----------|----------|
| 用户名 | 3-20字符，字母数字下划线 | 用户名格式不正确 |
| 密码 | 6-20字符，包含字母和数字 | 密码强度不够 |
| 邮箱 | 标准邮箱格式 | 邮箱格式不正确 |
| 手机号 | 11位数字，1开头 | 手机号格式不正确 |
| 验证码 | 6位数字，5分钟有效 | 验证码错误或已过期 |

## 用户登录流程

### 登录流程图

```mermaid
flowchart TD
    A[用户访问登录页面] --> B[选择登录方式]
    B --> C{登录方式}
    C -->|用户名密码| D[输入用户名和密码]
    C -->|手机号验证码| E[输入手机号]
    C -->|邮箱密码| F[输入邮箱和密码]
    
    D --> G[提交登录请求]
    E --> H[获取验证码]
    H --> I[输入验证码]
    I --> G
    F --> G
    
    G --> J{验证用户信息}
    J -->|验证失败| K[显示错误信息]
    K --> B
    J -->|验证成功| L[生成JWT令牌]
    L --> M[保存用户会话到Redis]
    M --> N[返回登录成功]
    N --> O[跳转到目标页面]
    
    style A fill:#e1f5fe
    style N fill:#c8e6c9
    style O fill:#c8e6c9
```

### 登录时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端
    participant G as API网关
    participant UC as UserController
    participant US as UserService
    participant UM as UserMapper
    participant DB as 数据库
    participant R as Redis
    participant JWT as JWT工具
    
    U->>F: 输入登录信息
    F->>G: 提交登录请求
    G->>UC: 路由到登录接口
    UC->>US: 调用登录服务
    US->>UM: 查询用户信息
    UM->>DB: 根据用户名查询
    DB->>UM: 返回用户数据
    UM->>US: 返回用户信息
    
    alt 用户不存在
        US->>UC: 返回用户不存在
        UC->>F: 返回错误响应
        F->>U: 显示错误提示
    else 用户存在
        US->>US: 验证密码
        alt 密码错误
            US->>UC: 返回密码错误
            UC->>F: 返回错误响应
            F->>U: 显示密码错误
        else 密码正确
            US->>JWT: 生成JWT令牌
            JWT->>US: 返回令牌
            US->>R: 保存用户会话
            R->>US: 保存成功
            US->>UC: 返回登录成功
            UC->>F: 返回成功响应和令牌
            F->>F: 保存令牌到本地存储
            F->>U: 跳转到首页
        end
    end
```

## 用户信息管理流程

### 个人信息修改流程

```mermaid
flowchart TD
    A[用户进入个人中心] --> B[查看当前信息]
    B --> C[点击编辑按钮]
    C --> D[修改个人信息]
    D --> E{信息格式验证}
    E -->|验证失败| F[显示错误信息]
    F --> D
    E -->|验证通过| G[提交修改请求]
    G --> H{权限验证}
    H -->|无权限| I[显示权限错误]
    I --> B
    H -->|有权限| J[更新用户信息]
    J --> K[清除相关缓存]
    K --> L[返回更新成功]
    L --> M[刷新页面显示]
    
    style A fill:#e1f5fe
    style L fill:#c8e6c9
    style M fill:#c8e6c9
```

### 头像上传流程

```mermaid
flowchart TD
    A[用户点击头像] --> B[选择上传方式]
    B --> C{上传方式}
    C -->|本地上传| D[选择本地图片]
    C -->|拍照上传| E[调用摄像头拍照]
    
    D --> F[图片格式和大小验证]
    E --> F
    F --> G{验证结果}
    G -->|验证失败| H[显示错误提示]
    H --> B
    G -->|验证通过| I[上传到对象存储]
    I --> J[生成图片URL]
    J --> K[更新用户头像字段]
    K --> L[删除旧头像文件]
    L --> M[返回上传成功]
    M --> N[更新页面显示]
    
    style A fill:#e1f5fe
    style M fill:#c8e6c9
    style N fill:#c8e6c9
```

## 收货地址管理流程

### 添加地址流程

```mermaid
flowchart TD
    A[用户进入地址管理] --> B[点击添加地址]
    B --> C[填写地址信息]
    C --> D[选择省市区]
    D --> E[填写详细地址]
    E --> F[设置默认地址选项]
    F --> G{信息完整性验证}
    G -->|验证失败| H[显示错误提示]
    H --> C
    G -->|验证通过| I[提交地址信息]
    I --> J{是否设为默认}
    J -->|是| K[取消其他默认地址]
    J -->|否| L[保存地址信息]
    K --> L
    L --> M[返回添加成功]
    M --> N[刷新地址列表]
    
    style A fill:#e1f5fe
    style M fill:#c8e6c9
    style N fill:#c8e6c9
```

### 地址管理时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant F as 前端
    participant AC as AddressController
    participant AS as AddressService
    participant AM as AddressMapper
    participant DB as 数据库
    
    U->>F: 添加收货地址
    F->>AC: 提交地址信息
    AC->>AS: 调用添加地址服务
    AS->>AM: 检查地址数量限制
    AM->>DB: 查询用户地址数量
    DB->>AM: 返回地址数量
    AM->>AS: 返回检查结果
    
    alt 地址数量超限
        AS->>AC: 返回数量超限错误
        AC->>F: 返回错误响应
        F->>U: 显示错误提示
    else 地址数量正常
        alt 设为默认地址
            AS->>AM: 取消其他默认地址
            AM->>DB: 更新其他地址为非默认
            DB->>AM: 更新成功
        end
        AS->>AM: 保存新地址
        AM->>DB: 插入地址记录
        DB->>AM: 插入成功
        AM->>AS: 返回保存结果
        AS->>AC: 返回添加成功
        AC->>F: 返回成功响应
        F->>U: 显示添加成功
    end
```

## 密码管理流程

### 修改密码流程

```mermaid
flowchart TD
    A[用户进入密码修改页面] --> B[输入当前密码]
    B --> C[输入新密码]
    C --> D[确认新密码]
    D --> E{密码格式验证}
    E -->|验证失败| F[显示格式错误]
    F --> C
    E -->|验证通过| G{新密码确认}
    G -->|不一致| H[显示密码不一致]
    H --> C
    G -->|一致| I[提交修改请求]
    I --> J{验证当前密码}
    J -->|验证失败| K[显示当前密码错误]
    K --> B
    J -->|验证通过| L[加密新密码]
    L --> M[更新密码到数据库]
    M --> N[清除用户会话]
    N --> O[返回修改成功]
    O --> P[跳转到登录页面]
    
    style A fill:#e1f5fe
    style O fill:#c8e6c9
    style P fill:#c8e6c9
```

### 忘记密码流程

```mermaid
flowchart TD
    A[用户点击忘记密码] --> B[输入邮箱或手机号]
    B --> C{账号验证}
    C -->|账号不存在| D[显示账号不存在]
    D --> B
    C -->|账号存在| E[发送验证码]
    E --> F[输入验证码]
    F --> G{验证码验证}
    G -->|验证失败| H[显示验证码错误]
    H --> F
    G -->|验证通过| I[输入新密码]
    I --> J[确认新密码]
    J --> K{密码格式验证}
    K -->|验证失败| L[显示格式错误]
    L --> I
    K -->|验证通过| M{密码确认}
    M -->|不一致| N[显示密码不一致]
    N --> I
    M -->|一致| O[更新密码]
    O --> P[清除所有会话]
    P --> Q[返回重置成功]
    Q --> R[跳转到登录页面]
    
    style A fill:#e1f5fe
    style Q fill:#c8e6c9
    style R fill:#c8e6c9
```

## 用户状态管理

### 用户状态流转图

```mermaid
stateDiagram-v2
    [*] --> 未激活: 注册成功
    未激活 --> 正常: 邮箱/手机验证
    未激活 --> 已删除: 长期未激活清理
    正常 --> 冻结: 违规操作
    正常 --> 已删除: 用户注销
    冻结 --> 正常: 申诉成功
    冻结 --> 已删除: 严重违规
    已删除 --> [*]
    
    正常: 可正常使用所有功能
    冻结: 限制登录和交易
    未激活: 需要验证激活
    已删除: 账号已删除
```

## 业务规则说明

### 用户注册规则
1. **唯一性约束**: 用户名、邮箱、手机号必须唯一
2. **密码强度**: 至少6位，包含字母和数字
3. **验证机制**: 邮箱或手机号验证激活
4. **防刷机制**: 同IP限制注册频率

### 登录安全规则
1. **失败限制**: 连续5次失败锁定30分钟
2. **会话管理**: JWT令牌24小时有效期
3. **设备限制**: 同时最多5个设备登录
4. **异地登录**: 异地登录需要验证

### 信息修改规则
1. **敏感信息**: 手机号、邮箱修改需要验证
2. **修改频率**: 24小时内最多修改3次
3. **审核机制**: 昵称、头像需要审核
4. **日志记录**: 所有修改操作记录日志

### 地址管理规则
1. **数量限制**: 每个用户最多20个收货地址
2. **默认地址**: 只能有一个默认地址
3. **地址验证**: 省市区必须真实有效
4. **删除限制**: 有未完成订单的地址不能删除

---
*最后更新时间: 2025-06-18*
*维护者: 青柠檬*
