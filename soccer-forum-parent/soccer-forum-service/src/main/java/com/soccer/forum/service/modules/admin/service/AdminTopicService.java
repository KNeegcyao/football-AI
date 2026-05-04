package com.soccer.forum.service.modules.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.TopicCreateDTO;
import com.soccer.forum.service.modules.admin.dto.TopicUpdateDTO;
import com.soccer.forum.service.modules.admin.dto.TopicVO;

public interface AdminTopicService {
    Page<TopicVO> getTopicList(int page, int size, String keyword);
    TopicVO createTopic(TopicCreateDTO dto);
    TopicVO updateTopic(Long id, TopicUpdateDTO dto);
    void deleteTopic(Long id);
}
