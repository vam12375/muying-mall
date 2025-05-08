package com.muyingmall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.security.Key;

/**
 * JWT工具类
 */
@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret:ThisIsAReallyLongAndSecureSecretKeyForHS512AlgorithmSoItShouldWorkFineNowPleaseEnsureItIsReallySecureAndRandomEnough}")
    private String secretString;

    @Value("${jwt.expiration:86400}")
    private Long expiration;

    private Key signingKey;

    @PostConstruct
    public void init() {
        log.info("Initializing JwtUtils...");
        log.info("JWT Secret from config (first 10 chars): {}",
                (secretString != null && secretString.length() > 10) ? secretString.substring(0, 10) + "..."
                        : secretString);
        log.info("JWT Secret length from config: {}", secretString != null ? secretString.length() : "null");

        if (secretString == null || secretString.isEmpty()
                || "DefaultSecretKeyMustBeConfiguredAndLongEnoughForHS512IfNotBase64Encoded".equals(secretString)) {
            if ("DefaultSecretKeyMustBeConfiguredAndLongEnoughForHS512IfNotBase64Encoded".equals(secretString)) {
                log.warn(
                        "JWT Secret is using a known placeholder value: '{}'. This is NOT secure for production! Please set a unique, strong secret in application.yml.",
                        secretString);
                this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
                log.warn(
                        "Generated a dynamic JWT signing key because a placeholder secret was detected. Tokens will invalidate on app restart.");
            } else if (secretString == null || secretString.isEmpty()) {
                log.error("JWT Secret is missing or empty in configuration!");
                this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
                log.warn(
                        "Generated a dynamic JWT signing key due to missing secret. Tokens will invalidate on app restart.");
            } else {
                log.info("Using the configured long example secret key. Ensure this is changed for production.");
                byte[] keyBytes = secretString.getBytes(StandardCharsets.UTF_8);
                if (keyBytes.length < 64) {
                    log.warn(
                            "Configured JWT Secret is shorter than 64 bytes ({} bytes), which is recommended for HS512. Consider a longer secret.",
                            keyBytes.length);
                }
                this.signingKey = Keys.hmacShaKeyFor(keyBytes);
                log.info("Successfully initialized JWT signing key from configured (long example) secret string.");
            }
        } else {
            byte[] keyBytes = secretString.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 64) {
                log.warn(
                        "Configured JWT Secret is shorter than 64 bytes ({} bytes), which is recommended for HS512. Consider a longer secret.",
                        keyBytes.length);
            }
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
            log.info("Successfully initialized JWT signing key from configured custom secret string.");
        }
        // if (this.signingKey != null) {
        // log.info("Initialized signingKey (Base64): {}",
        // Base64.getEncoder().encodeToString(this.signingKey.getEncoded()));
        // } else {
        // log.error("SigningKey is NULL after init!");
        // }
    }

    /**
     * 生成令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     * @return JWT令牌
     */
    public String generateToken(Integer userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        return createToken(claims);
    }

    /**
     * 创建令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createToken(Map<String, Object> claims) {
        if (this.signingKey == null) {
            log.error("Signing key is not initialized! JWT cannot be created.");
            // log.error("Current secretString at createToken when signingKey is null: {}",
            // secretString); // Keep this if it was part of the original fix for a null key
            throw new IllegalStateException("JWT signing key is not initialized. Check configuration and logs.");
        }
        // log.info("createToken using signingKey (Base64): {}",
        // Base64.getEncoder().encodeToString(this.signingKey.getEncoded()));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(this.signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    public Claims getClaimsFromToken(String token) {
        if (this.signingKey == null) {
            log.error("Signing key is not initialized! JWT cannot be parsed.");
            // log.error("Current secretString at getClaimsFromToken when signingKey is
            // null: {}", secretString); // Keep this if it was part of the original fix for
            // a null key
            throw new IllegalStateException("JWT signing key is not initialized. Check configuration and logs.");
        }
        // log.info("getClaimsFromToken using signingKey (Base64): {}",
        // Base64.getEncoder().encodeToString(this.signingKey.getEncoded()));
        return Jwts.parser()
                .setSigningKey(this.signingKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        if (this.signingKey == null) {
            log.warn("Signing key is not initialized. Token validation will always fail.");
            return false;
        }
        try {
            // Claims claims = getClaimsFromToken(token); // getClaimsFromToken already logs
            // if needed
            // For cleaner code, call the parsing directly here if not reusing the claims
            // object elsewhere in this method
            Claims claims = Jwts.parser().setSigningKey(this.signingKey).parseClaimsJws(token).getBody();
            boolean tokenExpired = claims.getExpiration().before(new Date());
            if (tokenExpired) {
                String subject = claims.getSubject();
                if (subject == null)
                    subject = claims.get("username", String.class);
                log.warn("Token for user '{}' has expired at {}. Current time: {}.", subject, claims.getExpiration(),
                        new Date());
            }
            return !tokenExpired;
        } catch (io.jsonwebtoken.SignatureException se) {
            log.warn(
                    "JWT signature validation failed: {}. This usually means the signing key is incorrect or the token was tampered with.",
                    se.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Token validation failed for other reasons: {}", e.getMessage());
            return false;
        }
    }

    // Removed temporary getter for debugging
    // public String getSigningKeyBase64ForDebug() {
    // if (this.signingKey != null) {
    // return Base64.getEncoder().encodeToString(this.signingKey.getEncoded());
    // }
    // return "SigningKey is NULL in JwtUtils for debug";
    // }
}