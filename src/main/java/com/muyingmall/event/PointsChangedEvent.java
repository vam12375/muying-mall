package com.muyingmall.event;

import com.muyingmall.enums.MessageType;
import lombok.Getter;

/**
 * 积分变更事件
 */
@Getter
public class PointsChangedEvent extends MessageEvent {

    /**
     * 积分变化值（正数为增加，负数为减少）
     */
    private final Integer pointsChange;

    /**
     * 变更后的总积分
     */
    private final Integer totalPoints;

    /**
     * 积分变更原因
     */
    private final String reason;

    /**
     * 构造函数
     *
     * @param source       事件源
     * @param userId       用户ID
     * @param pointsChange 积分变化值
     * @param totalPoints  变更后总积分
     * @param reason       变更原因
     * @param extra        额外信息
     */
    public PointsChangedEvent(Object source, Integer userId, Integer pointsChange, Integer totalPoints,
            String reason, String extra) {
        super(source, userId, generateTitle(pointsChange), generateContent(pointsChange, totalPoints, reason), extra);
        this.pointsChange = pointsChange;
        this.totalPoints = totalPoints;
        this.reason = reason;
    }

    @Override
    public String getMessageType() {
        return MessageType.POINTS.getCode();
    }

    /**
     * 生成消息标题
     *
     * @param pointsChange 积分变化值
     * @return 消息标题
     */
    private static String generateTitle(Integer pointsChange) {
        if (pointsChange > 0) {
            return "恭喜您获得" + pointsChange + "积分";
        } else {
            return "您的账户扣除了" + Math.abs(pointsChange) + "积分";
        }
    }

    /**
     * 生成消息内容
     *
     * @param pointsChange 积分变化值
     * @param totalPoints  变更后总积分
     * @param reason       变更原因
     * @return 消息内容
     */
    private static String generateContent(Integer pointsChange, Integer totalPoints, String reason) {
        StringBuilder content = new StringBuilder();
        if (pointsChange > 0) {
            content.append("恭喜您获得").append(pointsChange).append("积分");
        } else {
            content.append("您的账户扣除了").append(Math.abs(pointsChange)).append("积分");
        }
        content.append("，原因：").append(reason).append("。");
        content.append("当前账户积分余额：").append(totalPoints).append("。");
        return content.toString();
    }
}