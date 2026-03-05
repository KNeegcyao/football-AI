package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soccer.forum.domain.entity.ChatSession;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.mapper.ChatSessionMapper;
import com.soccer.forum.service.model.dto.ChatSessionResp;
import com.soccer.forum.service.service.ChatSessionService;
import com.soccer.forum.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    public ChatSession getOrCreateSession(Long userOneId, Long userTwoId) {
        // 保证 userOneId < userTwoId
        long one = Math.min(userOneId, userTwoId);
        long two = Math.max(userOneId, userTwoId);

        ChatSession session = this.getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserOneId, one)
                .eq(ChatSession::getUserTwoId, two));

        if (session == null) {
            session = new ChatSession();
            session.setUserOneId(one);
            session.setUserTwoId(two);
            session.setUnreadCountOne(0);
            session.setUnreadCountTwo(0);
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            this.save(session);
        }
        return session;
    }

    @Override
    public List<ChatSessionResp> getUserSessions(Long userId) {
        List<ChatSession> sessions = this.list(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserOneId, userId)
                .or()
                .eq(ChatSession::getUserTwoId, userId)
                .orderByDesc(ChatSession::getUpdatedAt));

        return sessions.stream().map(session -> {
            ChatSessionResp resp = new ChatSessionResp();
            resp.setId(session.getId());
            resp.setLastMessage(session.getLastMessage());
            resp.setLastMessageTime(session.getLastMessageTime());
            
            Long otherUserId = session.getUserOneId().equals(userId) ? session.getUserTwoId() : session.getUserOneId();
            resp.setOtherUserId(otherUserId);
            
            // 获取对方用户信息
            User otherUser = userService.getById(otherUserId);
            if (otherUser != null) {
                // 优先显示昵称，如果没有昵称则显示用户名
                String displayName = otherUser.getNickname();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = otherUser.getUsername();
                }
                resp.setOtherNickname(displayName);
                resp.setOtherAvatar(otherUser.getAvatar());
            }
            
            // 设置我的未读数
            resp.setUnreadCount(session.getUserOneId().equals(userId) ? session.getUnreadCountOne() : session.getUnreadCountTwo());
            
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateLastMessage(Long sessionId, String content, Long senderId) {
        ChatSession session = this.getById(sessionId);
        if (session != null) {
            session.setLastMessage(content);
            session.setLastMessageTime(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            
            // 增加接收者的未读数
            if (session.getUserOneId().equals(senderId)) {
                session.setUnreadCountTwo(session.getUnreadCountTwo() + 1);
            } else {
                session.setUnreadCountOne(session.getUnreadCountOne() + 1);
            }
            this.updateById(session);
        }
    }

    @Override
    public void resetUnreadCount(Long sessionId, Long userId) {
        ChatSession session = this.getById(sessionId);
        if (session != null) {
            if (session.getUserOneId().equals(userId)) {
                session.setUnreadCountOne(0);
            } else if (session.getUserTwoId().equals(userId)) {
                session.setUnreadCountTwo(0);
            }
            this.updateById(session);
        }
    }
}
