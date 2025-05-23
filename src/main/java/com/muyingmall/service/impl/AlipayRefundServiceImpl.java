package com.muyingmall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.muyingmall.config.AlipayConfig;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.Payment;
import com.muyingmall.entity.Refund;
import com.muyingmall.exception.BusinessException;
import com.muyingmall.service.AlipayRefundService;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝退款服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlipayRefundServiceImpl implements AlipayRefundService {

    private final AlipayConfig alipayConfig;
    private final OrderService orderService;
    private final PaymentService paymentService;

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

        // 初始化AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json",
                "UTF-8",
                alipayConfig.getPublicKey(),
                "RSA2");

        // 创建退款请求
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        // 设置退款异步通知URL
        if (alipayConfig.getRefundNotifyUrl() != null && !alipayConfig.getRefundNotifyUrl().isEmpty()) {
            request.setNotifyUrl(alipayConfig.getRefundNotifyUrl());
        }

        // 组装业务参数
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", payment.getPaymentNo()); // 商户订单号
        bizContent.put("trade_no", payment.getTransactionId()); // 支付宝交易号
        bizContent.put("refund_amount", refund.getAmount().toString()); // 退款金额
        bizContent.put("out_request_no", refund.getRefundNo()); // 退款请求号
        bizContent.put("refund_reason", refund.getRefundReason()); // 退款原因

        request.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));

        // 发送退款请求
        AlipayTradeRefundResponse response = alipayClient.execute(request);

        if (response.isSuccess()) {
            log.info("支付宝退款请求成功：{}", response.getBody());
            // 返回支付宝退款交易号
            return response.getTradeNo();
        } else {
            log.error("支付宝退款请求失败：{}", response.getBody());
            throw new BusinessException("支付宝退款失败：" + response.getSubMsg());
        }
    }

    @Override
    public String queryRefundStatus(String refundNo, String transactionId) throws AlipayApiException {
        // 初始化AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json",
                "UTF-8",
                alipayConfig.getPublicKey(),
                "RSA2");

        // 创建退款查询请求
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();

        // 组装业务参数
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_request_no", refundNo); // 退款请求号

        if (transactionId != null && !transactionId.isEmpty()) {
            bizContent.put("trade_no", transactionId); // 支付宝交易号
        }

        request.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));

        // 发送查询请求
        AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);

        if (response.isSuccess()) {
            log.info("支付宝退款查询成功：{}", response.getBody());
            // 返回退款状态
            return response.getRefundStatus();
        } else {
            log.error("支付宝退款查询失败：{}", response.getBody());
            throw new BusinessException("支付宝退款查询失败：" + response.getSubMsg());
        }
    }

    @Override
    public boolean handleRefundNotify(Map<String, String> params) throws AlipayApiException {
        // 验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getPublicKey(),
                "UTF-8",
                "RSA2");

        if (!signVerified) {
            log.error("支付宝退款通知签名验证失败");
            return false;
        }

        // 处理退款通知
        String refundStatus = params.get("refund_status");
        String outRequestNo = params.get("out_request_no"); // 退款请求号，即我们的退款编号
        String tradeNo = params.get("trade_no"); // 支付宝交易号

        log.info("收到支付宝退款通知：退款状态={}, 退款编号={}, 交易号={}", refundStatus, outRequestNo, tradeNo);

        // 这里可以根据退款状态更新系统中的退款记录
        // 具体逻辑需要根据业务需求实现

        return true;
    }
}