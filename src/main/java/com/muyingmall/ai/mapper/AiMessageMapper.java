package com.muyingmall.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.ai.entity.AiMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 消息 Mapper。
 */
@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessage> {
}
