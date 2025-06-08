package com.muyingmall.security;

import com.muyingmall.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证过滤器
 */
@Component
@RequiredArgsConstructor
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtils.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtUtils.getClaimsFromToken(token);
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);
        Integer userId = null;

        try {
            userId = claims.get("userId", Integer.class);
            System.out.println("JwtAuthenticationFilter: 从token中提取到userId: " + userId);
        } catch (Exception e) {
            System.out.println("JwtAuthenticationFilter: 无法从token提取userId: " + e.getMessage());
        }

        if (username != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Map<String, Object> details = new HashMap<>();
            if (userId != null) {
                details.put("userId", userId);
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.singletonList(new SimpleGrantedAuthority(role)));

            WebAuthenticationDetailsSource detailsSource = new WebAuthenticationDetailsSource();
            authentication.setDetails(details);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("JwtAuthenticationFilter: 已设置认证信息，username=" + username +
                    ", role=" + role + ", userId=" + userId);
        }

        filterChain.doFilter(request, response);
    }
}