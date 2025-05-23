package com.muyingmall.statemachine;

import com.muyingmall.entity.Refund;
import com.muyingmall.enums.RefundStatus;
import lombok.Data;

/**
 * 退款状态上下文
 */
@Data
public class RefundStateContext {

    /**
     * 退款申请
     */
    private Refund refund;

    /**
     * 原退款状态
     */
    private RefundStatus oldStatus;

    /**
     * 新退款状态
     */
    private RefundStatus newStatus;

    /**
     * 触发的事件
     */
    private RefundEvent event;

    /**
     * 状态变更的原因
     */
    private String reason;

    /**
     * 状态变更的操作者
     */
    private String operator;

    /**
     * 操作者类型：USER-用户, ADMIN-管理员, SYSTEM-系统
     */
    private String operatorType;

    /**
     * 操作者ID
     */
    private Integer operatorId;

    /**
     * 状态变更时间
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 创建上下文
     *
     * @param refund 退款申请
     * @return 退款状态上下文
     */
    public static RefundStateContext of(Refund refund) {
        RefundStateContext context = new RefundStateContext();
        context.setRefund(refund);
        context.setOldStatus(RefundStatus.getByCode(refund.getStatus()));
        return context;
    }
}