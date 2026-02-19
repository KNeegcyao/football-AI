package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soccer.forum.domain.entity.Comment;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.model.dto.CommentCreateReq;
import com.soccer.forum.service.model.dto.CommentPageReq;
import com.soccer.forum.service.model.dto.CommentResp;

public interface CommentService extends IService<Comment> {
    void createComment(CommentCreateReq req, Long userId);
    Page<CommentResp> getCommentPage(CommentPageReq req);
    void deleteComment(Long id, Long userId);
}
