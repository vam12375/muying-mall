package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.PaymentStateLog;
import com.muyingmall.mapper.PaymentStateLogMapper;
import com.muyingmall.service.PaymentStateLogService;
import com.muyingmall.statemachine.PaymentStateContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 支付状态变更日志服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentStateLogServiceImpl extends ServiceImpl<PaymentStateLogMapper, PaymentStateLog>
        implements PaymentStateLogService {

    @Override
    public PaymentStateLog recordStateChange(PaymentStateContext context) {
        PaymentStateLog stateLog = PaymentStateLog.of(
                context.getPayment().getId(),
                context.getPayment().getPaymentNo(),
                context.getPayment().getOrderId(),
                context.getPayment().getOrderNo(),
                context.getOldStatus(),
                context.getNewStatus(),
                context.getEvent(),
                context.getOperator(),
                context.getReason());

        // 保存日志
        boolean result = save(stateLog);
        if (!result) {
            log.error("支付状态变更日志保存失败: {}", stateLog);
        }

        return stateLog;
    }

    @Override
    public List<PaymentStateLog> getPaymentStateHistory(Long paymentId) {
        LambdaQueryWrapper<PaymentStateLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentStateLog::getPaymentId, paymentId)
                .orderByAsc(PaymentStateLog::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public List<PaymentStateLog> getPaymentStateHistoryByOrderId(Integer orderId) {
        LambdaQueryWrapper<PaymentStateLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentStateLog::getOrderId, orderId)
                .orderByAsc(PaymentStateLog::getCreateTime);
        return list(queryWrapper);
    }
}