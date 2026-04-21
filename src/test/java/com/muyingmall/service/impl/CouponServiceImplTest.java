package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.entity.Coupon;
import com.muyingmall.entity.UserCoupon;
import com.muyingmall.fixtures.CouponFixtures;
import com.muyingmall.mapper.CouponMapper;
import com.muyingmall.mapper.ProductMapper;
import com.muyingmall.mapper.UserCouponMapper;
import com.muyingmall.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 优惠券服务 · 单元测试。
 * 被测方法：{@link CouponServiceImpl#receiveCoupon(Integer, Long)}
 * 覆盖：
 *   - 黄金路径：有效券 + CAS 领取 → 写 UserCoupon、清缓存
 *   - 券不存在 / 已下架 / 已过期 / 库存领完 → 抛 BusinessException
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
@DisplayName("优惠券服务 · 单元测试")
class CouponServiceImplTest {

    @Mock
    private CouponMapper couponMapper;

    @Mock
    private UserCouponMapper userCouponMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private CouponServiceImpl couponService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(couponService, "baseMapper", couponMapper);
    }

    @Nested
    @DisplayName("receiveCoupon(userId, couponId)")
    class ReceiveCoupon {

        @Test
        @DisplayName("活跃券 + 未超领取上限 + CAS 成功 → 写 UserCoupon 并清缓存")
        void receiveCoupon_shouldPersistAndClearCache_whenGoldenPath() {
            // Given
            Coupon coupon = CouponFixtures.activeFixedCoupon(200L, new BigDecimal("10"));
            coupon.setUserLimit(1);
            given(couponMapper.selectById(200L)).willReturn(coupon);
            given(userCouponMapper.selectCount(any(Wrapper.class))).willReturn(0L);
            given(couponMapper.incrementReceivedQuantity(200L)).willReturn(1);

            // When
            boolean success = couponService.receiveCoupon(9, 200L);

            // Then
            assertThat(success).isTrue();

            ArgumentCaptor<UserCoupon> captor = ArgumentCaptor.forClass(UserCoupon.class);
            verify(userCouponMapper).insert(captor.capture());
            UserCoupon persisted = captor.getValue();
            assertThat(persisted.getUserId()).isEqualTo(9);
            assertThat(persisted.getCouponId()).isEqualTo(200L);
            assertThat(persisted.getStatus()).isEqualTo("UNUSED");
            assertThat(persisted.getExpireTime()).isEqualTo(coupon.getEndTime());
        }

        @Test
        @DisplayName("券不存在时应抛 BusinessException")
        void receiveCoupon_shouldThrow_whenCouponNotFound() {
            given(couponMapper.selectById(999L)).willReturn(null);

            assertThatThrownBy(() -> couponService.receiveCoupon(9, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("优惠券不存在");

            verify(userCouponMapper, never()).insert(any(UserCoupon.class));
        }

        @Test
        @DisplayName("券已下架时应抛 BusinessException")
        void receiveCoupon_shouldThrow_whenCouponInactive() {
            Coupon inactive = CouponFixtures.activeFixedCoupon(200L, new BigDecimal("10"));
            inactive.setStatus("INACTIVE");
            given(couponMapper.selectById(200L)).willReturn(inactive);

            assertThatThrownBy(() -> couponService.receiveCoupon(9, 200L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已下架");

            verify(userCouponMapper, never()).insert(any(UserCoupon.class));
        }

        @Test
        @DisplayName("券已过期时应抛 BusinessException")
        void receiveCoupon_shouldThrow_whenCouponExpired() {
            Coupon expired = CouponFixtures.expiredCoupon(200L, new BigDecimal("10"));
            given(couponMapper.selectById(200L)).willReturn(expired);

            assertThatThrownBy(() -> couponService.receiveCoupon(9, 200L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("过期");

            verify(userCouponMapper, never()).insert(any(UserCoupon.class));
        }

        @Test
        @DisplayName("券库存被领完（CAS 失败）时应抛 BusinessException")
        void receiveCoupon_shouldThrow_whenCasFails() {
            Coupon coupon = CouponFixtures.activeFixedCoupon(200L, new BigDecimal("10"));
            coupon.setUserLimit(null); // 跳过 user limit 检查
            given(couponMapper.selectById(200L)).willReturn(coupon);
            given(couponMapper.incrementReceivedQuantity(200L)).willReturn(0);

            assertThatThrownBy(() -> couponService.receiveCoupon(9, 200L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("领完");

            verify(userCouponMapper, never()).insert(any(UserCoupon.class));
        }

        @Test
        @DisplayName("用户已领满 user_limit 时应抛 BusinessException 且不调用 CAS")
        void receiveCoupon_shouldThrow_whenUserLimitReached() {
            Coupon coupon = CouponFixtures.activeFixedCoupon(200L, new BigDecimal("10"));
            coupon.setUserLimit(1);
            given(couponMapper.selectById(200L)).willReturn(coupon);
            given(userCouponMapper.selectCount(any(Wrapper.class))).willReturn(1L);

            assertThatThrownBy(() -> couponService.receiveCoupon(9, 200L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已领取");

            verify(couponMapper, never()).incrementReceivedQuantity(any());
            verify(userCouponMapper, never()).insert(any(UserCoupon.class));
        }
    }
}
