package com.muyingmall.event;

import com.muyingmall.enums.MessageType;
import lombok.Getter;

/**
 * 签到事件
 */
@Getter
public class CheckinEvent extends MessageEvent {

    /**
     * 签到获得的积分
     */
    private final Integer points;

    /**
     * 连续签到天数
     */
    private final Integer consecutiveDays;

    /**
     * 构造函数
     *
     * @param source          事件源
     * @param userId          用户ID
     * @param points          签到获得的积分
     * @param consecutiveDays 连续签到天数
     * @param extra           额外信息
     */
    public CheckinEvent(Object source, Integer userId, Integer points, Integer consecutiveDays, String extra) {
        super(source, userId, generateTitle(consecutiveDays), generateContent(points, consecutiveDays), extra);
        this.points = points;
        this.consecutiveDays = consecutiveDays;
    }

    @Override
    public String getMessageType() {
        return MessageType.CHECKIN.getCode();
    }

    /**
     * 生成消息标题
     *
     * @param consecutiveDays 连续签到天数
     * @return 消息标题
     */
    private static String generateTitle(Integer consecutiveDays) {
        if (consecutiveDays > 1) {
            return "恭喜您，已连续签到" + consecutiveDays + "天";
        } else {
            return "签到成功，请继续保持";
        }
    }

    /**
     * 生成消息内容
     *
     * @param points          签到获得的积分
     * @param consecutiveDays 连续签到天数
     * @return 消息内容
     */
    private static String generateContent(Integer points, Integer consecutiveDays) {
        StringBuilder content = new StringBuilder();
        content.append("您今日签到获得了").append(points).append("积分。");

        if (consecutiveDays > 1) {
            content.append("您已连续签到").append(consecutiveDays).append("天，继续保持可获得更多奖励！");
        } else {
            content.append("开始您的签到之旅，连续签到可以获得更多奖励哦！");
        }

        return content.toString();
    }
}