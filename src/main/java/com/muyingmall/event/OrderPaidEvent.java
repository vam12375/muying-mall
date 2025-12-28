package com.muyingmall.event;

import com.muyingmall.entity.Address;
import com.muyingmall.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 订单支付完成事件
 * 用于解耦订单服务和物流服务的循环依赖
 */
@Getter
public class OrderPaidEvent extends ApplicationEvent {

    /**
     * 订单信息
     */
    private final Order order;

    /**
     * 收货地址信息
     */
    private final Address address;

    public OrderPaidEvent(Object source, Order order, Address address) {
        super(source);
        this.order = order;
        this.address = address;
    }
}
