package com.soccer.forum.service.modules.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.CommentVO;
import com.soccer.forum.service.modules.admin.mapper.AdminCommentMapper;
import com.soccer.forum.service.modules.admin.service.AdminCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCommentServiceImpl implements AdminCommentService {

    private final AdminCommentMapper adminCommentMapper;

    @Override
    public Page<CommentVO> getCommentList(int page, int size, String keyword, Long postId) {
        Page<CommentVO> pageParam = new Page<>(page, size);
        return adminCommentMapper.selectCommentPage(pageParam, keyword, postId);
    }

    @Override
    public void deleteComment(Long id) {
        adminCommentMapper.deleteComment(id);
    }
}
