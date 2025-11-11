package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.PointsExchange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 积分兑换记录Mapper接口
 */
@Mapper
public interface PointsExchangeMapper extends BaseMapper<PointsExchange> {
    
    /**
     * 查询用户兑换记录（包含商品信息）
     * 
     * @param userId 用户ID
     * @return 兑换记录列表（包含商品信息）
     */
    @Select("SELECT pe.*, pp.name as productName, pp.image as productImage, " +
            "pp.category as productCategory, pp.description as productDescription " +
            "FROM points_exchange pe " +
            "LEFT JOIN points_product pp ON pe.product_id = pp.id " +
            "WHERE pe.user_id = #{userId} " +
            "ORDER BY pe.create_time DESC")
    List<Map<String, Object>> selectUserExchangesWithProduct(@Param("userId") Integer userId);
    
    /**
     * 查询兑换详情（包含商品和用户信息）
     * 
     * @param id 兑换记录ID
     * @return 兑换详情（包含商品、用户、地址信息）
     */
    @Select("SELECT pe.*, pp.name as productName, pp.image as productImage, " +
            "pp.description as productDescription, pp.category as productCategory, " +
            "u.username, u.nickname, ua.receiver_name as receiverName, " +
            "ua.receiver_phone as receiverPhone, ua.province, ua.city, " +
            "ua.district, ua.detail_address as detailAddress " +
            "FROM points_exchange pe " +
            "LEFT JOIN points_product pp ON pe.product_id = pp.id " +
            "LEFT JOIN user u ON pe.user_id = u.user_id " +
            "LEFT JOIN user_address ua ON pe.address_id = ua.id " +
            "WHERE pe.id = #{id}")
    Map<String, Object> selectExchangeDetailById(@Param("id") Long id);
    
    /**
     * 统计用户兑换情况
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(pe.id) as exchangeCount, " +
            "SUM(pe.points) as totalPointsUsed, " +
            "SUM(pe.quantity) as totalQuantity " +
            "FROM points_exchange pe " +
            "WHERE pe.user_id = #{userId} AND pe.status IN (1, 2)")
    Map<String, Object> selectUserExchangeStats(@Param("userId") Integer userId);
}