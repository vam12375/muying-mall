package com.muyingmall.controller.common;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.muyingmall.common.api.Result;
import com.muyingmall.config.AlipayConfig;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.Payment;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserAccount;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.mapper.OrderMapper;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.UserService;
import com.muyingmall.service.UserAccountService;
import com.muyingmall.service.CacheRefreshService;
import com.muyingmall.service.OrderNotificationService;
import com.muyingmall.service.MessageProducerService;
import com.muyingmall.dto.PaymentMessage;
import com.muyingmall.util.EnumUtil;
import com.muyingmall.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "支付接口", description = "处理支付相关操作")
public class PaymentController {

    // 支付状态常量定义（保留整数值常量，用于比较和显示）
    private static final int PAYMENT_STATUS_PENDING = 0; // 待支付
    private static final int PAYMENT_STATUS_PROCESSING = 1; // 支付中
    private static final int PAYMENT_STATUS_SUCCESS = 2; // 支付成功
    private static final int PAYMENT_STATUS_FAILED = 3; // 支付失败
    private static final int PAYMENT_STATUS_CLOSED = 4; // 已关闭

    // 订单状态常量定义
    private static final String ORDER_STATUS_PENDING_PAYMENT = "pending_payment"; // 待支付
    private static final String ORDER_STATUS_PENDING_SHIPMENT = "pending_shipment"; // 待发货

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final UserService userService;
    private final UserAccountService userAccountService; // 注入UserAccountService
    private final AlipayConfig alipayConfig;
    private final CacheRefreshService cacheRefreshService; // 注入缓存刷新服务
    private final OrderNotificationService orderNotificationService; // 注入订单通知服务
    private final MessageProducerService messageProducerService; // 注入消息发送服务
    @Autowired
    private OrderMapper orderMapper; // 注入OrderMapper

    @Autowired(required = false)
    private DataSource dataSource; // 注入数据源，用于JDBC操作

    @Autowired
    private RedisTemplate<String, Object> redisTemplate; // 注入Redis模板，用于缓存操作

    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * 创建支付宝支付
     */
    @PostMapping("/alipay/create/{orderId}")
    @Operation(summary = "创建支付宝支付订单")
    @Transactional
    public Result<Map<String, Object>> createAlipayPayment(@PathVariable("orderId") Integer orderId) {
        // 获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            // 获取订单信息
            Order order = orderService.getById(orderId);
            if (order == null) {
                return Result.error(404, "订单不存在");
            }

            if (!order.getUserId().equals(user.getUserId())) {
                return Result.error(403, "无权访问此订单");
            }

            if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                return Result.error(400, "订单状态不是待支付状态");
            }

            // 创建支付记录
            String paymentNo = generatePaymentNo();
            Payment payment = new Payment();
            payment.setPaymentNo(paymentNo);
            payment.setOrderId(order.getOrderId());
            payment.setOrderNo(order.getOrderNo());
            payment.setUserId(user.getUserId());
            payment.setAmount(order.getActualAmount());
            payment.setPaymentMethod("alipay");
            payment.setStatus(PaymentStatus.PENDING); // 使用枚举值
            payment.setCreateTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());
            payment.setExpireTime(LocalDateTime.now().plusHours(2)); // 2小时后过期
            payment.setNotifyUrl(alipayConfig.getNotifyUrl());
            payment.setReturnUrl(alipayConfig.getReturnUrl());

            // 保存支付记录
            paymentService.createPayment(payment);

            // 更新订单支付ID和支付方式
            order.setPaymentId(payment.getId());
            order.setPaymentMethod("alipay");
            order.setUpdateTime(LocalDateTime.now());
            orderService.updateById(order);

            // 发送支付请求消息到RabbitMQ
            try {
                PaymentMessage paymentMessage = PaymentMessage.createRequestMessage(payment);
                messageProducerService.sendPaymentMessage(paymentMessage);
                log.info("支付宝支付请求消息发送成功: paymentId={}", payment.getId());
            } catch (Exception e) {
                log.error("支付宝支付请求消息发送失败: paymentId={}, error={}", payment.getId(), e.getMessage(), e);
                // 消息发送失败不影响支付流程
            }

            // 调用支付宝接口获取支付链接
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGatewayUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    "UTF-8",
                    alipayConfig.getPublicKey(),
                    "RSA2");

            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());
            alipayRequest.setReturnUrl(alipayConfig.getReturnUrl());

            // 构建支付请求参数
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", payment.getPaymentNo());
            bizContent.put("total_amount", payment.getAmount().toString());
            bizContent.put("subject", "母婴商城订单-" + order.getOrderNo());
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            // 设置订单过期时间
            bizContent.put("timeout_express", "2h");

            alipayRequest.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));

            // 发送支付请求获取支付链接
            String form = alipayClient.pageExecute(alipayRequest).getBody();

            // 返回支付信息
            Map<String, Object> result = new HashMap<>();
            result.put("paymentId", payment.getId());
            result.put("paymentNo", payment.getPaymentNo());
            result.put("amount", payment.getAmount());
            result.put("form", form); // 支付宝表单

            return Result.success(result, "创建支付成功");
        } catch (Exception e) {
            log.error("创建支付宝支付失败", e);
            return Result.error("创建支付失败: " + e.getMessage());
        }
    }

    /**
     * 创建微信沙箱模拟支付订单
     */
    @PostMapping("/wechat/sandbox/{orderId}")
    @Operation(summary = "创建微信沙箱模拟支付订单")
    @Transactional
    public Result<Map<String, Object>> createWechatSandboxPayment(@PathVariable("orderId") Integer orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            Order order = orderService.getById(orderId);
            if (order == null) {
                return Result.error(404, "订单不存在");
            }
            if (!order.getUserId().equals(user.getUserId())) {
                return Result.error(403, "无权操作此订单");
            }
            if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                return Result.error(400, "订单状态非待支付");
            }

            // 创建支付记录
            String paymentNo = generatePaymentNo();
            Payment payment = new Payment();
            payment.setPaymentNo(paymentNo);
            payment.setOrderId(order.getOrderId());
            payment.setOrderNo(order.getOrderNo());
            payment.setUserId(user.getUserId());
            payment.setAmount(order.getActualAmount());
            payment.setPaymentMethod("wechat"); // 支付方式：微信
            payment.setStatus(PaymentStatus.PENDING); // 初始状态为待支付
            payment.setCreateTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());
            payment.setExpireTime(LocalDateTime.now().plusHours(2)); // 假设2小时过期
            // For sandbox, notifyUrl and returnUrl might not be strictly necessary if
            // success is simulated immediately
            // or handled by polling from frontend.
            // payment.setReturnUrl(frontendUrl + "/payment/result?paymentId=" +
            // payment.getId()); // Example

            paymentService.createPayment(payment);

            // 更新订单的支付ID和支付方式
            order.setPaymentId(payment.getId());
            order.setPaymentMethod("wechat");
            order.setUpdateTime(LocalDateTime.now());
            orderService.updateById(order);

            // 发送支付请求消息到RabbitMQ
            try {
                PaymentMessage paymentMessage = PaymentMessage.createRequestMessage(payment);
                messageProducerService.sendPaymentMessage(paymentMessage);
                log.info("微信支付请求消息发送成功: paymentId={}", payment.getId());
            } catch (Exception e) {
                log.error("微信支付请求消息发送失败: paymentId={}, error={}", payment.getId(), e.getMessage(), e);
                // 消息发送失败不影响支付流程
            }

            // --- 模拟微信沙箱支付成功 ---
            String mockTransactionId = "WX" + UUID.randomUUID().toString().replace("-", "");
            // 直接更新支付和订单状态为成功
            // The existing updatePaymentAndOrderStatus method updates payment status to
            // SUCCESS
            // and then calls updateOrderStatusAfterPayment.
            updatePaymentAndOrderStatus(payment.getId(), mockTransactionId);
            // payment object itself is not updated here with status SUCCESS yet by
            // updatePaymentAndOrderStatus,
            // but database record will be. For response, we can fetch it or assume success.

            Map<String, Object> result = new HashMap<>();
            result.put("paymentId", payment.getId());
            result.put("paymentNo", payment.getPaymentNo());
            result.put("message", "微信沙箱支付模拟成功");
            result.put("status", PaymentStatus.SUCCESS.getCode()); // Reflecting immediate success

            return Result.success(result, "微信沙箱支付创建并模拟成功");

        } catch (Exception e) {
            log.error("创建微信沙箱支付失败", e);
            return Result.error("创建微信沙箱支付失败: " + e.getMessage());
        }
    }

    /**
     * 创建微信沙箱模拟支付订单（兼容原始路径）
     */
    @PostMapping("/wechat/sandbox/create/{orderId}")
    @Operation(summary = "创建微信沙箱模拟支付订单（兼容原始路径）")
    @Transactional
    public Result<Map<String, Object>> createWechatSandboxPaymentCompat(@PathVariable("orderId") Integer orderId) {
        // 委托给新的实现方法，确保兼容性
        return createWechatSandboxPayment(orderId);
    }

    /**
     * 创建钱包支付订单
     */
    @PostMapping("/wallet/create/{orderId}")
    @Operation(summary = "创建钱包支付订单")
    @Transactional
    public Result<Map<String, Object>> createWalletPayment(@PathVariable("orderId") Integer orderId,
            @RequestBody(required = false) Map<String, Object> requestParams) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            // 添加详细的请求参数日志
            log.info("钱包支付请求参数: orderId={}, requestParams={}, requestParams类型={}",
                    orderId, requestParams, requestParams != null ? requestParams.getClass().getName() : "null");

            // 检查请求参数中的每个键值对
            if (requestParams != null) {
                requestParams.forEach((key, value) -> {
                    log.info("参数键值对: key={}, value={}, value类型={}",
                            key, value, value != null ? value.getClass().getName() : "null");
                });
            }

            Order order = orderService.getById(orderId);
            if (order == null) {
                return Result.error(404, "订单不存在");
            }
            if (!order.getUserId().equals(user.getUserId())) {
                return Result.error(403, "无权操作此订单");
            }
            if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                return Result.error(400, "订单状态非待支付");
            }

            // 从订单获取金额 - 不依赖前端传递
            BigDecimal orderAmount = order.getActualAmount();

            // 获取用户的实际账户余额 - 从UserAccount表获取
            UserAccount userAccount = userAccountService.getUserAccountByUserId(user.getUserId());
            if (userAccount == null) {
                log.error("用户账户不存在: userId={}", user.getUserId());
                return Result.error(404, "用户账户不存在");
            }

            BigDecimal userBalance = userAccount.getBalance() == null ? BigDecimal.ZERO : userAccount.getBalance();

            // 详细记录用于比较的金额值
            log.info("钱包支付 - 订单金额(数据库): {}, 用户余额(user_account表): {}", orderAmount, userBalance);

            // 精确比较BigDecimal值，避免精度问题
            int comparisonResult = userBalance.compareTo(orderAmount);
            log.info("余额比较结果: {} (负数表示余额不足，0或正数表示余额足够)", comparisonResult);

            if (comparisonResult < 0) {
                log.warn("钱包支付失败 - 余额不足: 用户ID={}, 订单ID={}, 订单金额={}, 用户余额={}, 差额={}",
                        user.getUserId(), orderId, orderAmount, userBalance,
                        orderAmount.subtract(userBalance).setScale(2, java.math.RoundingMode.HALF_UP));
                return Result.error(400, "钱包余额不足，当前余额：" + userBalance + "，订单金额：" + orderAmount);
            }

            log.info("余额检查通过，开始钱包支付");

            // 使用钱包支付订单（会创建正确的消费交易记录）
            boolean paymentSuccess = userAccountService.payOrderByWallet(user.getUserId(), orderId, orderAmount);
            if (!paymentSuccess) {
                log.error("钱包支付失败: 用户ID={}, 订单ID={}, 金额={}", user.getUserId(), orderId, orderAmount);
                return Result.error("钱包支付失败");
            }
            log.info("钱包支付成功，扣减金额: {}", orderAmount);

            // 创建支付记录
            String paymentNo = generatePaymentNo();
            log.info("生成支付单号: {}", paymentNo);

            Payment payment = new Payment();
            payment.setPaymentNo(paymentNo); // 确保设置支付单号
            payment.setOrderId(order.getOrderId());
            payment.setOrderNo(order.getOrderNo());
            payment.setUserId(user.getUserId());
            payment.setAmount(orderAmount);
            payment.setPaymentMethod("wallet"); // 支付方式：钱包
            payment.setStatus(PaymentStatus.SUCCESS); // 钱包支付通常立即成功
            payment.setPayTime(LocalDateTime.now()); // 记录支付时间
            payment.setTransactionId("WALLET" + UUID.randomUUID().toString().replace("-", ""));
            payment.setCreateTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());
            // ExpireTime might not be relevant for immediate success
            // payment.setExpireTime(LocalDateTime.now().plusHours(2));

            log.info("即将创建支付记录: {}", payment);
            paymentService.createPayment(payment);
            log.info("支付记录创建成功: ID={}, PaymentNo={}", payment.getId(), payment.getPaymentNo());

            // 更新订单的支付ID和支付方式
            order.setPaymentId(payment.getId());
            order.setPaymentMethod("wallet");
            order.setUpdateTime(LocalDateTime.now());
            // Order status will be updated by updatePaymentAndOrderStatus
            orderService.updateById(order);
            log.info("订单支付信息更新成功: OrderID={}, PaymentID={}", order.getOrderId(), payment.getId());

            // 发送支付请求消息到RabbitMQ（钱包支付立即成功）
            try {
                PaymentMessage paymentMessage = PaymentMessage.createRequestMessage(payment);
                messageProducerService.sendPaymentMessage(paymentMessage);
                log.info("钱包支付请求消息发送成功: paymentId={}", payment.getId());
            } catch (Exception e) {
                log.error("钱包支付请求消息发送失败: paymentId={}, error={}", payment.getId(), e.getMessage(), e);
                // 消息发送失败不影响支付流程
            }

            // 更新支付和订单状态 (订单状态将变为待发货等)
            updatePaymentAndOrderStatus(payment.getId(), payment.getTransactionId());
            log.info("支付和订单状态更新成功");

            Map<String, Object> result = new HashMap<>();
            result.put("paymentId", payment.getId());
            result.put("paymentNo", payment.getPaymentNo());
            result.put("message", "钱包支付成功");
            result.put("status", PaymentStatus.SUCCESS.getCode());

            return Result.success(result, "钱包支付成功");

        } catch (BusinessException e) { // Catch specific business exceptions like Insufficient Balance
            log.warn("钱包支付业务异常: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建钱包支付失败", e);
            // Rollback will be handled by @Transactional
            return Result.error("创建钱包支付失败: " + e.getMessage());
        }
    }

    /**
     * 支付宝异步通知
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(values[i]);
                if (i != values.length - 1) {
                    valueStr.append(",");
                }
            }
            params.put(key, valueStr.toString());
        }

        try {
            log.info("支付宝异步通知参数: {}", params);

            // 验签
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getPublicKey(),
                    "UTF-8",
                    "RSA2");
            log.info("支付宝异步通知验签结果: {}", signVerified);

            if (signVerified) {
                String paymentNo = params.get("out_trade_no");
                String tradeStatus = params.get("trade_status");
                String tradeNo = params.get("trade_no"); // 支付宝交易号
                String gmtPayment = params.get("gmt_payment"); // 交易付款时间
                String totalAmount = params.get("total_amount"); // 订单金额

                log.info("异步通知 - Payment No: {}, Trade Status: {}, Trade No: {}, Payment Time: {}, Amount: {}",
                        paymentNo, tradeStatus, tradeNo, gmtPayment, totalAmount);

                // 查询本地支付记录
                Payment payment = paymentService.getByPaymentNo(paymentNo);
                if (payment == null) {
                    log.error("异步通知 - 未找到支付记录: {}", paymentNo);
                    return "failure";
                }

                log.info("异步通知 - 找到支付记录: ID {}, OrderID {}, Status {}",
                        payment.getId(), payment.getOrderId(), payment.getStatus());

                // 根据通知类型处理
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 如果支付已经成功，避免重复处理
                    if (PaymentStatus.SUCCESS.equals(payment.getStatus())) {
                        log.info("异步通知 - 支付已处理，跳过: {}", paymentNo);
                        return "success";
                    }

                    try {
                        // 更新支付记录和订单状态
                        payment.setTransactionId("AP-" + tradeNo);
                        payment.setPayTime(LocalDateTime.now());
                        payment.setUpdateTime(LocalDateTime.now());
                        updatePaymentAndOrderStatus(payment.getId(), tradeNo);
                        log.info("异步通知 - 订单状态更新成功: {}", paymentNo);
                        return "success";
                    } catch (Exception e) {
                        log.error("异步通知 - 更新支付记录和订单状态失败: {}", e.getMessage(), e);
                        
                        // 发送支付失败消息到RabbitMQ
                        try {
                            PaymentMessage paymentMessage = PaymentMessage.createFailedMessage(payment, "支付状态更新失败: " + e.getMessage());
                            messageProducerService.sendPaymentMessage(paymentMessage);
                            log.info("支付失败消息发送成功: paymentId={}", payment.getId());
                        } catch (Exception msgException) {
                            log.error("支付失败消息发送失败: paymentId={}, error={}", payment.getId(), msgException.getMessage(), msgException);
                        }
                        
                        // 支付宝异步通知，返回failure会触发重试
                        return "failure";
                    }
                } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                    // 交易关闭
                    try {
                        payment.setStatus(PaymentStatus.CLOSED);
                        payment.setUpdateTime(LocalDateTime.now());
                        paymentService.updateById(payment);
                        
                        // 发送支付失败消息到RabbitMQ
                        try {
                            PaymentMessage paymentMessage = PaymentMessage.createFailedMessage(payment, "交易关闭");
                            messageProducerService.sendPaymentMessage(paymentMessage);
                            log.info("交易关闭消息发送成功: paymentId={}", payment.getId());
                        } catch (Exception msgException) {
                            log.error("交易关闭消息发送失败: paymentId={}, error={}", payment.getId(), msgException.getMessage(), msgException);
                        }
                        
                        log.info("异步通知 - 交易关闭状态更新成功: {}", paymentNo);
                        return "success";
                    } catch (Exception e) {
                        log.error("异步通知 - 更新交易关闭状态失败: {}", e.getMessage(), e);
                        return "failure";
                    }
                } else {
                    // 其他状态，记录但不处理
                    log.info("异步通知 - 收到其他交易状态: {}, {}", paymentNo, tradeStatus);
                    return "success";
                }
            } else {
                log.error("异步通知 - 验签失败");
                return "failure";
            }
        } catch (AlipayApiException e) {
            log.error("异步通知 - 验签异常: {}", e.getMessage(), e);
            return "failure";
        } catch (Exception e) {
            log.error("异步通知 - 处理异常: {}", e.getMessage(), e);
            return "failure";
        }
    }

    /**
     * 支付宝同步回调
     */
    @GetMapping("/alipay/return")
    public void alipayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(values[i]);
                if (i != values.length - 1) {
                    valueStr.append(",");
                }
            }
            params.put(key, valueStr.toString());
        }

        log.info("支付宝同步回调参数: {}", params);

        boolean signVerified = false;
        String paymentResult = "unknown";
        Integer orderId = null;
        String paymentNo = null;

        try {
            signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getPublicKey(),
                    "UTF-8",
                    "RSA2");
            log.info("支付宝同步回调验签结果: {}", signVerified);

            if (signVerified) {
                // 验签成功，获取支付信息
                paymentNo = params.get("out_trade_no");
                String tradeNo = params.get("trade_no"); // 支付宝交易号
                log.info("同步回调 - Payment No: {}, Trade No: {}", paymentNo, tradeNo);

                Payment payment = paymentService.getByPaymentNo(paymentNo);

                if (payment != null) {
                    orderId = payment.getOrderId();
                    log.info("同步回调 - 找到支付记录: Payment ID {}, Order ID: {}", payment.getId(), orderId);

                    // 仅查询支付状态，不进行更新
                    try {
                        queryAlipayTradeStatus(payment);
                        paymentResult = "success";
                        log.info("同步回调 - 支付状态检查完成，准备重定向到成功页面");
                    } catch (Exception e) {
                        log.error("同步回调 - 查询支付状态失败: {}", e.getMessage(), e);
                        paymentResult = "error";
                    }
                } else {
                    log.warn("同步回调 - 未找到支付记录: Payment No {}", paymentNo);
                    paymentResult = "not_found";
                }
            } else {
                log.error("支付宝同步回调验签失败");
                paymentResult = "invalid_sign";
            }
        } catch (AlipayApiException e) {
            log.error("支付宝同步回调验签异常: {}", e.getMessage(), e);
            paymentResult = "error";
        } catch (Exception e) {
            log.error("支付宝同步回调处理异常: {}", e.getMessage(), e);
            paymentResult = "error";
        } finally {
            // 构建重定向URL
            StringBuilder redirectUrl = new StringBuilder(frontendUrl);

            if (orderId != null) {
                redirectUrl.append("/order/").append(orderId);
            } else {
                redirectUrl.append("/orders");
            }

            redirectUrl.append("?paymentResult=").append(paymentResult);
            if (paymentNo != null) {
                redirectUrl.append("&paymentNo=").append(paymentNo);
            }

            try {
                log.info("重定向到: {}", redirectUrl);
                response.sendRedirect(redirectUrl.toString());
            } catch (IOException e) {
                log.error("重定向失败: {}", e.getMessage(), e);
                // 最后的尝试 - 如果之前的重定向都失败了，尝试简单输出
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().write("<script>window.location.href='" + redirectUrl + "';</script>");
            }
        }
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{paymentId}")
    @Operation(summary = "查询支付状态")
    public Result<Map<String, Object>> queryPaymentStatus(@PathVariable("paymentId") Long paymentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Payment payment = paymentService.getById(paymentId);
        if (payment == null) {
            return Result.error(404, "支付记录不存在");
        }

        if (!payment.getUserId().equals(user.getUserId())) {
            return Result.error(403, "无权访问此支付记录");
        }

        // 如果状态是待支付或支付中，则向支付宝查询最新状态
        if (PaymentStatus.PENDING.equals(payment.getStatus()) || PaymentStatus.PROCESSING.equals(payment.getStatus())) {
            try {
                checkAlipayTradeStatus(payment);
            } catch (Exception e) {
                log.error("查询支付宝交易状态失败", e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getId());
        result.put("paymentNo", payment.getPaymentNo());
        result.put("orderId", payment.getOrderId());
        result.put("orderNo", payment.getOrderNo());
        result.put("amount", payment.getAmount());
        result.put("status", EnumUtil.getPaymentStatusCode(payment.getStatus())); // 转换为整数码
        result.put("paymentMethod", payment.getPaymentMethod());
        result.put("createTime", payment.getCreateTime());
        result.put("updateTime", payment.getUpdateTime());
        result.put("paymentTime", payment.getPaymentTime());

        return Result.success(result);
    }

    /**
     * 关闭支付订单
     */
    @PostMapping("/close/{paymentId}")
    @Operation(summary = "关闭支付订单")
    public Result<Boolean> closePayment(@PathVariable("paymentId") Long paymentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Payment payment = paymentService.getById(paymentId);
        if (payment == null) {
            return Result.error(404, "支付记录不存在");
        }

        if (!payment.getUserId().equals(user.getUserId())) {
            return Result.error(403, "无权操作此支付记录");
        }

        if (!PaymentStatus.PENDING.equals(payment.getStatus())
                && !PaymentStatus.PROCESSING.equals(payment.getStatus())) {
            return Result.error(400, "当前支付状态不可关闭");
        }

        // 更新支付状态为已关闭
        payment.setStatus(PaymentStatus.CLOSED); // 使用枚举值
        payment.setUpdateTime(LocalDateTime.now());
        boolean result = paymentService.updateById(payment);

        if (result) {
            // 发送支付关闭消息到RabbitMQ
            try {
                PaymentMessage paymentMessage = PaymentMessage.createFailedMessage(payment, "用户主动关闭支付");
                messageProducerService.sendPaymentMessage(paymentMessage);
                log.info("支付关闭消息发送成功: paymentId={}", payment.getId());
            } catch (Exception e) {
                log.error("支付关闭消息发送失败: paymentId={}, error={}", payment.getId(), e.getMessage(), e);
                // 消息发送失败不影响关闭流程
            }

            // 更新订单状态为已取消
            Order order = orderService.getById(payment.getOrderId());
            if (order != null) {
                order.setStatus(OrderStatus.CANCELLED); // 使用枚举值
                order.setUpdateTime(LocalDateTime.now());
                orderService.updateById(order);
            }
            return Result.success(true, "支付订单已关闭");
        } else {
            return Result.error("关闭支付订单失败");
        }
    }

    /**
     * 查询订单支付信息
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "查询订单支付信息")
    public Result<Payment> getOrderPaymentInfo(@PathVariable("orderId") Integer orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Order order = orderService.getById(orderId);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }

        if (!order.getUserId().equals(user.getUserId())) {
            return Result.error(403, "无权访问此订单");
        }

        Payment payment = paymentService.getById(order.getPaymentId());
        if (payment == null) {
            return Result.error(404, "支付记录不存在");
        }

        // 如果payment状态为成功，但order状态仍为待支付，自动修复一下
        if (PaymentStatus.SUCCESS.equals(payment.getStatus())
                && OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
            log.info("检测到支付已成功但订单状态未更新，自动修复订单状态: Order ID: {}", order.getOrderId());
            try {
                updateOrderStatusAfterPayment(payment);
                // 重新查询更新后的订单信息
                order = orderService.getById(orderId);
                log.info("订单状态修复结果: Order ID: {}, New Status: {}", order.getOrderId(), order.getStatus());
            } catch (Exception e) {
                log.error("自动修复订单状态失败", e);
            }
        }

        // 如果支付状态是待支付，但支付已经过期，则查询一下最新状态
        if (PaymentStatus.PENDING.equals(payment.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            if (payment.getExpireTime() != null && now.isAfter(payment.getExpireTime())) {
                log.info("支付已过期，查询最新状态: Payment ID: {}", payment.getId());
                try {
                    queryAlipayTradeStatus(payment);
                    // 重新查询更新后的支付信息
                    payment = paymentService.getById(order.getPaymentId());
                } catch (Exception e) {
                    log.error("查询过期支付状态失败", e);
                }
            }
        }

        return Result.success(payment);
    }

    /**
     * 向支付宝查询交易状态并更新本地支付记录
     */
    @Transactional
    private void checkAlipayTradeStatus(Payment payment) throws AlipayApiException {
        if (!"alipay".equals(payment.getPaymentMethod())) {
            return;
        }

        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json",
                "UTF-8",
                alipayConfig.getPublicKey(),
                "RSA2");

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", payment.getPaymentNo());
        request.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));

        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            String tradeStatus = response.getTradeStatus();
            String tradeNo = response.getTradeNo();

            // 幂等性检查：如果已经是支付成功状态，不再重复更新
            if (PaymentStatus.SUCCESS.equals(payment.getStatus())) {
                log.info("支付已处理为成功状态，跳过更新: Payment ID: {}", payment.getId());
                // 仍然检查订单状态是否已更新
                updateOrderStatusAfterPayment(payment);
                return;
            }

            // 更新本地支付记录
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                log.info("支付成功 (Check Status), Payment ID: {}, Alipay TradeNo: {}", payment.getId(), tradeNo);
                payment.setStatus(PaymentStatus.SUCCESS); // 使用枚举值
                payment.setTransactionId("AP-" + tradeNo); // 添加前缀
                payment.setPayTime(LocalDateTime.now()); // 使用 payTime
                payment.setUpdateTime(LocalDateTime.now());

                log.info("准备更新支付记录 (Check Status): {}", payment);
                boolean paymentUpdated = paymentService.updateById(payment);
                log.info("支付记录更新结果 (Check Status): {}", paymentUpdated);

                if (paymentUpdated) {
                    // 更新订单状态
                    updateOrderStatusAfterPayment(payment);
                } else {
                    log.error("支付记录更新失败 (Check Status), Payment ID: {}", payment.getId());
                }
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                payment.setStatus(PaymentStatus.CLOSED); // 使用枚举值
                payment.setUpdateTime(LocalDateTime.now());
                paymentService.updateById(payment);
                log.info("支付记录状态更新为已关闭 (Check Status), Payment ID: {}", payment.getId());
            }
        } else {
            log.error("查询支付宝交易失败: Code={}, Message={}", response.getCode(), response.getMsg());
        }
    }

    /**
     * 仅查询支付宝交易状态，不进行数据库更新操作
     */
    private void queryAlipayTradeStatus(Payment payment) throws AlipayApiException {
        if (!"alipay".equals(payment.getPaymentMethod())) {
            return;
        }

        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json",
                "UTF-8",
                alipayConfig.getPublicKey(),
                "RSA2");

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", payment.getPaymentNo());
        request.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));

        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            String tradeStatus = response.getTradeStatus();
            String tradeNo = response.getTradeNo();
            log.info("查询支付宝交易状态成功 - Payment ID: {}, TradeStatus: {}, TradeNo: {}",
                    payment.getId(), tradeStatus, tradeNo);

            // 检测是否为支付成功状态，如果是且本地状态未更新，则触发状态更新
            if (("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))
                    && !PaymentStatus.SUCCESS.equals(payment.getStatus())) {
                log.info("检测到支付成功但本地状态未更新，触发状态更新 - Payment ID: {}", payment.getId());
                try {
                    // 在新事务中更新支付和订单状态
                    updatePaymentAndOrderStatus(payment.getId(), tradeNo);
                } catch (Exception e) {
                    log.error("同步回调中更新支付状态失败", e);
                    // 更新失败不影响用户体验，用户仍会被重定向到订单页面
                }
            }
        } else {
            log.warn("查询支付宝交易状态失败: Code={}, Message={}", response.getCode(), response.getMsg());
        }
    }

    /**
     * 在新事务中更新支付和订单状态
     * 使用REQUIRES_NEW确保无论外部是否有事务，都会创建新事务
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updatePaymentAndOrderStatus(Long paymentId, String tradeNo) {
        try {
            // 查询支付记录
            Payment payment = paymentService.getById(paymentId);
            if (payment == null) {
                log.error("未找到支付记录: Payment ID {}", paymentId);
                return;
            }

            // 幂等性检查：如果已经是支付成功状态，不再重复更新
            if (PaymentStatus.SUCCESS.equals(payment.getStatus())) {
                log.info("支付已经是成功状态，无需更新: Payment ID {}", paymentId);
                // 仍然检查订单状态，确保订单状态和支付状态一致
                updateOrderStatusAfterPayment(payment);
                return;
            }

            // 更新支付记录状态
            payment.setStatus(PaymentStatus.SUCCESS); // 使用枚举值

            // 根据支付方式设置交易流水号
            String paymentMethod = payment.getPaymentMethod();
            if ("alipay".equals(paymentMethod)) {
                // 支付宝支付添加AP-前缀
                payment.setTransactionId("AP" + tradeNo);
            } else {
                // 微信支付和钱包支付使用原始流水号
                payment.setTransactionId(tradeNo);
            }

            payment.setPayTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());

            boolean paymentUpdated = paymentService.updateById(payment);
            log.info("支付状态更新结果: {}, Payment ID: {}", paymentUpdated, paymentId);

            if (paymentUpdated) {
                // 发送支付成功消息到RabbitMQ
                try {
                    PaymentMessage paymentMessage = PaymentMessage.createSuccessMessage(payment);
                    messageProducerService.sendPaymentMessage(paymentMessage);
                    log.info("支付成功消息发送成功: paymentId={}", payment.getId());
                } catch (Exception e) {
                    log.error("支付成功消息发送失败: paymentId={}, error={}", payment.getId(), e.getMessage(), e);
                    // 消息发送失败不影响支付流程
                }

                // 更新订单状态
                updateOrderStatusAfterPayment(payment);
            } else {
                log.error("支付记录更新失败，无法更新订单状态: Payment ID {}", paymentId);
            }
        } catch (Exception e) {
            log.error("更新支付和订单状态异常: Payment ID {}, 错误: {}", paymentId, e.getMessage(), e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }

    /**
     * 更新订单状态为支付成功后的状态
     */
    private void updateOrderStatusAfterPayment(Payment payment) {
        try {
            Order order = orderService.getById(payment.getOrderId());
            if (order == null) {
                log.error("找不到对应的订单: Order ID {}", payment.getOrderId());
                return;
            }

            OrderStatus targetStatus = OrderStatus.getByCode(ORDER_STATUS_PENDING_SHIPMENT);
            if (targetStatus == null) {
                log.error("无效的订单状态编码: {}", ORDER_STATUS_PENDING_SHIPMENT);
                return;
            }

            if (!order.getStatus().canTransitionTo(targetStatus)) {
                log.warn("订单状态无法从 {} 转换为 {}: Order ID {}",
                        order.getStatus().getDesc(), targetStatus.getDesc(), order.getOrderId());
                return;
            }

            log.info("订单 (ID: {}) 当前状态: {}, 目标状态: {}",
                    order.getOrderId(), order.getStatus(), targetStatus);
            log.info("准备更新订单 (ID: {}) 状态为: {}", order.getOrderId(), targetStatus);
            log.info("使用JdbcTemplate更新订单状态，确保SQL精确");

            try {
                if (dataSource == null) {
                    log.error("DataSource未注入，无法执行JDBC操作");
                    return;
                }
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                String sql = "UPDATE `order` SET `status` = ?, `pay_time` = ?, `update_time` = NOW() WHERE `order_id` = ?";

                log.info("执行SQL: {} 带参数: status={}, pay_time={}, order_id={}",
                        sql, targetStatus.getCode(), payment.getPayTime(), order.getOrderId());

                int result = jdbcTemplate.update(
                        sql,
                        targetStatus.getCode(),
                        Timestamp.valueOf(payment.getPayTime()),
                        order.getOrderId());

                if (result > 0) {
                    log.info("JdbcTemplate订单状态已成功更新为{}: Order ID {}", targetStatus.getDesc(), order.getOrderId());

                    // 立即刷新订单相关缓存
                    cacheRefreshService.refreshOrderCache(order.getOrderId(), order.getUserId());

                    // 发布订单状态变更事件
                    publishOrderStatusChangeEvent(order, targetStatus);
                } else {
                    log.error("JdbcTemplate订单状态更新失败，影响行数为0: Order ID {}", order.getOrderId());
                }
            } catch (Exception e) {
                log.error("使用JdbcTemplate更新订单状态异常: {}", e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("更新订单状态顶层异常: Payment ID {}, Order ID {}, 错误: {}",
                    payment.getId(), payment.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 发布订单状态变更事件
     */
    private void publishOrderStatusChangeEvent(Order order, OrderStatus newStatus) {
        try {
            // 发布订单状态变更事件
            log.info("发布订单状态变更事件: orderId={}, oldStatus={}, newStatus={}",
                    order.getOrderId(), order.getStatus(), newStatus);

            // 通知订单状态变更
            orderNotificationService.notifyOrderStatusChange(
                    order.getOrderId(),
                    order.getUserId(),
                    order.getStatus().getCode(),
                    newStatus.getCode(),
                    "payment_success");

            // 发送实时同步通知
            orderNotificationService.notifyRealTimeSync(order.getOrderId(), order.getUserId());

            // 通知缓存刷新
            orderNotificationService.notifyCacheRefresh(order.getOrderId(), order.getUserId(), "order_detail");

        } catch (Exception e) {
            log.error("发布订单状态变更事件失败: orderId={}, error={}", order.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 处理退款
     */
    @PostMapping("/refund/{paymentId}")
    @Operation(summary = "处理退款")
    @Transactional
    public Result<Boolean> processRefund(@PathVariable("paymentId") Long paymentId,
                                       @RequestBody Map<String, Object> refundRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Result.error(401, "用户未认证");
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            Payment payment = paymentService.getById(paymentId);
            if (payment == null) {
                return Result.error(404, "支付记录不存在");
            }

            if (!payment.getUserId().equals(user.getUserId())) {
                return Result.error(403, "无权操作此支付记录");
            }

            // 获取退款金额和原因
            BigDecimal refundAmount = null;
            String reason = (String) refundRequest.get("reason");
            
            Object amountObj = refundRequest.get("amount");
            if (amountObj != null) {
                if (amountObj instanceof Number) {
                    refundAmount = new BigDecimal(amountObj.toString());
                } else if (amountObj instanceof String) {
                    refundAmount = new BigDecimal((String) amountObj);
                }
            }

            // 如果没有指定退款金额，默认全额退款
            if (refundAmount == null) {
                refundAmount = payment.getAmount();
            }

            if (reason == null || reason.trim().isEmpty()) {
                reason = "用户申请退款";
            }

            boolean success = paymentService.processRefund(payment.getPaymentNo(), refundAmount, reason);
            
            if (success) {
                return Result.success(true, "退款申请提交成功");
            } else {
                return Result.error("退款申请失败");
            }

        } catch (Exception e) {
            log.error("处理退款失败", e);
            return Result.error("处理退款失败: " + e.getMessage());
        }
    }

    /**
     * 生成支付单号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4);
    }
}