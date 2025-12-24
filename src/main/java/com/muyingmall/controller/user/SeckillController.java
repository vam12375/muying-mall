package com.muyingmall.controller.user;

import com.muyingmall.common.api.Result;
import com.muyingmall.dto.SeckillProductDTO;
import com.muyingmall.dto.SeckillRequestDTO;
import com.muyingmall.entity.SeckillActivity;
import com.muyingmall.service.SeckillActivityService;
import com.muyingmall.service.SeckillOrderService;
import com.muyingmall.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀控制器
 */
@Slf4j
@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
@Tag(name = "秒杀管理", description = "秒杀活动和商品接口")
public class SeckillController {
    
    private final SeckillActivityService seckillActivityService;
    private final SeckillOrderService seckillOrderService;
    private final UserContext userContext;
    
    @GetMapping("/activities")
    @Operation(summary = "获取进行中的秒杀活动列表")
    public Result<List<SeckillActivity>> getActiveActivities() {
        List<SeckillActivity> activities = seckillActivityService.getActiveActivities();
        return Result.success(activities);
    }
    
    @GetMapping("/activity/{activityId}/products")
    @Operation(summary = "获取秒杀活动的商品列表")
    public Result<List<SeckillProductDTO>> getActivityProducts(@PathVariable Long activityId) {
        List<SeckillProductDTO> products = seckillActivityService.getActivityProducts(activityId);
        return Result.success(products);
    }
    
    @GetMapping("/product/{seckillProductId}")
    @Operation(summary = "获取秒杀商品详情")
    public Result<SeckillProductDTO> getSeckillProductDetail(@PathVariable Long seckillProductId) {
        SeckillProductDTO product = seckillActivityService.getSeckillProductDetail(seckillProductId);
        if (product == null) {
            return Result.error("秒杀商品不存在");
        }
        return Result.success(product);
    }
    
    @PostMapping("/execute")
    @Operation(summary = "执行秒杀")
    public Result<Long> executeSeckill(@RequestBody SeckillRequestDTO request) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        try {
            Long orderId = seckillOrderService.executeSeckill(userId, request);
            return Result.success(orderId, "秒杀成功");
        } catch (Exception e) {
            log.error("秒杀失败: userId={}, request={}", userId, request, e);
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/check/{seckillProductId}")
    @Operation(summary = "检查用户是否可以参与秒杀")
    public Result<Boolean> checkUserParticipation(@PathVariable Long seckillProductId) {
        Integer userId = userContext.getCurrentUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        boolean canParticipate = seckillOrderService.canUserParticipate(userId, seckillProductId);
        return Result.success(canParticipate);
    }
}
