package com.muyingmall.service.impl;

import com.muyingmall.entity.Payment;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.exception.BusinessException;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.PaymentStateLogService;
import com.muyingmall.service.PaymentStateService;
import com.muyingmall.statemachine.PaymentEvent;
import com.muyingmall.statemachine.PaymentStateContext;
import com.muyingmall.statemachine.PaymentStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 支付状态服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentStateServiceImpl implements PaymentStateService {

    private final PaymentService paymentService;
    private final PaymentStateMachine paymentStateMachine;
    private final PaymentStateLogService paymentStateLogService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment sendEvent(Long paymentId, PaymentEvent event, String operator, String reason) {
        Payment payment = paymentService.getById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在：" + paymentId);
        }
        return sendEvent(payment, event, operator, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment sendEvent(Payment payment, PaymentEvent event, String operator, String reason) {
        PaymentStateContext context = PaymentStateContext.of(payment);
        context.setEvent(event);
        context.setOperator(operator);
        context.setReason(reason);
        return sendEvent(context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment sendEvent(PaymentStateContext context) {
        Payment payment = context.getPayment();
        PaymentStatus currentStatus = payment.getStatus();
        PaymentEvent event = context.getEvent();

        try {
            // 执行状态转换
            PaymentStatus nextStatus = paymentStateMachine.sendEvent(currentStatus, event, context);

            // 更新支付状态
            updatePaymentStatus(payment, nextStatus, context);

            // 记录状态变更日志
            recordStateLog(context);

            // 发布状态变更事件
            publishStateChangedEvent(context);

            return payment;
        } catch (Exception e) {
            log.error("支付状态转换失败：paymentId={}, event={}, error={}",
                    payment.getId(), event, e.getMessage(), e);
            throw new BusinessException("支付状态转换失败：" + e.getMessage(), e);
        }
    }

    @Override
    public boolean canTransit(PaymentStatus currentStatus, PaymentStatus targetStatus) {
        return paymentStateMachine.canTransit(currentStatus, targetStatus);
    }

    @Override
    public PaymentStatus[] getPossibleNextStates(PaymentStatus currentStatus) {
        return paymentStateMachine.getPossibleNextStates(currentStatus);
    }

    /**
     * 更新支付状态
     *
     * @param payment    支付
     * @param nextStatus 下一个状态
     * @param context    状态上下文
     */
    private void updatePaymentStatus(Payment payment, PaymentStatus nextStatus, PaymentStateContext context) {
        // 设置新状态
        payment.setStatus(nextStatus);
        payment.setUpdateTime(LocalDateTime.now());

        // 根据事件类型设置特定的字段
        switch (context.getEvent()) {
            case SUCCESS:
                // 支付成功，设置支付时间和第三方交易号
                payment.setPaymentTime(LocalDateTime.now());
                if (context.getTransactionId() != null) {
                    payment.setTransactionId(context.getTransactionId());
                }
                break;
            case TIMEOUT:
                // 支付超时，关闭支付
                break;
            case REFUND_SUCCESS:
                // 退款成功
                break;
            default:
                break;
        }

        // 使用乐观锁更新支付记录，可防止并发更新问题
        boolean updateResult = paymentService.updateById(payment);
        if (!updateResult) {
            throw new BusinessException("支付状态更新失败，可能已被其他操作修改");
        }
    }

    /**
     * 记录支付状态变更日志
     *
     * @param context 状态上下文
     */
    private void recordStateLog(PaymentStateContext context) {
        // 记录日志到控制台
        log.info("支付状态变更：paymentId={}, paymentNo={}, oldStatus={}, newStatus={}, event={}, operator={}, reason={}",
                context.getPayment().getId(),
                context.getPayment().getPaymentNo(),
                context.getOldStatus(),
                context.getNewStatus(),
                context.getEvent(),
                context.getOperator(),
                context.getReason());

        // 记录日志到数据库
        paymentStateLogService.recordStateChange(context);
    }

    /**
     * 发布支付状态变更事件
     * 
     * @param context 状态上下文
     */
    private void publishStateChangedEvent(PaymentStateContext context) {
        // 发布支付状态变更事件，供其他模块监听处理
        if (eventPublisher != null) {
            PaymentStateChangedEvent event = new PaymentStateChangedEvent(
                    context.getPayment(),
                    context.getOldStatus(),
                    context.getNewStatus(),
                    context.getEvent(),
                    context.getOperator(),
                    context.getReason(),
                    context.getTransactionId());
            eventPublisher.publishEvent(event);
        }
    }

    /**
     * 支付状态变更事件
     */
    public static class PaymentStateChangedEvent {
        private final Payment payment;
        private final PaymentStatus oldStatus;
        private final PaymentStatus newStatus;
        private final PaymentEvent event;
        private final String operator;
        private final String reason;
        private final String transactionId;

        public PaymentStateChangedEvent(Payment payment, PaymentStatus oldStatus, PaymentStatus newStatus,
                PaymentEvent event, String operator, String reason, String transactionId) {
            this.payment = payment;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.event = event;
            this.operator = operator;
            this.reason = reason;
            this.transactionId = transactionId;
        }

        public Payment getPayment() {
            return payment;
        }

        public PaymentStatus getOldStatus() {
            return oldStatus;
        }

        public PaymentStatus getNewStatus() {
            return newStatus;
        }

        public PaymentEvent getEvent() {
            return event;
        }

        public String getOperator() {
            return operator;
        }

        public String getReason() {
            return reason;
        }

        public String getTransactionId() {
            return transactionId;
        }
    }
}