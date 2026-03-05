package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soccer.forum.domain.entity.ChatSession;
import com.soccer.forum.service.model.dto.ChatSessionResp;

import java.util.List;

public interface ChatSessionService extends IService<ChatSession> {
    /**
     * 获取或创建会话
     */
    ChatSession getOrCreateSession(Long userOneId, Long userTwoId);

    /**
     * 获取用户的所有会话
     */
    List<ChatSessionResp> getUserSessions(Long userId);

    /**
     * 更新会话最后一条消息
     */
    void updateLastMessage(Long sessionId, String content, Long senderId);

    /**
     * 重置未读数
     */
    void resetUnreadCount(Long sessionId, Long userId);
}
