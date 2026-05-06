package com.muyingmall.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.ai.dto.AiToolCallLogRequest;
import com.muyingmall.ai.entity.AiToolCallLog;

/**
 * AI 工具调用日志服务。
 */
public interface AiToolCallLogService extends IService<AiToolCallLog> {

    AiToolCallLog recordToolCall(Integer userId, AiToolCallLogRequest request);
}
