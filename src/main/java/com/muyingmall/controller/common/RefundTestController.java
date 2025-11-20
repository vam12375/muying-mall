package com.muyingmall.controller.common;

import com.muyingmall.entity.Refund;
import com.muyingmall.enums.RefundStatus;
import com.muyingmall.service.RefundService;
import com.muyingmall.service.RefundStateService;
import com.muyingmall.statemachine.RefundEvent;
import com.muyingmall.common.api.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 退款测试控制器
 * 仅用于开发和测试环境
 */
@RestController
@RequestMapping("/test/refund")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(name = "系统功能", description = "退款功能测试接口")
public class RefundTestController {

    private final RefundService refundService;
    private final RefundStateService refundStateService;

    /**
     * 查看所有退款状态的数量
     */
    @GetMapping("/status/count")
    public Result<Map<String, Object>> getRefundStatusCount() {
        Map<String, Object> result = new HashMap<>();

        // 获取总退款数量
        long totalCount = refundService.count();
        result.put("totalCount", totalCount);

        // 获取各状态退款数量
        Map<String, Long> statusCounts = new HashMap<>();
        for (RefundStatus status : RefundStatus.values()) {
            long count = refundService.lambdaQuery()
                    .eq(Refund::getStatus, status.getCode())
                    .count();
            statusCounts.put(status.getCode(), count);
        }
        result.put("statusCounts", statusCounts);

        return Result.success(result);
    }

    /**
     * 查看指定状态的退款列表
     */
    @GetMapping("/status/{status}")
    public Result<List<Map<String, Object>>> getRefundsByStatus(@PathVariable("status") String status) {
        List<Refund> refunds = refundService.lambdaQuery()
                .eq(Refund::getStatus, status)
                .orderByDesc(Refund::getCreateTime)
                .last("LIMIT 10") // 限制最多返回10条
                .list();

        List<Map<String, Object>> result = refunds.stream().map(refund -> {
            Map<String, Object> map = new HashMap<>();
            map.put("refundId", refund.getId());
            map.put("refundNo", refund.getRefundNo());
            map.put("orderId", refund.getOrderId());
            map.put("orderNo", refund.getOrderNo());
            map.put("userId", refund.getUserId());
            map.put("amount", refund.getAmount());
            map.put("status", refund.getStatus());
            map.put("createTime", refund.getCreateTime());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 手动触发状态转换事件
     */
    @PostMapping("/trigger/{refundId}/{event}")
    public Result<Boolean> triggerEvent(
            @PathVariable("refundId") Long refundId,
            @PathVariable("event") String eventName,
            @RequestParam(value = "operatorType", defaultValue = "SYSTEM") String operatorType,
            @RequestParam(value = "operatorName", defaultValue = "测试系统") String operatorName,
            @RequestParam(value = "reason", defaultValue = "手动测试触发") String reason) {

        try {
            RefundEvent event = RefundEvent.valueOf(eventName.toUpperCase());
            log.info("手动触发退款状态事件: refundId={}, event={}, operatorType={}, operatorName={}, reason={}",
                    refundId, event, operatorType, operatorName, reason);

            Refund refund = refundService.getById(refundId);
            if (refund == null) {
                return Result.error("退款不存在");
            }

            log.info("当前退款状态: {}", refund.getStatus());

            boolean success = refundStateService.sendEvent(refundId, event, operatorType, operatorName, 0, reason);

            // 重新获取状态
            refund = refundService.getById(refundId);
            log.info("触发后状态: {}", refund.getStatus());

            return Result.success(success);
        } catch (IllegalArgumentException e) {
            return Result.error("无效的事件名称: " + eventName);
        } catch (Exception e) {
            log.error("触发事件失败", e);
            return Result.error("触发事件失败: " + e.getMessage());
        }
    }
}