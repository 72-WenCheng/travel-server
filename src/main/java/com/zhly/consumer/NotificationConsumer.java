package com.zhly.consumer;

import com.zhly.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通知消息消费者
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @RabbitListener(queues = "travel.notification.queue")
    public void handleNotificationMessage(Map<String, Object> message) {
        try {
            logger.info("收到通知消息: {}", message);
            
            Long userId = Long.valueOf(message.get("userId").toString());
            String title = (String) message.get("title");
            String content = (String) message.get("content");
            Integer type = (Integer) message.get("type");
            
            // 发送通知
            notificationService.sendNotification(userId, title, content, type);
            
            logger.info("通知消息处理完成: userId={}", userId);
        } catch (Exception e) {
            logger.error("处理通知消息失败: {}", e.getMessage(), e);
        }
    }
}


