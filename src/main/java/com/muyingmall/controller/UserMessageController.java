package com.muyingmall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.muyingmall.common.response.Result;
import com.muyingmall.dto.UserMessageDTO;
import com.muyingmall.entity.Order;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserMessage;
import com.muyingmall.enums.MessageType;
import com.muyingmall.enums.OrderStatus;
import com.muyingmall.event.ShippingReminderEvent;
import com.muyingmall.service.OrderService;
import com.muyingmall.service.UserMessageService;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户消息控制器
 */
@RestController
@RequestMapping("/user/message")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户消息", description = "用户消息相关接口")
public class UserMessageController {

    private final UserMessageService userMessageService;
    private final UserService userService;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 获取用户消息列表
     *
     * @param type   消息类型
     * @param isRead 是否已读
     * @param page   页码
     * @param size   每页大小
     * @return 分页消息列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户消息列表", description = "分页获取用户消息列表")
    public Result<IPage<UserMessageDTO>> getMessageList(
            @Parameter(description = "消息类型") @RequestParam(required = false) String type,
            @Parameter(description = "是否已读：0-未读，1-已读") @RequestParam(required = false) Integer isRead,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {

        // 获取当前登录用户
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        // 检查用户角色，普通用户不应接收催发货提醒消息
        boolean isAdmin = false;
        if (user.getRole() != null) {
            isAdmin = "admin".equals(user.getRole());
        }

        // 查询消息列表
        IPage<UserMessage> messagePage = userMessageService.getUserMessages(user.getUserId(), type, isRead, page, size);

        // 普通用户过滤掉催发货类型的消息
        if (!isAdmin && messagePage.getRecords() != null) {
            messagePage.getRecords()
                    .removeIf(message -> MessageType.SHIPPING_REMINDER.getCode().equals(message.getType()));
        }

        // 转换为DTO
        IPage<UserMessageDTO> dtoPage = messagePage.convert(message -> {
            UserMessageDTO dto = new UserMessageDTO();
            BeanUtils.copyProperties(message, dto);

            // 添加消息类型描述
            MessageType messageType = MessageType.getByCode(message.getType());
            if (messageType != null) {
                dto.setTypeDesc(messageType.getDesc());
            }

            return dto;
        });

        return Result.success(dtoPage);
    }

    /**
     * 获取未读消息数量
     *
     * @return 未读消息数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息数量", description = "获取当前用户的未读消息总数")
    public Result<Integer> getUnreadCount() {
        // 获取当前登录用户
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        // 检查用户角色
        boolean isAdmin = false;
        if (user.getRole() != null) {
            isAdmin = "admin".equals(user.getRole());
        }

        // 查询未读消息数量
        int count = userMessageService.getUnreadCount(user.getUserId());

        // 如果不是管理员，需要减去催发货消息的数量
        if (!isAdmin) {
            Map<String, Integer> countByType = userMessageService.getUnreadCountByType(user.getUserId());
            Integer shippingReminders = countByType.getOrDefault(MessageType.SHIPPING_REMINDER.getCode(), 0);
            count -= shippingReminders;
        }

        return Result.success(count);
    }

    /**
     * 获取各类型未读消息数量
     *
     * @return 各类型未读消息数量
     */
    @GetMapping("/unread/count/type")
    @Operation(summary = "获取各类型未读消息数量", description = "获取当前用户各类型的未读消息数量")
    public Result<Map<String, Integer>> getUnreadCountByType() {
        // 获取当前登录用户
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        // 检查用户角色
        boolean isAdmin = false;
        if (user.getRole() != null) {
            isAdmin = "admin".equals(user.getRole());
        }

        // 查询各类型未读消息数量
        Map<String, Integer> countMap = userMessageService.getUnreadCountByType(user.getUserId());

        // 如果不是管理员，移除催发货消息计数
        if (!isAdmin) {
            countMap.remove(MessageType.SHIPPING_REMINDER.getCode());
        }

        return Result.success(countMap);
    }

    /**
     * 标记消息为已读
     *
     * @param messageId 消息ID
     * @return 操作结果
     */
    @PutMapping("/read/{messageId}")
    @Operation(summary = "标记消息为已读", description = "将指定消息标记为已读状态")
    public Result<Boolean> markAsRead(@PathVariable String messageId) {
        // 获取当前登录用户
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        // 标记消息为已读
        boolean success = userMessageService.markAsRead(messageId);

        if (success) {
            return Result.success(true);
        } else {
            return Result.error("标记失败，消息不存在或已被删除");
        }
    }

    /**
     * 标记所有消息为已读
     *
     * @return 操作结果
     */
    @PutMapping("/read/all")
    @Operation(summary = "标记所有消息为已读", description = "将当前用户的所有消息标记为已读状态")
    public Result<Integer> markAllAsRead() {
        // 获取当前登录用户
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        // 标记所有消息为已读
        int count = userMessageService.markAllAsRead(user.getUserId());

        return Result.success(count);
    }

    /**
     * 标记指定类型的所有消息为已读
     *
     * @param type 消息类型
     * @return 操作结果
     */
    @PutMapping("/read/type/{type}")
    @Operation(summary = "标记指定类型的所有消息为已读", description = "将当前用户指定类型的所有消息标记为已读状态")
    public Result<Integer> markTypeAsRead(@PathVariable String type) {
        // 获取当前登录用户
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        // 验证消息类型
        MessageType messageType = MessageType.getByCode(type);
        if (messageType == null) {
            return Result.error("消息类型不存在");
        }

        // 标记指定类型的所有消息为已读
        int count = userMessageService.markTypeAsRead(user.getUserId(), type);

        return Result.success(count);
    }

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     * @return 操作结果
     */
    @DeleteMapping("/{messageId}")
    @Operation(summary = "删除消息", description = "删除指定的消息")
    public Result<Boolean> deleteMessage(@PathVariable String messageId) {
        // 获取当前登录用户
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        // 删除消息
        boolean success = userMessageService.deleteMessage(messageId);

        if (success) {
            return Result.success(true);
        } else {
            return Result.error("删除失败，消息不存在或已被删除");
        }
    }

    /**
     * 发送催发货提醒
     *
     * @param orderId 订单ID
     * @param message 用户留言（可选）
     * @return 操作结果
     */
    @PostMapping("/shippingReminder")
    @Operation(summary = "发送催发货提醒", description = "用户对订单发送催发货提醒")
    public Result<Boolean> sendShippingReminder(
            @Parameter(description = "订单ID") @RequestParam Integer orderId,
            @Parameter(description = "用户留言") @RequestParam(required = false) String message) {

        // 获取当前登录用户
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }

        // 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 验证订单是否属于当前用户
        if (!order.getUserId().equals(user.getUserId())) {
            return Result.error("无权操作此订单");
        }

        // 验证订单状态是否可以催发货
        if (!OrderStatus.PENDING_SHIPMENT.equals(order.getStatus())) {
            return Result.error("当前订单状态不可催发货");
        }

        try {
            // 创建催发货事件
            ShippingReminderEvent event = new ShippingReminderEvent(
                    this,
                    user.getUserId(),
                    order.getOrderId(),
                    order.getOrderNo(),
                    order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    message,
                    "{\"orderId\":" + order.getOrderId() + "}");

            // 发布事件
            eventPublisher.publishEvent(event);

            return Result.success(true, "催发货提醒已发送");
        } catch (Exception e) {
            log.error("发送催发货提醒失败: userId={}, orderId={}, error={}",
                    user.getUserId(), orderId, e.getMessage(), e);
            return Result.error("发送催发货提醒失败");
        }
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户，如果未登录则返回null
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                return userService.findByUsername(username);
            }
        } catch (Exception e) {
            log.error("获取当前登录用户失败: {}", e.getMessage(), e);
        }
        return null;
    }
}