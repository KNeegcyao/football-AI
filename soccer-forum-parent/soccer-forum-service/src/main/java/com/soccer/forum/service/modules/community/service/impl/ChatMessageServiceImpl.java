package com.soccer.forum.service.modules.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soccer.forum.domain.entity.ChatMessage;
import com.soccer.forum.domain.entity.ChatSession;
import com.soccer.forum.service.modules.community.mapper.ChatMessageMapper;
import com.soccer.forum.service.modules.community.service.ChatMessageService;
import com.soccer.forum.service.modules.community.service.ChatSessionService;
import com.soccer.forum.service.modules.user.service.UserRelationshipService;
import com.soccer.forum.service.modules.ai.service.DifyAiService;
import com.soccer.forum.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Autowired
    private ChatSessionService sessionService;

    @Autowired
    private UserRelationshipService relationshipService;

    @Lazy
    @Autowired
    private DifyAiService difyAiService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content, Integer type) {
        // 检查是否被对方拉黑
        if (relationshipService.isBlacklisted(receiverId, senderId)) {
            throw new ServiceException("消息发送失败，你已被对方加入黑名单");
        }
        
        // 检查自己是否拉黑了对方
        if (relationshipService.isBlacklisted(senderId, receiverId)) {
            throw new ServiceException("消息发送失败，你已将对方加入黑名单");
        }
        
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
        
        // 如果发给官方助手 (id=1)，则触发 AI 自动回复
        if (Long.valueOf(1).equals(receiverId) && !Long.valueOf(1).equals(senderId)) {
            difyAiService.asyncReplyAsOfficialAssistant(senderId, content);
        }
        
        return message;
    }

    @Override
    public IPage<ChatMessage> getSessionMessages(Long sessionId, Page<ChatMessage> page) {
        return this.baseMapper.getSessionMessagesBySessionId(page, sessionId);
    }

    @Override
    public void markAsRead(Long sessionId, Long receiverId) {
        this.baseMapper.markAsReadByReceiver(sessionId, receiverId);

        // 閲嶇疆浼氳瘽鏈鏁?
        sessionService.resetUnreadCount(sessionId, receiverId);
    }
}
