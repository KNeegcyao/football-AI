package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soccer.forum.domain.entity.ChatMessage;
import com.soccer.forum.domain.entity.ChatSession;
import com.soccer.forum.service.mapper.ChatMessageMapper;
import com.soccer.forum.service.mapper.ChatSessionMapper;
import com.soccer.forum.service.model.dto.ChatSessionResp;
import com.soccer.forum.service.service.ChatSessionService;
import com.soccer.forum.service.service.UserRelationshipService;
import com.soccer.forum.service.service.UserService;
import com.soccer.forum.service.service.impl.ChatMessageServiceImpl;
import com.soccer.forum.service.service.impl.ChatSessionServiceImpl;
import com.soccer.forum.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatSessionService sessionService;

    @Mock
    private ChatMessageMapper messageMapper;

    @Mock
    private ChatSessionMapper sessionMapper;

    @Mock
    private UserRelationshipService relationshipService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ChatMessageServiceImpl messageService;

    @InjectMocks
    private ChatSessionServiceImpl sessionServiceImpl;

    private Long senderId = 1L;
    private Long receiverId = 2L;
    private Long sessionId = 10L;
    private ChatSession session;

    @BeforeEach
    void setUp() throws Exception {
        session = new ChatSession();
        session.setId(sessionId);
        session.setUserOneId(senderId);
        session.setUserTwoId(receiverId);

        // 手动注入 baseMapper，因为 ServiceImpl 需要它
        java.lang.reflect.Field messageBaseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        messageBaseMapperField.setAccessible(true);
        messageBaseMapperField.set(messageService, messageMapper);

        java.lang.reflect.Field sessionBaseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        sessionBaseMapperField.setAccessible(true);
        sessionBaseMapperField.set(sessionServiceImpl, sessionMapper);
    }

    @Test
    void sendMessage_WhenNotBlacklisted_ShouldSucceed() {
        // Arrange
        String content = "Hello";
        when(relationshipService.isBlacklisted(receiverId, senderId)).thenReturn(false);
        when(sessionService.getOrCreateSession(senderId, receiverId)).thenReturn(session);
        when(messageMapper.insert(any(ChatMessage.class))).thenReturn(1);

        // Act
        ChatMessage result = messageService.sendMessage(senderId, receiverId, content, 0);

        // Assert
        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(sessionId, result.getSessionId());
        verify(sessionService).updateLastMessage(eq(sessionId), eq(content), eq(senderId));
    }

    @Test
    void sendMessage_WhenBlacklisted_ShouldThrowException() {
        // Arrange
        String content = "Hello";
        when(relationshipService.isBlacklisted(receiverId, senderId)).thenReturn(true);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            messageService.sendMessage(senderId, receiverId, content, 0);
        });

        assertEquals("消息发送失败，您已被对方加入黑名单", exception.getMessage());
        verify(messageMapper, never()).insert(any(ChatMessage.class));
        verify(sessionService, never()).updateLastMessage(any(), any(), any());
    }

    @Test
    void setTop_ShouldUpdateSession() {
        // Arrange
        when(sessionMapper.selectById(sessionId)).thenReturn(session);
        when(sessionMapper.updateById(any(ChatSession.class))).thenReturn(1);

        // Act
        sessionServiceImpl.setTop(sessionId, senderId, true);

        // Assert
        assertEquals(1, session.getIsTopOne());
        verify(sessionMapper).updateById(session);
    }

    @Test
    void getUserSessions_ShouldBeSortedByTop() {
        // Arrange
        ChatSession s1 = new ChatSession();
        s1.setId(1L);
        s1.setUserOneId(senderId);
        s1.setUserTwoId(3L);
        s1.setIsTopOne(0);

        ChatSession s2 = new ChatSession();
        s2.setId(2L);
        s2.setUserOneId(senderId);
        s2.setUserTwoId(4L);
        s2.setIsTopOne(1); // 置顶

        when(sessionMapper.selectList(any())).thenReturn(List.of(s1, s2));

        // Act
        List<ChatSessionResp> results = sessionServiceImpl.getUserSessions(senderId);

        // Assert
        assertEquals(2, results.size());
        assertEquals(2L, results.get(0).getId()); // 置顶的在前面
        assertTrue(results.get(0).getIsTop());
        assertFalse(results.get(1).getIsTop());
    }
}
