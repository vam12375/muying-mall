package com.muyingmall.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.common.api.Result;
import com.muyingmall.config.RabbitMQSeckillConfig;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀控制器 - 支持同步和异步两种模式
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
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

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
    @Operation(summary = "执行秒杀（同步模式）")
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

    @PostMapping("/execute-async")
    @Operation(summary = "执行秒杀（异步模式 - 推荐）")
    public Result<String> executeSeckillAsync(@RequestBody SeckillRequestDTO request) {
        Integer userId = userContext.getCurrentUserId();

        if (userId == null) {
            return Result.error("请先登录");
        }

        try {
            // 构建消息
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("seckillProductId", request.getSeckillProductId());
            message.put("quantity", request.getQuantity());
            message.put("addressId", request.getAddressId());

            // 发送到MQ异步处理
            rabbitTemplate.convertAndSend(
                    RabbitMQSeckillConfig.SECKILL_EXCHANGE,
                    RabbitMQSeckillConfig.SECKILL_ROUTING_KEY,
                    objectMapper.writeValueAsString(message));

            log.info("秒杀请求已提交到队列: userId={}, productId={}", userId, request.getSeckillProductId());
            return Result.success("秒杀请求已提交，请稍后查看订单");

        } catch (Exception e) {
            log.error("秒杀请求提交失败: userId={}, request={}", userId, request, e);
            return Result.error("秒杀请求提交失败");
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
