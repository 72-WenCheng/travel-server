package com.zhly.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户私信WebSocket会话管理器
 *
 * @author zhly
 * @since 2025-11-19
 */
@Component
public class UserMessageSessionManager {

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public UserMessageSessionManager() {
        this.objectMapper = new ObjectMapper();
        // 注册Java 8时间类型支持模块
        this.objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳（使用ISO-8601格式）
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void registerSession(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removeSession(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    public void sendJsonMessage(Long userId, String type, Object data) {
        if (userId == null) {
            System.err.println("❌ 发送消息失败: userId为空");
            return;
        }
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            System.out.println("⚠️ 用户[" + userId + "]没有活跃的WebSocket连接，消息无法实时推送");
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", type,
                    "data", data
            ));
            TextMessage message = new TextMessage(payload);
            int sentCount = 0;
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                    sentCount++;
                    System.out.println("✅ 消息已发送给用户[" + userId + "], 会话数: " + sentCount);
                } else {
                    System.out.println("⚠️ 用户[" + userId + "]的会话已关闭，跳过");
                }
            }
            if (sentCount == 0) {
                System.out.println("⚠️ 用户[" + userId + "]的所有会话都已关闭");
            }
        } catch (JsonProcessingException e) {
            System.err.println("❌ 序列化WebSocket消息失败: " + e.getMessage());
            throw new RuntimeException("序列化WebSocket消息失败", e);
        } catch (IOException e) {
            System.err.println("❌ 发送WebSocket消息失败: " + e.getMessage());
            throw new RuntimeException("发送WebSocket消息失败", e);
        }
    }
}

