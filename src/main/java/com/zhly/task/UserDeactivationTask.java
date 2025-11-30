package com.zhly.task;

import com.zhly.service.UserDeactivationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定期处理超过宽限期仍未登录的注销申请
 */
@Component
public class UserDeactivationTask {

    private static final Logger logger = LoggerFactory.getLogger(UserDeactivationTask.class);

    @Autowired
    private UserDeactivationManager userDeactivationManager;

    /**
     * 每小时检查一次是否有到期的注销申请
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void finalizeExpiredRequests() {
        try {
            int processed = userDeactivationManager.finalizeExpiredRequests();
            if (processed > 0) {
                logger.info("已自动注销 {} 个超过7天未登录的账号", processed);
            }
        } catch (Exception e) {
            logger.error("处理注销申请失败", e);
        }
    }
}














