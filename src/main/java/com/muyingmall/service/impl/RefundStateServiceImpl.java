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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 退款状态服务实现类
 */
@Service
@Slf4j
public class RefundStateServiceImpl implements RefundStateService {

    private final RefundService refundService;
    private final RefundLogService refundLogService;
    private final OrderService orderService;
    private final RefundStateMachine refundStateMachine;
    private final OrderStateMachine orderStateMachine;

    @Autowired
    public RefundStateServiceImpl(@Lazy RefundService refundService,
                                  RefundLogService refundLogService,
                                  OrderService orderService,
                                  RefundStateMachine refundStateMachine,
                                  OrderStateMachine orderStateMachine) {
        this.refundService = refundService;
        this.refundLogService = refundLogService;
        this.orderService = orderService;
        this.refundStateMachine = refundStateMachine;
        this.orderStateMachine = orderStateMachine;
    }

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
                log.error("无效的当前退款状态, refundId: {}, status: {}", refundId, refund.getStatus());
                throw new BusinessException("无效的当前退款状态");
            }

            log.debug("准备执行状态转换, refundId: {}, 当前状态: {}, 事件: {}, 操作者: {}/{}, 原因: {}",
                    refundId, currentStatus.getDesc(), event, operatorType, operatorName, reason);

            // 特殊处理SUBMIT事件，确保其始终成功
            if (event == RefundEvent.SUBMIT) {
                log.debug("处理SUBMIT事件，重要的状态初始化事件，refundId: {}", refundId);

                // 即使当前状态不是PENDING，也尝试更新状态日志
                if (currentStatus != RefundStatus.PENDING) {
                    log.warn("SUBMIT事件期望当前状态为PENDING，但实际是: {}, refundId: {}", currentStatus.getDesc(), refundId);
                }

                // 记录状态变更日志
                refundLogService.logStatusChange(
                        refundId,
                        refund.getRefundNo(),
                        currentStatus.getCode(),
                        RefundStatus.PENDING.getCode(),
                        operatorType,
                        operatorId,
                        operatorName,
                        reason);
                log.debug("SUBMIT事件已记录状态日志, refundId: {}", refundId);
                return true;
            }

            // 记录可能的下一个状态
            RefundStatus[] possibleNextStates = refundStateMachine.getPossibleNextStates(currentStatus);
            if (possibleNextStates != null && possibleNextStates.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (RefundStatus status : possibleNextStates) {
                    sb.append(status.getDesc()).append(", ");
                }
                log.debug("当前状态[{}]允许的下一个状态: {}", currentStatus.getDesc(), sb.toString());
            } else {
                log.debug("当前状态[{}]没有配置下一个状态", currentStatus.getDesc());
            }

            // 执行状态转换
            log.debug("执行状态转换, refundId: {}, 从[{}]状态通过[{}]事件转换",
                    refundId, currentStatus.getDesc(), event);
            RefundStatus newStatus = refundStateMachine.sendEvent(currentStatus, event, context);
            log.debug("状态转换成功, refundId: {}, 从[{}]状态转换为[{}]状态",
                    refundId, currentStatus.getDesc(), newStatus.getDesc());

            // 更新退款状态
            refund.setStatus(newStatus.getCode());
            refundService.updateById(refund);
            log.debug("已更新退款记录状态, refundId: {}, 新状态: {}", refundId, newStatus.getDesc());

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
            log.debug("已记录状态变更日志, refundId: {}", refundId);

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