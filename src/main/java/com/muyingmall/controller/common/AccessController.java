package com.muyingmall.controller.common;

import com.muyingmall.common.api.Result;
import com.muyingmall.dto.ProductAccessVerifyRequest;
import com.muyingmall.service.GlobalAccessGuardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 全站访问验证控制器。
 * 当前端被全站访问频率保护拦截后，通过该接口提交 Turnstile 校验结果，
 * 校验通过后为当前访客放行后续访问。
 */
@RestController
@RequestMapping("/access")
@RequiredArgsConstructor
@Tag(name = "全站访问验证")
public class AccessController {

    /**
     * 处理全站访问频率校验与放行状态写入。
     */
    private final GlobalAccessGuardService globalAccessGuardService;

    /**
     * 提交全站访问验证结果。
     * verifyKey 用于定位本次待验证会话，turnstileToken 用于向 Cloudflare 校验人机结果。
     */
    @PostMapping("/verify")
    @Operation(summary = "验证全站访问验证码")
    public Result<Map<String, Object>> verifyAccess(@RequestBody ProductAccessVerifyRequest request,
            HttpServletRequest httpServletRequest) {
        boolean passed = globalAccessGuardService.verifyAccess(request, httpServletRequest);
        if (!passed) {
            return Result.validateFailed("Cloudflare 人机验证失败或已过期");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("passed", true);
        return Result.success(result, "验证通过");
    }
}
