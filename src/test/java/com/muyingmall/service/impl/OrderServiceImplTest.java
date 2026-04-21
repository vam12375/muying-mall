package com.muyingmall.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.OrderProduct;
import com.muyingmall.fixtures.OrderFixtures;
import com.muyingmall.mapper.CartMapper;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.OrderProductMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.mapper.SeckillOrderMapper;
import com.muyingmall.mapper.UserAddressMapper;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.AddressService;
import com.muyingmall.service.BatchQueryService;
import com.muyingmall.service.CouponService;
import com.muyingmall.service.MessageProducerService;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.PointsService;
import com.muyingmall.service.ProductService;
import com.muyingmall.service.ProductSkuService;
import com.muyingmall.service.SeckillOrderReleaseService;
import com.muyingmall.service.UserCouponService;
import com.muyingmall.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 订单服务（查询路径）· 单元测试。
 * 被测方法：{@link OrderServiceImpl#getOrderDetail(Integer, Integer)}
 * 覆盖：
 *   - 黄金路径：orderId != null 且归属匹配 → 返回带商品列表的订单并写缓存
 *   - 缓存命中且 userId 不匹配 → 抛 BusinessException
 *   - orderId 为 null → 返回 null
 *   - 数据库中不存在 → 返回 null
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
@DisplayName("订单服务 · 查询路径单元测试")
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserAddressMapper addressMapper;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private OrderProductMapper orderProductMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ProductService productService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private PointsService pointsService;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CouponService couponService;
    @Mock
    private UserCouponService userCouponService;
    @Mock
    private MessageProducerService messageProducerService;
    @Mock
    private ProductSkuService productSkuService;
    @Mock
    private BatchQueryService batchQueryService;
    @Mock
    private SeckillOrderMapper seckillOrderMapper;
    @Mock
    private SeckillOrderReleaseService seckillOrderReleaseService;
    @Mock
    private AddressService addressService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        // MyBatis-Plus ServiceImpl 的 baseMapper 由 Spring 注入，单元测试通过反射注入。
        ReflectionTestUtils.setField(orderService, "baseMapper", orderMapper);
    }

    @Nested
    @DisplayName("getOrderDetail(orderId, userId)")
    class GetOrderDetail {

        @Test
        @DisplayName("orderId 为 null 时直接返回 null")
        void getOrderDetail_shouldReturnNull_whenOrderIdIsNull() {
            assertThat(orderService.getOrderDetail(null, 9)).isNull();
        }

        @Test
        @DisplayName("缓存未命中且订单归属匹配时应返回订单并写缓存")
        void getOrderDetail_shouldReturnOrder_whenOwnerMatches() {
            // Given
            Order order = OrderFixtures.pendingPayment(1001, 9, new BigDecimal("100"));
            given(redisUtil.get(anyString())).willReturn(null);
            given(orderMapper.selectById(1001)).willReturn(order);
            List<OrderProduct> products = Collections.singletonList(new OrderProduct());
            given(orderProductMapper.selectList(any())).willReturn(products);

            // When
            Order result = orderService.getOrderDetail(1001, 9);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(1001);
            assertThat(result.getProducts()).hasSize(1);
            // 验证缓存写入
            verify(redisUtil).set(anyString(), eq(order), anyLong());
        }

        @Test
        @DisplayName("缓存命中且 userId 与订单所有者不一致时应抛 BusinessException")
        void getOrderDetail_shouldThrow_whenUserIdMismatch() {
            // Given
            Order cachedOrder = OrderFixtures.pendingPayment(1001, 9, new BigDecimal("100"));
            given(redisUtil.get(anyString())).willReturn(cachedOrder);

            // When / Then
            assertThatThrownBy(() -> orderService.getOrderDetail(1001, 999))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权查看");
        }

        @Test
        @DisplayName("数据库查不到订单时返回 null")
        void getOrderDetail_shouldReturnNull_whenOrderNotFound() {
            given(redisUtil.get(anyString())).willReturn(null);
            given(orderMapper.selectById(9999)).willReturn(null);

            assertThat(orderService.getOrderDetail(9999, 9)).isNull();
        }
    }
}
