package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyingmall.entity.CircleTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 育儿圈话题Mapper
 */
@Mapper
public interface CircleTopicMapper extends BaseMapper<CircleTopic> {

    /**
     * 增加帖子数量
     */
    @Update("UPDATE circle_topic SET post_count = post_count + 1 WHERE topic_id = #{topicId}")
    int incrementPostCount(Integer topicId);

    /**
     * 减少帖子数量
     */
    @Update("UPDATE circle_topic SET post_count = GREATEST(post_count - 1, 0) WHERE topic_id = #{topicId}")
    int decrementPostCount(Integer topicId);
}
