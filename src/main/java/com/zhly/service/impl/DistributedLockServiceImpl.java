package com.zhly.service.impl;

import com.zhly.service.DistributedLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class DistributedLockServiceImpl implements DistributedLockService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOCK_PREFIX = "lock:";
    private static final String LOCK_VALUE = "locked";
    private static final long DEFAULT_TIMEOUT = 30; // 默认30秒超时
    
    // Lua脚本：释放锁
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";
    
    // Lua脚本：续期锁
    private static final String RENEW_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('expire', KEYS[1], ARGV[2]) " +
        "else " +
        "    return 0 " +
        "end";
    
    @Override
    public boolean tryLock(String lockKey, long timeout, TimeUnit unit) {
        String fullKey = LOCK_PREFIX + lockKey;
        String lockValue = UUID.randomUUID().toString();
        
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(fullKey, lockValue, timeout, unit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            throw new RuntimeException("获取分布式锁失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean lock(String lockKey) {
        return tryLock(lockKey, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }
    
    @Override
    public boolean unlock(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;
        
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(UNLOCK_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(script, Collections.singletonList(fullKey), LOCK_VALUE);
            return result != null && result > 0;
        } catch (Exception e) {
            throw new RuntimeException("释放分布式锁失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isLocked(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;
        
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(fullKey));
        } catch (Exception e) {
            throw new RuntimeException("检查分布式锁失败: " + e.getMessage());
        }
    }
    
    @Override
    public long getLockTtl(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;
        
        try {
            Long ttl = redisTemplate.getExpire(fullKey, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            throw new RuntimeException("获取锁TTL失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean renewLock(String lockKey, long timeout, TimeUnit unit) {
        String fullKey = LOCK_PREFIX + lockKey;
        
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(RENEW_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(script, 
                Collections.singletonList(fullKey), 
                LOCK_VALUE, 
                String.valueOf(unit.toSeconds(timeout))
            );
            return result != null && result > 0;
        } catch (Exception e) {
            throw new RuntimeException("续期分布式锁失败: " + e.getMessage());
        }
    }
}


















