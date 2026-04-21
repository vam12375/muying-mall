package com.muyingmall.service.impl;

import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.mapper.PointsExchangeMapper;
import com.muyingmall.mapper.PointsHistoryMapper;
import com.muyingmall.mapper.PointsRuleMapper;
import com.muyingmall.mapper.UserPointsMapper;
import com.muyingmall.service.MemberLevelService;
import com.muyingmall.service.PointsOperationService;
import com.muyingmall.service.PointsProductService;
import com.muyingmall.service.UserService;
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
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 积分服务 · 单元测试。
 * 覆盖：
 *   - getUserPoints 委托 PointsOperationService 并透传返回
 *   - addPoints / deductPoints 委托 PointsOperationService，参数完全透传
 *   - deductPoints 积分不足（下游返回 false）时上层透传 false
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
@DisplayName("积分服务 · 单元测试")
class PointsServiceImplTest {

    @Mock
    private UserPointsMapper userPointsMapper;

    @Mock
    private PointsHistoryMapper pointsHistoryMapper;
    @Mock
    private PointsRuleMapper pointsRuleMapper;
    @Mock
    private PointsProductService pointsProductService;
    @Mock
    private PointsOperationService pointsOperationService;
    @Mock
    private MemberLevelService memberLevelService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserService userService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private PointsExchangeMapper pointsExchangeMapper;

    @InjectMocks
    private PointsServiceImpl pointsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pointsService, "baseMapper", userPointsMapper);
    }

    @Nested
    @DisplayName("getUserPoints(userId)")
    class GetUserPoints {

        @Test
        @DisplayName("应委托给 PointsOperationService 并透传返回值")
        void getUserPoints_shouldDelegate() {
            given(pointsOperationService.getUserPoints(9)).willReturn(123);

            assertThat(pointsService.getUserPoints(9)).isEqualTo(123);
            verify(pointsOperationService).getUserPoints(9);
        }
    }

    @Nested
    @DisplayName("addPoints / deductPoints")
    class AddOrDeduct {

        @Test
        @DisplayName("addPoints 应透传全部参数到 PointsOperationService.addPoints")
        void addPoints_shouldDelegateWithAllArgs() {
            given(pointsOperationService.addPoints(eq(9), eq(50), eq("ORDER"), eq("ORD-1"), eq("订单奖励")))
                    .willReturn(true);

            boolean ok = pointsService.addPoints(9, 50, "ORDER", "ORD-1", "订单奖励");

            assertThat(ok).isTrue();
            verify(pointsOperationService).addPoints(9, 50, "ORDER", "ORD-1", "订单奖励");
        }

        @Test
        @DisplayName("deductPoints 下游返回 false（余额不足）时上层透传 false")
        void deductPoints_shouldReturnFalse_whenInsufficient() {
            given(pointsOperationService.deductPoints(eq(9), eq(999), eq("USE"), eq("REF-1"), eq("兑换")))
                    .willReturn(false);

            boolean ok = pointsService.deductPoints(9, 999, "USE", "REF-1", "兑换");

            assertThat(ok).isFalse();
            verify(pointsOperationService).deductPoints(9, 999, "USE", "REF-1", "兑换");
        }

        @Test
        @DisplayName("deductPoints 成功时应返回 true")
        void deductPoints_shouldReturnTrue_whenSuccess() {
            given(pointsOperationService.deductPoints(eq(9), eq(10), eq("USE"), eq("REF-1"), eq("兑换")))
                    .willReturn(true);

            boolean ok = pointsService.deductPoints(9, 10, "USE", "REF-1", "兑换");

            assertThat(ok).isTrue();
        }
    }
}
