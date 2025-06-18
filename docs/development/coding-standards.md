# 母婴商城编码规范

## 概述

本文档定义了母婴商城项目的编码规范和最佳实践，旨在提高代码质量、可读性和可维护性。所有开发人员都应严格遵循这些规范。

## Java编码规范

### 1. 命名规范

#### 类命名
```java
// ✅ 正确：使用PascalCase，名词或名词短语
public class UserService { }
public class OrderController { }
public class PaymentServiceImpl { }

// ❌ 错误
public class userservice { }
public class order_controller { }
```

#### 方法命名
```java
// ✅ 正确：使用camelCase，动词或动词短语
public User getUserById(Integer userId) { }
public boolean validatePassword(String password) { }
public void updateOrderStatus(Integer orderId, OrderStatus status) { }

// ❌ 错误
public User GetUserById(Integer userId) { }
public boolean validate_password(String password) { }
```

#### 变量命名
```java
// ✅ 正确：使用camelCase，有意义的名称
private String userName;
private List<Product> productList;
private BigDecimal totalAmount;

// ❌ 错误
private String un;
private List<Product> list;
private BigDecimal amt;
```

#### 常量命名
```java
// ✅ 正确：使用UPPER_SNAKE_CASE
public static final String DEFAULT_CHARSET = "UTF-8";
public static final int MAX_RETRY_COUNT = 3;
public static final long CACHE_EXPIRE_TIME = 3600L;

// ❌ 错误
public static final String defaultCharset = "UTF-8";
public static final int maxRetryCount = 3;
```

### 2. 代码格式

#### 缩进和空格
```java
// ✅ 正确：使用4个空格缩进
public class UserService {
    
    private UserMapper userMapper;
    
    public User getUserById(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return userMapper.selectById(userId);
    }
}
```

#### 大括号规范
```java
// ✅ 正确：左大括号不换行
if (condition) {
    doSomething();
} else {
    doSomethingElse();
}

// ❌ 错误：左大括号换行
if (condition)
{
    doSomething();
}
```

#### 行长度限制
```java
// ✅ 正确：每行不超过120字符，适当换行
public ApiResponse<PageInfo<Product>> getProductList(Integer categoryId, 
                                                    String keyword, 
                                                    Integer page, 
                                                    Integer size) {
    // 方法实现
}

// 长字符串换行
String message = "这是一个很长的错误消息，需要进行适当的换行处理，" +
                "以保证代码的可读性和维护性";
```

### 3. 注释规范

#### 类注释
```java
/**
 * 用户服务类
 * 
 * 提供用户相关的业务操作，包括用户注册、登录、信息管理等功能
 * 
 * @author 开发者姓名
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserService {
    // 类实现
}
```

#### 方法注释
```java
/**
 * 根据用户ID获取用户信息
 * 
 * @param userId 用户ID，不能为空
 * @return 用户信息，如果用户不存在返回null
 * @throws IllegalArgumentException 当userId为空时抛出
 */
public User getUserById(Integer userId) {
    if (userId == null) {
        throw new IllegalArgumentException("用户ID不能为空");
    }
    return userMapper.selectById(userId);
}
```

#### 行内注释
```java
// 检查用户权限
if (!hasPermission(user, operation)) {
    throw new AccessDeniedException("用户权限不足");
}

// TODO: 优化查询性能，考虑添加缓存
List<Product> products = productMapper.selectByCategory(categoryId);

// FIXME: 这里的逻辑有问题，需要重构
// 临时解决方案，后续需要优化
```

### 4. 异常处理

#### 异常捕获
```java
// ✅ 正确：具体的异常处理
try {
    User user = userService.getUserById(userId);
    return ApiResponse.success(user);
} catch (UserNotFoundException e) {
    log.warn("用户不存在: userId={}", userId);
    return ApiResponse.failed("用户不存在");
} catch (DatabaseException e) {
    log.error("数据库查询异常: userId={}", userId, e);
    return ApiResponse.failed("系统异常，请稍后重试");
}

// ❌ 错误：捕获过于宽泛的异常
try {
    // 业务逻辑
} catch (Exception e) {
    // 处理所有异常
}
```

#### 自定义异常
```java
/**
 * 业务异常基类
 */
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
```

### 5. 日志规范

#### 日志级别使用
```java
@Slf4j
@Service
public class UserService {
    
    public User createUser(UserCreateRequest request) {
        log.info("开始创建用户: username={}", request.getUsername());
        
        try {
            // 业务逻辑
            User user = doCreateUser(request);
            log.info("用户创建成功: userId={}, username={}", user.getUserId(), user.getUsername());
            return user;
        } catch (Exception e) {
            log.error("用户创建失败: username={}", request.getUsername(), e);
            throw new BusinessException("USER_CREATE_FAILED", "用户创建失败", e);
        }
    }
    
    private void validateUser(User user) {
        log.debug("验证用户信息: userId={}", user.getUserId());
        // 验证逻辑
    }
}
```

#### 日志格式规范
```java
// ✅ 正确：使用参数化日志
log.info("用户登录成功: userId={}, loginTime={}", userId, LocalDateTime.now());
log.error("支付处理失败: orderId={}, amount={}", orderId, amount, exception);

// ❌ 错误：字符串拼接
log.info("用户登录成功: userId=" + userId + ", loginTime=" + LocalDateTime.now());
```

## Spring Boot规范

### 1. 注解使用

#### Controller层
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
@Validated
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户信息")
    public ApiResponse<User> getUserById(
            @Parameter(description = "用户ID") @PathVariable Integer userId) {
        User user = userService.getUserById(userId);
        return ApiResponse.success(user);
    }
    
    @PostMapping
    @Operation(summary = "创建用户")
    public ApiResponse<User> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        User user = userService.createUser(request);
        return ApiResponse.success(user);
    }
}
```

#### Service层
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Transactional(rollbackFor = Exception.class)
    public User createUser(UserCreateRequest request) {
        // 业务逻辑实现
    }
    
    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Integer userId) {
        // 查询逻辑实现
    }
}
```

### 2. 配置管理

#### 配置属性类
```java
@ConfigurationProperties(prefix = "app.payment")
@Data
@Component
public class PaymentProperties {
    
    /**
     * 支付超时时间（分钟）
     */
    private Integer timeoutMinutes = 30;
    
    /**
     * 支付回调地址
     */
    private String callbackUrl;
    
    /**
     * 支付宝配置
     */
    private Alipay alipay = new Alipay();
    
    @Data
    public static class Alipay {
        private String appId;
        private String privateKey;
        private String publicKey;
    }
}
```

## 数据库规范

### 1. 表命名规范
```sql
-- ✅ 正确：使用小写字母和下划线
CREATE TABLE user_account (
    account_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ❌ 错误：使用大写或驼峰命名
CREATE TABLE UserAccount (
    AccountId INT,
    UserId INT
);
```

### 2. 字段命名规范
```sql
-- ✅ 正确：有意义的字段名
CREATE TABLE product (
    product_id INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    price_new DECIMAL(10,2) NOT NULL COMMENT '现价',
    price_old DECIMAL(10,2) NULL COMMENT '原价',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

-- ❌ 错误：缩写或无意义的字段名
CREATE TABLE product (
    id INT,
    name VARCHAR(100),
    price1 DECIMAL(10,2),
    price2 DECIMAL(10,2)
);
```

### 3. 索引规范
```sql
-- ✅ 正确：有意义的索引名
CREATE INDEX idx_user_email ON user(email);
CREATE INDEX idx_order_user_status ON `order`(user_id, status);
CREATE INDEX idx_product_category_status ON product(category_id, product_status);

-- ❌ 错误：无意义的索引名
CREATE INDEX index1 ON user(email);
CREATE INDEX idx ON `order`(user_id, status);
```

## API设计规范

### 1. RESTful API设计
```java
// ✅ 正确：RESTful风格
@GetMapping("/users")              // 获取用户列表
@GetMapping("/users/{id}")         // 获取单个用户
@PostMapping("/users")             // 创建用户
@PutMapping("/users/{id}")         // 更新用户
@DeleteMapping("/users/{id}")      // 删除用户

// ❌ 错误：非RESTful风格
@PostMapping("/getUserList")
@PostMapping("/getUserById")
@PostMapping("/createUser")
```

### 2. 响应格式规范
```java
/**
 * 统一API响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> ApiResponse<T> failed(String message) {
        return ApiResponse.<T>builder()
                .code(500)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
```

### 3. 参数验证规范
```java
@Data
@Schema(description = "用户创建请求")
public class UserCreateRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20字符之间")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20字符之间")
    @Schema(description = "密码", example = "123456")
    private String password;
    
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
}
```

## 代码质量检查

### 1. 静态代码分析
```xml
<!-- pom.xml中添加代码质量插件 -->
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.7.3.0</version>
</plugin>

<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.9.1.2184</version>
</plugin>
```

### 2. 代码覆盖率
```xml
<!-- JaCoCo代码覆盖率插件 -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## 代码审查清单

### 提交前检查
- [ ] 代码符合命名规范
- [ ] 方法和类有适当的注释
- [ ] 异常处理得当
- [ ] 日志记录合理
- [ ] 单元测试覆盖
- [ ] 无明显的性能问题
- [ ] 安全性考虑充分
- [ ] 代码格式化正确

### 代码审查要点
- [ ] 业务逻辑正确性
- [ ] 边界条件处理
- [ ] 错误处理机制
- [ ] 性能影响评估
- [ ] 安全漏洞检查
- [ ] 代码可读性
- [ ] 设计模式使用
- [ ] 依赖关系合理

---
*最后更新时间: 2025-06-18*
*维护者: 青柠檬*
