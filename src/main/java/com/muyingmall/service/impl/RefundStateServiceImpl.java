package com.muyingmall.service.impl;

import com.muyingmall.entity.Order;
import com.muyingmall.entity.Refund;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.enums.RefundStatus;
import com.muyingmall.exception.BusinessException;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.RefundLogService;
import com.muyingmall.service.RefundService;
import com.muyingmall.service.RefundStateService;
import com.muyingmall.statemachine.OrderEvent;
import com.muyingmall.statemachine.OrderStateContext;
import com.muyingmall.statemachine.OrderStateMachine;
import com.muyingmall.statemachine.RefundEvent;
import com.muyingmall.statemachine.RefundStateContext;
import com.muyingmall.statemachine.RefundStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 退款状态服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefundStateServiceImpl implements RefundStateService {

    private final RefundService refundService;
    private final RefundLogService refundLogService;
    private final OrderService orderService;
    private final RefundStateMachine refundStateMachine;
    private final OrderStateMachine orderStateMachine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendEvent(Long refundId, RefundEvent event, String operatorType, String reason) {
        return sendEvent(refundId, event, operatorType, null, null, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendEvent(Long refundId, RefundEvent event, String operatorType, String operatorName,
            Integer operatorId, String reason) {
        // 获取退款申请
        Refund refund = refundService.getById(refundId);
        if (refund == null) {
            throw new BusinessException("退款申请不存在");
        }

        // 创建状态上下文
        RefundStateContext context = RefundStateContext.of(refund);
        context.setOperatorType(operatorType);
        context.setOperator(operatorName);
        context.setOperatorId(operatorId);
        context.setReason(reason);

        try {
            // 获取当前状态
            RefundStatus currentStatus = RefundStatus.getByCode(refund.getStatus());
            if (currentStatus == null) {
                throw new BusinessException("无效的当前退款状态");
            }

            // 执行状态转换
            RefundStatus newStatus = refundStateMachine.sendEvent(currentStatus, event, context);

            // 更新退款状态
            refund.setStatus(newStatus.getCode());
            refundService.updateById(refund);

            // 记录状态变更日志
            refundLogService.logStatusChange(
                    refundId,
                    refund.getRefundNo(),
                    currentStatus.getCode(),
                    newStatus.getCode(),
                    operatorType,
                    operatorId,
                    operatorName,
                    reason);

            // 特殊事件处理（如触发订单状态变更）
            handleSpecialEvents(refund, event, newStatus);

            return true;
        } catch (Exception e) {
            log.error("退款状态变更失败, refundId: {}, event: {}, error: {}", refundId, event, e.getMessage(), e);
            throw new BusinessException("退款状态变更失败: " + e.getMessage());
        }
    }

    @Override
    public boolean canTransit(RefundStatus currentStatus, RefundStatus targetStatus) {
        return refundStateMachine.canTransit(currentStatus, targetStatus);
    }

    @Override
    public RefundStatus[] getPossibleNextStates(RefundStatus currentStatus) {
        return refundStateMachine.getPossibleNextStates(currentStatus);
    }

    /**
     * 处理特殊事件，如同步更新订单状态
     */
    private void handleSpecialEvents(Refund refund, RefundEvent event, RefundStatus newStatus) {
        // 获取关联的订单
        Order order = orderService.getById(refund.getOrderId());
        if (order == null) {
            log.warn("退款关联的订单不存在, refundId: {}, orderId: {}", refund.getId(), refund.getOrderId());
            return;
        }

        // 获取订单状态
        OrderStatus orderStatus = order.getStatus();
        if (orderStatus == null) {
            log.warn("无效的订单状态, refundId: {}, orderId: {}", refund.getId(), refund.getOrderId());
            return;
        }

        OrderStateContext orderContext = OrderStateContext.of(order);
        orderContext.setOperator("系统");
        orderContext.setOperatorType("SYSTEM");

        try {
            // 申请退款，将订单状态变更为退款中
            if (event == RefundEvent.SUBMIT && newStatus == RefundStatus.PENDING) {
                OrderStatus newOrderStatus = orderStateMachine.sendEvent(orderStatus, OrderEvent.REFUND_APPLY,
                        orderContext);
                order.setStatus(newOrderStatus);
                orderService.updateById(order);
            }
            // 退款完成，将订单状态变更为已退款
            else if (event == RefundEvent.COMPLETE && newStatus == RefundStatus.COMPLETED) {
                OrderStatus newOrderStatus = orderStateMachine.sendEvent(orderStatus, OrderEvent.REFUND_COMPLETE,
                        orderContext);
                order.setStatus(newOrderStatus);
                orderService.updateById(order);
            }
            // 退款失败，将订单状态恢复
            else if (event == RefundEvent.FAIL && newStatus == RefundStatus.FAILED) {
                OrderStatus newOrderStatus = orderStateMachine.sendEvent(orderStatus, OrderEvent.REFUND_FAIL,
                        orderContext);
                order.setStatus(newOrderStatus);
                orderService.updateById(order);
            }
        } catch (Exception e) {
            log.error("同步更新订单状态失败, refundId: {}, orderId: {}, error: {}",
                    refund.getId(), refund.getOrderId(), e.getMessage(), e);
        }
    }
}