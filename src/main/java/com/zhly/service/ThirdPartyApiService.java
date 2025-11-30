package com.zhly.service;

import java.util.Map;

/**
 * 第三方API服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface ThirdPartyApiService {
    
    /**
     * 获取天气信息
     */
    Map<String, Object> getWeatherInfo(String city);
    
    /**
     * 获取高德地图POI信息
     */
    Map<String, Object> getAmapPoi(String keyword, String city);
    
    /**
     * 获取高德地图路线规划
     */
    Map<String, Object> getAmapRoute(String origin, String destination, String strategy);
    
    /**
     * 获取12306火车票信息
     */
    Map<String, Object> getTrainTickets(String from, String to, String date);
    
    /**
     * 获取景点门票信息
     */
    Map<String, Object> getAttractionTickets(String attractionId);
    
    /**
     * 获取酒店信息
     */
    Map<String, Object> getHotels(String city, String checkIn, String checkOut);
    
    /**
     * 获取航班信息
     */
    Map<String, Object> getFlights(String from, String to, String date);
    
    /**
     * 地理编码：根据地址获取经纬度
     */
    Map<String, Object> geocode(String address);
    
    /**
     * 高德地图输入提示：根据关键词获取地址建议
     */
    java.util.List<Map<String, Object>> getAmapInputTips(String keywords, String city, Boolean citylimit);
    
    /**
     * 逆地理编码：根据经纬度获取地址和城市信息
     */
    Map<String, Object> reverseGeocode(double longitude, double latitude);
    
    /**
     * 根据IP地址获取位置信息（备用方案，不依赖浏览器定位）
     */
    Map<String, Object> getLocationByIp(String ipAddress);
}






