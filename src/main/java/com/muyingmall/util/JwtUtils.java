package com.muyingmall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.security.Key;

/**
 * JWT工具类
 */
@Component
public class JwtUtils {

    @Value("${jwt.expiration:86400}")
    private Long expiration;

    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

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
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
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
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}