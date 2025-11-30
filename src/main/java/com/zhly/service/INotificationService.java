package com.zhly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhly.entity.Notification;

/**
 * 通知Service接口
 * 
 * @author zhly
 * @since 2025-10-24
 */
public interface INotificationService extends IService<Notification> {
    
    /**
     * 发送通知
     */
    void sendNotification(Long userId, Integer notifyType, String title, String content, 
                         Long senderId, String relatedType, Long relatedId, String linkUrl);
    
    /**
     * 标记为已读
     */
    void markAsRead(Long notificationId);
    
    /**
     * 标记全部已读
     */
    void markAllAsRead(Long userId);
    
    /**
     * 获取未读数量
     */
    Integer getUnreadCount(Long userId);
    
    /**
     * 发送系统通知（群发）
     */
    void sendSystemNotification(String title, String content);
}







