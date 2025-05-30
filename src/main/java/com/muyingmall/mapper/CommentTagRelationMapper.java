package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.CommentTag;
import com.muyingmall.entity.CommentTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

/**
 * 评价标签关系Mapper接口
 */
@Mapper
public interface CommentTagRelationMapper extends BaseMapper<CommentTagRelation> {

    /**
     * 获取评价的标签列表
     *
     * @param commentId 评价ID
     * @return 标签列表
     */
    @Select("SELECT t.* FROM comment_tag t " +
            "JOIN comment_tag_relation r ON t.tag_id = r.tag_id " +
            "WHERE r.comment_id = #{commentId} AND t.status = 1")
    List<CommentTag> getCommentTags(@Param("commentId") Integer commentId);

    /**
     * 获取使用了指定标签的评价ID列表
     *
     * @param tagId 标签ID
     * @return 评价ID列表
     */
    @Select("SELECT comment_id FROM comment_tag_relation WHERE tag_id = #{tagId}")
    List<Integer> getCommentIdsByTagId(@Param("tagId") Integer tagId);

    /**
     * 批量插入评价标签关系
     *
     * @param commentId 评价ID
     * @param tagIds    标签ID列表
     * @return 影响行数
     */
    @Insert({
            "<script>",
            "INSERT INTO comment_tag_relation(comment_id, tag_id) VALUES ",
            "<foreach collection='tagIds' item='tagId' separator=','>",
            "(#{commentId}, #{tagId})",
            "</foreach>",
            "</script>"
    })
    int batchInsert(@Param("commentId") Integer commentId, @Param("tagIds") List<Integer> tagIds);

    /**
     * 删除评价的所有标签关系
     *
     * @param commentId 评价ID
     * @return 影响行数
     */
    @Select("DELETE FROM comment_tag_relation WHERE comment_id = #{commentId}")
    int deleteByCommentId(@Param("commentId") Integer commentId);

    /**
     * 删除指定评价的指定标签关系
     *
     * @param commentId 评价ID
     * @param tagId     标签ID
     * @return 影响行数
     */
    @Select("DELETE FROM comment_tag_relation WHERE comment_id = #{commentId} AND tag_id = #{tagId}")
    int deleteByCommentIdAndTagId(@Param("commentId") Integer commentId, @Param("tagId") Integer tagId);
}