package com.muyingmall.statemachine;

import com.muyingmall.enums.RefundStatus;
import com.muyingmall.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 退款状态机
 */
@Component
public class RefundStateMachine implements StateMachine<RefundStatus, RefundEvent, RefundStateContext> {

    /**
     * 退款状态转换规则
     */
    private static final Map<RefundStatus, Map<RefundEvent, RefundStatus>> STATE_MACHINE_MAP = new HashMap<>();

    static {
        // 初始化状态机规则

        // 待处理状态下的转换规则
        Map<RefundEvent, RefundStatus> pendingMap = new HashMap<>();
        pendingMap.put(RefundEvent.APPROVE, RefundStatus.APPROVED);
        pendingMap.put(RefundEvent.REJECT, RefundStatus.REJECTED);
        pendingMap.put(RefundEvent.CANCEL, RefundStatus.REJECTED);
        // 添加SUBMIT事件，用于新建退款申请时的状态保持
        pendingMap.put(RefundEvent.SUBMIT, RefundStatus.PENDING);
        STATE_MACHINE_MAP.put(RefundStatus.PENDING, pendingMap);

        // 已批准状态下的转换规则
        Map<RefundEvent, RefundStatus> approvedMap = new HashMap<>();
        approvedMap.put(RefundEvent.PROCESS, RefundStatus.PROCESSING);
        STATE_MACHINE_MAP.put(RefundStatus.APPROVED, approvedMap);

        // 处理中状态下的转换规则
        Map<RefundEvent, RefundStatus> processingMap = new HashMap<>();
        processingMap.put(RefundEvent.COMPLETE, RefundStatus.COMPLETED);
        processingMap.put(RefundEvent.FAIL, RefundStatus.FAILED);
        STATE_MACHINE_MAP.put(RefundStatus.PROCESSING, processingMap);

        // 已拒绝状态下的转换规则 - 终态，无法再转换
        STATE_MACHINE_MAP.put(RefundStatus.REJECTED, new HashMap<>());

        // 已完成状态下的转换规则 - 终态，无法再转换
        STATE_MACHINE_MAP.put(RefundStatus.COMPLETED, new HashMap<>());

        // 退款失败状态下的转换规则 - 终态，无法再转换
        STATE_MACHINE_MAP.put(RefundStatus.FAILED, new HashMap<>());
    }

    @Override
    public RefundStatus sendEvent(RefundStatus currentStatus, RefundEvent event, RefundStateContext context) {
        Map<RefundEvent, RefundStatus> eventMap = STATE_MACHINE_MAP.get(currentStatus);
        if (eventMap == null || !eventMap.containsKey(event)) {
            throw new BusinessException("不支持的状态转换：从[" + currentStatus.getDesc() + "]状态触发[" + event + "]事件");
        }

        RefundStatus nextStatus = eventMap.get(event);

        // 记录状态变更
        context.setOldStatus(currentStatus);
        context.setNewStatus(nextStatus);
        context.setEvent(event);

        return nextStatus;
    }

    @Override
    public boolean canTransit(RefundStatus currentStatus, RefundStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return true;
        }

        Map<RefundEvent, RefundStatus> eventMap = STATE_MACHINE_MAP.get(currentStatus);
        if (eventMap == null) {
            return false;
        }

        return eventMap.values().contains(targetStatus);
    }

    @Override
    public RefundStatus[] getPossibleNextStates(RefundStatus currentStatus) {
        Map<RefundEvent, RefundStatus> eventMap = STATE_MACHINE_MAP.get(currentStatus);
        if (eventMap == null) {
            return new RefundStatus[0];
        }

        return eventMap.values().toArray(new RefundStatus[0]);
    }
}