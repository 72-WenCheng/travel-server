package com.zhly.service.dto;

/**
 * 地理IP信息DTO
 * 用于存储IP地址对应的地理位置信息
 * 
 * @author zhly
 */
public class GeoIpInfo {
    
    /**
     * 国家代码（ISO 3166-1 alpha-2）
     */
    private String countryCode;
    
    /**
     * 国家名称
     */
    private String countryName;
    
    /**
     * 省份/州名称
     */
    private String province;
    
    /**
     * 城市名称
     */
    private String city;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    public GeoIpInfo() {
    }
    
    public GeoIpInfo(String countryCode, String countryName) {
        this.countryCode = countryCode;
        this.countryName = countryName;
    }
    
    public GeoIpInfo(String countryCode, String countryName, String province, String city) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.province = province;
        this.city = city;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    public String getCountryName() {
        return countryName;
    }
    
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}


















