package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.RefundLog;
import com.muyingmall.mapper.RefundLogMapper;
import com.muyingmall.service.RefundLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 退款日志服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefundLogServiceImpl extends ServiceImpl<RefundLogMapper, RefundLog> implements RefundLogService {

    @Override
    public boolean logStatusChange(Long refundId, String refundNo, String oldStatus, String newStatus,
            String operatorType, Integer operatorId, String operatorName, String comment) {
        try {
            RefundLog refundLog = new RefundLog();
            refundLog.setRefundId(refundId);
            refundLog.setRefundNo(refundNo);
            refundLog.setOldStatus(oldStatus);
            refundLog.setNewStatus(newStatus);
            refundLog.setOperatorType(operatorType);
            refundLog.setOperatorId(operatorId);
            refundLog.setOperatorName(operatorName);
            refundLog.setComment(comment);
            refundLog.setCreateTime(LocalDateTime.now());

            return save(refundLog);
        } catch (Exception e) {
            log.error("记录退款状态变更日志失败，refundId: {}, error: {}", refundId, e.getMessage(), e);
            return false;
        }
    }
}