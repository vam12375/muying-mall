package com.muyingmall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * OpenAPI (Swagger) 配置类
 */
@Configuration
public class OpenAPIConfig {

        /**
         * OpenAPI 主配置
         */
        @Bean
        public OpenAPI muyingMallOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("母婴商城 API 文档")
                                                .description("""
                                                                # 母婴商城系统 RESTful API

                                                                ## 概述
                                                                母婴商城系统提供完整的电商功能，包括用户管理、商品管理、订单处理、支付集成等核心功能。

                                                                ## 特性
                                                                - 🔐 JWT 认证授权
                                                                - 📱 移动端友好的 API 设计
                                                                - 💳 支持支付宝、微信支付
                                                                - 🛒 完整的购物车和订单流程
                                                                - 🎯 智能商品推荐
                                                                - 📊 数据统计分析

                                                                ## 快速开始
                                                                1. 获取 API 访问令牌
                                                                2. 在请求头中添加 `Authorization: Bearer {token}`
                                                                3. 开始调用 API

                                                                ## 支持
                                                                如有问题，请联系开发团队或查看详细文档。
                                                                """)
                                                .version("v1.2.0")
                                                .termsOfService("https://www.muying-mall.com/terms")
                                                .contact(new Contact()
                                                                .name("母婴商城开发团队")
                                                                .email("api-support@muying-mall.com")
                                                                .url("https://www.muying-mall.com/contact"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                                .servers(Arrays.asList(
                                                new Server().url("http://localhost:8080/api").description("本地开发环境"),
                                                new Server().url("https://api-test.muying-mall.com/api")
                                                                .description("测试环境"),
                                                new Server().url("https://api-staging.muying-mall.com/api")
                                                                .description("预生产环境"),
                                                new Server().url("https://api.muying-mall.com/api")
                                                                .description("生产环境")))
                                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                                .components(new Components()
                                                .addSecuritySchemes("JWT", new SecurityScheme()
                                                                .name("Authorization")
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .in(SecurityScheme.In.HEADER)
                                                                .description("JWT 认证令牌，格式：Bearer {token}")))
                                .externalDocs(new ExternalDocumentation()
                                                .description("完整的 API 文档和开发指南")
                                                .url("https://docs.muying-mall.com"))
                                .tags(Arrays.asList(
                                                new Tag().name("用户管理").description("用户注册、登录、信息管理等功能"),
                                                new Tag().name("商品管理").description("商品查询、分类、品牌等功能"),
                                                new Tag().name("订单管理").description("订单创建、查询、状态管理等功能"),
                                                new Tag().name("支付管理").description("支付处理、退款等功能"),
                                                new Tag().name("购物车").description("购物车商品管理功能"),
                                                new Tag().name("地址管理").description("用户收货地址管理"),
                                                new Tag().name("优惠券").description("优惠券领取和使用"),
                                                new Tag().name("积分系统").description("积分获取和兑换"),
                                                new Tag().name("评论系统").description("商品评论和回复"),
                                                new Tag().name("管理员功能").description("后台管理相关接口"),
                                                new Tag().name("系统功能").description("系统状态、健康检查等")));
        }

        /**
         * 用户相关API分组
         */
        @Bean
        public GroupedOpenApi userApi() {
                return GroupedOpenApi.builder()
                                .group("用户管理")
                                .pathsToMatch("/user/**", "/address/**", "/favorite/**")
                                .displayName("👤 用户管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("用户注册、登录、个人信息管理、收货地址、收藏等功能")))
                                .build();
        }

        /**
         * 商品相关API分组
         */
        @Bean
        public GroupedOpenApi productApi() {
                return GroupedOpenApi.builder()
                                .group("商品管理")
                                .pathsToMatch("/products/**", "/brands/**", "/categories/**")
                                .displayName("🛍️ 商品管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("商品查询、分类管理、品牌管理、商品推荐等功能")))
                                .build();
        }

        /**
         * 订单相关API分组
         */
        @Bean
        public GroupedOpenApi orderApi() {
                return GroupedOpenApi.builder()
                                .group("订单管理")
                                .pathsToMatch("/order/**", "/payment/**", "/logistics/**", "/cart/**")
                                .displayName("📦 订单管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("订单创建、查询、支付、物流跟踪、购物车管理等功能")))
                                .build();
        }

        /**
         * 营销相关API分组
         */
        @Bean
        public GroupedOpenApi marketingApi() {
                return GroupedOpenApi.builder()
                                .group("营销功能")
                                .pathsToMatch("/coupons/**", "/points/**", "/comments/**")
                                .displayName("🎯 营销功能")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("优惠券、积分系统、评论管理等营销相关功能")))
                                .build();
        }

        /**
         * 管理员相关API分组
         */
        @Bean
        public GroupedOpenApi adminApi() {
                return GroupedOpenApi.builder()
                                .group("管理员功能")
                                .pathsToMatch("/admin/**")
                                .displayName("🔧 管理员功能")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("后台管理、数据统计、系统配置等管理员专用功能")))
                                .build();
        }

        /**
         * 系统功能API分组
         */
        @Bean
        public GroupedOpenApi systemApi() {
                return GroupedOpenApi.builder()
                                .group("系统功能")
                                .pathsToMatch("/test/**", "/actuator/**")
                                .displayName("⚙️ 系统功能")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("系统健康检查、测试接口、监控指标等系统功能")))
                                .build();
        }
}