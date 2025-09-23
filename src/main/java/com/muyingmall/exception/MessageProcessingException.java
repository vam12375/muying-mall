package com.muyingmall.exception;

/**
 * 消息处理异常
 * 用于消息队列消息处理过程中的异常情况
 */
public class MessageProcessingException extends RuntimeException {

    public MessageProcessingException(String message) {
        super(message);
    }

    public MessageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageProcessingException(Throwable cause) {
        super(cause);
    }
}