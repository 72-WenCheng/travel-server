package com.zhly.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的限流服务，优先使用Redis，在无Redis时退化为单机内存
 */
@Service
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<String, LocalBucket> localBuckets = new ConcurrentHashMap<>();

    public RateLimitService(@Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 在给定窗口内尝试消耗一次令牌
     *
     * @param key    限流的唯一标识（例如IP）
     * @param limit  允许的最大次数
     * @param window 时间窗口
     * @return true表示允许，false表示需要限流
     */
    public boolean tryAcquire(String key, int limit, Duration window) {
        if (limit <= 0) {
            return false;
        }
        if (redisTemplate != null) {
            Long value = redisTemplate.opsForValue().increment(key);
            if (value != null && value == 1L) {
                redisTemplate.expire(key, window);
            }
            return value != null && value <= limit;
        }
        LocalBucket bucket = localBuckets.computeIfAbsent(key, k -> new LocalBucket());
        synchronized (bucket) {
            long now = System.currentTimeMillis();
            if (now - bucket.windowStart >= window.toMillis()) {
                bucket.windowStart = now;
                bucket.counter = 0;
            }
            bucket.counter++;
            return bucket.counter <= limit;
        }
    }

    private static final class LocalBucket {
        private long windowStart = System.currentTimeMillis();
        private int counter = 0;
    }
}

































