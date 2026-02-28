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
import com.soccer.forum.service.service.PostService;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.domain.entity.Comment;
import com.soccer.forum.domain.entity.Post;
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
    private final PostService postService;

    public NotificationController(NotificationService notificationService, UserService userService, CommentService commentService, PostService postService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.commentService = commentService;
        this.postService = postService;
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
                                          @Parameter(description = "类型过滤 (支持单个数字或逗号分隔的多个数字)") @RequestParam(required = false) String type,
                                          @Parameter(description = "多个类型过滤 (逗号分隔，兼容旧版)") @RequestParam(required = false) String types,
                                          @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        
        List<Integer> typeList = new ArrayList<>();
        String typeStr = type != null ? type : types;
        
        System.out.println("Notification API Filter - Type: " + type + ", Types: " + types + ", Final TypeStr: " + typeStr);
        
        if (typeStr != null && !typeStr.isEmpty()) {
            for (String t : typeStr.split(",")) {
                try {
                    int tInt = Integer.parseInt(t.trim());
                    typeList.add(tInt);
                } catch (NumberFormatException e) {
                    System.err.println("Failed to parse type: " + t);
                }
            }
        }
        
        System.out.println("Final TypeList for Query: " + typeList);

        Page<Notification> notificationPage = notificationService.getNotificationPage(loginUser.getUser().getId(), page, size, typeList);
        
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

        // 3. 获取评论/赞对应的帖子标题
        List<Long> postIdsForTitle = notificationPage.getRecords().stream()
                .filter(n -> n.getType() == 1 || n.getType() == 3)
                .map(Notification::getTargetId)
                .collect(Collectors.toList());

        // 评论回复中的原帖ID
        List<Long> postIdsFromComments = notificationPage.getRecords().stream()
                .filter(n -> n.getType() == 2 || n.getType() == 4)
                .map(n -> commentPostMap.get(n.getTargetId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        postIdsForTitle.addAll(postIdsFromComments);

        Map<Long, String> postTitleMap;
        if (!postIdsForTitle.isEmpty()) {
            List<Post> posts = postService.listByIds(postIdsForTitle.stream().distinct().collect(Collectors.toList()));
            postTitleMap = posts.stream().collect(Collectors.toMap(Post::getId, Post::getTitle));
        } else {
            postTitleMap = Map.of();
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
            
            // 设置 PostId 和 PostTitle
            if (notification.getType() == 1 || notification.getType() == 3) {
                // 1:点赞帖子, 3:评论帖子 -> targetId 就是 postId
                resp.setPostId(notification.getTargetId());
                resp.setPostTitle(postTitleMap.get(notification.getTargetId()));
            } else if (notification.getType() == 2 || notification.getType() == 4) {
                // 2:点赞评论, 4:回复评论 -> targetId 是 commentId
                Long pid = commentPostMap.get(notification.getTargetId());
                resp.setPostId(pid);
                resp.setPostTitle(postTitleMap.get(pid));
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

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "通知ID") @PathVariable Long id, 
                          @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        notificationService.deleteNotification(id, loginUser.getUser().getId());
        return R.ok();
    }
}
