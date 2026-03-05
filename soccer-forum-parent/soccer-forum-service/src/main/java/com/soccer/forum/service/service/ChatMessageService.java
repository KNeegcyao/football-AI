package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.soccer.forum.domain.entity.ChatMessage;

public interface ChatMessageService extends IService<ChatMessage> {
    /**
     * 发送消息
     */
    ChatMessage sendMessage(Long senderId, Long receiverId, String content, Integer type);

    /**
     * 获取会话消息列表
     */
    IPage<ChatMessage> getSessionMessages(Long sessionId, Page<ChatMessage> page);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long sessionId, Long receiverId);
}
