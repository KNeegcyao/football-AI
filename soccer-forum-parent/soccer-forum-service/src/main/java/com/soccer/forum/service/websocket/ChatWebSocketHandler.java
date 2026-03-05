package com.soccer.forum.service.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccer.forum.domain.entity.ChatMessage;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.service.ChatMessageService;
import com.soccer.forum.service.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 在线用户会话管理 Key: userId, Value: session
    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Autowired
    private ChatMessageService messageService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket 尝试连接, session: {}, query: {}", session.getId(), session.getUri().getQuery());
        Long userId = getUserId(session);
        if (userId != null) {
            SESSIONS.put(userId, session);
            // 存储在线状态到 Redis
            redisTemplate.opsForValue().set("online:" + userId, session.getId());
            log.info("用户 {} 连接成功, 当前在线人数: {}", userId, SESSIONS.size());
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("收到消息: {}", message.getPayload());
        Long senderId = getUserId(session);
        if (senderId == null) return;

        // 解析消息
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        String content = payload.get("content").toString();
        Integer type = (Integer) payload.getOrDefault("type", 0);

        // 保存消息到数据库
        ChatMessage chatMessage = messageService.sendMessage(senderId, receiverId, content, type);

        // 推送给接收者
        WebSocketSession receiverSession = SESSIONS.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
        } else {
            log.info("接收者 {} 不在线，消息已存库", receiverId);
        }
        
        // 推送回执给发送者 (包含消息ID和时间)
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            SESSIONS.remove(userId);
            redisTemplate.delete("online:" + userId);
            log.info("用户 {} 断开连接, 当前在线人数: {}", userId, SESSIONS.size());
        }
    }

    private Long getUserId(WebSocketSession session) {
        // 从 query string 获取 token
        String query = session.getUri().getQuery();
        if (query != null && query.contains("token=")) {
            String token = query.split("token=")[1].split("&")[0];
            try {
                return jwtUtils.getUserIdFromToken(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
