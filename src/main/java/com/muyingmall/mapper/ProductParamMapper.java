package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.ProductParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品参数Mapper
 */
@Mapper
public interface ProductParamMapper extends BaseMapper<ProductParam> {
    
    /**
     * 根据商品ID查询参数列表
     * @param productId 商品ID
     * @return 参数列表
     */
    @Select("SELECT * FROM product_param WHERE product_id = #{productId} ORDER BY sort_order ASC")
    List<ProductParam> selectByProductId(Integer productId);
}
