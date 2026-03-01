package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.soccer.forum.domain.entity.Conversation;
import com.soccer.forum.domain.entity.Message;
import com.soccer.forum.service.model.dto.MessageSendReq;

public interface MessageService extends IService<Message> {
    /**
     * 发送私信
     */
    void sendMessage(MessageSendReq req, Long senderId);

    /**
     * 获取会话列表
     */
    Page<Conversation> getConversationPage(Long userId, Integer page, Integer size);

    /**
     * 获取会话详情消息列表
     */
    Page<Message> getMessagePage(Long userId, Long otherUserId, Integer page, Integer size);

    /**
     * 统计对方未回复的消息数
     */
    int countUnrepliedMessage(Long senderId, Long receiverId);
}
