package com.muyingmall.statemachine;

import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付状态机
 */
@Component
public class PaymentStateMachine implements StateMachine<PaymentStatus, PaymentEvent, PaymentStateContext> {

    /**
     * 支付状态转换规则
     */
    private static final Map<PaymentStatus, Map<PaymentEvent, PaymentStatus>> STATE_MACHINE_MAP = new HashMap<>();

    static {
        // 初始化状态机规则

        // 待支付状态下的转换规则
        Map<PaymentEvent, PaymentStatus> pendingMap = new HashMap<>();
        pendingMap.put(PaymentEvent.PROCESS, PaymentStatus.PROCESSING);
        pendingMap.put(PaymentEvent.CLOSE, PaymentStatus.CLOSED);
        pendingMap.put(PaymentEvent.TIMEOUT, PaymentStatus.CLOSED);
        STATE_MACHINE_MAP.put(PaymentStatus.PENDING, pendingMap);

        // 处理中状态下的转换规则
        Map<PaymentEvent, PaymentStatus> processingMap = new HashMap<>();
        processingMap.put(PaymentEvent.SUCCESS, PaymentStatus.SUCCESS);
        processingMap.put(PaymentEvent.FAIL, PaymentStatus.FAILED);
        processingMap.put(PaymentEvent.CLOSE, PaymentStatus.CLOSED);
        processingMap.put(PaymentEvent.TIMEOUT, PaymentStatus.CLOSED);
        STATE_MACHINE_MAP.put(PaymentStatus.PROCESSING, processingMap);

        // 支付成功状态下的转换规则
        Map<PaymentEvent, PaymentStatus> successMap = new HashMap<>();
        successMap.put(PaymentEvent.REFUND_REQUEST, PaymentStatus.REFUNDING);
        STATE_MACHINE_MAP.put(PaymentStatus.SUCCESS, successMap);

        // 支付失败状态下的转换规则 - 终态，无法再转换
        STATE_MACHINE_MAP.put(PaymentStatus.FAILED, new HashMap<>());

        // 已关闭状态下的转换规则 - 终态，无法再转换
        STATE_MACHINE_MAP.put(PaymentStatus.CLOSED, new HashMap<>());

        // 退款中状态下的转换规则
        Map<PaymentEvent, PaymentStatus> refundingMap = new HashMap<>();
        refundingMap.put(PaymentEvent.REFUND_SUCCESS, PaymentStatus.REFUNDED);
        refundingMap.put(PaymentEvent.REFUND_FAIL, PaymentStatus.FAILED);
        STATE_MACHINE_MAP.put(PaymentStatus.REFUNDING, refundingMap);

        // 已退款状态下的转换规则 - 终态，无法再转换
        STATE_MACHINE_MAP.put(PaymentStatus.REFUNDED, new HashMap<>());
    }

    @Override
    public PaymentStatus sendEvent(PaymentStatus currentState, PaymentEvent event, PaymentStateContext context) {
        Map<PaymentEvent, PaymentStatus> eventMap = STATE_MACHINE_MAP.get(currentState);
        if (eventMap == null || !eventMap.containsKey(event)) {
            throw new BusinessException("不支持的状态转换：从[" + currentState.getDesc() + "]状态触发[" + event + "]事件");
        }

        PaymentStatus nextState = eventMap.get(event);

        // 记录状态变更
        context.setOldStatus(currentState);
        context.setNewStatus(nextState);
        context.setEvent(event);

        return nextState;
    }

    @Override
    public boolean canTransit(PaymentStatus currentState, PaymentStatus targetState) {
        if (currentState == targetState) {
            return true;
        }

        Map<PaymentEvent, PaymentStatus> eventMap = STATE_MACHINE_MAP.get(currentState);
        if (eventMap == null) {
            return false;
        }

        return eventMap.values().contains(targetState);
    }

    @Override
    public PaymentStatus[] getPossibleNextStates(PaymentStatus currentState) {
        Map<PaymentEvent, PaymentStatus> eventMap = STATE_MACHINE_MAP.get(currentState);
        if (eventMap == null) {
            return new PaymentStatus[0];
        }

        return eventMap.values().toArray(new PaymentStatus[0]);
    }
}