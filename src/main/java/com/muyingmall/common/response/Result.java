package com.muyingmall.common.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 */
@Data
public class Result<T> implements Serializable {

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
     * 成功返回结果
     *
     * @param <T> 泛型参数
     * @return 成功的响应结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功返回结果
     *
     * @param data 返回的数据
     * @param <T>  泛型参数
     * @return 成功的响应结果
     */
    public static <T> Result<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功返回结果
     *
     * @param data    返回的数据
     * @param message 提示信息
     * @param <T>     泛型参数
     * @return 成功的响应结果
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setData(data);
        result.setMessage(message);
        result.setSuccess(true);
        return result;
    }

    /**
     * 失败返回结果
     *
     * @param <T> 泛型参数
     * @return 失败的响应结果
     */
    public static <T> Result<T> error() {
        return error("操作失败");
    }

    /**
     * 失败返回结果
     *
     * @param message 提示信息
     * @param <T>     泛型参数
     * @return 失败的响应结果
     */
    public static <T> Result<T> error(String message) {
        return error(400, message);
    }

    /**
     * 失败返回结果
     *
     * @param code    错误码
     * @param message 提示信息
     * @param <T>     泛型参数
     * @return 失败的响应结果
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }
}