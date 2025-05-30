package com.muyingmall.service;

import com.muyingmall.entity.Refund;
import com.muyingmall.enums.RefundStatus;
import com.muyingmall.statemachine.RefundEvent;

/**
 * 退款状态服务接口
 */
public interface RefundStateService {

    /**
     * 触发退款状态变更事件
     *
     * @param refundId     退款ID
     * @param event        事件
     * @param operatorType 操作者类型
     * @param reason       原因
     * @return 是否成功
     */
    boolean sendEvent(Long refundId, RefundEvent event, String operatorType, String reason);

    /**
     * 触发退款状态变更事件（带操作人信息）
     *
     * @param refundId     退款ID
     * @param event        事件
     * @param operatorType 操作者类型
     * @param operatorName 操作者名称
     * @param operatorId   操作者ID
     * @param reason       原因
     * @return 是否成功
     */
    boolean sendEvent(Long refundId, RefundEvent event, String operatorType, String operatorName, Integer operatorId,
            String reason);

    /**
     * 检查状态是否可转换
     *
     * @param currentStatus 当前状态
     * @param targetStatus  目标状态
     * @return 是否可转换
     */
    boolean canTransit(RefundStatus currentStatus, RefundStatus targetStatus);

    /**
     * 获取指定状态可以转换到的下一个状态集合
     *
     * @param currentStatus 当前状态
     * @return 可转换的状态集合
     */
    RefundStatus[] getPossibleNextStates(RefundStatus currentStatus);
}