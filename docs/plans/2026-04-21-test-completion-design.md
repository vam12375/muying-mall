# 核心 Service 单元测试补全 · 设计方案

- 日期：2026-04-21
- 范围：12 个核心 Service，每个黄金路径 + 1 边界 = 约 24 个 `@Test`（考虑 `@Nested` 分组后实际编写 26-30 个）
- 风格：**大厂标准** —— JUnit 5 + Mockito 5 + AssertJ + `@Nested` / `@DisplayName` / Given-When-Then / `@ArgumentCaptor`
- 提交策略：按业务链路 **3 批提交**

## 一、背景

仓库目前仅有 3 个秒杀相关单元测试。本次补全聚焦 12 个核心业务 Service，建立可长期维护的测试基线，
为后续 L2/L3 扩展（Controller 层、集成测试）留出空间。

## 二、设计哲学对齐

| 原则 | 落地 |
|---|---|
| **KISS** | 纯单元测试，不启动 Spring 容器，不引入 Testcontainers |
| **YAGNI** | 不做 62 个非核心 Service、不做 Controller/Mapper 层、不做集成测试 |
| **SRP** | 每个测试方法只断言一个行为（一条规则） |
| **ISP** | 被测 Service 的依赖全部 Mock，避免测试耦合底层实现细节 |
| **OCP** | 共享夹具（`TestFixtures`）封装构造细节，新增字段不破坏既有测试 |

## 三、风格规约（大厂标准）

### 3.1 必备元素

```java
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("<中文业务域名> · 单元测试")
class XxxServiceImplTest {
    @Mock Dep1 dep1;
    @Mock Dep2 dep2;
    private XxxServiceImpl service;

    @BeforeEach void setUp() {
        service = new XxxServiceImpl(dep1, dep2);
    }

    @Nested
    @DisplayName("methodA() · 业务语义")
    class MethodA {
        @Test
        @DisplayName("<中文场景描述>")
        void methodA_shouldDoXxx_whenYyy() {
            // Given
            ...
            given(dep1.foo()).willReturn(...);

            // When
            var result = service.methodA(...);

            // Then
            assertThat(result)....;
            verify(dep2).bar(...);
        }
    }
}
```

### 3.2 规范清单

| 项 | 要求 |
|---|---|
| 断言库 | **AssertJ**（Spring Boot Test 自带，无需改 pom）—— `assertThat(...)` 流畅 API |
| 异常断言 | `assertThatThrownBy(() -> ...).isInstanceOf(BusinessException.class).hasMessageContaining("...")` |
| 参数捕获 | `ArgumentCaptor<T>` 验证写入实体字段（insert/update 场景必用） |
| Given/When/Then | 三段注释必写，visual 分段 |
| 测试命名 | 方法名用 `verb_shouldXxx_whenYyy`；`@DisplayName` 补中文描述 |
| 分组 | 被测方法 ≥2 时使用 `@Nested` 子类 |
| Stubbing | 用 BDDMockito 风格 `given(...).willReturn(...)`；验证用 `verify(...)` |
| 数据构造 | 不 new 实体，使用 `src/test/java/com/muyingmall/fixtures/*Fixtures` 静态工厂 |
| 不必要 `@Spy` / `PowerMockito` / 反射 | 除非被测类用了 `@PostConstruct` 需要注入字段，用 `ReflectionTestUtils.setField`（秒杀已有先例） |
| Spring 容器 | **不启动**（不 `@SpringBootTest`、不 `@AutoConfigureXxx`） |

### 3.3 TestFixtures 模块

**路径**：`src/test/java/com/muyingmall/fixtures/`

**最小必要 Fixtures（按需增）**：
- `OrderFixtures.paidOrder(Long id, BigDecimal amount)` / `pendingPaymentOrder(...)` / `shippedOrder(...)`
- `PaymentFixtures.pending(...)` / `success(...)`
- `UserFixtures.regular(...)` / `banned(...)`
- `UserAccountFixtures.withBalance(BigDecimal)`
- `CouponFixtures.active(...)` / `expired(...)` / `usedUp(...)`
- `RefundFixtures.pending(...)` / `approved(...)`

## 四、范围（12 个 Service × 2 场景 ≈ 24 测试）

| # | Service | 黄金路径 | 边界 | 所属批次 |
|--:|---|---|---|:-:|
| 1 | `OrderServiceImpl` | 下单时扣库存并创建订单 | 库存不足抛 `BusinessException` | A |
| 2 | `PaymentServiceImpl` | 创建支付记录，状态 PENDING | 重复支付同一订单被拒绝 | A |
| 3 | `RefundServiceImpl` | 申请退款写入 pending 记录 | 退款金额超订单实付金额被拒 | A |
| 4 | `OrderStateServiceImpl` | 合法状态转移 PENDING→PAID | 非法状态转移（PAID→PENDING）被拒 | A |
| 5 | `PaymentStateServiceImpl` | PENDING→SUCCESS | SUCCESS→PENDING 被拒 | A |
| 6 | `RefundStateServiceImpl` | pending→approved | pending→completed 跳跃拒绝 | A |
| 7 | `UserAccountServiceImpl` | 钱包扣款成功写交易 | 余额不足被拒 | B |
| 8 | `UserServiceImpl` | 注册成功写库返回用户 | 用户名冲突 | B |
| 9 | `CartServiceImpl` | 加入购物车聚合同 SKU 数量 | 超过上限拒绝 | B |
| 10 | `CouponServiceImpl` | 领券成功（在有效期） | 过期券领取拒绝 | C |
| 11 | `UserCouponServiceImpl` | 使用券成功（状态变 USED） | 重复使用同一券被拒 | C |
| 12 | `PointsServiceImpl` | 积分增加写流水 | 积分不足扣减被拒 | C |

## 五、Mock 策略

- Mapper / 外部 Service 全部 `@Mock`
- Redis/Alipay/RabbitMQ 通过 `@Mock RedisTemplate` / `@Mock AlipayClient` 等直接打桩
- `@ArgumentCaptor` 在"写入操作需要校验字段"的场景强制使用
- 不使用静态方法 mock（若遇到静态工具类，重构为实例 bean 或可注入组件——属于测试外任务，此次遇到则跳过该场景）

## 六、提交节奏

| Batch | Service | 命令 | commit 消息（模板） |
|---|---|---|---|
| **A · 订单支付链** | 1-6 | `mvn -o test -Dtest='Order*Test,Payment*Test,Refund*Test,*StateServiceImpl*Test'` | `test(core): 补齐订单/支付/退款 Service 单元测试` |
| **B · 账户用户** | 7-9 | `mvn -o test -Dtest='UserAccount*Test,UserServiceImplTest,CartServiceImplTest'` | `test(core): 补齐账户/用户/购物车 Service 单元测试` |
| **C · 优惠激励** | 10-12 | `mvn -o test -Dtest='Coupon*Test,UserCouponServiceImplTest,PointsServiceImplTest'` | `test(core): 补齐优惠券/积分 Service 单元测试` |

**最终收尾**：`mvn -o test`（含 3 个旧秒杀测试）全绿，等同通过。

## 七、风险与取舍

| 风险 | 概率 | 缓解 |
|---|---|---|
| 被测 Service 构造函数注入依赖过多 | 中 | 用 `@InjectMocks` 替代手工 `new`；或按需 `ReflectionTestUtils` 注入 |
| 存在 `@PostConstruct` 预热 | 中 | 参照 `SeckillServiceImplTest` 用 `ReflectionTestUtils.setField` |
| 依赖静态工具类（`JwtUtils.xxx`） | 中 | 跳过该分支场景，选其它黄金路径；不引入 mockito-inline |
| 真实实现依赖 `MyBatis-Plus` 的 `ServiceImpl` 父类方法（如 `lambdaQuery()`） | 中 | 通过构造时传 Mock Mapper 后 `super.baseMapper` 自然可用；无法 stub 的链式调用降级为 `@Spy` 或改用真实 Mapper Mock |
| `AssertJ` 不在 test classpath 中 | 低 | Spring Boot 3 `spring-boot-starter-test` 默认包含 `assertj-core`，先 `mvn dependency:tree` 核查 |
| 测试耗时膨胀 | 低 | 纯 Mock 无 I/O，单个 class 通常 < 200ms |

## 八、非目标

- **不** 写 Controller 层测试（`@WebMvcTest`/MockMvc/RestAssured）
- **不** 写 Mapper 层测试（`@MybatisTest`/H2/Testcontainers）
- **不** 写集成测试（`@SpringBootTest`）
- **不** 回补其余 62 个非核心 Service impl
- **不** 引入 Jacoco 覆盖率报告（另开 plan）
- **不** 修改被测 Service 的生产代码（除非测试暴露了真实 bug，届时单独 commit）

以上若未来推进至 L2/L3 再开新 plan。
