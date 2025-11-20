package com.muyingmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.muyingmall.common.api.PageResult;
import com.muyingmall.dto.RechargeRequestDTO;
import com.muyingmall.dto.TransactionQueryDTO;
import com.muyingmall.entity.AccountTransaction;
import com.muyingmall.entity.UserAccount;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户账户服务接口
 */
public interface UserAccountService {

    /**
     * 分页获取用户账户列表
     *
     * @param page    页码
     * @param size    每页数量
     * @param keyword 搜索关键字(用户名/昵称/邮箱/手机)
     * @param status  账户状态: 0-冻结, 1-正常
     * @return 分页结果
     */
    PageResult<UserAccount> getUserAccountPage(Integer page, Integer size, String keyword, Integer status);

    /**
     * 根据用户ID获取账户信息
     *
     * @param userId 用户ID
     * @return 用户账户信息
     */
    UserAccount getUserAccountByUserId(Integer userId);

    /**
     * 管理员给用户充值
     *
     * @param rechargeRequest 充值请求参数
     */
    void rechargeUserAccount(RechargeRequestDTO rechargeRequest);

    /**
     * 管理员调整用户余额
     *
     * @param userId 用户ID
     * @param amount 调整金额（正数为增加，负数为减少）
     * @param reason 调整原因
     */
    void adjustUserBalance(Integer userId, BigDecimal amount, String reason);

    /**
     * 更改用户账户状态（冻结/解冻）
     *
     * @param userId 用户ID
     * @param status 账户状态: 0-冻结, 1-正常
     * @param reason 操作原因
     */
    void toggleUserAccountStatus(Integer userId, Integer status, String reason);

    /**
     * 分页获取交易记录列表
     *
     * @param page     页码
     * @param size     每页数量
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<AccountTransaction> getTransactionPage(Integer page, Integer size, TransactionQueryDTO queryDTO);

    /**
     * 根据ID获取交易记录详情
     *
     * @param id 交易记录ID
     * @return 交易记录详情
     */
    AccountTransaction getTransactionById(Integer id);

    /**
     * 获取钱包统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, BigDecimal> getWalletStats(Integer userId);

    /**
     * 创建充值订单
     * 
     * @param userId        用户ID
     * @param amount        充值金额
     * @param paymentMethod 支付方式
     * @return 订单信息
     */
    Map<String, Object> createRechargeOrder(Integer userId, BigDecimal amount, String paymentMethod);

    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID
     */
    Integer getCurrentUserId();

    /**
     * 获取用户账户交易记录
     * 
     * @param userId    用户ID
     * @param page      页码
     * @param size      每页数量
     * @param type      交易类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 交易记录分页
     */
    IPage<AccountTransaction> getAccountTransactions(Integer userId, Integer page, Integer size, Integer type,
            String startTime, String endTime);

    /**
     * 使用钱包余额支付订单
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @param amount  支付金额
     * @return 是否支付成功
     */
    boolean payOrderByWallet(Integer userId, Integer orderId, BigDecimal amount);

    /**
     * 退款到钱包
     *
     * @param userId      用户ID
     * @param amount      退款金额
     * @param description 退款描述
     */
    void refundToWallet(Integer userId, BigDecimal amount, String description);
}