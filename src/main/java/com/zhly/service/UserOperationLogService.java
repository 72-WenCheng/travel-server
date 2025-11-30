package com.zhly.service;

/**
 * 用户操作日志服务接口
 */
public interface UserOperationLogService {
    
    /**
     * 记录操作日志
     */
    void recordOperationLog(Long userId,
                            String operationType,
                            String operationModule,
                            String operationContent,
                            String operationIp,
                            boolean success,
                            String remark);
}

