package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.ChatMessage;
import com.soccer.forum.service.model.dto.ChatSessionResp;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.ChatMessageService;
import com.soccer.forum.service.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "聊天管理", description = "私信聊天相关接口")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatSessionService sessionService;

    @Autowired
    private ChatMessageService messageService;

    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public R<List<ChatSessionResp>> getSessions(@AuthenticationPrincipal LoginUser loginUser) {
        return R.ok(sessionService.getUserSessions(loginUser.getUser().getId()));
    }

    @Operation(summary = "获取会话详情消息列表")
    @GetMapping("/messages/{sessionId}")
    public R<IPage<ChatMessage>> getMessages(@PathVariable Long sessionId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "20") Integer size,
                                            @AuthenticationPrincipal LoginUser loginUser) {
        // 标记消息为已读
        messageService.markAsRead(sessionId, loginUser.getUser().getId());
        return R.ok(messageService.getSessionMessages(sessionId, new Page<>(page, size)));
    }

    @Operation(summary = "通过对方用户ID获取或创建会话")
    @GetMapping("/session/user/{otherUserId}")
    public R<Long> getSessionByUserId(@PathVariable Long otherUserId,
                                     @AuthenticationPrincipal LoginUser loginUser) {
        return R.ok(sessionService.getOrCreateSession(loginUser.getUser().getId(), otherUserId).getId());
    }
}
