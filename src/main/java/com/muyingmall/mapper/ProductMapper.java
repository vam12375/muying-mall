package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

        /**
         * 查询商品列表，并关联查询分类名称和品牌名称
         * 
         * @param page         分页参数
         * @param queryWrapper 查询条件
         * @return 商品分页列表
         */
        @Select("SELECT p.*, c.name as category_name, b.name as brand_name " +
                        "FROM product p " +
                        "LEFT JOIN category c ON p.category_id = c.category_id " +
                        "LEFT JOIN brand b ON p.brand_id = b.brand_id " +
                        "ORDER BY p.create_time DESC")
        Page<Product> selectProductPageWithCategoryAndBrand(Page<Product> page);

        /**
         * 根据多条件查询商品列表，并关联查询分类名称和品牌名称
         * 
         * @param page       分页参数
         * @param categoryId 分类ID
         * @param brandId    品牌ID
         * @param keyword    关键词
         * @param status     状态
         * @return 商品分页列表
         */
        @Select("<script>" +
                        "SELECT p.*, c.name as category_name, b.name as brand_name " +
                        "FROM product p " +
                        "LEFT JOIN category c ON p.category_id = c.category_id " +
                        "LEFT JOIN brand b ON p.brand_id = b.brand_id " +
                        "<where>" +
                        "  <if test='categoryId != null'> AND p.category_id = #{categoryId} </if>" +
                        "  <if test='brandId != null'> AND p.brand_id = #{brandId} </if>" +
                        "  <if test='keyword != null and keyword != \"\"'> AND (p.product_name LIKE CONCAT('%', #{keyword}, '%') OR p.product_detail LIKE CONCAT('%', #{keyword}, '%')) </if>"
                        +
                        "  <if test='status != null'> AND p.product_status = #{status} </if>" +
                        "</where>" +
                        "ORDER BY p.create_time DESC" +
                        "</script>")
        Page<Product> selectProductPageWithParams(Page<Product> page,
                        @Param("categoryId") Integer categoryId,
                        @Param("brandId") Integer brandId,
                        @Param("keyword") String keyword,
                        @Param("status") String status);

        /**
         * 根据品牌ID统计关联商品数量
         *
         * @param brandId 品牌ID
         * @return 商品数量
         */
        @Select("SELECT COUNT(*) FROM product WHERE brand_id = #{brandId}")
        int countProductByBrandId(@Param("brandId") Integer brandId);

        /**
         * 批量查询分类商品数量
         * 性能优化：一次SQL查询所有分类的商品数量，避免N+1查询
         * 来源：N+1查询优化 - 批量统计分类商品数量
         *
         * @param categoryIds 分类ID列表
         * @return 分类ID与商品数量的映射列表
         */
        @Select("<script>" +
                        "SELECT category_id as categoryId, COUNT(*) as productCount " +
                        "FROM product " +
                        "WHERE category_id IN " +
                        "<foreach collection='categoryIds' item='id' open='(' separator=',' close=')'>" +
                        "#{id}" +
                        "</foreach>" +
                        "GROUP BY category_id" +
                        "</script>")
        List<Map<String, Object>> batchCountProductsByCategories(@Param("categoryIds") List<Integer> categoryIds);
}