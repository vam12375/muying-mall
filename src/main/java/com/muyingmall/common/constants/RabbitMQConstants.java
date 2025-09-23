package com.muyingmall.common.constants;

/**
 * RabbitMQ常量类
 * 定义交换机、队列和路由键名称
 */
public class RabbitMQConstants {

    // ==================== 交换机定义 ====================

    /**
     * 订单交换机
     */
    public static final String ORDER_EXCHANGE = "order.exchange";

    /**
     * 支付交换机
     */
    public static final String PAYMENT_EXCHANGE = "payment.exchange";

    // ==================== 订单相关队列定义 ====================

    /**
     * 订单创建队列
     */
    public static final String ORDER_CREATE_QUEUE = "order.create.queue";

    /**
     * 订单状态变更队列
     */
    public static final String ORDER_STATUS_QUEUE = "order.status.queue";

    /**
     * 订单取消队列
     */
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";

    /**
     * 订单完成队列
     */
    public static final String ORDER_COMPLETE_QUEUE = "order.complete.queue";

    // ==================== 支付相关队列定义 ====================

    /**
     * 支付成功队列
     */
    public static final String PAYMENT_SUCCESS_QUEUE = "payment.success.queue";

    /**
     * 支付失败队列
     */
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";

    /**
     * 退款处理队列
     */
    public static final String PAYMENT_REFUND_QUEUE = "payment.refund.queue";

    // ==================== 订单相关路由键定义 ====================

    /**
     * 订单创建路由键
     */
    public static final String ORDER_CREATE_KEY = "order.create";

    /**
     * 订单状态变更路由键模式（支持通配符）
     */
    public static final String ORDER_STATUS_KEY = "order.status.#";

    /**
     * 订单状态变更路由键前缀
     */
    public static final String ORDER_STATUS_PREFIX = "order.status";

    /**
     * 订单取消路由键
     */
    public static final String ORDER_CANCEL_KEY = "order.cancel";

    /**
     * 订单完成路由键
     */
    public static final String ORDER_COMPLETE_KEY = "order.complete";

    // ==================== 支付相关路由键定义 ====================

    /**
     * 支付成功路由键
     */
    public static final String PAYMENT_SUCCESS_KEY = "payment.success";

    /**
     * 支付失败路由键
     */
    public static final String PAYMENT_FAILED_KEY = "payment.failed";

    /**
     * 退款处理路由键
     */
    public static final String PAYMENT_REFUND_KEY = "payment.refund";

    // ==================== 死信队列相关定义 ====================

    /**
     * 死信交换机
     */
    public static final String DLX_EXCHANGE = "dlx.exchange";

    /**
     * 死信队列
     */
    public static final String DLX_QUEUE = "dlx.queue";

    /**
     * 死信路由键
     */
    public static final String DLX_ROUTING_KEY = "dlx.routing.key";

    // ==================== 消息TTL和重试配置 ====================

    /**
     * 消息默认TTL（毫秒）- 30分钟
     */
    public static final long DEFAULT_MESSAGE_TTL = 30 * 60 * 1000L;

    /**
     * 队列最大长度
     */
    public static final int MAX_QUEUE_LENGTH = 10000;

    /**
     * 消息最大重试次数
     */
    public static final int MAX_RETRY_COUNT = 3;

    // ==================== 工具方法 ====================

    /**
     * 生成订单状态变更路由键
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @return 路由键
     */
    public static String getOrderStatusRoutingKey(String oldStatus, String newStatus) {
        return ORDER_STATUS_PREFIX + "." + oldStatus + "." + newStatus;
    }

    /**
     * 生成用户相关的路由键
     * @param baseKey 基础路由键
     * @param userId 用户ID
     * @return 用户相关路由键
     */
    public static String getUserRoutingKey(String baseKey, Integer userId) {
        return baseKey + ".user." + userId;
    }
}