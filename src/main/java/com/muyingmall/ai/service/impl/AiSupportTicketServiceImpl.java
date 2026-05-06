package com.muyingmall.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyingmall.ai.dto.AiTicketCreateRequest;
import com.muyingmall.ai.dto.AiTicketUpdateRequest;
import com.muyingmall.ai.entity.AiSupportTicket;
import com.muyingmall.ai.enums.AiRiskLevel;
import com.muyingmall.ai.enums.AiTicketStatus;
import com.muyingmall.ai.mapper.AiSupportTicketMapper;
import com.muyingmall.ai.service.AiSupportTicketService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * AI 工单服务实现。
 */
@Service
public class AiSupportTicketServiceImpl extends ServiceImpl<AiSupportTicketMapper, AiSupportTicket>
        implements AiSupportTicketService {

    @Override
    public AiSupportTicket createTicket(Integer userId, AiTicketCreateRequest request) {
        AiSupportTicket ticket = new AiSupportTicket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setConversationId(request.getConversationId());
        ticket.setUserId(userId);
        ticket.setOrderId(request.getOrderId());
        ticket.setProductId(request.getProductId());
        ticket.setTitle(request.getTitle());
        ticket.setContent(request.getContent());
        ticket.setIntent(request.getIntent());
        ticket.setRiskLevel(StringUtils.hasText(request.getRiskLevel()) ? request.getRiskLevel() : AiRiskLevel.MEDIUM.getCode());
        ticket.setStatus(AiTicketStatus.PENDING.getCode());
        ticket.setSource("AI_AGENT");
        ticket.setCreateTime(LocalDateTime.now());
        ticket.setUpdateTime(LocalDateTime.now());
        save(ticket);
        return ticket;
    }

    @Override
    public AiSupportTicket updateTicket(Long ticketId, AiTicketUpdateRequest request) {
        AiSupportTicket ticket = getById(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("AI工单不存在");
        }
        ticket.setStatus(request.getStatus());
        ticket.setAssigneeId(request.getAssigneeId());
        ticket.setAssigneeName(request.getAssigneeName());
        ticket.setHandleRemark(request.getHandleRemark());
        ticket.setUpdateTime(LocalDateTime.now());
        if (AiTicketStatus.RESOLVED.getCode().equals(request.getStatus())
                || AiTicketStatus.CLOSED.getCode().equals(request.getStatus())) {
            ticket.setCloseTime(LocalDateTime.now());
        }
        updateById(ticket);
        return ticket;
    }

    private String generateTicketNo() {
        String datePart = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "AIT" + datePart + randomPart;
    }
}
