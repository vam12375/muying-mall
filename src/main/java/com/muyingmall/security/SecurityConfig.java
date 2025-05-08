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
    private final CorsFilter corsFilter; // 注入CorsFilter

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 BCrypt 进行密码加密
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 配置请求授权
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                // 允许预检请求
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                // 允许访问 Swagger UI 和 API 文档
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 允许访问用户注册和登录接口
                .requestMatchers("/user/register", "/user/login").permitAll()
                // 允许访问管理员登录接口
                .requestMatchers("/admin/login").permitAll()
                // 允许访问商品和分类相关接口
                .requestMatchers("/products/**", "/categories/**").permitAll()
                // 允许访问测试连接接口
                .requestMatchers("/test/connection", "/test/jwt-demo", "/test/**").permitAll()
                // 允许访问支付回调接口
                .requestMatchers("/payment/alipay/notify", "/payment/wechat/notify").permitAll()
                .requestMatchers("/payment/alipay/return").permitAll() // 支付宝同步回调通常也需放行
                // 允许访问静态资源
                .requestMatchers("/upload/**", "/static/**").permitAll()
                // 允许访问公开可用的优惠券列表
                .requestMatchers("/coupons/available").permitAll()
                // 管理员接口需要admin权限（排除已经允许的登录接口）
                .requestMatchers(request -> request.getRequestURI().startsWith("/api/admin/") &&
                        !request.getRequestURI().equals("/api/admin/login"))
                .hasAuthority("admin")
                // 用户相关接口需要登录
                .requestMatchers("/user/**").authenticated()
                .requestMatchers("/order/**").authenticated()
                .requestMatchers("/points/**").authenticated()
                .requestMatchers("/coupons/{couponId}/receive").authenticated()
                // 其他所有请求都需要认证
                .anyRequest().authenticated());

        // 启用CORS
        http.cors(cors -> cors.configure(http));

        // 禁用CSRF保护
        http.csrf(AbstractHttpConfigurer::disable);

        // 设置会话管理为无状态
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 禁用表单登录
        http.formLogin(AbstractHttpConfigurer::disable);

        // 禁用HTTP基本认证
        http.httpBasic(AbstractHttpConfigurer::disable);

        // 添加过滤器 - 分开单独添加以避免链式调用可能的问题
        http.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
