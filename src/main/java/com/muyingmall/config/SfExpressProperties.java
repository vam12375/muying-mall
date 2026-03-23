package com.muyingmall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 顺丰开放平台配置属性
 * 绑定 sf.express.* YAML 配置，支持沙箱/生产环境切换
 * <p>
 * 使用方式：在 application.yml 中配置：
 * <pre>
 * sf:
 *   express:
 *     enabled: true
 *     sandbox-enabled: true
 *     base-url: https://sfapi-sbox.sf-express.com/std/service
 *     client-code: ${SF_CLIENT_CODE:}
 *     check-word: ${SF_CHECK_WORD:}
 *     sync-mode: polling
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "sf.express")
public class SfExpressProperties {

    /** 是否启用顺丰接入 */
    private boolean enabled = false;

    /** 是否使用沙箱环境 */
    private boolean sandboxEnabled = true;

    /** API 基础地址，沙箱与生产地址不同 */
    private String baseUrl = "https://sfapi-sbox.sf-express.com/std/service";

    /** 丰桥客户编码（沙箱/生产共用同一个编码，只是地址不同） */
    private String clientCode;

    /** 丰桥校验码（对应沙箱/生产环境的 checkWord） */
    private String checkWord;

    /** 顺丰月结账号（用于下单接口） */
    private String monthlyAccount;

    /** 路由同步模式：polling（轮询）或 push（推送） */
    private String syncMode = "polling";

    /** 沙箱专用下单服务码 */
    private String orderServiceCode = "EXP_RECE_CREATE_ORDER";

    /** 沙箱专用路由查询服务码 */
    private String routeQueryServiceCode = "EXP_RECE_SEARCH_ROUTES";

    /**
     * 获取实际使用的 baseUrl
     */
    public String getActiveBaseUrl() {
        return baseUrl;
    }

    /**
     * 判断当前是否为沙箱环境
     */
    public boolean isSandbox() {
        return sandboxEnabled;
    }
}
