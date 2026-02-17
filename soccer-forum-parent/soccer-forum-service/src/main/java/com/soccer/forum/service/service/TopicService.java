package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soccer.forum.domain.entity.Topic;

import java.util.List;

public interface TopicService extends IService<Topic> {
    /**
     * 获取热门话题
     * @param limit 数量限制
     * @return 话题列表
     */
    List<Topic> getHotTopics(int limit);
}
