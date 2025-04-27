package com.muyingmall.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private int code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}