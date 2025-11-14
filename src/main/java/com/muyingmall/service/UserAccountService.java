package com.muyingmall.service;

import com.muyingmall.common.PageResult;
import com.muyingmall.dto.RechargeRequestDTO;
import com.muyingmall.dto.TransactionQueryDTO;
import com.muyingmall.entity.UserAccount;
import com.muyingmall.entity.AccountTransaction;

import java.math.BigDecimal;

/**
 * 用户账户服务接口
 */
public interface UserAccountService {

    /**
     * 分页获取用户账户列表
     */
    PageResult<UserAccount> getUserAccountPage(Integer page, Integer size, String keyword, Integer status);

    /**
     * 根据用户ID获取账户信息
     */
    UserAccount getUserAccountByUserId(Integer userId);

    /**
     * 管理员给用户充值
     */
    void rechargeUserAccount(RechargeRequestDTO rechargeRequest);

    /**
     * 管理员调整用户余额
     */
    void adjustUserBalance(Integer userId, BigDecimal amount, String reason);

    /**
     * 更改用户账户状态
     */
    void toggleUserAccountStatus(Integer userId, Integer status, String reason);

    /**
     * 分页获取交易记录
     */
    PageResult<AccountTransaction> getTransactionPage(Integer page, Integer size, TransactionQueryDTO queryDTO);

    /**
     * 根据ID获取交易记录
     */
    AccountTransaction getTransactionById(Integer id);
}
