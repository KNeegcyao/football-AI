package com.soccer.forum.service.modules.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soccer.forum.domain.entity.Topic;

import java.util.List;

/**
 * 话题服务接口
 */
public interface TopicService extends IService<Topic> {
    /**
     * 获取热门话题
     * @param limit 获取数量
     * @return 话题列表
     */
    List<Topic> getHotTopics(int limit);

    /**
     * 增加话题阅读量并校准帖子数
     * @param id 话题ID
     * @return 话题对象
     */
    Topic incrementViewCountAndSyncPostCount(Long id);
}
