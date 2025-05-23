package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.CommentTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 评价标签Mapper接口
 */
@Mapper
public interface CommentTagMapper extends BaseMapper<CommentTag> {

    /**
     * 获取热门标签列表（按使用次数排序）
     *
     * @param limit 限制数量
     * @return 热门标签列表
     */
    @Select("SELECT * FROM comment_tag WHERE status = 1 ORDER BY usage_count DESC LIMIT #{limit}")
    List<CommentTag> getHotTags(@Param("limit") Integer limit);

    /**
     * 根据商品分类获取标签列表
     *
     * @param categoryId 商品分类ID
     * @return 标签列表
     */
    @Select("SELECT * FROM comment_tag WHERE status = 1 AND (product_category_id = #{categoryId} OR product_category_id IS NULL) ORDER BY usage_count DESC")
    List<CommentTag> getTagsByCategory(@Param("categoryId") Integer categoryId);

    /**
     * 增加标签使用次数
     *
     * @param tagId 标签ID
     * @return 影响行数
     */
    @Update("UPDATE comment_tag SET usage_count = usage_count + 1 WHERE tag_id = #{tagId}")
    int incrementUsageCount(@Param("tagId") Integer tagId);

    /**
     * 减少标签使用次数
     *
     * @param tagId 标签ID
     * @return 影响行数
     */
    @Update("UPDATE comment_tag SET usage_count = usage_count - 1 WHERE tag_id = #{tagId} AND usage_count > 0")
    int decrementUsageCount(@Param("tagId") Integer tagId);

    /**
     * 搜索标签
     *
     * @param keyword 关键词
     * @param limit   限制数量
     * @return 标签列表
     */
    @Select("SELECT * FROM comment_tag WHERE status = 1 AND tag_name LIKE CONCAT('%', #{keyword}, '%') ORDER BY usage_count DESC LIMIT #{limit}")
    List<CommentTag> searchTags(@Param("keyword") String keyword, @Param("limit") Integer limit);
}