package com.muyingmall.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyingmall.entity.SeckillActivity;
import com.muyingmall.mapper.SeckillActivityMapper;
import com.muyingmall.service.SeckillActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀系统初始化器
 * 应用启动时自动初始化进行中的秒杀活动库存到Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillInitializer implements ApplicationRunner {
    
    private final SeckillActivityMapper seckillActivityMapper;
    private final SeckillActivityService seckillActivityService;
    
    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("开始初始化秒杀活动库存...");
            
            // 查询进行中或即将开始的秒杀活动（未来24小时内）
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = now.plusHours(24);
            
            LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SeckillActivity::getStatus, 1)
                   .le(SeckillActivity::getStartTime, future)  // 开始时间在未来24小时内
                   .ge(SeckillActivity::getEndTime, now);      // 结束时间在当前时间之后
            
            List<SeckillActivity> activities = seckillActivityMapper.selectList(wrapper);
            
            if (activities.isEmpty()) {
                log.info("当前没有进行中或即将开始的秒杀活动");
                return;
            }
            
            log.info("找到{}个需要初始化的秒杀活动", activities.size());
            
            // 初始化每个活动的库存
            int successCount = 0;
            for (SeckillActivity activity : activities) {
                try {
                    seckillActivityService.initActivityStock(activity.getId());
                    successCount++;
                    log.info("秒杀活动库存初始化成功: activityId={}, activityName={}, startTime={}", 
                            activity.getId(), activity.getName(), activity.getStartTime());
                } catch (Exception e) {
                    log.error("秒杀活动库存初始化失败: activityId={}, activityName={}", 
                            activity.getId(), activity.getName(), e);
                }
            }
            
            log.info("秒杀活动库存初始化完成，成功初始化{}/{}个活动", successCount, activities.size());
            
        } catch (Exception e) {
            log.error("秒杀系统初始化失败", e);
        }
    }
}
