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
        log.info("接收到订单完成事件: Order ID {}", event.getOrderId());
        try {
            Integer userId = event.getUserId();
            BigDecimal actualAmount = event.getActualAmount();
            String orderNo = event.getOrderNo();
            Integer orderId = event.getOrderId();

            // 检查用户ID和金额是否有效
            if (userId == null || actualAmount == null || actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("无效的用户ID或订单金额，无法为订单 {} 添加积分", orderNo);
                return;
            }

            // 计算积分 (实付金额的10%，四舍五入到整数)
            int pointsToAdd = actualAmount.multiply(BigDecimal.valueOf(0.1))
                    .setScale(0, RoundingMode.HALF_UP).intValue();

            if (pointsToAdd > 0) {
                log.info("为用户 {} 订单 {} (ID: {}) 增加积分: {}", userId, orderNo, orderId, pointsToAdd);

                // 1. 记录积分历史
                PointsHistory history = new PointsHistory();
                history.setUserId(userId);
                history.setPoints(pointsToAdd);
                history.setType("earn"); // 'earn' 表示获得积分
                history.setSource("order"); // 设置来源为订单
                history.setReferenceId(String.valueOf(orderId)); // 设置关联ID为订单ID字符串
                history.setDescription(String.format("订单 %s 完成获得积分", orderNo));
                // create_time 由数据库自动填充
                pointsHistoryMapper.insert(history);
                log.info("积分历史记录成功: userId={}, points={}, historyId={}", userId, pointsToAdd, history.getId());

                // 2. 更新用户总积分 (使用 PointsService 来处理)
                pointsService.addPoints(userId, pointsToAdd, "order", String.valueOf(orderId),
                        String.format("订单 %s 完成获得积分", orderNo));
                log.info("用户总积分更新调用成功: userId={}, addedPoints={}", userId, pointsToAdd);

            } else {
                log.info("订单 {} (ID: {}) 实付金额过低 ({})，无需增加积分", orderNo, orderId, actualAmount);
            }
        } catch (Exception e) {
            log.error("处理订单完成事件失败 for Order ID {}: {}", event.getOrderId(), e.getMessage(), e);
            // 考虑更健壮的异常处理，例如重试或记录到死信队列
        }
    }
}