package com.zhly.service;

import java.util.Map;

/**
 * 消息生产者服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface MessageProducerService {
    
    /**
     * 发送邮件消息
     */
    void sendEmailMessage(String to, String subject, String content);
    
    /**
     * 发送通知消息
     */
    void sendNotificationMessage(Long userId, String title, String content, Integer type);
    
    /**
     * 发送AI处理消息
     */
    void sendAiMessage(Long userId, String request, String modelName);
    
    /**
     * 发送日志消息
     */
    void sendLogMessage(String level, String message, Map<String, Object> context);
    
    /**
     * 发送延迟消息
     */
    void sendDelayedMessage(String queueName, Object message, long delayMillis);
}


















