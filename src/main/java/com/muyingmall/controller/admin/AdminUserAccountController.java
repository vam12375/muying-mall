package com.muyingmall.controller.admin;

import com.muyingmall.common.api.PageResult;
import com.muyingmall.common.api.Result;
import com.muyingmall.dto.RechargeRequestDTO;
import com.muyingmall.dto.TransactionQueryDTO;
import com.muyingmall.entity.UserAccount;
import com.muyingmall.entity.AccountTransaction;
import com.muyingmall.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 后台管理系统 - 用户账户管理控制器
 */
@RestController
@RequestMapping("/admin/user-accounts")
@Tag(name = "后台-用户账户管理")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserAccountController {

    @Autowired
    private UserAccountService userAccountService;

    /**
     * 分页获取用户账户列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取用户账户列表")
    public Result<PageResult<UserAccount>> getUserAccountPage(
            @Parameter(description = "页码", required = true) @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", required = true) @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键字(用户名/昵称/邮箱/手机)") @RequestParam(required = false) String keyword,
            @Parameter(description = "账户状态: 0-冻结, 1-正常") @RequestParam(required = false) Integer status) {

        PageResult<UserAccount> pageResult = userAccountService.getUserAccountPage(page, size, keyword, status);
        return Result.success(pageResult);
    }

    /**
     * 获取用户账户详情
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户账户详情")
    public Result<UserAccount> getUserAccount(
            @Parameter(description = "用户ID", required = true) @PathVariable Integer userId) {

        UserAccount userAccount = userAccountService.getUserAccountByUserId(userId);
        return Result.success(userAccount);
    }

    /**
     * 管理员给用户充值
     */
    @PostMapping("/recharge")
    @Operation(summary = "管理员给用户充值")
    public Result<Void> recharge(@Valid @RequestBody RechargeRequestDTO rechargeRequest) {
        userAccountService.rechargeUserAccount(rechargeRequest);
        return Result.success();
    }

    /**
     * 管理员调整用户余额
     */
    @PutMapping("/{userId}/balance")
    @Operation(summary = "管理员调整用户余额")
    public Result<Void> adjustBalance(
            @Parameter(description = "用户ID", required = true) @PathVariable Integer userId,
            @Parameter(description = "调整金额", required = true) @RequestParam BigDecimal amount,
            @Parameter(description = "调整原因", required = true) @RequestParam String reason) {

        userAccountService.adjustUserBalance(userId, amount, reason);
        return Result.success();
    }

    /**
     * 更改用户账户状态（冻结/解冻）
     */
    @PutMapping("/{userId}/status")
    @Operation(summary = "更改用户账户状态")
    public Result<Void> toggleStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Integer userId,
            @Parameter(description = "账户状态: 0-冻结, 1-正常", required = true) @RequestParam Integer status,
            @Parameter(description = "操作原因") @RequestParam(required = false) String reason) {

        userAccountService.toggleUserAccountStatus(userId, status, reason);
        return Result.success();
    }

    /**
     * 分页获取交易记录列表
     */
    @GetMapping("/transactions/page")
    @Operation(summary = "分页获取交易记录列表")
    public Result<PageResult<AccountTransaction>> getTransactionPage(
            @Parameter(description = "页码", required = true) @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", required = true) @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Integer userId,
            @Parameter(description = "交易类型: 1-充值，2-消费，3-退款，4-管理员调整") @RequestParam(required = false) Integer type,
            @Parameter(description = "交易状态: 0-失败，1-成功，2-处理中") @RequestParam(required = false) Integer status,
            @Parameter(description = "支付方式") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "交易流水号") @RequestParam(required = false) String transactionNo,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @Parameter(description = "搜索关键字(用户名/昵称/邮箱/手机)") @RequestParam(required = false) String keyword)
            throws ParseException {

        TransactionQueryDTO queryDTO = new TransactionQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setType(type);
        queryDTO.setStatus(status);
        queryDTO.setPaymentMethod(paymentMethod);
        queryDTO.setTransactionNo(transactionNo);

        // 日期转换
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (startTime != null && !startTime.isEmpty()) {
            queryDTO.setStartTime(dateFormat.parse(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            queryDTO.setEndTime(dateFormat.parse(endTime));
        }

        queryDTO.setKeyword(keyword);

        PageResult<AccountTransaction> pageResult = userAccountService.getTransactionPage(page, size, queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取交易记录详情
     */
    @GetMapping("/transactions/{id}")
    @Operation(summary = "获取交易记录详情")
    public Result<AccountTransaction> getTransactionDetail(
            @Parameter(description = "交易记录ID", required = true) @PathVariable Integer id) {

        AccountTransaction transaction = userAccountService.getTransactionById(id);
        return Result.success(transaction);
    }
}