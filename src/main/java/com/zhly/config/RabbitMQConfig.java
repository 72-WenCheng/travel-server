package com.zhly.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQConfig {
    
    // 队列名称
    public static final String QUEUE_EMAIL = "travel.email.queue";
    public static final String QUEUE_NOTIFICATION = "travel.notification.queue";
    public static final String QUEUE_AI = "travel.ai.queue";
    public static final String QUEUE_LOG = "travel.log.queue";
    
    // 交换机名称
    public static final String EXCHANGE_TOPIC = "travel.topic.exchange";
    public static final String EXCHANGE_DIRECT = "travel.direct.exchange";
    
    // 路由键
    public static final String ROUTING_KEY_EMAIL = "travel.email";
    public static final String ROUTING_KEY_NOTIFICATION = "travel.notification";
    public static final String ROUTING_KEY_AI = "travel.ai";
    public static final String ROUTING_KEY_LOG = "travel.log";
    
    /**
     * 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    /**
     * 监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
    
    // ========== 交换机配置 ==========
    
    /**
     * Topic交换机
     */
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC, true, false);
    }
    
    /**
     * Direct交换机
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false);
    }
    
    // ========== 队列配置 ==========
    
    /**
     * 邮件队列
     */
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL).build();
    }
    
    /**
     * 通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION).build();
    }
    
    /**
     * AI队列
     */
    @Bean
    public Queue aiQueue() {
        return QueueBuilder.durable(QUEUE_AI).build();
    }
    
    /**
     * 日志队列
     */
    @Bean
    public Queue logQueue() {
        return QueueBuilder.durable(QUEUE_LOG).build();
    }
    
    // ========== 绑定配置 ==========
    
    /**
     * 邮件队列绑定
     */
    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(topicExchange())
                .with(ROUTING_KEY_EMAIL);
    }
    
    /**
     * 通知队列绑定
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(topicExchange())
                .with(ROUTING_KEY_NOTIFICATION);
    }
    
    /**
     * AI队列绑定
     */
    @Bean
    public Binding aiBinding() {
        return BindingBuilder.bind(aiQueue())
                .to(directExchange())
                .with(ROUTING_KEY_AI);
    }
    
    /**
     * 日志队列绑定
     */
    @Bean
    public Binding logBinding() {
        return BindingBuilder.bind(logQueue())
                .to(directExchange())
                .with(ROUTING_KEY_LOG);
    }
}


