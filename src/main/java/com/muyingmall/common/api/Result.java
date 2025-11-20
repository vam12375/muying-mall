package com.muyingmall.common.api;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果类
 * 整合了原 common.Result, common.response.Result, common.result.Result 的功能
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 时间戳
     */
    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(int code, String message, T data, boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回结果
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null, true);
    }

    /**
     * 成功返回结果
     *
     * @param data 返回的数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, true);
    }

    /**
     * 成功返回结果
     *
     * @param data    返回的数据
     * @param message 提示信息
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(200, message, data, true);
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败", null, false);
    }

    /**
     * 失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null, false);
    }

    /**
     * 失败返回结果
     *
     * @param code    错误码
     * @param message 提示信息
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null, false);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> Result<T> validateFailed(String message) {
        return new Result<>(400, message, null, false);
    }

    /**
     * 未登录返回结果
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null, false);
    }

    /**
     * 未授权返回结果
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null, false);
    }

    /**
     * 失败返回结果（failure方法的别名，兼容旧代码）
     */
    public static <T> Result<T> failure(int code, String message) {
        return error(code, message);
    }

    /**
     * 失败返回结果（failure方法的别名，兼容旧代码）
     */
    public static <T> Result<T> failure(String message) {
        return error(message);
    }
}
