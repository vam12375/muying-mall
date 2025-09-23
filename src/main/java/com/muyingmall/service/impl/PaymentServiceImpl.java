package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.Payment;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.mapper.PaymentMapper;
import com.muyingmall.service.PaymentService;
import com.muyingmall.service.MessageProducerService;
import com.muyingmall.dto.PaymentMessage;
import com.muyingmall.util.EnumUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付服务实现类
 */
@Service
@Slf4j
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Autowired(required = false)
    private MessageProducerService messageProducerService;

    @Override
    public Payment createPayment(Payment payment) {
        this.save(payment);
        return payment;
    }

    @Override
    public Payment getByPaymentNo(String paymentNo) {
        LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Payment::getPaymentNo, paymentNo);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean updatePaymentStatus(String paymentNo, Integer status) {
        Payment payment = this.getByPaymentNo(paymentNo);
        if (payment != null) {
            payment.setStatus(EnumUtil.getPaymentStatusByCode(status));
            return this.updateById(payment);
        }
        return false;
    }

    @Override
    public Payment getByOrderNo(String orderNo) {
        LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Payment::getOrderNo, orderNo);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean processRefund(String paymentNo, BigDecimal refundAmount, String reason) {
        try {
            // 查询支付记录
            Payment payment = getByPaymentNo(paymentNo);
            if (payment == null) {
                log.error("退款失败：未找到支付记录，paymentNo={}", paymentNo);
                return false;
            }

            // 检查支付状态
            if (!PaymentStatus.SUCCESS.equals(payment.getStatus())) {
                log.error("退款失败：支付状态不是成功状态，paymentNo={}, status={}", paymentNo, payment.getStatus());
                return false;
            }

            // 检查退款金额
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("退款失败：退款金额无效，paymentNo={}, refundAmount={}", paymentNo, refundAmount);
                return false;
            }

            if (refundAmount.compareTo(payment.getAmount()) > 0) {
                log.error("退款失败：退款金额超过支付金额，paymentNo={}, refundAmount={}, paymentAmount={}", 
                        paymentNo, refundAmount, payment.getAmount());
                return false;
            }

            // 更新支付状态为退款中
            payment.setStatus(PaymentStatus.REFUNDING);
            payment.setUpdateTime(LocalDateTime.now());
            boolean updated = updateById(payment);

            if (updated) {
                // 发送退款消息到RabbitMQ
                if (messageProducerService != null) {
                    try {
                        PaymentMessage refundMessage = PaymentMessage.createRefundMessage(payment, refundAmount);
                        refundMessage.setExtra(reason);
                        messageProducerService.sendPaymentMessage(refundMessage);
                        log.info("退款消息发送成功: paymentNo={}, refundAmount={}", paymentNo, refundAmount);
                    } catch (Exception e) {
                        log.error("退款消息发送失败: paymentNo={}, error={}", paymentNo, e.getMessage(), e);
                        // 消息发送失败不影响退款流程
                    }
                } else {
                    log.warn("MessageProducerService未注入，跳过退款消息发送");
                }

                log.info("退款处理成功: paymentNo={}, refundAmount={}, reason={}", paymentNo, refundAmount, reason);
                return true;
            } else {
                log.error("退款失败：更新支付状态失败，paymentNo={}", paymentNo);
                return false;
            }

        } catch (Exception e) {
            log.error("退款处理异常: paymentNo={}, error={}", paymentNo, e.getMessage(), e);
            return false;
        }
    }
}