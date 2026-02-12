package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.service.model.dto.CommentCreateReq;
import com.soccer.forum.service.model.dto.CommentPageReq;
import com.soccer.forum.service.model.dto.CommentResp;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 评论管理控制器
 * <p>
 * 提供帖子评论的发布、分页查询及删除功能。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Tag(name = "评论管理", description = "评论功能接口")
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 发表评论接口
     * <p>
     * 支持对帖子发表一级评论，或对已有评论进行回复。
     * </p>
     *
     * @param req 评论创建请求对象
     * @param loginUser 当前登录用户信息
     * @return 操作结果
     */
    @Operation(summary = "发表评论", description = "发表新评论或回复他人评论")
    @PostMapping
    public R<Void> create(@Parameter(description = "评论信息") @Validated @RequestBody CommentCreateReq req, 
                         @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("收到发表评论请求: 用户ID={}, 帖子ID={}", loginUser.getUser().getId(), req.getPostId());
        commentService.createComment(req, loginUser.getUser().getId());
        return R.ok(null, "评论发表成功");
    }

    /**
     * 分页查询评论接口
     * <p>
     * 根据帖子 ID 获取评论列表，支持树状结构或平铺显示。
     * </p>
     *
     * @param req 分页查询参数对象
     * @return 评论分页列表
     */
    @Operation(summary = "获取评论列表", description = "分页获取指定帖子的评论列表")
    @GetMapping("/list")
    public R<Page<CommentResp>> list(@Parameter(description = "分页及查询参数") @Validated CommentPageReq req) {
        log.info("收到获取评论列表请求: 帖子ID={}, page={}", req.getPostId(), req.getPage());
        Page<CommentResp> commentPage = commentService.getCommentPage(req);
        return R.ok(commentPage);
    }

    /**
     * 删除评论接口
     * <p>
     * 逻辑删除指定评论。
     * </p>
     *
     * @param id 评论 ID
     * @param loginUser 当前登录用户信息
     * @return 操作结果
     */
    @Operation(summary = "删除评论", description = "逻辑删除指定评论")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "评论ID") @PathVariable Long id, 
                         @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        log.info("收到删除评论请求: 用户ID={}, 评论ID={}", loginUser.getUser().getId(), id);
        commentService.deleteComment(id, loginUser.getUser().getId());
        return R.ok(null, "评论删除成功");
    }
}
