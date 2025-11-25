package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.dto.SkuStockLogDTO;
import com.muyingmall.entity.ProductSkuStockLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SKU库存日志 Mapper
 * 
 * @author AI Assistant
 * @date 2024-11-24
 */
@Mapper
public interface ProductSkuStockLogMapper extends BaseMapper<ProductSkuStockLog> {

    /**
     * 分页查询库存日志
     */
    IPage<SkuStockLogDTO> selectLogPage(Page<?> page, 
                                        @Param("skuId") Long skuId,
                                        @Param("orderId") Integer orderId,
                                        @Param("changeType") String changeType,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 根据SKU ID查询日志
     */
    List<SkuStockLogDTO> selectBySkuId(@Param("skuId") Long skuId);

    /**
     * 根据订单ID查询日志
     */
    List<SkuStockLogDTO> selectByOrderId(@Param("orderId") Integer orderId);
}
