package com.zhly.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.UserLoginHistory;
import com.zhly.mapper.UserLoginHistoryMapper;
import com.zhly.service.UserLoginHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户登录历史服务实现类
 */
@Service
public class UserLoginHistoryServiceImpl extends ServiceImpl<UserLoginHistoryMapper, UserLoginHistory> implements UserLoginHistoryService {
    
    @Autowired(required = false)
    private UserLoginHistoryMapper userLoginHistoryMapper;
    
    @Override
    public void recordLoginHistory(Long userId, String loginIp, String userAgent, Integer status, String failReason) {
        try {
            if (userLoginHistoryMapper == null) {
                return;
            }
            
            UserLoginHistory history = new UserLoginHistory();
            history.setUserId(userId);
            history.setLoginTime(LocalDateTime.now());
            history.setLoginIp(loginIp);
            history.setUserAgent(userAgent);
            history.setStatus(status != null ? status : 1);
            history.setFailReason(failReason);
            history.setCreateTime(LocalDateTime.now());
            
            // 可以根据IP获取地理位置（需要第三方服务）
            // history.setLoginLocation(getLocationByIp(loginIp));
            
            // 可以根据User-Agent解析设备信息
            // history.setLoginDevice(parseDevice(userAgent));
            
            userLoginHistoryMapper.insert(history);
        } catch (Exception e) {
            // 登录历史记录失败不影响登录流程
            System.err.println("记录登录历史失败: " + e.getMessage());
        }
    }
}












































































