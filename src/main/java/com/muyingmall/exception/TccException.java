package com.muyingmall.exception;

/**
 * TCC事务异常类
 */
public class TccException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 事务ID
     */
    private String transactionId;

    /**
     * 事务阶段
     */
    private String phase;

    /**
     * 构造一个TCC事务异常
     */
    public TccException() {
        super();
    }

    /**
     * 构造一个TCC事务异常
     *
     * @param message 异常信息
     */
    public TccException(String message) {
        super(message);
    }

    /**
     * 构造一个TCC事务异常
     *
     * @param message 异常信息
     * @param cause   异常原因
     */
    public TccException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造一个TCC事务异常
     *
     * @param transactionId 事务ID
     * @param phase         事务阶段
     * @param message       异常信息
     */
    public TccException(String transactionId, String phase, String message) {
        super(message);
        this.transactionId = transactionId;
        this.phase = phase;
    }

    /**
     * 构造一个TCC事务异常
     *
     * @param transactionId 事务ID
     * @param phase         事务阶段
     * @param message       异常信息
     * @param cause         异常原因
     */
    public TccException(String transactionId, String phase, String message, Throwable cause) {
        super(message, cause);
        this.transactionId = transactionId;
        this.phase = phase;
    }

    /**
     * 获取事务ID
     *
     * @return 事务ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * 设置事务ID
     *
     * @param transactionId 事务ID
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * 获取事务阶段
     *
     * @return 事务阶段
     */
    public String getPhase() {
        return phase;
    }

    /**
     * 设置事务阶段
     *
     * @param phase 事务阶段
     */
    public void setPhase(String phase) {
        this.phase = phase;
    }
} 