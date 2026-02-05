package com.muyingmall.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.muyingmall.entity.SeckillActivity;
import com.muyingmall.service.SeckillActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀活动状态定时任务
 * 自动更新活动状态：未开始 -> 进行中 -> 已结束
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillActivityStatusTask {
    
    private final SeckillActivityService seckillActivityService;
    
    /**
     * 每分钟执行一次，更新活动状态
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updateActivityStatus() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int updatedCount = 0;
            
            // 1. 将到达开始时间的活动状态改为"进行中"
            LambdaUpdateWrapper<SeckillActivity> startWrapper = new LambdaUpdateWrapper<>();
            startWrapper.eq(SeckillActivity::getStatus, 0)  // 未开始
                       .le(SeckillActivity::getStartTime, now)  // 开始时间 <= 当前时间
                       .ge(SeckillActivity::getEndTime, now)    // 结束时间 >= 当前时间
                       .set(SeckillActivity::getStatus, 1)      // 改为进行中
                       .set(SeckillActivity::getUpdateTime, now);
            
            int startedCount = seckillActivityService.update(startWrapper) ? 
                    (int) seckillActivityService.count(new LambdaUpdateWrapper<SeckillActivity>()
                            .eq(SeckillActivity::getStatus, 1)
                            .le(SeckillActivity::getStartTime, now)
                            .ge(SeckillActivity::getEndTime, now)) : 0;
            
            if (startedCount > 0) {
                log.info("定时任务：{} 个活动已开始", startedCount);
                updatedCount += startedCount;
            }
            
            // 2. 将已过结束时间的活动状态改为"已结束"
            LambdaUpdateWrapper<SeckillActivity> endWrapper = new LambdaUpdateWrapper<>();
            endWrapper.eq(SeckillActivity::getStatus, 1)  // 进行中
                     .lt(SeckillActivity::getEndTime, now)  // 结束时间 < 当前时间
                     .set(SeckillActivity::getStatus, 2)    // 改为已结束
                     .set(SeckillActivity::getUpdateTime, now);
            
            int endedCount = seckillActivityService.update(endWrapper) ? 
                    (int) seckillActivityService.count(new LambdaUpdateWrapper<SeckillActivity>()
                            .eq(SeckillActivity::getStatus, 2)
                            .lt(SeckillActivity::getEndTime, now)) : 0;
            
            if (endedCount > 0) {
                log.info("定时任务：{} 个活动已结束", endedCount);
                updatedCount += endedCount;
            }
            
            if (updatedCount > 0) {
                log.info("定时任务：共更新 {} 个活动状态", updatedCount);
            }
            
        } catch (Exception e) {
            log.error("更新活动状态失败", e);
        }
    }
    
    /**
     * 每小时执行一次，记录活动状态统计
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void logActivityStatistics() {
        try {
            long notStarted = seckillActivityService.lambdaQuery()
                    .eq(SeckillActivity::getStatus, 0)
                    .count();
            
            long ongoing = seckillActivityService.lambdaQuery()
                    .eq(SeckillActivity::getStatus, 1)
                    .count();
            
            long ended = seckillActivityService.lambdaQuery()
                    .eq(SeckillActivity::getStatus, 2)
                    .count();
            
            log.info("活动状态统计 - 未开始: {}, 进行中: {}, 已结束: {}", notStarted, ongoing, ended);
            
        } catch (Exception e) {
            log.error("记录活动统计失败", e);

        }
    }
}
