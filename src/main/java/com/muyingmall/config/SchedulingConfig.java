package com.muyingmall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定时任务配置 - 虚拟线程版本
 * 
 * 配置定时任务使用虚拟线程执行器，提升并发能力
 * 
 * 优化策略：
 * 1. 使用ThreadPoolTaskScheduler + 虚拟线程工厂
 * 2. 支持多个定时任务并发执行，避免相互阻塞
 * 3. 虚拟线程自动管理，无需配置线程池大小
 * 4. 保留线程名称前缀，便于监控和问题排查
 * 
 * 虚拟线程优势：
 * - 定时任务可以并发执行，不会相互阻塞
 * - IO阻塞时自动让出CPU
 * - 内存占用小，支持大量定时任务
 */
@Slf4j
@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

    /**
     * 配置定时任务执行器
     * 使用虚拟线程执行器，支持多个定时任务并发执行
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(schedulingTaskScheduler());
    }

    /**
     * 定时任务调度器（虚拟线程版本）
     * 
     * @return 虚拟线程调度器
     */
    @Bean(name = "schedulingTaskScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler schedulingTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // 设置线程池大小（虚拟线程模式下可以设置较大值）
        scheduler.setPoolSize(20);
        
        // 设置虚拟线程工厂
        scheduler.setThreadFactory(createVirtualThreadFactory("scheduling-"));
        
        // 设置线程名称前缀（作为备用，虚拟线程工厂会覆盖）
        scheduler.setThreadNamePrefix("scheduling-");
        
        // 设置等待任务完成后再关闭
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        
        // 设置等待时间
        scheduler.setAwaitTerminationSeconds(60);
        
        // 初始化
        scheduler.initialize();
        
        log.info("定时任务调度器初始化完成 (虚拟线程模式): poolSize=20, threadPrefix=scheduling-");
        
        return scheduler;
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
