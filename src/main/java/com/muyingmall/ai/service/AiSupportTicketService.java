package com.muyingmall.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyingmall.ai.dto.AiTicketCreateRequest;
import com.muyingmall.ai.dto.AiTicketUpdateRequest;
import com.muyingmall.ai.entity.AiSupportTicket;

/**
 * AI 工单服务。
 */
public interface AiSupportTicketService extends IService<AiSupportTicket> {

    AiSupportTicket createTicket(Integer userId, AiTicketCreateRequest request);

    AiSupportTicket updateTicket(Long ticketId, AiTicketUpdateRequest request);
}
