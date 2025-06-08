package com.muyingmall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.CommonPage;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.entity.AccountTransaction;
import com.muyingmall.entity.UserAccount;
import com.muyingmall.service.UserAccountService;
import com.muyingmall.dto.WalletInfoDTO;
import com.muyingmall.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户钱包控制器
 */
@RestController
@RequestMapping("/user/wallet")
@Tag(name = "用户钱包接口")
@Slf4j
public class UserWalletController {

    @Autowired
    private UserAccountService userAccountService;

    @Operation(summary = "获取钱包信息")
    @GetMapping("/info")
    public CommonResult<WalletInfoDTO> getWalletInfo() {
        // 添加详细日志
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("UserWalletController.getWalletInfo - 当前认证对象: {}", authentication);

        if (authentication != null) {
            log.info("认证详情 - Principal: {}, Credentials: {}, Details: {}",
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    authentication.getDetails());

            // 检查和记录认证对象的详情中的userId
            if (authentication.getDetails() instanceof Map) {
                Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
                log.info("认证Details中的userId: {}", details.get("userId"));
            }
        }

        // 从当前登录用户获取用户ID
        Integer userId = userAccountService.getCurrentUserId();
        log.info("UserAccountService.getCurrentUserId() 返回的用户ID: {}", userId);
        log.info("SecurityUtil.getCurrentUserId() 返回的用户ID: {}", SecurityUtil.getCurrentUserId());

        if (userId == null) {
            log.error("获取钱包信息失败：用户ID为null");
            return CommonResult.unauthorized(null);
        }

        try {
            // 获取用户账户信息
            UserAccount userAccount = userAccountService.getUserAccountByUserId(userId);
            if (userAccount == null) {
                log.error("获取钱包信息失败：用户ID={}的账户不存在", userId);
                return CommonResult.failed("账户不存在");
            }
            log.info("找到用户账户: {}", userAccount);

            // 构建钱包信息VO
            WalletInfoDTO walletInfoDTO = new WalletInfoDTO();
            walletInfoDTO.setBalance(userAccount.getBalance());
            walletInfoDTO.setFrozenBalance(userAccount.getFrozenAmount());
            walletInfoDTO.setPoints(0);

            // 获取累计充值和消费金额
            Map<String, BigDecimal> stats = userAccountService.getWalletStats(userId);
            walletInfoDTO.setTotalRecharge(stats.getOrDefault("totalRecharge", BigDecimal.ZERO));
            walletInfoDTO.setTotalConsumption(stats.getOrDefault("totalConsumption", BigDecimal.ZERO));

            log.info("获取钱包信息成功：{}", walletInfoDTO);

            return CommonResult.success(walletInfoDTO);
        } catch (Exception e) {
            log.error("获取钱包信息失败", e);
            return CommonResult.failed("获取钱包信息失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取钱包余额")
    @GetMapping("/balance")
    public CommonResult<Map<String, Object>> getWalletBalance() {
        // 添加详细日志
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("UserWalletController.getWalletBalance - 当前认证对象: {}", authentication);

        // 从当前登录用户获取用户ID
        Integer userId = userAccountService.getCurrentUserId();
        log.info("UserAccountService.getCurrentUserId() 返回的用户ID: {}", userId);

        if (userId == null) {
            log.error("获取钱包余额失败：用户ID为null");
            return CommonResult.unauthorized(null);
        }

        try {
            // 获取用户账户信息
            UserAccount userAccount = userAccountService.getUserAccountByUserId(userId);
            if (userAccount == null) {
                log.error("获取钱包余额失败：用户ID={}的账户不存在", userId);
                return CommonResult.failed("账户不存在");
            }
            log.info("找到用户账户: {}", userAccount);

            Map<String, Object> result = new HashMap<>();
            result.put("balance", userAccount.getBalance());
            result.put("frozenBalance", userAccount.getFrozenAmount());
            result.put("points", 0);

            log.info("获取钱包余额成功：{}", result);

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取钱包余额失败", e);
            return CommonResult.failed("获取钱包余额失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取交易记录")
    @GetMapping("/transactions")
    public CommonResult<Map<String, Object>> getTransactions(
            @Parameter(description = "页码", example = "1") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "交易类型: 1-充值，2-消费，3-退款，4-管理员调整") @RequestParam(value = "type", required = false) Integer type,
            @Parameter(description = "开始时间") @RequestParam(value = "startTime", required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(value = "endTime", required = false) String endTime) {

        // 添加详细日志
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("UserWalletController.getTransactions - 当前认证对象: {}", authentication);

        // 从当前登录用户获取用户ID
        Integer userId = userAccountService.getCurrentUserId();
        log.info("UserAccountService.getCurrentUserId() 返回的用户ID: {}", userId);

        if (userId == null) {
            log.error("获取交易记录失败：用户ID为null");
            return CommonResult.unauthorized(null);
        }

        try {
            log.info("开始获取交易记录，userId={}, page={}, size={}, type={}, startTime={}, endTime={}",
                    userId, page, size, type, startTime, endTime);

            // 获取交易记录
            IPage<AccountTransaction> transactions = userAccountService.getAccountTransactions(userId, page, size, type,
                    startTime, endTime);

            // 创建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", transactions.getRecords());
            result.put("total", transactions.getTotal());
            result.put("size", transactions.getSize());
            result.put("current", transactions.getCurrent());
            result.put("pages", transactions.getPages());

            log.info("获取交易记录成功，总记录数：{}", transactions.getTotal());

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取交易记录失败", e);
            return CommonResult.failed("获取交易记录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "创建充值订单")
    @PostMapping("/recharge")
    public CommonResult<Map<String, Object>> createRechargeOrder(
            @Parameter(description = "充值金额", required = true) @RequestParam(value = "amount") BigDecimal amount,
            @Parameter(description = "支付方式：alipay-支付宝，wechat-微信支付", required = true) @RequestParam(value = "paymentMethod") String paymentMethod) {

        // 添加详细日志
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("UserWalletController.createRechargeOrder - 当前认证对象: {}", authentication);

        // 从当前登录用户获取用户ID
        Integer userId = userAccountService.getCurrentUserId();
        log.info("UserAccountService.getCurrentUserId() 返回的用户ID: {}", userId);

        if (userId == null) {
            log.error("创建充值订单失败：用户ID为null");
            return CommonResult.unauthorized(null);
        }

        try {
            // 验证充值金额
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("创建充值订单失败：充值金额必须大于0，当前金额={}", amount);
                return CommonResult.validateFailed("充值金额必须大于0");
            }

            log.info("开始创建充值订单，userId={}, amount={}, paymentMethod={}", userId, amount, paymentMethod);

            // 创建充值订单
            Map<String, Object> result = userAccountService.createRechargeOrder(userId, amount, paymentMethod);

            log.info("创建充值订单成功：{}", result);

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("创建充值订单失败", e);
            return CommonResult.failed("创建充值订单失败: " + e.getMessage());
        }
    }
}