package com.muyingmall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.entity.Refund;
import com.muyingmall.enums.RefundStatus;
import com.muyingmall.service.RefundService;
import com.muyingmall.service.RefundStateService;
import com.muyingmall.util.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 退款控制器
 */
@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.tags.Tag(name = "退款管理", description = "申请退款、查询退款列表、退款详情等功能")
public class RefundController {

    private final RefundService refundService;
    private final RefundStateService refundStateService;

    /**
     * 申请退款
     */
    @PostMapping("/apply")
    public Result<Long> applyRefund(@RequestBody Map<String, Object> requestData) {
        log.info("收到退款申请请求: {}", requestData);

        // 从请求体中获取参数
        Integer orderId = Integer.valueOf(requestData.get("orderId").toString());
        Integer userId = Integer.valueOf(requestData.get("userId").toString());
        BigDecimal amount = new BigDecimal(requestData.get("amount").toString());
        String reason = (String) requestData.get("reason");
        String reasonDetail = requestData.get("reasonDetail") != null ? (String) requestData.get("reasonDetail") : null;
        String evidenceImages = requestData.get("evidenceImages") != null ? requestData.get("evidenceImages").toString()
                : null;

        Long refundId = refundService.applyRefund(orderId, userId, amount, reason, reasonDetail, evidenceImages);
        return Result.success(refundId, "退款申请提交成功");
    }

    /**
     * 获取退款详情
     */
    @GetMapping("/{refundId}")
    public Result<Refund> getRefundDetail(@PathVariable("refundId") Long refundId) {
        Refund refund = refundService.getRefundDetail(refundId);
        return Result.success(refund);
    }

    /**
     * 取消退款申请
     */
    @PostMapping("/cancel/{refundId}")
    public Result<Boolean> cancelRefund(@PathVariable("refundId") Long refundId,
                                        @RequestParam("userId") Integer userId,
                                        @RequestParam("reason") String reason) {
        boolean success = refundService.cancelRefund(refundId, userId, reason);
        return Result.success(success, "退款申请已取消");
    }

    /**
     * 获取用户的退款列表
     */
    @GetMapping("/user/{userId}")
    public Result<Page<Refund>> getUserRefunds(@PathVariable("userId") Integer userId,
                                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<Refund> refunds = refundService.getUserRefunds(userId, page, size);
        return Result.success(refunds);
    }

    /**
     * 获取订单的退款列表
     */
    @GetMapping("/order/{orderId}")
    public Result<Page<Refund>> getOrderRefunds(@PathVariable("orderId") Integer orderId,
                                                @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("获取订单退款列表请求：orderId={}, page={}, size={}", orderId, page, size);
        try {
        Page<Refund> refunds = refundService.getOrderRefunds(orderId, page, size);
            log.info("获取订单退款列表成功：orderId={}, 总记录数={}", orderId, refunds.getTotal());
        return Result.success(refunds);
        } catch (Exception e) {
            log.error("获取订单退款列表失败：orderId={}", orderId, e);
            return Result.error("获取订单退款列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取退款状态枚举
     */
    @GetMapping("/status/list")
    public Result<RefundStatus[]> getRefundStatusList() {
        return Result.success(RefundStatus.values());
    }

    /**
     * 获取当前状态可以转换到的下一个状态集合
     */
    @GetMapping("/status/next/{status}")
    public Result<RefundStatus[]> getNextStatuses(@PathVariable("status") String status) {
        RefundStatus currentStatus = RefundStatus.getByCode(status);
        if (currentStatus == null) {
            return Result.error("无效的退款状态");
        }
        RefundStatus[] nextStatuses = refundStateService.getPossibleNextStates(currentStatus);
        return Result.success(nextStatuses);
    }
} 