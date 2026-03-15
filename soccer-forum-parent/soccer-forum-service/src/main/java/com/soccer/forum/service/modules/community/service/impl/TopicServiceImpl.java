package com.soccer.forum.service.modules.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soccer.forum.domain.entity.Post;
import com.soccer.forum.domain.entity.Topic;
import com.soccer.forum.service.modules.community.mapper.PostMapper;
import com.soccer.forum.service.modules.community.mapper.TopicMapper;
import com.soccer.forum.service.modules.community.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    private final PostMapper postMapper;

    @Override
    public List<Topic> getHotTopics(int limit) {
        LambdaQueryWrapper<Topic> query = new LambdaQueryWrapper<>();
        // 按照帖子数排序
        query.orderByDesc(Topic::getPostCount);
        query.last("LIMIT " + limit);
        return baseMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Topic incrementViewCountAndSyncPostCount(Long id) {
        Topic topic = getById(id);
        if (topic != null) {
            // 增加阅读
            topic.setViewCount((topic.getViewCount() != null ? topic.getViewCount() : 0) + 1);
            
            // 实时校准讨论数：统计该话题下的帖子数
            Long actualPostCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                    .eq(Post::getTopicId, id)
                    .eq(Post::getStatus, 1));
            
            topic.setPostCount(actualPostCount.intValue());
            updateById(topic);
        }
        return topic;
    }
}
