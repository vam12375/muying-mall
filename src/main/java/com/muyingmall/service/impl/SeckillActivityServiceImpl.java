package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.dto.SeckillProductDTO;
import com.muyingmall.entity.SeckillActivity;
import com.muyingmall.entity.SeckillProduct;
import com.muyingmall.mapper.SeckillActivityMapper;
import com.muyingmall.mapper.SeckillProductMapper;
import com.muyingmall.service.SeckillActivityService;
import com.muyingmall.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀活动服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> 
        implements SeckillActivityService {
    
    private final SeckillProductMapper seckillProductMapper;
    private final SeckillService seckillService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    
    @Override
    public List<SeckillActivity> getActiveActivities() {
        // 查询进行中的活动
        LocalDateTime now = LocalDateTime.now();
        return this.lambdaQuery()
                .eq(SeckillActivity::getStatus, 1)
                .le(SeckillActivity::getStartTime, now)
                .ge(SeckillActivity::getEndTime, now)
                .orderByDesc(SeckillActivity::getStartTime)
                .list();
    }
    
    @Override
    public List<SeckillActivity> getActivitiesByStatus(Integer status) {
        return this.lambdaQuery()
                .eq(SeckillActivity::getStatus, status)
                .orderByDesc(SeckillActivity::getStartTime)
                .list();
    }
    
    @Override
    public List<SeckillProductDTO> getActivityProducts(Long activityId) {
        List<SeckillProductDTO> products = seckillProductMapper.selectSeckillProductsByActivity(activityId);
        
        // 从Redis获取实时库存（只更新库存，soldCount已由SQL统计得出）
        for (SeckillProductDTO product : products) {
            Integer redisStock = seckillService.getRedisStock(product.getSkuId());
            if (redisStock != null) {
                product.setSeckillStock(redisStock);
            }
            // soldCount已由SQL查询统计（LEFT JOIN seckill_order），无需重新计算
        }
        
        return products;
    }
    
    @Override
    public SeckillProductDTO getSeckillProductDetail(Long seckillProductId) {
        SeckillProductDTO product = seckillProductMapper.selectSeckillProductDetail(seckillProductId);
        
        if (product != null) {
            // 从Redis获取实时库存
            Integer redisStock = seckillService.getRedisStock(product.getSkuId());
            if (redisStock != null) {
                product.setSeckillStock(redisStock);
            }
        }
        
        return product;
    }
    
    @Override
    public void initActivityStock(Long activityId) {
        // 查询活动下的所有秒杀商品
        LambdaQueryWrapper<SeckillProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillProduct::getActivityId, activityId);
        List<SeckillProduct> products = seckillProductMapper.selectList(wrapper);
        
        // 初始化每个商品的库存到Redis
        for (SeckillProduct product : products) {
            seckillService.initSeckillStock(product.getSkuId(), product.getSeckillStock());
            log.info("初始化秒杀商品库存: activityId={}, skuId={}, stock={}", 
                    activityId, product.getSkuId(), product.getSeckillStock());
        }
    }
    
    @Override
    public IPage<SeckillActivity> getActivityPage(Page<SeckillActivity> page, String keyword, Integer status) {
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(SeckillActivity::getName, keyword)
                   .or()
                   .like(SeckillActivity::getDescription, keyword);
        }
        
        // 状态筛选 - 不在查询时过滤，而是在结果中动态计算后过滤
        // 这样可以确保显示的状态是实时的
        
        // 移除默认排序，改为在后面按状态排序
        // wrapper.orderByDesc(SeckillActivity::getCreateTime);
        
        // 查询所有符合条件的记录（不分页）
        List<SeckillActivity> allRecords = this.list(wrapper);
        
        // 动态计算每个活动的实际状态
        LocalDateTime now = LocalDateTime.now();
        for (SeckillActivity activity : allRecords) {
            int actualStatus = calculateActivityStatus(activity, now);
            activity.setStatus(actualStatus);
        }
        
        // 如果指定了状态筛选，需要在结果中过滤
        if (status != null) {
            allRecords = allRecords.stream()
                    .filter(activity -> activity.getStatus().equals(status))
                    .toList();
        }
        
        // 按状态排序：进行中(1) > 未开始(0) > 已结束(2)
        // 同状态内按创建时间倒序排列
        allRecords.sort((a, b) -> {
            // 定义状态优先级：进行中=0, 未开始=1, 已结束=2
            int priorityA = a.getStatus() == 1 ? 0 : (a.getStatus() == 0 ? 1 : 2);
            int priorityB = b.getStatus() == 1 ? 0 : (b.getStatus() == 0 ? 1 : 2);
            
            if (priorityA != priorityB) {
                return priorityA - priorityB;
            }
            // 同状态按创建时间倒序
            return b.getCreateTime().compareTo(a.getCreateTime());
        });
        
        // 手动分页
        int total = allRecords.size();
        int start = (int) ((page.getCurrent() - 1) * page.getSize());
        int end = Math.min(start + (int) page.getSize(), total);
        
        List<SeckillActivity> pagedRecords = start < total ? 
                allRecords.subList(start, end) : List.of();
        
        // 构建分页结果
        Page<SeckillActivity> result = new Page<>(page.getCurrent(), page.getSize(), total);
        result.setRecords(pagedRecords);
        
        return result;
    }
    
    /**
     * 根据时间计算活动的实际状态
     * @param activity 活动对象
     * @param now 当前时间
     * @return 0-未开始，1-进行中，2-已结束
     */
    private int calculateActivityStatus(SeckillActivity activity, LocalDateTime now) {
        if (now.isBefore(activity.getStartTime())) {
            return 0; // 未开始
        } else if (now.isAfter(activity.getEndTime())) {
            return 2; // 已结束
        } else {
            return 1; // 进行中
        }
    }
    
    @Override
    public long countByStatus(Integer status) {
        // 获取所有活动
        List<SeckillActivity> allActivities = this.list();
        LocalDateTime now = LocalDateTime.now();
        
        // 动态计算每个活动的实际状态并统计
        return allActivities.stream()
                .filter(activity -> {
                    int actualStatus = calculateActivityStatus(activity, now);
                    return actualStatus == status;
                })
                .count();
    }
    
    @Override
    public int updateActivityStatus() {
        LocalDateTime now = LocalDateTime.now();
        int updatedCount = 0;
        
        // 更新未开始的活动为进行中
        List<SeckillActivity> toStart = this.lambdaQuery()
                .eq(SeckillActivity::getStatus, 0)
                .le(SeckillActivity::getStartTime, now)
                .gt(SeckillActivity::getEndTime, now)
                .list();
        
        for (SeckillActivity activity : toStart) {
            activity.setStatus(1);
            this.updateById(activity);
            updatedCount++;
            log.info("活动状态更新为进行中: activityId={}, name={}", activity.getId(), activity.getName());
        }
        
        // 更新进行中的活动为已结束
        List<SeckillActivity> toEnd = this.lambdaQuery()
                .eq(SeckillActivity::getStatus, 1)
                .le(SeckillActivity::getEndTime, now)
                .list();
        
        for (SeckillActivity activity : toEnd) {
            activity.setStatus(2);
            this.updateById(activity);
            updatedCount++;
            log.info("活动状态更新为已结束: activityId={}, name={}", activity.getId(), activity.getName());
        }
        
        return updatedCount;
    }
}
