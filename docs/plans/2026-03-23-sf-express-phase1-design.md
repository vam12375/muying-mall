# 顺丰沙箱 API 接入 — 阶段一实施设计

> 日期：2026-03-23
> 状态：已批准实施
> 关联文档：`../顺丰沙箱API接入.md`

## 1. 目标

完成顺丰沙箱 API 接入的**阶段一（基础接入）**：
- 在管理员发货时，对接顺丰沙箱进行真实下单
- 获取顺丰返回的真实运单号并保存到本地
- 代码结构支持未来平滑切换生产环境

## 2. 数据库变更

在 `logistics` 表上新增以下字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| provider_code | VARCHAR(32) | 物流供应商代码，如 `SF` |
| provider_type | VARCHAR(32) | `THIRD_PARTY` 或 `LOCAL_SIMULATION` |
| provider_waybill_no | VARCHAR(64) | 第三方真实运单号 |
| provider_status | VARCHAR(32) | 第三方标准化状态 |
| route_sync_mode | VARCHAR(16) | `POLLING` 或 `PUSH` |
| route_sync_status | VARCHAR(16) | `PENDING / SYNCING / SUCCESS / FAILED` |
| last_route_sync_time | DATETIME | 最近路由同步时间 |

**说明：**
- `tracking_no` 继续保留作为项目统一运单号
- 对顺丰单，`tracking_no` 与 `provider_waybill_no` 值相同
- `provider_*` 字段用于隔离第三方平台信息

## 3. 新增代码文件

### 3.1 配置类

- `com.muyingmall.config.SfExpressProperties`
  - 绑定 `sf.express.*` YAML 配置
  - 管理沙箱/生产环境切换参数

### 3.2 抽象接口

- `com.muyingmall.client.ExpressProviderClient`（interface）
  - `createWaybill()` — 创建运单
  - `queryRoutes()` — 查询路由
  - `registerRoutePush()` — 注册路由推送
  - `verifyCallbackSignature()` — 校验回调签名

### 3.3 顺丰实现

- `com.muyingmall.client.sf.SfExpressClient`
  - 统一调顺丰 API
- `com.muyingmall.client.sf.SfSignatureService`
  - 专门负责 `msgDigest` 签名计算
- `com.muyingmall.client.sf.SfRequestFactory`
  - 构建顺丰请求
- `com.muyingmall.client.sf.SfResponseParser`
  - 解析响应与错误

### 3.4 DTO 包

- `com.muyingmall.dto.sf`
  - 请求 DTO（`CreateWaybillCommand`、`QueryRoutesCommand` 等）
  - 响应 DTO（`CreateWaybillResult`、`QueryRoutesResult` 等）
  - 路由节点 DTO
  - 回调 DTO

## 4. 核心业务流程

```
管理员发货（选择顺丰）
  → 校验订单状态 + 幂等性检查
  → SfExpressClient.createWaybill()
      → 构建请求参数（寄件/收件/商品信息）
      → 计算 msgDigest 签名
      → 发送 HTTP POST 到沙箱地址
      → 解析响应，提取运单号
  → 保存 logistics（provider_type = THIRD_PARTY, route_sync_mode = POLLING）
  → 标记 route_sync_status = PENDING
  → 更新订单状态
  → 返回发货成功
```

## 5. 配置项

```yaml
sf:
  express:
    enabled: true
    sandbox-enabled: true
    base-url: https://sfapi-sbox.sf-express.com/std/service
    client-code: ${SF_CLIENT_CODE}
    check-word: ${SF_CHECK_WORD}
    sync-mode: polling
```

**原则：**
- `client-code` 与 `check-word` 不入库，统一放环境变量
- 沙箱/生产仅通过 `sandbox-enabled` 或 `base-url` 切换

## 6. 现有代码改造

### 6.1 AdminOrderController.shipOrder()

- 判断物流公司是否为顺丰
- 顺丰：调第三方下单，保存真实运单号，**不生成本地轨迹**
- 非顺丰：保持现有本地物流逻辑

### 6.2 OrderServiceImpl.shipOrder()

- 解耦物流创建逻辑，`shipOrder()` 只接收已准备好的运单结果
- 移除监听器副作用：第三方物流不走 `OrderPaidEventListener` 自动创建

### 6.3 OrderPaidEventListener

- 增加 `provider_type` 判断，对 `THIRD_PARTY` 类型订单跳过自动创建物流

## 7. 错误处理策略

| 场景 | 处理 |
|------|------|
| 顺丰接口超时 | 发货接口返回明确错误原因，不写本地物流 |
| 鉴权失败 | 记录日志，返回错误，不更新订单状态 |
| 下单失败 | 同上，事务回滚 |
| 幂等性 | 发货前检查 `existingLogistics`，防止重复 |

## 8. 安全设计

- `client-code` 与 `check-word` 仅存环境变量，代码中不硬编码
- 日志打印时对手机号、详细地址做脱敏
- 回调接口未来启用时单独校验签名

## 9. 测试验证点

- [ ] 顺丰签名计算正确
- [ ] 沙箱配置可正确读取
- [ ] 管理员发货成功获取真实运单号
- [ ] 本地订单、物流记录保存成功
- [ ] 非顺丰物流发货不受影响
- [ ] 回归：订单状态流转正常

## 10. 后续阶段预告

- **阶段二**：轮询任务 + 路由同步 + 幂等落库
- **阶段三**：展示升级 + 坐标缓存
- **阶段四**：生产预留 + 回调控制器
- **阶段五**：推送模式升级
