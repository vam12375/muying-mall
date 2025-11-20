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
 * 提供完整的中文API文档，优化分组结构
 * 
 * Source: OpenAPI 配置优化
 * 
 */
@Configuration
public class OpenAPIConfig {

        /**
         * OpenAPI 主配置
         * 配置API文档的基本信息、服务器地址、安全认证等
         */
        @Bean
        public OpenAPI muyingMallOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("母婴商城 API 文档")
                                                .description("""
                                                                # 母婴商城系统 RESTful API 文档

                                                                ## 📖 系统概述
                                                                母婴商城系统是一个功能完整的电商平台，提供用户端购物和后台管理两大核心功能模块。
                                                                系统采用前后端分离架构，基于 Spring Boot 3.x + MyBatis Plus 构建。

                                                                ## ✨ 核心特性
                                                                - 🔐 **安全认证**: JWT Token 认证授权机制
                                                                - 📱 **移动优先**: RESTful API 设计，完美支持移动端
                                                                - 💳 **支付集成**: 支持支付宝、微信支付、钱包支付
                                                                - 🛒 **购物流程**: 完整的购物车、订单、支付、物流流程
                                                                - 🎯 **智能推荐**: 基于用户行为的商品推荐系统
                                                                - 📊 **数据分析**: 实时统计分析和可视化报表
                                                                - 🎁 **营销工具**: 优惠券、积分、评论奖励等营销功能
                                                                - 🔄 **售后服务**: 完善的退款退货处理流程

                                                                ## 🚀 快速开始

                                                                ### 1. 用户端接口
                                                                ```bash
                                                                # 用户注册
                                                                POST /user/register
                                                                
                                                                # 用户登录（获取Token）
                                                                POST /user/login
                                                                
                                                                # 使用Token访问受保护接口
                                                                GET /user/info
                                                                Headers: Authorization: Bearer {your_token}
                                                                ```

                                                                ### 2. 管理后台接口
                                                                所有管理后台接口路径以 `/admin` 开头，需要管理员权限。
                                                                ```bash
                                                                # 获取仪表盘数据
                                                                GET /admin/dashboard/stats
                                                                Headers: Authorization: Bearer {admin_token}
                                                                ```

                                                                ## 🔑 认证说明
                                                                1. 调用登录接口获取 JWT Token
                                                                2. 在后续请求的 Header 中添加: `Authorization: Bearer {token}`
                                                                3. Token 有效期为 7 天，过期后需重新登录

                                                                ## 📝 接口规范
                                                                - 所有接口统一返回格式: `{code, message, data}`
                                                                - 成功响应: `code=200`
                                                                - 业务错误: `code=400-499`
                                                                - 服务器错误: `code=500-599`

                                                                ## 💡 技术支持
                                                                如有问题，请联系开发团队或查看完整文档。
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
                                                new Server().url("http://localhost:8080/api").description("🔧 本地开发环境"),
                                                new Server().url("https://api-test.muying-mall.com/api").description("🧪 测试环境"),
                                                new Server().url("https://api-staging.muying-mall.com/api").description("🎭 预生产环境"),
                                                new Server().url("https://api.muying-mall.com/api").description("🚀 生产环境")))
                                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                                .components(new Components()
                                                .addSecuritySchemes("JWT", new SecurityScheme()
                                                                .name("Authorization")
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .in(SecurityScheme.In.HEADER)
                                                                .description("JWT 认证令牌，格式：Bearer {token}。登录后获取，有效期7天。")))
                                .externalDocs(new ExternalDocumentation()
                                                .description("📚 完整的 API 文档和开发指南")
                                                .url("https://docs.muying-mall.com"));
        }

        // ==================== 用户端 API 分组 ====================
        
        @Bean
        public GroupedOpenApi userApi() {
                return GroupedOpenApi.builder()
                                .group("01-用户管理")
                                .pathsToMatch("/user/**")
                                .displayName("👤 用户管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("用户注册、登录、个人信息管理、头像上传、密码修改等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi productApi() {
                return GroupedOpenApi.builder()
                                .group("02-商品管理")
                                .pathsToMatch("/products/**")
                                .displayName("🛍️ 商品管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("商品列表查询、商品详情、商品推荐、商品规格和参数查询等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi categoryApi() {
                return GroupedOpenApi.builder()
                                .group("03-商品分类")
                                .pathsToMatch("/categories/**")
                                .displayName("📂 商品分类")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("分类列表、分类详情、子分类查询、分类商品列表等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi brandApi() {
                return GroupedOpenApi.builder()
                                .group("04-品牌管理")
                                .pathsToMatch("/brands/**")
                                .displayName("🏷️ 品牌管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("品牌列表、品牌详情、品牌商品查询等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi cartApi() {
                return GroupedOpenApi.builder()
                                .group("05-购物车")
                                .pathsToMatch("/cart/**")
                                .displayName("�  购物车")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("添加商品、查询购物车、修改数量、删除商品、清空购物车等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi orderApi() {
                return GroupedOpenApi.builder()
                                .group("06-订单管理")
                                .pathsToMatch("/order/**")
                                .displayName("📦 订单管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("创建订单、直接购买、订单查询、取消订单、确认收货、订单统计等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi paymentApi() {
                return GroupedOpenApi.builder()
                                .group("07-支付管理")
                                .pathsToMatch("/payment/**", "/alipay/**", "/wallet-payment/**")
                                .displayName("💳 支付管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("支付宝支付、微信支付、钱包支付、支付回调、支付状态查询等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi refundApi() {
                return GroupedOpenApi.builder()
                                .group("08-退款管理")
                                .pathsToMatch("/refund/**")
                                .displayName("↩️ 退款管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("申请退款、查询退款列表、退款详情、取消退款、退款进度查询等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi logisticsApi() {
                return GroupedOpenApi.builder()
                                .group("09-物流管理")
                                .pathsToMatch("/logistics/**")
                                .displayName("🚚 物流管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("查询订单物流信息、物流轨迹跟踪、物流公司列表等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi addressApi() {
                return GroupedOpenApi.builder()
                                .group("10-地址管理")
                                .pathsToMatch("/user/addresses/**")
                                .displayName("📍 地址管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("添加地址、查询地址列表、修改地址、删除地址、设置默认地址等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi couponApi() {
                return GroupedOpenApi.builder()
                                .group("11-优惠券")
                                .pathsToMatch("/coupons/**", "/user/coupons/**")
                                .displayName("🎫 优惠券")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("查询可领取优惠券、领取优惠券、我的优惠券、订单可用优惠券、优惠码兑换等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi pointsApi() {
                return GroupedOpenApi.builder()
                                .group("12-积分系统")
                                .pathsToMatch("/points/**")
                                .displayName("⭐ 积分系统")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("查询积分余额、积分历史、积分商品、积分兑换、兑换记录等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi commentApi() {
                return GroupedOpenApi.builder()
                                .group("13-评价系统")
                                .pathsToMatch("/comment/**")
                                .displayName("💬 评价系统")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("创建评价、查询评价列表、我的评价、评价回复、评价标签、评价模板等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi favoriteApi() {
                return GroupedOpenApi.builder()
                                .group("14-收藏管理")
                                .pathsToMatch("/favorite/**")
                                .displayName("❤️ 收藏管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("添加收藏、取消收藏、查询收藏列表、批量取消收藏等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi messageApi() {
                return GroupedOpenApi.builder()
                                .group("15-消息管理")
                                .pathsToMatch("/user/messages/**")
                                .displayName("📧 消息管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("查询消息列表、消息详情、标记已读、删除消息、未读消息数量等功能")))
                                .build();
        }

        @Bean
        public GroupedOpenApi walletApi() {
                return GroupedOpenApi.builder()
                                .group("16-钱包管理")
                                .pathsToMatch("/user/wallet/**")
                                .displayName("💰 钱包管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("查询钱包余额、钱包充值、交易记录查询、提现申请等功能")))
                                .build();
        }

        // ==================== 后台管理 API 分组 ====================
        
        @Bean
        public GroupedOpenApi adminDashboardApi() {
                return GroupedOpenApi.builder()
                                .group("20-后台-仪表盘")
                                .pathsToMatch("/admin/dashboard/**")
                                .displayName("📊 后台-仪表盘")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("后台首页数据统计、图表展示、待办事项等功能。需要管理员权限（ROLE_ADMIN）。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminUserApi() {
                return GroupedOpenApi.builder()
                                .group("21-后台-用户管理")
                                .pathsToMatch("/admin/users/**", "/admin/user-accounts/**")
                                .displayName("👥 后台-用户管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("用户列表、用户详情、用户状态管理、用户账户管理、余额调整、充值、交易记录等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminProductApi() {
                return GroupedOpenApi.builder()
                                .group("22-后台-商品管理")
                                .pathsToMatch("/admin/products/**")
                                .displayName("📦 后台-商品管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("商品列表、商品创建、商品编辑、商品删除、商品上下架、库存管理、规格管理等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminCategoryApi() {
                return GroupedOpenApi.builder()
                                .group("23-后台-分类管理")
                                .pathsToMatch("/admin/categories/**")
                                .displayName("🗂️ 后台-分类管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("分类列表、分类树形结构、创建分类、编辑分类、删除分类、分类排序、状态管理等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminBrandApi() {
                return GroupedOpenApi.builder()
                                .group("24-后台-品牌管理")
                                .pathsToMatch("/admin/brands/**")
                                .displayName("🏷️ 后台-品牌管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("品牌列表、品牌创建、品牌编辑、品牌删除、品牌状态管理、品牌排序等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminOrderApi() {
                return GroupedOpenApi.builder()
                                .group("25-后台-订单管理")
                                .pathsToMatch("/admin/orders/**", "/admin/order/**")
                                .displayName("📋 后台-订单管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("订单列表、订单详情、订单发货、订单状态管理、订单统计、订单导出等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminRefundApi() {
                return GroupedOpenApi.builder()
                                .group("26-后台-退款管理")
                                .pathsToMatch("/admin/refund/**")
                                .displayName("💸 后台-退款管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("退款列表、退款审核、退款处理、退款完成、退款统计、支付宝退款查询等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminCouponApi() {
                return GroupedOpenApi.builder()
                                .group("27-后台-优惠券管理")
                                .pathsToMatch("/admin/coupons/**")
                                .displayName("🎟️ 后台-优惠券管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("优惠券列表、优惠券创建、优惠券编辑、优惠券删除、优惠券发放、使用统计等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminPointsApi() {
                return GroupedOpenApi.builder()
                                .group("28-后台-积分管理")
                                .pathsToMatch("/admin/points/**")
                                .displayName("🌟 后台-积分管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("积分规则配置、积分商品管理、积分兑换记录、积分统计分析、用户积分调整等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminCommentApi() {
                return GroupedOpenApi.builder()
                                .group("29-后台-评价管理")
                                .pathsToMatch("/admin/comments/**")
                                .displayName("📝 后台-评价管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("评价列表、评价审核、评价回复、评价删除、评价统计等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminLogisticsApi() {
                return GroupedOpenApi.builder()
                                .group("30-后台-物流管理")
                                .pathsToMatch("/admin/logistics/**")
                                .displayName("🚛 后台-物流管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("物流公司列表、物流公司管理、物流信息查询、物流轨迹跟踪等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminMessageApi() {
                return GroupedOpenApi.builder()
                                .group("31-后台-消息管理")
                                .pathsToMatch("/admin/messages/**")
                                .displayName("📮 后台-消息管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("发送系统消息、消息模板管理、消息发送记录、批量发送消息等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminContentApi() {
                return GroupedOpenApi.builder()
                                .group("32-后台-内容管理")
                                .pathsToMatch("/admin/content/**", "/admin/banners/**", "/admin/notices/**")
                                .displayName("📄 后台-内容管理")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("轮播图管理、公告管理、帮助文档管理、富文本编辑等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminAnalyticsApi() {
                return GroupedOpenApi.builder()
                                .group("33-后台-数据分析")
                                .pathsToMatch("/admin/analytics/**", "/admin/statistics/**")
                                .displayName("📈 后台-数据分析")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("销售数据分析、用户行为分析、商品销售排行、数据报表导出等功能。需要管理员权限。")))
                                .build();
        }

        @Bean
        public GroupedOpenApi adminSystemApi() {
                return GroupedOpenApi.builder()
                                .group("34-后台-系统设置")
                                .pathsToMatch("/admin/system/**")
                                .displayName("⚙️ 后台-系统设置")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("系统参数配置、系统监控、日志查询、缓存管理、数据备份等功能。需要管理员权限。")))
                                .build();
        }

        // ==================== 系统功能 API 分组 ====================
        
        @Bean
        public GroupedOpenApi systemApi() {
                return GroupedOpenApi.builder()
                                .group("99-系统功能")
                                .pathsToMatch("/test/**", "/actuator/**", "/session/**")
                                .displayName("🔧 系统功能")
                                .addOpenApiCustomizer(openApi -> openApi.info(openApi.getInfo()
                                                .description("系统健康检查、测试接口、监控指标、Session管理等系统级功能。主要用于开发和运维。")))
                                .build();
        }
}
