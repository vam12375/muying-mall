package com.muyingmall.security;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsUtils;

import org.springframework.web.filter.CorsFilter;

import lombok.RequiredArgsConstructor;

@Configuration

@EnableWebSecurity

@RequiredArgsConstructor

public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CorsFilter corsFilter; // 注入CorsFilter*

    @Bean

    public PasswordEncoder passwordEncoder() {

    // 使用 BCrypt 进行密码加密*

    return new BCryptPasswordEncoder();

    }

    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    // ⚠️ 测试模式：允许所有请求通过（生产环境请勿使用）*

    http.authorizeHttpRequests(authorizeRequests -> authorizeRequests.anyRequest().permitAll());

    // 启用CORS*

    http.cors(cors -> cors.configure(http));

    // 禁用CSRF保护*

    http.csrf(AbstractHttpConfigurer::disable);

    // 设置会话管理为无状态*

    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // 禁用表单登录*

    http.formLogin(AbstractHttpConfigurer::disable);

    // 禁用HTTP基本认证*

    http.httpBasic(AbstractHttpConfigurer::disable);

    // 添加过滤器 - 分开单独添加以避免链式调用可能的问题*

    http.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class);

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();

    }

}