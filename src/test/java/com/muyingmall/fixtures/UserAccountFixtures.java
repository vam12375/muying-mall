package com.muyingmall.fixtures;

import com.muyingmall.entity.UserAccount;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户账户测试夹具。
 */
public final class UserAccountFixtures {

    private UserAccountFixtures() {
    }

    /**
     * 构造一个正常状态且余额指定的账户。
     */
    public static UserAccount withBalance(Integer accountId, Integer userId, BigDecimal balance) {
        UserAccount account = new UserAccount();
        account.setId(accountId);
        account.setUserId(userId);
        account.setBalance(balance);
        account.setFrozenAmount(BigDecimal.ZERO);
        account.setStatus(1);
        account.setCreateTime(new Date());
        account.setUpdateTime(new Date());
        return account;
    }

    /**
     * 构造一个被冻结的账户。
     */
    public static UserAccount frozen(Integer accountId, Integer userId, BigDecimal balance) {
        UserAccount account = withBalance(accountId, userId, balance);
        account.setStatus(0);
        return account;
    }
}
