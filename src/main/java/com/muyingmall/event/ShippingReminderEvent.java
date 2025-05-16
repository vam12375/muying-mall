package com.muyingmall.event;

import com.muyingmall.enums.MessageType;
import lombok.Getter;

/**
 * 催发货事件
 */
@Getter
public class ShippingReminderEvent extends MessageEvent {

    /**
     * 订单ID
     */
    private final Integer orderId;

    /**
     * 订单号
     */
    private final String orderNo;

    /**
     * 用户留言
     */
    private final String userMessage;

    /**
     * 下单时间
     */
    private final String orderTime;

    /**
     * 构造函数
     *
     * @param source      事件源
     * @param userId      用户ID
     * @param orderId     订单ID
     * @param orderNo     订单号
     * @param orderTime   下单时间
     * @param userMessage 用户留言
     * @param extra       额外信息
     */
    public ShippingReminderEvent(Object source, Integer userId, Integer orderId, String orderNo,
            String orderTime, String userMessage, String extra) {
        super(source, userId, generateTitle(orderNo), generateContent(orderNo, orderTime, userMessage), extra);
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.userMessage = userMessage;
        this.orderTime = orderTime;
    }

    @Override
    public String getMessageType() {
        return MessageType.SHIPPING_REMINDER.getCode();
    }

    /**
     * 生成消息标题
     *
     * @param orderNo 订单号
     * @return 消息标题
     */
    private static String generateTitle(String orderNo) {
        String shortOrderNo = orderNo.substring(Math.max(0, orderNo.length() - 8));
        return "用户催发货提醒 - " + shortOrderNo;
    }

    /**
     * 生成消息内容
     *
     * @param orderNo     订单号
     * @param orderTime   下单时间
     * @param userMessage 用户留言
     * @return 消息内容
     */
    private static String generateContent(String orderNo, String orderTime, String userMessage) {
        StringBuilder content = new StringBuilder();
        content.append("用户对订单 ").append(orderNo).append(" 进行了催发货。\n");
        content.append("下单时间：").append(orderTime).append("\n");
        if (userMessage != null && !userMessage.isEmpty()) {
            content.append("用户留言：").append(userMessage);
        } else {
            content.append("用户未留言。");
        }
        return content.toString();
    }
}