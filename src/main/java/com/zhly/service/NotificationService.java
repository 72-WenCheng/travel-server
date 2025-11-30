package com.zhly.service;

import com.zhly.entity.Notification;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface NotificationService extends IService<Notification> {
    
    /**
     * 发送通知
     */
    boolean sendNotification(Long userId, String title, String content, Integer type);
    
    /**
     * 批量发送通知
     */
    boolean batchSendNotification(List<Long> userIds, String title, String content, Integer type);
    
    /**
     * 获取用户通知列表
     */
    Map<String, Object> getUserNotifications(Long userId, Integer type, Integer isRead, Integer page, Integer size);
    
    /**
     * 获取未读通知数量
     */
    Long getUnreadCount(Long userId);
    
    /**
     * 标记通知为已读
     */
    boolean markAsRead(Long userId, List<Long> notificationIds);
    
    /**
     * 标记所有通知为已读
     */
    boolean markAllAsRead(Long userId);
    
    /**
     * 删除通知
     */
    boolean deleteNotification(Long id);
    
    /**
     * 获取通知统计
     */
    Map<String, Object> getNotificationStatistics();
    
    /**
     * 清理过期通知
     */
    boolean cleanExpiredNotifications(Integer days);
}


















