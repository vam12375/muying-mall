package com.muyingmall.service.impl;

import com.muyingmall.config.AlipayConfig;
import com.muyingmall.entity.AccountTransaction;
import com.muyingmall.entity.UserAccount;
import com.muyingmall.fixtures.UserAccountFixtures;
import com.muyingmall.mapper.AccountTransactionMapper;
import com.muyingmall.mapper.UserAccountMapper;
import com.muyingmall.mapper.UserMapper;
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
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 用户账户服务 · 单元测试。
 * 被测方法：{@link UserAccountServiceImpl#payOrderByWallet(Integer, Integer, java.math.BigDecimal)}
 * 覆盖：
 *   - 黄金路径：余额充足 → 扣款成功并写交易记录 + 清缓存
 *   - 边界：deductBalance 返回 0（余额不足）→ 抛 IllegalArgumentException
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
@DisplayName("用户账户服务 · 钱包支付单元测试")
class UserAccountServiceImplTest {

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private AccountTransactionMapper accountTransactionMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AlipayConfig alipayConfig;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private UserAccountServiceImpl userAccountService;

    @BeforeEach
    void setUp() {
        // 三个 @Autowired 字段未被 @InjectMocks 构造器注入，手动补注入。
        ReflectionTestUtils.setField(userAccountService, "userAccountMapper", userAccountMapper);
        ReflectionTestUtils.setField(userAccountService, "accountTransactionMapper", accountTransactionMapper);
        ReflectionTestUtils.setField(userAccountService, "userMapper", userMapper);
    }

    @Nested
    @DisplayName("payOrderByWallet(userId, orderId, amount)")
    class PayOrderByWallet {

        @Test
        @DisplayName("余额充足时应扣款并写入消费交易、清缓存")
        void payOrderByWallet_shouldDeductAndRecordTx_whenBalanceSufficient() {
            // Given：缓存命中直接返回账户（跳过 DB 查询路径）
            UserAccount account = UserAccountFixtures.withBalance(11, 9, new BigDecimal("500"));
            given(redisUtil.get(anyString())).willReturn(account);
            given(userAccountMapper.deductBalance(eq(9), eq(new BigDecimal("100")), any(Date.class)))
                    .willReturn(1);

            // When
            boolean success = userAccountService.payOrderByWallet(9, 1001, new BigDecimal("100"));

            // Then
            assertThat(success).isTrue();

            ArgumentCaptor<AccountTransaction> txCaptor = ArgumentCaptor.forClass(AccountTransaction.class);
            verify(accountTransactionMapper).insert(txCaptor.capture());
            AccountTransaction tx = txCaptor.getValue();
            assertThat(tx.getUserId()).isEqualTo(9);
            assertThat(tx.getType()).isEqualTo(2); // 消费
            assertThat(tx.getAmount()).isEqualByComparingTo("100");
            assertThat(tx.getBeforeBalance()).isEqualByComparingTo("500");
            assertThat(tx.getAfterBalance()).isEqualByComparingTo("400");
            assertThat(tx.getBalance()).isEqualByComparingTo("400");
            assertThat(tx.getStatus()).isEqualTo(1); // 成功
            assertThat(tx.getPaymentMethod()).isEqualTo("wallet");
            assertThat(tx.getRelatedId()).isEqualTo("1001");
            assertThat(tx.getAccountId()).isEqualTo(11);
            assertThat(tx.getTransactionNo()).isNotBlank();

            // 清缓存（del 调用）
            verify(redisUtil).del(anyString());
        }

        @Test
        @DisplayName("deductBalance 返回 0（余额不足）时应抛 IllegalArgumentException 且不写交易记录")
        void payOrderByWallet_shouldThrow_whenBalanceInsufficient() {
            // Given
            UserAccount account = UserAccountFixtures.withBalance(11, 9, new BigDecimal("10"));
            given(redisUtil.get(anyString())).willReturn(account);
            given(userAccountMapper.deductBalance(eq(9), any(BigDecimal.class), any(Date.class)))
                    .willReturn(0);

            // When / Then
            assertThatThrownBy(() -> userAccountService.payOrderByWallet(9, 1001, new BigDecimal("100")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("余额不足");

            verify(accountTransactionMapper, never()).insert(any(AccountTransaction.class));
        }
    }
}
