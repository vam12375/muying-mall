package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.UserSearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户搜索历史Mapper接口
 */
@Mapper
public interface UserSearchHistoryMapper extends BaseMapper<UserSearchHistory> {

    /**
     * 获取用户搜索历史关键词列表
     * @param userId 用户ID
     * @param limit 返回数量
     * @return 搜索历史关键词列表
     */
    @Select("SELECT keyword FROM user_search_history " +
            "WHERE user_id = #{userId} " +
            "ORDER BY last_search_time DESC " +
            "LIMIT #{limit}")
    List<String> getUserSearchKeywords(@Param("userId") Integer userId, @Param("limit") int limit);
}
