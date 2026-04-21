# Knife4j 文档接口完善 · 设计方案

- 日期：2026-04-21
- 作者：Claude Code（协作）
- 范围层级：**L1 · 只补缺**
- 提交策略：**方案 A · 一次性全补**

## 一、背景与目标

项目基于 `knife4j-openapi3-jakarta-spring-boot-starter 4.3.0` + Spring Boot 3.3.0，
`Knife4jConfig.java` 已提供完整的 OpenAPI 主信息、JWT 安全方案及 41 个 `GroupedOpenApi` 分组。
但 Controller 层注解覆盖不一致：

| 指标 | 总量 | 已覆盖 | 缺口 |
|---|---|---|---|
| Controller 类 | 72 | 58 | **13 个缺 `@Tag`** |
| HTTP 端点 | ≈580 | ≈444 | **≈120 个方法缺 `@Operation`** |

本次目标：**以最小代价把文档"可见性"补齐到 100%**，避免接口面板出现无标签/无摘要项，
不涉及 DTO 字段级 `@Schema` 与 `@ApiResponses` 的统一化（留待未来 L2/L3 任务）。

## 二、设计哲学对齐

| 原则 | 落地 |
|---|---|
| **KISS** | 固定最小模板，不扩展 `@Parameter` / `@ApiResponses`；方法级一行 `summary` 搞定 |
| **YAGNI** | 不触碰已存在但简陋的注解；不为 Test/Notify 设计"隐藏"策略 |
| **SRP** | 文档注解紧贴对应方法，维护就近 |
| **OCP** | 不修改 `Knife4jConfig` 现有分组，保持已有行为稳定 |

## 三、修改范围（精确清单）

### 3.1 类级 `@Tag`（13 个，必补）

**common（6）**
- `AlipayNotifyController` → `@Tag(name = "支付异步回调", description = "支付宝异步通知接收接口")`
- `FileUploadController`   → `@Tag(name = "文件上传", description = "通用文件/图片上传接口")`
- `RefundTestController`   → `@Tag(name = "退款测试接口", description = "退款流程本地测试辅助接口")`
- `StaticResourceController` → `@Tag(name = "静态资源", description = "本地静态资源代理与访问接口")`
- `SystemMonitorController`  → `@Tag(name = "系统监控", description = "运行时健康/指标查看接口")`
- `TestController`           → `@Tag(name = "测试接口", description = "开发期联调用测试接口")`

**user（3）**
- `CircleController`       → `@Tag(name = "用户-圈子社区", description = "孕产社区内容浏览与互动")`
- `ParentingTipController` → `@Tag(name = "用户-育儿知识", description = "育儿知识内容浏览")`
- `RefundController`       → `@Tag(name = "用户-退款管理", description = "用户端退款申请与查询")`

**admin（4）**
- `AdminCircleController`  → `@Tag(name = "后台-圈子管理", description = "后台管理圈子内容与成员")`
- `AdminController`        → `@Tag(name = "后台-登录认证", description = "管理员登录/登出/信息查询")`
- `AdminProfileController` → `@Tag(name = "后台-个人设置", description = "管理员个人资料维护")`
- `SystemController`       → `@Tag(name = "后台-系统管理", description = "系统配置与运维接口")`

### 3.2 方法级 `@Operation`（≈120 处）

按缺失数量排序的热区（见 `Bash` 审计结果）：

| Controller | 缺口 |
|---|---:|
| `CircleController` | 25 |
| `AdminController` | 19 |
| `AdminCircleController` | 18 |
| `StaticResourceController` | 11 |
| `SystemMonitorController` / `AdminRefundController` | 10 / 9 |
| `WalletPaymentController` / `ParentingTipController` | 8 / 8 |
| `RefundController` / `SystemController` | 7 / 7 |
| `TestController` / `RefundTestController` / `AdminProfileController` | 3 × 3 |
| 其他 (PaymentController 等 30+ 文件) | 合计 ≈20，多数仅 1-2 处零星缺口 |

*注：Bash 统计中包含类级 `@RequestMapping`，因此"缺口"数字会略高于实际方法级缺口；实现阶段以逐方法目测为准。*

## 四、注解模板

```java
// 类级（所有新加的类必带）
@Tag(name = "<业务域>", description = "<一句话说明>")

// 方法级（所有新加的方法必带）
@Operation(summary = "<动作+宾语，≤16汉字>")

// 以下属于 L1 范围外，除非方法签名无法自解释，否则不加：
// @ApiResponses(...)
// @Parameter(name = ..., description = ...)
// @Parameters(...)
```

### 命名规范

- `@Tag.name` 格式：端类（`用户-` / `后台-` / 无前缀）+ 业务域
- `@Operation.summary` 以动词开头：获取/查询/新增/修改/删除/上传/下载/导出...
- 尽量匹配现有 `Knife4jConfig` 的中文分组命名风格

## 五、实现顺序（单批次内）

1. **common 包**（6 个类 + ≈35 方法）
2. **user 包**（3 个类 + ≈55 方法）
3. **admin 包**（4 个类 + ≈50 方法）

全部在同一次 `git commit` 内提交，commit message 示例：

```
docs(api): 补齐所有 Controller 的 @Tag 与 @Operation 基础注解
```

## 六、验证策略

1. **编译**：`mvn -o compile` 通过，无 `io.swagger.v3.oas.annotations.*` 导入缺失。
2. **启动**：`mvn spring-boot:run`，等待 `Started MuyingMallApplication` 日志。
3. **UI 抽查**：访问 `http://localhost:8080/api/doc.html`，确认：
   - 13 个新加 `@Tag` 的 Controller 在左侧 tag 列表显示正确中文名；
   - 抽查 CircleController / AdminController 任选 3 个方法，`summary` 显示正常；
   - 原有 58 个 Controller 的 tag 不变。
4. **兜底**：`git diff --stat` 修改文件数应在 **13~30 个** 区间。

## 七、风险与回滚

| 风险 | 概率 | 缓解 |
|---|---|---|
| import 漏加导致编译失败 | 中 | 每个文件首加注解时统一 3 行 import；mvn compile 兜底 |
| Tag 命名与现有分组不一致 | 低 | 严格参考 `Knife4jConfig` 分组命名 |
| 某些方法实际已有更详细注解（被 Bash 计数误杀） | 低 | 实施时逐方法目测，跳过已有 `@Operation` 者 |
| 大 diff 不易 review | 接受 | 本次由用户选定方案 A，已知取舍 |

回滚：`git revert <commit>` 即可，零运行时副作用。

## 八、非目标（显式声明）

- **不** 修改 `Knife4jConfig` 分组配置
- **不** 为 DTO / VO / Entity 字段补 `@Schema`
- **不** 统一已有 `@Operation` 的 `description` 文风
- **不** 为所有方法补 `@ApiResponses` / `@Parameter`
- **不** 隐藏 Test / Notify 类接口（用户明确选"所有"）

以上工作若未来推进至 L2/L3 再开新 plan。
