package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.entity.Refund;
import com.muyingmall.enums.RefundStatus;
import com.muyingmall.service.AlipayRefundService;
import com.muyingmall.service.RefundService;
import com.muyingmall.service.RefundStateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员退款控制器
 */
@RestController
@RequestMapping("/admin/refund")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "后台-退款管理", description = "退款列表、退款审核、退款处理等功能")
public class AdminRefundController {

    private final RefundService refundService;
    private final RefundStateService refundStateService;
    private final AlipayRefundService alipayRefundService;

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

        log.debug("====================开始处理退款列表请求====================");
        log.debug("请求路径: {}", "/admin/refund/list");
        log.debug("获取退款列表请求: page={}, size={}, status={}, userId={}, orderId={}, startTime={}, endTime={}",
                page, size, status, userId, orderId, startTime, endTime);

        // 获取总退款数量以便调试
        long totalRefunds = refundService.count();
        log.debug("系统中总退款数量: {}", totalRefunds);

        // 查询各状态退款数量
        for (RefundStatus refundStatus : RefundStatus.values()) {
            LambdaQueryWrapper<Refund> countQuery = new LambdaQueryWrapper<>();
            countQuery.eq(Refund::getStatus, refundStatus.getCode());
            long statusCount = refundService.count(countQuery);
            log.debug("状态[{}]的退款数量: {}", refundStatus.getDesc(), statusCount);
        }

        // 查询退款分页数据
        Page<Refund> refunds = refundService.adminGetRefunds(page, size, status, userId, orderId, startTime, endTime);

        log.debug("查询结果: 总记录数={}, 当前页记录数={}",
                refunds.getTotal(), refunds.getRecords().size());

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
    public CommonResult<Boolean> reviewRefund(@RequestBody Map<String, Object> requestData) {
        log.debug("收到退款审核请求: {}", requestData);

        // 检查必要参数
        if (requestData == null || !requestData.containsKey("refundId") || requestData.get("refundId") == null) {
            log.error("退款审核请求缺少必要参数refundId");
            return CommonResult.failed("退款ID不能为空");
        }

        if (!requestData.containsKey("approved") || requestData.get("approved") == null) {
            log.error("退款审核请求缺少必要参数approved");
            return CommonResult.failed("审核结果不能为空");
        }

        try {
            // 从请求体中获取参数
            Long refundId = Long.valueOf(requestData.get("refundId").toString());
            Boolean approved = (Boolean) requestData.get("approved");
            String rejectReason = requestData.get("rejectReason") != null ? (String) requestData.get("rejectReason")
                    : null;
            Integer adminId = requestData.get("adminId") != null
                    ? Integer.valueOf(requestData.get("adminId").toString())
                    : 0;
            String adminName = (String) requestData.get("adminName");

            log.debug("处理退款审核: refundId={}, approved={}, adminId={}, adminName={}, rejectReason={}",
                    refundId, approved, adminId, adminName, rejectReason);

            boolean success = refundService.reviewRefund(refundId, approved, rejectReason, adminId, adminName);
            return CommonResult.success(success, approved ? "退款申请已批准" : "退款申请已拒绝");
        } catch (Exception e) {
            log.error("处理退款审核请求出错", e);
            return CommonResult.failed("处理退款审核请求出错: " + e.getMessage());
        }
    }

    /**
     * 处理退款
     */
    @PostMapping("/process")
    public CommonResult<Boolean> processRefund(@RequestBody Map<String, Object> requestData) {
        log.debug("收到退款处理请求: {}", requestData);

        // 检查必要参数
        if (requestData == null || !requestData.containsKey("refundId") || requestData.get("refundId") == null) {
            log.error("退款处理请求缺少必要参数refundId");
            return CommonResult.failed("退款ID不能为空");
        }

        if (!requestData.containsKey("refundChannel") || requestData.get("refundChannel") == null) {
            log.error("退款处理请求缺少必要参数refundChannel");
            return CommonResult.failed("退款渠道不能为空");
        }

        try {
            // 从请求体中获取参数
            Long refundId = Long.valueOf(requestData.get("refundId").toString());
            String refundChannel = (String) requestData.get("refundChannel");
            String refundAccount = requestData.get("refundAccount") != null ? (String) requestData.get("refundAccount")
                    : null;
            Integer adminId = requestData.get("adminId") != null
                    ? Integer.valueOf(requestData.get("adminId").toString())
                    : 0;
            String adminName = (String) requestData.get("adminName");

            log.debug("处理退款: refundId={}, refundChannel={}, adminId={}, adminName={}",
                    refundId, refundChannel, adminId, adminName);

            boolean success = refundService.processRefund(refundId, refundChannel, refundAccount, adminId, adminName);
            return CommonResult.success(success, "退款处理已开始");
        } catch (Exception e) {
            log.error("处理退款请求出错", e);
            return CommonResult.failed("处理退款请求出错: " + e.getMessage());
        }
    }

    /**
     * 完成退款
     */
    @PostMapping("/complete")
    public CommonResult<Boolean> completeRefund(@RequestBody Map<String, Object> requestData) {
        log.debug("收到完成退款请求: {}", requestData);

        // 检查必要参数
        if (requestData == null || !requestData.containsKey("refundId") || requestData.get("refundId") == null) {
            log.error("完成退款请求缺少必要参数refundId");
            return CommonResult.failed("退款ID不能为空");
        }

        if (!requestData.containsKey("transactionId") || requestData.get("transactionId") == null) {
            log.error("完成退款请求缺少必要参数transactionId");
            return CommonResult.failed("交易ID不能为空");
        }

        try {
            // 从请求体中获取参数
            Long refundId = Long.valueOf(requestData.get("refundId").toString());
            String transactionId = (String) requestData.get("transactionId");
            Integer adminId = requestData.get("adminId") != null
                    ? Integer.valueOf(requestData.get("adminId").toString())
                    : 0;
            String adminName = (String) requestData.get("adminName");

            log.debug("完成退款: refundId={}, transactionId={}, adminId={}, adminName={}",
                    refundId, transactionId, adminId, adminName);

            boolean success = refundService.completeRefund(refundId, transactionId, adminId, adminName);
            return CommonResult.success(success, "退款已完成");
        } catch (Exception e) {
            log.error("完成退款请求出错", e);
            return CommonResult.failed("完成退款请求出错: " + e.getMessage());
        }
    }

    /**
     * 标记退款失败
     */
    @PostMapping("/fail")
    public CommonResult<Boolean> failRefund(@RequestBody Map<String, Object> requestData) {
        log.debug("收到标记退款失败请求: {}", requestData);

        // 检查必要参数
        if (requestData == null || !requestData.containsKey("refundId") || requestData.get("refundId") == null) {
            log.error("标记退款失败请求缺少必要参数refundId");
            return CommonResult.failed("退款ID不能为空");
        }

        if (!requestData.containsKey("reason") || requestData.get("reason") == null) {
            log.error("标记退款失败请求缺少必要参数reason");
            return CommonResult.failed("失败原因不能为空");
        }

        try {
            // 从请求体中获取参数
            Long refundId = Long.valueOf(requestData.get("refundId").toString());
            String reason = (String) requestData.get("reason");
            Integer adminId = requestData.get("adminId") != null
                    ? Integer.valueOf(requestData.get("adminId").toString())
                    : 0;
            String adminName = (String) requestData.get("adminName");

            log.debug("标记退款失败: refundId={}, reason={}, adminId={}, adminName={}",
                    refundId, reason, adminId, adminName);

            boolean success = refundService.failRefund(refundId, reason, adminId, adminName);
            return CommonResult.success(success, "退款已标记为失败");
        } catch (Exception e) {
            log.error("标记退款失败请求出错", e);
            return CommonResult.failed("标记退款失败请求出错: " + e.getMessage());
        }
    }

    /**
     * 获取退款统计数据
     */
    @GetMapping("/statistics")
    public CommonResult<Map<String, Object>> getRefundStatistics(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        log.debug("====================开始处理退款统计请求====================");
        log.debug("请求路径: {}", "/admin/refund/statistics");
        log.debug("请求参数: startTime={}, endTime={}", startTime, endTime);

        try {
            Map<String, Object> statistics = refundService.getRefundStatistics(startTime, endTime);
            log.debug("统计数据获取成功: {}", statistics);
            return CommonResult.success(statistics);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return CommonResult.failed("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取待处理的退款数量
     */
    @GetMapping("/pending/count")
    public CommonResult<Long> getPendingRefundCount() {
        log.debug("====================开始处理待处理退款数量请求====================");
        log.debug("请求路径: {}", "/admin/refund/pending/count");

        try {
            long count = refundService.getPendingRefundCount();
            log.debug("待处理退款数量: {}", count);
            return CommonResult.success(count);
        } catch (Exception e) {
            log.error("获取待处理退款数量失败", e);
            return CommonResult.failed("获取待处理退款数量失败: " + e.getMessage());
        }
    }

    /**
     * 查询支付宝退款状态
     */
    @GetMapping("/alipay/query")
    public CommonResult<Map<String, String>> queryAlipayRefundStatus(
            @RequestParam("refundNo") String refundNo,
            @RequestParam(value = "transactionId", required = false) String transactionId) {
        try {
            String status = alipayRefundService.queryRefundStatus(refundNo, transactionId);

            Map<String, String> result = new HashMap<>();
            result.put("refundNo", refundNo);
            result.put("status", status);

            // 解释支付宝退款状态码
            String statusDesc;
            switch (status) {
                case "REFUND_SUCCESS":
                    statusDesc = "退款成功";
                    break;
                case "REFUND_FAIL":
                    statusDesc = "退款失败";
                    break;
                case "REFUND_PROCESSING":
                    statusDesc = "退款处理中";
                    break;
                default:
                    statusDesc = "未知状态";
            }
            result.put("statusDesc", statusDesc);

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("查询支付宝退款状态失败", e);
            return CommonResult.failed("查询支付宝退款状态失败: " + e.getMessage());
        }
    }
}