package com.muyingmall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步任务线程池配置 - 虚拟线程版本
 *
 * 项目已启用虚拟线程 (spring.threads.virtual.enabled=true)
 * 
 * 优化策略：
 * 1. 使用虚拟线程执行器替代传统ThreadPoolTaskExecutor
 * 2. 简化配置，移除复杂的线程池参数（KISS原则）
 * 3. 虚拟线程自动管理，无需手动配置核心线程数、最大线程数、队列容量
 * 4. 保留线程名称前缀，便于监控和问题排查
 * 
 * 虚拟线程优势：
 * - 支持数万级并发任务
 * - 内存占用小（每个虚拟线程约1KB）
 * - IO阻塞时自动让出CPU
 * - 无需担心线程池耗尽
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 通用异步任务执行器（虚拟线程版本）
     * 用于搜索统计、日志记录等异步任务
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadFactory factory = createVirtualThreadFactory("async-");
        Executor executor = Executors.newThreadPerTaskExecutor(factory);
        
        log.info("通用异步任务执行器初始化完成 (虚拟线程模式): threadPrefix=async-");
        
        return executor;
    }

    /**
     * ES专用执行器（虚拟线程版本）
     * 用于ES批量索引、重建索引等IO密集型任务
     */
    @Bean(name = "esTaskExecutor")
    public Executor esTaskExecutor() {
        ThreadFactory factory = createVirtualThreadFactory("es-async-");
        Executor executor = Executors.newThreadPerTaskExecutor(factory);
        
        log.info("ES异步任务执行器初始化完成 (虚拟线程模式): threadPrefix=es-async-");
        
        return executor;
    }

    /**
     * 搜索统计专用执行器（虚拟线程版本）
     * 用于记录搜索日志、用户行为等低优先级任务
     */
    @Bean(name = "statsTaskExecutor")
    public Executor statsTaskExecutor() {
        ThreadFactory factory = createVirtualThreadFactory("stats-async-");
        Executor executor = Executors.newThreadPerTaskExecutor(factory);
        
        log.info("统计异步任务执行器初始化完成 (虚拟线程模式): threadPrefix=stats-async-");
        
        return executor;
    }

    /**
     * 创建虚拟线程工厂，支持自定义线程名称前缀
     * 
     * @param prefix 线程名称前缀
     * @return 虚拟线程工厂
     */
    private ThreadFactory createVirtualThreadFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger(0);
        return task -> Thread.ofVirtual()
                .name(prefix + counter.incrementAndGet())
                .unstarted(task);
    }
}
