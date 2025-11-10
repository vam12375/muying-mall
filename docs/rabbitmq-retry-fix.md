# RabbitMQ 连接重试优化说明

## 问题描述

在 RabbitMQ 服务未启动的情况下，Spring Boot 应用会不断尝试重新连接 RabbitMQ，导致日志不断刷屏，影响开发体验和日志可读性。

## 解决方案

### 1. 配置文件修改 (application.yml)

#### 1.1 RabbitMQ 监听器配置优化

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        auto-startup: false  # 禁用自动启动监听器
        missing-queues-fatal: false  # 队列不存在时不抛出致命异常
        retry:
          enabled: true
          initial-interval: 5000  # 增加初始重试间隔到5秒
          max-attempts: 3  # 最多重试3次
          max-interval: 30000  # 最大间隔30秒
          multiplier: 2
```

#### 1.2 禁用健康检查

```yaml
management:
  health:
    rabbitmq:
      enabled: false  # 禁用RabbitMQ健康检查，避免日志刷屏
```

#### 1.3 降低日志级别

```yaml
logging:
  level:
    org.springframework.amqp: ERROR  # 降低RabbitMQ日志级别
    org.springframework.amqp.rabbit.listener: ERROR  # 降低监听器日志级别
    org.springframework.amqp.rabbit.connection: ERROR  # 降低连接日志级别
```

### 2. 代码层面优化

#### 2.1 自定义连接工厂 (RabbitMQConfig.java)

创建自定义的 `CachingConnectionFactory`，添加连接监听器来限制错误日志输出：

```java
@Bean
@Primary
public CachingConnectionFactory customConnectionFactory() {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
    // ... 设置基本配置
    
    // 添加连接监听器，限制错误日志输出
    connectionFactory.addConnectionListener(connectionListener);
    
    return connectionFactory;
}
```

#### 2.2 自定义监听器容器工厂

```java
@Bean
public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    // ... 其他配置
    
    // 关键配置：禁用自动启动，防止启动时一直重试连接
    factory.setAutoStartup(false);
    
    // 设置队列不存在时不抛出致命异常
    factory.setMissingQueuesFatal(false);
    
    // 设置恢复间隔
    factory.setRecoveryInterval(30000L);
    
    return factory;
}
```

#### 2.3 连接监听器 (RabbitMQConnectionListener.java)

创建自定义的连接监听器，限制错误日志只输出3次：

```java
@Component
public class RabbitMQConnectionListener implements ConnectionListener {
    
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private static final int MAX_FAILURE_LOGS = 3;
    
    @Override
    public void onFailed(Exception exception) {
        int currentCount = failureCount.incrementAndGet();
        
        if (currentCount <= MAX_FAILURE_LOGS) {
            log.error("✗ RabbitMQ初始连接失败 [{}/{}]: {}", 
                    currentCount, MAX_FAILURE_LOGS, exception.getMessage());
            
            if (currentCount == MAX_FAILURE_LOGS) {
                log.warn("⚠ RabbitMQ连接失败已达到{}次，后续错误将不再输出到日志。", 
                        MAX_FAILURE_LOGS);
            }
        }
    }
}
```

## 效果说明

### 修改前
- 日志不断刷屏，每秒多次重试连接
- 控制台输出混乱，难以查看其他重要信息
- 应用启动变慢

### 修改后
- **只在启动时提示3次连接失败**，之后不再输出错误日志
- 重试间隔增加到30秒，大大减少了日志输出频率
- 应用正常启动，使用降级模式（同步处理）
- 提供友好的提示信息，告知用户如何禁用 RabbitMQ

## 日志输出示例

```
✗ RabbitMQ初始连接失败 [1/3]: Connection refused: getsockopt
✗ RabbitMQ初始连接失败 [2/3]: Connection refused: getsockopt
✗ RabbitMQ初始连接失败 [3/3]: Connection refused: getsockopt
⚠ RabbitMQ连接失败已达到3次，后续错误将不再输出到日志。应用将以降级模式运行（同步处理）。请检查 RabbitMQ 服务是否正常启动。
⚠ 提示：如果不需要使用消息队列功能，可以在 application.yml 中设置 rabbitmq.enabled=false
```

## 如何完全禁用 RabbitMQ

如果不需要使用消息队列功能，可以在 `application.yml` 中设置：

```yaml
rabbitmq:
  enabled: false  # 完全禁用 RabbitMQ 功能
```

## 如何手动启动监听器

如果需要在 RabbitMQ 服务启动后手动启动监听器，可以通过以下方式：

```java
@Autowired
private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

public void startRabbitListeners() {
    rabbitListenerEndpointRegistry.start();
}
```

## 注意事项

1. **降级模式**：在 RabbitMQ 不可用时，应用会使用同步处理模式，不影响核心业务功能
2. **监听器自动启动**：设置为 `false` 后，需要在 RabbitMQ 服务可用后手动启动
3. **健康检查**：禁用健康检查后，actuator 端点不会报告 RabbitMQ 的健康状态
4. **生产环境**：在生产环境中，建议确保 RabbitMQ 服务正常运行，不要依赖降级模式

## 相关文件

- `src/main/resources/application.yml` - 配置文件
- `src/main/java/com/muyingmall/config/RabbitMQConfig.java` - RabbitMQ 配置类
- `src/main/java/com/muyingmall/config/RabbitMQConnectionListener.java` - 连接监听器
- `src/main/java/com/muyingmall/config/properties/RabbitMQProperties.java` - 属性配置类

## 修改日期

2025-10-17

