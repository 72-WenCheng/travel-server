package com.zhly.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户管理器（单机内存实现）
 * 用于统计在线用户并在禁止多端登录时做校验
 */
@Component
public class OnlineUserManager {

    private final ConcurrentHashMap<Long, SessionInfo> onlineUsers = new ConcurrentHashMap<>();

    /**
     * 标记用户在线
     *
     * @param userId 用户ID
     * @param role   用户角色
     */
    public void markOnline(Long userId, Integer role) {
        if (userId == null) {
            return;
        }
        onlineUsers.put(userId, new SessionInfo(role));
    }

    /**
     * 是否已有在线会话
     */
    public boolean hasActiveSession(Long userId) {
        if (userId == null) {
            return false;
        }
        return onlineUsers.containsKey(userId);
    }

    /**
     * 标记用户下线
     */
    public void markOffline(Long userId) {
        if (userId == null) {
            return;
        }
        onlineUsers.remove(userId);
    }

    /**
     * 获取在线普通用户数量
     */
    public long getOnlineUserCount() {
        return onlineUsers.values().stream()
                .filter(info -> info.role != null && info.role == 2)
                .count();
    }

    private static final class SessionInfo {
        private final Integer role;

        private SessionInfo(Integer role) {
            this.role = role;
        }
    }
}




