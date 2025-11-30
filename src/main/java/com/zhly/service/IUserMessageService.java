package com.zhly.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhly.dto.UserConversationSummary;
import com.zhly.entity.UserMessage;

import java.util.List;

/**
 * 用户私信消息Service接口
 *
 * @author zhly
 * @since 2025-11-19
 */
public interface IUserMessageService extends IService<UserMessage> {

    /**
     * 发送私信
     */
    UserMessage sendMessage(Long senderId, Long receiverId, String content);

    /**
     * 获取指定用户的会话列表
     */
    List<UserConversationSummary> getConversationList(Long userId);

    /**
     * 获取与指定用户的聊天记录
     */
    Page<UserMessage> getChatHistory(Long userId, Long targetUserId, Integer page, Integer size);

    /**
     * 将会话标记为已读
     */
    void markConversationAsRead(Long userId, Long targetUserId);

    /**
     * 获取该用户的未读私信数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 撤回消息（2分钟内）
     */
    boolean recallMessage(Long messageId, Long userId);
}

