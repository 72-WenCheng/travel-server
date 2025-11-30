package com.zhly.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 处理登录失败计数和锁定逻辑，支持Redis和本地内存两种存储方式
 */
@Service
public class LoginSecurityService {

    private static final String FAIL_KEY_PREFIX = "security:login:fail:";
    private static final String LOCK_KEY_PREFIX = "security:login:lock:";
    private static final Logger log = LoggerFactory.getLogger(LoginSecurityService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecurityPolicyService securityPolicyService;
    private final Map<String, Counter> localFailures = new ConcurrentHashMap<>();
    private final Map<String, Long> localLocks = new ConcurrentHashMap<>();

    @Autowired
    public LoginSecurityService(SecurityPolicyService securityPolicyService,
                                @Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.securityPolicyService = securityPolicyService;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        resetAllFailures();
    }

    /**
     * 检查账号是否处于锁定状态
     */
    public boolean isLocked(Long userId, String username) {
        SecurityPolicy policy = securityPolicyService.getPolicy();
        if (!policy.isLoginLockEnabled()) {
            return false;
        }
        String key = buildKey(LOCK_KEY_PREFIX, userId, username);
        Long lockedUntil = getLongValue(key);
        if (lockedUntil == null && redisTemplate == null) {
            lockedUntil = localLocks.get(key);
        }
        return lockedUntil != null && lockedUntil > System.currentTimeMillis();
    }

    /**
     * 记录一次登录失败，返回当前失败次数
     */
    public int recordFailedAttempt(Long userId, String username) {
        SecurityPolicy policy = securityPolicyService.getPolicy();
        if (!policy.isLoginLockEnabled()) {
            return 0;
        }
        String key = buildKey(FAIL_KEY_PREFIX, userId, username);
        int attempts = incrementCounter(key, Duration.ofMinutes(policy.getLockTimeMinutes()));
        if (attempts >= policy.getMaxLoginAttempts()) {
            lockUser(userId, username, policy.getLockTimeMinutes());
            clearFailures(userId, username, false);
        }
        return attempts;
    }

    /**
     * 清理失败记录和锁定状态
     */
    public void clearFailures(Long userId, String username) {
        clearFailures(userId, username, true);
    }

    private void clearFailures(Long userId, String username, boolean clearLock) {
        String failKey = buildKey(FAIL_KEY_PREFIX, userId, username);
        String lockKey = buildKey(LOCK_KEY_PREFIX, userId, username);
        if (redisTemplate != null) {
            redisTemplate.delete(failKey);
            if (clearLock) {
                redisTemplate.delete(lockKey);
            }
        } else {
            localFailures.remove(failKey);
            if (clearLock) {
                localLocks.remove(lockKey);
            }
        }
    }

    /**
     * 重置所有失败记录（用于重启或配置变更后）
     */
    public void resetAllFailures() {
        localFailures.clear();
        localLocks.clear();
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(redisTemplate.keys(FAIL_KEY_PREFIX + "*"));
                redisTemplate.delete(redisTemplate.keys(LOCK_KEY_PREFIX + "*"));
            } catch (Exception e) {
                log.warn("清理登录失败记录时发生异常: {}", e.getMessage());
            }
        }
    }

    private void lockUser(Long userId, String username, int lockMinutes) {
        String key = buildKey(LOCK_KEY_PREFIX, userId, username);
        long lockedUntil = System.currentTimeMillis() + lockMinutes * 60_000L;
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, lockedUntil, Duration.ofMinutes(lockMinutes));
        } else {
            localLocks.put(key, lockedUntil);
        }
    }

    private int incrementCounter(String key, Duration ttl) {
        if (redisTemplate != null) {
            Long value = redisTemplate.opsForValue().increment(key);
            if (value != null && value == 1L) {
                redisTemplate.expire(key, ttl);
            }
            return value != null ? value.intValue() : 1;
        }
        Counter counter = localFailures.computeIfAbsent(key, k -> new Counter());
        synchronized (counter) {
            long now = System.currentTimeMillis();
            if (now - counter.windowStart > ttl.toMillis()) {
                counter.windowStart = now;
                counter.counter.set(0);
            }
            return counter.counter.incrementAndGet();
        }
    }

    private Long getLongValue(String key) {
        if (redisTemplate == null) {
            return null;
        }
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String buildKey(String prefix, Long userId, String username) {
        if (userId != null) {
            return prefix + "uid:" + userId;
        }
        return prefix + "name:" + (username != null ? username.toLowerCase() : "unknown");
    }

    private static final class Counter {
        private long windowStart = System.currentTimeMillis();
        private final AtomicInteger counter = new AtomicInteger(0);
    }
}

