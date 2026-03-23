# 顺丰沙箱 API 接入 — 阶段一实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** 完成阶段一基础接入：配置类 + 抽象接口 + 顺丰客户端 + 发货下单打通，获取真实运单号。

**Architecture:** 采用物流接入抽象层（`ExpressProviderClient`）+ 顺丰实现层（`SfExpressClient`）分层设计，通过 `provider_type` 字段隔离第三方物流与本地模拟物流，配置驱动沙箱/生产切换。

**Tech Stack:** Spring Boot 3.3 + Java 21, RestTemplate, MyBatis-Plus, Redis, YAML Configuration

---

## 阶段一任务总览

| # | 任务 | 类型 | 状态 |
|---|------|------|------|
| 1 | 数据库 logistics 表新增字段 | SQL 变更 | — |
| 2 | 新增 SfExpressProperties 配置类 | 新增文件 | — |
| 3 | 新增 ExpressProviderClient 抽象接口 | 新增文件 | — |
| 4 | 新增 SF DTO（请求/响应/路由节点）| 新增文件 | — |
| 5 | 新增 SfSignatureService 签名服务 | 新增文件 | — |
| 6 | 新增 SfRequestFactory 请求工厂 | 新增文件 | — |
| 7 | 新增 SfResponseParser 响应解析器 | 新增文件 | — |
| 8 | 新增 SfExpressClient 顺丰客户端 | 新增文件 | — |
| 9 | 修改 AdminOrderController 发货入口 | 修改文件 | — |
| 10 | 修改 OrderPaidEventListener 防干扰 | 修改文件 | — |
| 11 | 新增 SF 沙箱配置到 application.yml | 修改文件 | — |
| 12 | 回归验证 | 验证 | — |

---

## Task 1: 数据库 logistics 表新增字段

**Files:**
- Modify: `src/main/resources/db/migration/` 或直接提供 SQL 脚本
- 目标表: `logistics`

**Step 1: 确认数据库迁移文件目录**

```bash
# 检查是否存在 Flyway/Liquibase 迁移目录
ls src/main/resources/db/migration/ 2>/dev/null || echo "无迁移目录，使用 SQL 脚本"
```

**Step 2: 编写 SQL 变更脚本**

```sql
-- file: src/main/resources/db/migration/V20260323__add_sf_fields_to_logistics.sql

-- ============================================================
-- 顺丰沙箱接入：logistics 表新增第三方物流字段
-- 关联设计文档: docs/plans/2026-03-23-sf-express-phase1-design.md
-- ============================================================

ALTER TABLE logistics
  ADD COLUMN provider_code         VARCHAR(32)  DEFAULT NULL COMMENT '物流供应商代码，如 SF'
  ADD COLUMN provider_type         VARCHAR(32)  DEFAULT NULL COMMENT 'THIRD_PARTY / LOCAL_SIMULATION'
  ADD COLUMN provider_waybill_no  VARCHAR(64)  DEFAULT NULL COMMENT '第三方真实运单号'
  ADD COLUMN provider_status      VARCHAR(32)  DEFAULT NULL COMMENT '第三方标准化状态'
  ADD COLUMN route_sync_mode      VARCHAR(16)  DEFAULT NULL COMMENT 'POLLING / PUSH'
  ADD COLUMN route_sync_status    VARCHAR(16)  DEFAULT NULL COMMENT 'PENDING / SYNCING / SUCCESS / FAILED'
  ADD COLUMN last_route_sync_time DATETIME     DEFAULT NULL COMMENT '最近路由同步时间';

-- 给 provider_type 添加索引（按供应商类型查询时用到）
ALTER TABLE logistics
  ADD INDEX idx_provider_type (provider_type),
  ADD INDEX idx_route_sync_status (route_sync_status);
```

**Step 3: 执行 SQL 脚本**

在 MySQL 客户端执行以上 SQL，或由 DBA 执行。

**Step 4: 提交**

```bash
git add src/main/resources/db/migration/V20260323__add_sf_fields_to_logistics.sql
git commit -m "db: logistics表新增第三方物流字段(provider_type等)

用于阶段一接入顺丰沙箱，隔离第三方物流与本地模拟物流。
关联: docs/plans/2026-03-23-sf-express-phase1-design.md

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 2: 新增 SfExpressProperties 配置类

**Files:**
- Create: `src/main/java/com/muyingmall/config/SfExpressProperties.java`

**Step 1: 创建配置文件**

```java
// src/main/java/com/muyingmall/config/SfExpressProperties.java

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
 *     client-code: ${SF_CLIENT_CODE}
 *     check-word: ${SF_CHECK_WORD}
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

    /** 路由同步模式：polling（轮询）或 push（推送） */
    private String syncMode = "polling";

    /** 沙箱专用下单服务码 */
    private String orderServiceCode = "EXP_RECE_CREATE_ORDER";

    /** 沙箱专用路由查询服务码 */
    private String routeQueryServiceCode = "EXP_RECE_SEARCH_ROUTES";

    /**
     * 获取实际使用的 baseUrl
     * 沙箱：https://sfapi-sbox.sf-express.com/std/service
     * 生产：https://sfapi.sf-express.com/std/service
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
```

**Step 2: 确认 application.yml 中有对应配置占位**

在 `application.yml` 的 `sf:` 节点下添加占位（具体值由用户提供环境变量）：

```yaml
# src/main/resources/application.yml 新增节点
sf:
  express:
    enabled: true
    sandbox-enabled: true
    base-url: https://sfapi-sbox.sf-express.com/std/service
    client-code: ${SF_CLIENT_CODE:}
    check-word: ${SF_CHECK_WORD:}
    sync-mode: polling
```

**Step 3: 提交**

```bash
git add src/main/java/com/muyingmall/config/SfExpressProperties.java
git add src/main/resources/application.yml
git commit -m "feat: 新增SfExpressProperties配置类，支持沙箱/生产切换

绑定sf.express.* YAML配置，含clientCode、checkWord、baseUrl等。
顺丰接入阶段一基础设施。

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 3: 新增 ExpressProviderClient 抽象接口

**Files:**
- Create: `src/main/java/com/muyingmall/client/ExpressProviderClient.java`

**Step 1: 创建抽象接口**

```java
// src/main/java/com/muyingmall/client/ExpressProviderClient.java

package com.muyingmall.client;

import com.muyingmall.dto.sf.CreateWaybillCommand;
import com.muyingmall.dto.sf.CreateWaybillResult;
import com.muyingmall.dto.sf.QueryRoutesCommand;
import com.muyingmall.dto.sf.QueryRoutesResult;
import com.muyingmall.dto.sf.RegisterRoutePushCommand;
import com.muyingmall.dto.sf.RegisterRoutePushResult;

/**
 * 物流供应商接入抽象接口
 * <p>
 * 定义所有第三方物流商必须实现的统一契约，屏蔽不同供应商的协议差异。
 * 当前阶段只实现顺丰（SF），未来可扩展其他供应商。
 *
 * @see com.muyingmall.client.sf.SfExpressClient
 */
public interface ExpressProviderClient {

    /**
     * 创建运单（下单）
     *
     * @param command 下单命令，包含寄件人、收件人、商品信息等
     * @return 下单结果，含运单号、错误码等
     */
    CreateWaybillResult createWaybill(CreateWaybillCommand command);

    /**
     * 查询物流路由（轨迹）
     *
     * @param command 查询命令，包含运单号等
     * @return 路由查询结果，含节点列表
     */
    QueryRoutesResult queryRoutes(QueryRoutesCommand command);

    /**
     * 注册路由推送（回调）
     * <p>
     * 注意：沙箱阶段不启用回调，仅预留接口。
     * 第二阶段有公网地址后启用。
     *
     * @param command 注册命令
     * @return 注册结果
     */
    RegisterRoutePushResult registerRoutePush(RegisterRoutePushCommand command);

    /**
     * 校验回调签名
     * <p>
     * 用于验证顺丰推送过来的数据是否被篡改。
     * 沙箱阶段不启用，但预留接口。
     *
     * @param payload 原始报文
     * @param signature 签名
     * @return true = 签名合法，false = 签名不合法
     */
    boolean verifyCallbackSignature(String payload, String signature);
}
```

**Step 2: 提交**

```bash
git add src/main/java/com/muyingmall/client/ExpressProviderClient.java
git commit -m "feat: 新增ExpressProviderClient物流商抽象接口

定义createWaybill、queryRoutes、registerRoutePush、verifyCallbackSignature契约。
符合SOLID依赖倒置原则，支持未来扩展其他物流商。

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 4: 新增 SF DTO（请求/响应/路由节点）

**Files:**
- Create: `src/main/java/com/muyingmall/dto/sf/CreateWaybillCommand.java`
- Create: `src/main/java/com/muyingmall/dto/sf/CreateWaybillResult.java`
- Create: `src/main/java/com/muyingmall/dto/sf/QueryRoutesCommand.java`
- Create: `src/main/java/com/muyingmall/dto/sf/QueryRoutesResult.java`
- Create: `src/main/java/com/muyingmall/dto/sf/RegisterRoutePushCommand.java`
- Create: `src/main/java/com/muyingmall/dto/sf/RegisterRoutePushResult.java`
- Create: `src/main/java/com/muyingmall/dto/sf/SfRouteNodeDTO.java`
- Create: `src/main/java/com/muyingmall/dto/sf/SfCallbackRequest.java`

**Step 1: CreateWaybillCommand.java**

```java
// src/main/java/com/muyingmall/dto/sf/CreateWaybillCommand.java

package com.muyingmall.dto.sf;

import lombok.Data;
import java.util.List;

/**
 * 顺丰下单命令
 * 对应顺丰 EXP_RECE_CREATE_ORDER 接口的请求参数
 */
@Data
public class CreateWaybillCommand {

    /** 订单号（我方业务单号） */
    private String orderNo;

    /** 寄件人信息 */
    private ContactInfo sender;

    /** 收件人信息 */
    private ContactInfo receiver;

    /** 商品信息列表 */
    private List<CargoInfo> cargoList;

    /** 顺丰月结账号 */
    private String monthlyAccount;

    /** 备注 */
    private String remark;

    @Data
    public static class ContactInfo {
        /** 联系人姓名 */
        private String name;
        /** 联系电话 */
        private String phone;
        /** 所在省 */
        private String province;
        /** 所在市 */
        private String city;
        /** 所在区/县 */
        private String district;
        /** 详细地址 */
        private String address;
        /** 邮编（可选） */
        private String postalCode;
    }

    @Data
    public static class CargoInfo {
        /** 商品名称 */
        private String name;
        /** 商品数量 */
        private Integer count;
        /** 单位 */
        private String unit;
        /** 重量（Kg） */
        private Double weight;
        /** 商品编码（可选） */
        private String sku;
    }
}
```

**Step 2: CreateWaybillResult.java**

```java
// src/main/java/com/muyingmall/dto/sf/CreateWaybillResult.java

package com.muyingmall.dto.sf;

import lombok.Data;

/**
 * 顺丰下单结果
 * 对应顺丰下单接口的响应数据
 */
@Data
public class CreateWaybillResult {

    /** 是否成功 */
    private boolean success;

    /** 顺丰运单号（成功时有值） */
    private String waybillNo;

    /** 错误码（失败时有值） */
    private String errorCode;

    /** 错误信息（失败时有值） */
    private String errorMsg;

    /** 顺丰原始响应（便于排查问题） */
    private String rawResponse;

    public static CreateWaybillResult success(String waybillNo, String rawResponse) {
        CreateWaybillResult result = new CreateWaybillResult();
        result.setSuccess(true);
        result.setWaybillNo(waybillNo);
        result.setRawResponse(rawResponse);
        return result;
    }

    public static CreateWaybillResult failure(String errorCode, String errorMsg, String rawResponse) {
        CreateWaybillResult result = new CreateWaybillResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMsg(errorMsg);
        result.setRawResponse(rawResponse);
        return result;
    }
}
```

**Step 3: QueryRoutesCommand.java**

```java
// src/main/java/com/muyingmall/dto/sf/QueryRoutesCommand.java

package com.muyingmall.dto.sf;

import lombok.Data;

/**
 * 顺丰路由查询命令
 */
@Data
public class QueryRoutesCommand {

    /** 顺丰运单号 */
    private String waybillNo;

    /** 顺丰月结账号 */
    private String monthlyAccount;

    /** 查询日期（格式：yyyy-MM-dd，可选） */
    private String queryDate;
}
```

**Step 4: QueryRoutesResult.java**

```java
// src/main/java/com/muyingmall/dto/sf/QueryRoutesResult.java

package com.muyingmall.dto.sf;

import lombok.Data;
import java.util.List;

/**
 * 顺丰路由查询结果
 */
@Data
public class QueryRoutesResult {

    /** 是否成功 */
    private boolean success;

    /** 路由节点列表 */
    private List<SfRouteNodeDTO> nodes;

    /** 当前状态（签收/运输中/异常等） */
    private String currentStatus;

    /** 错误码 */
    private String errorCode;

    /** 错误信息 */
    private String errorMsg;

    /** 原始响应 */
    private String rawResponse;

    public static QueryRoutesResult success(List<SfRouteNodeDTO> nodes, String currentStatus, String rawResponse) {
        QueryRoutesResult result = new QueryRoutesResult();
        result.setSuccess(true);
        result.setNodes(nodes);
        result.setCurrentStatus(currentStatus);
        result.setRawResponse(rawResponse);
        return result;
    }

    public static QueryRoutesResult failure(String errorCode, String errorMsg, String rawResponse) {
        QueryRoutesResult result = new QueryRoutesResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMsg(errorMsg);
        result.setRawResponse(rawResponse);
        return result;
    }
}
```

**Step 5: SfRouteNodeDTO.java**

```java
// src/main/java/com/muyingmall/dto/sf/SfRouteNodeDTO.java

package com.muyingmall.dto.sf;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 顺丰路由节点
 * 对应顺丰返回的单个物流节点（轨迹点）
 */
@Data
public class SfRouteNodeDTO {

    /** 节点时间 */
    private LocalDateTime eventTime;

    /** 节点地点 */
    private String location;

    /** 节点描述 */
    private String description;

    /** 顺丰操作码（如：PUP=揽收，SOR=发出，ARR=到达，DEL=派送，FIN=签收） */
    private String eventCode;

    /** 外部事件唯一ID（用于幂等） */
    private String eventId;

    /** 原始报文（便于排查） */
    private String rawJson;
}
```

**Step 6: RegisterRoutePushCommand.java、RegisterRoutePushResult.java**

```java
// src/main/java/com/muyingmall/dto/sf/RegisterRoutePushCommand.java
package com.muyingmall.dto.sf;

import lombok.Data;

@Data
public class RegisterRoutePushCommand {
    /** 顺丰运单号 */
    private String waybillNo;
    /** 顺丰月结账号 */
    private String monthlyAccount;
    /** 回调通知地址（公网 HTTPS） */
    private String notifyUrl;
}
```

```java
// src/main/java/com/muyingmall/dto/sf/RegisterRoutePushResult.java
package com.muyingmall.dto.sf;

import lombok.Data;

@Data
public class RegisterRoutePushResult {
    private boolean success;
    private String errorCode;
    private String errorMsg;
}
```

**Step 7: SfCallbackRequest.java**

```java
// src/main/java/com/muyingmall/dto/sf/SfCallbackRequest.java

package com.muyingmall.dto.sf;

import lombok.Data;

/**
 * 顺丰路由回调请求体（沙箱阶段预留）
 */
@Data
public class SfCallbackRequest {

    /** 推送类型 */
    private String pushType;

    /** 顺丰运单号 */
    private String waybillNo;

    /** 推送时间戳 */
    private Long timestamp;

    /** 签名 */
    private String signature;

    /** 消息体 */
    private String msgData;
}
```

**Step 8: 提交**

```bash
git add src/main/java/com/muyingmall/dto/sf/
git commit -m "feat: 新增顺丰API相关DTO（请求/响应/路由节点/回调）

包含CreateWaybillCommand/Result、QueryRoutesCommand/Result、
SfRouteNodeDTO、SfCallbackRequest等，全部位于dto/sf包下。

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 5: 新增 SfSignatureService 签名服务

**Files:**
- Create: `src/main/java/com/muyingmall/client/sf/SfSignatureService.java`

**Step 1: 创建签名服务**

```java
// src/main/java/com/muyingmall/client/sf/SfSignatureService.java

package com.muyingmall.client.sf;

import com.muyingmall.config.SfExpressProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 顺丰 API 签名服务
 * <p>
 * 顺丰丰桥新 API 使用 msgDigest 签名，算法为：
 * msgDigest = Base64(HMAC-SHA256(checkWord + msgData + checkWord, checkWord))
 * <p>
 * 注意：沙箱与生产环境使用相同的 checkWord，仅 baseUrl 不同。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SfSignatureService {

    private final SfExpressProperties sfProperties;

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * 计算顺丰请求签名（msgDigest）
     *
     * @param msgData 业务 JSON 字符串（不含 msgDigest 本身）
     * @return Base64 编码的签名
     */
    public String calculateMsgDigest(String msgData) {
        String checkWord = sfProperties.getCheckWord();
        if (checkWord == null || checkWord.isBlank()) {
            throw new IllegalStateException("顺丰 checkWord 未配置，请检查 sf.express.check-word 配置项");
        }

        try {
            String data = checkWord + msgData + checkWord;
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    checkWord.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("顺丰签名计算失败: checkWord={}", maskCheckWord(checkWord), e);
            throw new RuntimeException("顺丰签名计算失败", e);
        }
    }

    /**
     * 校验顺丰回调签名
     *
     * @param msgData   回调报文中的 msgData 字段
     * @param signature  回调报文中的 msgDigest 字段
     * @return true = 签名合法
     */
    public boolean verifySignature(String msgData, String signature) {
        if (msgData == null || signature == null) {
            return false;
        }
        String expected = calculateMsgDigest(msgData);
        boolean matched = expected.equals(signature);
        if (!matched) {
            log.warn("顺丰回调签名校验失败: expected={}, actual={}", expected, signature);
        }
        return matched;
    }

    /**
     * 脱敏 checkWord 日志打印
     */
    private String maskCheckWord(String checkWord) {
        if (checkWord == null || checkWord.length() <= 4) {
            return "****";
        }
        return checkWord.substring(0, 2) + "****" + checkWord.substring(checkWord.length() - 2);
    }
}
```

**Step 2: 提交**

```bash
git add src/main/java/com/muyingmall/client/sf/SfSignatureService.java
git commit -m "feat: 新增SfSignatureService，顺丰API签名计算与校验

实现msgDigest签名算法：Base64(HMAC-SHA256(checkWord + msgData + checkWord))。
用于请求签名和回调签名校验（回调阶段使用）。

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 6: 新增 SfRequestFactory 请求工厂

**Files:**
- Create: `src/main/java/com/muyingmall/client/sf/SfRequestFactory.java`

**Step 1: 创建请求工厂**

```java
// src/main/java/com/muyingmall/client/sf/SfRequestFactory.java

package com.muyingmall.client.sf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.config.SfExpressProperties;
import com.muyingmall.dto.sf.CreateWaybillCommand;
import com.muyingmall.dto.sf.QueryRoutesCommand;
import com.muyingmall.dto.sf.RegisterRoutePushCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 顺丰请求工厂
 * <p>
 * 负责构建符合顺丰丰桥新 API 规范的公共请求参数。
 * 公共参数包括：partnerID、requestID、serviceCode、timestamp、msgData、msgDigest
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SfRequestFactory {

    private final SfExpressProperties sfProperties;
    private final SfSignatureService signatureService;
    private final ObjectMapper objectMapper;

    /**
     * 构建下单请求
     *
     * @param command 下单命令
     * @return 完整请求 Map（含公共参数 + msgDigest）
     */
    public Map<String, Object> buildCreateOrderRequest(CreateWaybillCommand command) {
        Map<String, Object> msgData = buildMsgData(command);
        return buildCommonRequest(sfProperties.getOrderServiceCode(), msgData);
    }

    /**
     * 构建路由查询请求
     *
     * @param command 查询命令
     * @return 完整请求 Map
     */
    public Map<String, Object> buildQueryRoutesRequest(QueryRoutesCommand command) {
        Map<String, Object> msgData = new HashMap<>();
        msgData.put("waybillNo", command.getWaybillNo());
        if (command.getMonthlyAccount() != null) {
            msgData.put("monthlyAccount", command.getMonthlyAccount());
        }
        return buildCommonRequest(sfProperties.getRouteQueryServiceCode(), msgData);
    }

    /**
     * 构建路由推送注册请求（沙箱阶段预留）
     */
    public Map<String, Object> buildRegisterPushRequest(RegisterRoutePushCommand command) {
        Map<String, Object> msgData = new HashMap<>();
        msgData.put("waybillNo", command.getWaybillNo());
        msgData.put("monthlyAccount", command.getMonthlyAccount());
        msgData.put("notifyUrl", command.getNotifyUrl());
        return buildCommonRequest("EXP_RECE_ROUTE_REG", msgData);
    }

    /**
     * 构建公共请求参数
     *
     * @param serviceCode 服务码
     * @param msgData     业务数据
     * @return 完整请求 Map
     */
    private Map<String, Object> buildCommonRequest(String serviceCode, Map<String, Object> msgData) {
        String msgDataJson = toJson(msgData);

        Map<String, Object> request = new HashMap<>();
        request.put("partnerID", sfProperties.getClientCode());
        request.put("requestID", UUID.randomUUID().toString().replace("-", ""));
        request.put("serviceCode", serviceCode);
        request.put("timestamp", Instant.now().getEpochSecond());
        request.put("msgData", msgDataJson);
        request.put("msgDigest", signatureService.calculateMsgDigest(msgDataJson));

        return request;
    }

    /**
     * 将对象序列化为 JSON 字符串
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化请求数据失败", e);
        }
    }
}
```

**Step 2: 提交**

```bash
git add src/main/java/com/muyingmall/client/sf/SfRequestFactory.java
git commit -m "feat: 新增SfRequestFactory，顺丰请求参数构建

封装公共请求参数构建：partnerID、requestID、serviceCode、timestamp、msgData、msgDigest。
提供buildCreateOrderRequest、buildQueryRoutesRequest等方法。

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 7: 新增 SfResponseParser 响应解析器

**Files:**
- Create: `src/main/java/com/muyingmall/client/sf/SfResponseParser.java`

**Step 1: 创建响应解析器**

```java
// src/main/java/com/muyingmall/client/sf/SfResponseParser.java

package com.muyingmall.client.sf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.dto.sf.CreateWaybillResult;
import com.muyingmall.dto.sf.QueryRoutesResult;
import com.muyingmall.dto.sf.RegisterRoutePushResult;
import com.muyingmall.dto.sf.SfRouteNodeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 顺丰响应解析器
 * <p>
 * 解析顺丰 API 返回的 JSON 响应，提取关键字段并封装为项目 DTO。
 * 沙箱返回格式与生产略有差异，解析逻辑需兼容。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SfResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * 解析下单响应
     *
     * @param responseJson 顺丰返回的 JSON 字符串
     * @return 封装后的下单结果
     */
    public CreateWaybillResult parseCreateOrderResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            // 顺丰响应结构：{ "code": 0, "msg": "...", "data": { ... } }
            int code = root.has("code") ? root.get("code").asInt() : -1;
            String msg = root.has("msg") ? root.get("msg").asText() : "";

            if (code == 0 || "OK".equalsIgnoreCase(msg)) {
                JsonNode data = root.get("data");
                String waybillNo = extractWaybillNo(data);
                return CreateWaybillResult.success(waybillNo, responseJson);
            } else {
                String errorCode = String.valueOf(code);
                String errorMsg = msg;
                // 尝试从 data 中提取更详细的错误信息
                if (data != null && data.has("errorCode")) {
                    errorCode = data.get("errorCode").asText();
                }
                if (data != null && data.has("errorMsg")) {
                    errorMsg = data.get("errorMsg").asText();
                }
                return CreateWaybillResult.failure(errorCode, errorMsg, responseJson);
            }
        } catch (JsonProcessingException e) {
            log.error("解析顺丰下单响应失败: {}", e.getMessage());
            return CreateWaybillResult.failure("PARSE_ERROR", "响应解析失败: " + e.getMessage(), responseJson);
        }
    }

    /**
     * 解析路由查询响应
     *
     * @param responseJson 顺丰返回的 JSON 字符串
     * @return 封装后的路由查询结果
     */
    public QueryRoutesResult parseQueryRoutesResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            int code = root.has("code") ? root.get("code").asInt() : -1;
            String msg = root.has("msg") ? root.get("msg").asText() : "";

            if (code == 0 || "OK".equalsIgnoreCase(msg)) {
                JsonNode data = root.get("data");
                List<SfRouteNodeDTO> nodes = extractRouteNodes(data);
                String currentStatus = extractCurrentStatus(data);
                return QueryRoutesResult.success(nodes, currentStatus, responseJson);
            } else {
                return QueryRoutesResult.failure(String.valueOf(code), msg, responseJson);
            }
        } catch (JsonProcessingException e) {
            log.error("解析顺丰路由查询响应失败: {}", e.getMessage());
            return QueryRoutesResult.failure("PARSE_ERROR", "响应解析失败", responseJson);
        }
    }

    /**
     * 解析路由推送注册响应（沙箱阶段预留）
     */
    public RegisterRoutePushResult parseRegisterPushResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            int code = root.has("code") ? root.get("code").asInt() : -1;
            String msg = root.has("msg") ? root.get("msg").asText() : "";
            RegisterRoutePushResult result = new RegisterRoutePushResult();
            result.setSuccess(code == 0 || "OK".equalsIgnoreCase(msg));
            result.setErrorCode(String.valueOf(code));
            result.setErrorMsg(msg);
            return result;
        } catch (JsonProcessingException e) {
            RegisterRoutePushResult result = new RegisterRoutePushResult();
            result.setSuccess(false);
            result.setErrorCode("PARSE_ERROR");
            result.setErrorMsg(e.getMessage());
            return result;
        }
    }

    // ----- 私有辅助方法 -----

    /**
     * 从响应 data 节点提取运单号
     */
    private String extractWaybillNo(JsonNode data) {
        if (data == null) return null;
        // 顺丰返回格式可能是 waybillNo 或 waybillNos（数组）
        if (data.has("waybillNo")) {
            return data.get("waybillNo").asText();
        }
        if (data.has("waybillNos") && data.get("waybillNos").isArray()) {
            return data.get("waybillNos").get(0).asText();
        }
        return null;
    }

    /**
     * 从响应 data 节点提取路由节点列表
     */
    private List<SfRouteNodeDTO> extractRouteNodes(JsonNode data) {
        List<SfRouteNodeDTO> nodes = new ArrayList<>();
        if (data == null || !data.has("routeList")) return nodes;

        JsonNode routeList = data.get("routeList");
        if (!routeList.isArray()) return nodes;

        for (JsonNode node : routeList) {
            SfRouteNodeDTO dto = new SfRouteNodeDTO();
            dto.setEventId(node.has("id") ? node.get("id").asText() : null);
            dto.setEventTime(parseTime(node.has("acceptTime") ? node.get("acceptTime").asText() : null));
            dto.setLocation(node.has("acceptAddress") ? node.get("acceptAddress").asText() : null);
            dto.setDescription(node.has("remark") ? node.get("remark").asText() : null);
            dto.setEventCode(node.has("opCode") ? node.get("opCode").asText() : null);
            dto.setRawJson(node.toString());
            nodes.add(dto);
        }
        return nodes;
    }

    /**
     * 从响应 data 节点提取当前状态
     */
    private String extractCurrentStatus(JsonNode data) {
        if (data == null) return null;
        if (data.has("curStatus")) {
            return data.get("curStatus").asText();
        }
        return null;
    }

    /**
     * 解析时间字符串为 LocalDateTime
     * 顺丰返回格式：yyyy-MM-dd HH:mm:ss
     */
    private LocalDateTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return null;
        try {
            // 尝试标准格式
            return LocalDateTime.parse(timeStr,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            try {
                // 尝试 ISO 格式
                return LocalDateTime.parse(timeStr);
            } catch (Exception e2) {
                log.warn("无法解析顺丰时间: {}", timeStr);
                return null;
            }
        }
    }
}
```

**Step 2: 提交**

```bash
git add src/main/java/com/muyingmall/client/sf/SfResponseParser.java
git commit -m "feat: 新增SfResponseParser，顺丰响应解析与错误映射

解析顺丰下单/路由查询/推送注册响应，提取运单号、路由节点列表、当前状态。
包含多种顺丰响应格式兼容处理逻辑。

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 8: 新增 SfExpressClient 顺丰客户端

**Files:**
- Create: `src/main/java/com/muyingmall/client/sf/SfExpressClient.java`
- Also needs RestTemplate — check if existing or inject

**Step 1: 检查 RestTemplate 配置**

```bash
# 检查是否已有 RestTemplate 配置
grep -r "RestTemplate" src/main/java/com/muyingmall/config/ --include="*.java" | head -5
```

**Step 2: 创建顺丰客户端**

```java
// src/main/java/com/muyingmall/client/sf/SfExpressClient.java

package com.muyingmall.client.sf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.client.ExpressProviderClient;
import com.muyingmall.config.SfExpressProperties;
import com.muyingmall.dto.sf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 顺丰开放平台客户端实现
 * <p>
 * 负责发送 HTTP 请求到顺丰丰桥 API，统一处理：
 * - 公共请求参数拼装（通过 SfRequestFactory）
 * - 签名生成（通过 SfSignatureService）
 * - 响应解析（通过 SfResponseParser）
 * - 异常处理与日志记录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SfExpressClient implements ExpressProviderClient {

    private final SfExpressProperties sfProperties;
    private final SfRequestFactory requestFactory;
    private final SfResponseParser responseParser;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 30000;

    @Override
    public CreateWaybillResult createWaybill(CreateWaybillCommand command) {
        if (!sfProperties.isEnabled()) {
            log.warn("顺丰接入未启用，跳过下单");
            return CreateWaybillResult.failure("DISABLED", "顺丰接入未启用", null);
        }

        try {
            // 1. 构建请求
            Map<String, Object> request = requestFactory.buildCreateOrderRequest(command);
            String requestJson = objectMapper.writeValueAsString(request);
            log.info("【顺丰下单】发送请求: baseUrl={}, serviceCode={}, orderNo={}",
                    sfProperties.getActiveBaseUrl(),
                    sfProperties.getOrderServiceCode(),
                    command.getOrderNo());
            log.debug("【顺丰下单】请求体: {}", requestJson);

            // 2. 发送 HTTP POST
            String responseJson = doPost(requestJson);

            // 3. 解析响应
            CreateWaybillResult result = responseParser.parseCreateOrderResponse(responseJson);

            if (result.isSuccess()) {
                log.info("【顺丰下单】成功: orderNo={}, waybillNo={}",
                        command.getOrderNo(), result.getWaybillNo());
            } else {
                log.warn("【顺丰下单】失败: orderNo={}, errorCode={}, errorMsg={}",
                        command.getOrderNo(), result.getErrorCode(), result.getErrorMsg());
            }
            return result;

        } catch (RestClientException e) {
            log.error("【顺丰下单】HTTP请求失败: orderNo={}, error={}",
                    command.getOrderNo(), e.getMessage(), e);
            return CreateWaybillResult.failure("HTTP_ERROR", "顺丰API调用失败: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("【顺丰下单】未知异常: orderNo={}", command.getOrderNo(), e);
            return CreateWaybillResult.failure("UNKNOWN", e.getMessage(), null);
        }
    }

    @Override
    public QueryRoutesResult queryRoutes(QueryRoutesCommand command) {
        if (!sfProperties.isEnabled()) {
            return QueryRoutesResult.failure("DISABLED", "顺丰接入未启用", null);
        }

        try {
            Map<String, Object> request = requestFactory.buildQueryRoutesRequest(command);
            String requestJson = objectMapper.writeValueAsString(request);
            log.info("【顺丰路由查询】waybillNo={}", command.getWaybillNo());

            String responseJson = doPost(requestJson);
            QueryRoutesResult result = responseParser.parseQueryRoutesResponse(responseJson);

            log.info("【顺丰路由查询】waybillNo={}, success={}, nodeCount={}",
                    command.getWaybillNo(), result.isSuccess(),
                    result.getNodes() != null ? result.getNodes().size() : 0);
            return result;

        } catch (RestClientException e) {
            log.error("【顺丰路由查询】HTTP失败: waybillNo={}", command.getWaybillNo(), e);
            return QueryRoutesResult.failure("HTTP_ERROR", e.getMessage(), null);
        } catch (Exception e) {
            log.error("【顺丰路由查询】异常: waybillNo={}", command.getWaybillNo(), e);
            return QueryRoutesResult.failure("UNKNOWN", e.getMessage(), null);
        }
    }

    @Override
    public RegisterRoutePushResult registerRoutePush(RegisterRoutePushCommand command) {
        log.info("【顺丰路由推送注册】沙箱阶段暂不启用: waybillNo={}", command.getWaybillNo());
        RegisterRoutePushResult result = new RegisterRoutePushResult();
        result.setSuccess(false);
        result.setErrorCode("NOT_IMPLEMENTED");
        result.setErrorMsg("沙箱阶段不启用回调，请在生产环境切换后启用");
        return result;
    }

    @Override
    public boolean verifyCallbackSignature(String payload, String signature) {
        // 沙箱阶段不启用回调，此处直接返回 true（仅预留）
        log.info("【顺丰回调签名校验】沙箱阶段跳过，直接返回true");
        return true;
    }

    /**
     * 发送 HTTP POST 请求
     */
    private String doPost(String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                sfProperties.getActiveBaseUrl(),
                HttpMethod.POST,
                entity,
                String.class);

        return response.getBody();
    }
}
```

**Step 3: 如无 RestTemplate Bean 则新增配置**

```java
// src/main/java/com/muyingmall/config/RestTemplateConfig.java（如果不存在）

package com.muyingmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);   // 10s
        factory.setReadTimeout(30000);     // 30s
        return new RestTemplate(factory);
    }
}
```

**Step 4: 提交**

```bash
git add src/main/java/com/muyingmall/client/sf/SfExpressClient.java
git add src/main/java/com/muyingmall/config/RestTemplateConfig.java  # 如果是新文件
git commit -m "feat: 新增SfExpressClient顺丰客户端实现

实现ExpressProviderClient接口，提供createWaybill、queryRoutes能力。
封装HTTP POST请求、签名计算、响应解析全链路。
沙箱阶段registerRoutePush和verifyCallback预留实现。

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 9: 修改 AdminOrderController 发货入口

**Files:**
- Modify: `src/main/java/com/muyingmall/controller/admin/AdminOrderController.java`
- 注意：仅修改 `shipOrder` 方法，增加顺丰判断逻辑

**Step 1: 在类成员变量中注入 SfExpressClient**

```java
// AdminOrderController.java 文件顶部 import 部分增加
import com.muyingmall.client.sf.SfExpressClient;
import com.muyingmall.dto.sf.CreateWaybillCommand;
import com.muyingmall.dto.sf.CreateWaybillResult;

// 类成员变量增加
private final SfExpressClient sfExpressClient;
```

**Step 2: 修改 shipOrder 方法，增加顺丰下单逻辑**

在 `shipOrder` 方法中，原逻辑是"生成运单号"之后直接创建物流记录。

**改造思路**：在生成运单号之前，先判断是否为顺丰，如果是顺丰则调顺丰沙箱下单。

具体修改位置在：
```java
// 当前：String finalTrackingNo = trackingNo;
// 改造后：
String finalTrackingNo;
String providerCode = null;
String providerWaybillNo = null;

// 判断是否为顺丰（物流公司编码为 SF）
if ("SF".equalsIgnoreCase(company.getCode())) {
    log.info("【管理员发货】顺丰发货: orderId={}", id);

    // 构建顺丰下单命令
    CreateWaybillCommand command = new CreateWaybillCommand();
    command.setOrderNo(order.getOrderNo());
    command.setMonthlyAccount(sfProperties.getMonthlyAccount()); // 月结账号

    // 寄件人信息（商家）
    CreateWaybillCommand.ContactInfo sender = new CreateWaybillCommand.ContactInfo();
    sender.setName("母婴商城商家");
    sender.setPhone("400-123-4567");
    sender.setProvince("浙江省");
    sender.setCity("杭州市");
    sender.setDistrict("西湖区");
    sender.setAddress("XX路XX号");
    command.setSender(sender);

    // 收件人信息（从订单获取）
    CreateWaybillCommand.ContactInfo receiver = new CreateWaybillCommand.ContactInfo();
    receiver.setName(finalReceiverName);
    receiver.setPhone(finalReceiverPhone);
    receiver.setProvince(order.getReceiverProvince());
    receiver.setCity(order.getReceiverCity());
    receiver.setDistrict(order.getReceiverDistrict());
    receiver.setAddress(order.getReceiverAddress());
    command.setReceiver(receiver);

    // 调用顺丰下单
    CreateWaybillResult sfResult = sfExpressClient.createWaybill(command);

    if (!sfResult.isSuccess()) {
        log.error("【顺丰下单失败】orderId={}, errorCode={}, errorMsg={}",
                id, sfResult.getErrorCode(), sfResult.getErrorMsg());
        return Result.error("顺丰下单失败: " + sfResult.getErrorMsg());
    }

    // 成功，获取真实运单号
    finalTrackingNo = sfResult.getWaybillNo();
    providerCode = "SF";
    providerWaybillNo = sfResult.getWaybillNo();
    log.info("【顺丰下单成功】orderId={}, waybillNo={}", id, finalTrackingNo);

} else {
    // 非顺丰，保持原有本地逻辑
    finalTrackingNo = StringUtils.hasText(trackingNo)
            ? trackingNo
            : logisticsService.generateTrackingNo(company.getCode());
}
```

**Step 3: 在创建 Logistics 时设置 provider_type**

在创建 `Logistics` 对象后增加：
```java
// 设置第三方物流标识（仅顺丰单设置）
if (providerCode != null) {
    logistics.setProviderCode(providerCode);
    logistics.setProviderType("THIRD_PARTY");
    logistics.setProviderWaybillNo(providerWaybillNo);
    logistics.setRouteSyncMode("POLLING");
    logistics.setRouteSyncStatus("PENDING");
} else {
    logistics.setProviderType("LOCAL_SIMULATION");
}
```

**Step 4: 提交**

```bash
git add src/main/java/com/muyingmall/controller/admin/AdminOrderController.java
git commit -m "feat: AdminOrderController.shipOrder支持顺丰沙箱下单

判断物流公司编码为SF时，调用SfExpressClient.createWaybill获取真实运单号。
第三方物流设置provider_type=THIRD_PARTY，本地模拟物流设置provider_type=LOCAL_SIMULATION。
订单状态和物流记录保存逻辑保持不变。

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 10: 修改 OrderPaidEventListener 防干扰

**Files:**
- Modify: `src/main/java/com/muyingmall/listener/OrderPaidEventListener.java`

**Step 1: 在 handleOrderPaid 方法开头增加 provider_type 判断**

```java
// 在 "双重检查" 之前增加：

// 【新增】如果该订单已标记为第三方物流（由管理员发货），则跳过自动创建
// 说明：第三方物流由管理员发货时已在 AdminOrderController 中创建，此处不应重复创建
if (logisticsService.hasThirdPartyLogistics(order.getOrderId())) {
    log.info("【物流创建】订单已存在第三方物流记录，跳过自动创建: orderId={}",
            order.getOrderId());
    return;
}
```

**Step 2: 在 LogisticsService 接口增加 hasThirdPartyLogistics 方法**

```java
// src/main/java/com/muyingmall/service/LogisticsService.java 新增方法
/**
 * 检查订单是否有第三方物流记录
 * @param orderId 订单ID
 * @return true=有第三方物流记录，false=无
 */
boolean hasThirdPartyLogistics(Integer orderId);
```

**Step 3: 在 LogisticsServiceImpl 实现该方法**

```java
// src/main/java/com/muyingmall/service/impl/LogisticsServiceImpl.java 新增方法
@Override
public boolean hasThirdPartyLogistics(Integer orderId) {
    LambdaQueryWrapper<Logistics> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Logistics::getOrderId, orderId)
            .eq(Logistics::getProviderType, "THIRD_PARTY");
    return count(queryWrapper) > 0;
}
```

**Step 4: 提交**

```bash
git add src/main/java/com/muyingmall/listener/OrderPaidEventListener.java
git add src/main/java/com/muyingmall/service/LogisticsService.java
git add src/main/java/com/muyingmall/service/impl/LogisticsServiceImpl.java
git commit -m "fix: OrderPaidEventListener跳过第三方物流自动创建

解决管理员发货（第三方）后，OrderPaidEventListener重复创建物流的问题。
新增hasThirdPartyLogistics()方法，监听器中判断provider_type=THIRD_PARTY时跳过。
同时将自动创建的物流标记为provider_type=LOCAL_SIMULATION。

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 11: 新增 SF 沙箱配置到 application.yml

**Files:**
- Modify: `src/main/resources/application.yml`

在配置文件末尾添加：

```yaml
# ============================================================
# 顺丰开放平台配置（沙箱阶段）
# client-code 和 check-word 请通过环境变量注入，不要硬编码
# ============================================================
sf:
  express:
    enabled: true                    # 是否启用顺丰接入（生产可改为false临时关闭）
    sandbox-enabled: true            # true=沙箱环境，false=生产环境
    base-url: https://sfapi-sbox.sf-express.com/std/service
    client-code: ${SF_CLIENT_CODE:}   # 从环境变量读取，沙箱编码
    check-word: ${SF_CHECK_WORD:}    # 从环境变量读取，沙箱校验码
    monthly-account: ${SF_MONTHLY_ACCOUNT:}  # 顺丰月结账号
    sync-mode: polling               # 路由同步模式：polling=轮询，push=推送
```

> ⚠️ 注意：请用户根据顺丰沙箱控制台提供的实际 client-code、check-word、monthly-account 填入环境变量。

---

## Task 12: 回归验证

**Step 1: 编译验证**

```bash
cd g:/muying/muying-mall
mvn compile -q  # 无错误即可
```

**Step 2: 验证清单**

| 检查项 | 方法 |
|--------|------|
| 顺丰配置类加载 | 启动应用，观察日志是否出现 "SfExpressProperties" 相关 Bean |
| 非顺丰物流发货正常 | 用非顺丰公司发货，确认物流记录创建正常 |
| 顺丰发货沙箱调用 | 确认环境变量已设置后发货，观察日志 "【顺丰下单】发送请求" |
| 监听器不再干扰第三方物流 | 第三方物流订单支付后，监听器日志应显示 "跳过自动创建" |

---

## 实施完成后提交

```bash
git add -A
git commit -m "$(cat <<'EOF'
feat: 完成顺丰沙箱API阶段一接入（配置+客户端+发货打通）

阶段一完成内容：
- 新增SfExpressProperties配置类（沙箱/生产切换）
- 新增ExpressProviderClient抽象接口（SOLID依赖倒置）
- 新增SfSignatureService签名服务（HMAC-SHA256）
- 新增SfRequestFactory请求工厂（公共参数封装）
- 新增SfResponseParser响应解析器（多种格式兼容）
- 新增SfExpressClient顺丰客户端（HTTP+签名+解析全链路）
- 新增完整SF DTO包（请求/响应/路由节点/回调）
- 修改AdminOrderController支持顺丰下单
- 修改OrderPaidEventListener防第三方物流干扰
- logistics表新增第三方物流字段

关联设计文档: docs/plans/2026-03-23-sf-express-phase1-design.md

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```
