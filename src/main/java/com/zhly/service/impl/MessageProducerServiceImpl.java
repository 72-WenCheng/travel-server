package com.zhly.service.impl;

import com.zhly.config.RabbitMQConfig;
import com.zhly.service.MessageProducerService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息生产者服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
@ConditionalOnBean(RabbitTemplate.class)
public class MessageProducerServiceImpl implements MessageProducerService {
    
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;
    
    @Override
    public void sendEmailMessage(String to, String subject, String content) {
        try {
            if (rabbitTemplate == null) {
                System.out.println("RabbitMQ不可用，跳过邮件消息发送: " + to + " - " + subject);
                return;
            }
            
            Map<String, Object> message = new HashMap<>();
            message.put("to", to);
            message.put("subject", subject);
            message.put("content", content);
            message.put("timestamp", LocalDateTime.now());
            message.put("type", "email");
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_TOPIC,
                RabbitMQConfig.ROUTING_KEY_EMAIL,
                message
            );
        } catch (Exception e) {
            throw new RuntimeException("发送邮件消息失败: " + e.getMessage());
        }
    }
    
    @Override
    public void sendNotificationMessage(Long userId, String title, String content, Integer type) {
        try {
            if (rabbitTemplate == null) {
                System.out.println("RabbitMQ不可用，跳过通知消息发送: " + userId + " - " + title);
                return;
            }
            
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("title", title);
            message.put("content", content);
            message.put("type", type);
            message.put("timestamp", LocalDateTime.now());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_TOPIC,
                RabbitMQConfig.ROUTING_KEY_NOTIFICATION,
                message
            );
        } catch (Exception e) {
            throw new RuntimeException("发送通知消息失败: " + e.getMessage());
        }
    }
    
    @Override
    public void sendAiMessage(Long userId, String request, String modelName) {
        try {
            if (rabbitTemplate == null) {
                System.out.println("RabbitMQ不可用，跳过AI消息发送: " + userId + " - " + request);
                return;
            }
            
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("request", request);
            message.put("modelName", modelName);
            message.put("timestamp", LocalDateTime.now());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.ROUTING_KEY_AI,
                message
            );
        } catch (Exception e) {
            throw new RuntimeException("发送AI消息失败: " + e.getMessage());
        }
    }
    
    @Override
    public void sendLogMessage(String level, String message, Map<String, Object> context) {
        try {
            if (rabbitTemplate == null) {
                System.out.println("RabbitMQ不可用，跳过日志消息发送: " + level + " - " + message);
                return;
            }
            
            Map<String, Object> logMessage = new HashMap<>();
            logMessage.put("level", level);
            logMessage.put("message", message);
            logMessage.put("context", context);
            logMessage.put("timestamp", LocalDateTime.now());
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.ROUTING_KEY_LOG,
                logMessage
            );
        } catch (Exception e) {
            throw new RuntimeException("发送日志消息失败: " + e.getMessage());
        }
    }
    
    @Override
    public void sendDelayedMessage(String queueName, Object message, long delayMillis) {
        try {
            if (rabbitTemplate == null) {
                System.out.println("RabbitMQ不可用，跳过延迟消息发送: " + queueName);
                return;
            }
            
            MessageProperties properties = new MessageProperties();
            properties.setDelay((int) delayMillis);
            
            Message msg = rabbitTemplate.getMessageConverter().toMessage(message, properties);
            rabbitTemplate.send(queueName, msg);
        } catch (Exception e) {
            throw new RuntimeException("发送延迟消息失败: " + e.getMessage());
        }
    }
}


