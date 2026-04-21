package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.muyingmall.entity.UserCoupon;
import com.muyingmall.fixtures.CouponFixtures;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 用户优惠券服务 · 单元测试。
 * 覆盖：
 *   - markAsUsed 黄金路径：未使用券 → 标记为 USED + orderId
 *   - markAsUsed 边界：券不存在 → 返回 false，不更新
 *   - getUserCoupon：查到 / 查不到
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
@DisplayName("用户优惠券服务 · 单元测试")
class UserCouponServiceImplTest {

    @Mock
    private UserCouponMapper userCouponMapper;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private UserCouponServiceImpl userCouponService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userCouponService, "baseMapper", userCouponMapper);
    }

    @Nested
    @DisplayName("markAsUsed(id, orderId)")
    class MarkAsUsed {

        @Test
        @DisplayName("未使用券应被标记为 USED 并记录订单与使用时间")
        void markAsUsed_shouldUpdateStatus_whenUnusedCouponExists() {
            // Given
            UserCoupon coupon = CouponFixtures.unusedUserCoupon(50L, 9, 200L);
            given(userCouponMapper.selectById(50L)).willReturn(coupon);
            given(userCouponMapper.updateById(any(UserCoupon.class))).willReturn(1);

            // When
            boolean ok = userCouponService.markAsUsed(50L, 1001L);

            // Then
            assertThat(ok).isTrue();
            ArgumentCaptor<UserCoupon> captor = ArgumentCaptor.forClass(UserCoupon.class);
            verify(userCouponMapper).updateById(captor.capture());
            UserCoupon updated = captor.getValue();
            assertThat(updated.getStatus()).isEqualTo("USED");
            assertThat(updated.getOrderId()).isEqualTo(1001L);
            assertThat(updated.getUseTime()).isNotNull();
        }

        @Test
        @DisplayName("券不存在时应返回 false 且不调用 updateById")
        void markAsUsed_shouldReturnFalse_whenCouponNotFound() {
            given(userCouponMapper.selectById(9999L)).willReturn(null);

            boolean ok = userCouponService.markAsUsed(9999L, 1001L);

            assertThat(ok).isFalse();
            verify(userCouponMapper, never()).updateById(any(UserCoupon.class));
        }
    }

    @Nested
    @DisplayName("getUserCoupon(userId, couponId)")
    class GetUserCoupon {

        @Test
        @DisplayName("查到记录时应返回 UserCoupon 实例")
        void getUserCoupon_shouldReturnRecord_whenExists() {
            UserCoupon coupon = CouponFixtures.unusedUserCoupon(50L, 9, 200L);
            given(userCouponMapper.selectOne(any(Wrapper.class), anyBoolean())).willReturn(coupon);

            UserCoupon result = userCouponService.getUserCoupon(9, 200L);

            assertThat(result).isSameAs(coupon);
        }

        @Test
        @DisplayName("查不到时应返回 null")
        void getUserCoupon_shouldReturnNull_whenNotFound() {
            given(userCouponMapper.selectOne(any(Wrapper.class), anyBoolean())).willReturn(null);

            assertThat(userCouponService.getUserCoupon(9, 200L)).isNull();
        }
    }
}
