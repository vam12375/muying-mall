package com.muyingmall.event;

import com.muyingmall.entity.AdminLoginRecord;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 管理员登录事件
 */
@Getter
public class AdminLoginEvent extends ApplicationEvent {

    private final AdminLoginRecord loginRecord;

    public AdminLoginEvent(Object source, AdminLoginRecord loginRecord) {
        super(source);
        this.loginRecord = loginRecord;
    }
}
