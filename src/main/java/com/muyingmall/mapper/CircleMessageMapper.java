package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.CircleMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 育儿圈消息Mapper
 */
@Mapper
public interface CircleMessageMapper extends BaseMapper<CircleMessage> {

    /**
     * 标记用户所有消息为已读
     */
    @Update("UPDATE circle_message SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(Integer userId);
}
