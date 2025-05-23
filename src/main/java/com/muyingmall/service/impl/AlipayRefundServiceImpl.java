package com.muyingmall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muyingmall.config.AlipayConfig;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.Payment;
import com.muyingmall.entity.Refund;
import com.muyingmall.exception.BusinessException;
import com.muyingmall.mapper.RefundMapper;
import com.muyingmall.service.AlipayRefundService;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.RefundService;
import com.muyingmall.service.RefundStateService;
import com.muyingmall.statemachine.RefundEvent;
import com.muyingmall.util.AlipayClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝退款服务实现类
 */
@Service
@Slf4j
public class AlipayRefundServiceImpl implements AlipayRefundService {

    private final AlipayConfig alipayConfig;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final RefundStateService refundStateService;
    private final RefundService refundService;

    @Autowired
    public AlipayRefundServiceImpl(
            AlipayConfig alipayConfig,
            OrderService orderService,
            PaymentService paymentService,
            RefundStateService refundStateService,
            @Lazy RefundService refundService) {
        this.alipayConfig = alipayConfig;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.refundStateService = refundStateService;
        this.refundService = refundService;
    }

    @Override
    public String refund(Refund refund) throws AlipayApiException {
        // 查询订单和支付信息
        Order order = orderService.getById(refund.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        Payment payment = paymentService.getById(refund.getPaymentId());
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        // 验证支付是否已经完成
        if (!"PAID".equals(payment.getStatus())) {
            throw new BusinessException("支付未完成，无法退款");
        }

        // 验证退款金额
        if (refund.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new BusinessException("退款金额不能大于支付金额");
        }

        // 获取AlipayClient实例
        AlipayClient alipayClient = AlipayClientFactory.getAlipayClient();

        // 创建退款请求对象
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        // 设置退款异步通知URL
        if (alipayConfig.getRefundNotifyUrl() != null && !alipayConfig.getRefundNotifyUrl().isEmpty()) {
            request.setNotifyUrl(alipayConfig.getRefundNotifyUrl());
        }

        // 创建退款业务模型
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(payment.getPaymentNo()); // 商户订单号
        model.setTradeNo(payment.getTransactionId()); // 支付宝交易号
        model.setRefundAmount(refund.getAmount().toString()); // 退款金额
        model.setOutRequestNo(refund.getRefundNo()); // 退款请求号
        model.setRefundReason(refund.getRefundReason()); // 退款原因

        // 设置请求模型
        request.setBizModel(model);

        // 添加重试机制
        int maxRetryCount = 2; // 最多重试2次
        long retryInterval = 3000; // 3秒重试间隔

        AlipayApiException lastException = null;

        for (int i = 0; i <= maxRetryCount; i++) {
            try {
                // 发送退款请求
                log.info("发起支付宝退款请求，订单号：{}，退款单号：{}，金额：{}，尝试次数：{}/{}",
                        payment.getPaymentNo(), refund.getRefundNo(), refund.getAmount(), i + 1, maxRetryCount + 1);
                AlipayTradeRefundResponse response = alipayClient.execute(request);

                // 处理响应结果
                if (response.isSuccess()) {
                    log.info("支付宝退款请求成功，响应结果：{}", response.getBody());
                    // 返回支付宝退款交易号（可能为空，此时使用商户退款单号）
                    String refundTradeNo = response.getTradeNo();
                    return refundTradeNo != null ? refundTradeNo : refund.getRefundNo();
                } else if ("SYSTEM_ERROR".equals(response.getSubCode())) {
                    // 系统错误，需要重试
                    log.warn("支付宝退款请求遇到系统错误，准备重试，退款单号：{}，尝试次数：{}/{}",
                            refund.getRefundNo(), i + 1, maxRetryCount + 1);
                    lastException = new AlipayApiException(response.getSubCode() + ":" + response.getSubMsg());

                    // 如果不是最后一次尝试，则等待一段时间后重试
                    if (i < maxRetryCount) {
                        try {
                            Thread.sleep(retryInterval);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new AlipayApiException("退款过程被中断", e);
                        }
                    }
                } else {
                    // 处理特定业务错误
                    String subCode = response.getSubCode();
                    String subMsg = response.getSubMsg();
                    log.error("支付宝退款请求失败，错误代码：{}，错误信息：{}", subCode, subMsg);

                    // 根据不同错误码返回友好的错误信息
                    if ("TRADE_NOT_EXIST".equals(subCode)) {
                        throw new BusinessException("交易不存在，请检查交易号是否正确");
                    } else if ("TRADE_STATUS_ERROR".equals(subCode)) {
                        throw new BusinessException("交易状态不允许退款");
                    } else if ("REFUND_AMT_RESTRICTION".equals(subCode) || "REQUEST_AMOUNT_EXCEED".equals(subCode)) {
                        throw new BusinessException("退款金额超限");
                    } else if ("MERCHANT_BALANCE_NOT_ENOUGH".equals(subCode)) {
                        throw new BusinessException("商户余额不足");
                    } else {
                        throw new BusinessException("支付宝退款失败：" + subMsg);
                    }
                }
            } catch (AlipayApiException e) {
                // API调用异常
                if (i < maxRetryCount) {
                    lastException = e;
                    log.warn("支付宝退款请求发生异常，准备重试，退款单号：{}，尝试次数：{}/{}，异常：{}",
                            refund.getRefundNo(), i + 1, maxRetryCount + 1, e.getMessage());

                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AlipayApiException("退款过程被中断", ie);
                    }
                } else {
                    throw e; // 最后一次尝试仍然失败，抛出异常
                }
            }
        }

        // 如果所有重试都失败，抛出最后一次的异常
        if (lastException != null) {
            throw lastException;
        }

        // 正常情况下不会执行到这里
        throw new BusinessException("支付宝退款请求失败，所有重试均失败");
    }

    @Override
    public String queryRefundStatus(String refundNo, String transactionId) throws AlipayApiException {
        // 获取AlipayClient实例
        AlipayClient alipayClient = AlipayClientFactory.getAlipayClient();

        // 创建退款查询请求
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();

        // 创建查询业务模型
        com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel model = new com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel();
        model.setOutRequestNo(refundNo); // 退款请求号

        if (transactionId != null && !transactionId.isEmpty()) {
            model.setTradeNo(transactionId); // 支付宝交易号
        }

        // 设置业务模型
        request.setBizModel(model);

        // 添加重试机制
        int maxRetryCount = alipayConfig.getMaxQueryRetry();
        long retryInterval = alipayConfig.getQueryDuration();

        AlipayApiException lastException = null;

        for (int i = 0; i <= maxRetryCount; i++) {
            try {
                // 发送查询请求
                log.info("查询支付宝退款状态，退款单号：{}，交易号：{}，尝试次数：{}", refundNo, transactionId, i + 1);
                AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);

                if (response.isSuccess()) {
                    log.info("支付宝退款查询成功，退款状态：{}", response.getRefundStatus());
                    // 返回退款状态
                    return response.getRefundStatus();
                } else if ("SYSTEM_ERROR".equals(response.getSubCode())) {
                    // 系统错误，需要重试
                    log.warn("查询支付宝退款状态遇到系统错误，准备重试，退款单号：{}，尝试次数：{}/{}",
                            refundNo, i + 1, maxRetryCount + 1);
                    lastException = new AlipayApiException(response.getSubCode() + ":" + response.getSubMsg());

                    // 如果不是最后一次尝试，则等待一段时间后重试
                    if (i < maxRetryCount) {
                        try {
                            Thread.sleep(retryInterval);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new AlipayApiException("查询过程被中断", e);
                        }
                    }
                } else {
                    // 其他业务错误，不需要重试
                    log.error("支付宝退款查询失败，错误代码：{}，错误信息：{}",
                            response.getSubCode(), response.getSubMsg());
                    throw new BusinessException("支付宝退款查询失败：" + response.getSubMsg());
                }
            } catch (AlipayApiException e) {
                // API调用异常
                if (i < maxRetryCount) {
                    lastException = e;
                    log.warn("查询支付宝退款状态发生异常，准备重试，退款单号：{}，尝试次数：{}/{}，异常：{}",
                            refundNo, i + 1, maxRetryCount + 1, e.getMessage());

                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AlipayApiException("查询过程被中断", ie);
                    }
                } else {
                    throw e; // 最后一次尝试仍然失败，抛出异常
                }
            }
        }

        // 如果所有重试都失败，抛出最后一次的异常
        if (lastException != null) {
            throw lastException;
        }

        // 正常情况下不会执行到这里
        throw new BusinessException("查询支付宝退款状态失败，所有重试均失败");
    }

    @Override
    public boolean handleRefundNotify(Map<String, String> params) throws AlipayApiException {
        log.info("收到支付宝退款异步通知，参数：{}", params);

        // 验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getPublicKey(),
                alipayConfig.getCharset(),
                alipayConfig.getSignType());

        if (!signVerified) {
            log.error("支付宝退款通知签名验证失败");
            return false;
        }

        // 获取通知参数
        String refundStatus = params.get("refund_status"); // 退款状态
        String outRequestNo = params.get("out_request_no"); // 退款请求号，即我们的退款编号
        String tradeNo = params.get("trade_no"); // 支付宝交易号
        String refundAmount = params.get("refund_amount"); // 退款金额
        String gmtRefund = params.get("gmt_refund"); // 退款时间

        log.info("支付宝退款通知验签成功，退款状态：{}，退款单号：{}，交易号：{}，退款金额：{}，退款时间：{}",
                refundStatus, outRequestNo, tradeNo, refundAmount, gmtRefund);

        try {
            // 根据退款单号查询系统中的退款记录
            Refund refund = refundService.getRefundByRefundNo(outRequestNo);

            if (refund == null) {
                log.error("系统中不存在退款单号为{}的退款记录", outRequestNo);
                return false;
            }

            // 判断退款状态，REFUND_SUCCESS表示退款成功
            if ("REFUND_SUCCESS".equals(refundStatus)) {
                // 更新系统中的退款记录状态为已完成
                refund.setStatus("COMPLETED");
                refund.setTransactionId(tradeNo);
                refund.setRefundTime(LocalDateTime.now());

                // 更新退款记录
                boolean updated = refundService.updateById(refund);
                if (updated) {
                    log.info("退款成功，已更新退款记录：{}", refund.getId());

                    // 触发状态机事件，完成退款状态流转
                    refundStateService.sendEvent(refund.getId(), RefundEvent.COMPLETE, "SYSTEM", "系统自动", 0,
                            "支付宝异步通知触发退款完成");

                    return true;
                } else {
                    log.error("更新退款记录失败，退款ID：{}", refund.getId());
                    return false;
                }
            } else if ("REFUND_FAIL".equals(refundStatus)) {
                // 处理退款失败情况
                log.warn("支付宝通知退款失败，退款单号：{}", outRequestNo);

                // 触发状态机事件，标记退款失败
                refundStateService.sendEvent(refund.getId(), RefundEvent.FAIL, "SYSTEM", "系统自动", 0,
                        "支付宝异步通知：退款失败");

                return true;
            } else {
                log.warn("退款状态非成功也非失败，退款单号：{}，状态：{}", outRequestNo, refundStatus);
                return true; // 仍然返回成功，避免支付宝重复通知
            }
        } catch (Exception e) {
            log.error("处理支付宝退款通知异常", e);
            return false;
        }
    }
}