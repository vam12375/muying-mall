package com.muyingmall.common;

import lombok.Data;

/**
 * API 响应包装类
 *
 * @param <T> 响应数据类型
 */
@Data
public class ApiResponse<T> {

    /**
     * 响应码
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
     * 创建成功响应
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return of(200, "操作成功", data, true);
    }

    /**
     * 创建成功响应
     *
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return of(200, message, data, true);
    }

    /**
     * 创建失败响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return of(code, message, null, false);
    }

    /**
     * 创建失败响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return of(500, message, null, false);
    }

    /**
     * 创建响应
     *
     * @param code    响应码
     * @param message 响应消息
     * @param data    响应数据
     * @param success 是否成功
     * @param <T>     数据类型
     * @return API响应
     */
    private static <T> ApiResponse<T> of(int code, String message, T data, boolean success) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        response.setSuccess(success);
        return response;
    }
}