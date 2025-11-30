package com.zhly.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhly.entity.*;
import com.zhly.mapper.*;
import com.zhly.user.service.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户画像服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class UserPortraitServiceImpl implements UserPortraitService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserBrowseHistoryMapper browseHistoryMapper;
    
    @Autowired
    private UserCollectMapper userCollectMapper;
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    @Autowired
    private TravelPlanMapper travelPlanMapper;
    
    @Autowired
    private SearchLogMapper searchLogMapper;
    
    @Override
    public Map<String, Object> getUserPortrait(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取用户基本信息
            User user = userMapper.selectById(userId);
            if (user == null) {
                // 用户不存在，返回空数据（不返回默认值）
                return buildEmptyPortrait();
            }
            
            // 2. 统计数据
            Long browseCount = getBrowseCount(userId);
            Long favoriteCount = getFavoriteCount(userId);
            Long searchCount = getSearchCount(userId);
            Integer avgStayTime = getAvgStayTime(userId);
            
            // 3. 旅游偏好分析
            Map<String, Object> preferenceAnalysis = analyzeTravelPreference(userId);
            
            // 4. 兴趣标签云
            List<Map<String, Object>> interestTags = analyzeInterestTags(userId);
            
            // 5. 消费行为分析
            Map<String, Object> consumptionAnalysis = analyzeConsumptionBehavior(userId);
            
            // 6. 出行特征
            Map<String, Object> travelCharacteristics = analyzeTravelCharacteristics(userId, user);
            
            // 7. 组装结果
            result.put("browseCount", browseCount);
            result.put("favoriteCount", favoriteCount);
            result.put("searchCount", searchCount);
            result.put("avgStayTime", avgStayTime);
            result.put("primaryPreference", preferenceAnalysis.get("primaryPreference"));
            result.put("preferenceDistribution", preferenceAnalysis.get("distribution"));
            result.put("interestTags", interestTags);
            result.put("consumptionLevel", consumptionAnalysis.get("level"));
            result.put("avgConsumption", consumptionAnalysis.get("avgConsumption"));
            result.put("consumptionFrequency", consumptionAnalysis.get("frequency"));
            result.put("priceSensitivity", consumptionAnalysis.get("priceSensitivity"));
            result.put("travelMode", travelCharacteristics.get("travelMode"));
            result.put("tripDuration", travelCharacteristics.get("tripDuration"));
            result.put("seasonPreference", travelCharacteristics.get("seasonPreference"));
            result.put("frequentDestinations", travelCharacteristics.get("frequentDestinations"));
            result.put("lastUpdateTime", LocalDateTime.now().toString().replace("T", " ").substring(0, 16));
            
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            // 发生异常，返回空数据（不返回默认值）
            return buildEmptyPortrait();
        }
    }
    
    /**
     * 获取浏览次数
     */
    private Long getBrowseCount(Long userId) {
        QueryWrapper<UserBrowseHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return browseHistoryMapper.selectCount(wrapper);
    }
    
    /**
     * 获取收藏数量
     */
    private Long getFavoriteCount(Long userId) {
        QueryWrapper<UserCollect> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return userCollectMapper.selectCount(wrapper);
    }
    
    /**
     * 获取搜索次数（从搜索日志表统计）
     */
    private Long getSearchCount(Long userId) {
        try {
            // 从搜索日志表统计真实的搜索次数
            QueryWrapper<SearchLog> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            return searchLogMapper.selectCount(wrapper);
        } catch (Exception e) {
            // 如果搜索日志表不存在或查询失败，返回0（不返回估算值）
            System.err.println("获取搜索次数失败: " + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * 获取平均停留时间（分钟）
     */
    private Integer getAvgStayTime(Long userId) {
        QueryWrapper<UserBrowseHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.select("duration");
        wrapper.isNotNull("duration"); // 只统计有duration的记录
        wrapper.gt("duration", 0); // duration必须大于0
        List<UserBrowseHistory> histories = browseHistoryMapper.selectList(wrapper);
        
        // 如果没有有效的停留时间记录，返回0（表示没有数据）
        if (histories == null || histories.isEmpty()) {
            return 0;
        }
        
        // 计算平均停留时间（秒）
        int totalDuration = histories.stream()
            .mapToInt(h -> h.getDuration() != null ? h.getDuration() : 0)
            .sum();
        
        int avgSeconds = totalDuration / histories.size();
        
        // 转换为分钟，如果小于1分钟则设为1分钟，如果大于180分钟（3小时）则设为180分钟
        int avgMinutes = Math.max(1, avgSeconds / 60);
        return Math.min(avgMinutes, 180); // 最大180分钟（3小时）
    }
    
    /**
     * 分析旅游偏好
     */
    private Map<String, Object> analyzeTravelPreference(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 统计用户浏览和收藏的景点类型分布
        Map<String, Integer> typeCountMap = new HashMap<>();
        
        // 从浏览历史分析
        QueryWrapper<UserBrowseHistory> browseWrapper = new QueryWrapper<>();
        browseWrapper.eq("user_id", userId);
        List<UserBrowseHistory> browseHistories = browseHistoryMapper.selectList(browseWrapper);
        
        for (UserBrowseHistory history : browseHistories) {
            if (history.getBrowseType() == 1) { // 景点
                Attraction attraction = attractionMapper.selectById(history.getBrowseId());
                if (attraction != null && attraction.getType() != null) {
                    String typeName = getAttractionTypeName(attraction.getType());
                    typeCountMap.put(typeName, typeCountMap.getOrDefault(typeName, 0) + 1);
                }
            } else if (history.getBrowseType() == 2) { // 攻略
                TravelPlan plan = travelPlanMapper.selectById(history.getBrowseId());
                if (plan != null && plan.getTags() != null) {
                    // 从标签中提取偏好
                    String[] tags = plan.getTags().split(",");
                    for (String tag : tags) {
                        String preference = mapTagToPreference(tag.trim());
                        if (preference != null) {
                            typeCountMap.put(preference, typeCountMap.getOrDefault(preference, 0) + 1);
                        }
                    }
                }
            }
        }
        
        // 从收藏记录分析
        QueryWrapper<UserCollect> collectWrapper = new QueryWrapper<>();
        collectWrapper.eq("user_id", userId);
        List<UserCollect> collects = userCollectMapper.selectList(collectWrapper);
        
        for (UserCollect collect : collects) {
            if (collect.getCollectType() == 2) { // 景点收藏
                Attraction attraction = attractionMapper.selectById(collect.getCollectId());
                if (attraction != null && attraction.getType() != null) {
                    String typeName = getAttractionTypeName(attraction.getType());
                    typeCountMap.put(typeName, typeCountMap.getOrDefault(typeName, 0) + 1);
                }
            }
        }
        
        // 如果没有任何数据，返回空数据
        if (typeCountMap.isEmpty()) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("primaryPreference", "");
            emptyResult.put("distribution", new ArrayList<>());
            return emptyResult;
        }
        
        // 计算百分比
        int total = typeCountMap.values().stream().mapToInt(Integer::intValue).sum();
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        // 定义偏好类型（与管理平台创建景点的类型对应）
        Map<String, String> preferenceMap = new HashMap<>();
        preferenceMap.put("自然风光", "🏞️");  // type=1
        preferenceMap.put("人文历史", "🏛️");  // type=2
        preferenceMap.put("主题公园", "🎢");  // type=3
        preferenceMap.put("美食街", "🍜");    // type=4
        preferenceMap.put("古镇古村", "🏘️");  // type=5
        preferenceMap.put("温泉度假", "♨️");   // type=6
        preferenceMap.put("宗教场所", "⛩️");   // type=7
        preferenceMap.put("购物娱乐", "🛍️");   // type=8
        
        // 合并类型到偏好分类（与管理平台创建景点的类型对应）
        Map<String, Integer> mergedMap = new HashMap<>();
        mergedMap.put("自然风光", 0);      // type=1
        mergedMap.put("人文历史", 0);      // type=2
        mergedMap.put("主题公园", 0);      // type=3
        mergedMap.put("美食街", 0);        // type=4
        mergedMap.put("古镇古村", 0);      // type=5
        mergedMap.put("温泉度假", 0);      // type=6
        mergedMap.put("宗教场所", 0);      // type=7
        mergedMap.put("购物娱乐", 0);      // type=8
        
        for (Map.Entry<String, Integer> entry : typeCountMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            // 直接匹配类型名称（与管理平台创建景点的类型名称一致）
            if (key.equals("自然风光")) {
                mergedMap.put("自然风光", mergedMap.get("自然风光") + value);
            } else if (key.equals("人文历史")) {
                mergedMap.put("人文历史", mergedMap.get("人文历史") + value);
            } else if (key.equals("主题公园")) {
                mergedMap.put("主题公园", mergedMap.get("主题公园") + value);
            } else if (key.equals("美食街")) {
                mergedMap.put("美食街", mergedMap.get("美食街") + value);
            } else if (key.equals("古镇古村")) {
                mergedMap.put("古镇古村", mergedMap.get("古镇古村") + value);
            } else if (key.equals("温泉度假")) {
                mergedMap.put("温泉度假", mergedMap.get("温泉度假") + value);
            } else if (key.equals("宗教场所")) {
                mergedMap.put("宗教场所", mergedMap.get("宗教场所") + value);
            } else if (key.equals("购物娱乐")) {
                mergedMap.put("购物娱乐", mergedMap.get("购物娱乐") + value);
            }
            // 兼容旧数据：如果类型名称包含某些关键词，也进行映射
            else if (key.contains("自然") || key.contains("景观") || key.contains("风景")) {
                mergedMap.put("自然风光", mergedMap.get("自然风光") + value);
            } else if (key.contains("历史") || key.contains("人文") || key.contains("文化") || key.contains("博物馆") || key.contains("历史遗迹")) {
                mergedMap.put("人文历史", mergedMap.get("人文历史") + value);
            } else if (key.contains("美食") || key.contains("食物") || key.contains("餐厅")) {
                mergedMap.put("美食街", mergedMap.get("美食街") + value);
            } else if (key.contains("主题") || key.contains("公园") || key.contains("乐园")) {
                mergedMap.put("主题公园", mergedMap.get("主题公园") + value);
            } else if (key.contains("古镇") || key.contains("古村") || key.contains("村落")) {
                mergedMap.put("古镇古村", mergedMap.get("古镇古村") + value);
            } else if (key.contains("温泉") || key.contains("度假") || key.contains("休闲")) {
                mergedMap.put("温泉度假", mergedMap.get("温泉度假") + value);
            } else if (key.contains("宗教") || key.contains("寺庙") || key.contains("教堂") || key.contains("道观")) {
                mergedMap.put("宗教场所", mergedMap.get("宗教场所") + value);
            } else if (key.contains("购物") || key.contains("商业") || key.contains("娱乐")) {
                mergedMap.put("购物娱乐", mergedMap.get("购物娱乐") + value);
            }
        }
        
        // 如果合并后还是空的，返回空数据
        if (mergedMap.values().stream().allMatch(v -> v == 0)) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("primaryPreference", "");
            emptyResult.put("distribution", new ArrayList<>());
            return emptyResult;
        }
        
        // 计算百分比并排序
        List<Map.Entry<String, Integer>> sorted = mergedMap.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .collect(Collectors.toList());
        
        String primaryPreference = sorted.get(0).getKey();
        
        // 计算百分比
        for (Map.Entry<String, Integer> entry : sorted) {
            if (entry.getValue() > 0) {
                int percentage = (int) Math.round((entry.getValue() * 100.0) / total);
                Map<String, Object> item = new HashMap<>();
                item.put("type", entry.getKey());
                item.put("icon", preferenceMap.getOrDefault(entry.getKey(), "📍"));
                item.put("percentage", percentage);
                item.put("color", getPreferenceColor(entry.getKey()));
                distribution.add(item);
            }
        }
        
        // 确保百分比总和为100
        int sum = distribution.stream().mapToInt(d -> (Integer) d.get("percentage")).sum();
        if (sum != 100 && !distribution.isEmpty()) {
            int diff = 100 - sum;
            Map<String, Object> first = distribution.get(0);
            first.put("percentage", (Integer) first.get("percentage") + diff);
        }
        
        result.put("primaryPreference", primaryPreference);
        result.put("distribution", distribution);
        
        return result;
    }
    
    /**
     * 分析兴趣标签
     */
    private List<Map<String, Object>> analyzeInterestTags(Long userId) {
        Map<String, Integer> tagWeightMap = new HashMap<>();
        
        // 从浏览历史提取标签
        QueryWrapper<UserBrowseHistory> browseWrapper = new QueryWrapper<>();
        browseWrapper.eq("user_id", userId);
        List<UserBrowseHistory> browseHistories = browseHistoryMapper.selectList(browseWrapper);
        
        for (UserBrowseHistory history : browseHistories) {
            if (history.getBrowseType() == 1) { // 景点
                Attraction attraction = attractionMapper.selectById(history.getBrowseId());
                if (attraction != null && attraction.getTags() != null) {
                    String[] tags = attraction.getTags().split(",");
                    for (String tag : tags) {
                        // 去除前后空格，并去除#号等特殊字符
                        String trimmed = tag.trim().replace("#", "").replace("@", "");
                        if (!trimmed.isEmpty()) {
                            tagWeightMap.put(trimmed, tagWeightMap.getOrDefault(trimmed, 0) + 1);
                        }
                    }
                }
            } else if (history.getBrowseType() == 2) { // 攻略
                TravelPlan plan = travelPlanMapper.selectById(history.getBrowseId());
                if (plan != null && plan.getTags() != null) {
                    String[] tags = plan.getTags().split(",");
                    for (String tag : tags) {
                        // 去除前后空格，并去除#号等特殊字符
                        String trimmed = tag.trim().replace("#", "").replace("@", "");
                        if (!trimmed.isEmpty()) {
                            tagWeightMap.put(trimmed, tagWeightMap.getOrDefault(trimmed, 0) + 1);
                        }
                    }
                }
            }
        }
        
        // 从收藏记录提取标签
        QueryWrapper<UserCollect> collectWrapper = new QueryWrapper<>();
        collectWrapper.eq("user_id", userId);
        List<UserCollect> collects = userCollectMapper.selectList(collectWrapper);
        
        for (UserCollect collect : collects) {
            if (collect.getCollectType() == 2) { // 景点
                Attraction attraction = attractionMapper.selectById(collect.getCollectId());
                if (attraction != null && attraction.getTags() != null) {
                    String[] tags = attraction.getTags().split(",");
                    for (String tag : tags) {
                        // 去除前后空格，并去除#号等特殊字符
                        String trimmed = tag.trim().replace("#", "").replace("@", "");
                        if (!trimmed.isEmpty()) {
                            tagWeightMap.put(trimmed, tagWeightMap.getOrDefault(trimmed, 0) + 2); // 收藏权重更高
                        }
                    }
                }
            }
        }
        
        // 如果没有数据，返回空列表
        if (tagWeightMap.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 转换为标准兴趣标签（定义标准标签及其图标）
        Map<String, String> standardTags = new HashMap<>();
        standardTags.put("摄影", "📷");
        standardTags.put("美食", "🍜");
        standardTags.put("文化", "🎭");
        standardTags.put("自然风光", "🏔️");
        standardTags.put("历史", "📜");
        standardTags.put("建筑", "🏛️");
        standardTags.put("徒步", "🥾");
        standardTags.put("温泉", "♨️");
        standardTags.put("博物馆", "🏛️");
        standardTags.put("夜生活", "🌙");
        standardTags.put("夜景", "🌙"); // 夜景作为独立标签
        standardTags.put("购物", "🛍️");
        standardTags.put("度假", "🏖️");
        standardTags.put("情侣", "💑");
        standardTags.put("亲子", "👨‍👩‍👧"); // 添加亲子标签
        standardTags.put("休闲", "🏖️"); // 休闲映射到度假
        
        // 映射用户标签到标准标签
        Map<String, Integer> standardWeightMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : tagWeightMap.entrySet()) {
            String userTag = entry.getKey();
            Integer weight = entry.getValue();
            
            // 首先检查是否直接匹配标准标签（精确匹配优先）
            if (standardTags.containsKey(userTag)) {
                standardWeightMap.put(userTag, standardWeightMap.getOrDefault(userTag, 0) + weight);
            } else {
                // 如果不直接匹配，则进行关键词映射
                boolean mapped = false;
                if (userTag.contains("摄影") || userTag.contains("拍照")) {
                    standardWeightMap.put("摄影", standardWeightMap.getOrDefault("摄影", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("美食") || userTag.contains("吃") || userTag.contains("餐厅")) {
                    standardWeightMap.put("美食", standardWeightMap.getOrDefault("美食", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("文化") || userTag.contains("人文")) {
                    standardWeightMap.put("文化", standardWeightMap.getOrDefault("文化", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("自然") || userTag.contains("风景") || userTag.contains("景观")) {
                    standardWeightMap.put("自然风光", standardWeightMap.getOrDefault("自然风光", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("历史") || userTag.contains("古迹")) {
                    standardWeightMap.put("历史", standardWeightMap.getOrDefault("历史", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("建筑") || userTag.contains("古建筑")) {
                    standardWeightMap.put("建筑", standardWeightMap.getOrDefault("建筑", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("徒步") || userTag.contains("登山") || userTag.contains("爬山")) {
                    standardWeightMap.put("徒步", standardWeightMap.getOrDefault("徒步", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("温泉")) {
                    standardWeightMap.put("温泉", standardWeightMap.getOrDefault("温泉", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("博物馆")) {
                    standardWeightMap.put("博物馆", standardWeightMap.getOrDefault("博物馆", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("夜景")) {
                    // 夜景优先作为独立标签
                    standardWeightMap.put("夜景", standardWeightMap.getOrDefault("夜景", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("夜生活") || userTag.contains("夜市")) {
                    standardWeightMap.put("夜生活", standardWeightMap.getOrDefault("夜生活", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("购物") || userTag.contains("买")) {
                    standardWeightMap.put("购物", standardWeightMap.getOrDefault("购物", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("度假") || userTag.contains("休闲")) {
                    standardWeightMap.put("度假", standardWeightMap.getOrDefault("度假", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("情侣") || userTag.contains("恋爱") || userTag.contains("约会")) {
                    standardWeightMap.put("情侣", standardWeightMap.getOrDefault("情侣", 0) + weight);
                    mapped = true;
                }
                if (userTag.contains("亲子") || userTag.contains("家庭") || userTag.contains("带孩子")) {
                    standardWeightMap.put("亲子", standardWeightMap.getOrDefault("亲子", 0) + weight);
                    mapped = true;
                }
                // 如果没有匹配到任何标准标签，但标签不为空，也保留原始标签（使用默认图标）
                if (!mapped && !userTag.isEmpty()) {
                    standardWeightMap.put(userTag, standardWeightMap.getOrDefault(userTag, 0) + weight);
                }
            }
        }
        
        // 如果没有匹配到标准标签，返回空列表
        if (standardWeightMap.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 计算权重并排序
        int maxWeight = standardWeightMap.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : standardWeightMap.entrySet()) {
            Map<String, Object> tag = new HashMap<>();
            tag.put("name", entry.getKey());
            tag.put("icon", standardTags.getOrDefault(entry.getKey(), "📍"));
            // 权重归一化到1-10
            tag.put("weight", Math.max(1, Math.min(10, (int) Math.round(entry.getValue() * 10.0 / maxWeight))));
            result.add(tag);
        }
        
        // 按权重排序
        result.sort((a, b) -> ((Integer) b.get("weight")).compareTo((Integer) a.get("weight")));
        
        // 限制返回数量
        return result.stream().limit(10).collect(Collectors.toList());
    }
    
    /**
     * 分析消费行为
     */
    private Map<String, Object> analyzeConsumptionBehavior(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 使用Set去重浏览记录，key为"browseType-browseId"
        Set<String> browseKeys = new HashSet<>();
        Map<String, BigDecimal> priceMap = new HashMap<>(); // key为"browseType-browseId"，value为日均价格（元/天）
        Map<String, Integer> weightMap = new HashMap<>(); // key为"browseType-browseId"，value为权重（1=浏览，2=收藏）
        
        // 1. 从用户浏览的景点和攻略中分析消费水平（去重）
        QueryWrapper<UserBrowseHistory> browseWrapper = new QueryWrapper<>();
        browseWrapper.eq("user_id", userId);
        List<UserBrowseHistory> histories = browseHistoryMapper.selectList(browseWrapper);
        
        for (UserBrowseHistory history : histories) {
            String key = history.getBrowseType() + "-" + history.getBrowseId();
            // 去重：同一个攻略/景点只统计一次（按时间倒序，保留最新的）
            if (browseKeys.contains(key)) {
                continue;
            }
            browseKeys.add(key);
            
            if (history.getBrowseType() == 1) { // 景点
                Attraction attraction = attractionMapper.selectById(history.getBrowseId());
                if (attraction != null && attraction.getTicketPrice() != null 
                    && attraction.getTicketPrice().compareTo(BigDecimal.ZERO) > 0) {
                    // 景点门票价格：假设用户一天可能去多个景点，但为了统一单位（元/天），
                    // 我们将门票价格视为"日均消费的一部分"
                    // 如果门票价格较高，说明用户愿意为单次体验支付更多，可以视为日均消费的参考
                    // 这里直接使用门票价格作为日均消费参考值（单位统一为元/天）
                    priceMap.put(key, attraction.getTicketPrice());
                    weightMap.put(key, 1); // 浏览权重为1
                }
            } else if (history.getBrowseType() == 2) { // 攻略
                TravelPlan plan = travelPlanMapper.selectById(history.getBrowseId());
                if (plan != null && plan.getBudget() != null 
                    && plan.getBudget().compareTo(BigDecimal.ZERO) > 0) {
                    // 攻略预算按天数平均，得到日均预算（单位：元/天）
                    BigDecimal avgBudget = plan.getBudget();
                    if (plan.getDays() != null && plan.getDays() > 0) {
                        avgBudget = avgBudget.divide(BigDecimal.valueOf(plan.getDays()), 2, RoundingMode.HALF_UP);
                    }
                    // 只统计大于0的预算
                    if (avgBudget.compareTo(BigDecimal.ZERO) > 0) {
                        priceMap.put(key, avgBudget);
                        weightMap.put(key, 1); // 浏览权重为1
                    }
                }
            }
        }
        
        // 2. 从收藏记录中分析消费水平（收藏权重更高，覆盖浏览记录）
        QueryWrapper<UserCollect> collectWrapper = new QueryWrapper<>();
        collectWrapper.eq("user_id", userId);
        List<UserCollect> collects = userCollectMapper.selectList(collectWrapper);
        
        for (UserCollect collect : collects) {
            String key = collect.getCollectType() + "-" + collect.getCollectId();
            
            if (collect.getCollectType() == 2) { // 景点收藏
                Attraction attraction = attractionMapper.selectById(collect.getCollectId());
                if (attraction != null && attraction.getTicketPrice() != null 
                    && attraction.getTicketPrice().compareTo(BigDecimal.ZERO) > 0) {
                    // 收藏的景点价格覆盖浏览记录，权重更高
                    priceMap.put(key, attraction.getTicketPrice());
                    weightMap.put(key, 2); // 收藏权重为2
                }
            } else if (collect.getCollectType() == 1) { // 攻略收藏
                TravelPlan plan = travelPlanMapper.selectById(collect.getCollectId());
                if (plan != null && plan.getBudget() != null 
                    && plan.getBudget().compareTo(BigDecimal.ZERO) > 0) {
                    // 攻略预算按天数平均，得到日均预算（单位：元/天）
                    BigDecimal avgBudget = plan.getBudget();
                    if (plan.getDays() != null && plan.getDays() > 0) {
                        avgBudget = avgBudget.divide(BigDecimal.valueOf(plan.getDays()), 2, RoundingMode.HALF_UP);
                    }
                    // 只统计大于0的预算
                    if (avgBudget.compareTo(BigDecimal.ZERO) > 0) {
                        // 收藏的攻略预算覆盖浏览记录，权重更高
                        priceMap.put(key, avgBudget);
                        weightMap.put(key, 2); // 收藏权重为2
                    }
                }
            }
        }
        
        // 计算加权平均消费（根据权重计算，单位：元/天）
        BigDecimal avgConsumption = BigDecimal.ZERO;
        BigDecimal totalWeightedSum = BigDecimal.ZERO;
        int totalWeight = 0;
        
        for (Map.Entry<String, BigDecimal> entry : priceMap.entrySet()) {
            String key = entry.getKey();
            BigDecimal price = entry.getValue();
            Integer weight = weightMap.getOrDefault(key, 1);
            
            // 加权求和：价格 × 权重
            totalWeightedSum = totalWeightedSum.add(price.multiply(BigDecimal.valueOf(weight)));
            totalWeight += weight;
        }
        
        // 计算加权平均值
        if (totalWeight > 0) {
            avgConsumption = totalWeightedSum.divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);
        }
        
        // 如果没有数据，平均消费为0（不设置默认值）
        
        // 判断消费水平（如果没有数据，返回空字符串）
        String consumptionLevel = "";
        String levelIcon = "";
        String levelColor = "";
        
        if (avgConsumption.compareTo(BigDecimal.ZERO) > 0) {
            if (avgConsumption.compareTo(BigDecimal.valueOf(1500)) >= 0) {
                consumptionLevel = "豪华型";
                levelIcon = "💎";
                levelColor = "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)";
            } else if (avgConsumption.compareTo(BigDecimal.valueOf(500)) >= 0) {
                consumptionLevel = "品质舒适型";
                levelIcon = "💳";
                levelColor = "linear-gradient(135deg, #667eea 0%, #764ba2 100%)";
            } else {
                consumptionLevel = "经济实惠型";
                levelIcon = "💰";
                levelColor = "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)";
            }
        }
        
        // 消费频次（基于去重后的浏览记录和收藏记录估算）
        // 统计最近一个月的去重浏览记录（按日期去重，同一天浏览多个内容算一次）
        QueryWrapper<UserBrowseHistory> freqWrapper = new QueryWrapper<>();
        freqWrapper.eq("user_id", userId);
        freqWrapper.ge("create_time", LocalDateTime.now().minusMonths(1));
        List<UserBrowseHistory> recentHistories = browseHistoryMapper.selectList(freqWrapper);
        
        // 按日期去重，统计实际活跃天数
        Set<String> activeDays = new HashSet<>();
        if (recentHistories != null && !recentHistories.isEmpty()) {
            for (UserBrowseHistory history : recentHistories) {
                if (history.getCreateTime() != null) {
                    String dayKey = history.getCreateTime().toLocalDate().toString();
                    activeDays.add(dayKey);
                }
            }
        }
        
        // 统计最近一个月的收藏记录（收藏更能反映真实兴趣和出行意愿）
        QueryWrapper<UserCollect> collectFreqWrapper = new QueryWrapper<>();
        collectFreqWrapper.eq("user_id", userId);
        collectFreqWrapper.ge("create_time", LocalDateTime.now().minusMonths(1));
        Long recentCollects = userCollectMapper.selectCount(collectFreqWrapper);
        
        // 综合活跃天数和收藏数来判断消费频次
        int activeDayCount = activeDays.size();
        int collectCount = recentCollects != null ? recentCollects.intValue() : 0;
        
        // 消费频次（如果没有数据，返回空字符串）
        String frequency = "";
        if (activeDayCount == 0 && collectCount == 0) {
            frequency = ""; // 没有数据，返回空字符串
        } else {
            // 综合活跃天数和收藏数来判断消费频次
            // 活跃天数：反映用户浏览旅游内容的频率
            // 收藏数：反映用户对旅游内容的真实兴趣和出行意愿
            // 将收藏数转换为等效活跃天数（每2个收藏算1天活跃，因为收藏更能反映真实意愿）
            int equivalentDays = activeDayCount + (collectCount / 2);
            
            // 根据等效活跃天数判断消费频次（一个月约30天）
            // 15天以上：相当于每月有一半时间在关注旅游，说明出行频繁
            // 8-14天：相当于每月有1/4到1/2时间在关注，说明出行较频繁
            // 4-7天：相当于每月有1/8到1/4时间在关注，说明偶尔出行
            // 4天以下：偶尔关注，出行较少
            if (equivalentDays >= 15) {
                frequency = "每月3-4次";
            } else if (equivalentDays >= 8) {
                frequency = "每月2-3次";
            } else if (equivalentDays >= 4) {
                frequency = "每月1-2次";
            } else {
                frequency = "偶尔出行";
            }
        }
        
        // 价格敏感度（如果没有消费数据，返回空字符串）
        String priceSensitivity = "";
        if (avgConsumption.compareTo(BigDecimal.ZERO) > 0) {
            if (avgConsumption.compareTo(BigDecimal.valueOf(500)) < 0) {
                priceSensitivity = "高";
            } else if (avgConsumption.compareTo(BigDecimal.valueOf(1000)) < 0) {
                priceSensitivity = "中等";
            } else {
                priceSensitivity = "低";
            }
        }
        
        Map<String, Object> level = new HashMap<>();
        level.put("level", consumptionLevel);
        level.put("icon", levelIcon);
        level.put("color", levelColor);
        
        result.put("level", level);
        result.put("avgConsumption", avgConsumption.intValue());
        result.put("frequency", frequency);
        result.put("priceSensitivity", priceSensitivity);
        
        return result;
    }
    
    /**
     * 分析出行特征
     */
    private Map<String, Object> analyzeTravelCharacteristics(Long userId, User user) {
        Map<String, Object> result = new HashMap<>();
        
        // 使用Set去重浏览记录
        Set<String> planKeys = new HashSet<>();
        
        // 出行方式分析（只统计攻略，去重）
        QueryWrapper<UserBrowseHistory> browseWrapper = new QueryWrapper<>();
        browseWrapper.eq("user_id", userId);
        browseWrapper.eq("browse_type", 2); // 攻略
        List<UserBrowseHistory> planHistories = browseHistoryMapper.selectList(browseWrapper);
        
        String travelMode = ""; // 如果没有数据，返回空字符串
        if (!planHistories.isEmpty()) {
            int freeTravelCount = 0;
            int groupTravelCount = 0;
            
            for (UserBrowseHistory history : planHistories) {
                String key = "plan-" + history.getBrowseId();
                // 去重：同一个攻略只统计一次
                if (planKeys.contains(key)) {
                    continue;
                }
                planKeys.add(key);
                
                TravelPlan plan = travelPlanMapper.selectById(history.getBrowseId());
                if (plan != null && plan.getTravelType() != null) {
                    String travelType = plan.getTravelType();
                    if (travelType.contains("自由") || travelType.contains("自驾")) {
                        freeTravelCount++;
                    } else if (travelType.contains("跟团") || travelType.contains("团队")) {
                        groupTravelCount++;
                    }
                }
            }
            
            if (freeTravelCount > groupTravelCount) {
                travelMode = "自由行 + 自驾游";
            } else if (groupTravelCount > 0) {
                travelMode = "跟团游";
            } else {
                travelMode = ""; // 如果没有有效数据，返回空字符串
            }
        }
        
        // 旅游时长偏好（去重）
        String tripDuration = ""; // 如果没有数据，返回空字符串
        planKeys.clear(); // 重新使用Set去重
        if (!planHistories.isEmpty()) {
            List<Integer> daysList = new ArrayList<>();
            for (UserBrowseHistory history : planHistories) {
                String key = "plan-" + history.getBrowseId();
                // 去重：同一个攻略只统计一次
                if (planKeys.contains(key)) {
                    continue;
                }
                planKeys.add(key);
                
                TravelPlan plan = travelPlanMapper.selectById(history.getBrowseId());
                if (plan != null && plan.getDays() != null && plan.getDays() > 0) {
                    daysList.add(plan.getDays());
                }
            }
            
            if (!daysList.isEmpty()) {
                int avgDays = (int) Math.round(daysList.stream().mapToInt(Integer::intValue).average().orElse(0));
                if (avgDays > 0) {
                    if (avgDays <= 2) {
                        tripDuration = "1-2天";
                    } else if (avgDays <= 5) {
                        tripDuration = "3-5天";
                    } else if (avgDays <= 7) {
                        tripDuration = "5-7天";
                    } else {
                        tripDuration = "7天以上";
                    }
                }
            }
        }
        
        // 季节偏好（去重，统计攻略和景点的季节）
        String seasonPreference = ""; // 如果没有数据，返回空字符串
        planKeys.clear(); // 重新使用Set去重
        
        // 统计攻略的季节
        Map<String, Integer> seasonCount = new HashMap<>();
        seasonCount.put("春季", 0);
        seasonCount.put("夏季", 0);
        seasonCount.put("秋季", 0);
        seasonCount.put("冬季", 0);
        seasonCount.put("全年", 0);
        
        if (!planHistories.isEmpty()) {
            for (UserBrowseHistory history : planHistories) {
                String key = "plan-" + history.getBrowseId();
                // 去重：同一个攻略只统计一次
                if (planKeys.contains(key)) {
                    continue;
                }
                planKeys.add(key);
                
                TravelPlan plan = travelPlanMapper.selectById(history.getBrowseId());
                if (plan != null && plan.getBestSeason() != null) {
                    String season = plan.getBestSeason().trim();
                    if (!season.isEmpty()) {
                        // 如果包含"全年"，统计为"全年"
                        if (season.contains("全年") || season.equals("全年")) {
                            seasonCount.put("全年", seasonCount.get("全年") + 1);
                        } else {
                            // 否则统计具体季节
                            if (season.contains("春")) {
                                seasonCount.put("春季", seasonCount.get("春季") + 1);
                            }
                            if (season.contains("夏")) {
                                seasonCount.put("夏季", seasonCount.get("夏季") + 1);
                            }
                            if (season.contains("秋")) {
                                seasonCount.put("秋季", seasonCount.get("秋季") + 1);
                            }
                            if (season.contains("冬")) {
                                seasonCount.put("冬季", seasonCount.get("冬季") + 1);
                            }
                        }
                    }
                }
            }
        }
        
        // 统计景点的季节
        QueryWrapper<UserBrowseHistory> attractionWrapper = new QueryWrapper<>();
        attractionWrapper.eq("user_id", userId);
        attractionWrapper.eq("browse_type", 1); // 景点
        List<UserBrowseHistory> attractionHistories = browseHistoryMapper.selectList(attractionWrapper);
        
        Set<String> attractionKeys = new HashSet<>();
        if (!attractionHistories.isEmpty()) {
            for (UserBrowseHistory history : attractionHistories) {
                String key = "attraction-" + history.getBrowseId();
                // 去重：同一个景点只统计一次
                if (attractionKeys.contains(key)) {
                    continue;
                }
                attractionKeys.add(key);
                
                Attraction attraction = attractionMapper.selectById(history.getBrowseId());
                if (attraction != null && attraction.getBestSeason() != null) {
                    String season = attraction.getBestSeason().trim();
                    if (!season.isEmpty()) {
                        // 如果包含"全年"，统计为"全年"
                        if (season.contains("全年") || season.equals("全年")) {
                            seasonCount.put("全年", seasonCount.get("全年") + 1);
                        } else {
                            // 否则统计具体季节
                            if (season.contains("春")) {
                                seasonCount.put("春季", seasonCount.get("春季") + 1);
                            }
                            if (season.contains("夏")) {
                                seasonCount.put("夏季", seasonCount.get("夏季") + 1);
                            }
                            if (season.contains("秋")) {
                                seasonCount.put("秋季", seasonCount.get("秋季") + 1);
                            }
                            if (season.contains("冬")) {
                                seasonCount.put("冬季", seasonCount.get("冬季") + 1);
                            }
                        }
                    }
                }
            }
        }
        
        // 判断结果：如果全是"全年"，显示"全年"；否则显示具体季节
        int totalSeasons = seasonCount.values().stream().mapToInt(Integer::intValue).sum();
        if (totalSeasons == 0) {
            seasonPreference = ""; // 没有数据
        } else if (seasonCount.get("全年") == totalSeasons) {
            // 如果全部都是"全年"，显示"全年"
            seasonPreference = "全年";
        } else {
            // 否则显示具体季节（排除"全年"）
            List<String> topSeasons = seasonCount.entrySet().stream()
                .filter(e -> e.getValue() > 0 && !e.getKey().equals("全年")) // 排除"全年"
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
            if (!topSeasons.isEmpty()) {
                seasonPreference = String.join("、", topSeasons);
            } else {
                seasonPreference = ""; // 如果没有有效季节，返回空字符串
            }
        }
        
        // 常去目的地（去重，且只统计有城市名称的）
        String frequentDestinations = ""; // 如果没有数据，返回空字符串
        QueryWrapper<UserBrowseHistory> destWrapper = new QueryWrapper<>();
        destWrapper.eq("user_id", userId);
        List<UserBrowseHistory> allHistories = browseHistoryMapper.selectList(destWrapper);
        
        if (!allHistories.isEmpty()) {
            Map<String, Integer> cityCount = new HashMap<>();
            Set<String> cityKeys = new HashSet<>(); // 去重
            
            for (UserBrowseHistory history : allHistories) {
                String key = history.getBrowseType() + "-" + history.getBrowseId();
                // 去重：同一个景点/攻略只统计一次
                if (cityKeys.contains(key)) {
                    continue;
                }
                cityKeys.add(key);
                
                if (history.getBrowseType() == 1) { // 景点
                    Attraction attraction = attractionMapper.selectById(history.getBrowseId());
                    if (attraction != null && attraction.getCity() != null) {
                        String city = attraction.getCity().trim();
                        // 显示原始数据，不过滤
                        if (!city.isEmpty()) {
                            cityCount.put(city, cityCount.getOrDefault(city, 0) + 1);
                        }
                    }
                } else if (history.getBrowseType() == 2) { // 攻略
                    TravelPlan plan = travelPlanMapper.selectById(history.getBrowseId());
                    if (plan != null && plan.getDestination() != null) {
                        String dest = plan.getDestination().trim();
                        // 提取城市名（可能包含省份）
                        String city = "";
                        if (dest.contains("市")) {
                            city = dest.substring(0, dest.indexOf("市") + 1);
                        } else if (dest.contains("省")) {
                            // 如果只有省名，使用省名
                            city = dest;
                        } else {
                            city = dest;
                        }
                        // 显示原始数据，不过滤
                        if (!city.isEmpty()) {
                            cityCount.put(city, cityCount.getOrDefault(city, 0) + 1);
                        }
                    }
                }
            }
            
            if (!cityCount.isEmpty()) {
                List<String> topCities = cityCount.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
                
                if (!topCities.isEmpty()) {
                    frequentDestinations = String.join("、", topCities);
                }
            }
        }
        
        result.put("travelMode", travelMode);
        result.put("tripDuration", tripDuration);
        result.put("seasonPreference", seasonPreference);
        result.put("frequentDestinations", frequentDestinations);
        
        return result;
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 获取景点类型名称（与管理平台创建景点的类型对应）
     * 这些类型名称会用于：
     * 1. 景点推荐页面的筛选标签
     * 2. 用户画像的偏好统计
     * 3. 统计用户浏览相应类型景点的次数
     */
    private String getAttractionTypeName(Integer type) {
        switch (type) {
            case 1: return "自然风光";
            case 2: return "人文历史";
            case 3: return "主题公园";
            case 4: return "美食街";
            case 5: return "古镇古村";
            case 6: return "温泉度假";
            case 7: return "宗教场所";
            case 8: return "购物娱乐";
            default: return "其他";
        }
    }
    
    private String mapTagToPreference(String tag) {
        if (tag.contains("自然") || tag.contains("风景") || tag.contains("景观")) {
            return "自然风光";
        } else if (tag.contains("历史") || tag.contains("人文") || tag.contains("文化") || tag.contains("博物馆")) {
            return "人文历史";
        } else if (tag.contains("美食") || tag.contains("食物")) {
            return "美食体验";
        } else if (tag.contains("休闲") || tag.contains("度假") || tag.contains("温泉")) {
            return "休闲度假";
        }
        return null;
    }
    
    private String getPreferenceColor(String preference) {
        switch (preference) {
            case "自然风光":
                return "linear-gradient(135deg, #667eea 0%, #764ba2 100%)";
            case "人文历史":
                return "linear-gradient(135deg, #fa709a 0%, #fee140 100%)";
            case "主题公园":
                return "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)";
            case "美食街":
                return "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)";
            case "古镇古村":
                return "linear-gradient(135deg, #fad961 0%, #f76b1c 100%)";
            case "温泉度假":
                return "linear-gradient(135deg, #30cfd0 0%, #330867 100%)";
            case "宗教场所":
                return "linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)";
            case "购物娱乐":
                return "linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)";
            default:
                return "linear-gradient(135deg, #667eea 0%, #764ba2 100%)";
        }
    }
    
    private Map<String, Object> buildDefaultPreference() {
        Map<String, Object> result = new HashMap<>();
        result.put("primaryPreference", "自然风光");
        
        List<Map<String, Object>> distribution = new ArrayList<>();
        distribution.add(createPreferenceItem("自然风光", "🏞️", 25));
        distribution.add(createPreferenceItem("人文历史", "🏛️", 20));
        distribution.add(createPreferenceItem("主题公园", "🎢", 15));
        distribution.add(createPreferenceItem("美食街", "🍜", 15));
        distribution.add(createPreferenceItem("古镇古村", "🏘️", 10));
        distribution.add(createPreferenceItem("温泉度假", "♨️", 8));
        distribution.add(createPreferenceItem("宗教场所", "⛩️", 4));
        distribution.add(createPreferenceItem("购物娱乐", "🛍️", 3));
        
        result.put("distribution", distribution);
        return result;
    }
    
    private Map<String, Object> createPreferenceItem(String type, String icon, int percentage) {
        Map<String, Object> item = new HashMap<>();
        item.put("type", type);
        item.put("icon", icon);
        item.put("percentage", percentage);
        item.put("color", getPreferenceColor(type));
        return item;
    }
    
    private List<Map<String, Object>> buildDefaultInterestTags() {
        List<Map<String, Object>> tags = new ArrayList<>();
        tags.add(createTag("摄影", "📷", 10));
        tags.add(createTag("美食", "🍜", 9));
        tags.add(createTag("文化", "🎭", 8));
        tags.add(createTag("自然风光", "🏔️", 10));
        tags.add(createTag("历史", "📜", 7));
        tags.add(createTag("建筑", "🏛️", 6));
        tags.add(createTag("徒步", "🥾", 8));
        tags.add(createTag("温泉", "♨️", 5));
        tags.add(createTag("博物馆", "🏛️", 6));
        tags.add(createTag("夜生活", "🌙", 4));
        return tags;
    }
    
    private Map<String, Object> createTag(String name, String icon, int weight) {
        Map<String, Object> tag = new HashMap<>();
        tag.put("name", name);
        tag.put("icon", icon);
        tag.put("weight", weight);
        return tag;
    }
    
    /**
     * 构建空用户画像（所有字段为0或空，不使用默认值）
     */
    private Map<String, Object> buildEmptyPortrait() {
        Map<String, Object> result = new HashMap<>();
        result.put("browseCount", 0);
        result.put("favoriteCount", 0);
        result.put("searchCount", 0);
        result.put("avgStayTime", 0);
        result.put("primaryPreference", "");
        result.put("preferenceDistribution", new ArrayList<>());
        result.put("interestTags", new ArrayList<>());
        
        Map<String, Object> consumptionLevel = new HashMap<>();
        consumptionLevel.put("level", "");
        consumptionLevel.put("icon", "");
        consumptionLevel.put("color", "");
        result.put("consumptionLevel", consumptionLevel);
        result.put("avgConsumption", 0);
        result.put("consumptionFrequency", "");
        result.put("priceSensitivity", "");
        result.put("travelMode", "");
        result.put("tripDuration", "");
        result.put("seasonPreference", "");
        result.put("frequentDestinations", "");
        result.put("lastUpdateTime", LocalDateTime.now().toString().replace("T", " ").substring(0, 16));
        
        return result;
    }
    
    private Map<String, Object> buildDefaultPortrait() {
        Map<String, Object> result = new HashMap<>();
        result.put("browseCount", 141);
        result.put("favoriteCount", 11);
        result.put("searchCount", 68);
        result.put("avgStayTime", 15);
        result.putAll(buildDefaultPreference());
        result.put("interestTags", buildDefaultInterestTags());
        
        Map<String, Object> consumptionLevel = new HashMap<>();
        consumptionLevel.put("level", "品质舒适型");
        consumptionLevel.put("icon", "💳");
        consumptionLevel.put("color", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        result.put("consumptionLevel", consumptionLevel);
        result.put("avgConsumption", 800);
        result.put("consumptionFrequency", "每月2-3次");
        result.put("priceSensitivity", "中等");
        result.put("travelMode", "自由行 + 自驾游");
        result.put("tripDuration", "3-5天");
        result.put("seasonPreference", "春季、秋季");
        result.put("frequentDestinations", "重庆、成都、西安");
        result.put("lastUpdateTime", LocalDateTime.now().toString().replace("T", " ").substring(0, 16));
        
        return result;
    }
}

