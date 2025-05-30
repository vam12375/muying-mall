# 母婴商城开发最佳实践

本文档提供了母婴商城项目的开发最佳实践指南，旨在规范开发流程，提高代码质量，确保系统安全性和性能。所有项目开发人员应当遵循这些准则。

## 代码风格与规范

### Java代码规范

1. **命名规范**
   - 类名：使用PascalCase（首字母大写驼峰式），如`UserService`
   - 接口名：使用PascalCase，通常以I开头或者不加前缀，如`IUserService`或`UserService`
   - 方法名：使用camelCase（首字母小写驼峰式），如`getUserById`
   - 常量：全部大写，单词间用下划线分隔，如`MAX_RETRY_COUNT`
   - 变量：使用camelCase，如`userName`
   - 包名：全部小写，如`com.muyingmall.user`

2. **代码格式化**
   - 使用项目提供的代码格式化模板（IntelliJ IDEA: `idea-code-style.xml`）
   - 缩进使用4个空格，不使用Tab
   - 行宽不超过120个字符
   - 方法之间空一行
   - 相关的变量声明放在一起
   - 类的成员顺序：静态变量 > 实例变量 > 构造方法 > 公共方法 > 私有方法

3. **注释规范**
   - 所有公共API必须有JavaDoc注释
   - 复杂的业务逻辑需要添加注释说明
   - 使用`TODO:`标记待完成的任务，使用`FIXME:`标记需要修复的问题
   - 注释应该说明"为什么"这样做，而不仅仅是"做了什么"

```java
/**
 * 用户服务接口，提供用户相关的核心业务逻辑
 * 
 * @author 开发者姓名
 * @since 1.0.0
 */
public interface UserService {
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID，不能为null
     * @return 用户信息，如果用户不存在则返回null
     * @throws IllegalArgumentException 如果userId为null
     */
    UserDTO getUserById(Long userId);
    
    // 其他方法...
}
```

### SQL规范

1. **命名规范**
   - 表名：使用小写，单词间用下划线分隔，使用单数形式，如`user_order`
   - 字段名：使用小写，单词间用下划线分隔，如`create_time`
   - 索引：以`idx_`开头，如`idx_user_id`
   - 主键：以`pk_`开头，如`pk_user_id`
   - 外键：以`fk_`开头，如`fk_order_user_id`

2. **SQL编写规范**
   - 关键字大写，如`SELECT`, `UPDATE`, `WHERE`
   - 使用参数化查询，不直接拼接SQL
   - 复杂查询添加注释
   - 注意避免全表扫描和笛卡尔积查询

### 前端代码规范（如果有前端）

1. **命名规范**
   - 组件名：使用PascalCase，如`UserProfile`
   - 文件名：使用kebab-case（小写中划线），如`user-profile.vue`
   - CSS类名：使用BEM命名规范，如`.user-profile__avatar`

2. **代码结构**
   - 按功能模块组织代码
   - 组件化开发，提高复用性
   - CSS使用SCSS预处理器，遵循模块化原则

## 架构设计原则

1. **分层架构**
   - 严格遵循分层架构，每层只能调用下一层提供的接口
   - Controller层：处理请求和响应，参数校验，不包含业务逻辑
   - Service层：实现业务逻辑，事务管理
   - DAO层：数据访问，仅包含与数据库交互的代码
   - Entity/Model层：实体类，与数据表对应

2. **接口设计**
   - 遵循RESTful API设计规范
   - 版本控制：URL中使用`/v1/`, `/v2/`等标识API版本
   - 统一响应格式：包含状态码、消息和数据体
   - 分页查询统一使用`page`和`size`参数

3. **模块化设计**
   - 功能模块间低耦合，高内聚
   - 通过接口暴露模块功能，隐藏实现细节
   - 避免循环依赖

## 安全最佳实践

1. **输入验证**
   - 所有外部输入都必须进行验证
   - 使用Bean Validation进行参数验证
   - 对特殊字符进行转义，防止XSS攻击

```java
@Data
public class UserRegistrationRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Length(min = 4, max = 20, message = "用户名长度必须在4-20之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Length(min = 8, max = 20, message = "密码长度必须在8-20之间")
    private String password;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
```

2. **身份认证与授权**
   - 使用JWT进行无状态认证
   - 敏感操作必须进行权限验证
   - 使用@PreAuthorize注解进行权限控制
   - 避免硬编码角色和权限

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ORDER_READ') or @orderPermissionEvaluator.isOrderOwner(authentication, #orderId)")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId) {
        // 业务逻辑
    }
}
```

3. **密码处理**
   - 使用BCrypt等强哈希算法存储密码
   - 不在日志中记录密码
   - 定期要求用户更改密码
   - 实施密码强度策略

4. **数据保护**
   - 敏感数据传输使用HTTPS
   - 敏感字段加密存储，如支付信息
   - 个人敏感信息脱敏展示，如手机号、身份证号

5. **安全配置**
   - 关闭生产环境调试功能
   - 限制失败登录尝试次数
   - 实施CSRF保护
   - 设置适当的Content Security Policy

## 性能优化

1. **数据库优化**
   - 合理设计索引，避免过度索引
   - 大查询分页处理
   - 使用批处理插入和更新
   - 避免在循环中执行SQL
   - 定期分析慢查询

```java
// 错误示例
for (Order order : orders) {
    orderRepository.save(order); // 每次循环都会执行一次SQL
}

// 正确示例
orderRepository.saveAll(orders); // 使用批处理，只执行一次SQL
```

2. **缓存策略**
   - 热点数据使用Redis缓存
   - 实现多级缓存：本地缓存 -> 分布式缓存 -> 数据库
   - 设置合理的缓存过期时间
   - 更新数据时同步更新缓存

```java
@Service
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    @Cacheable(value = "product", key = "#productId", unless = "#result == null")
    public ProductDTO getProductById(Long productId) {
        // 先查缓存，缓存不存在则查数据库并放入缓存
        return productRepository.findById(productId)
                .map(this::convertToDTO)
                .orElse(null);
    }
    
    @Override
    @CacheEvict(value = "product", key = "#product.id")
    public void updateProduct(ProductDTO product) {
        // 更新数据库，同时清除缓存
        productRepository.save(convertToEntity(product));
    }
}
```

3. **代码优化**
   - 避免创建不必要的对象
   - 使用StringBuilder而不是String拼接
   - 集合类初始化时指定初始容量
   - 批量处理大数据集
   - 异步处理耗时操作

4. **JVM优化**
   - 根据系统需求配置堆内存大小
   - 选择合适的垃圾收集器
   - 定期进行JVM参数调优
   - 监控GC活动，避免频繁Full GC

## 日志最佳实践

1. **日志配置**
   - 不同环境使用不同日志级别：开发环境DEBUG，生产环境INFO或WARN
   - 使用异步日志减少I/O阻塞
   - 实现日志轮转，防止单个日志文件过大
   - 设置日志保留策略

2. **日志内容**
   - 记录有价值的信息，避免过度日志
   - 包含上下文信息：用户ID、请求ID、操作类型
   - 异常日志记录完整堆栈信息
   - 敏感信息脱敏处理

```java
try {
    // 业务逻辑
} catch (Exception e) {
    log.error("处理订单时发生错误，订单ID: {}, 用户ID: {}", orderId, userId, e);
    throw new BusinessException("订单处理失败", e);
}
```

3. **审计日志**
   - 记录关键业务操作：登录、支付、修改敏感信息
   - 包含操作人、操作时间、操作内容、操作结果
   - 审计日志不可删除和修改

```java
@Aspect
@Component
public class AuditLogAspect {
    
    @Autowired
    private AuditLogService auditLogService;
    
    @AfterReturning("@annotation(auditLog)")
    public void logAction(JoinPoint joinPoint, AuditLog auditLog) {
        // 记录审计日志
        AuditLogDTO log = new AuditLogDTO();
        log.setAction(auditLog.action());
        log.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        log.setTimestamp(LocalDateTime.now());
        log.setResult("成功");
        // 设置其他信息...
        
        auditLogService.saveLog(log);
    }
}
```

## 测试策略

1. **单元测试**
   - 使用JUnit 5和Mockito进行单元测试
   - 每个服务类至少80%的测试覆盖率
   - 测试方法命名规则：`should<预期结果>_when<条件>`
   - 单元测试应该独立，不依赖外部资源

```java
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void shouldReturnUser_whenUserExists() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // Act
        UserDTO result = userService.getUserById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }
}
```

2. **集成测试**
   - 使用@SpringBootTest测试组件集成
   - 使用测试专用数据库（H2或测试环境数据库）
   - 测试主要业务流程和异常场景

3. **API测试**
   - 使用MockMvc或RestAssured测试REST接口
   - 验证请求参数验证、响应格式和状态码
   - 测试权限控制和安全限制

```java
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(username = "admin", authorities = {"ORDER_READ"})
    void shouldGetOrder_whenAuthorized() throws Exception {
        mockMvc.perform(get("/api/v1/orders/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
```

## 版本控制与协作

1. **Git工作流**
   - 主分支（master/main）：只接受合并请求，不直接提交
   - 开发分支（develop）：集成测试通过的功能
   - 功能分支（feature/xxx）：新功能开发
   - 发布分支（release/x.x.x）：版本发布准备
   - 修复分支（hotfix/xxx）：紧急生产问题修复

2. **提交规范**
   - 提交消息格式：`<类型>: <简短描述>`
   - 类型：feat（新功能）、fix（修复）、docs（文档）、style（格式）、refactor（重构）、perf（性能）、test（测试）、chore（构建/工具）
   - 每个提交应该只做一件事，保持原子性

```
feat: 添加用户注册功能
fix: 修复订单状态更新bug
docs: 更新API文档
refactor: 重构商品服务实现类
```

3. **代码审查**
   - 所有代码必须经过至少一人审查才能合并
   - 审查重点：功能正确性、代码质量、安全问题、测试覆盖
   - 使用GitLab/GitHub的Merge/Pull Request功能

## 持续集成与部署

1. **CI/CD流程**
   - 提交代码触发自动构建和测试
   - 代码质量检查：使用SonarQube进行静态代码分析
   - 自动化测试：单元测试、集成测试、API测试
   - 测试环境自动部署，生产环境手动确认

2. **环境管理**
   - 本地开发环境（local）
   - 开发环境（dev）
   - 测试环境（test）
   - 预生产环境（staging）
   - 生产环境（prod）
   - 每个环境使用不同的配置文件和数据源

## 常见问题与解决方案

1. **N+1查询问题**
   - 问题：在循环中查询关联实体导致多次数据库查询
   - 解决：使用JOIN FETCH或@EntityGraph预加载关联实体

2. **并发处理**
   - 问题：多线程并发修改同一资源
   - 解决：使用乐观锁（@Version）或分布式锁（Redis）

3. **大数据量处理**
   - 问题：大数据量导致内存溢出
   - 解决：使用分页查询和流式处理

```java
// 使用JPA的流式查询处理大数据量
@Transactional(readOnly = true)
public void processManyUsers() {
    try (Stream<User> userStream = userRepository.streamAllBy()) {
        userStream.forEach(user -> {
            // 处理单个用户数据
        });
    }
}
```

4. **缓存穿透**
   - 问题：查询不存在的数据绕过缓存直接访问数据库
   - 解决：缓存空结果、布隆过滤器

5. **缓存雪崩**
   - 问题：大量缓存同时失效
   - 解决：设置随机过期时间、多级缓存

## 技术债务管理

1. **识别技术债务**
   - 定期代码审查，识别潜在问题
   - 使用SonarQube等工具进行静态分析
   - 维护技术债务清单

2. **还清技术债务**
   - 安排专门的迭代处理技术债务
   - 重构低质量代码
   - 补充缺失的测试
   - 更新过时的依赖

## 参考资料

- [阿里巴巴Java开发手册](https://github.com/alibaba/p3c)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spring Framework参考文档](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [OWASP安全编码实践](https://owasp.org/www-project-secure-coding-practices-quick-reference-guide/)

---

本最佳实践文档会随着项目的发展不断更新和完善。欢迎所有团队成员提供反馈和建议。 