package com.muyingmall.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 消息事件基类
 */
@Getter
public abstract class MessageEvent extends ApplicationEvent {

    /**
     * 用户ID
     */
    private final Integer userId;

    /**
     * 消息标题
     */
    private final String title;

    /**
     * 消息内容
     */
    private final String content;

    /**
     * 额外数据（JSON格式）
     */
    private final String extra;

    /**
     * 构造函数
     *
     * @param source  事件源
     * @param userId  用户ID
     * @param title   消息标题
     * @param content 消息内容
     * @param extra   额外信息（JSON格式）
     */
    public MessageEvent(Object source, Integer userId, String title, String content, String extra) {
        super(source);
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.extra = extra;
    }

    /**
     * 获取消息类型，由子类实现
     *
     * @return 消息类型代码
     */
    public abstract String getMessageType();
}