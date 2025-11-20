package com.muyingmall.tcc;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * TCC事务模型类
 */
@Data
public class TccTransaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事务ID
     */
    private String transactionId;

    /**
     * 事务类型
     */
    private String transactionType;

    /**
     * 业务标识（例如支付ID或订单ID）
     */
    private String businessKey;

    /**
     * 事务状态
     * TRYING：尝试执行阶段
     * CONFIRMING：确认执行阶段
     * CANCELLING：取消执行阶段
     */
    private TccTransactionStatus status;

    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 最大重试次数
     */
    private int maxRetryCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 事务超时时间（毫秒）
     */
    private long timeout;

    /**
     * 序列化的业务参数
     */
    private String serializedParams;

    /**
     * TCC事务状态枚举
     */
    public enum TccTransactionStatus {
        /**
         * 尝试阶段
         */
        TRYING,

        /**
         * 确认阶段
         */
        CONFIRMING,

        /**
         * 取消阶段
         */
        CANCELLING,

        /**
         * 已确认（已完成）
         */
        CONFIRMED,

        /**
         * 已取消
         */
        CANCELLED
    }
}