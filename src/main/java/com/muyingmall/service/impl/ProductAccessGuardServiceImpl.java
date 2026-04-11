package com.muyingmall.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.dto.ProductAccessVerifyRequest;
import com.muyingmall.service.ProductAccessGuardService;
import com.muyingmall.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 商品详情页访问防护服务实现。
 * 按“商品ID + 客户端IP”维度统计详情页访问频率，超过阈值后要求用户完成人机验证，
 * 验证通过后为当前商品写入短时放行标记，降低恶意频繁刷新带来的压力。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductAccessGuardServiceImpl implements ProductAccessGuardService {

    private static final String ACCESS_COUNT_PREFIX = "product:access:count:";
    private static final String ACCESS_PASS_PREFIX = "product:access:pass:";
    private static final String VERIFY_META_PREFIX = "product:access:verify:";

    private static final long ACCESS_WINDOW_SECONDS = 60;
    private static final long ACCESS_PASS_SECONDS = 300;
    private static final long VERIFY_EXPIRE_SECONDS = 300;
    private static final int ACCESS_THRESHOLD = 8;
    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    /**
     * Redis 工具类，用于保存商品访问次数、验证元数据和放行状态。
     */
    private final RedisUtil redisUtil;

    /**
     * 调用 Cloudflare Turnstile 校验接口所需的 HTTP 客户端。
     */
    private final RestTemplate restTemplate;

    /**
     * JSON 解析工具，用于读取 siteverify 返回结果。
     */
    private final ObjectMapper objectMapper;

    /**
     * 前端渲染商品访问验证组件所需的站点公钥。
     */
    @Value("${cloudflare.turnstile.site-key:}")
    private String turnstileSiteKey;

    /**
     * 后端调用 Cloudflare siteverify 接口所需的密钥。
     */
    @Value("${cloudflare.turnstile.secret-key:}")
    private String turnstileSecretKey;

    /**
     * 评估当前商品详情访问是否需要人机验证。
     * 在同一时间窗口内访问次数超过阈值时，返回 verifyKey 和 siteKey 给前端弹窗使用。
     */
    @Override
    public Map<String, Object> evaluateAccess(Integer productId, HttpServletRequest request) {
        String clientKey = buildClientKey(request, productId);
        String passKey = ACCESS_PASS_PREFIX + clientKey;

        if (redisUtil.hasKey(passKey)) {
            return buildPassResult(false, null, null);
        }

        String countKey = ACCESS_COUNT_PREFIX + clientKey;
        long accessCount = redisUtil.incr(countKey, 1);
        if (accessCount == 1) {
            redisUtil.expire(countKey, ACCESS_WINDOW_SECONDS);
        }

        if (accessCount < ACCESS_THRESHOLD) {
            return buildPassResult(false, null, null);
        }

        String verifyKey = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> meta = new HashMap<>();
        meta.put("productId", String.valueOf(productId));
        meta.put("clientKey", clientKey);
        redisUtil.hmset(VERIFY_META_PREFIX + verifyKey, meta, VERIFY_EXPIRE_SECONDS);

        Map<String, Object> result = buildPassResult(true, verifyKey, "页面刷新过于频繁，请先完成人机验证");
        result.put("threshold", ACCESS_THRESHOLD);
        result.put("windowSeconds", ACCESS_WINDOW_SECONDS);
        result.put("siteKey", getTurnstileSiteKey());
        return result;
    }

    /**
     * 校验商品访问验证结果。
     * 只有商品ID、客户端标识与 Turnstile 校验都通过时，才会写入该商品的短时放行标记。
     */
    @Override
    public boolean verifyAccess(Integer productId, ProductAccessVerifyRequest request,
            HttpServletRequest httpServletRequest) {
        if (request == null || !StringUtils.hasText(request.getVerifyKey())
                || !StringUtils.hasText(request.getTurnstileToken())) {
            return false;
        }

        String verifyKey = request.getVerifyKey();
        String metaKey = VERIFY_META_PREFIX + verifyKey;
        if (!redisUtil.hasKey(metaKey)) {
            return false;
        }

        Map<Object, Object> meta = redisUtil.hmget(metaKey);
        if (meta == null || meta.isEmpty()) {
            return false;
        }

        String savedProductId = String.valueOf(meta.get("productId"));
        String savedClientKey = String.valueOf(meta.get("clientKey"));
        String currentClientKey = buildClientKey(httpServletRequest, productId);

        if (!String.valueOf(productId).equals(savedProductId) || !currentClientKey.equals(savedClientKey)) {
            return false;
        }

        boolean matched = verifyTurnstileToken(request.getTurnstileToken(), resolveClientIp(httpServletRequest));
        if (!matched) {
            return false;
        }

        redisUtil.set(ACCESS_PASS_PREFIX + currentClientKey, "1", ACCESS_PASS_SECONDS);
        redisUtil.del(metaKey, ACCESS_COUNT_PREFIX + currentClientKey);
        return true;
    }

    /**
     * 返回商品详情访问验证弹窗所需的 Turnstile 站点公钥。
     */
    @Override
    public String getTurnstileSiteKey() {
        return turnstileSiteKey;
    }

    /**
     * 调用 Cloudflare 官方接口校验 Turnstile 令牌。
     */
    private boolean verifyTurnstileToken(String token, String remoteIp) {
        if (!StringUtils.hasText(turnstileSecretKey)) {
            log.warn("Cloudflare Turnstile secret-key 未配置，无法校验人机验证");
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("secret", turnstileSecretKey);
            form.add("response", token);
            if (StringUtils.hasText(remoteIp)) {
                form.add("remoteip", remoteIp);
            }

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(TURNSTILE_VERIFY_URL, requestEntity,
                    String.class);
            String body = response.getBody();
            if (!StringUtils.hasText(body)) {
                return false;
            }

            JsonNode root = objectMapper.readTree(body);
            boolean success = root.path("success").asBoolean(false);
            if (!success) {
                log.warn("Turnstile 校验失败: {}", body);
            }
            return success;
        } catch (Exception e) {
            log.error("Turnstile 校验异常", e);
            return false;
        }
    }

    /**
     * 统一封装商品访问验证响应数据。
     */
    private Map<String, Object> buildPassResult(boolean needVerify, String verifyKey, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("needVerify", needVerify);
        result.put("verifyKey", verifyKey);
        result.put("message", message);
        result.put("passDurationSeconds", ACCESS_PASS_SECONDS);
        return result;
    }

    /**
     * 生成“商品ID + 客户端IP”组合键，作为商品详情访问频控的统计维度。
     */
    private String buildClientKey(HttpServletRequest request, Integer productId) {
        return productId + ":" + resolveClientIp(request);
    }

    /**
     * 解析客户端真实 IP，优先读取反向代理与 CDN 转发头。
     */
    private String resolveClientIp(HttpServletRequest request) {
        String[] headerNames = { "CF-Connecting-IP", "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP" };
        for (String header : headerNames) {
            String value = request.getHeader(header);
            if (StringUtils.hasText(value) && !"unknown".equalsIgnoreCase(value)) {
                if (value.contains(",")) {
                    return value.split(",")[0].trim();
                }
                return value.trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        if (!StringUtils.hasText(remoteAddr)) {
            return "unknown";
        }
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "127.0.0.1".equals(remoteAddr)) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                return "127.0.0.1";
            }
        }
        return remoteAddr;
    }
}
