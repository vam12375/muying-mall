package com.muyingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.dto.UserMessageDTO;
import com.muyingmall.entity.User;
import com.muyingmall.entity.UserMessage;
import com.muyingmall.enums.MessageType;
import com.muyingmall.service.UserMessageService;
import com.muyingmall.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员消息控制器
 */
@RestController
@RequestMapping("/admin/message")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理员消息", description = "管理员消息相关接口")
@PreAuthorize("hasRole('admin')")
public class AdminMessageController {

    private final UserMessageService userMessageService;
    private final UserService userService;

    /**
     * 获取消息列表
     *
     * @param type    消息类型
     * @param isRead  是否已读
     * @param page    页码
     * @param size    每页大小
     * @param keyword 搜索关键词（用户名、消息标题、内容）
     * @return 分页消息列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取消息列表", description = "分页获取所有用户的消息列表")
    public CommonResult<Page<UserMessageDTO>> getMessageList(
            @Parameter(description = "消息类型") @RequestParam(required = false) String type,
            @Parameter(description = "是否已读：0-未读，1-已读") @RequestParam(required = false) Integer isRead,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {

        try {
            // TODO: 实现管理员消息列表查询，这里暂时用催发货消息代替
            Page<UserMessage> messagePage;
            if (type != null && type.equals(MessageType.SHIPPING_REMINDER.getCode())) {
                messagePage = userMessageService.getShippingReminderMessages(page, size);
            } else {
                // 获取所有消息的方法待实现，暂时使用催发货消息
                messagePage = userMessageService.getShippingReminderMessages(page, size);
            }

            // 转换为DTO
            Page<UserMessageDTO> dtoPage = new Page<>(
                    messagePage.getCurrent(),
                    messagePage.getSize(),
                    messagePage.getTotal(),
                    messagePage.getPages() == 0);

            List<UserMessageDTO> dtoList = messagePage.getRecords().stream()
                    .map(message -> {
                        UserMessageDTO dto = new UserMessageDTO();
                        BeanUtils.copyProperties(message, dto);

                        // 添加消息类型描述
                        MessageType messageType = MessageType.getByCode(message.getType());
                        if (messageType != null) {
                            dto.setTypeDesc(messageType.getDesc());
                        }

                        // 添加用户信息
                        User user = message.getUser();
                        if (user != null) {
                            dto.setUsername(user.getUsername());
                            dto.setAvatar(user.getAvatar());
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());

            dtoPage.setRecords(dtoList);

            return CommonResult.success(dtoPage);
        } catch (Exception e) {
            log.error("获取消息列表失败: ", e);
            return CommonResult.failed("获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取催发货消息列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页催发货消息列表
     */
    @GetMapping("/shipping-reminder")
    @Operation(summary = "获取催发货消息列表", description = "分页获取所有用户的催发货提醒消息")
    public CommonResult<Page<UserMessageDTO>> getShippingReminderMessages(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {

        try {
            Page<UserMessage> messagePage = userMessageService.getShippingReminderMessages(page, size);

            // 转换为DTO
            Page<UserMessageDTO> dtoPage = new Page<>(
                    messagePage.getCurrent(),
                    messagePage.getSize(),
                    messagePage.getTotal(),
                    messagePage.getPages() == 0);

            List<UserMessageDTO> dtoList = messagePage.getRecords().stream()
                    .map(message -> {
                        UserMessageDTO dto = new UserMessageDTO();
                        BeanUtils.copyProperties(message, dto);

                        // 添加消息类型描述
                        MessageType messageType = MessageType.getByCode(message.getType());
                        if (messageType != null) {
                            dto.setTypeDesc(messageType.getDesc());
                        }

                        // 添加用户信息
                        User user = userService.getById(message.getUserId());
                        if (user != null) {
                            dto.setUsername(user.getUsername());
                            dto.setAvatar(user.getAvatar());
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());

            dtoPage.setRecords(dtoList);

            return CommonResult.success(dtoPage);
        } catch (Exception e) {
            log.error("获取催发货消息列表失败: ", e);
            return CommonResult.failed("获取催发货消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建系统消息
     *
     * @param title   消息标题
     * @param content 消息内容
     * @param extra   额外信息（可选）
     * @return 操作结果
     */
    @PostMapping("/system")
    @Operation(summary = "创建系统消息", description = "创建发送给所有用户的系统消息")
    public CommonResult<Boolean> createSystemMessage(
            @Parameter(description = "消息标题") @RequestParam String title,
            @Parameter(description = "消息内容") @RequestParam String content,
            @Parameter(description = "额外信息") @RequestParam(required = false) String extra) {

        try {
            boolean success = userMessageService.createSystemMessage(title, content, extra);

            if (success) {
                return CommonResult.success(true, "系统消息创建成功");
            } else {
                return CommonResult.failed("系统消息创建失败");
            }
        } catch (Exception e) {
            log.error("创建系统消息失败: ", e);
            return CommonResult.failed("创建系统消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读消息数量
     *
     * @return 未读消息数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息数量", description = "获取管理员未读消息数量")
    public CommonResult<Integer> getUnreadCount() {
        try {
            // TODO: 这里暂时只返回催发货消息数量
            Page<UserMessage> messagePage = userMessageService.getShippingReminderMessages(1, 1);

            return CommonResult.success((int) messagePage.getTotal());
        } catch (Exception e) {
            log.error("获取未读消息数量失败: ", e);
            return CommonResult.failed("获取未读消息数量失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     *
     * @param id 消息ID
     * @return 操作结果
     */
    @PutMapping("/read")
    @Operation(summary = "标记消息为已读", description = "将指定消息标记为已读状态")
    public CommonResult<Boolean> markMessageRead(
            @RequestParam String id) {

        try {
            boolean success = userMessageService.markAsRead(id);

            if (success) {
                return CommonResult.success(true, "标记已读成功");
            } else {
                return CommonResult.failed("标记已读失败，消息不存在或已被删除");
            }
        } catch (Exception e) {
            log.error("标记消息已读失败: ", e);
            return CommonResult.failed("标记消息已读失败: " + e.getMessage());
        }
    }

    /**
     * 标记所有消息为已读
     *
     * @return 操作结果
     */
    @PutMapping("/readAll")
    @Operation(summary = "标记所有消息为已读", description = "将所有催发货消息标记为已读状态")
    public CommonResult<Integer> markAllRead() {
        try {
            // TODO: 这里暂时只实现标记所有催发货消息为已读
            Page<UserMessage> messagePage = userMessageService.getShippingReminderMessages(1, Integer.MAX_VALUE);

            int count = 0;
            for (UserMessage message : messagePage.getRecords()) {
                if (message.getIsRead() == 0) { // 未读
                    boolean success = userMessageService.markAsRead(message.getMessageId());
                    if (success) {
                        count++;
                    }
                }
            }

            return CommonResult.success(count, "成功标记" + count + "条消息为已读");
        } catch (Exception e) {
            log.error("标记所有消息已读失败: ", e);
            return CommonResult.failed("标记所有消息已读失败: " + e.getMessage());
        }
    }

    /**
     * 删除消息
     *
     * @param id 消息ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息", description = "删除指定的消息")
    public CommonResult<Boolean> deleteMessage(@PathVariable String id) {
        try {
            boolean success = userMessageService.deleteMessage(id);

            if (success) {
                return CommonResult.success(true, "删除成功");
            } else {
                return CommonResult.failed("删除失败，消息不存在或已被删除");
            }
        } catch (Exception e) {
            log.error("删除消息失败: ", e);
            return CommonResult.failed("删除消息失败: " + e.getMessage());
        }
    }
}