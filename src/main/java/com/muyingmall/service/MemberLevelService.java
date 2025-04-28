package com.muyingmall.service;

import com.muyingmall.entity.MemberLevel;
import java.util.List;

/**
 * 会员等级服务接口
 */
public interface MemberLevelService {

    /**
     * 获取所有会员等级
     * 
     * @return 会员等级列表
     */
    List<MemberLevel> getAllLevels();

    /**
     * 根据积分获取会员等级
     * 
     * @param points 用户积分
     * @return 会员等级对象
     */
    MemberLevel getLevelByPoints(Integer points);

    /**
     * 根据积分获取会员等级名称
     * 
     * @param points 用户积分
     * @return 会员等级名称
     */
    String getLevelNameByPoints(Integer points);
}