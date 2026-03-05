package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soccer.forum.domain.entity.ChatMessage;
import com.soccer.forum.domain.entity.ChatSession;
import com.soccer.forum.service.mapper.ChatMessageMapper;
import com.soccer.forum.service.service.ChatMessageService;
import com.soccer.forum.service.service.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Autowired
    private ChatSessionService sessionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content, Integer type) {
        ChatSession session = sessionService.getOrCreateSession(senderId, receiverId);
        
        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(type);
        message.setStatus(0); // 未读
        message.setCreatedAt(LocalDateTime.now());
        this.save(message);

        // 更新会话最后一条消息
        sessionService.updateLastMessage(session.getId(), content, senderId);
        
        return message;
    }

    @Override
    public IPage<ChatMessage> getSessionMessages(Long sessionId, Page<ChatMessage> page) {
        return this.page(page, new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreatedAt));
    }

    @Override
    public void markAsRead(Long sessionId, Long receiverId) {
        this.update(new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getReceiverId, receiverId)
                .eq(ChatMessage::getStatus, 0)
                .set(ChatMessage::getStatus, 1));
        
        // 重置会话未读数
        sessionService.resetUnreadCount(sessionId, receiverId);
    }
}
