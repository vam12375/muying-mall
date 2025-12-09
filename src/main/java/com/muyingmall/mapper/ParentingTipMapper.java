package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.ParentingTip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 育儿知识Mapper
 */
@Mapper
public interface ParentingTipMapper extends BaseMapper<ParentingTip> {
    
    /**
     * 增加浏览量
     */
    @Update("UPDATE parenting_tip SET view_count = view_count + 1 WHERE id = #{id}")
    int increaseViewCount(Long id);
}
