package com.zhly.service;

import com.zhly.service.dto.GeoIpInfo;

/**
 * 地理IP服务接口
 * 用于根据IP地址查询地理位置信息
 * 
 * @author zhly
 */
public interface GeoIpService {
    
    /**
     * 根据IP地址查询地理位置信息
     * 
     * @param ipAddress IP地址，可以为null或空字符串
     * @return GeoIpInfo对象，如果查询失败或IP无效则返回null
     */
    GeoIpInfo lookup(String ipAddress);
    
    /**
     * 判断IP地址是否是内网地址
     * 
     * @param ipAddress IP地址
     * @return 如果是内网地址返回true，否则返回false
     */
    boolean isPrivateIp(String ipAddress);
}


















