package com.zhly.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.UserOperationLog;
import com.zhly.mapper.UserOperationLogMapper;
import com.zhly.service.UserOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户操作日志服务实现类
 */
@Service
public class UserOperationLogServiceImpl extends ServiceImpl<UserOperationLogMapper, UserOperationLog> implements UserOperationLogService {
    
    @Autowired(required = false)
    private UserOperationLogMapper userOperationLogMapper;
    
    @Override
    public void recordOperationLog(Long userId,
                                   String operationType,
                                   String operationModule,
                                   String operationContent,
                                   String operationIp,
                                   boolean success,
                                   String remark) {
        try {
            if (userOperationLogMapper == null) {
                return;
            }
            
            UserOperationLog log = new UserOperationLog();
            log.setUserId(userId);
            log.setOperationType(operationType);
            log.setOperationModule(operationModule);
            log.setOperationContent(operationContent);
            log.setOperationIp(operationIp);
            log.setOperationTime(LocalDateTime.now());
            log.setStatus(success ? 1 : 0);
            log.setRemark(remark);
            log.setCreateTime(LocalDateTime.now());
            
            userOperationLogMapper.insert(log);
        } catch (Exception e) {
            // 操作日志记录失败不影响主流程
            System.err.println("记录操作日志失败: " + e.getMessage());
        }
    }
}












































































