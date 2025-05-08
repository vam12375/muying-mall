package com.muyingmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.entity.Payment;
import com.muyingmall.enums.PaymentStatus;
import com.muyingmall.mapper.PaymentMapper;
import com.muyingmall.service.PaymentService;
import com.muyingmall.util.EnumUtil;
import org.springframework.stereotype.Service;

/**
 * 支付服务实现类
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

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
}