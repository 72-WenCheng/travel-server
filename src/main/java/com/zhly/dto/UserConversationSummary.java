package com.zhly.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户私信会话摘要
 *
 * @author zhly
 * @since 2025-11-19
 */
@Data
public class UserConversationSummary {

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 对方用户ID
     */
    private Long targetUserId;

    /**
     * 对方昵称
     */
    private String targetNickname;

    /**
     * 对方头像
     */
    private String targetAvatar;

    /**
     * 最新一条消息内容
     */
    private String latestMessage;

    /**
     * 最新消息时间
     */
    private LocalDateTime latestMessageTime;

    /**
     * 最新消息发送者ID
     */
    private Long latestSenderId;

    /**
     * 未读数量
     */
    private Integer unreadCount = 0;

    /**
     * 总消息数量（用于排序展示）
     */
    private Integer messageCount = 0;
}

