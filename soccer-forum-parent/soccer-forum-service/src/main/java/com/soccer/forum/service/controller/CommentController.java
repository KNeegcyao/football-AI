package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.model.dto.CommentCreateReq;
import com.soccer.forum.service.model.dto.CommentPageReq;
import com.soccer.forum.service.model.dto.CommentResp;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.CommentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public String create(@RequestBody CommentCreateReq req, @AuthenticationPrincipal LoginUser loginUser) {
        commentService.createComment(req, loginUser.getUser().getId());
        return "success";
    }

    @GetMapping
    public Page<CommentResp> list(CommentPageReq req) {
        return commentService.getCommentPage(req);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        commentService.deleteComment(id, loginUser.getUser().getId());
        return "success";
    }
}
