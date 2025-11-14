package com.muyingmall.common.exception;

import com.muyingmall.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
        return Result.error(400, message == null ? "参数校验失败" : message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
        return Result.error(400, message == null ? "参数绑定失败" : message);
    }

    /**
     * 处理 Spring Boot 3.x 的 NoResourceFoundException
     * 当请求的路径不是静态资源也不是 API 接口时抛出
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public Result<Void> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException e) {
        log.error("找不到资源: {}", e.getMessage());
        log.error("请求路径: {}", e.getResourcePath());
        // 这个异常通常表示路径配置有问题，返回 404
        return Result.error(404, "请求的资源不存在: " + e.getResourcePath());
    }

    /**
     * 处理未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 打印完整的异常堆栈，便于调试
        log.error("系统异常: {}", e.getMessage(), e);
        log.error("异常类型: {}", e.getClass().getName());
        log.error("异常堆栈: ", e);
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}