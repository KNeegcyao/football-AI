package com.soccer.forum.service.modules.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.PostVO;
import com.soccer.forum.service.modules.admin.mapper.AdminPostMapper;
import com.soccer.forum.service.modules.admin.service.AdminPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPostServiceImpl implements AdminPostService {

    private final AdminPostMapper adminPostMapper;

    @Override
    public Page<PostVO> getPostList(int page, int size, String keyword, Integer status, Long topicId) {
        Page<PostVO> pageParam = new Page<>(page, size);
        return adminPostMapper.selectPostPage(pageParam, keyword, status, topicId);
    }

    @Override
    public void updatePostStatus(Long id, Integer status) {
        adminPostMapper.updatePostStatus(id, status);
    }

    @Override
    public void deletePost(Long id) {
        adminPostMapper.updatePostStatus(id, 1);
    }
}
