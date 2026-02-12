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

@Tag(name = "通知管理", description = "用户通知/消息相关接口")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread-count")
    public R<Long> getUnreadCount(@Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        return R.ok(notificationService.getUnreadCount(loginUser.getUser().getId()));
    }

    @Operation(summary = "分页获取通知列表")
    @GetMapping
    public R<Page<Notification>> list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                                      @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
                                      @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        return R.ok(notificationService.getNotificationPage(loginUser.getUser().getId(), page, size));
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
