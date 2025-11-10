package com.muyingmall.filter;

import com.muyingmall.monitor.ApiStatisticsMonitor;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * API统计过滤器
 * 自动记录所有API调用的统计信息
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class ApiStatisticsFilter implements Filter {

    private final ApiStatisticsMonitor apiStatisticsMonitor;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        try {
            // 继续处理请求
            chain.doFilter(request, response);
        } finally {
            // 计算响应时间
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 获取请求信息
            String endpoint = httpRequest.getRequestURI();
            String method = httpRequest.getMethod();
            int statusCode = httpResponse.getStatus();
            
            // 记录API调用统计
            try {
                apiStatisticsMonitor.recordApiCall(endpoint, method, statusCode, responseTime);
            } catch (Exception e) {
                log.error("记录API统计失败", e);
            }
        }
    }
}
