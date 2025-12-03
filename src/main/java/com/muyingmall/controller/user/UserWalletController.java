package com.muyingmall.controller.user;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.CommonPage;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.common.api.ResultCode;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.config.AlipayConfig;
import com.muyingmall.dto.WalletInfoDTO;
import com.muyingmall.dto.TransactionQueryDTO;
import com.muyingmall.dto.RechargeRequestDTO;
import com.muyingmall.entity.AccountTransaction;
import com.muyingmall.entity.UserAccount;
import com.muyingmall.mapper.AccountTransactionMapper;
import com.muyingmall.service.UserAccountService;
import com.muyingmall.util.SecurityUtil;
import com.muyingmall.util.SpringContextUtil;
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
import java.util.List;
import java.util.Map;

/**
 * 用户钱包控制器
 * 提供钱包余额查询、充值、交易记录等功能
 */
@RestController
@RequestMapping("/user/wallet")
@Tag(name = "用户钱包", description = """
        用户钱包管理接口，提供余额查询、充值、交易记录查询等功能。
        
        **需要用户登录认证**
        
        **交易类型：**
        - 1: 充值
        - 2: 消费（订单支付）
        - 3: 退款
        - 4: 管理员调整
        
        **支持的充值方式：**
        - alipay: 支付宝支付
        - wechat: 微信支付（暂未开放）
        """)
@Slf4j
public class UserWalletController {

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private AccountTransactionMapper accountTransactionMapper;

    @Operation(summary = "获取钱包信息", description = "获取当前用户的钱包完整信息，包括余额、冻结金额、累计充值、累计消费等")
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

    @Operation(summary = "获取钱包余额", description = "快速获取当前用户的钱包余额和冻结金额，用于页面显示")
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

    @Operation(summary = "获取交易记录", description = "分页查询当前用户的钱包交易记录，支持按交易类型和时间范围筛选")
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

    @Operation(summary = "创建充值订单", description = "创建钱包充值订单，返回支付所需的订单号和支付表单。充值金额必须大于0。")
    @PostMapping("/recharge")
    public CommonResult<Map<String, Object>> createRechargeOrder(
            @RequestBody RechargeRequestDTO rechargeRequest) {

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
            BigDecimal amount = rechargeRequest.getAmount();
            String paymentMethod = rechargeRequest.getPaymentMethod();

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("创建充值订单失败：充值金额必须大于0，当前金额={}", amount);
                return CommonResult.validateFailed("充值金额必须大于0");
            }

            // 验证支付方式
            if (!"alipay".equals(paymentMethod) && !"wechat".equals(paymentMethod)) {
                log.error("创建充值订单失败：不支持的支付方式，paymentMethod={}", paymentMethod);
                return CommonResult.validateFailed("不支持的支付方式");
            }

            log.info("开始创建充值订单，userId={}, amount={}, paymentMethod={}", userId, amount, paymentMethod);

            // 创建充值订单
            try {
                Map<String, Object> result = userAccountService.createRechargeOrder(userId, amount, paymentMethod);
                log.info("创建充值订单成功：{}", result);
                return CommonResult.success(result);
            } catch (Exception e) {
                log.error("调用userAccountService.createRechargeOrder时发生异常", e);
                // 提供更详细的错误信息
                String errorMessage = "创建充值订单失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误");
                log.error(errorMessage);
                return CommonResult.failed(errorMessage);
            }
        } catch (Exception e) {
            log.error("创建充值订单过程中发生未预期的异常", e);
            return CommonResult.failed("创建充值订单失败: " + (e.getMessage() != null ? e.getMessage() : "系统错误，请稍后再试"));
        }
    }

    @Operation(summary = "查询充值订单状态")
    @GetMapping("/recharge/{orderNo}/status")
    public CommonResult<Map<String, Object>> queryRechargeStatus(
            @Parameter(description = "充值订单号", required = true) @PathVariable(value = "orderNo") String orderNo) {

        log.info("查询充值订单状态，orderNo={}", orderNo);

        // 从当前登录用户获取用户ID
        Integer userId = userAccountService.getCurrentUserId();
        if (userId == null) {
            log.error("查询充值订单状态失败：用户ID为null");
            return CommonResult.unauthorized(null);
        }

        try {
            // 查询交易记录
            AccountTransaction transaction = accountTransactionMapper.selectOne(
                    new LambdaQueryWrapper<AccountTransaction>()
                            .eq(AccountTransaction::getUserId, userId)
                            .eq(AccountTransaction::getTransactionNo, orderNo)
                            .eq(AccountTransaction::getType, 1) // 1表示充值
            );

            if (transaction == null) {
                log.error("查询充值订单状态失败：订单不存在，orderNo={}", orderNo);
                return CommonResult.validateFailed("充值订单不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", orderNo);
            result.put("amount", transaction.getAmount());
            result.put("status",
                    transaction.getStatus() == 1 ? "SUCCESS" : (transaction.getStatus() == 0 ? "PENDING" : "FAILED"));
            result.put("statusDesc",
                    transaction.getStatus() == 1 ? "支付成功" : (transaction.getStatus() == 0 ? "处理中" : "支付失败"));
            result.put("paymentMethod", transaction.getPaymentMethod());
            result.put("createTime", transaction.getCreateTime());
            result.put("updateTime", transaction.getUpdateTime());

            log.info("查询充值订单状态成功：{}", result);

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("查询充值订单状态失败", e);
            return CommonResult.failed("查询充值订单状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取充值订单支付表单")
    @GetMapping("/recharge/form")
    public CommonResult<Map<String, Object>> getRechargeOrderForm(
            @Parameter(description = "充值订单号", required = true) @RequestParam(value = "orderNo") String orderNo) {

        log.info("获取充值订单支付表单，orderNo={}", orderNo);

        // 从当前登录用户获取用户ID
        Integer userId = userAccountService.getCurrentUserId();
        if (userId == null) {
            log.error("获取充值订单支付表单失败：用户ID为null");
            return CommonResult.unauthorized(null);
        }

        try {
            // 查询交易记录
            AccountTransaction transaction = accountTransactionMapper.selectOne(
                    new LambdaQueryWrapper<AccountTransaction>()
                            .eq(AccountTransaction::getUserId, userId)
                            .eq(AccountTransaction::getTransactionNo, orderNo)
                            .eq(AccountTransaction::getType, 1) // 1表示充值
            );

            if (transaction == null) {
                log.error("获取充值订单支付表单失败：订单不存在，orderNo={}", orderNo);
                return CommonResult.validateFailed("充值订单不存在");
            }

            // 获取支付表单
            try {
                // 获取AlipayConfig实例
                AlipayConfig alipayConfig = SpringContextUtil.getBean(AlipayConfig.class);

                // 创建AlipayClient实例
                AlipayClient alipayClient = new DefaultAlipayClient(
                        alipayConfig.getGatewayUrl(),
                        alipayConfig.getAppId(),
                        alipayConfig.getPrivateKey(),
                        "json",
                        "UTF-8",
                        alipayConfig.getPublicKey(),
                        "RSA2");

                // 创建支付宝支付请求
                AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
                alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl()); // 设置异步通知地址
                alipayRequest.setReturnUrl(alipayConfig.getReturnUrl()); // 设置同步返回地址

                // 构建支付请求参数
                Map<String, Object> bizContent = new HashMap<>();
                bizContent.put("out_trade_no", orderNo);
                bizContent.put("total_amount", transaction.getAmount().toString());
                bizContent.put("subject", "母婴商城账户充值");
                bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
                // 设置订单过期时间
                bizContent.put("timeout_express", "2h");

                alipayRequest.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));

                // 发送支付请求获取支付表单HTML
                String formHtml = alipayClient.pageExecute(alipayRequest).getBody();

                // 返回支付表单HTML
                Map<String, Object> result = new HashMap<>();
                result.put("formHtml", formHtml);
                log.info("获取充值订单支付表单成功");

                return CommonResult.success(result);
            } catch (Exception e) {
                log.error("获取充值订单支付表单失败: {}", e.getMessage(), e);
                return CommonResult.failed("获取充值订单支付表单失败: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("获取充值订单支付表单过程中发生未预期的异常", e);
            return CommonResult.failed("获取充值订单支付表单失败: " + (e.getMessage() != null ? e.getMessage() : "系统错误，请稍后再试"));
        }
    }
}