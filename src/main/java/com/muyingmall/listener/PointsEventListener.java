package com.muyingmall.listener;

import com.muyingmall.entity.PointsHistory;
import com.muyingmall.event.OrderCompletedEvent;
import com.muyingmall.mapper.PointsHistoryMapper;
import com.muyingmall.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener; // 基础事件监听
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointsEventListener {

    private final PointsHistoryMapper pointsHistoryMapper;
    private final PointsService pointsService; // 注入PointsService

    /**
     * 处理订单完成事件（确认收货后）
     * 在主事务提交后执行，确保订单状态已持久化
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 在新事务中执行积分操作
    // @Async // 取消注释以启用异步处理 (需要 @EnableAsync 配置)
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("接收到订单完成事件: Order ID {}, 开始处理积分奖励", event.getOrderId());
        try {
            Integer userId = event.getUserId();
            BigDecimal actualAmount = event.getActualAmount();
            String orderNo = event.getOrderNo();
            Integer orderId = event.getOrderId();

            log.info("订单完成事件详情 - 用户ID: {}, 订单号: {}, 订单ID: {}, 实付金额: {}", 
                    userId, orderNo, orderId, actualAmount);

            // 检查用户ID和金额是否有效
            if (userId == null || actualAmount == null || actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("无效的用户ID或订单金额，无法为订单 {} 添加积分", orderNo);
                return;
            }

            // 调用pointsService的awardPointsForOrder方法处理积分奖励
            log.info("调用 pointsService.awardPointsForOrder 为订单 {} 奖励积分", orderId);
            pointsService.awardPointsForOrder(userId, orderId, actualAmount);
            log.info("订单 {} 积分奖励处理完成", orderId);

        } catch (Exception e) {
            log.error("处理订单完成事件积分奖励失败 for Order ID {}: {}", event.getOrderId(), e.getMessage(), e);
            // 考虑更健壮的异常处理，例如重试或记录到死信队列
        }
    }
}