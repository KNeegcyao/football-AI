package com.soccer.forum.service.modules.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soccer.forum.domain.entity.Comment;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.community.model.CommentCreateReq;
import com.soccer.forum.service.modules.community.model.CommentPageReq;
import com.soccer.forum.service.modules.community.model.CommentResp;

/**
 * 评论服务接口
 */
public interface CommentService extends IService<Comment> {
    /**
     * 创建评论
     * @param req 评论请求
     * @param userId 用户ID
     */
    void createComment(CommentCreateReq req, Long userId);

    /**
     * 分页查询评论
     * @param req 分页请求
     * @return 评论分页对象
     */
    Page<CommentResp> getCommentPage(CommentPageReq req);

    /**
     * 删除评论
     * @param id 评论ID
     * @param userId 用户ID
     */
    void deleteComment(Long id, Long userId);
}
