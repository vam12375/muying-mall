package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

        /**
         * 更新订单为支付成功后的状态
         * 
         * @param orderId    订单ID
         * @param status     订单状态
         * @param payTime    支付时间
         * @param updateTime 更新时间
         * @return 影响的行数
         */
        @Update("UPDATE `order` SET `status` = #{status}, `pay_time` = #{payTime}, `update_time` = #{updateTime} WHERE `order_id` = #{orderId}")
        int updateOrderStatusAfterPayment(@Param("orderId") Integer orderId,
                        @Param("status") String status,
                        @Param("payTime") LocalDateTime payTime,
                        @Param("updateTime") LocalDateTime updateTime);

        /**
         * 通过ID更新订单状态 - 使用更简单的语法
         */
        @Update("UPDATE `order` SET `status`=#{status}, `pay_time`=#{payTime}, `update_time`=NOW() WHERE `order_id`=#{orderId}")
        int updateOrderStatus(@Param("orderId") Integer orderId,
                        @Param("status") String status,
                        @Param("payTime") LocalDateTime payTime);

        /**
         * 尝试不同的列名来更新订单状态
         * 这个方法尝试使用可能的替代列名 ('id' 而不是 'order_id')
         */
        @Update("UPDATE `order` SET `status`=#{status}, `pay_time`=#{payTime}, `update_time`=NOW() WHERE `order_id`=#{orderId}")
        int updateOrderStatusByAlternateId(@Param("orderId") Integer orderId,
                        @Param("status") String status,
                        @Param("payTime") LocalDateTime payTime);
}