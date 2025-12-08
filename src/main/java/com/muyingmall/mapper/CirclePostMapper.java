package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.CirclePost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 育儿圈帖子Mapper
 */
@Mapper
public interface CirclePostMapper extends BaseMapper<CirclePost> {

    /**
     * 增加浏览量
     */
    @Update("UPDATE circle_post SET view_count = view_count + 1 WHERE post_id = #{postId}")
    int incrementViewCount(Long postId);

    /**
     * 增加点赞数
     */
    @Update("UPDATE circle_post SET like_count = like_count + 1 WHERE post_id = #{postId}")
    int incrementLikeCount(Long postId);

    /**
     * 减少点赞数
     */
    @Update("UPDATE circle_post SET like_count = GREATEST(like_count - 1, 0) WHERE post_id = #{postId}")
    int decrementLikeCount(Long postId);

    /**
     * 增加评论数
     */
    @Update("UPDATE circle_post SET comment_count = comment_count + 1 WHERE post_id = #{postId}")
    int incrementCommentCount(Long postId);

    /**
     * 减少评论数
     */
    @Update("UPDATE circle_post SET comment_count = GREATEST(comment_count - 1, 0) WHERE post_id = #{postId}")
    int decrementCommentCount(Long postId);
}
