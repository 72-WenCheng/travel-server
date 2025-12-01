package com.zhly.service;

import com.zhly.service.dto.GeoIpInfo;

/**
 * IP 地理位置解析服务
 */
public interface GeoIpService {

    /**
     * 根据 IP 地址查询国家/地区信息
     *
     * @param ipAddress 客户端 IP
     * @return GeoIpInfo，若无法解析则返回 null
     */
    GeoIpInfo lookup(String ipAddress);
}

