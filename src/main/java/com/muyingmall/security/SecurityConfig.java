package com.muyingmall.security;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.Customizer;

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
                // http.authorizeHttpRequests(authorizeRequests ->
                // authorizeRequests.anyRequest().permitAll());
                // 配置请求授权
                http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                                // 允许预检请求
                                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                                // 允许访问 Knife4j 和 API 文档
                                .requestMatchers("/doc.html", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                                "/webjars/**",
                                                "/swagger-resources/**")
                                .permitAll()
                                // 允许访问用户注册和登录接口
                                .requestMatchers("/user/register", "/user/login").permitAll()
                                // 允许访问验证码/登录页相关接口（前台走 /api 前缀时也需要放行）
                                .requestMatchers(
                                                "/user/captcha",
                                                "/user/login/captcha",
                                                "/api/user/captcha",
                                                "/api/user/login/captcha",
                                                "/user/password/reset/**",
                                                "/api/user/password/reset/**")
                                .permitAll()
                                // 允许访问管理员登录接口
                                .requestMatchers("/admin/login", "/api/admin/login").permitAll()
                                // 允许访问商品、分类和品牌相关接口
                                .requestMatchers("/products/**", "/categories/**", "/brands/**").permitAll()
                                // 允许访问搜索相关接口
                                .requestMatchers("/search/**").permitAll()
                                // [P1 安全修复] 测试接口已禁用，生产环境不应暴露测试端点
                                // .requestMatchers("/test/connection", "/test/jwt-demo", "/test/**").permitAll()
                                // 允许访问WebSocket端点（WebSocket握手请求）
                                .requestMatchers("/ws/**").permitAll()
                                // 允许访问支付配置接口（获取启用的支付方式）
                                .requestMatchers("/payment/config/**").permitAll()
                                // 允许访问支付回调接口
                                .requestMatchers("/payment/alipay/notify", "/payment/wechat/notify").permitAll()
                                .requestMatchers("/payment/alipay/return").permitAll() // 支付宝同步回调通常也需放行
                                // [P0 安全修复] 以下钱包测试接口已禁用，生产环境严禁暴露未鉴权的资金操作端点
                                // .requestMatchers("/payment/wallet/manual-complete").permitAll()
                                // .requestMatchers("/payment/wallet/query-recharge").permitAll()
                                // .requestMatchers("/payment/wallet/query-account").permitAll()
                                // .requestMatchers("/payment/wallet/check-status").permitAll()
                                // .requestMatchers("/api/payment/wallet/manual-complete").permitAll()
                                // .requestMatchers("/api/payment/wallet/query-recharge").permitAll()
                                // .requestMatchers("/api/payment/wallet/query-account").permitAll()
                                // .requestMatchers("/api/payment/wallet/check-status").permitAll()
                                // .requestMatchers("/api/payment/wallet/create-test-recharge").permitAll()
                                // [P1 安全修复] 钱包通配符放行已禁用，避免覆盖 /user/wallet/** 的鉴权规则
                                // .requestMatchers("/api/payment/wallet/**").permitAll()
                                .requestMatchers("/api/payment/alipay/refund/notify").permitAll() // 支付宝退款异步通知
                                // 允许访问静态资源
                                .requestMatchers("/upload/**", "/static/**").permitAll()
                                // 允许访问公开可用的优惠券列表
                                .requestMatchers("/coupons/available").permitAll()
                                // 退款接口需要登录
                                .requestMatchers("/refund/**").authenticated()
                                // [P0 安全修复] 管理员退款接口已恢复鉴权，不再临时放行
                                // .requestMatchers("/admin/refund/**").permitAll()
                                // [P2 安全修复] 使用标准路径匹配替代Lambda，避免URL编码绕过风险
                                .requestMatchers("/admin/**", "/api/admin/**").hasAuthority("admin")
                                // 钱包相关接口需要登录（明确配置）
                                .requestMatchers("/user/wallet/**").authenticated()
                                // 用户相关接口需要登录
                                .requestMatchers("/user/**").authenticated()
                                // 订单相关接口需要登录
                                .requestMatchers("/order/**").authenticated()
                                .requestMatchers("/points/**").authenticated()
                                .requestMatchers("/coupons/{couponId}/receive").authenticated()
                                // 其他所有请求都需要认证
                                .anyRequest().authenticated());
                // 使用标准CORS配置方式
                http.cors(Customizer.withDefaults());

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