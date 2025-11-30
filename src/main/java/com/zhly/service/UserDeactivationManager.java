package com.zhly.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhly.entity.UserDeactivateRequest;
import com.zhly.mapper.UserDeactivateRequestMapper;
import com.zhly.mapper.UserMapper;
import com.zhly.util.OnlineUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理用户注销申请的生命周期
 */
@Component
public class UserDeactivationManager {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_CANCELLED = 1;
    public static final int STATUS_COMPLETED = 2;
    private static final int GRACE_DAYS = 7;

    @Autowired
    private UserDeactivateRequestMapper userDeactivateRequestMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OnlineUserManager onlineUserManager;

    /**
     * 申请注销：记录待生效的注销请求（7天后生效）
     */
    public UserDeactivateRequest scheduleDeactivation(Long userId, String reason) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(GRACE_DAYS);

        UserDeactivateRequest request = findPendingRequest(userId);
        if (request == null) {
            request = new UserDeactivateRequest();
            request.setUserId(userId);
            request.setStatus(STATUS_PENDING);
            request.setReason(reason);
            request.setRequestTime(now);
            request.setDeadlineTime(deadline);
            request.setCreateTime(now);
            request.setUpdateTime(now);
            userDeactivateRequestMapper.insert(request);
        } else {
            request.setReason(reason);
            request.setRequestTime(now);
            request.setDeadlineTime(deadline);
            request.setStatus(STATUS_PENDING);
            request.setCancelTime(null);
            request.setUpdateTime(now);
            userDeactivateRequestMapper.updateById(request);
        }

        return request;
    }

    /**
     * 获取当前仍处于待生效状态的注销申请
     */
    public UserDeactivateRequest getPendingRequest(Long userId) {
        return findPendingRequest(userId);
    }

    /**
     * 若申请已超过宽限期，则立即生效
     *
     * @return true 表示已完成注销
     */
    public boolean finalizeIfExpired(UserDeactivateRequest request) {
        if (request == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (request.getDeadlineTime() != null && !now.isBefore(request.getDeadlineTime())) {
            finalizeRequest(request);
            return true;
        }
        return false;
    }

    /**
     * 在用户成功登录后，取消未到期的注销申请
     */
    public void cancelPendingRequest(UserDeactivateRequest request) {
        if (request == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        request.setStatus(STATUS_CANCELLED);
        request.setCancelTime(now);
        request.setUpdateTime(now);
        userDeactivateRequestMapper.updateById(request);
    }

    /**
     * 定时任务：处理宽限期已结束的注销申请
     *
     * @return 本次处理的数量
     */
    public int finalizeExpiredRequests() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<UserDeactivateRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("status", STATUS_PENDING)
                .le("deadline_time", now);
        List<UserDeactivateRequest> expiredRequests = userDeactivateRequestMapper.selectList(wrapper);
        if (expiredRequests == null || expiredRequests.isEmpty()) {
            return 0;
        }

        for (UserDeactivateRequest request : expiredRequests) {
            finalizeRequest(request);
        }
        return expiredRequests.size();
    }

    private UserDeactivateRequest findPendingRequest(Long userId) {
        QueryWrapper<UserDeactivateRequest> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("status", STATUS_PENDING)
                .orderByDesc("request_time")
                .last("LIMIT 1");
        return userDeactivateRequestMapper.selectOne(wrapper);
    }

    private void finalizeRequest(UserDeactivateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        request.setStatus(STATUS_COMPLETED);
        request.setCompleteTime(now);
        request.setUpdateTime(now);
        userDeactivateRequestMapper.updateById(request);

        deleteUserData(request.getUserId());

        try {
            onlineUserManager.markOffline(request.getUserId());
        } catch (Exception e) {
            // 忽略标记失败
        }
    }

    private void deleteUserData(Long userId) {
        if (userId == null) {
            return;
        }
        userMapper.deleteById(userId);
    }
}


