package com.zhly.service.impl;

import com.zhly.service.ThirdPartyApiService;
import com.zhly.config.ThirdPartyApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * 第三方API服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class ThirdPartyApiServiceImpl implements ThirdPartyApiService {
    
    @Autowired
    private ThirdPartyApiConfig apiConfig;
    
    @Autowired
    @Qualifier("thirdPartyRestTemplate")
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public Map<String, Object> getWeatherInfo(String city) {
        // 只使用高德天气API
        if (apiConfig.getAmapApiKey() == null || 
            apiConfig.getAmapApiKey().equals("your-amap-api-key") ||
            apiConfig.getAmapApiKey().equals("your-dev-amap-api-key") ||
            apiConfig.getAmapApiKey().isEmpty()) {
            System.err.println("❌ 高德API Key未配置！请在application.yml或环境变量中配置AMAP_API_KEY");
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "高德API Key未配置");
            errorResult.put("errorMessage", "请在application.yml或环境变量中配置AMAP_API_KEY");
            errorResult.put("success", false);
            return errorResult;
        }
        
        try {
            System.out.println("使用高德天气API获取城市天气: " + city);
            Map<String, Object> amapResult = getWeatherFromAmap(city);
            if (amapResult != null && !amapResult.isEmpty()) {
                System.out.println("✅ 高德天气API调用成功，返回数据");
                return amapResult;
            } else {
                System.err.println("❌ 高德天气API返回空数据，可能是API Key无效或城市名称不正确");
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "高德API返回空数据");
                errorResult.put("errorMessage", "请检查高德API Key是否有效，或城市名称是否正确");
                errorResult.put("success", false);
                return errorResult;
            }
        } catch (Exception e) {
            System.err.println("❌ 高德天气API调用失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "高德API调用异常");
            errorResult.put("errorMessage", e.getMessage());
            errorResult.put("success", false);
            return errorResult;
        }
    }
    
    /**
     * 使用高德天气API获取天气信息
     */
    private Map<String, Object> getWeatherFromAmap(String city) {
        try {
            // 高德天气API支持：
            // 1. 城市名称（如"北京"、"大连市"）
            // 2. 城市代码（adcode，如"110000"）
            // 注意：有些城市需要带"市"后缀，有些不需要
            
            // 第一步：处理可能的JSON数组格式（如 "[\"重庆\"]" 或 URL编码后的格式）
            String baseCityName = city;
            if (city != null) {
                String trimmedCity = city.trim();
                // 检查是否是JSON数组格式（可能包含转义字符）
                if (trimmedCity.startsWith("[") && trimmedCity.endsWith("]")) {
                    try {
                        // 使用ObjectMapper解析JSON数组（更可靠）
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(trimmedCity);
                        if (jsonNode.isArray() && jsonNode.size() > 0) {
                            baseCityName = jsonNode.get(0).asText();
                            System.out.println("✅ 解析JSON数组格式，提取城市名: " + baseCityName + " (原始: " + city + ")");
                        }
                    } catch (Exception e) {
                        // JSON解析失败，尝试手动提取
                        try {
                            String jsonArrayStr = trimmedCity;
                            // 移除方括号
                            jsonArrayStr = jsonArrayStr.substring(1, jsonArrayStr.length() - 1);
                            // 移除引号和转义字符
                            jsonArrayStr = jsonArrayStr.replaceAll("\\\\\"", "\"");
                            // 如果有多个城市，取第一个
                            if (jsonArrayStr.contains(",")) {
                                String[] cities = jsonArrayStr.split(",");
                                if (cities.length > 0) {
                                    baseCityName = cities[0].trim().replaceAll("^\"|\"$", "").replaceAll("\\\\", "");
                                }
                            } else {
                                baseCityName = jsonArrayStr.trim().replaceAll("^\"|\"$", "").replaceAll("\\\\", "");
                            }
                            System.out.println("✅ 手动解析JSON数组格式，提取城市名: " + baseCityName + " (原始: " + city + ")");
                        } catch (Exception e2) {
                            System.out.println("⚠️ 解析JSON数组格式失败，使用原始值: " + city);
                            // 最后的备用方案：直接提取
                            baseCityName = trimmedCity.replaceAll("^\\[|\\]$", "").replaceAll("\"", "").replaceAll("\\\\", "").trim();
                        }
                    }
                } else if (trimmedCity.contains("[\"") || trimmedCity.contains("\\\"")) {
                    // 可能是URL编码后的格式，尝试提取
                    try {
                        // 尝试提取引号内的内容
                        int startIdx = trimmedCity.indexOf("\"");
                        int endIdx = trimmedCity.lastIndexOf("\"");
                        if (startIdx >= 0 && endIdx > startIdx) {
                            baseCityName = trimmedCity.substring(startIdx + 1, endIdx);
                            System.out.println("✅ 从编码字符串中提取城市名: " + baseCityName + " (原始: " + city + ")");
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️ 提取城市名失败，使用原始值: " + city);
                    }
                }
            }
            
            // 第二步：解析城市和区县格式
            if (baseCityName.contains("·")) {
                // 如果是"北京·朝阳区"格式，提取城市名
                String[] parts = baseCityName.split("·");
                if (parts.length >= 2) {
                    baseCityName = parts[0]; // 如"北京"
                }
            } else if (baseCityName.endsWith("区") || baseCityName.endsWith("县")) {
                // 如果只是区县名，直接使用（可能失败）
                // baseCityName保持不变
            }
            
            // 优先使用adcode查询（最可靠的方式）
            // 常见的城市名称格式映射
            java.util.Map<String, String> cityAdcodeMap = new java.util.HashMap<>();
            cityAdcodeMap.put("北京", "110000");
            cityAdcodeMap.put("上海", "310000");
            cityAdcodeMap.put("广州", "440100");
            cityAdcodeMap.put("深圳", "440300");
            cityAdcodeMap.put("杭州", "330100");
            cityAdcodeMap.put("成都", "510100");
            cityAdcodeMap.put("重庆", "500000");
            cityAdcodeMap.put("重庆市", "500000");
            cityAdcodeMap.put("西安", "610100");
            cityAdcodeMap.put("武汉", "420100");
            cityAdcodeMap.put("南京", "320100");
            cityAdcodeMap.put("天津", "120000");
            cityAdcodeMap.put("大连", "210200");
            cityAdcodeMap.put("青岛", "370200");
            cityAdcodeMap.put("厦门", "350200");
            cityAdcodeMap.put("福州", "350100");
            cityAdcodeMap.put("昆明", "530100");
            cityAdcodeMap.put("南宁", "450100");
            cityAdcodeMap.put("南宁市", "450100");
            
            // 查找adcode（优先使用adcode，这是最可靠的方式）
            String adcode = cityAdcodeMap.get(baseCityName);
            if (adcode == null && baseCityName.endsWith("市")) {
                // 去掉"市"后缀再查找
                String cityWithoutSuffix = baseCityName.replace("市", "");
                adcode = cityAdcodeMap.get(cityWithoutSuffix);
            }
            if (adcode == null && !baseCityName.endsWith("市")) {
                // 加上"市"后缀再查找
                adcode = cityAdcodeMap.get(baseCityName + "市");
            }
            // 特殊处理：如果城市名包含"·"，提取城市名部分
            if (adcode == null && baseCityName.contains("·")) {
                String cityPart = baseCityName.split("·")[0];
                adcode = cityAdcodeMap.get(cityPart);
                if (adcode == null) {
                    adcode = cityAdcodeMap.get(cityPart + "市");
                }
            }
            
            // 如果找到了adcode，直接使用（跳过无效的城市名查询）
            if (adcode != null) {
                System.out.println("✅ 找到城市adcode，直接使用: " + adcode + " (城市: " + baseCityName + ")");
                String encodedCity = java.net.URLEncoder.encode(adcode, "UTF-8");
                String url = String.format("%s/v3/weather/weatherInfo?key=%s&city=%s&extensions=base", 
                    apiConfig.getAmapApiUrl(), apiConfig.getAmapApiKey(), encodedCity);
                
                System.out.println("使用高德天气API获取城市天气:" + baseCityName);
                System.out.println("请求URL: " + url.replace(apiConfig.getAmapApiKey(), "***"));
                
                // 检查API Key是否有效
                if (apiConfig.getAmapApiKey() == null || 
                    apiConfig.getAmapApiKey().equals("your-amap-api-key") ||
                    apiConfig.getAmapApiKey().equals("your-dev-amap-api-key") ||
                    apiConfig.getAmapApiKey().isEmpty()) {
                    System.err.println("❌ API Key未配置");
                    return null;
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "TravelApp/1.0");
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    String responseBody = response.getBody();
                    System.out.println("API响应: " + responseBody);
                    
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    
                    String status = jsonNode.has("status") ? jsonNode.get("status").asText() : "0";
                    String info = jsonNode.has("info") ? jsonNode.get("info").asText() : "";
                    String infocode = jsonNode.has("infocode") ? jsonNode.get("infocode").asText() : "";
                    String count = jsonNode.has("count") ? jsonNode.get("count").asText() : "0";
                    
                    System.out.println("响应解析: status=" + status + ", info=" + info + ", infocode=" + infocode + ", count=" + count);
                    
                    if ("1".equals(status)) {
                        JsonNode lives = jsonNode.has("lives") ? jsonNode.get("lives") : null;
                        if (lives != null && lives.isArray() && lives.size() > 0) {
                            System.out.println("✅ 成功！使用adcode: " + adcode + "，获取到 " + lives.size() + " 条数据");
                            
                            JsonNode live = lives.get(0);
                            Map<String, Object> result = new HashMap<>();
                            
                            String cityName = live.has("city") ? live.get("city").asText() : baseCityName;
                            if (cityName.endsWith("市")) {
                                cityName = cityName.substring(0, cityName.length() - 1);
                            }
                            result.put("city", cityName);
                            
                            result.put("temperature", Double.parseDouble(live.get("temperature").asText()));
                            result.put("description", live.has("weather") ? live.get("weather").asText() : "晴");
                            
                            String humidityStr = live.has("humidity") ? live.get("humidity").asText().replace("%", "").trim() : "60";
                            result.put("humidity", Integer.parseInt(humidityStr.isEmpty() ? "60" : humidityStr));
                            
                            double windSpeed = 3.5;
                            if (live.has("windpower")) {
                                windSpeed = parseWindPower(live.get("windpower").asText());
                            }
                            result.put("windSpeed", windSpeed);
                            
                            result.put("windDirection", live.has("winddir") ? live.get("winddir").asText() : "");
                            result.put("pressure", live.has("pressure") ? Double.parseDouble(live.get("pressure").asText()) : 1013);
                            result.put("visibility", live.has("visibility") ? Integer.parseInt(live.get("visibility").asText()) : 10000);
                            
                            result.put("timestamp", System.currentTimeMillis());
                            
                            String weather = live.has("weather") ? live.get("weather").asText() : "晴";
                            double temp = Double.parseDouble(live.get("temperature").asText());
                            String advice = generateWeatherAdviceFromAmap(weather, temp);
                            result.put("advice", advice);
                            
                            System.out.println("✅ 高德天气API数据解析成功");
                            return result;
                        }
                    }
                }
            }
            
            // 如果没有找到adcode，才尝试城市名格式（作为备用方案）
            System.out.println("⚠️ 未找到adcode映射，尝试使用城市名查询: " + baseCityName);
            java.util.List<String> cityFormats = new java.util.ArrayList<>();
            
            if (!baseCityName.endsWith("市")) {
                cityFormats.add(baseCityName);
                cityFormats.add(baseCityName + "市");
            } else {
                cityFormats.add(baseCityName);
                cityFormats.add(baseCityName.replace("市", ""));
            }
            
            // 依次尝试每种格式
            for (String cityParam : cityFormats) {
                System.out.println("========================================");
                System.out.println("尝试查询城市: " + cityParam);
                
                String encodedCity = java.net.URLEncoder.encode(cityParam, "UTF-8");
                String url = String.format("%s/v3/weather/weatherInfo?key=%s&city=%s&extensions=base", 
                    apiConfig.getAmapApiUrl(), apiConfig.getAmapApiKey(), encodedCity);
                
                System.out.println("请求URL: " + url.replace(apiConfig.getAmapApiKey(), "***"));
            
                // 检查API Key是否有效
                if (apiConfig.getAmapApiKey() == null || 
                    apiConfig.getAmapApiKey().equals("your-amap-api-key") ||
                    apiConfig.getAmapApiKey().equals("your-dev-amap-api-key") ||
                    apiConfig.getAmapApiKey().isEmpty()) {
                    continue; // 跳过无效Key
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "TravelApp/1.0");
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    String responseBody = response.getBody();
                    System.out.println("API响应: " + responseBody);
                    
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    
                    // 检查返回状态
                    String status = jsonNode.has("status") ? jsonNode.get("status").asText() : "0";
                    String info = jsonNode.has("info") ? jsonNode.get("info").asText() : "";
                    String infocode = jsonNode.has("infocode") ? jsonNode.get("infocode").asText() : "";
                    String count = jsonNode.has("count") ? jsonNode.get("count").asText() : "0";
                    
                    System.out.println("响应解析: status=" + status + ", info=" + info + ", infocode=" + infocode + ", count=" + count);
                    
                    if (!"1".equals(status)) {
                        // API错误，尝试下一个格式
                        System.out.println("⚠️ 该格式查询失败，尝试下一个格式...");
                        continue;
                    }
                    
                    // 检查lives数组
                    JsonNode lives = jsonNode.has("lives") ? jsonNode.get("lives") : null;
                    if (lives == null || !lives.isArray() || lives.size() == 0) {
                        System.out.println("⚠️ 该格式返回空数据（count=" + count + "），尝试下一个格式...");
                        continue; // 尝试下一个格式
                    }
                    
                    System.out.println("✅ 成功！使用格式: " + cityParam + "，获取到 " + lives.size() + " 条数据");
                
                    JsonNode live = lives.get(0);
                    Map<String, Object> result = new HashMap<>();
                    
                    // 提取城市信息（高德返回的可能包含市，需要处理）
                    String cityName = live.has("city") ? live.get("city").asText() : cityParam;
                    // 去掉"市"后缀，如"北京市" -> "北京"
                    if (cityName.endsWith("市")) {
                        cityName = cityName.substring(0, cityName.length() - 1);
                    }
                    result.put("city", cityName);
                
                    // 温度
                    result.put("temperature", Double.parseDouble(live.get("temperature").asText()));
                    
                    // 天气描述
                    result.put("description", live.has("weather") ? live.get("weather").asText() : "晴");
                    
                    // 湿度（高德返回的格式如"60%"，需要去掉%号）
                    String humidityStr = live.has("humidity") ? live.get("humidity").asText().replace("%", "").trim() : "60";
                    result.put("humidity", Integer.parseInt(humidityStr.isEmpty() ? "60" : humidityStr));
                    
                    // 风速（高德返回的是风力等级，如"3级"，需要转换为数值）
                    double windSpeed = 3.5;
                    if (live.has("windpower")) {
                        windSpeed = parseWindPower(live.get("windpower").asText());
                    }
                    result.put("windSpeed", windSpeed);
                    
                    // 风向
                    result.put("windDirection", live.has("winddir") ? live.get("winddir").asText() : "");
                    
                    // 气压（高德可能没有，使用默认值）
                    result.put("pressure", live.has("pressure") ? Double.parseDouble(live.get("pressure").asText()) : 1013);
                    
                    // 能见度（高德可能没有，使用默认值）
                    result.put("visibility", live.has("visibility") ? Integer.parseInt(live.get("visibility").asText()) : 10000);
                    
                    result.put("timestamp", System.currentTimeMillis());
                    
                    // 生成天气建议
                    String weather = live.has("weather") ? live.get("weather").asText() : "晴";
                    double temp = Double.parseDouble(live.get("temperature").asText());
                    String advice = generateWeatherAdviceFromAmap(weather, temp);
                    result.put("advice", advice);
                    
                    System.out.println("✅ 高德天气API数据解析成功");
                    return result;
                }
            }
            
            // 所有格式都失败了
            System.err.println("❌ 尝试了所有城市名称格式都失败: " + cityFormats.toString());
            System.err.println("   可能的原因：");
            System.err.println("   1. 该城市名称不在高德天气API支持列表中");
            System.err.println("   2. 需要使用adcode（行政区划代码）查询");
            System.err.println("   3. API Key的'天气查询API'服务配额已用完");
            System.err.println("   4. API Key未启用'天气查询API'服务");
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ 高德天气API调用异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 解析高德天气的风力等级
     * 高德返回的风力格式如 "3级" 或 "≤3级" 或 "3-4级"
     * 返回风力等级（整数），用于前端直接显示
     */
    private double parseWindPower(String windPower) {
        try {
            String cleaned = windPower.replace("≤", "")
                                      .replace("≥", "")
                                      .replace("级", "")
                                      .trim();
            
            // 处理范围格式如 "3-4" -> 取平均值或第一个值
            if (cleaned.contains("-")) {
                String[] parts = cleaned.split("-");
                if (parts.length == 2) {
                    double min = Double.parseDouble(parts[0].trim());
                    double max = Double.parseDouble(parts[1].trim());
                    // 返回平均值，但转为整数
                    return Math.round((min + max) / 2.0);
                }
            }
            
            // 直接解析为数字
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            System.err.println("解析风力等级失败: " + windPower + ", 错误: " + e.getMessage());
            return 3.0;
        }
    }
    
    /**
     * 根据高德天气数据生成建议
     */
    private String generateWeatherAdviceFromAmap(String weather, double temp) {
        // 优先处理特殊天气情况
        if (weather.contains("暴雨") || weather.contains("大暴雨")) {
            return "暴雨天气，建议减少出行，如需外出请带好雨具，注意安全哦";
        }
        
        if (weather.contains("雷") || weather.contains("雷电")) {
            return "雷雨天气，请避免户外活动，如在户外请寻找安全避雷场所";
        }
        
        if (weather.contains("雪")) {
            if (weather.contains("大雪") || weather.contains("暴雪")) {
                return "大雪纷飞，银装素裹很美，但出行要注意保暖防滑，慢行小心";
            }
            return "雪花飘落，浪漫唯美，记得添衣保暖，小心路滑";
        }
        
        if (weather.contains("雨")) {
            if (weather.contains("小雨")) {
                return "绵绵细雨，诗意朦胧，记得带把小伞，享受雨中漫步的浪漫";
            }
            if (weather.contains("中雨")) {
                return "中雨天气，出门记得带伞，小心地滑，注意安全";
            }
            return "雨天路滑，出行请带好雨具，注意交通安全";
        }
        
        if (weather.contains("雾") || weather.contains("霾")) {
            return "雾霾天气，空气质量不佳，建议减少户外活动，出门记得佩戴口罩";
        }
        
        if (weather.contains("沙") || weather.contains("尘")) {
            return "沙尘天气，空气质量较差，建议减少外出，如需出门请做好防护";
        }
        
        if (weather.contains("阴")) {
            return "阴云密布，虽然少了阳光，但也别有一番宁静之美，适合室内活动";
        }
        
        if (weather.contains("多云") || weather.contains("少云")) {
            if (temp > 28) {
                return "多云天气，温度较高，出门记得防晒和补水，享受舒适的云层遮阳";
            }
            if (temp < 15) {
                return "多云天气，温度适宜，微风轻拂，正是出游的好时光";
            }
            return "多云天气，云卷云舒，温度适中，适合各种户外活动";
        }
        
        // 根据温度处理晴天情况
        if (weather.contains("晴")) {
            if (temp >= 35) {
                return "烈日当空，天气炎热，注意防暑降温，多喝水，尽量避免正午外出";
            }
            if (temp > 30) {
                return "阳光明媚，温度较高，出门记得防晒，适当补水，享受夏日美好";
            }
            if (temp >= 25) {
                return "晴空万里，温度宜人，正是外出游玩的好天气，享受美好时光";
            }
            if (temp >= 20) {
                return "天气晴朗，温度舒适，微风和煦，适合各种户外活动，心情也会格外愉悦";
            }
            if (temp >= 15) {
                return "阳光正好，温度适中，不冷不热，正是出门散步的好时机";
            }
            if (temp >= 10) {
                return "晴空万里，略有凉意，记得适当添衣，享受清新的空气和温暖的阳光";
            }
            if (temp >= 5) {
                return "天气晴朗，但温度较低，注意保暖，享受冬日的暖阳";
            }
            return "阳光明媚，但天气较冷，记得多穿衣物，注意防寒保暖";
        }
        
        // 根据温度的一般性建议
        if (temp >= 35) {
            return "高温天气，请注意防暑，多补充水分，合理安排户外活动时间";
        }
        
        if (temp >= 30) {
            return "天气较热，出门记得做好防晒，多喝水，享受夏日的热情";
        }
        
        if (temp < 0) {
            return "严寒天气，记得添衣保暖，注意防寒，室内外温差大，小心感冒";
        }
        
        if (temp < 10) {
            return "天气较冷，记得多穿衣物，注意保暖，一杯热茶会让您更温暖";
        }
        
        // 默认提示
        return "天气宜人，温度适中，祝您有美好的一天，心情愉悦";
    }
    
    /**
     * 转换中文城市名为英文（用于OpenWeatherMap）
     */
    private String convertCityToEnglish(String city) {
        Map<String, String> cityMap = new HashMap<>();
        cityMap.put("北京", "Beijing");
        cityMap.put("上海", "Shanghai");
        cityMap.put("广州", "Guangzhou");
        cityMap.put("深圳", "Shenzhen");
        cityMap.put("成都", "Chengdu");
        cityMap.put("重庆", "Chongqing");
        cityMap.put("杭州", "Hangzhou");
        cityMap.put("南京", "Nanjing");
        cityMap.put("西安", "Xi'an");
        cityMap.put("武汉", "Wuhan");
        
        return cityMap.getOrDefault(city, city);
    }
    
    /**
     * 生成天气建议
     */
    private String generateWeatherAdvice(JsonNode weatherData) {
        double temp = weatherData.get("main").get("temp").asDouble();
        String description = weatherData.get("weather").get(0).get("description").asText();
        int humidity = weatherData.get("main").get("humidity").asInt();
        
        StringBuilder advice = new StringBuilder();
        
        if (temp > 30) {
            advice.append("天气炎热，建议多喝水，注意防晒。");
        } else if (temp < 10) {
            advice.append("天气较冷，注意保暖。");
        } else {
            advice.append("天气适宜，适合出行。");
        }
        
        if (humidity > 80) {
            advice.append("湿度较高，注意防潮。");
        }
        
        if (description.contains("雨")) {
            advice.append("有降雨，建议携带雨具。");
        } else if (description.contains("晴")) {
            advice.append("天气晴朗，适合户外活动。");
        }
        
        return advice.toString();
    }
    
    /**
     * 生成模拟天气数据
     */
    private Map<String, Object> generateMockWeatherData(String city) {
        Map<String, Object> result = new HashMap<>();
        result.put("city", city);
        result.put("temperature", 25.0);
        result.put("feelsLike", 27.0);
        result.put("description", "晴朗");
        result.put("humidity", 60);
        result.put("windSpeed", 3.5);
        result.put("pressure", 1013);
        result.put("visibility", 10000);
        result.put("timestamp", System.currentTimeMillis());
        result.put("advice", "天气适宜，适合出行。");
        return result;
    }
    
    @Override
    public Map<String, Object> getAmapPoi(String keyword, String city) {
        try {
            String url = String.format("%s/v3/place/text?key=%s&keywords=%s&city=%s&output=json&extensions=all", 
                apiConfig.getAmapApiUrl(), apiConfig.getAmapApiKey(), keyword, city);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                Map<String, Object> result = new HashMap<>();
                result.put("status", jsonNode.get("status").asText());
                result.put("count", jsonNode.get("count").asInt());
                result.put("pois", jsonNode.get("pois"));
                result.put("info", jsonNode.get("info").asText());
                
                // 处理POI数据，提取有用信息
                List<Map<String, Object>> processedPois = processPoiData(jsonNode.get("pois"));
                result.put("processedPois", processedPois);
                
                return result;
            }
            
        } catch (Exception e) {
            System.err.println("获取高德地图POI失败: " + e.getMessage());
        }
        
        // 返回模拟数据
        return generateMockPoiData(keyword, city);
    }
    
    @Override
    public Map<String, Object> geocode(String address) {
        try {
            // 使用高德地图地理编码API
            String encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");
            String url = String.format("%s/v3/geocode/geo?key=%s&address=%s&output=json", 
                apiConfig.getAmapApiUrl(), apiConfig.getAmapApiKey(), encodedAddress);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String status = jsonNode.get("status").asText();
                
                if ("1".equals(status) && jsonNode.has("geocodes") && jsonNode.get("geocodes").size() > 0) {
                    JsonNode geocode = jsonNode.get("geocodes").get(0);
                    String location = geocode.get("location").asText();
                    String[] coords = location.split(",");
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("longitude", new java.math.BigDecimal(coords[0]));
                    result.put("latitude", new java.math.BigDecimal(coords[1]));
                    result.put("formattedAddress", geocode.get("formatted_address").asText());
                    return result;
                }
            }
        } catch (Exception e) {
            System.err.println("地理编码失败: " + e.getMessage());
        }
        
        return new HashMap<>();
    }
    
    @Override
    public List<Map<String, Object>> getAmapInputTips(String keywords, String city, Boolean citylimit) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            String encodedKeywords = java.net.URLEncoder.encode(keywords, "UTF-8");
            String cityParam = (city != null && !city.isEmpty()) ? "&city=" + java.net.URLEncoder.encode(city, "UTF-8") : "";
            String citylimitParam = (citylimit != null && citylimit) ? "&citylimit=true" : "";
            
            String url = String.format("%s/v3/assistant/inputtips?key=%s&keywords=%s%s%s&output=json",
                apiConfig.getAmapApiUrl(), apiConfig.getAmapApiKey(), encodedKeywords, cityParam, citylimitParam);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String status = jsonNode.get("status").asText();
                
                if ("1".equals(status) && jsonNode.has("tips") && jsonNode.get("tips").isArray()) {
                    JsonNode tips = jsonNode.get("tips");
                    
                    for (JsonNode tip : tips) {
                        Map<String, Object> tipMap = new HashMap<>();
                        tipMap.put("name", tip.has("name") ? tip.get("name").asText() : "");
                        tipMap.put("district", tip.has("district") ? tip.get("district").asText() : "");
                        tipMap.put("address", tip.has("address") ? tip.get("address").asText() : "");
                        tipMap.put("location", tip.has("location") ? tip.get("location").asText() : "");
                        tipMap.put("adcode", tip.has("adcode") ? tip.get("adcode").asText() : "");
                        tipMap.put("type", tip.has("type") ? tip.get("type").asText() : "");
                        result.add(tipMap);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取高德地图输入提示失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> reverseGeocode(double longitude, double latitude) {
        try {
            // 使用高德地图逆地理编码API
            String url = String.format("%s/v3/geocode/regeo?key=%s&location=%s,%s&output=json&radius=1000&extensions=base", 
                apiConfig.getAmapApiUrl(), apiConfig.getAmapApiKey(), longitude, latitude);
            
            // 检查API Key是否有效
            if (apiConfig.getAmapApiKey() == null || 
                apiConfig.getAmapApiKey().equals("your-amap-api-key") ||
                apiConfig.getAmapApiKey().equals("your-dev-amap-api-key") ||
                apiConfig.getAmapApiKey().isEmpty()) {
                return null;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String status = jsonNode.get("status").asText();
                
                if ("1".equals(status) && jsonNode.has("regeocode")) {
                    JsonNode regeocode = jsonNode.get("regeocode");
                    JsonNode addressComponent = regeocode.get("addressComponent");
                    
                    Map<String, Object> result = new HashMap<>();
                    
                    // 提取城市信息
                    String province = addressComponent.has("province") ? addressComponent.get("province").asText() : "";
                    String city = addressComponent.has("city") ? addressComponent.get("city").asText() : "";
                    String district = addressComponent.has("district") ? addressComponent.get("district").asText() : "";
                    
                    // 如果city为空，使用province（直辖市的情况）
                    if (city == null || city.isEmpty()) {
                        city = province;
                    }
                    
                    result.put("city", city);
                    result.put("province", province);
                    result.put("district", district);
                    result.put("formattedAddress", regeocode.has("formatted_address") ? regeocode.get("formatted_address").asText() : "");
                    
                    return result;
                }
            }
        } catch (Exception e) {
            System.err.println("逆地理编码失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 判断是否是内网IP
     */
    private boolean isPrivateIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        
        // 检查IPv6（包含冒号）
        if (ip.contains(":")) {
            return ip.startsWith("::1") || ip.startsWith("fe80:") || ip.startsWith("fc00:");
        }
        
        // 检查IPv4内网地址
        return ip.equals("127.0.0.1") || ip.equals("localhost") ||
               ip.startsWith("192.168.") || ip.startsWith("10.") ||
               ip.startsWith("172.16.") || ip.startsWith("172.17.") ||
               ip.startsWith("172.18.") || ip.startsWith("172.19.") ||
               ip.startsWith("172.20.") || ip.startsWith("172.21.") ||
               ip.startsWith("172.22.") || ip.startsWith("172.23.") ||
               ip.startsWith("172.24.") || ip.startsWith("172.25.") ||
               ip.startsWith("172.26.") || ip.startsWith("172.27.") ||
               ip.startsWith("172.28.") || ip.startsWith("172.29.") ||
               ip.startsWith("172.30.") || ip.startsWith("172.31.");
    }
    
    @Override
    public Map<String, Object> getLocationByIp(String ipAddress) {
        try {
            // 处理IP地址：如果是内网IP或IPv6，使用空字符串让高德API自动检测
            // 但注意：自动检测可能识别到服务器IP而非客户端IP
            if (ipAddress == null || ipAddress.isEmpty()) {
                System.out.println("⚠️ IP地址为空，使用高德API自动检测（可能检测到服务器IP）");
                ipAddress = "";
            } else if (isPrivateIp(ipAddress)) {
                System.out.println("⚠️ IP地址是内网地址，使用高德API自动检测: " + ipAddress);
                System.out.println("💡 注意：自动检测可能识别到服务器IP而非客户端IP");
                ipAddress = "";
            } else if (ipAddress.contains(":")) {
                System.out.println("⚠️ IP地址是IPv6，使用高德API自动检测: " + ipAddress);
                ipAddress = "";
            } else {
                System.out.println("✅ 使用客户端真实IP: " + ipAddress);
            }
            
            // 使用高德地图IP定位API
            // 重要：如果传了客户端真实IP，高德API会使用该IP定位（准确）
            // 如果不传IP参数，高德API会检测到服务器的IP（可能不准确）
            String url;
            if (ipAddress != null && !ipAddress.isEmpty() && !isPrivateIp(ipAddress) && !ipAddress.contains(":")) {
                // 有客户端真实IP，传递给高德API（最准确）
                url = String.format("%s/v3/ip?key=%s&ip=%s", 
                    apiConfig.getAmapApiUrl(), apiConfig.getAmapApiKey(), 
                    java.net.URLEncoder.encode(ipAddress, "UTF-8"));
                System.out.println("🌐 使用高德IP定位API（传递客户端真实IP）");
                System.out.println("📡 请求URL: " + url.replace(apiConfig.getAmapApiKey(), "***"));
                System.out.println("💡 说明：传递客户端真实IP，确保定位准确");
            } else {
                // 没有客户端真实IP，使用自动检测模式（可能检测到服务器IP）
                url = String.format("%s/v3/ip?key=%s", 
                    apiConfig.getAmapApiUrl(), apiConfig.getAmapApiKey());
                System.out.println("🌐 使用高德IP定位API（自动检测模式）");
                System.out.println("📡 请求URL: " + url.replace(apiConfig.getAmapApiKey(), "***"));
                System.out.println("⚠️ 警告：自动检测可能识别到服务器IP而非客户端IP，导致定位不准确");
            }
            
            // 检查API Key是否有效
            if (apiConfig.getAmapApiKey() == null || 
                apiConfig.getAmapApiKey().equals("your-amap-api-key") ||
                apiConfig.getAmapApiKey().equals("your-dev-amap-api-key") ||
                apiConfig.getAmapApiKey().isEmpty()) {
                System.err.println("❌ 高德API Key未配置");
                return null;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TravelApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                System.out.println("高德IP定位API完整响应: " + responseBody);
                
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String status = jsonNode.get("status").asText();
                String info = jsonNode.has("info") ? jsonNode.get("info").asText() : "";
                
                System.out.println("IP定位API响应解析: status=" + status + ", info=" + info);
                
                if ("1".equals(status)) {
                    Map<String, Object> result = new HashMap<>();
                    
                    // 提取城市信息
                    String province = jsonNode.has("province") ? jsonNode.get("province").asText() : "";
                    String city = jsonNode.has("city") ? jsonNode.get("city").asText() : "";
                    String adcode = jsonNode.has("adcode") ? jsonNode.get("adcode").asText() : "";
                    String rectangle = jsonNode.has("rectangle") ? jsonNode.get("rectangle").asText() : "";
                    
                    System.out.println("📦 高德IP定位API原始数据:");
                    System.out.println("   province: " + province);
                    System.out.println("   city: " + city);
                    System.out.println("   adcode: " + adcode);
                    System.out.println("   rectangle: " + rectangle);
                    
                    // 如果city为空，使用province（直辖市的情况）
                    if (city == null || city.isEmpty()) {
                        city = province;
                        System.out.println("⚠️ city为空，使用province: " + city);
                    }
                    
                    // 检查是否定位到崇左（可能是IP定位不准确）
                    if (city != null && (city.contains("崇左") || city.contains("崇左市"))) {
                        System.out.println("⚠️ 高德IP定位返回崇左，可能是IP定位不准确，建议使用浏览器定位获取更准确位置");
                    }
                    
                    // 去掉"市"、"省"等后缀
                    String originalCity = city;
                    if (city != null && city.endsWith("市")) {
                        city = city.substring(0, city.length() - 1);
                    }
                    if (province != null && province.endsWith("省")) {
                        province = province.substring(0, province.length() - 1);
                    }
                    
                    System.out.println("📍 处理后的城市信息: " + city + " (原始: " + originalCity + ")");
                    
                    result.put("city", city);
                    result.put("province", province);
                    result.put("adcode", adcode);
                    result.put("rectangle", rectangle);
                    result.put("source", "ip"); // 标记来源是IP定位
                    
                    // 如果获取到了rectangle（边界坐标），尝试使用逆地理编码获取更详细的区县信息
                    if (rectangle != null && !rectangle.isEmpty() && !adcode.isEmpty()) {
                        try {
                            // rectangle格式通常是"左下角经度,左下角纬度;右上角经度,右上角纬度"
                            // 例如："108.123456,22.123456;108.234567,22.234567"
                            String[] parts = rectangle.split(";");
                            if (parts.length == 2) {
                                String[] bottomLeft = parts[0].split(",");
                                String[] topRight = parts[1].split(",");
                                
                                if (bottomLeft.length == 2 && topRight.length == 2) {
                                    // 计算中心点坐标
                                    double centerLon = (Double.parseDouble(bottomLeft[0]) + Double.parseDouble(topRight[0])) / 2.0;
                                    double centerLat = (Double.parseDouble(bottomLeft[1]) + Double.parseDouble(topRight[1])) / 2.0;
                                    
                                    System.out.println("📍 计算得到中心点坐标: " + centerLon + ", " + centerLat);
                                    
                                    // 使用逆地理编码获取详细的区县信息
                                    Map<String, Object> reverseGeoResult = reverseGeocode(centerLon, centerLat);
                                    if (reverseGeoResult != null && !reverseGeoResult.isEmpty()) {
                                        String detailedDistrict = (String) reverseGeoResult.get("district");
                                        String detailedCity = (String) reverseGeoResult.get("city");
                                        
                                        // 如果逆地理编码返回的城市与IP定位一致，使用详细的区县信息
                                        if (detailedCity != null && !detailedCity.isEmpty()) {
                                            String detailedCityName = detailedCity.replace("市", "").replace("省", "").trim();
                                            if (detailedCityName.equals(city) || city.equals(detailedCityName)) {
                                                if (detailedDistrict != null && !detailedDistrict.isEmpty()) {
                                                    result.put("district", detailedDistrict);
                                                    System.out.println("✅ 通过逆地理编码获取到详细区县: " + detailedDistrict);
                                                }
                                            }
                                        }
                                        
                                        // 保存完整的地址信息
                                        if (reverseGeoResult.containsKey("formattedAddress")) {
                                            result.put("formattedAddress", reverseGeoResult.get("formattedAddress"));
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("⚠️ 解析rectangle坐标失败: " + e.getMessage());
                            // 继续使用IP定位的结果，不因为逆地理编码失败而影响整体流程
                        }
                    }
                    
                    if (city != null && !city.isEmpty()) {
                        System.out.println("✅ IP定位成功: " + city + " (IP: " + (ipAddress.isEmpty() ? "自动检测" : ipAddress) + ")");
                    } else {
                        System.err.println("⚠️ IP定位API返回成功，但城市信息为空");
                    }
                    
                    return result;
                } else {
                    System.err.println("❌ IP定位API返回失败: status=" + status + ", info=" + info);
                    if ("10001".equals(status)) {
                        System.err.println("   可能原因：API Key无效或未启用IP定位服务");
                    } else if ("10003".equals(status)) {
                        System.err.println("   可能原因：API Key权限不足");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ IP定位异常: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 处理POI数据
     */
    private List<Map<String, Object>> processPoiData(JsonNode pois) {
        List<Map<String, Object>> processedPois = new ArrayList<>();
        
        if (pois != null && pois.isArray()) {
            for (JsonNode poi : pois) {
                Map<String, Object> processedPoi = new HashMap<>();
                processedPoi.put("id", poi.get("id").asText());
                processedPoi.put("name", poi.get("name").asText());
                processedPoi.put("type", poi.get("type").asText());
                processedPoi.put("address", poi.get("address").asText());
                processedPoi.put("location", poi.get("location").asText());
                processedPoi.put("tel", poi.has("tel") ? poi.get("tel").asText() : "");
                processedPoi.put("distance", poi.has("distance") ? poi.get("distance").asText() : "");
                processedPoi.put("rating", poi.has("rating") ? poi.get("rating").asText() : "");
                processedPois.add(processedPoi);
            }
        }
        
        return processedPois;
    }
    
    /**
     * 生成模拟POI数据
     */
    private Map<String, Object> generateMockPoiData(String keyword, String city) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "1");
        result.put("count", 3);
        result.put("info", "OK");
        
        List<Map<String, Object>> mockPois = new ArrayList<>();
        
        // 模拟景点数据
        Map<String, Object> poi1 = new HashMap<>();
        poi1.put("id", "mock_001");
        poi1.put("name", keyword + "景点1");
        poi1.put("type", "风景名胜");
        poi1.put("address", city + "市某区某街道");
        poi1.put("location", "116.397428,39.90923");
        poi1.put("tel", "010-12345678");
        poi1.put("distance", "500");
        poi1.put("rating", "4.5");
        mockPois.add(poi1);
        
        Map<String, Object> poi2 = new HashMap<>();
        poi2.put("id", "mock_002");
        poi2.put("name", keyword + "景点2");
        poi2.put("type", "风景名胜");
        poi2.put("address", city + "市某区某街道");
        poi2.put("location", "116.407428,39.91923");
        poi2.put("tel", "010-87654321");
        poi2.put("distance", "800");
        poi2.put("rating", "4.2");
        mockPois.add(poi2);
        
        result.put("pois", mockPois);
        result.put("processedPois", mockPois);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getAmapRoute(String origin, String destination, String strategy) {
        try {
            String url = String.format("%s/v3/direction/driving?key=%s&origin=%s&destination=%s&strategy=%s&output=json", 
                apiConfig.getAmapApiUrl(), apiConfig.getAmapApiKey(), origin, destination, strategy);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                Map<String, Object> result = new HashMap<>();
                result.put("status", jsonNode.get("status").asText());
                result.put("route", jsonNode.get("route"));
                
                return result;
            }
            
        } catch (Exception e) {
            System.err.println("获取高德地图路线失败: " + e.getMessage());
        }
        
        // 返回模拟数据
        Map<String, Object> result = new HashMap<>();
        result.put("status", "1");
        result.put("route", new HashMap<>());
        return result;
    }
    
    @Override
    public Map<String, Object> getTrainTickets(String from, String to, String date) {
        try {
            // 12306 API通常需要复杂的认证，这里返回模拟数据
            Map<String, Object> result = new HashMap<>();
            result.put("from", from);
            result.put("to", to);
            result.put("date", date);
            result.put("trains", new ArrayList<>());
            result.put("message", "12306 API需要特殊认证，返回模拟数据");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("获取火车票信息失败: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Object> getAttractionTickets(String attractionId) {
        try {
            // 景点门票API通常需要对接具体的票务平台
            Map<String, Object> result = new HashMap<>();
            result.put("attractionId", attractionId);
            result.put("tickets", new ArrayList<>());
            result.put("message", "景点门票API需要对接具体票务平台");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("获取景点门票失败: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Object> getHotels(String city, String checkIn, String checkOut) {
        try {
            // 酒店API通常需要对接携程、去哪儿等平台
            Map<String, Object> result = new HashMap<>();
            result.put("city", city);
            result.put("checkIn", checkIn);
            result.put("checkOut", checkOut);
            result.put("hotels", new ArrayList<>());
            result.put("message", "酒店API需要对接具体平台");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("获取酒店信息失败: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Object> getFlights(String from, String to, String date) {
        try {
            // 航班API通常需要对接航司或第三方平台
            Map<String, Object> result = new HashMap<>();
            result.put("from", from);
            result.put("to", to);
            result.put("date", date);
            result.put("flights", new ArrayList<>());
            result.put("message", "航班API需要对接具体平台");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("获取航班信息失败: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
