package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.Payment;
import com.muyingmall.entity.Refund;
import com.muyingmall.entity.RefundLog;
import com.muyingmall.entity.User;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.enums.RefundStatus;
import com.muyingmall.exception.BusinessException;
import com.muyingmall.mapper.RefundMapper;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.RefundLogService;
import com.muyingmall.service.RefundService;
import com.muyingmall.service.RefundStateService;
import com.muyingmall.service.UserService;
import com.muyingmall.statemachine.OrderEvent;
import com.muyingmall.statemachine.PaymentEvent;
import com.muyingmall.statemachine.RefundEvent;
import com.muyingmall.util.EnumUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 退款服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl extends ServiceImpl<RefundMapper, Refund> implements RefundService {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final RefundStateService refundStateService;
    private final RefundLogService refundLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyRefund(Integer orderId, Integer userId, BigDecimal amount, String refundReason,
            String refundReasonDetail, String evidenceImages) {
        // 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证订单属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权限申请此订单的退款");
        }

        // 检查订单状态是否允许退款
        OrderStatus orderStatus = order.getStatus();
        if (orderStatus == OrderStatus.PENDING_PAYMENT || orderStatus == OrderStatus.CANCELLED) {
            throw new BusinessException("当前订单状态不允许退款");
        }

        // 检查退款金额是否超过订单实付金额
        if (amount.compareTo(order.getActualAmount()) > 0) {
            throw new BusinessException("退款金额不能超过实付金额");
        }

        // 检查是否已有未完成的退款申请
        LambdaQueryWrapper<Refund> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Refund::getOrderId, orderId)
                .in(Refund::getStatus, RefundStatus.PENDING.getCode(), RefundStatus.APPROVED.getCode(),
                        RefundStatus.PROCESSING.getCode());
        long count = count(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该订单已有未完成的退款申请");
        }

        // 创建退款申请
        Refund refund = new Refund();
        refund.setRefundNo(generateRefundNo());
        refund.setOrderId(orderId);
        refund.setOrderNo(order.getOrderNo());
        refund.setUserId(userId);
        refund.setPaymentId(order.getPaymentId());
        refund.setAmount(amount);
        refund.setRefundReason(refundReason);
        refund.setRefundReasonDetail(refundReasonDetail);
        refund.setEvidenceImages(evidenceImages);
        refund.setStatus(RefundStatus.PENDING.getCode());
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());

        // 保存退款申请
        save(refund);

        // 更新订单状态为退款中
        refundStateService.sendEvent(refund.getId(), RefundEvent.SUBMIT, "USER", "用户申请退款");

        return refund.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewRefund(Long refundId, boolean approved, String rejectReason, Integer adminId,
            String adminName) {
        Refund refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException("退款申请不存在");
        }

        if (!RefundStatus.PENDING.getCode().equals(refund.getStatus())) {
            throw new BusinessException("当前退款状态不允许审核");
        }

        if (approved) {
            // 批准退款
            refundStateService.sendEvent(refundId, RefundEvent.APPROVE, "ADMIN", adminName, adminId, "管理员批准退款");
        } else {
            // 拒绝退款
            if (!StringUtils.hasText(rejectReason)) {
                throw new BusinessException("拒绝退款时必须提供拒绝原因");
            }
            refund.setRejectReason(rejectReason);
            updateById(refund);
            refundStateService.sendEvent(refundId, RefundEvent.REJECT, "ADMIN", adminName, adminId,
                    "管理员拒绝退款：" + rejectReason);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processRefund(Long refundId, String refundChannel, String refundAccount, Integer adminId,
            String adminName) {
        Refund refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException("退款申请不存在");
        }

        if (!RefundStatus.APPROVED.getCode().equals(refund.getStatus())) {
            throw new BusinessException("当前退款状态不允许处理");
        }

        // 验证退款渠道
        if (!StringUtils.hasText(refundChannel)) {
            throw new BusinessException("退款渠道不能为空");
        }

        // 更新退款信息
        refund.setRefundChannel(refundChannel);
        refund.setRefundAccount(refundAccount);
        refund.setAdminId(adminId);
        refund.setAdminName(adminName);
        updateById(refund);

        // 更新退款状态为处理中
        refundStateService.sendEvent(refundId, RefundEvent.PROCESS, "ADMIN", adminName, adminId,
                "管理员开始处理退款，渠道：" + refundChannel);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeRefund(Long refundId, String transactionId, Integer adminId, String adminName) {
        Refund refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException("退款申请不存在");
        }

        if (!RefundStatus.PROCESSING.getCode().equals(refund.getStatus())) {
            throw new BusinessException("当前退款状态不允许完成");
        }

        // 更新退款信息
        refund.setTransactionId(transactionId);
        refund.setRefundTime(LocalDateTime.now());
        updateById(refund);

        // 更新退款状态为已完成
        refundStateService.sendEvent(refundId, RefundEvent.COMPLETE, "ADMIN", adminName, adminId,
                "管理员完成退款，交易号：" + transactionId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean failRefund(Long refundId, String reason, Integer adminId, String adminName) {
        Refund refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException("退款申请不存在");
        }

        if (!RefundStatus.PROCESSING.getCode().equals(refund.getStatus())) {
            throw new BusinessException("当前退款状态不允许标记为失败");
        }

        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("失败原因不能为空");
        }

        // 更新退款状态为失败
        refundStateService.sendEvent(refundId, RefundEvent.FAIL, "ADMIN", adminName, adminId,
                "退款失败：" + reason);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRefund(Long refundId, Integer userId, String reason) {
        Refund refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException("退款申请不存在");
        }

        // 验证退款申请属于当前用户
        if (!refund.getUserId().equals(userId)) {
            throw new BusinessException("无权限取消此退款申请");
        }

        // 只有待处理状态的退款可以取消
        if (!RefundStatus.PENDING.getCode().equals(refund.getStatus())) {
            throw new BusinessException("当前退款状态不允许取消");
        }

        // 更新退款状态为已拒绝（用户自己取消）
        refundStateService.sendEvent(refundId, RefundEvent.CANCEL, "USER", "用户", userId,
                "用户取消退款申请：" + reason);

        return true;
    }

    @Override
    public Refund getRefundDetail(Long refundId) {
        Refund refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException("退款申请不存在");
        }

        // 加载关联数据
        Order order = orderService.getById(refund.getOrderId());
        User user = userService.getById(refund.getUserId());
        Payment payment = null;
        if (refund.getPaymentId() != null) {
            payment = paymentService.getById(refund.getPaymentId());
        }

        refund.setOrder(order);
        refund.setUser(user);
        refund.setPayment(payment);

        return refund;
    }

    @Override
    public Page<Refund> getUserRefunds(Integer userId, Integer page, Integer size) {
        if (page == null || page < 1)
            page = 1;
        if (size == null || size < 1)
            size = 10;

        Page<Refund> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Refund> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Refund::getUserId, userId)
                .orderByDesc(Refund::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    public Page<Refund> getOrderRefunds(Integer orderId, Integer page, Integer size) {
        if (page == null || page < 1)
            page = 1;
        if (size == null || size < 1)
            size = 10;

        Page<Refund> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Refund> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Refund::getOrderId, orderId)
                .orderByDesc(Refund::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    public Page<Refund> adminGetRefunds(Integer page, Integer size, String status, Integer userId,
            Integer orderId, String startTime, String endTime) {
        if (page == null || page < 1)
            page = 1;
        if (size == null || size < 1)
            size = 10;

        Page<Refund> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Refund> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Refund::getStatus, status);
        }

        if (userId != null) {
            queryWrapper.eq(Refund::getUserId, userId);
        }

        if (orderId != null) {
            queryWrapper.eq(Refund::getOrderId, orderId);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (StringUtils.hasText(startTime)) {
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            queryWrapper.ge(Refund::getCreateTime, start);
        }

        if (StringUtils.hasText(endTime)) {
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            queryWrapper.le(Refund::getCreateTime, end);
        }

        queryWrapper.orderByDesc(Refund::getCreateTime);

        return page(pageParam, queryWrapper);
    }

    @Override
    public long getPendingRefundCount() {
        LambdaQueryWrapper<Refund> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Refund::getStatus, RefundStatus.PENDING.getCode());
        return count(queryWrapper);
    }

    @Override
    public Map<String, Object> getRefundStatistics(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();

        // 处理时间范围
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (StringUtils.hasText(startTime)) {
            start = LocalDateTime.parse(startTime, formatter);
        }

        if (StringUtils.hasText(endTime)) {
            end = LocalDateTime.parse(endTime, formatter);
        }

        // 统计各状态的退款数量
        QueryWrapper<Refund> queryWrapper = new QueryWrapper<>();
        if (start != null) {
            queryWrapper.ge("create_time", start);
        }
        if (end != null) {
            queryWrapper.le("create_time", end);
        }
        queryWrapper.select("status, count(*) as count");
        queryWrapper.groupBy("status");

        Map<String, Long> statusCounts = new HashMap<>();
        getBaseMapper().selectMaps(queryWrapper).forEach(map -> {
            String status = (String) map.get("status");
            Long count = (Long) map.get("count");
            statusCounts.put(status, count);
        });

        // 计算总退款金额
        QueryWrapper<Refund> amountQuery = new QueryWrapper<>();
        if (start != null) {
            amountQuery.ge("create_time", start);
        }
        if (end != null) {
            amountQuery.le("create_time", end);
        }
        amountQuery.eq("status", RefundStatus.COMPLETED.getCode());
        amountQuery.select("sum(amount) as total_amount");

        Map<String, Object> amountMap = getBaseMapper().selectMaps(amountQuery).get(0);
        BigDecimal totalAmount = amountMap.get("total_amount") != null ? (BigDecimal) amountMap.get("total_amount")
                : BigDecimal.ZERO;

        // 返回结果
        result.put("statusCounts", statusCounts);
        result.put("totalAmount", totalAmount);
        result.put("pendingCount", statusCounts.getOrDefault(RefundStatus.PENDING.getCode(), 0L));
        result.put("approvedCount", statusCounts.getOrDefault(RefundStatus.APPROVED.getCode(), 0L));
        result.put("processingCount", statusCounts.getOrDefault(RefundStatus.PROCESSING.getCode(), 0L));
        result.put("completedCount", statusCounts.getOrDefault(RefundStatus.COMPLETED.getCode(), 0L));
        result.put("rejectedCount", statusCounts.getOrDefault(RefundStatus.REJECTED.getCode(), 0L));
        result.put("failedCount", statusCounts.getOrDefault(RefundStatus.FAILED.getCode(), 0L));

        return result;
    }

    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        return "R" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}