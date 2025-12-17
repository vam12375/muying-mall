package com.muyingmall.listener;

import com.muyingmall.entity.UserMessage;
import com.muyingmall.event.CheckinEvent;
import com.muyingmall.event.MessageEvent;
import com.muyingmall.event.OrderStatusChangedEvent;
import com.muyingmall.event.PointsChangedEvent;
import com.muyingmall.event.ShippingReminderEvent;
import com.muyingmall.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 消息事件监听器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageEventListener {

    private final UserMessageService userMessageService;

    /**
     * 处理消息事件
     *
     * @param event 消息事件
     */
    @Async
    @EventListener
    public void handleMessageEvent(MessageEvent event) {
        log.debug("接收到消息事件: type={}, userId={}", event.getMessageType(), event.getUserId());

        try {
            // 创建用户消息
            UserMessage message = userMessageService.createMessage(
                    event.getUserId(),
                    event.getMessageType(),
                    event.getTitle(),
                    event.getContent(),
                    event.getExtra());

            log.debug("成功创建用户消息: messageId={}, type={}", message.getMessageId(), event.getMessageType());
        } catch (Exception e) {
            log.error("处理消息事件异常: type={}, userId={}, error={}",
                    event.getMessageType(), event.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 处理积分变更事件
     *
     * @param event 积分变更事件
     */
    @Async
    @EventListener
    public void handlePointsChangedEvent(PointsChangedEvent event) {
        log.debug("接收到积分变更事件: userId={}, pointsChange={}", event.getUserId(), event.getPointsChange());

        // 具体的积分变更消息处理逻辑
        // 此事件会被上面的通用事件处理器捕获并创建消息，这里可以添加特殊处理逻辑
    }

    /**
     * 处理订单状态变更事件
     *
     * @param event 订单状态变更事件
     */
    @Async
    @EventListener
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        log.debug("接收到订单状态变更事件: userId={}, orderId={}, newStatus={}",
                event.getUserId(), event.getOrderId(), event.getNewStatus());

        // 具体的订单状态变更消息处理逻辑
        // 此事件会被上面的通用事件处理器捕获并创建消息，这里可以添加特殊处理逻辑
    }

    /**
     * 处理催发货事件
     *
     * @param event 催发货事件
     */
    @Async
    @EventListener
    public void handleShippingReminderEvent(ShippingReminderEvent event) {
        log.debug("接收到催发货事件: userId={}, orderId={}", event.getUserId(), event.getOrderId());

        try {
            // 1. 创建给用户的回执消息 - 这条消息会显示在用户的消息中心
            userMessageService.createMessage(
                    event.getUserId(),
                    "REMIND", // 使用提醒类型
                    "您的催发货申请已收到",
                    "您对订单 " + event.getOrderNo() + " 的催发货申请已收到，我们将尽快为您发货。",
                    event.getExtra());
            
            // 2. 单独创建管理员通知消息 - 这条消息只在管理员后台显示
            // 注意：这里不会触发通用事件处理器，因为我们直接调用服务方法
            // 使用-1作为接收者ID表示这是发给管理员的系统消息
            UserMessage adminMessage = userMessageService.createMessage(
                    -1, // 管理员专用ID，表示这是系统消息/管理员消息
                    "SHIPPING_REMINDER", // 使用催发货类型
                    "用户催发货提醒 - " + event.getOrderNo().substring(Math.max(0, event.getOrderNo().length() - 8)),
                    event.getContent(), // 使用事件中预先生成的消息内容
                    event.getExtra());
            
            log.debug("成功创建催发货管理员通知: messageId={}", adminMessage.getMessageId());
        } catch (Exception e) {
            log.error("创建催发货消息异常: userId={}, orderId={}, error={}",
                    event.getUserId(), event.getOrderId(), e.getMessage(), e);
        }
        
        // 不再依赖通用事件处理器创建消息，避免将管理员消息显示给用户
    }

    /**
     * 处理签到事件
     *
     * @param event 签到事件
     */
    @Async
    @EventListener
    public void handleCheckinEvent(CheckinEvent event) {
        log.debug("接收到签到事件: userId={}, points={}, consecutiveDays={}",
                event.getUserId(), event.getPoints(), event.getConsecutiveDays());

        // 具体的签到消息处理逻辑
        // 此事件会被上面的通用事件处理器捕获并创建消息，这里可以添加特殊处理逻辑
    }
}