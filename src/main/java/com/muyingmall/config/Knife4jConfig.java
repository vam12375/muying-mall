package com.muyingmall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
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
 * Knife4j API 文档配置类
 * 访问地址: http://localhost:8080/api/doc.html
 */
@Configuration
public class Knife4jConfig {

    /**
     * OpenAPI 主配置
     */
    @Bean
    public OpenAPI muyingMallOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("母婴商城 API 文档")
                        .description("母婴商城系统 RESTful API 文档，基于 Spring Boot 3.3.0 构建")
                        .version("v1.2.0")
                        .contact(new Contact()
                                .name("青柠檬")
                                .email("405394597@qq.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("本地开发环境")))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("JWT认证令牌")))
                .externalDocs(new ExternalDocumentation()
                        .description("完整API文档")
                        .url("https://docs.muying-mall.com"));
    }

    // ==================== 默认分组（解决swagger-config问题） ====================

    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/**")
                .build();
    }

    // ==================== 用户端 API 分组 ====================

    @Bean
    public GroupedOpenApi group01UserApi() {
        return GroupedOpenApi.builder()
                .group("01-用户中心")
                .pathsToMatch("/user/**")
                .pathsToExclude("/user/addresses/**", "/user/favorites/**", "/user/message/**", "/user/wallet/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group02AddressApi() {
        return GroupedOpenApi.builder()
                .group("02-地址管理")
                .pathsToMatch("/user/addresses/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group03FavoriteApi() {
        return GroupedOpenApi.builder()
                .group("03-收藏管理")
                .pathsToMatch("/user/favorites/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group04WalletApi() {
        return GroupedOpenApi.builder()
                .group("04-钱包管理")
                .pathsToMatch("/user/wallet/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group05MessageApi() {
        return GroupedOpenApi.builder()
                .group("05-消息中心")
                .pathsToMatch("/user/message/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group06ProductApi() {
        return GroupedOpenApi.builder()
                .group("06-商品浏览")
                .pathsToMatch("/products/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group07CategoryApi() {
        return GroupedOpenApi.builder()
                .group("07-商品分类")
                .pathsToMatch("/categories/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group08BrandApi() {
        return GroupedOpenApi.builder()
                .group("08-品牌管理")
                .pathsToMatch("/brands/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group09CartApi() {
        return GroupedOpenApi.builder()
                .group("09-购物车")
                .pathsToMatch("/cart/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group10OrderApi() {
        return GroupedOpenApi.builder()
                .group("10-订单管理")
                .pathsToMatch("/order/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group11PaymentApi() {
        return GroupedOpenApi.builder()
                .group("11-支付管理")
                .pathsToMatch("/payment/**", "/api/payment/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group12RefundApi() {
        return GroupedOpenApi.builder()
                .group("12-退款管理")
                .pathsToMatch("/refund/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group13LogisticsApi() {
        return GroupedOpenApi.builder()
                .group("13-物流查询")
                .pathsToMatch("/logistics/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group14PointsApi() {
        return GroupedOpenApi.builder()
                .group("14-积分系统")
                .pathsToMatch("/points/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group15CommentApi() {
        return GroupedOpenApi.builder()
                .group("15-评价系统")
                .pathsToMatch("/comment/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group16SearchApi() {
        return GroupedOpenApi.builder()
                .group("16-搜索服务")
                .pathsToMatch("/search/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group17SessionApi() {
        return GroupedOpenApi.builder()
                .group("17-会话管理")
                .pathsToMatch("/session/**")
                .build();
    }

    // ==================== 后台管理 API 分组 ====================

    @Bean
    public GroupedOpenApi group20AdminAuthApi() {
        return GroupedOpenApi.builder()
                .group("20-后台-登录认证")
                .pathsToMatch("/admin/login", "/admin/logout", "/admin/info")
                .build();
    }

    @Bean
    public GroupedOpenApi group21AdminDashboardApi() {
        return GroupedOpenApi.builder()
                .group("21-后台-数据看板")
                .pathsToMatch("/admin/dashboard/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group22AdminAnalyticsApi() {
        return GroupedOpenApi.builder()
                .group("22-后台-数据分析")
                .pathsToMatch("/admin/analytics/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group23AdminUserApi() {
        return GroupedOpenApi.builder()
                .group("23-后台-用户管理")
                .pathsToMatch("/admin/users/**", "/admin/user-accounts/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group24AdminProductApi() {
        return GroupedOpenApi.builder()
                .group("24-后台-商品管理")
                .pathsToMatch("/admin/products/**", "/api/admin/products/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group25AdminCategoryApi() {
        return GroupedOpenApi.builder()
                .group("25-后台-分类管理")
                .pathsToMatch("/admin/categories/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group26AdminBrandApi() {
        return GroupedOpenApi.builder()
                .group("26-后台-品牌管理")
                .pathsToMatch("/admin/brands/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group27AdminOrderApi() {
        return GroupedOpenApi.builder()
                .group("27-后台-订单管理")
                .pathsToMatch("/admin/order/**", "/api/admin/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group28AdminRefundApi() {
        return GroupedOpenApi.builder()
                .group("28-后台-退款管理")
                .pathsToMatch("/admin/refund/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group29AdminLogisticsApi() {
        return GroupedOpenApi.builder()
                .group("29-后台-物流管理")
                .pathsToMatch("/admin/logistics/**", "/api/admin/logistics/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group30AdminCouponApi() {
        return GroupedOpenApi.builder()
                .group("30-后台-优惠券管理")
                .pathsToMatch("/admin/coupon/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group31AdminPointsApi() {
        return GroupedOpenApi.builder()
                .group("31-后台-积分管理")
                .pathsToMatch("/admin/points/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group32AdminCommentApi() {
        return GroupedOpenApi.builder()
                .group("32-后台-评价管理")
                .pathsToMatch("/admin/comments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group33AdminMessageApi() {
        return GroupedOpenApi.builder()
                .group("33-后台-消息管理")
                .pathsToMatch("/admin/message/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group34AdminContentApi() {
        return GroupedOpenApi.builder()
                .group("34-后台-内容管理")
                .pathsToMatch("/admin/content/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group35AdminSearchApi() {
        return GroupedOpenApi.builder()
                .group("35-后台-搜索管理")
                .pathsToMatch("/admin/search/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group36AdminProfileApi() {
        return GroupedOpenApi.builder()
                .group("36-后台-个人设置")
                .pathsToMatch("/admin/profile/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group37AdminSystemApi() {
        return GroupedOpenApi.builder()
                .group("37-后台-系统管理")
                .pathsToMatch("/admin/system/**")
                .build();
    }

    // ==================== 系统功能 API 分组 ====================

    @Bean
    public GroupedOpenApi group18CouponApi() {
        return GroupedOpenApi.builder()
                .group("18-优惠券")
                .pathsToMatch("/coupons/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group90OrderStateApi() {
        return GroupedOpenApi.builder()
                .group("90-订单状态机")
                .pathsToMatch("/api/v1/orders/state/**", "/api/v1/orders/history/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group91PaymentStateApi() {
        return GroupedOpenApi.builder()
                .group("91-支付状态机")
                .pathsToMatch("/api/v1/payments/state/**", "/api/v1/payments/history/**")
                .build();
    }

    @Bean
    public GroupedOpenApi group99TestApi() {
        return GroupedOpenApi.builder()
                .group("99-测试接口")
                .pathsToMatch("/test/**", "/admin/ip-test/**")
                .build();
    }
}
