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
import com.muyingmall.entity.OrderProduct;
import com.muyingmall.entity.Product;
import com.muyingmall.mapper.OrderProductMapper;
import com.muyingmall.service.AlipayRefundService;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.ProductService;
import com.muyingmall.service.ProductSkuService;
import com.muyingmall.service.RefundLogService;
import com.muyingmall.service.RefundService;
import com.muyingmall.service.RefundStateService;
import com.muyingmall.service.UserAccountService;
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
import java.util.List;
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
    private final UserAccountService userAccountService;
    private final RefundStateService refundStateService;
    private final RefundLogService refundLogService;
    private final AlipayRefundService alipayRefundService;
    private final OrderProductMapper orderProductMapper;
    private final ProductSkuService productSkuService;
    private final ProductService productService;

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

        // 触发状态机SUBMIT事件，确保状态转换被正确记录
        try {
            refundStateService.sendEvent(refund.getId(), RefundEvent.SUBMIT, "USER",
                    "用户" + userId, userId, "用户申请退款：" + refundReason);
            log.debug("已触发退款申请SUBMIT事件，退款ID: {}, 用户ID: {}", refund.getId(), userId);
        } catch (Exception e) {
            // 不影响退款申请的创建，但需要记录异常
            log.error("触发退款申请SUBMIT事件失败，但退款申请已创建，退款ID: {}, 错误: {}", refund.getId(), e.getMessage(), e);
        }

        // 同步更新订单状态为退款中
        try {
            Order orderToUpdate = orderService.getById(orderId);
            OrderStatus currentOrderStatus = orderToUpdate.getStatus();

            // 只有在订单状态允许的情况下才更新订单状态
            if (currentOrderStatus != OrderStatus.REFUNDING &&
                    currentOrderStatus != OrderStatus.REFUNDED) {
                orderToUpdate.setStatus(OrderStatus.REFUNDING);
                orderService.updateById(orderToUpdate);

                // 记录订单状态变更日志（如果需要）
                log.debug("订单状态已更新为退款中, 订单ID: {}, 退款ID: {}", orderId, refund.getId());
            }
        } catch (Exception e) {
            log.error("更新订单状态失败，但退款申请已创建，订单ID: {}, 错误: {}", orderId, e.getMessage());
            // 不抛出异常，允许退款申请继续处理
        }

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

        try {
            // 根据不同的退款渠道处理退款
            String transactionId = null;
            if ("ALIPAY".equals(refundChannel)) {
                // 调用支付宝退款
                log.debug("开始调用支付宝退款接口, 退款单号: {}", refund.getRefundNo());
                transactionId = alipayRefundService.refund(refund);
                if (StringUtils.hasText(transactionId)) {
                    // 保存支付宝返回的交易号
                    refund.setTransactionId(transactionId);
                    updateById(refund);
                    log.debug("支付宝退款调用成功, 退款单号: {}, 交易号: {}", refund.getRefundNo(), transactionId);
                }
            }
            // 其他退款渠道处理...
            // else if ("WECHAT".equals(refundChannel)) { ... }
        } catch (Exception e) {
            log.error("退款处理失败", e);
            // 发生异常时不影响状态流转，但需要记录日志
            refundLogService.logStatusChange(refundId, refund.getRefundNo(), refund.getStatus(), refund.getStatus(),
                    "SYSTEM", null, "系统", "调用退款接口失败: " + e.getMessage());
        }

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

        // 如果是支付宝退款，验证退款状态
        if ("ALIPAY".equals(refund.getRefundChannel())) {
            try {
                String refundStatus = alipayRefundService.queryRefundStatus(refund.getRefundNo(), transactionId);
                log.debug("支付宝退款状态查询结果: {}, 退款单号: {}", refundStatus, refund.getRefundNo());

                // 如果支付宝返回退款失败，则不允许标记为完成
                if ("REFUND_FAIL".equals(refundStatus)) {
                    throw new BusinessException("支付宝退款失败，无法标记为完成");
                }
            } catch (Exception e) {
                log.error("查询支付宝退款状态失败", e);
                // 查询失败时，记录日志但允许继续操作
                refundLogService.logStatusChange(refundId, refund.getRefundNo(), refund.getStatus(), refund.getStatus(),
                        "SYSTEM", null, "系统", "查询支付宝退款状态失败: " + e.getMessage());
            }
        }

        // 处理钱包退款
        try {
            // 查询订单信息，获取支付方式
            Order order = orderService.getById(refund.getOrderId());
            if (order != null && "wallet".equals(order.getPaymentMethod())) {
                // 钱包支付的订单，退款到钱包
                log.debug("处理钱包退款: 订单ID={}, 用户ID={}, 退款金额={}",
                        order.getOrderId(), order.getUserId(), refund.getAmount());

                // 调用钱包退款方法
                userAccountService.refundToWallet(order.getUserId(), refund.getAmount(),
                        "订单退款：" + order.getOrderNo());

                log.debug("钱包退款完成: 订单ID={}, 退款金额={}", order.getOrderId(), refund.getAmount());
            }
        } catch (Exception e) {
            log.error("处理钱包退款失败: 退款ID={}, 错误={}", refundId, e.getMessage(), e);
            // 钱包退款失败不影响退款状态流转，但需要记录日志
            refundLogService.logStatusChange(refundId, refund.getRefundNo(), refund.getStatus(), refund.getStatus(),
                    "SYSTEM", null, "系统", "钱包退款失败: " + e.getMessage());
        }

        // 更新退款信息
        refund.setTransactionId(transactionId);
        refund.setRefundTime(LocalDateTime.now());
        updateById(refund);

        // 恢复库存
        try {
            restoreStockForRefund(refund.getOrderId());
            log.debug("退款完成，库存已恢复: 订单ID={}", refund.getOrderId());
        } catch (Exception e) {
            log.error("恢复库存失败: 订单ID={}, 错误={}", refund.getOrderId(), e.getMessage(), e);
            // 库存恢复失败不影响退款状态流转，但需要记录日志
            refundLogService.logStatusChange(refundId, refund.getRefundNo(), refund.getStatus(), refund.getStatus(),
                    "SYSTEM", null, "系统", "库存恢复失败: " + e.getMessage());
        }

        // 更新退款状态为已完成
        refundStateService.sendEvent(refundId, RefundEvent.COMPLETE, "ADMIN", adminName, adminId,
                "管理员完成退款，交易号：" + transactionId);

        return true;
    }

    /**
     * 退款时恢复库存
     * @param orderId 订单ID
     */
    private void restoreStockForRefund(Integer orderId) {
        // 查询订单商品
        LambdaQueryWrapper<OrderProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderProduct::getOrderId, orderId);
        List<OrderProduct> orderProducts = orderProductMapper.selectList(queryWrapper);

        if (orderProducts == null || orderProducts.isEmpty()) {
            log.warn("订单商品为空，无法恢复库存: orderId={}", orderId);
            return;
        }

        for (OrderProduct orderProduct : orderProducts) {
            Integer quantity = orderProduct.getQuantity();
            Long skuId = orderProduct.getSkuId();
            Integer productId = orderProduct.getProductId();

            if (skuId != null) {
                // 有SKU，恢复SKU库存
                productSkuService.restoreStock(skuId, quantity);
                log.debug("SKU库存已恢复: skuId={}, quantity={}, orderId={}", skuId, quantity, orderId);
            } else {
                // 无SKU，恢复商品主表库存
                Product product = productService.getById(productId);
                if (product != null) {
                    product.setStock(product.getStock() + quantity);
                    product.setSales(Math.max(0, product.getSales() - quantity)); // 销量减少，但不能为负
                    productService.updateById(product);
                    log.debug("商品库存已恢复: productId={}, quantity={}, orderId={}", productId, quantity, orderId);
                }
            }
        }
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

        log.debug("管理员查询退款列表: page={}, size={}, status={}, userId={}, orderId={}, startTime={}, endTime={}",
                page, size, status, userId, orderId, startTime, endTime);

        Page<Refund> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Refund> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Refund::getStatus, status);
            log.debug("添加状态过滤条件: status={}", status);
        } else {
            log.debug("未指定状态过滤条件，将返回所有状态的退款申请");
        }

        if (userId != null) {
            queryWrapper.eq(Refund::getUserId, userId);
            log.debug("添加用户ID过滤条件: userId={}", userId);
        }

        if (orderId != null) {
            queryWrapper.eq(Refund::getOrderId, orderId);
            log.debug("添加订单ID过滤条件: orderId={}", orderId);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (StringUtils.hasText(startTime)) {
            try {
                LocalDateTime start = LocalDateTime.parse(startTime, formatter);
                queryWrapper.ge(Refund::getCreateTime, start);
                log.debug("添加开始时间过滤条件: startTime={}", start);
            } catch (Exception e) {
                log.warn("解析开始时间失败: {}, 将忽略此条件", startTime, e);
            }
        }

        if (StringUtils.hasText(endTime)) {
            try {
                LocalDateTime end = LocalDateTime.parse(endTime, formatter);
                queryWrapper.le(Refund::getCreateTime, end);
                log.debug("添加结束时间过滤条件: endTime={}", end);
            } catch (Exception e) {
                log.warn("解析结束时间失败: {}, 将忽略此条件", endTime, e);
            }
        }

        // 默认按创建时间倒序排序
        queryWrapper.orderByDesc(Refund::getCreateTime);
        log.debug("设置排序: 按创建时间降序");

        // 执行查询
        Page<Refund> resultPage = page(pageParam, queryWrapper);
        log.debug("查询结果: 总记录数={}, 当前页记录数={}", resultPage.getTotal(), resultPage.getRecords().size());

        // 如果没有记录，记录一下SQL以便调试
        if (resultPage.getTotal() == 0) {
            log.warn("未找到符合条件的退款记录，请检查查询条件或数据库");
        }

        // 关联查询用户信息
        if (!resultPage.getRecords().isEmpty()) {
            log.debug("开始关联查询用户信息，退款记录数: {}", resultPage.getRecords().size());
            for (Refund refund : resultPage.getRecords()) {
                if (refund.getUserId() != null) {
                    try {
                        User user = userService.getById(refund.getUserId());
                        if (user != null) {
                            refund.setUsername(user.getUsername());
                            log.debug("为退款单 {} 设置用户名: {}", refund.getRefundNo(), user.getUsername());
                        }
                    } catch (Exception e) {
                        log.warn("获取用户信息失败，用户ID: {}, 错误: {}", refund.getUserId(), e.getMessage());
                    }
                }
            }
            log.debug("用户信息关联查询完成");
        }

        return resultPage;
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

        try {
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
            BigDecimal totalAmount = BigDecimal.ZERO;
            try {
                QueryWrapper<Refund> amountQuery = new QueryWrapper<>();
                if (start != null) {
                    amountQuery.ge("create_time", start);
                }
                if (end != null) {
                    amountQuery.le("create_time", end);
                }
                amountQuery.eq("status", RefundStatus.COMPLETED.getCode());
                amountQuery.select("sum(amount) as total_amount");

                List<Map<String, Object>> amountResults = getBaseMapper().selectMaps(amountQuery);
                if (amountResults != null && !amountResults.isEmpty()) {
                    Map<String, Object> amountMap = amountResults.get(0);
                    if (amountMap != null && amountMap.get("total_amount") != null) {
                        totalAmount = (BigDecimal) amountMap.get("total_amount");
                    }
                }
            } catch (Exception e) {
                log.error("计算总退款金额失败", e);
                // 失败时使用默认值0
            }

            // 返回结果
            result.put("statusCounts", statusCounts);
            result.put("totalAmount", totalAmount);
            result.put("pendingCount", statusCounts.getOrDefault(RefundStatus.PENDING.getCode(), 0L));
            result.put("approvedCount", statusCounts.getOrDefault(RefundStatus.APPROVED.getCode(), 0L));
            result.put("processingCount", statusCounts.getOrDefault(RefundStatus.PROCESSING.getCode(), 0L));
            result.put("completedCount", statusCounts.getOrDefault(RefundStatus.COMPLETED.getCode(), 0L));
            result.put("rejectedCount", statusCounts.getOrDefault(RefundStatus.REJECTED.getCode(), 0L));
            result.put("failedCount", statusCounts.getOrDefault(RefundStatus.FAILED.getCode(), 0L));
        } catch (Exception e) {
            log.error("获取退款统计数据失败", e);
            // 发生异常时提供默认值
            result.put("statusCounts", new HashMap<>());
            result.put("totalAmount", BigDecimal.ZERO);
            result.put("pendingCount", 0L);
            result.put("approvedCount", 0L);
            result.put("processingCount", 0L);
            result.put("completedCount", 0L);
            result.put("rejectedCount", 0L);
            result.put("failedCount", 0L);
        }

        return result;
    }

    /**
     * 根据退款单号查询退款记录
     *
     * @param refundNo 退款单号
     * @return 退款记录，如果不存在返回null
     */
    @Override
    public Refund getRefundByRefundNo(String refundNo) {
        if (!StringUtils.hasText(refundNo)) {
            return null;
        }

        LambdaQueryWrapper<Refund> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Refund::getRefundNo, refundNo);

        return getOne(queryWrapper);
    }

    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        return "R" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}