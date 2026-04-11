package com.muyingmall.service;

import com.muyingmall.dto.ProductAccessVerifyRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 全站访问防护服务接口。
 * 负责对普通页面请求做频率评估，并在需要时生成验证上下文、校验人机结果、
 * 返回前端渲染 Turnstile 所需的站点密钥。
 */
public interface GlobalAccessGuardService {

    /**
     * 评估当前资源访问是否需要触发全站人机验证。
     */
    Map<String, Object> evaluateAccess(String resourceKey, HttpServletRequest request);

    /**
     * 校验前端提交的全站访问验证结果，并写入短时放行标记。
     */
    boolean verifyAccess(ProductAccessVerifyRequest request, HttpServletRequest httpServletRequest);

    /**
     * 判断当前请求是否需要参与全站访问频率检查。
     */
    boolean shouldCheck(HttpServletRequest request);

    /**
     * 根据请求路径与查询参数构建资源标识，作为频控统计维度。
     */
    String buildResourceKey(HttpServletRequest request);

    /**
     * 获取前端渲染 Cloudflare Turnstile 所需的站点公钥。
     */
    String getTurnstileSiteKey();
}
