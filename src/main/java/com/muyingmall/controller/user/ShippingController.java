package com.muyingmall.controller.user;

import com.muyingmall.common.api.Result;
import com.muyingmall.common.result.ResultCode;
import com.muyingmall.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 运费计算控制器
 */
@Tag(name = "运费计算", description = "运费计算相关接口")
@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    /**
     * 【场景2：智能运费计算】计算运费
     */
    @Operation(summary = "计算运费", description = "根据收货地址和订单金额计算运费")
    @GetMapping("/calculate")
    public Result<Map<String, Object>> calculateShippingFee(
            @RequestParam Integer addressId,
            @RequestParam BigDecimal orderAmount) {

        // 检查是否可配送
        boolean deliverable = shippingService.isDeliverable(addressId);
        if (!deliverable) {
            return Result.error("该地址超出配送范围");
        }

        // 计算运费
        BigDecimal shippingFee = shippingService.calculateShippingFee(addressId, orderAmount);

        Map<String, Object> result = new HashMap<>();
        result.put("shippingFee", shippingFee);
        result.put("deliverable", true);
        result.put("freeShipping", shippingFee.compareTo(BigDecimal.ZERO) == 0);

        return Result.success(result);
    }

    /**
     * 检查地址是否可配送
     */
    @Operation(summary = "检查配送范围", description = "检查地址是否在配送范围内")
    @GetMapping("/check/{addressId}")
    public Result<Map<String, Object>> checkDeliverable(@PathVariable Integer addressId) {
        boolean deliverable = shippingService.isDeliverable(addressId);

        Map<String, Object> result = new HashMap<>();
        result.put("deliverable", deliverable);
        result.put("message", deliverable ? "该地址在配送范围内" : "该地址超出配送范围");

        return Result.success(result);
    }
}
