package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.entity.ParentingTipComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 育儿知识评论Mapper
 */
@Mapper
public interface ParentingTipCommentMapper extends BaseMapper<ParentingTipComment> {
    
    /**
     * 分页查询评论（带用户信息）
     */
    @Select("SELECT c.*, u.username, u.avatar FROM parenting_tip_comment c " +
            "LEFT JOIN user u ON c.user_id = u.user_id " +
            "WHERE c.tip_id = #{tipId} AND c.status = 1 " +
            "ORDER BY c.create_time DESC")
    IPage<ParentingTipComment> selectPageWithUser(Page<ParentingTipComment> page, Long tipId);
}
