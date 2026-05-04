package com.soccer.forum.service.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.TopicCreateDTO;
import com.soccer.forum.service.modules.admin.dto.TopicUpdateDTO;
import com.soccer.forum.service.modules.admin.dto.TopicVO;
import com.soccer.forum.service.modules.admin.service.AdminTopicService;
import com.soccer.forum.domain.entity.Topic;
import com.soccer.forum.service.modules.community.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTopicServiceImpl implements AdminTopicService {

    private final TopicMapper topicMapper;

    @Override
    public Page<TopicVO> getTopicList(int page, int size, String keyword) {
        Page<Topic> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Topic::getTitle, keyword);
        }
        wrapper.orderByDesc(Topic::getCreatedAt);
        Page<Topic> topicPage = topicMapper.selectPage(pageParam, wrapper);

        List<TopicVO> records = topicPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        Page<TopicVO> result = new Page<>(topicPage.getCurrent(), topicPage.getSize(), topicPage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public TopicVO createTopic(TopicCreateDTO dto) {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getTitle, dto.getTitle());
        if (topicMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("话题标题已存在");
        }

        Topic topic = new Topic();
        topic.setTitle(dto.getTitle());
        topic.setDescription(dto.getDescription());
        topic.setViewCount(0);
        topic.setPostCount(0);
        topic.setCreatedAt(LocalDateTime.now());
        topic.setUpdatedAt(LocalDateTime.now());
        topicMapper.insert(topic);

        return convertToVO(topic);
    }

    @Override
    public TopicVO updateTopic(Long id, TopicUpdateDTO dto) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            throw new RuntimeException("话题不存在");
        }

        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getTitle, dto.getTitle());
        wrapper.ne(Topic::getId, id);
        if (topicMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("话题标题已存在");
        }

        topic.setTitle(dto.getTitle());
        topic.setDescription(dto.getDescription());
        topic.setUpdatedAt(LocalDateTime.now());
        topicMapper.updateById(topic);

        return convertToVO(topic);
    }

    @Override
    public void deleteTopic(Long id) {
        topicMapper.deleteById(id);
    }

    private TopicVO convertToVO(Topic topic) {
        TopicVO vo = new TopicVO();
        BeanUtils.copyProperties(topic, vo);
        vo.setCreatedAt(topic.getCreatedAt());
        vo.setUpdatedAt(topic.getUpdatedAt());
        return vo;
    }
}
