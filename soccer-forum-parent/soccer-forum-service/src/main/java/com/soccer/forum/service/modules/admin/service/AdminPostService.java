package com.soccer.forum.service.modules.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.PostVO;

public interface AdminPostService {
    Page<PostVO> getPostList(int page, int size, String keyword, Integer status, Long topicId);
    void updatePostStatus(Long id, Integer status);
    void deletePost(Long id);
}
