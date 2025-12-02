package com.zhly.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhly.config.ThirdPartyApiConfig;
import com.zhly.service.GeoIpService;
import com.zhly.service.dto.GeoIpInfo;
import com.zhly.util.IpAddressUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 地理IP服务实现类
 * 使用高德地图API进行IP定位
 * 
 * @author zhly
 */
@Service
public class GeoIpServiceImpl implements GeoIpService {
    
    @Autowired(required = false)
    private ThirdPartyApiConfig apiConfig;
    
    @Autowired(required = false)
    @Qualifier("thirdPartyRestTemplate")
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public GeoIpInfo lookup(String ipAddress) {
        // 如果IP地址为空或者是内网IP，返回null
        if (ipAddress == null || ipAddress.isEmpty() || isPrivateIp(ipAddress)) {
            return null;
        }
        
        // 如果没有配置API，返回基本的地理信息（仅国家代码）
        if (apiConfig == null || restTemplate == null) {
            // 简单判断：如果是中国IP段，返回中国信息
            // 这里可以扩展更精确的判断逻辑
            GeoIpInfo info = new GeoIpInfo();
            info.setCountryCode("CN");
            info.setCountryName("中国");
            info.setIpAddress(ipAddress);
            return info;
        }
        
        try {
            // 使用高德地图IP定位API
            String url = String.format("%s/v3/ip?key=%s&ip=%s&output=json",
                apiConfig.getAmapApiUrl(), apiConfig.getAmapApiKey(), ipAddress);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String status = jsonNode.get("status").asText();
                
                if ("1".equals(status)) {
                    GeoIpInfo info = new GeoIpInfo();
                    info.setIpAddress(ipAddress);
                    
                    if (jsonNode.has("country")) {
                        info.setCountryName(jsonNode.get("country").asText());
                    }
                    if (jsonNode.has("province")) {
                        info.setProvince(jsonNode.get("province").asText());
                    }
                    if (jsonNode.has("city")) {
                        info.setCity(jsonNode.get("city").asText());
                    }
                    if (jsonNode.has("rectangle")) {
                        // rectangle格式：左下角经度,左下角纬度;右上角经度,右上角纬度
                        String rectangle = jsonNode.get("rectangle").asText();
                        if (rectangle != null && rectangle.contains(";")) {
                            String[] parts = rectangle.split(";");
                            if (parts.length == 2) {
                                String[] bottomLeft = parts[0].split(",");
                                String[] topRight = parts[1].split(",");
                                if (bottomLeft.length >= 2 && topRight.length >= 2) {
                                    double lon1 = Double.parseDouble(bottomLeft[0]);
                                    double lat1 = Double.parseDouble(bottomLeft[1]);
                                    double lon2 = Double.parseDouble(topRight[0]);
                                    double lat2 = Double.parseDouble(topRight[1]);
                                    // 取中心点
                                    info.setLongitude((lon1 + lon2) / 2);
                                    info.setLatitude((lat1 + lat2) / 2);
                                }
                            }
                        }
                    }
                    
                    // 设置国家代码（可以根据国家名称映射）
                    if (info.getCountryName() != null) {
                        info.setCountryCode(mapCountryNameToCode(info.getCountryName()));
                    }
                    
                    return info;
                }
            }
        } catch (Exception e) {
            // 如果API调用失败，返回null或基本信息
            System.err.println("IP定位失败: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public boolean isPrivateIp(String ipAddress) {
        return IpAddressUtils.isPrivateIp(ipAddress);
    }
    
    /**
     * 将国家名称映射为国家代码
     * 这里只处理常见国家，可以根据需要扩展
     */
    private String mapCountryNameToCode(String countryName) {
        if (countryName == null) {
            return null;
        }
        
        // 常见国家映射
        switch (countryName) {
            case "中国":
                return "CN";
            case "美国":
                return "US";
            case "日本":
                return "JP";
            case "韩国":
                return "KR";
            case "英国":
                return "GB";
            case "法国":
                return "FR";
            case "德国":
                return "DE";
            case "俄罗斯":
                return "RU";
            case "印度":
                return "IN";
            case "澳大利亚":
                return "AU";
            case "加拿大":
                return "CA";
            default:
                // 如果无法映射，返回前两个字符的大写（简单处理）
                return countryName.length() >= 2 ? countryName.substring(0, 2).toUpperCase() : "UN";
        }
    }
}

