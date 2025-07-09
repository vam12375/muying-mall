package com.muyingmall.event;

import com.muyingmall.entity.AdminOperationLog;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 管理员操作事件
 */
@Getter
public class AdminOperationEvent extends ApplicationEvent {

    private final AdminOperationLog operationLog;

    public AdminOperationEvent(Object source, AdminOperationLog operationLog) {
        super(source);
        this.operationLog = operationLog;
    }
}
