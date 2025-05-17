package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * 评价Mapper接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

        /**
         * 获取商品评价列表（包含用户信息）
         * 
         * @param productId 商品ID
         * @return 评价列表
         */
        @Select("SELECT c.*, u.username, u.nickname, u.avatar, p.product_name as product_name, p.product_img as product_image "
                        +
                        "FROM comment c " +
                        "LEFT JOIN user u ON c.user_id = u.user_id " +
                        "LEFT JOIN product p ON c.product_id = p.product_id " +
                        "WHERE c.product_id = #{productId} AND c.status = 1 " +
                        "ORDER BY c.create_time DESC")
        List<Comment> getProductComments(@Param("productId") Integer productId);

        /**
         * 获取用户的评价列表
         * 
         * @param userId 用户ID
         * @return 评价列表
         */
        @Select("SELECT c.*, u.username, u.nickname, u.avatar, p.product_name as product_name, p.product_img as product_image "
                        +
                        "FROM comment c " +
                        "LEFT JOIN user u ON c.user_id = u.user_id " +
                        "LEFT JOIN product p ON c.product_id = p.product_id " +
                        "WHERE c.user_id = #{userId} " +
                        "ORDER BY c.create_time DESC")
        List<Comment> getUserComments(@Param("userId") Integer userId);

        /**
         * 统计商品平均评分
         * 
         * @param productId 商品ID
         * @return 平均评分
         */
        @Select("SELECT IFNULL(AVG(rating), 0) FROM comment WHERE product_id = #{productId} AND status = 1")
        Double getProductAverageRating(@Param("productId") Integer productId);

        /**
         * 统计各评分等级的数量
         * 
         * @param productId 商品ID，可为null
         * @return 各评分等级的数量
         */
        @Select({
                        "<script>",
                        "SELECT rating, COUNT(*) as count FROM comment ",
                        "<where>",
                        "<if test='productId != null'>",
                        "product_id = #{productId} AND ",
                        "</if>",
                        "status = 1 ",
                        "</where>",
                        "GROUP BY rating ORDER BY rating DESC",
                        "</script>"
        })
        List<Map<String, Object>> getRatingDistribution(@Param("productId") Integer productId);
}