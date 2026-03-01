package com.soccer.forum.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.core.domain.R;
import com.soccer.forum.domain.entity.Conversation;
import com.soccer.forum.domain.entity.Message;
import com.soccer.forum.service.model.dto.MessageSendReq;
import com.soccer.forum.service.service.MessageService;
import com.soccer.forum.service.security.model.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "私信管理", description = "私信消息/会话相关接口")
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "发送私信")
    @PostMapping("/send")
    public R<Void> send(@Valid @RequestBody MessageSendReq req, 
                        @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        messageService.sendMessage(req, loginUser.getUser().getId());
        return R.ok();
    }

    @Operation(summary = "分页获取会话列表")
    @GetMapping("/conversations")
    public R<Page<Conversation>> conversations(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                                              @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
                                              @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        return R.ok(messageService.getConversationPage(loginUser.getUser().getId(), page, size));
    }

    @Operation(summary = "分页获取私信详情")
    @GetMapping("/detail/{otherUserId}")
    public R<Page<Message>> detail(@PathVariable Long otherUserId,
                                  @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                                  @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size,
                                  @Parameter(hidden = true) @AuthenticationPrincipal LoginUser loginUser) {
        return R.ok(messageService.getMessagePage(loginUser.getUser().getId(), otherUserId, page, size));
    }
}
