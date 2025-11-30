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
 * 邮件消息消费者
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class EmailConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailConsumer.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @RabbitListener(queues = "travel.email.queue")
    public void handleEmailMessage(Map<String, Object> message) {
        try {
            logger.info("收到邮件消息: {}", message);
            
            String to = (String) message.get("to");
            String subject = (String) message.get("subject");
            String content = (String) message.get("content");
            
            // 这里可以集成邮件服务，如阿里云邮件推送、腾讯云邮件等
            // 目前先记录到通知系统
            sendEmailNotification(to, subject, content);
            
            logger.info("邮件消息处理完成: {}", to);
        } catch (Exception e) {
            logger.error("处理邮件消息失败: {}", e.getMessage(), e);
        }
    }
    
    private void sendEmailNotification(String to, String subject, String content) {
        try {
            // 这里可以实现实际的邮件发送逻辑
            // 目前先发送系统通知
            logger.info("发送邮件: {} - {} - {}", to, subject, content);
            
            // 可以在这里集成第三方邮件服务
            // 例如：阿里云邮件推送、腾讯云邮件、SendGrid等
            
        } catch (Exception e) {
            logger.error("发送邮件失败: {}", e.getMessage(), e);
        }
    }
}


