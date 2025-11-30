package com.zhly.task;

import com.zhly.service.CacheService;
import com.zhly.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Component
public class ScheduledTasks {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private AiService aiService;
    
    /**
     * 每小时清理过期缓存
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanExpiredCache() {
        try {
            logger.info("开始清理过期缓存...");
            // 这里可以实现清理过期缓存的逻辑
            logger.info("过期缓存清理完成");
        } catch (Exception e) {
            logger.error("清理过期缓存失败", e);
        }
    }
    
    /**
     * 每天凌晨2点清理过期通知
     * TODO: 需要在 INotificationService 中添加 cleanExpiredNotifications 方法后再启用
     */
    // @Scheduled(cron = "0 0 2 * * ?")
    // public void cleanExpiredNotifications() {
    //     try {
    //         logger.info("开始清理过期通知...");
    //         // notificationService.cleanExpiredNotifications(30);
    //         logger.info("过期通知清理完成");
    //     } catch (Exception e) {
    //         logger.error("清理过期通知失败", e);
    //     }
    // }
    
    /**
     * 每天凌晨3点清理AI日志
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanAiLogs() {
        try {
            logger.info("开始清理AI日志...");
            aiService.cleanExpiredLogs(30);
            logger.info("AI日志清理完成");
        } catch (Exception e) {
            logger.error("清理AI日志失败", e);
        }
    }
    
    /**
     * 每5分钟更新系统统计信息到缓存
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void updateSystemStats() {
        try {
            logger.debug("更新系统统计信息...");
            // 这里可以实现更新系统统计信息的逻辑
            cacheService.set("system:stats:last_update", System.currentTimeMillis());
        } catch (Exception e) {
            logger.error("更新系统统计信息失败", e);
        }
    }
    
    /**
     * 每天凌晨1点备份重要数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void backupImportantData() {
        try {
            logger.info("开始备份重要数据...");
            // 这里可以实现数据备份的逻辑
            logger.info("重要数据备份完成");
        } catch (Exception e) {
            logger.error("备份重要数据失败", e);
        }
    }
}












