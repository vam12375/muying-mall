package com.muyingmall.common;

import lombok.Data;

/**
 * 通用结果类
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> {
    /**
     * 状态码
     */
    private int code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 数据
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
     * 成功结果
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null, true);
    }

    /**
     * 成功结果
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, true);
    }

    /**
     * 成功结果
     *
     * @param message 提示信息
     * @param data    数据
     * @param <T>     数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data, true);
    }

    /**
     * 失败结果
     *
     * @param code    状态码
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 失败结果
     */
    public static <T> Result<T> failure(int code, String message) {
        return new Result<>(code, message, null, false);
    }

    /**
     * 失败结果
     *
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 失败结果
     */
    public static <T> Result<T> failure(String message) {
        return new Result<>(500, message, null, false);
    }

    /**
     * 错误结果 (failure方法的别名)
     *
     * @param code    状态码
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(int code, String message) {
        return failure(code, message);
    }

    /**
     * 错误结果 (failure方法的别名)
     *
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(String message) {
        return failure(message);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> Result<T> validateFailed(String message) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMessage(message);
        return result;
    }

    /**
     * 未登录返回结果
     */
    public static <T> Result<T> unauthorized(String message) {
        Result<T> result = new Result<>();
        result.setCode(401);
        result.setMessage(message);
        return result;
    }

    /**
     * 未授权返回结果
     */
    public static <T> Result<T> forbidden(String message) {
        Result<T> result = new Result<>();
        result.setCode(403);
        result.setMessage(message);
        return result;
    }
}