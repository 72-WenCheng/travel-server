package com.zhly.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.R;
import com.zhly.dto.UserConversationSummary;
import com.zhly.entity.UserMessage;
import com.zhly.service.IUserMessageService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端-私信控制器
 *
 * @author zhly
 * @since 2025-11-19
 */
@RestController
@RequestMapping("/api/user/message")
public class UserMessageController {

    @Autowired
    private IUserMessageService userMessageService;

    /**
     * 发送私信
     */
    @PostMapping("/send")
    public R<UserMessage> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            UserMessage message = userMessageService.sendMessage(request.getSenderId(), request.getReceiverId(), request.getContent());
            return R.ok("发送成功", message);
        } catch (IllegalArgumentException e) {
            return R.badRequest(e.getMessage());
        } catch (Exception e) {
            return R.error("发送私信失败: " + e.getMessage());
        }
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public R<List<UserConversationSummary>> getConversations(@RequestParam Long userId) {
        try {
            List<UserConversationSummary> list = userMessageService.getConversationList(userId);
            return R.ok(list);
        } catch (Exception e) {
            return R.error("获取会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天记录
     */
    @GetMapping("/history")
    public R<Page<UserMessage>> getHistory(@RequestParam Long userId,
                                           @RequestParam Long targetUserId,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "20") Integer limit) {
        try {
            Page<UserMessage> history = userMessageService.getChatHistory(userId, targetUserId, page, limit);
            return R.ok(history);
        } catch (Exception e) {
            return R.error("获取聊天记录失败: " + e.getMessage());
        }
    }

    /**
     * 标记会话已读
     */
    @PostMapping("/mark-read")
    public R<String> markRead(@RequestBody MarkReadRequest request) {
        try {
            userMessageService.markConversationAsRead(request.getUserId(), request.getTargetUserId());
            return R.ok("已标记为已读");
        } catch (Exception e) {
            return R.error("标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread-count")
    public R<Map<String, Integer>> getUnreadCount(@RequestParam Long userId) {
        try {
            Integer count = userMessageService.getUnreadCount(userId);
            Map<String, Integer> result = new HashMap<>();
            result.put("unreadCount", count);
            return R.ok(result);
        } catch (Exception e) {
            return R.error("获取未读数量失败: " + e.getMessage());
        }
    }

    /**
     * 撤回消息（2分钟内）
     */
    @PostMapping("/recall")
    public R<String> recallMessage(@RequestBody RecallMessageRequest request) {
        try {
            boolean success = userMessageService.recallMessage(request.getMessageId(), request.getUserId());
            if (success) {
                return R.ok("消息已撤回");
            } else {
                return R.error("撤回失败");
            }
        } catch (IllegalArgumentException e) {
            return R.badRequest(e.getMessage());
        } catch (Exception e) {
            return R.error("撤回消息失败: " + e.getMessage());
        }
    }

    @Data
    private static class SendMessageRequest {
        private Long senderId;
        private Long receiverId;
        private String content;
    }

    @Data
    private static class MarkReadRequest {
        private Long userId;
        private Long targetUserId;
    }

    @Data
    private static class RecallMessageRequest {
        private Long messageId;
        private Long userId;
    }
}

