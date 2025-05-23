package com.muyingmall.service;

import com.alipay.api.AlipayApiException;
import com.muyingmall.entity.Refund;
import java.util.Map;

/**
 * 支付宝退款服务接口
 */
public interface AlipayRefundService {

    /**
     * 发起支付宝退款请求
     *
     * @param refund 退款信息
     * @return 退款请求结果，包含退款交易号
     * @throws AlipayApiException 支付宝API异常
     */
    String refund(Refund refund) throws AlipayApiException;

    /**
     * 查询支付宝退款状态
     *
     * @param refundNo      退款单号
     * @param transactionId 交易号（可选）
     * @return 退款状态
     * @throws AlipayApiException 支付宝API异常
     */
    String queryRefundStatus(String refundNo, String transactionId) throws AlipayApiException;

    /**
     * 处理支付宝退款异步通知
     *
     * @param params 通知参数
     * @return 处理结果
     * @throws AlipayApiException 支付宝API异常
     */
    boolean handleRefundNotify(Map<String, String> params) throws AlipayApiException;
}