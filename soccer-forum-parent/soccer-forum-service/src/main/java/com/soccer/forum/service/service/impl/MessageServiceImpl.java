package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Conversation;
import com.soccer.forum.domain.entity.Message;
import com.soccer.forum.service.mapper.ConversationMapper;
import com.soccer.forum.service.mapper.MessageMapper;
import com.soccer.forum.service.model.dto.MessageSendReq;
import com.soccer.forum.service.service.MessageService;
import com.soccer.forum.service.service.NotificationService;
import com.soccer.forum.service.service.UserRelationshipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final MessageMapper messageMapper;
    private final ConversationMapper conversationMapper;
    private final UserRelationshipService relationshipService;
    private final NotificationService notificationService;

    public MessageServiceImpl(MessageMapper messageMapper, 
                              ConversationMapper conversationMapper, 
                              UserRelationshipService relationshipService, 
                              NotificationService notificationService) {
        this.messageMapper = messageMapper;
        this.conversationMapper = conversationMapper;
        this.relationshipService = relationshipService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(MessageSendReq req, Long senderId) {
        Long receiverId = req.getReceiverId();
        if (senderId.equals(receiverId)) {
            throw new ServiceException("不能给自己发私信", ServiceErrorCode.PARAM_ERROR.getCode());
        }

        // 1. 检查关注关系
        boolean isMutual = relationshipService.isMutualFollow(senderId, receiverId);
        
        if (!isMutual) {
            // 2. 检查未回复消息数
            int unrepliedCount = countUnrepliedMessage(senderId, receiverId);
            if (unrepliedCount >= 1) {
                throw new ServiceException("对方尚未回关，您只能发送一条打招呼消息", ServiceErrorCode.FORBIDDEN.getCode());
            }
        }

        // 3. 保存消息
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(req.getContent());
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);

        // 4. 更新/创建会话
        updateConversation(senderId, receiverId, req.getContent());

        // 5. 发送通知 (私信类型为 7)
        notificationService.sendNotification(receiverId, senderId, 7, message.getId(), req.getContent());
    }

    private void updateConversation(Long senderId, Long receiverId, String content) {
        Long userA = Math.min(senderId, receiverId);
        Long userB = Math.max(senderId, receiverId);

        Conversation conv = conversationMapper.selectOne(new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserA, userA)
                .eq(Conversation::getUserB, userB));

        if (conv == null) {
            conv = new Conversation();
            conv.setUserA(userA);
            conv.setUserB(userB);
            conv.setLastMessage(content);
            conv.setLastSenderId(senderId);
            conv.setUnreadCountA(senderId.equals(userB) ? 1 : 0);
            conv.setUnreadCountB(senderId.equals(userA) ? 1 : 0);
            conv.setUpdatedAt(LocalDateTime.now());
            conversationMapper.insert(conv);
        } else {
            conv.setLastMessage(content);
            conv.setLastSenderId(senderId);
            if (senderId.equals(userA)) {
                conv.setUnreadCountB(conv.getUnreadCountB() + 1);
            } else {
                conv.setUnreadCountA(conv.getUnreadCountA() + 1);
            }
            conv.setUpdatedAt(LocalDateTime.now());
            conversationMapper.updateById(conv);
        }
    }

    @Override
    public Page<Conversation> getConversationPage(Long userId, Integer page, Integer size) {
        Page<Conversation> p = new Page<>(page, size);
        return conversationMapper.selectPage(p, new LambdaQueryWrapper<Conversation>()
                .and(w -> w.eq(Conversation::getUserA, userId).or().eq(Conversation::getUserB, userId))
                .orderByDesc(Conversation::getUpdatedAt));
    }

    @Override
    public Page<Message> getMessagePage(Long userId, Long otherUserId, Integer page, Integer size) {
        Page<Message> p = new Page<>(page, size);
        
        // 标记该会话的消息为已读
        clearUnreadCount(userId, otherUserId);

        return messageMapper.selectPage(p, new LambdaQueryWrapper<Message>()
                .and(w -> w.and(iw -> iw.eq(Message::getSenderId, userId).eq(Message::getReceiverId, otherUserId))
                        .or(iw -> iw.eq(Message::getSenderId, otherUserId).eq(Message::getReceiverId, userId)))
                .orderByDesc(Message::getCreatedAt));
    }

    private void clearUnreadCount(Long userId, Long otherUserId) {
        Long userA = Math.min(userId, otherUserId);
        Long userB = Math.max(userId, otherUserId);

        Conversation conv = conversationMapper.selectOne(new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserA, userA)
                .eq(Conversation::getUserB, userB));

        if (conv != null) {
            if (userId.equals(userA)) {
                conv.setUnreadCountA(0);
            } else {
                conv.setUnreadCountB(0);
            }
            conversationMapper.updateById(conv);
        }
    }

    @Override
    public int countUnrepliedMessage(Long senderId, Long receiverId) {
        // 检查 B 是否给 A 发过消息 (回复)
        Long replyCount = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getSenderId, receiverId)
                .eq(Message::getReceiverId, senderId));
        
        if (replyCount > 0) {
            return 0; // 只要回复过，就不算未回复
        }

        // 统计 A 给 B 发的消息数
        Long sentCount = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getSenderId, senderId)
                .eq(Message::getReceiverId, receiverId));
        
        return sentCount.intValue();
    }
}
