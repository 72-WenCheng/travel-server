package com.zhly.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.Notification;
import com.zhly.mapper.NotificationMapper;
import com.zhly.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 通知Service实现
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Override
    @Transactional
    public void sendNotification(Long userId, Integer notifyType, String title, String content,
                                Long senderId, String relatedType, Long relatedId, String linkUrl) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(notifyType);  // 字段名是 type，不是 notifyType
        notification.setTitle(title);
        notification.setContent(content);
        notification.setSenderId(senderId);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setLinkUrl(linkUrl);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        
        notificationMapper.insert(notification);
    }
    
    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getIsRead() == 0) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }
    
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }
    
    @Override
    public Integer getUnreadCount(Long userId) {
        return notificationMapper.getUnreadCount(userId);
    }
    
    @Override
    @Transactional
    public void sendSystemNotification(String title, String content) {
        // TODO: 获取所有用户ID，批量发送
        // 这里简化处理，实际应该批量插入
    }
}
