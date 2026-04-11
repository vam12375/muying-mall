package com.muyingmall.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.common.api.Result;
import com.muyingmall.service.GlobalAccessGuardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 全站访问频率拦截器。
 * 对命中的 GET 访问请求进行频率评估，超过阈值时直接返回 429，
 * 由前端弹出全站访问验证窗口并引导用户完成人机校验。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GlobalAccessInterceptor implements HandlerInterceptor {

    /**
     * 全站访问防护服务，负责判断是否需要验证以及生成验证上下文。
     */
    private final GlobalAccessGuardService globalAccessGuardService;

    /**
     * JSON 序列化工具，用于将统一响应结果写回前端。
     */
    private final ObjectMapper objectMapper;

    /**
     * 在控制器执行前完成全站访问频率校验。
     * 命中频控后会直接中断请求，并返回包含 verifyKey、siteKey 等信息的 429 响应。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!globalAccessGuardService.shouldCheck(request)) {
            return true;
        }

        String resourceKey = globalAccessGuardService.buildResourceKey(request);
        Map<String, Object> accessCheck = globalAccessGuardService.evaluateAccess(resourceKey, request);
        if (!Boolean.TRUE.equals(accessCheck.get("needVerify"))) {
            return true;
        }

        // 将验证所需上下文透传给前端，便于前端弹窗后继续完成本次访问验证。
        Result<Map<String, Object>> result = Result.error(429, String.valueOf(accessCheck.get("message")));
        result.setData(accessCheck);
        result.setSuccess(false);

        response.setStatus(429);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(result));
        response.getWriter().flush();

        log.debug("全站访问频率触发验证: {}", resourceKey);
        return false;
    }
}
