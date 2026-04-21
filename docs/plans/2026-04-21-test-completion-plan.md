# 核心 Service 单元测试补全实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为 12 个核心 Service 新增 24~30 个符合大厂标准的单元测试，每个 Service 覆盖黄金路径 + 1 条关键边界。

**Architecture:** 纯 JUnit 5 + Mockito 5 + AssertJ 单元测试，不启动 Spring 容器；被测对象通过 `@InjectMocks` 自动装配，所有 Mapper/外部 Service 以 `@Mock` 注入；通过 `ArgumentCaptor` 验证写入字段；公共数据构造集中在 `fixtures/*Fixtures` 静态工厂。

**Tech Stack:** JUnit 5.10 + Mockito 5 + AssertJ（Spring Boot Test 自带）+ BDDMockito + `@Nested` / `@DisplayName`

---

## 全局约定

### 必备注解与骨架

```java
package com.muyingmall.service.impl;

import com.muyingmall.fixtures.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("<中文业务域名> · 单元测试")
class XxxServiceImplTest {
    @Mock Dep1 dep1;
    @Mock Dep2 dep2;
    @InjectMocks XxxServiceImpl service;

    @Nested
    @DisplayName("methodA() · 业务语义")
    class MethodA {
        @Test
        @DisplayName("条件 A 下应当 B")
        void methodA_shouldB_whenA() {
            // Given
            given(dep1.foo(anyLong())).willReturn(...);

            // When
            var result = service.methodA(...);

            // Then
            assertThat(result)....;
            ArgumentCaptor<Xxx> captor = ArgumentCaptor.forClass(Xxx.class);
            verify(dep2).persist(captor.capture());
            assertThat(captor.getValue().getField()).isEqualTo(...);
        }
    }
}
```

### 关键规则

1. **`@InjectMocks` 为主**：12 个 Service 统一用 `@InjectMocks`（大厂标准、boilerplate 最少）。若被测类继承 `ServiceImpl<M, T>`，`@InjectMocks` 会自动把 `@Mock M` 注入到父类 `baseMapper` 字段。
2. **`@PostConstruct` 预热字段**：用 `ReflectionTestUtils.setField(service, "fieldName", mock)` 补注入（参照 `SeckillServiceImplTest`）。
3. **不 mock 静态工具**：`SecurityContextHolder.getContext()`、`SpringContextUtil.getBean()` 等依赖**不要**用 mockito-inline；改选其他入口方法做测试。
4. **不测 DB / Redis 真实交互**：仅 Mock `Mapper.xxx()` / `RedisUtil.xxx()` / `RedisTemplate.xxx()` 的返回值。
5. **异常断言统一**：`assertThatThrownBy(() -> service.xxx()).isInstanceOf(BusinessException.class).hasMessageContaining("...")`
6. **参数断言用 `ArgumentCaptor`**：对 insert/update 的实体字段值要有断言，**不要**只 `verify(mapper).insert(any())`。

---

## Task 0: 建立公共测试夹具（TestFixtures）

**Files:**
- Create: `src/test/java/com/muyingmall/fixtures/OrderFixtures.java`
- Create: `src/test/java/com/muyingmall/fixtures/PaymentFixtures.java`
- Create: `src/test/java/com/muyingmall/fixtures/UserFixtures.java`
- Create: `src/test/java/com/muyingmall/fixtures/UserAccountFixtures.java`
- Create: `src/test/java/com/muyingmall/fixtures/CouponFixtures.java`
- Create: `src/test/java/com/muyingmall/fixtures/RefundFixtures.java`

**Step 0.1: 写 OrderFixtures**

```java
package com.muyingmall.fixtures;

import com.muyingmall.entity.Order;
import com.muyingmall.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class OrderFixtures {
    private OrderFixtures() {}

    public static Order pendingPayment(Integer orderId, Integer userId, BigDecimal amount) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderNo("ORD" + orderId);
        order.setUserId(userId);
        order.setActualAmount(amount);
        order.setTotalAmount(amount);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCreateTime(LocalDateTime.now());
        return order;
    }

    public static Order paid(Integer orderId, Integer userId, BigDecimal amount) {
        Order order = pendingPayment(orderId, userId, amount);
        order.setStatus(OrderStatus.PENDING_SHIPMENT);
        order.setPaymentMethod("alipay");
        return order;
    }
}
```

**Step 0.2-0.6: 写另 5 个 Fixtures**（同样模式，按需最小字段）

**Step 0.7: 编译验证**

Run: `mvn -o test-compile -q`
Expected: BUILD SUCCESS

**Step 0.8: 本 Task 无独立 commit**（与 Batch A 首个测试一起提交）

---

## Batch A · 订单支付链（6 个 Service）

### Task A1: `OrderStateServiceImplTest`（状态机最简单，先攻）

**Files:**
- Read first: `src/main/java/com/muyingmall/service/impl/OrderStateServiceImpl.java`
- Create: `src/test/java/com/muyingmall/service/impl/OrderStateServiceImplTest.java`

**Mock 依赖：** `OrderService, OrderMapper, OrderStateMachine, OrderStateLogService, ApplicationEventPublisher`

**被测方法：** `sendEvent(Integer orderId, OrderEvent event, ...)`（查源码确认签名）

**测试方法：**
- `sendEvent_shouldTransitionStatus_whenEventAllowed`（黄金）：当前 PENDING_PAYMENT，事件 PAY，应转到 PENDING_SHIPMENT，写状态日志
- `sendEvent_shouldRejectTransition_whenEventNotAllowed`（边界）：当前 PENDING_SHIPMENT，事件 PAY，应拒绝（返回 false 或抛异常，依源码）

**Step A1.1: 读源码 5 分钟** - 确认 `sendEvent` 返回值、是否抛异常

**Step A1.2: 写测试类骨架**

```java
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("订单状态机 · 单元测试")
class OrderStateServiceImplTest {
    @Mock OrderService orderService;
    @Mock OrderMapper orderMapper;
    @Mock OrderStateMachine orderStateMachine;
    @Mock OrderStateLogService orderStateLogService;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks OrderStateServiceImpl stateService;

    @Nested
    @DisplayName("sendEvent() · 订单状态流转")
    class SendEvent {
        @Test
        @DisplayName("合法事件 PAY 应将 PENDING_PAYMENT 流转到 PENDING_SHIPMENT")
        void sendEvent_shouldTransitionStatus_whenEventAllowed() {
            // Given
            Order order = OrderFixtures.pendingPayment(1001, 9, new BigDecimal("100"));
            given(orderService.getById(1001)).willReturn(order);
            given(orderStateMachine.sendEvent(order, OrderEvent.PAY))
                    .willReturn(OrderStatus.PENDING_SHIPMENT);

            // When
            boolean success = stateService.sendEvent(1001, OrderEvent.PAY, "user", "u1", 9, "支付");

            // Then
            assertThat(success).isTrue();
            verify(orderStateLogService).recordLog(eq(1001), any(), any(), eq("user"), eq("u1"), eq(9), eq("支付"));
        }

        @Test
        @DisplayName("非法事件在已发货状态下应拒绝")
        void sendEvent_shouldRejectTransition_whenEventNotAllowed() {
            // Given
            Order order = OrderFixtures.paid(1001, 9, new BigDecimal("100"));
            order.setStatus(OrderStatus.SHIPPED);
            given(orderService.getById(1001)).willReturn(order);
            given(orderStateMachine.sendEvent(order, OrderEvent.PAY)).willReturn(null);

            // When
            boolean success = stateService.sendEvent(1001, OrderEvent.PAY, "user", "u1", 9, "重复支付");

            // Then
            assertThat(success).isFalse();
            verify(orderStateLogService, never()).recordLog(any(), any(), any(), any(), any(), any(), any());
        }
    }
}
```

**Step A1.3: 运行**

Run: `mvn -o test -Dtest=OrderStateServiceImplTest -q`
Expected: PASS（2/2 绿）

**如失败：** 调整 mock 返回值匹配真实方法签名，或按源码契约调整断言。不要动生产代码。

---

### Task A2: `PaymentStateServiceImplTest`

**Mock:** `PaymentService, PaymentStateMachine, PaymentStateLogService, ApplicationEventPublisher`

**测试方法：**
- `sendEvent_shouldTransitionStatus_whenEventAllowed`：PENDING → SUCCESS
- `sendEvent_shouldRejectTransition_whenEventNotAllowed`：SUCCESS → PENDING 被拒

**代码结构同 A1，替换实体为 Payment、状态为 PaymentStatus。**

Run: `mvn -o test -Dtest=PaymentStateServiceImplTest -q`

---

### Task A3: `RefundStateServiceImplTest`

**Mock:** `RefundService, RefundLogService, OrderService, RefundStateMachine, OrderStateMachine, ApplicationEventPublisher`（加 `@Autowired` 注入的）

**测试方法：**
- `sendEvent_shouldTransitionStatus_whenEventAllowed`：pending → approved
- `sendEvent_shouldRejectTransition_whenEventNotAllowed`：pending → completed（跳跃）

---

### Task A4: `OrderServiceImplTest`（依赖最多，选简单 query 方法）

**Mock:** `UserMapper, UserAddressMapper, CartMapper, OrderProductMapper, ProductMapper, ProductService, PaymentService, ApplicationEventPublisher, PointsService, RedisUtil, RedisTemplate, ObjectMapper, CouponService, UserCouponService, MessageProducerService, ProductSkuService, BatchQueryService, SeckillOrderMapper, SeckillOrderReleaseService, AddressService`

（用 `@InjectMocks` 自动装配，避免 19 个手写参数。）

**被测方法建议：** `getOrderDetail(Integer orderId, Integer userId)` 或 `cancelOrder(...)`（读源码选依赖最少的）

**测试方法：**
- 黄金：`getOrderDetail_shouldReturnOrder_whenOwnerMatches`
- 边界：`getOrderDetail_shouldThrow_whenUserIdMismatch`（或返回 null，按源码）

**提示：** 若目标方法调用 `this.getById()`（MyBatis-Plus 父类），需要 `@Mock OrderMapper orderMapper`（`@InjectMocks` 会注入到父类 `baseMapper`）。`given(orderMapper.selectById(eq(1001))).willReturn(order);`

---

### Task A5: `PaymentServiceImplTest`

**Mock:** `PaymentMapper（父类 baseMapper）, MessageProducerService`

**被测方法：**
- 黄金：`createPayment(Payment)` → 调用 `this.save()` → `baseMapper.insert()`
- 边界：`getByPaymentNo(String)` 不存在时返回 null（`this.getOne()` → `baseMapper.selectOne()`）

**关键：** `@InjectMocks` 会把 `@Mock PaymentMapper paymentMapper` 注入父类 `baseMapper`。stub `paymentMapper.insert(any())` 返回 1。

---

### Task A6: `RefundServiceImplTest`

**Mock:** 10 个依赖（同源码 `@RequiredArgsConstructor` 字段）+ `RefundMapper`（父类 baseMapper）

**被测方法：** `applyRefund(Integer orderId, Integer userId, BigDecimal amount, String reason, String reasonDetail, String evidenceImages)`

**测试方法：**
- 黄金：订单金额充足 + pending 状态 → 创建退款 pending 记录 + 返回 ID
- 边界：退款金额 > 订单实付 → 抛 `BusinessException`

**用 `ArgumentCaptor<Refund>`** 验证 `refundMapper.insert(...)` 写入字段（金额、状态、userId）。

**Step A6.x: Batch A commit**

当 A1-A6 全部 PASS：

Run:
```bash
git add src/test/java/com/muyingmall/fixtures src/test/java/com/muyingmall/service/impl/OrderStateServiceImplTest.java src/test/java/com/muyingmall/service/impl/PaymentStateServiceImplTest.java src/test/java/com/muyingmall/service/impl/RefundStateServiceImplTest.java src/test/java/com/muyingmall/service/impl/OrderServiceImplTest.java src/test/java/com/muyingmall/service/impl/PaymentServiceImplTest.java src/test/java/com/muyingmall/service/impl/RefundServiceImplTest.java
mvn -o test -Dtest='Order*Test,Payment*Test,Refund*Test,*StateServiceImpl*Test' -q
git commit -m "$(cat <<'EOF'
test(core): 补齐订单/支付/退款 Service 单元测试 (Batch A)

- OrderStateServiceImpl / PaymentStateServiceImpl / RefundStateServiceImpl：状态机合法/非法转移
- OrderServiceImpl：查询黄金路径 + 鉴权边界
- PaymentServiceImpl：createPayment / getByPaymentNo
- RefundServiceImpl：applyRefund 黄金路径 + 金额超限边界
- 新增 fixtures 模块：OrderFixtures/PaymentFixtures/RefundFixtures 等

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Batch B · 账户用户（3 个 Service）

### Task B1: `UserAccountServiceImplTest`

**Mock:** `UserAccountMapper（@Autowired 字段）, AccountTransactionMapper（@Autowired 字段）, UserMapper（@Autowired 字段）, AlipayConfig, RedisUtil`

（`@InjectMocks` 支持字段注入 + 构造注入混合）

**被测方法：** `payOrderByWallet(Integer userId, Integer orderId, BigDecimal amount)` 或 `getUserAccountByUserId(Integer)`（读源码选最独立的）

**测试方法：**
- 黄金：余额充足 → 扣款成功 + 写交易记录（`ArgumentCaptor<AccountTransaction>` 断言 type=2 消费、金额、userId）
- 边界：余额不足 → 返回 false 或抛异常

### Task B2: `UserServiceImplTest`

**Mock:** 11+ 依赖 + `UserMapper`（父类 baseMapper）+ `PasswordEncoder, JwtUtils`

**被测方法：** `findByUsername(String)` 或 `register(UserDTO)`

- 黄金：`findByUsername` 查到用户 → 返回
- 边界：用户名不存在 → 返回 null

（register 依赖链过长，此处选 query 方法为安全；若读源码发现 register 可测，择优）

### Task B3: `CartServiceImplTest`

**Mock:** `CartMapper（父类 baseMapper）, ProductService, ObjectMapper, RedisUtil, RedisTemplate`

**被测方法：** `addToCart(...)` 或 `listCart(Integer userId)`

- 黄金：新增购物车项，同 SKU 存在则数量累加
- 边界：数量超过上限 → 抛异常或返回失败

**Batch B commit:**
```bash
mvn -o test -Dtest='UserAccount*Test,UserServiceImplTest,CartServiceImplTest' -q
git add src/test/java/com/muyingmall/service/impl/UserAccountServiceImplTest.java src/test/java/com/muyingmall/service/impl/UserServiceImplTest.java src/test/java/com/muyingmall/service/impl/CartServiceImplTest.java
git commit -m "test(core): 补齐账户/用户/购物车 Service 单元测试 (Batch B)"
```

---

## Batch C · 优惠激励（3 个 Service）

### Task C1: `CouponServiceImplTest`

**Mock:** `CouponMapper（父类 baseMapper）, UserCouponMapper, ProductMapper, RedisUtil`

**被测方法：** `receiveCoupon(Integer userId, Integer couponId)` 或 `listAvailableCoupons(...)`

- 黄金：在有效期内的券领取成功 → 写 `UserCoupon`
- 边界：过期券 → 抛异常

### Task C2: `UserCouponServiceImplTest`

**Mock:** `UserCouponMapper（父类 baseMapper）, RedisUtil`

**被测方法：** `useCoupon(Integer userCouponId)` 或 `listUserCoupons(Integer userId)`

- 黄金：使用 UNUSED 券 → 状态变 USED
- 边界：重复使用 USED 券 → 抛异常

### Task C3: `PointsServiceImplTest`

**Mock:** 11 个依赖 + `UserPointsMapper`（父类 baseMapper）

**被测方法：** `addPoints(Integer userId, Integer points, String source)` 或 `deductPoints(...)`

- 黄金：增加积分并写 history
- 边界：deductPoints 积分不足 → 抛异常

**Batch C commit:**
```bash
mvn -o test -Dtest='Coupon*Test,UserCouponServiceImplTest,PointsServiceImplTest' -q
git add src/test/java/com/muyingmall/service/impl/CouponServiceImplTest.java src/test/java/com/muyingmall/service/impl/UserCouponServiceImplTest.java src/test/java/com/muyingmall/service/impl/PointsServiceImplTest.java
git commit -m "test(core): 补齐优惠券/积分 Service 单元测试 (Batch C)"
```

---

## Task 4: 全量验证

Run: `mvn -o test -q 2>&1 | tail -30`
Expected:
```
[INFO] Tests run: 29, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

（3 个旧秒杀测试 + 新增 24+ 个测试）

---

## Task 5: 风险应对指南

| 症状 | 可能原因 | 行动 |
|---|---|---|
| `UnnecessaryStubbingException` | `when(...)` 未被用到 | 删 stub 或加 `@MockitoSettings(strictness = Strictness.LENIENT)` |
| `@InjectMocks` 未注入任何依赖 | 字段名不匹配 | 检查 Mock 名称与被测字段对齐；若父类 baseMapper 未注入，改为 `ReflectionTestUtils.setField(service, "baseMapper", mapper)` |
| `NullPointerException` in SUT | 被测方法调用了未 stub 的依赖返回 null | 补 `given(...).willReturn(...)` |
| 方法签名变化 | 源码更新了 | 读最新源码更新 mock 与断言 |
| 被测方法用 `SecurityContextHolder.getContext()` | 测试调用得不到 Authentication | 换一个不依赖 SecurityContext 的 public 方法；或跳过该场景 |
| 被测方法调用静态工具（如 `EnumUtil.xxx`） | 无需 mock，直接让其真实运行 | 不处理 |
| `ServiceImpl.lambdaQuery()` 链式调用 | 难 Mock | 选不调用 lambdaQuery 的方法；或改为直接调用 `baseMapper.selectXxx` 的测试目标方法 |

---

## 非目标（再次声明）

- 不写 Controller 测试、Mapper 测试、集成测试
- 不补其余 62 个 Service impl
- 不修改生产代码（除非测试暴露真实 bug，另行 commit）
- 不引入 Jacoco / Testcontainers / WireMock / mockito-inline

---

## 回滚

所有新增测试文件为纯新增，回滚只需：
```bash
git revert <batch-a-sha> <batch-b-sha> <batch-c-sha>
# 或
rm -rf src/test/java/com/muyingmall/fixtures
rm src/test/java/com/muyingmall/service/impl/{Order,Payment,Refund,User,Cart,Coupon,UserCoupon,Points}*Test.java
rm src/test/java/com/muyingmall/service/impl/*StateServiceImpl*Test.java
```
