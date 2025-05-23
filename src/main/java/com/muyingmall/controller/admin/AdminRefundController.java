package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.entity.Refund;
import com.muyingmall.enums.RefundStatus;
import com.muyingmall.service.RefundService;
import com.muyingmall.service.RefundStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员退款控制器
 */
@RestController
@RequestMapping("/api/admin/refund")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;
    private final RefundStateService refundStateService;

    /**
     * 获取退款列表
     */
    @GetMapping("/list")
    public CommonResult<Page<Refund>> getRefunds(@RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "orderId", required = false) Integer orderId,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        Page<Refund> refunds = refundService.adminGetRefunds(page, size, status, userId, orderId, startTime, endTime);
        return CommonResult.success(refunds);
    }

    /**
     * 获取退款详情
     */
    @GetMapping("/{refundId}")
    public CommonResult<Refund> getRefundDetail(@PathVariable("refundId") Long refundId) {
        Refund refund = refundService.getRefundDetail(refundId);
        return CommonResult.success(refund);
    }

    /**
     * 审核退款申请
     */
    @PostMapping("/review")
    public CommonResult<Boolean> reviewRefund(@RequestParam("refundId") Long refundId,
            @RequestParam("approved") Boolean approved,
            @RequestParam(value = "rejectReason", required = false) String rejectReason,
            @RequestParam("adminId") Integer adminId,
            @RequestParam("adminName") String adminName) {
        boolean success = refundService.reviewRefund(refundId, approved, rejectReason, adminId, adminName);
        return CommonResult.success(success, approved ? "退款申请已批准" : "退款申请已拒绝");
    }

    /**
     * 处理退款
     */
    @PostMapping("/process")
    public CommonResult<Boolean> processRefund(@RequestParam("refundId") Long refundId,
            @RequestParam("refundChannel") String refundChannel,
            @RequestParam(value = "refundAccount", required = false) String refundAccount,
            @RequestParam("adminId") Integer adminId,
            @RequestParam("adminName") String adminName) {
        boolean success = refundService.processRefund(refundId, refundChannel, refundAccount, adminId, adminName);
        return CommonResult.success(success, "退款处理已开始");
    }

    /**
     * 完成退款
     */
    @PostMapping("/complete")
    public CommonResult<Boolean> completeRefund(@RequestParam("refundId") Long refundId,
            @RequestParam("transactionId") String transactionId,
            @RequestParam("adminId") Integer adminId,
            @RequestParam("adminName") String adminName) {
        boolean success = refundService.completeRefund(refundId, transactionId, adminId, adminName);
        return CommonResult.success(success, "退款已完成");
    }

    /**
     * 标记退款失败
     */
    @PostMapping("/fail")
    public CommonResult<Boolean> failRefund(@RequestParam("refundId") Long refundId,
            @RequestParam("reason") String reason,
            @RequestParam("adminId") Integer adminId,
            @RequestParam("adminName") String adminName) {
        boolean success = refundService.failRefund(refundId, reason, adminId, adminName);
        return CommonResult.success(success, "退款已标记为失败");
    }

    /**
     * 获取退款统计数据
     */
    @GetMapping("/statistics")
    public CommonResult<Map<String, Object>> getRefundStatistics(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        Map<String, Object> statistics = refundService.getRefundStatistics(startTime, endTime);
        return CommonResult.success(statistics);
    }

    /**
     * 获取待处理的退款数量
     */
    @GetMapping("/pending/count")
    public CommonResult<Long> getPendingRefundCount() {
        long count = refundService.getPendingRefundCount();
        return CommonResult.success(count);
    }
}