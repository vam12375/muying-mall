# Knife4j 文档接口完善实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 一次性补齐所有 Controller 的类级 `@Tag` 与方法级 `@Operation`，把 Knife4j API 面板的文档可见性补到 100%。

**Architecture:** 对 13 个存在缺口的 Controller 定点追加 OpenAPI 注解；采用最小模板（仅 `@Tag` + `@Operation(summary=...)`）；不引入 `@ApiResponses` / `@Parameter` / DTO `@Schema`；不动 `Knife4jConfig`。

**Tech Stack:** Spring Boot 3.3.0 + Java 21 + knife4j-openapi3-jakarta-spring-boot-starter 4.3.0，注解来自 `io.swagger.v3.oas.annotations.*`。

---

## TDD 适配说明

Swagger 注解是声明式元数据，**没有对应的单元测试**。本 plan 的「验证」替换为：

- **静态**：`mvn -o compile -DskipTests` 必须 BUILD SUCCESS（验证 import 与注解语法）
- **运行时（仅 Task 5）**：启动应用，浏览器访问 `http://localhost:8080/api/doc.html`，肉眼抽查 3-5 个新注解生效

---

## 精确范围（实施前二次审计的结果）

**缺 `@Tag` 的 5 个 Controller**：
- `common/FileUploadController`
- `common/StaticResourceController`
- `user/CircleController`
- `user/ParentingTipController`
- `admin/AdminCircleController`

**缺 `@Operation` 的方法（共 96 个，按文件）**：

| Controller | 数量 |
|---|---:|
| `user/CircleController` | 25 |
| `admin/AdminController` | 19 |
| `admin/AdminCircleController` | 18 |
| `common/StaticResourceController` | 12 |
| `common/SystemMonitorController` | 9 |
| `admin/AdminRefundController` | 9 |
| `user/WalletPaymentController` | 8 |
| `user/ParentingTipController` | 8 |
| `admin/SystemController` | 7 |
| `user/RefundController` | 6 |
| `common/TestController` | 3 |
| `common/RefundTestController` | 3 |
| `admin/AdminProfileController` | 3 |
| `common/PaymentController` | 2 |
| `common/SessionController` | 1 |
| `common/FileUploadController` | 1 |
| `common/AlipayNotifyController` | 1 |

（审计命令：`grep -cE "^\s*@(Get|Post|Put|Delete|Patch)Mapping" - grep -cE "^\s*@(io\.swagger\.v3\.oas\.annotations\.)?Operation\("`）

---

## 通用规则

1. **注解短名 + import**：每个文件统一使用 `@Tag` / `@Operation` 短名。修改前先检查顶部 import：
   ```java
   import io.swagger.v3.oas.annotations.Operation;
   import io.swagger.v3.oas.annotations.tags.Tag;
   ```
   缺则补齐（保持 import 按字母序，放在其他 `io.swagger.*` 旁）。
2. **位置**：`@Operation` 直接加在 `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping/@PatchMapping` 的**上一行**；`@Tag` 加在 `@RestController` 所在类定义的上方注解块尾部。
3. **文风**：
   - `summary` 动词开头，中文，≤16 汉字
   - 重复性接口用括号补上下文：`获取商品图片（备用路径）`
   - 调试/测试接口在 summary 后加 `（调试）` 或 `（测试）` 以便 UI 区分
4. **不增加**：`@ApiResponses`、`@Parameter`、`@Parameters`、DTO `@Schema`

---

## Task 1: 补齐 common 包（7 个文件）

**Files:**
- Modify: `src/main/java/com/muyingmall/controller/common/FileUploadController.java`
- Modify: `src/main/java/com/muyingmall/controller/common/StaticResourceController.java`
- Modify: `src/main/java/com/muyingmall/controller/common/AlipayNotifyController.java`
- Modify: `src/main/java/com/muyingmall/controller/common/TestController.java`
- Modify: `src/main/java/com/muyingmall/controller/common/RefundTestController.java`
- Modify: `src/main/java/com/muyingmall/controller/common/SystemMonitorController.java`
- Modify: `src/main/java/com/muyingmall/controller/common/PaymentController.java`
- Modify: `src/main/java/com/muyingmall/controller/common/SessionController.java`

### Step 1.1: FileUploadController

在 `public class FileUploadController` 上方加：
```java
@io.swagger.v3.oas.annotations.tags.Tag(name = "文件上传", description = "通用图片上传接口")
```

为 `uploadImage(MultipartFile file)` 加：
```java
@io.swagger.v3.oas.annotations.Operation(summary = "上传图片")
```

（不改 import，直接用全限定名，本文件仅两处新增）

### Step 1.2: StaticResourceController

在 `public class StaticResourceController` 上方加：
```java
@io.swagger.v3.oas.annotations.tags.Tag(name = "静态资源", description = "本地静态文件代理与访问接口")
```

为 12 个 `@GetMapping` 方法加 `@io.swagger.v3.oas.annotations.Operation(summary = "...")`：

| 方法 | summary |
|---|---|
| `testController` | `静态资源控制器连通性测试` |
| `getProductImage` | `获取商品图片` |
| `getProductImageAlt` | `获取商品图片（备用路径）` |
| `getBrandImage` | `获取品牌图片` |
| `getCategoryIcon` | `获取分类图标` |
| `getDetailImage` | `获取商品详情图片` |
| `getBannerImage` | `获取轮播图` |
| `getGeneralImage` | `获取通用图片` |
| `getCircleImage` | `获取育儿圈图片（按日期）` |
| `getAvatarImageStatic` | `获取头像图片` |
| `getAvatarImageWithUserId` | `获取头像图片（带用户ID）` |
| `getAvatarImage` | `获取头像图片（旧路径兼容）` |

### Step 1.3: AlipayNotifyController

已有 `@Tag`。为 `handleRefundNotify` 加：
```java
@io.swagger.v3.oas.annotations.Operation(summary = "接收支付宝退款异步通知")
```

### Step 1.4: TestController

已有 `@Tag`。新增 import：
```java
import io.swagger.v3.oas.annotations.Operation;
```
加注解：
| 方法 | summary |
|---|---|
| `testConnection` | `测试后端连接` |
| `testAuth` | `测试JWT认证` |
| `testJwt` | `生成演示JWT令牌（测试）` |

### Step 1.5: RefundTestController

已有 `@Tag`。新增 import `io.swagger.v3.oas.annotations.Operation`。加注解：
| 方法 | summary |
|---|---|
| `getRefundStatusCount` | `查询各退款状态数量（测试）` |
| `getRefundsByStatus` | `按状态查询退款列表（测试）` |
| `triggerEvent` | `手动触发退款状态事件（测试）` |

### Step 1.6: SystemMonitorController

已有 `@Tag`。新增 import `io.swagger.v3.oas.annotations.Operation`。加注解：
| 方法 | summary |
|---|---|
| `getSystemMetrics` | `获取系统监控指标` |
| `getSystemHealth` | `获取系统健康状态` |
| `getServerPerformance` | `获取服务器性能指标` |
| `getDatabaseMetrics` | `获取数据库监控指标` |
| `getRedisMetrics` | `获取Redis监控指标` |
| `getApiStatistics` | `获取API调用统计` |
| `resetApiStatistics` | `重置API统计数据` |
| `getDatabaseVersion` | `获取数据库版本` |
| `pingRedis` | `检查Redis连接状态` |

### Step 1.7: PaymentController

已有 `@Tag`、已 import `Operation`。补 2 处：
| 方法 | summary |
|---|---|
| `alipayNotify` | `支付宝异步通知` |
| `alipayReturn` | `支付宝同步回调` |

*注意*：其他方法如 `createAlipayPayment` 等已有 `@Operation`，**不要重复添加**。

### Step 1.8: SessionController

已有 `@Tag`、已 import `Operation`。补 `syncSession`：
```java
@Operation(summary = "同步当前会话")
```

### Step 1.9: 验证 common 包

Run: `mvn -o compile -DskipTests -pl . -q`
Expected: BUILD SUCCESS

---

## Task 2: 补齐 user 包（4 个文件）

**Files:**
- Modify: `src/main/java/com/muyingmall/controller/user/CircleController.java`
- Modify: `src/main/java/com/muyingmall/controller/user/ParentingTipController.java`
- Modify: `src/main/java/com/muyingmall/controller/user/RefundController.java`
- Modify: `src/main/java/com/muyingmall/controller/user/WalletPaymentController.java`

### Step 2.1: CircleController

新增 import：
```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
```

类上加：
```java
@Tag(name = "用户-圈子社区", description = "孕产社区内容浏览与互动")
```

为 25 个方法加 `@Operation(summary="...")`：
| 方法 | summary |
|---|---|
| `getTopics` | `获取话题列表` |
| `getHotTopics` | `获取热门话题` |
| `createPost` | `发布帖子` |
| `getPostList` | `分页获取帖子列表` |
| `getFollowingPosts` | `获取关注用户的帖子` |
| `getHotPosts` | `获取热门帖子` |
| `getPostDetail` | `获取帖子详情` |
| `deletePost` | `删除自己的帖子` |
| `createComment` | `发表评论` |
| `getComments` | `分页获取帖子评论` |
| `getReplies` | `分页获取评论回复` |
| `deleteComment` | `删除自己的评论` |
| `togglePostLike` | `点赞/取消点赞帖子` |
| `toggleCommentLike` | `点赞/取消点赞评论` |
| `toggleFollow` | `关注/取消关注用户` |
| `getFollowingList` | `获取用户关注列表` |
| `getFollowerList` | `获取用户粉丝列表` |
| `getUserStats` | `获取关注与粉丝统计` |
| `getUserProfile` | `获取用户社区主页信息` |
| `getUserPosts` | `获取用户发布的帖子` |
| `updatePost` | `编辑帖子` |
| `getMessages` | `分页获取社区消息` |
| `getUnreadCount` | `获取未读消息数量` |
| `markAsRead` | `标记指定消息已读` |
| `markAllAsRead` | `一键标记全部消息已读` |

### Step 2.2: ParentingTipController

新增 import `Operation` 与 `Tag`。

类上加：
```java
@Tag(name = "用户-育儿知识", description = "育儿知识内容浏览与评论")
```

8 个方法：
| 方法 | summary |
|---|---|
| `getList` | `分页查询育儿知识` |
| `getHotTips` | `获取热门育儿知识` |
| `getCategories` | `获取育儿知识分类列表` |
| `getDetail` | `获取育儿知识详情` |
| `increaseViewCount` | `增加浏览量` |
| `getRelatedTips` | `获取相关育儿知识` |
| `getComments` | `分页获取知识评论` |
| `addComment` | `发表育儿知识评论` |

### Step 2.3: RefundController

已有 `@Tag` 与 import。`applyRefund` 已有 `@Operation`，**跳过**。补 6 个：
| 方法 | summary |
|---|---|
| `getRefundDetail` | `获取退款详情` |
| `cancelRefund` | `取消退款申请` |
| `getUserRefunds` | `获取用户退款列表` |
| `getOrderRefunds` | `获取订单退款列表` |
| `getRefundStatusList` | `获取退款状态枚举` |
| `getNextStatuses` | `查询状态可流转的下一步` |

### Step 2.4: WalletPaymentController

已有 `@Tag` 与 import `Operation`。补 8 个：
| 方法 | summary |
|---|---|
| `alipayRechargeNotify` | `支付宝充值异步通知` |
| `alipayRechargeReturn` | `支付宝充值同步回调` |
| `manualCompleteRecharge` | `手动完成充值（调试）` |
| `queryRechargeRecord` | `查询充值记录（调试）` |
| `checkTransactionStatus` | `检查充值交易状态（调试）` |
| `wechatRechargeNotify` | `微信充值异步通知` |
| `queryUserAccount` | `查询用户账户详情（调试）` |
| `createTestRecharge` | `创建测试充值记录（调试）` |

### Step 2.5: 验证 user 包

Run: `mvn -o compile -DskipTests -pl . -q`
Expected: BUILD SUCCESS

---

## Task 3: 补齐 admin 包（5 个文件）

**Files:**
- Modify: `src/main/java/com/muyingmall/controller/admin/AdminController.java`
- Modify: `src/main/java/com/muyingmall/controller/admin/AdminCircleController.java`
- Modify: `src/main/java/com/muyingmall/controller/admin/AdminProfileController.java`
- Modify: `src/main/java/com/muyingmall/controller/admin/AdminRefundController.java`
- Modify: `src/main/java/com/muyingmall/controller/admin/SystemController.java`

### Step 3.1: AdminController

已有 `@Tag`（全限定名）。新增 import `io.swagger.v3.oas.annotations.Operation`。
补 19 个 `@Operation`：
| 方法 | summary |
|---|---|
| `login` | `管理员登录` |
| `getUserInfo` | `获取当前管理员信息` |
| `uploadFile` | `管理端通用文件上传` |
| `uploadAvatar` | `上传管理员头像` |
| `updateAdminInfo` | `更新管理员信息` |
| `updatePassword` | `修改管理员密码` |
| `getLoginRecords` | `分页查询登录记录` |
| `getSystemLogs` | `分页查询系统日志` |
| `getSystemLogDetail` | `查询系统日志详情` |
| `getOperationRecords` | `分页查询操作记录` |
| `getAdminStatistics` | `获取管理员统计信息` |
| `exportLoginRecords` | `导出登录记录` |
| `exportOperationRecords` | `导出操作记录` |
| `getWebSocketStatus` | `获取WebSocket连接状态` |
| `sendSystemNotification` | `发送系统通知` |
| `pushStatsUpdate` | `手动推送统计数据` |
| `getSystemLogStatistics` | `获取系统日志统计` |
| `batchDeleteSystemLogs` | `批量删除系统日志` |
| `clearOldSystemLogs` | `清空历史日志` |

### Step 3.2: AdminCircleController

新增 import `Operation` 与 `Tag`。

类上加：
```java
@Tag(name = "后台-圈子管理", description = "后台管理育儿圈帖子、话题与评论")
```

补 18 个：
| 方法 | summary |
|---|---|
| `getStats` | `获取育儿圈统计数据` |
| `getPostList` | `分页获取帖子列表` |
| `getPostDetail` | `获取帖子详情` |
| `updatePostStatus` | `审核更新帖子状态` |
| `togglePostTop` | `设置/取消帖子置顶` |
| `togglePostHot` | `设置/取消帖子热门` |
| `deletePost` | `管理员删除帖子` |
| `getTopicList` | `分页获取话题列表` |
| `getTopicDetail` | `获取话题详情` |
| `createTopic` | `创建话题` |
| `updateTopic` | `更新话题信息` |
| `updateTopicStatus` | `更新话题状态` |
| `deleteTopic` | `删除话题` |
| `updateTopicSort` | `批量更新话题排序` |
| `getCommentList` | `分页获取评论列表` |
| `updateCommentStatus` | `更新评论状态` |
| `deleteComment` | `管理员删除评论` |
| `batchDeleteComments` | `批量删除评论` |

### Step 3.3: AdminProfileController

已有 `@Tag`（全限定名）。新增 import `io.swagger.v3.oas.annotations.Operation`。
补 3 个：
| 方法 | summary |
|---|---|
| `getStatistics` | `获取管理员个人统计` |
| `getLoginRecords` | `分页查询个人登录记录` |
| `getOperationLogs` | `分页查询个人操作记录` |

### Step 3.4: AdminRefundController

已有 `@Tag`。**检查 import**：当前已 import `io.swagger.v3.oas.annotations.tags.Tag`，需再加：
```java
import io.swagger.v3.oas.annotations.Operation;
```
补 9 个：
| 方法 | summary |
|---|---|
| `getRefunds` | `分页获取退款列表` |
| `getRefundDetail` | `获取退款详情` |
| `reviewRefund` | `审核退款申请` |
| `processRefund` | `处理退款` |
| `completeRefund` | `完成退款` |
| `failRefund` | `标记退款失败` |
| `getRefundStatistics` | `获取退款统计数据` |
| `getPendingRefundCount` | `获取待处理退款数量` |
| `queryAlipayRefundStatus` | `查询支付宝退款状态` |

### Step 3.5: SystemController

已有 `@Tag`（全限定名）。新增 import `io.swagger.v3.oas.annotations.Operation`。
补 7 个：
| 方法 | summary |
|---|---|
| `getRedisInfo` | `获取Redis服务器信息` |
| `getRedisCacheKeys` | `分页获取Redis键列表` |
| `getRedisCacheValue` | `获取Redis指定键的值` |
| `deleteRedisCache` | `删除Redis指定键` |
| `flushRedisCache` | `清空Redis当前库` |
| `clearRedisCache` | `清空Redis当前库（别名）` |
| `refreshRedisStats` | `刷新Redis服务器状态` |

### Step 3.6: 验证 admin 包

Run: `mvn -o compile -DskipTests -pl . -q`
Expected: BUILD SUCCESS

---

## Task 4: 全量编译验证

**Step 4.1: 清洁编译**

Run: `mvn -o clean compile -DskipTests`
Expected: BUILD SUCCESS，无 `package ... does not exist` / 符号找不到等错误

**Step 4.2: 复核缺口闭合**

Run:
```bash
for f in src/main/java/com/muyingmall/controller/common/*.java src/main/java/com/muyingmall/controller/user/*.java src/main/java/com/muyingmall/controller/admin/*.java; do if ! grep -qE "@(io\.swagger\.v3\.oas\.annotations\.tags\.)?Tag\(name" "$f"; then echo "STILL_MISS_TAG: $f"; fi; done
```
Expected: 无输出（全部 Controller 都有 Tag）

Run:
```bash
for f in src/main/java/com/muyingmall/controller/common/*.java src/main/java/com/muyingmall/controller/user/*.java src/main/java/com/muyingmall/controller/admin/*.java; do mc=$(grep -cE "^\s*@(Get|Post|Put|Delete|Patch)Mapping" "$f"); oc=$(grep -cE "^\s*@(io\.swagger\.v3\.oas\.annotations\.)?Operation\(" "$f"); miss=$((mc-oc)); if [ "$miss" -gt 0 ]; then echo "STILL_MISS: $miss  $f"; fi; done
```
Expected: 无输出

如仍有缺口，回到对应 Task 补齐。

---

## Task 5: 运行时 UI 抽查（可选但强烈推荐）

**Step 5.1: 启动应用**

Run: `mvn spring-boot:run`（可后台跑，等日志 `Started MuyingMallApplication`）

**Step 5.2: 浏览器访问**

打开 `http://localhost:8080/api/doc.html`

**Step 5.3: 抽查以下内容**

- 左侧 Tag 列表出现：`用户-圈子社区`、`用户-育儿知识`、`后台-圈子管理`、`文件上传`、`静态资源`
- 展开 `用户-圈子社区`：25 个接口都有 summary 文本（不再显示方法名）
- 展开 `后台-用户管理`（含 AdminController 的 19 个新 summary）
- 原有 `用户管理`（UserController）行为未变

**Step 5.4: 停止应用**

Ctrl+C 或 `mvn spring-boot:stop`

---

## Task 6: 提交

**Step 6.1: 暂存修改**

Run:
```bash
git add src/main/java/com/muyingmall/controller/common src/main/java/com/muyingmall/controller/user src/main/java/com/muyingmall/controller/admin
git status --short
```
Expected: `M src/.../controller/common/...` 共 13 个文件

**Step 6.2: 单次提交**

Run:
```bash
git commit -m "$(cat <<'EOF'
docs(api): 补齐所有 Controller 的 @Tag 与 @Operation 基础注解

- 5 个 Controller 新增类级 @Tag（FileUpload/StaticResource/Circle/ParentingTip/AdminCircle）
- 96 个方法新增 @Operation(summary)
- 仅补缺，不改动已有注解、不添加 DTO @Schema、不动 Knife4jConfig
- 对应设计文档：docs/plans/2026-04-21-knife4j-doc-completion-design.md

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```
Expected: `[master xxxxxxx] docs(api): ...` 13 files changed

**Step 6.3: 确认**

Run: `git log --oneline -3`
Expected: 最新提交为本次文档改动

---

## 回滚

```bash
git revert <commit-sha>
```
零运行时副作用，编译通过即可安全回滚。

---

## 非目标（显式声明）

- **不** 补 `@ApiResponses` / `@Parameter` / `@Parameters`
- **不** 为 DTO / VO / Entity 字段补 `@Schema`
- **不** 统一已有 `@Operation` 的文风（如 UserController 中丰富的 `@ApiResponses` 保持原样）
- **不** 修改 `Knife4jConfig` 现有分组
- **不** 隐藏测试/Notify 类接口

这些若未来要做，开新 plan（docs/plans/...-knife4j-L2-design.md）。
