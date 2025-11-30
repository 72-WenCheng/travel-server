package com.zhly.service;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface DistributedLockService {
    
    /**
     * 尝试获取锁
     */
    boolean tryLock(String lockKey, long timeout, TimeUnit unit);
    
    /**
     * 获取锁
     */
    boolean lock(String lockKey);
    
    /**
     * 释放锁
     */
    boolean unlock(String lockKey);
    
    /**
     * 检查锁是否存在
     */
    boolean isLocked(String lockKey);
    
    /**
     * 获取锁的剩余时间
     */
    long getLockTtl(String lockKey);
    
    /**
     * 续期锁
     */
    boolean renewLock(String lockKey, long timeout, TimeUnit unit);
}


















