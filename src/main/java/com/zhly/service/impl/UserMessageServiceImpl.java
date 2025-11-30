package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.dto.UserConversationSummary;
import com.zhly.entity.User;
import com.zhly.entity.UserMessage;
import com.zhly.mapper.UserMapper;
import com.zhly.mapper.UserMessageMapper;
import com.zhly.service.IUserMessageService;
import com.zhly.websocket.UserMessageSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户私信消息Service实现
 *
 * @author zhly
 * @since 2025-11-19
 */
@Service
public class UserMessageServiceImpl extends ServiceImpl<UserMessageMapper, UserMessage> implements IUserMessageService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserMessageSessionManager sessionManager;

    @Override
    public UserMessage sendMessage(Long senderId, Long receiverId, String content) {
        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("发送方和接收方不能为空");
        }
        if (Objects.equals(senderId, receiverId)) {
            throw new IllegalArgumentException("无法给自己发送私信");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        UserMessage message = new UserMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content.trim());
        message.setStatus(0);
        message.setConversationId(generateConversationId(senderId, receiverId));
        message.setCreateTime(now);
        message.setUpdateTime(now);
        this.save(message);
        pushNewMessage(message);
        return message;
    }

    @Override
    public List<UserConversationSummary> getConversationList(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<UserMessage> messages = this.lambdaQuery()
                .nested(wrapper -> wrapper.eq(UserMessage::getSenderId, userId)
                        .or().eq(UserMessage::getReceiverId, userId))
                .orderByDesc(UserMessage::getCreateTime)
                .last("limit 500")
                .list();

        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }

        LinkedHashMap<Long, UserConversationSummary> summaryMap = new LinkedHashMap<>();
        messages.forEach(message -> {
            Long targetUserId = Objects.equals(message.getSenderId(), userId) ? message.getReceiverId() : message.getSenderId();
            UserConversationSummary summary = summaryMap.computeIfAbsent(targetUserId, id -> {
                UserConversationSummary item = new UserConversationSummary();
                item.setTargetUserId(id);
                item.setConversationId(message.getConversationId());
                item.setLatestMessage(message.getContent());
                item.setLatestMessageTime(message.getCreateTime());
                item.setLatestSenderId(message.getSenderId());
                item.setUnreadCount(0);
                item.setMessageCount(0);
                return item;
            });

            if (Objects.equals(message.getReceiverId(), userId) && (message.getStatus() == null || message.getStatus() == 0)) {
                summary.setUnreadCount(summary.getUnreadCount() + 1);
            }
            summary.setMessageCount(summary.getMessageCount() + 1);
        });

        List<Long> targetIds = new ArrayList<>(summaryMap.keySet());
        if (!targetIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(targetIds);
            Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
            summaryMap.forEach((targetId, summary) -> {
                User targetUser = userMap.get(targetId);
                if (targetUser != null) {
                    String nickname = StringUtils.hasText(targetUser.getNickname()) ? targetUser.getNickname() : targetUser.getUsername();
                    summary.setTargetNickname(nickname);
                    summary.setTargetAvatar(targetUser.getAvatar());
                }
            });
        }

        return new ArrayList<>(summaryMap.values());
    }

    @Override
    public Page<UserMessage> getChatHistory(Long userId, Long targetUserId, Integer page, Integer size) {
        if (userId == null || targetUserId == null) {
            return new Page<>(page, size);
        }
        Page<UserMessage> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(query -> query
                .nested(nested -> nested.eq(UserMessage::getSenderId, userId).eq(UserMessage::getReceiverId, targetUserId))
                .or(nested -> nested.eq(UserMessage::getSenderId, targetUserId).eq(UserMessage::getReceiverId, userId))
        );
        wrapper.orderByDesc(UserMessage::getCreateTime);
        this.page(pageObj, wrapper);

        List<UserMessage> records = pageObj.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            Set<Long> userIds = records.stream()
                    .flatMap(record -> Stream.of(record.getSenderId(), record.getReceiverId()))
                    .collect(Collectors.toSet());
            List<User> userList = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));

            records.forEach(record -> {
                User sender = userMap.get(record.getSenderId());
                if (sender != null) {
                    record.setSenderNickname(StringUtils.hasText(sender.getNickname()) ? sender.getNickname() : sender.getUsername());
                    record.setSenderAvatar(sender.getAvatar());
                }
                User receiver = userMap.get(record.getReceiverId());
                if (receiver != null) {
                    record.setReceiverNickname(StringUtils.hasText(receiver.getNickname()) ? receiver.getNickname() : receiver.getUsername());
                    record.setReceiverAvatar(receiver.getAvatar());
                }
                record.setIsOutgoing(Objects.equals(record.getSenderId(), userId));
            });
        }

        return pageObj;
    }

    @Override
    public void markConversationAsRead(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            return;
        }
        boolean updated = this.lambdaUpdate()
                .eq(UserMessage::getSenderId, targetUserId)
                .eq(UserMessage::getReceiverId, userId)
                .eq(UserMessage::getStatus, 0)
                .set(UserMessage::getStatus, 1)
                .set(UserMessage::getReadTime, LocalDateTime.now())
                .update();
        if (updated) {
            notifyConversationRead(userId, targetUserId);
        }
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        Long count = this.lambdaQuery()
                .eq(UserMessage::getReceiverId, userId)
                .eq(UserMessage::getStatus, 0)
                .count();
        if (count == null) {
            return 0;
        }
        return Math.toIntExact(count);
    }

    private String generateConversationId(Long senderId, Long receiverId) {
        long first = Math.min(senderId, receiverId);
        long second = Math.max(senderId, receiverId);
        return first + "_" + second;
    }

    private void pushNewMessage(UserMessage message) {
        try {
            System.out.println("📤 开始推送私信消息: 发送者=" + message.getSenderId() + ", 接收者=" + message.getReceiverId());
            User sender = userMapper.selectById(message.getSenderId());
            User receiver = userMapper.selectById(message.getReceiverId());
            if (sender == null || receiver == null) {
                System.err.println("❌ 推送失败: 用户不存在 (发送者=" + message.getSenderId() + ", 接收者=" + message.getReceiverId() + ")");
                return;
            }

            // 推送给发送者
            Map<String, Object> senderPayload = buildMessagePayload(message, sender, receiver, true);
            Map<String, Object> senderConversation = buildConversationPayload(sender.getId(), receiver, message);
            System.out.println("📨 推送给发送者[" + sender.getId() + "]: " + message.getContent());
            sessionManager.sendJsonMessage(sender.getId(), "NEW_MESSAGE", Map.of(
                    "message", senderPayload,
                    "conversation", senderConversation
            ));

            // 推送给接收者
            Map<String, Object> receiverPayload = buildMessagePayload(message, sender, receiver, false);
            Map<String, Object> receiverConversation = buildConversationPayload(receiver.getId(), sender, message);
            System.out.println("📨 推送给接收者[" + receiver.getId() + "]: " + message.getContent());
            sessionManager.sendJsonMessage(receiver.getId(), "NEW_MESSAGE", Map.of(
                    "message", receiverPayload,
                    "conversation", receiverConversation
            ));
            System.out.println("✅ 消息推送完成");
        } catch (Exception e) {
            System.err.println("❌ 推送私信消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, Object> buildMessagePayload(UserMessage message, User sender, User receiver, boolean isOutgoing) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", message.getId());
        payload.put("senderId", message.getSenderId());
        payload.put("receiverId", message.getReceiverId());
        payload.put("content", message.getContent());
        payload.put("status", message.getStatus());
        payload.put("readTime", message.getReadTime());
        payload.put("createTime", message.getCreateTime());
        payload.put("updateTime", message.getUpdateTime());
        payload.put("senderNickname", StringUtils.hasText(sender.getNickname()) ? sender.getNickname() : sender.getUsername());
        payload.put("senderAvatar", sender.getAvatar());
        payload.put("receiverNickname", StringUtils.hasText(receiver.getNickname()) ? receiver.getNickname() : receiver.getUsername());
        payload.put("receiverAvatar", receiver.getAvatar());
        payload.put("isOutgoing", isOutgoing);
        return payload;
    }

    private Map<String, Object> buildConversationPayload(Long viewerId, User targetUser, UserMessage latestMessage) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("conversationId", latestMessage.getConversationId());
        payload.put("targetUserId", targetUser.getId());
        payload.put("targetNickname", StringUtils.hasText(targetUser.getNickname()) ? targetUser.getNickname() : targetUser.getUsername());
        payload.put("targetAvatar", targetUser.getAvatar());
        payload.put("latestMessage", latestMessage.getContent());
        payload.put("latestMessageTime", latestMessage.getCreateTime());
        payload.put("latestSenderId", latestMessage.getSenderId());
        payload.put("unreadCount", calculateUnreadCount(viewerId, targetUser.getId()));
        payload.put("messageCount", 0);
        return payload;
    }

    private int calculateUnreadCount(Long receiverId, Long senderId) {
        Long count = this.lambdaQuery()
                .eq(UserMessage::getReceiverId, receiverId)
                .eq(UserMessage::getSenderId, senderId)
                .eq(UserMessage::getStatus, 0)
                .count();
        return count != null ? count.intValue() : 0;
    }

    private void notifyConversationRead(Long readerId, Long targetUserId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("readerId", readerId);
        payload.put("timestamp", LocalDateTime.now().toString());
        sessionManager.sendJsonMessage(targetUserId, "READ_UPDATE", payload);
    }

    @Override
    public boolean recallMessage(Long messageId, Long userId) {
        if (messageId == null || userId == null) {
            throw new IllegalArgumentException("消息ID和用户ID不能为空");
        }

        UserMessage message = this.getById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("消息不存在");
        }

        // 只能撤回自己发送的消息
        if (!Objects.equals(message.getSenderId(), userId)) {
            throw new IllegalArgumentException("只能撤回自己发送的消息");
        }

        // 检查是否在2分钟内
        LocalDateTime now = LocalDateTime.now();
        long minutesBetween = java.time.Duration.between(message.getCreateTime(), now).toMinutes();
        if (minutesBetween > 2) {
            throw new IllegalArgumentException("消息发送超过2分钟，无法撤回");
        }

        // 检查是否已撤回
        if (message.getContent() != null && message.getContent().equals("[已撤回]")) {
            throw new IllegalArgumentException("消息已被撤回");
        }

        // 标记为已撤回
        message.setContent("[已撤回]");
        message.setUpdateTime(now);
        boolean updated = this.updateById(message);

        if (updated) {
            // 通知对方消息已撤回
            notifyMessageRecalled(message);
        }

        return updated;
    }

    private void notifyMessageRecalled(UserMessage message) {
        try {
            // 通知接收者消息已撤回
            Map<String, Object> payload = new HashMap<>();
            payload.put("messageId", message.getId());
            payload.put("conversationId", message.getConversationId());
            payload.put("timestamp", LocalDateTime.now().toString());
            sessionManager.sendJsonMessage(message.getReceiverId(), "MESSAGE_RECALLED", payload);

            // 通知发送者消息已撤回（用于同步状态）
            sessionManager.sendJsonMessage(message.getSenderId(), "MESSAGE_RECALLED", payload);
        } catch (Exception e) {
            System.err.println("❌ 通知消息撤回失败: " + e.getMessage());
        }
    }
}

