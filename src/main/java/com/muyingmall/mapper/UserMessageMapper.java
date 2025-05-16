package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.UserMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户消息Mapper接口
 */
@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {

}