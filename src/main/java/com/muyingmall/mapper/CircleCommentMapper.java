package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.CircleComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 育儿圈评论Mapper
 */
@Mapper
public interface CircleCommentMapper extends BaseMapper<CircleComment> {

    /**
     * 增加点赞数
     */
    @Update("UPDATE circle_comment SET like_count = like_count + 1 WHERE comment_id = #{commentId}")
    int incrementLikeCount(Long commentId);

    /**
     * 减少点赞数
     */
    @Update("UPDATE circle_comment SET like_count = GREATEST(like_count - 1, 0) WHERE comment_id = #{commentId}")
    int decrementLikeCount(Long commentId);
}
