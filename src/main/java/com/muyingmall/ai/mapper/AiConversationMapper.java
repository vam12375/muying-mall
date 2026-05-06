package com.muyingmall.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.ai.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 会话 Mapper。
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {
}
