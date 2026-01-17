package com.muyingmall.filter;

import com.muyingmall.entity.User;
import com.muyingmall.service.UserService;
import com.muyingmall.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 处理基于JWT的用户认证，并自动将JWT中的用户信息同步到HttpSession
 * 自动防止会话过期，同时减少数据库查询负担
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    // 会话同步防抖时间(毫秒)
    private static final long SESSION_SYNC_DEBOUNCE_MS = 60 * 1000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // log.debug("[JwtFilter] Processing request for: {}", request.getRequestURI());
        // log.debug("[JwtFilter] Injected JwtUtils instance hash: {}",
        // System.identityHashCode(jwtUtils));
        // log.debug("[JwtFilter] JwtUtils signingKey for token validation (Base64): {}",
        // jwtUtils.getSigningKeyBase64ForDebug());

        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");

        // 获取当前会话
        HttpSession session = request.getSession(false);

        // 如果请求包含有效的JWT，则尝试自动同步到会话
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // log.debug("[JwtFilter] Extracted Bearer token: {}...", (token.length() > 10)
            // ? token.substring(0,10) : token);

            try {
                // 验证token
                if (jwtUtils.validateToken(token)) {
                    // log.debug("[JwtFilter] Token validated successfully.");
                    // 从token中提取Claims
                    Claims claims = jwtUtils.getClaimsFromToken(token);
                    String username = claims.get("username", String.class);
                    // log.debug("[JwtFilter] Username from token: {}", username);

                    // 检查会话是否需要同步
                    boolean needsSync = true;

                    // 如果当前已有会话，检查是否需要更新
                    if (session != null) {
                        // 获取当前会话中的用户
                        User sessionUser = (User) session.getAttribute("user");

                        // 如果会话中已有相同用户，并且最近已同步过，不再同步
                        if (sessionUser != null && sessionUser.getUsername().equals(username)) {
                            // 获取上次会话更新时间
                            Long lastSync = (Long) session.getAttribute("lastSessionSync");
                            long currentTime = System.currentTimeMillis();

                            // 如果上次同步时间存在，并且距今不超过防抖时间，则不再同步
                            if (lastSync != null && (currentTime - lastSync) < SESSION_SYNC_DEBOUNCE_MS) {
                                needsSync = false;
                                // log.debug("[JwtFilter] Session sync debounced for user: {}", username);
                            }
                        }
                    } else {
                        // 如果没有现有会话，创建一个新会话
                        session = request.getSession(true);
                        // log.debug("[JwtFilter] Created new session.");
                    }

                    // 如果需要同步用户信息
                    if (needsSync) {
                        // log.debug("[JwtFilter] Syncing user {} to session.", username);
                        // 创建或更新会话
                        syncUserToSession(session, username);
                    }
                } else {
                    // log.warn("[JwtFilter] Token validation failed for token: {}...",
                    // (token.length() > 10) ? token.substring(0,10) : token); // 这条日志可能很有用
                }
            } catch (Exception e) {
                // 记录错误，但允许请求继续
                log.error("[JwtFilter] Error processing JWT: {}", e.getMessage(), e); // 保留此错误日志
            }
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 将用户信息同步到会话
     */
    private void syncUserToSession(HttpSession session, String username) {
        try {
            // 获取现有会话中的用户
            User existingUser = (User) session.getAttribute("user");

            // 如果会话已包含相同用户，仅更新同步时间戳
            if (existingUser != null && existingUser.getUsername().equals(username)) {
                session.setAttribute("lastSessionSync", System.currentTimeMillis());
                return;
            }

            // 否则从数据库获取用户信息
            User user = userService.findByUsername(username);
            if (user != null) {
                // 保存用户到会话
                session.setAttribute("user", user);
                // 更新同步时间戳
                session.setAttribute("lastSessionSync", System.currentTimeMillis());
                log.debug("User {} synced to session successfully.", username); // 这可能是一条有用的调试日志
            } else {
                log.warn("无法在数据库中找到用于会话同步的用户 '{}'。", username); // 保留此警告
            }
        } catch (Exception e) {
            log.error("Error syncing user to session: {}", e.getMessage(), e); // 保留此错误日志
        }
    }
}