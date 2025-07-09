package com.muyingmall.aspect;

import com.muyingmall.annotation.AdminOperationLog;
import com.muyingmall.entity.AdminOperationLog.OperationResult;
import com.muyingmall.entity.AdminOperationLog.OperationType;
import com.muyingmall.entity.User;
import com.muyingmall.service.AdminOperationLogService;
import com.muyingmall.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 管理员操作日志AOP切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminOperationLogAspect {

    private final AdminOperationLogService operationLogService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 定义切点：所有带有@AdminOperationLog注解的方法
     */
    @Pointcut("@annotation(com.muyingmall.annotation.AdminOperationLog)")
    public void operationLogPointcut() {
    }

    /**
     * 环绕通知：记录操作日志
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 记录操作日志
            recordOperationLog(joinPoint, result, exception, startTime);
        }
    }

    /**
     * 记录操作日志
     */
    private void recordOperationLog(ProceedingJoinPoint joinPoint, Object result, 
                                   Exception exception, long startTime) {
        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            AdminOperationLog annotation = method.getAnnotation(AdminOperationLog.class);

            if (annotation == null) {
                return;
            }

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            // 获取当前管理员信息
            User currentAdmin = getCurrentAdmin(request);
            if (currentAdmin == null) {
                log.warn("无法获取当前管理员信息，跳过操作日志记录");
                return;
            }

            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;

            // 确定操作结果
            String operationResult = exception == null ? 
                OperationResult.SUCCESS.getCode() : OperationResult.FAILED.getCode();

            // 获取错误信息
            String errorMessage = exception != null ? exception.getMessage() : null;

            // 获取响应状态码
            Integer responseStatus = exception == null ? 200 : 500;

            // 解析操作类型
            String operationType = parseOperationType(annotation.operationType(), request.getMethod());

            // 解析目标ID
            String targetId = parseTargetId(joinPoint.getArgs());

            // 记录操作日志
            operationLogService.recordOperation(
                currentAdmin.getUserId(),
                currentAdmin.getUsername(),
                annotation.operation(),
                annotation.module(),
                operationType,
                annotation.targetType(),
                targetId,
                request,
                responseStatus,
                operationResult,
                errorMessage,
                executionTime,
                annotation.description()
            );

        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取当前管理员信息
     */
    private User getCurrentAdmin(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtTokenUtil.getUsernameFromToken(token);
                if (StringUtils.hasText(username)) {
                    // 这里应该从数据库查询用户信息，暂时创建一个简单的用户对象
                    User user = new User();
                    user.setUsername(username);
                    // 从token中解析用户ID（需要在JWT中包含用户ID）
                    // 或者从数据库查询
                    user.setUserId(1); // 临时设置，实际应该从token或数据库获取
                    return user;
                }
            }
        } catch (Exception e) {
            log.warn("获取当前管理员信息失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 解析操作类型
     */
    private String parseOperationType(String annotationOperationType, String httpMethod) {
        if (StringUtils.hasText(annotationOperationType)) {
            return annotationOperationType;
        }

        // 根据HTTP方法推断操作类型
        switch (httpMethod.toUpperCase()) {
            case "GET":
                return OperationType.READ.getCode();
            case "POST":
                return OperationType.CREATE.getCode();
            case "PUT":
            case "PATCH":
                return OperationType.UPDATE.getCode();
            case "DELETE":
                return OperationType.DELETE.getCode();
            default:
                return OperationType.READ.getCode();
        }
    }

    /**
     * 解析目标ID
     */
    private String parseTargetId(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        // 尝试从参数中找到ID
        for (Object arg : args) {
            if (arg instanceof Integer || arg instanceof Long) {
                return arg.toString();
            }
            if (arg instanceof String && ((String) arg).matches("\\d+")) {
                return (String) arg;
            }
        }

        return null;
    }
}
