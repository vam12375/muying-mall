package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.common.PageResult;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.dto.RechargeRequestDTO;
import com.muyingmall.dto.TransactionQueryDTO;
import com.muyingmall.entity.AccountTransaction;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserAccount;
import com.muyingmall.mapper.AccountTransactionMapper;
import com.muyingmall.mapper.UserAccountMapper;
import com.muyingmall.mapper.UserMapper;
import com.muyingmall.service.UserAccountService;
import com.muyingmall.util.SecurityUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 用户账户服务实现类
 */
@Service
public class UserAccountServiceImpl implements UserAccountService {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private AccountTransactionMapper accountTransactionMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<UserAccount> getUserAccountPage(Integer page, Integer size, String keyword, Integer status) {
        Page<UserAccount> pageParam = new Page<>(page, size);

        // 构建查询条件
        LambdaQueryWrapper<UserAccount> queryWrapper = new LambdaQueryWrapper<>();

        // 添加状态条件
        if (status != null) {
            queryWrapper.eq(UserAccount::getStatus, status);
        }

        // 执行查询
        IPage<UserAccount> pageResult = userAccountMapper.getUserAccountPage(pageParam, keyword);

        // 转换为自定义PageResult
        return new PageResult<UserAccount>(
                pageResult.getTotal(),
                pageResult.getRecords(),
                (int) pageResult.getCurrent(),
                (int) pageResult.getSize());
    }

    @Override
    public UserAccount getUserAccountByUserId(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 查询用户账户
        UserAccount userAccount = userAccountMapper.getUserAccountByUserId(userId);
        if (userAccount == null) {
            throw new BusinessException("用户账户不存在");
        }

        return userAccount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rechargeUserAccount(RechargeRequestDTO rechargeRequest) {
        if (rechargeRequest == null) {
            throw new BusinessException("充值请求参数不能为空");
        }

        Integer userId = rechargeRequest.getUserId();
        BigDecimal amount = rechargeRequest.getAmount();
        String paymentMethod = rechargeRequest.getPaymentMethod();
        String description = rechargeRequest.getDescription();
        String remark = rechargeRequest.getRemark();

        // 参数校验
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于0");
        }
        if (StringUtils.isBlank(paymentMethod)) {
            throw new BusinessException("支付方式不能为空");
        }

        System.out.println("开始充值操作: userId=" + userId + ", amount=" + amount);

        // 查询用户账户
        UserAccount userAccount = userAccountMapper.getUserAccountByUserId(userId);
        if (userAccount == null) {
            throw new BusinessException("用户账户不存在");
        }

        System.out.println("充值操作 - 获取到用户账户: id=" + userAccount.getId() + ", userId=" + userAccount.getUserId()
                + ", 当前余额=" + userAccount.getBalance());

        // 获取当前管理员信息
        Integer adminId = SecurityUtil.getCurrentUserId();
        String adminName = SecurityUtil.getCurrentUsername();

        // 更新账户余额
        BigDecimal beforeBalance = userAccount.getBalance();
        BigDecimal afterBalance = beforeBalance.add(amount);

        userAccount.setBalance(afterBalance);
        userAccount.setLastRechargeTime(new Date());
        userAccount.setUpdateTime(new Date());
        userAccountMapper.updateById(userAccount);

        System.out.println("充值操作 - 更新账户余额: beforeBalance=" + beforeBalance + ", afterBalance=" + afterBalance);

        // 创建交易记录
        AccountTransaction transaction = new AccountTransaction();
        System.out.println("充值操作 - 创建交易记录对象: " + transaction);

        transaction.setUserId(userId);
        Integer accountId = userAccount.getId();
        System.out.println("充值操作 - 设置accountId，值为：" + accountId + "，来自userAccount.getId()");
        transaction.setAccountId(accountId);
        transaction.setType(1); // 1-充值
        transaction.setAmount(amount);
        transaction.setBeforeBalance(beforeBalance);
        transaction.setAfterBalance(afterBalance);
        transaction.setBalance(afterBalance);
        transaction.setStatus(1); // 1-成功
        transaction.setPaymentMethod(paymentMethod);
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setDescription(description);
        transaction.setOperatorId(adminId);
        transaction.setOperatorName(adminName);
        transaction.setRemark(remark);
        transaction.setCreateTime(new Date());
        transaction.setUpdateTime(new Date());

        System.out.println("充值操作 - 交易记录设置完成，准备插入，accountId=" + transaction.getAccountId());

        accountTransactionMapper.secureInsert(transaction);

        System.out.println("充值操作完成: 交易ID=" + transaction.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustUserBalance(Integer userId, BigDecimal amount, String reason) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("调整金额不能为0");
        }
        if (StringUtils.isBlank(reason)) {
            throw new BusinessException("调整原因不能为空");
        }

        System.out.println("开始余额调整操作: userId=" + userId + ", amount=" + amount);

        // 查询用户账户
        UserAccount userAccount = userAccountMapper.getUserAccountByUserId(userId);
        if (userAccount == null) {
            throw new BusinessException("用户账户不存在");
        }

        System.out.println("余额调整 - 获取到用户账户: id=" + userAccount.getId() + ", userId=" + userAccount.getUserId()
                + ", 当前余额=" + userAccount.getBalance());

        // 获取当前管理员信息
        Integer adminId = SecurityUtil.getCurrentUserId();
        String adminName = SecurityUtil.getCurrentUsername();

        // 更新账户余额
        BigDecimal beforeBalance = userAccount.getBalance();
        BigDecimal afterBalance = beforeBalance.add(amount);

        // 检查余额是否足够（如果是减少余额）
        if (amount.compareTo(BigDecimal.ZERO) < 0 && afterBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("用户余额不足");
        }

        userAccount.setBalance(afterBalance);
        userAccount.setUpdateTime(new Date());
        userAccountMapper.updateById(userAccount);

        System.out.println("余额调整 - 更新账户余额: beforeBalance=" + beforeBalance + ", afterBalance=" + afterBalance);

        // 创建交易记录
        AccountTransaction transaction = new AccountTransaction();
        System.out.println("余额调整 - 创建交易记录对象: " + transaction);

        transaction.setUserId(userId);
        Integer accountId = userAccount.getId();
        System.out.println("余额调整 - 设置accountId，值为：" + accountId + "，来自userAccount.getId()");
        transaction.setAccountId(accountId);
        transaction.setType(4); // 4-管理员调整
        transaction.setAmount(amount);
        transaction.setBeforeBalance(beforeBalance);
        transaction.setAfterBalance(afterBalance);
        transaction.setBalance(afterBalance);
        transaction.setStatus(1); // 1-成功
        transaction.setPaymentMethod("admin");
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setDescription("管理员调整余额");
        transaction.setOperatorId(adminId);
        transaction.setOperatorName(adminName);
        transaction.setRemark(reason);
        transaction.setCreateTime(new Date());
        transaction.setUpdateTime(new Date());

        System.out.println("余额调整 - 交易记录设置完成，准备插入，accountId=" + transaction.getAccountId());

        accountTransactionMapper.secureInsert(transaction);

        System.out.println("余额调整操作完成: 交易ID=" + transaction.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleUserAccountStatus(Integer userId, Integer status, String reason) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("账户状态参数错误");
        }

        // 查询用户账户
        UserAccount userAccount = userAccountMapper.getUserAccountByUserId(userId);
        if (userAccount == null) {
            throw new BusinessException("用户账户不存在");
        }

        // 如果状态相同，则不需要更新
        if (userAccount.getStatus().equals(status)) {
            return;
        }

        // 更新账户状态
        userAccount.setStatus(status);
        userAccount.setUpdateTime(new Date());
        userAccountMapper.updateById(userAccount);

        // 记录操作日志（可以根据需求添加）
        // ...
    }

    @Override
    public PageResult<AccountTransaction> getTransactionPage(Integer page, Integer size, TransactionQueryDTO queryDTO) {
        Page<AccountTransaction> pageParam = new Page<>(page, size);

        // 执行查询
        IPage<AccountTransaction> pageResult = accountTransactionMapper.getTransactionPage(pageParam, queryDTO);

        // 获取用户信息
        List<AccountTransaction> records = pageResult.getRecords();
        for (AccountTransaction transaction : records) {
            User user = userMapper.selectById(transaction.getUserId());
            transaction.setUser(user);
        }

        // 转换为自定义PageResult
        return new PageResult<AccountTransaction>(
                pageResult.getTotal(),
                records,
                (int) pageResult.getCurrent(),
                (int) pageResult.getSize());
    }

    @Override
    public AccountTransaction getTransactionById(Integer id) {
        if (id == null) {
            throw new BusinessException("交易记录ID不能为空");
        }

        // 查询交易记录
        AccountTransaction transaction = accountTransactionMapper.selectById(id);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        // 获取用户信息
        User user = userMapper.selectById(transaction.getUserId());
        transaction.setUser(user);

        return transaction;
    }

    /**
     * 生成交易流水号
     * 
     * @return 交易流水号
     */
    private String generateTransactionNo() {
        // 生成格式：前缀 + 时间戳 + 随机数
        return "TR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public Map<String, BigDecimal> getWalletStats(Integer userId) {
        Map<String, BigDecimal> stats = new HashMap<>();

        // 获取累计充值金额
        BigDecimal totalRecharge = accountTransactionMapper.sumAmountByUserIdAndType(userId, 1);
        stats.put("totalRecharge", totalRecharge != null ? totalRecharge : BigDecimal.ZERO);

        // 获取累计消费金额
        BigDecimal totalConsumption = accountTransactionMapper.sumAmountByUserIdAndType(userId, 2);
        stats.put("totalConsumption", totalConsumption != null ? totalConsumption : BigDecimal.ZERO);

        return stats;
    }

    @Override
    public Map<String, Object> createRechargeOrder(Integer userId, BigDecimal amount, String paymentMethod) {
        Map<String, Object> result = new HashMap<>();

        // 生成充值订单号
        String rechargeOrderNo = generateRechargeOrderNo();
        result.put("orderNo", rechargeOrderNo);
        result.put("amount", amount);
        result.put("paymentMethod", paymentMethod);

        // TODO: 调用支付接口，生成支付链接或二维码
        // 这里模拟支付链接
        if ("alipay".equals(paymentMethod)) {
            result.put("payUrl", "/payment/alipay?orderNo=" + rechargeOrderNo);
        } else if ("wechat".equals(paymentMethod)) {
            result.put("payUrl", "/payment/wechat?orderNo=" + rechargeOrderNo);
            result.put("qrCode",
                    "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAQAAAAEAAQMAAABmvDolAAAABlBMVEX///8AAABVwtN+AAAA");
        }

        return result;
    }

    @Override
    public Integer getCurrentUserId() {
        // 从Spring Security上下文中获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // 首先尝试从认证对象的details中获取userId
            if (authentication.getDetails() instanceof Map) {
                try {
                    Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
                    Object userId = details.get("userId");
                    if (userId instanceof Integer) {
                        System.out.println("从认证对象的details中获取到userId: " + userId);
                        return (Integer) userId;
                    } else if (userId instanceof String) {
                        System.out.println("从认证对象的details中获取到userId（字符串）: " + userId);
                        return Integer.parseInt((String) userId);
                    }
                } catch (Exception e) {
                    System.out.println("从认证对象的details中获取userId失败: " + e.getMessage());
                }
            }

            // 如果从details中无法获取，尝试从Principal获取
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // 从UserDetails中获取用户名，然后通过用户名查询用户ID
                String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                System.out.println("从UserDetails中获取到用户名: " + username);
                // 通过用户名从数据库查询用户ID
                User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
                if (user != null) {
                    System.out.println("根据用户名查询到用户ID: " + user.getUserId());
                    return user.getUserId();
                } else {
                    System.out.println("根据用户名未找到用户: " + username);
                    throw new BusinessException("用户不存在");
                }
            } else if (principal instanceof String) {
                // 如果principal是字符串（通常是用户名）
                String username = (String) principal;
                System.out.println("从String类型的principal中获取到用户名: " + username);
                // 通过用户名从数据库查询用户ID
                User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
                if (user != null) {
                    System.out.println("根据用户名查询到用户ID: " + user.getUserId());
                    return user.getUserId();
                } else {
                    System.out.println("根据用户名未找到用户: " + username);
                    throw new BusinessException("用户不存在");
                }
            }
        }
        return null;
    }

    @Override
    public IPage<AccountTransaction> getAccountTransactions(Integer userId, Integer page, Integer size,
            Integer type, String startTime, String endTime) {
        // 创建分页对象
        Page<AccountTransaction> pageParam = new Page<>(page, size);

        // 构建查询条件
        LambdaQueryWrapper<AccountTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccountTransaction::getUserId, userId);

        // 按交易类型过滤
        if (type != null) {
            queryWrapper.eq(AccountTransaction::getType, type);
        }

        // 按时间范围过滤
        if (startTime != null && !startTime.isEmpty()) {
            LocalDateTime startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_DATE_TIME);
            queryWrapper.ge(AccountTransaction::getCreateTime, startDateTime);
        }

        if (endTime != null && !endTime.isEmpty()) {
            LocalDateTime endDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE_TIME);
            queryWrapper.le(AccountTransaction::getCreateTime, endDateTime);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(AccountTransaction::getCreateTime);

        // 执行查询
        return accountTransactionMapper.selectPage(pageParam, queryWrapper);
    }

    /**
     * 生成充值订单号
     * 
     * @return 订单号
     */
    private String generateRechargeOrderNo() {
        return "RCH" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrderByWallet(Integer userId, Integer orderId, BigDecimal amount) {
        // 获取用户账户
        UserAccount userAccount = getUserAccountByUserId(userId);
        if (userAccount == null) {
            throw new IllegalArgumentException("用户账户不存在");
        }

        // 检查余额
        if (userAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("账户余额不足");
        }

        // 更新账户余额
        BigDecimal beforeBalance = userAccount.getBalance();
        BigDecimal afterBalance = beforeBalance.subtract(amount);
        userAccount.setBalance(afterBalance);
        userAccount.setUpdateTime(new Date());

        // 更新数据库
        int rows = userAccountMapper.updateById(userAccount);
        if (rows <= 0) {
            throw new RuntimeException("更新账户余额失败");
        }

        // 创建交易记录
        AccountTransaction transaction = new AccountTransaction();
        transaction.setUserId(userId);
        transaction.setType(2); // 2表示消费
        transaction.setAmount(amount);
        transaction.setBeforeBalance(beforeBalance);
        transaction.setAfterBalance(afterBalance);
        transaction.setBalance(afterBalance);
        transaction.setStatus(1); // 1表示成功
        transaction.setPaymentMethod("wallet");
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setAccountId(userAccount.getId());
        transaction.setRelatedId(orderId.toString());
        transaction.setDescription("钱包支付订单");
        transaction.setCreateTime(new Date());
        transaction.setUpdateTime(new Date());

        // 保存交易记录
        accountTransactionMapper.insert(transaction);

        // TODO: 更新订单状态为已支付
        // orderService.updateOrderStatus(orderId, 1); // 假设1表示已支付状态

        return true;
    }
}