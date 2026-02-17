package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soccer.forum.domain.entity.Topic;
import com.soccer.forum.service.mapper.TopicMapper;
import com.soccer.forum.service.service.TopicService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Override
    public List<Topic> getHotTopics(int limit) {
        LambdaQueryWrapper<Topic> query = new LambdaQueryWrapper<>();
        // 按帖子数和浏览量综合排序（这里简单地按帖子数降序）
        query.orderByDesc(Topic::getPostCount);
        query.last("LIMIT " + limit);
        return baseMapper.selectList(query);
    }
}
