package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.Notification;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.soccer.forum.service.model.dto.NotificationResp;
import com.soccer.forum.service.model.dto.UserSimpleResp;
import com.soccer.forum.service.service.UserService;
import com.soccer.forum.service.service.CommentService;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.domain.entity.Comment;
import org.springframework.beans.BeanUtils;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

@Tag(name = "通知管理", description = "用户通知/消息相关接口")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;
    private final CommentService commentService;

    public NotificationController(NotificationService notificationService, UserService userService, CommentService commentService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.commentService = commentService;
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread-count")
    public R<Long> getUnreadCount(@Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        return R.ok(notificationService.getUnreadCount(loginUser.getUser().getId()));
    }

    @Operation(summary = "分页获取通知列表")
    @GetMapping
    public R<Page<NotificationResp>> list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                                          @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
                                          @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        Page<Notification> notificationPage = notificationService.getNotificationPage(loginUser.getUser().getId(), page, size);
        
        // 1. 获取用户信息
        List<Long> userIds = notificationPage.getRecords().stream()
                .map(Notification::getFromUserId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, UserSimpleResp> userMap;
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, user -> {
                UserSimpleResp resp = new UserSimpleResp();
                BeanUtils.copyProperties(user, resp);
                return resp;
            }));
        } else {
            userMap = Map.of();
        }

        // 2. 获取评论对应的帖子ID
        List<Long> commentIds = notificationPage.getRecords().stream()
                .filter(n -> n.getType() == 2 || n.getType() == 4)
                .map(Notification::getTargetId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Long> commentPostMap;
        if (!commentIds.isEmpty()) {
            List<Comment> comments = commentService.listByIds(commentIds);
            commentPostMap = comments.stream().collect(Collectors.toMap(Comment::getId, Comment::getPostId));
        } else {
            commentPostMap = Map.of();
        }

        List<NotificationResp> resps = notificationPage.getRecords().stream().map(notification -> {
            UserSimpleResp fromUser = userMap.get(notification.getFromUserId());
            if (fromUser == null) {
                // 如果用户被删除或找不到，创建一个默认的
                fromUser = new UserSimpleResp();
                fromUser.setId(notification.getFromUserId());
                fromUser.setNickname("未知用户");
                fromUser.setAvatar("/static/default-avatar.png");
            }
            NotificationResp resp = NotificationResp.from(notification, fromUser);
            
            // 设置 PostId
            if (notification.getType() == 1 || notification.getType() == 3) {
                // 1:点赞帖子, 3:评论帖子 -> targetId 就是 postId
                resp.setPostId(notification.getTargetId());
            } else if (notification.getType() == 2 || notification.getType() == 4) {
                // 2:点赞评论, 4:回复评论 -> targetId 是 commentId
                resp.setPostId(commentPostMap.get(notification.getTargetId()));
            }
            
            return resp;
        }).collect(Collectors.toList());

        Page<NotificationResp> resultPage = new Page<>(page, size);
        resultPage.setTotal(notificationPage.getTotal());
        resultPage.setRecords(resps);
        
        return R.ok(resultPage);
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public R<Void> read(@Parameter(description = "通知ID") @PathVariable Long id, 
                        @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        notificationService.markAsRead(id, loginUser.getUser().getId());
        return R.ok();
    }

    @Operation(summary = "全部标记为已读")
    @PutMapping("/read-all")
    public R<Void> readAll(@Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        notificationService.markAllAsRead(loginUser.getUser().getId());
        return R.ok();
    }
}
