package com.zhly.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface CacheService {
    
    /**
     * 设置缓存
     */
    void set(String key, Object value);
    
    /**
     * 设置缓存（带过期时间）
     */
    void set(String key, Object value, long timeout, TimeUnit unit);
    
    /**
     * 获取缓存
     */
    Object get(String key);
    
    /**
     * 获取缓存（指定类型）
     */
    <T> T get(String key, Class<T> type);
    
    /**
     * 删除缓存
     */
    boolean delete(String key);
    
    /**
     * 批量删除缓存
     */
    boolean delete(List<String> keys);
    
    /**
     * 检查缓存是否存在
     */
    boolean hasKey(String key);
    
    /**
     * 设置过期时间
     */
    boolean expire(String key, long timeout, TimeUnit unit);
    
    /**
     * 获取过期时间
     */
    long getExpire(String key);
    
    /**
     * 获取匹配的key
     */
    Set<String> keys(String pattern);
    
    /**
     * 原子递增
     */
    long increment(String key, long delta);
    
    /**
     * 原子递减
     */
    long decrement(String key, long delta);
    
    /**
     * 设置Hash缓存
     */
    void hSet(String key, String field, Object value);
    
    /**
     * 获取Hash缓存
     */
    Object hGet(String key, String field);
    
    /**
     * 获取所有Hash缓存
     */
    Map<Object, Object> hGetAll(String key);
    
    /**
     * 删除Hash缓存
     */
    boolean hDelete(String key, String field);
    
    /**
     * 检查Hash缓存是否存在
     */
    boolean hHasKey(String key, String field);
}


















