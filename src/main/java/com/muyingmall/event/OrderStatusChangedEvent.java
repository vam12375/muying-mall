package com.muyingmall.event;

import com.muyingmall.enums.MessageType;
import lombok.Getter;

/**
 * 订单状态变更事件
 */
@Getter
public class OrderStatusChangedEvent extends MessageEvent {

    /**
     * 订单ID
     */
    private final Integer orderId;

    /**
     * 订单号
     */
    private final String orderNo;

    /**
     * 原订单状态
     */
    private final String oldStatus;

    /**
     * 新订单状态
     */
    private final String newStatus;

    /**
     * 构造函数
     *
     * @param source    事件源
     * @param userId    用户ID
     * @param orderId   订单ID
     * @param orderNo   订单号
     * @param oldStatus 原订单状态
     * @param newStatus 新订单状态
     * @param extra     额外信息
     */
    public OrderStatusChangedEvent(Object source, Integer userId, Integer orderId, String orderNo,
            String oldStatus, String newStatus, String extra) {
        super(source, userId, generateTitle(newStatus, orderNo), generateContent(oldStatus, newStatus, orderNo), extra);
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String getMessageType() {
        return MessageType.ORDER.getCode();
    }

    /**
     * 生成消息标题
     * 优化：支持大写和小写下划线格式的状态码
     *
     * @param newStatus 新状态
     * @param orderNo   订单号
     * @return 消息标题
     */
    private static String generateTitle(String newStatus, String orderNo) {
        String shortOrderNo = orderNo.substring(Math.max(0, orderNo.length() - 8));

        // 统一转换为大写进行匹配
        String upperNewStatus = newStatus != null ? newStatus.toUpperCase() : "";
        
        switch (upperNewStatus) {
            case "PAID":
            case "PENDING_SHIPMENT":
                return "订单支付成功，等待发货 - " + shortOrderNo;
            case "SHIPPED":
                return "订单已发货 - " + shortOrderNo;
            case "DELIVERED":
                return "订单已送达 - " + shortOrderNo;
            case "COMPLETED":
                return "订单已完成 - " + shortOrderNo;
            case "CANCELLED":
                return "订单已取消 - " + shortOrderNo;
            case "REFUNDING":
                return "订单申请退款中 - " + shortOrderNo;
            case "REFUNDED":
                return "订单已退款 - " + shortOrderNo;
            default:
                return "订单状态已更新 - " + shortOrderNo;
        }
    }

    /**
     * 生成消息内容
     * 优化：提供更友好的状态变更描述
     *
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param orderNo   订单号
     * @return 消息内容
     */
    private static String generateContent(String oldStatus, String newStatus, String orderNo) {
        StringBuilder content = new StringBuilder();
        content.append("您的订单 ").append(orderNo).append(" ");

        // 统一转换为大写进行匹配
        String upperNewStatus = newStatus != null ? newStatus.toUpperCase() : "";
        
        switch (upperNewStatus) {
            case "PAID":
            case "PENDING_SHIPMENT":
                content.append("已支付成功，商家正在处理您的订单，请耐心等待发货。");
                break;
            case "SHIPPED":
                content.append("已发货，请注意查收。");
                break;
            case "DELIVERED":
                content.append("已送达，如有问题请联系客服。");
                break;
            case "COMPLETED":
                content.append("已完成，感谢您的购买！");
                break;
            case "CANCELLED":
                content.append("已取消，如有疑问请联系客服。");
                break;
            case "REFUNDING":
                content.append("正在申请退款，我们将尽快处理。");
                break;
            case "REFUNDED":
                content.append("已退款，退款金额将原路返回，请注意查收。");
                break;
            default:
                // 使用友好的中文描述替代英文状态码
                content.append("状态已从【").append(getStatusDesc(oldStatus))
                        .append("】更新为【").append(getStatusDesc(newStatus)).append("】。");
        }

        return content.toString();
    }

    /**
     * 获取状态的中文描述
     * 支持大写格式（PAID）和小写下划线格式（pending_payment）
     *
     * @param status 状态代码
     * @return 状态描述
     */
    private static String getStatusDesc(String status) {
        if (status == null) {
            return "未知状态";
        }
        
        // 转换为大写以统一处理
        String upperStatus = status.toUpperCase();
        
        switch (upperStatus) {
            case "CREATED":
            case "PENDING_PAYMENT":
                return "待支付";
            case "PAID":
            case "PENDING_SHIPMENT":
                return "待发货";
            case "SHIPPED":
                return "已发货";
            case "DELIVERED":
                return "已送达";
            case "COMPLETED":
                return "已完成";
            case "CANCELLED":
                return "已取消";
            case "REFUNDING":
                return "退款中";
            case "REFUNDED":
                return "已退款";
            default:
                return status;
        }
    }
}