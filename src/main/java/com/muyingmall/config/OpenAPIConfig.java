package com.muyingmall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
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
                        .description("母婴商城系统的RESTful API文档，提供后端接口的详细说明和测试功能")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("support@muying-mall.com")
                                .url("https://www.muying-mall.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080/api").description("本地开发环境"),
                        new Server().url("https://api.muying-mall.com").description("生产环境")))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .name("JWT")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("请输入JWT令牌，格式为：Bearer {token}")));
    }

    /**
     * 用户相关API分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户管理")
                .pathsToMatch("/api/user/**", "/api/address/**", "/api/favorite/**")
                .displayName("用户管理 API")
                .build();
    }

    /**
     * 商品相关API分组
     */
    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
                .group("商品管理")
                .pathsToMatch("/api/product/**", "/api/brand/**", "/api/category/**")
                .displayName("商品管理 API")
                .build();
    }

    /**
     * 订单相关API分组
     */
    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("订单管理")
                .pathsToMatch("/api/order/**", "/api/payment/**", "/api/logistics/**")
                .displayName("订单管理 API")
                .build();
    }

    /**
     * 管理员相关API分组
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理员功能")
                .pathsToMatch("/api/admin/**")
                .displayName("管理员功能 API")
                .build();
    }
}