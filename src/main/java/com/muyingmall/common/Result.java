package com.muyingmall.common;

import lombok.Data;

/**
 * 通用返回结果
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
     * 成功返回结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功返回结果
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
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