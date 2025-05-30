package com.muyingmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.entity.RefundLog;

/**
 * 退款日志服务接口
 */
public interface RefundLogService extends IService<RefundLog> {

    /**
     * 记录退款状态变更日志
     *
     * @param refundId     退款ID
     * @param refundNo     退款单号
     * @param oldStatus    旧状态
     * @param newStatus    新状态
     * @param operatorType 操作者类型
     * @param operatorId   操作者ID
     * @param operatorName 操作者名称
     * @param comment      处理备注
     * @return 是否成功
     */
    boolean logStatusChange(Long refundId, String refundNo, String oldStatus, String newStatus,
            String operatorType, Integer operatorId, String operatorName, String comment);
}