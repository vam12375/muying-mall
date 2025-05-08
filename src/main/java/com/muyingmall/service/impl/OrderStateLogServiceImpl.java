package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.OrderStateLog;
import com.muyingmall.mapper.OrderStateLogMapper;
import com.muyingmall.service.OrderStateLogService;
import com.muyingmall.statemachine.OrderStateContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单状态变更日志服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderStateLogServiceImpl extends ServiceImpl<OrderStateLogMapper, OrderStateLog>
        implements OrderStateLogService {

    @Override
    public OrderStateLog recordStateChange(OrderStateContext context) {
        OrderStateLog stateLog = OrderStateLog.of(
                context.getOrder().getOrderId(),
                context.getOrder().getOrderNo(),
                context.getOldStatus(),
                context.getNewStatus(),
                context.getEvent(),
                context.getOperator(),
                context.getReason());

        // 保存日志
        boolean result = save(stateLog);
        if (!result) {
            log.error("订单状态变更日志保存失败: {}", stateLog);
        }

        return stateLog;
    }

    @Override
    public List<OrderStateLog> getOrderStateHistory(Integer orderId) {
        LambdaQueryWrapper<OrderStateLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderStateLog::getOrderId, orderId)
                .orderByAsc(OrderStateLog::getCreateTime);
        return list(queryWrapper);
    }
}