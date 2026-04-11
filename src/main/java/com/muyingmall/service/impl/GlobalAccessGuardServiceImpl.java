package com.muyingmall.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.dto.ProductAccessVerifyRequest;
import com.muyingmall.service.GlobalAccessGuardService;
import com.muyingmall.util.IpUtil;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 全站访问防护服务实现。
 * 通过“资源路径 + 客户端IP”维度统计访问频率，超过阈值时要求用户完成人机验证，
 * 验证通过后为该资源写入短时放行标记，避免同一页面反复弹出验证。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalAccessGuardServiceImpl implements GlobalAccessGuardService {

    private static final String ACCESS_COUNT_PREFIX = "global:access:count:";
    private static final String ACCESS_PASS_PREFIX = "global:access:pass:";
    private static final String VERIFY_META_PREFIX = "global:access:verify:";
    private static final long ACCESS_WINDOW_SECONDS = 60;
    private static final long ACCESS_PASS_SECONDS = 300;
    private static final long VERIFY_EXPIRE_SECONDS = 300;
    private static final int ACCESS_THRESHOLD = 8;
    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private static final List<String> EXCLUDE_PATTERNS = List.of(
            "/access/verify",
            "/products/*/access/verify",
            "/user/login",
            "/user/register",
            "/user/password/reset/**",
            "/admin/**",
            "/api/admin/**",
            "/upload/**",
            "/uploads/**",
            "/avatars/**",
            "/static/**",
            "/images/**",
            "/comment/**",
            "/ws/**",
            "/doc.html",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/swagger-resources/**");

    /**
     * Redis 工具类，用于记录访问次数、验证上下文以及短时放行状态。
     */
    private final RedisUtil redisUtil;

    /**
     * 用于向 Cloudflare 发起 Turnstile 校验请求。
     */
    private final RestTemplate restTemplate;

    /**
     * JSON 解析工具，用于读取 siteverify 返回结果。
     */
    private final ObjectMapper objectMapper;

    /**
     * 路径匹配器，用于判断哪些接口需要跳过全站访问频率检查。
     */
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 前端渲染 Turnstile 组件所需的站点公钥。
     */
    @Value("${cloudflare.turnstile.site-key:}")
    private String turnstileSiteKey;

    /**
     * 后端调用 Cloudflare siteverify 接口时使用的密钥。
     */
    @Value("${cloudflare.turnstile.secret-key:}")
    private String turnstileSecretKey;

    /**
     * 评估当前资源是否需要触发全站访问验证。
     * 若访问次数未超阈值则直接放行；若已超阈值则生成 verifyKey 并返回给前端。
     */
    @Override
    public Map<String, Object> evaluateAccess(String resourceKey, HttpServletRequest request) {
        String clientIp = IpUtil.getIpAddr(request);
        String clientKey = resourceKey + ":" + clientIp;
        String passKey = ACCESS_PASS_PREFIX + clientKey;

        if (redisUtil.hasKey(passKey)) {
            return buildResult(false, null, null, resourceKey);
        }

        String countKey = ACCESS_COUNT_PREFIX + clientKey;
        long accessCount = redisUtil.incr(countKey, 1);
        if (accessCount == 1) {
            redisUtil.expire(countKey, ACCESS_WINDOW_SECONDS);
        }

        if (accessCount < ACCESS_THRESHOLD) {
            return buildResult(false, null, null, resourceKey);
        }

        String verifyKey = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> meta = new HashMap<>();
        meta.put("resourceKey", resourceKey);
        meta.put("clientIp", clientIp);
        redisUtil.hmset(VERIFY_META_PREFIX + verifyKey, meta, VERIFY_EXPIRE_SECONDS);

        Map<String, Object> result = buildResult(true, verifyKey, "页面访问过于频繁，请先完成人机验证", resourceKey);
        result.put("threshold", ACCESS_THRESHOLD);
        result.put("windowSeconds", ACCESS_WINDOW_SECONDS);
        result.put("siteKey", getTurnstileSiteKey());
        return result;
    }

    /**
     * 校验前端提交的全站访问验证结果。
     * 只有 verifyKey、客户端 IP 与 Turnstile 校验全部匹配时，才会写入短时放行标记。
     */
    @Override
    public boolean verifyAccess(ProductAccessVerifyRequest request, HttpServletRequest httpServletRequest) {
        if (request == null || !StringUtils.hasText(request.getVerifyKey())
                || !StringUtils.hasText(request.getTurnstileToken())) {
            return false;
        }

        String metaKey = VERIFY_META_PREFIX + request.getVerifyKey();
        if (!redisUtil.hasKey(metaKey)) {
            return false;
        }

        Map<Object, Object> meta = redisUtil.hmget(metaKey);
        if (meta == null || meta.isEmpty()) {
            return false;
        }

        String savedResourceKey = meta.get("resourceKey") == null ? "" : String.valueOf(meta.get("resourceKey"));
        String savedClientIp = meta.get("clientIp") == null ? "" : String.valueOf(meta.get("clientIp"));
        String currentClientIp = IpUtil.getIpAddr(httpServletRequest);

        if (!StringUtils.hasText(savedResourceKey) || !StringUtils.hasText(savedClientIp)) {
            log.warn("全站访问验证元数据缺失: verifyKey={}", request.getVerifyKey());
            return false;
        }

        if (!savedClientIp.equals(currentClientIp)) {
            log.warn("全站访问验证客户端IP不匹配: verifyKey={}, savedClientIp={}, currentClientIp={}",
                    request.getVerifyKey(), savedClientIp, currentClientIp);
            return false;
        }

        boolean matched = verifyTurnstileToken(request.getTurnstileToken(), currentClientIp);
        if (!matched) {
            return false;
        }

        String clientKey = savedResourceKey + ":" + currentClientIp;
        redisUtil.set(ACCESS_PASS_PREFIX + clientKey, "1", ACCESS_PASS_SECONDS);
        redisUtil.del(metaKey, ACCESS_COUNT_PREFIX + clientKey);
        return true;
    }

    /**
     * 判断当前请求是否需要进入全站访问频率检查。
     * 仅拦截普通 GET 页面请求，登录、上传、静态资源、文档等路径会被排除。
     */
    @Override
    public boolean shouldCheck(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String path = request.getRequestURI();
        if (!StringUtils.hasText(path)) {
            return false;
        }

        for (String pattern : EXCLUDE_PATTERNS) {
            if (antPathMatcher.match(pattern, path)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 组合请求路径与查询字符串，生成当前资源的频控标识。
     */
    @Override
    public String buildResourceKey(HttpServletRequest request) {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        if (StringUtils.hasText(query)) {
            return path + "?" + query;
        }
        return path;
    }

    /**
     * 返回前端渲染全站访问验证弹窗所需的站点公钥。
     */
    @Override
    public String getTurnstileSiteKey() {
        return turnstileSiteKey;
    }

    /**
     * 调用 Cloudflare 官方 siteverify 接口校验 Turnstile 令牌。
     * 当远端 IP 可用时一并提交，提升校验安全性与准确性。
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
                log.warn("全站 Turnstile 校验失败: {}", body);
            }
            return success;
        } catch (Exception e) {
            log.error("全站 Turnstile 校验异常", e);
            return false;
        }
    }

    /**
     * 统一封装全站访问验证响应数据，供拦截器和控制器返回给前端。
     */
    private Map<String, Object> buildResult(boolean needVerify, String verifyKey, String message, String resourceKey) {
        Map<String, Object> result = new HashMap<>();
        result.put("needVerify", needVerify);
        result.put("verifyKey", verifyKey);
        result.put("message", message);
        result.put("resourceKey", resourceKey);
        result.put("passDurationSeconds", ACCESS_PASS_SECONDS);
        return result;
    }
}

/*
 * 修改说明：
 * 1. 全站访问验证改为使用 verifyKey 对应的原始资源元数据，不再错误地拿 /access/verify 当前请求路径参与比对。
 * 2. 验证成功后为原始受限资源写入放行标记，确保后续重试请求能够正常放行。
 */
