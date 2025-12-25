package com.muyingmall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 *
 * 注意：项目已启用虚拟线程 (spring.threads.virtual.enabled=true)
 * 
 * 虚拟线程与传统线程池的协作：
 * 1. Tomcat Web请求：自动使用虚拟线程处理（无需配置）
 * 2. @Async异步任务：仍使用本配置的线程池（可选择性迁移到虚拟线程）
 * 3. 自定义线程池：保留用于特定场景的精细控制
 * 
 * 性能优化：
 * 1. 根据CPU核心数动态计算线程池大小
 * 2. 配置合理的拒绝策略
 * 3. 添加线程池监控日志
 * 4. 分离不同类型任务的线程池
 * 
 * 未来优化方向：
 * - 可考虑将 @Async 任务迁移到虚拟线程执行器
 * - 使用 Executors.newVirtualThreadPerTaskExecutor() 替代传统线程池
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    // 通用异步任务线程池配置
    @Value("${async.executor.core-pool-size:0}")
    private int corePoolSize;

    @Value("${async.executor.max-pool-size:0}")
    private int maxPoolSize;

    @Value("${async.executor.queue-capacity:500}")
    private int queueCapacity;

    @Value("${async.executor.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    /**
     * 通用异步任务执行器
     * 用于搜索统计、日志记录等异步任务
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 动态计算线程池大小 (基于CPU核心数)
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int actualCoreSize = corePoolSize > 0 ? corePoolSize : cpuCores * 2;
        int actualMaxSize = maxPoolSize > 0 ? maxPoolSize : cpuCores * 4;

        executor.setCorePoolSize(actualCoreSize);
        executor.setMaxPoolSize(actualMaxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("async-");

        // 配置拒绝策略: 由调用线程执行，防止任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 允许核心线程超时销毁
        executor.setAllowCoreThreadTimeOut(true);

        // 等待任务完成后再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.debug("异步任务线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                actualCoreSize, actualMaxSize, queueCapacity);

        return executor;
    }

    /**
     * ES专用线程池
     * 用于ES批量索引、重建索引等IO密集型任务
     */
    @Bean(name = "esTaskExecutor")
    public Executor esTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // IO密集型任务需要更多线程
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int coreSize = cpuCores * 2;
        int maxSize = cpuCores * 4;

        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("es-async-");

        // 配置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);

        executor.initialize();

        log.debug("ES异步任务线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                coreSize, maxSize, 200);

        return executor;
    }

    /**
     * 搜索统计专用线程池
     * 用于记录搜索日志、用户行为等低优先级任务
     */
    @Bean(name = "statsTaskExecutor")
    public Executor statsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(1000);  // 大队列，允许堆积
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("stats-async-");

        // 统计任务优先级低，可以丢弃
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());

        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(false);  // 不等待统计任务

        executor.initialize();

        log.debug("统计异步任务线程池初始化完成: corePoolSize=2, maxPoolSize=4, queueCapacity=1000");

        return executor;
    }
}
