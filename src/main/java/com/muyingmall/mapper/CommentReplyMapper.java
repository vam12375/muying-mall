package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.CommentReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评价回复Mapper接口
 */
@Mapper
public interface CommentReplyMapper extends BaseMapper<CommentReply> {

    /**
     * 根据评价ID获取回复列表
     *
     * @param commentId 评价ID
     * @return 回复列表
     */
    List<CommentReply> getCommentReplies(@Param("commentId") Integer commentId);
} 