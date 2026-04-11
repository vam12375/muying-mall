package com.muyingmall.service;

import com.muyingmall.dto.ProductAccessVerifyRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 商品详情页访问防护服务接口。
 * 负责按商品维度统计访问频率，并在访问过于频繁时要求用户完成人机验证，
 * 验证通过后再为当前商品写入短时放行标记。
 */
public interface ProductAccessGuardService {

    /**
     * 评估当前商品访问是否需要触发人机验证。
     */
    Map<String, Object> evaluateAccess(Integer productId, HttpServletRequest request);

    /**
     * 校验商品访问验证结果，并在通过后为当前商品访问放行。
     */
    boolean verifyAccess(Integer productId, ProductAccessVerifyRequest request, HttpServletRequest httpServletRequest);

    /**
     * 获取前端渲染商品访问验证组件所需的 Turnstile 站点公钥。
     */
    String getTurnstileSiteKey();
}
