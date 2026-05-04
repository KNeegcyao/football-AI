package com.soccer.forum.service.modules.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.CommentVO;

public interface AdminCommentService {
    Page<CommentVO> getCommentList(int page, int size, String keyword, Long postId);
    void deleteComment(Long id);
}
