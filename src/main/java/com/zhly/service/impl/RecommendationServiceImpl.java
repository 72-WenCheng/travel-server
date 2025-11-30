package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhly.entity.*;
import com.zhly.mapper.*;
import com.zhly.service.RecommendationService;
import com.zhly.service.AttractionService;
import com.zhly.service.TravelPlanService;
import com.zhly.service.CultureProjectService;
import com.zhly.service.UserService;
import com.zhly.user.service.UserPortraitService;
import com.zhly.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能推荐服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class RecommendationServiceImpl implements RecommendationService {
    
    @Autowired
    private AttractionService attractionService;
    
    @Autowired
    private TravelPlanService travelPlanService;
    
    @Autowired
    private CultureProjectService cultureProjectService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserPortraitService userPortraitService;
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    @Autowired
    private TravelPlanMapper travelPlanMapper;
    
    @Autowired
    private UserBrowseHistoryMapper browseHistoryMapper;
    
    @Autowired
    private UserCollectMapper userCollectMapper;
    
    @Autowired
    private com.zhly.service.UserCollectService userCollectService;
    
    @Autowired
    private RecommendationLogMapper recommendationLogMapper;
    
    @Override
    public List<Map<String, Object>> recommendAttractions(Long userId, String city, Integer limit) {
        try {
            // 1. 获取用户画像数据
            Map<String, Object> portrait = userPortraitService.getUserPortrait(userId);
            
            // 2. 基于用户画像的智能推荐（只返回符合用户画像的推荐，不降级）
            List<Map<String, Object>> profileBasedRecommendations = getProfileBasedRecommendations(userId, portrait, city, limit);
            
            // 3. 记录推荐日志（用于持续学习优化）
            // 按推荐分数排序
            List<Map<String, Object>> sortedRecommendations = profileBasedRecommendations.stream()
                    .sorted((a, b) -> Double.compare(
                            ((Number) b.getOrDefault("score", 0.0)).doubleValue(),
                            ((Number) a.getOrDefault("score", 0.0)).doubleValue()
                    ))
                    .limit(limit)
                    .collect(Collectors.toList());
            
            List<Map<String, Object>> result = sortedRecommendations;
            
            // 异步记录推荐日志，不阻塞主流程
            recordRecommendationLog(userId, result, "attraction");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("推荐景点失败: " + e.getMessage());
            e.printStackTrace();
            return getDefaultRecommendations("attraction", city, limit);
        }
    }
    
    @Override
    public List<Map<String, Object>> recommendPlans(Long userId, String destination, Integer limit) {
        try {
            // 1. 获取用户画像数据（使用完整的用户画像服务）
            Map<String, Object> portrait = userPortraitService.getUserPortrait(userId);
            
            // 2. 基于用户画像的智能推荐（偏好类型、兴趣标签等）
            List<Map<String, Object>> profileBasedRecommendations = getProfileBasedPlanRecommendations(userId, portrait, destination, limit);
            
            // 3. 基于价格匹配的推荐（消费水平）
            List<Map<String, Object>> priceBasedRecommendations = getPriceBasedPlanRecommendations(userId, portrait, destination, limit);
            
            // 4. 基于地理位置的推荐（常去目的地）
            List<Map<String, Object>> locationRecommendations = getLocationBasedPlanRecommendations(userId, portrait, destination, limit);
            
            // 5. 基于协同过滤推荐
            List<Map<String, Object>> collaborativeRecommendations = getCollaborativeRecommendations(userId, "plan", destination, limit);
            
            // 6. 混合推荐结果（加权：用户画像40%，价格匹配25%，地理位置20%，协同过滤10%，热度5%）
            List<Map<String, Object>> finalRecommendations = hybridRecommendationWithWeights5(
                profileBasedRecommendations, 0.4,
                priceBasedRecommendations, 0.25,
                locationRecommendations, 0.2,
                collaborativeRecommendations, 0.1,
                getHotPlansRecommendations(destination, limit), 0.05
            );
            
            // 7. 记录推荐日志（用于持续学习优化）
            List<Map<String, Object>> result = finalRecommendations.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
            
            // 异步记录推荐日志，不阻塞主流程
            recordRecommendationLog(userId, result, "plan");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("推荐攻略失败: " + e.getMessage());
            e.printStackTrace();
            return getDefaultRecommendations("plan", destination, limit);
        }
    }
    
    @Override
    public List<Map<String, Object>> collaborativeFiltering(Long userId, String type, Integer limit) {
        try {
            // 获取相似用户
            List<Long> similarUsers = findSimilarUsers(userId);
            
            // 基于相似用户的行为推荐
            List<Map<String, Object>> recommendations = new ArrayList<>();
            
            for (Long similarUserId : similarUsers) {
                List<Map<String, Object>> userItems = getUserItems(similarUserId, type);
                recommendations.addAll(userItems);
            }
            
            // 去重并排序
            return recommendations.stream()
                    .distinct()
                    .sorted((a, b) -> Double.compare(
                            (Double) b.getOrDefault("score", 0.0),
                            (Double) a.getOrDefault("score", 0.0)
                    ))
                    .limit(limit)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            System.err.println("协同过滤推荐失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> contentBasedRecommendation(Long userId, String type, Integer limit) {
        try {
            // 获取用户历史行为
            List<Map<String, Object>> userHistory = getUserHistory(userId, type);
            
            // 提取用户偏好特征
            Map<String, Double> userPreferences = extractUserPreferences(userHistory);
            
            // 基于内容相似度推荐
            List<Map<String, Object>> allItems = getAllItems(type);
            List<Map<String, Object>> recommendations = new ArrayList<>();
            
            for (Map<String, Object> item : allItems) {
                double similarity = calculateContentSimilarity(userPreferences, item);
                if (similarity > 0.3) { // 相似度阈值
                    item.put("score", similarity);
                    recommendations.add(item);
                }
            }
            
            return recommendations.stream()
                    .sorted((a, b) -> Double.compare(
                            (Double) b.get("score"),
                            (Double) a.get("score")
                    ))
                    .limit(limit)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            System.err.println("基于内容推荐失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> locationBasedRecommendation(Double latitude, Double longitude, String type, Integer limit) {
        try {
            // 基于地理位置推荐附近的景点或攻略
            List<Map<String, Object>> recommendations = new ArrayList<>();
            
            // 模拟地理位置推荐逻辑
            String[] nearbyCities = {"北京", "上海", "广州", "深圳", "杭州", "成都", "西安", "重庆"};
            Random random = new Random();
            
            for (int i = 0; i < limit; i++) {
                Map<String, Object> item = new HashMap<>();
                String city = nearbyCities[random.nextInt(nearbyCities.length)];
                
                if ("attraction".equals(type)) {
                    item.put("id", "loc_" + i);
                    item.put("name", city + "热门景点" + (i + 1));
                    item.put("type", "景点");
                    item.put("location", city);
                    item.put("distance", random.nextInt(50) + 1 + "km");
                } else if ("plan".equals(type)) {
                    item.put("id", "plan_" + i);
                    item.put("title", city + "旅游攻略" + (i + 1));
                    item.put("destination", city);
                    item.put("days", random.nextInt(5) + 1);
                }
                
                item.put("score", 0.8 - i * 0.1); // 距离越近分数越高
                recommendations.add(item);
            }
            
            return recommendations;
            
        } catch (Exception e) {
            System.err.println("基于地理位置推荐失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> timeBasedRecommendation(String season, String weather, String type, Integer limit) {
        try {
            List<Map<String, Object>> recommendations = new ArrayList<>();
            
            // 根据季节和天气推荐
            Map<String, List<String>> seasonRecommendations = getSeasonRecommendations();
            List<String> seasonItems = seasonRecommendations.getOrDefault(season, new ArrayList<>());
            
            Map<String, List<String>> weatherRecommendations = getWeatherRecommendations();
            List<String> weatherItems = weatherRecommendations.getOrDefault(weather, new ArrayList<>());
            
            // 合并推荐
            Set<String> combinedItems = new HashSet<>(seasonItems);
            combinedItems.addAll(weatherItems);
            
            Random random = new Random();
            for (String itemName : combinedItems) {
                if (recommendations.size() >= limit) break;
                
                Map<String, Object> item = new HashMap<>();
                item.put("id", "time_" + recommendations.size());
                item.put("name", itemName);
                item.put("season", season);
                item.put("weather", weather);
                item.put("score", 0.9 - recommendations.size() * 0.1);
                recommendations.add(item);
            }
            
            return recommendations;
            
        } catch (Exception e) {
            System.err.println("基于时间推荐失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> hybridRecommendation(Long userId, String type, Integer limit) {
        try {
            // 获取多种推荐结果
            List<Map<String, Object>> tagBased = getTagBasedRecommendations(userId, type, null, limit);
            List<Map<String, Object>> collaborative = getCollaborativeRecommendations(userId, type, null, limit);
            List<Map<String, Object>> contentBased = getContentRecommendations(null, type, limit);
            
            // 混合推荐算法
            return hybridRecommendation(tagBased, collaborative, contentBased).stream()
                    .limit(limit)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            System.err.println("混合推荐失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public String getRecommendationExplanation(Long userId, String itemId, String type) {
        try {
            // 生成推荐解释
            Map<String, Object> userProfile = getUserProfile(userId);
            StringBuilder explanation = new StringBuilder();
            
            explanation.append("为您推荐此").append("attraction".equals(type) ? "景点" : "攻略").append("，因为：");
            explanation.append("\n1. 基于您的浏览行为和偏好分析");
            explanation.append("\n2. 与您相似的用户也喜欢");
            explanation.append("\n3. 符合您的用户画像特征");
            
            return explanation.toString();
            
        } catch (Exception e) {
            return "推荐理由：基于您的偏好和相似用户的行为";
        }
    }
    
    @Override
    public boolean updateUserPreferences(Long userId, Map<String, Object> preferences) {
        try {
            // 更新用户偏好
            User user = userService.getById(userId);
            if (user != null) {
                // 更新用户标签和偏好
                String interestTags = (String) preferences.getOrDefault("interestTags", user.getInterestTags());
                Integer travelPreference = (Integer) preferences.getOrDefault("travelPreference", user.getTravelPreference());
                
                user.setInterestTags(interestTags);
                user.setTravelPreference(travelPreference);
                
                return userService.updateById(user);
            }
            return false;
            
        } catch (Exception e) {
            System.err.println("更新用户偏好失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getUserProfile(Long userId) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                return new HashMap<>();
            }
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("userId", userId);
            profile.put("interestTags", user.getInterestTags());
            profile.put("travelPreference", user.getTravelPreference());
            profile.put("age", user.getAge());
            profile.put("gender", user.getGender());
            profile.put("location", user.getFrequentCities());
            
            // 分析用户行为
            List<Map<String, Object>> userHistory = getUserHistory(userId, "all");
            profile.put("historyCount", userHistory.size());
            profile.put("preferredTypes", analyzePreferredTypes(userHistory));
            
            return profile;
            
        } catch (Exception e) {
            System.err.println("获取用户画像失败: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    // 私有辅助方法
    
    /**
     * 基于用户画像的智能推荐（功能1：智能景点推荐）
     */
    private List<Map<String, Object>> getProfileBasedRecommendations(Long userId, Map<String, Object> portrait, String city, Integer limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            // 获取用户偏好类型
            String primaryPreference = (String) portrait.getOrDefault("primaryPreference", "");
            List<Map<String, Object>> preferenceDistribution = (List<Map<String, Object>>) portrait.getOrDefault("preferenceDistribution", new ArrayList<>());
            List<Map<String, Object>> interestTags = (List<Map<String, Object>>) portrait.getOrDefault("interestTags", new ArrayList<>());
            String frequentDestinations = (String) portrait.getOrDefault("frequentDestinations", "");
            
            // 不再过滤已收藏的景点，因为收藏不应该影响基于用户画像的推荐
            // 用户画像推荐应该展示符合用户偏好的所有景点，包括已收藏的
            Set<Long> collectedAttractionIds = new HashSet<>(); // 保留变量但不使用，避免代码报错
            
            // 解析常去城市列表
            List<String> frequentCities = new ArrayList<>();
            if (!frequentDestinations.isEmpty()) {
                // 支持多种分隔符：顿号、逗号、空格
                String[] cities = frequentDestinations.split("[、，,\\s]+");
                for (String destCity : cities) {
                    if (!destCity.trim().isEmpty()) {
                        // 清理城市名称（移除"市"、"省"等后缀，但保留原始名称用于匹配）
                        String cleanCity = destCity.trim().replaceAll("市|省|自治区|特别行政区", "").trim();
                        if (!cleanCity.isEmpty()) {
                            frequentCities.add(cleanCity);
                        }
                    }
                }
            }
            
            // 如果没有传入city参数，但有常去城市，使用第一个常去城市作为默认城市
            String targetCity = city;
            if ((targetCity == null || targetCity.isEmpty()) && !frequentCities.isEmpty()) {
                targetCity = frequentCities.get(0);
                System.out.println("未指定城市，使用常去城市作为默认: " + targetCity);
            }
            
            System.out.println("========== 用户画像推荐开始 ==========");
            System.out.println("用户ID: " + userId);
            System.out.println("主要偏好类型: " + primaryPreference);
            System.out.println("偏好分布数量: " + preferenceDistribution.size());
            System.out.println("兴趣标签数量: " + interestTags.size());
            System.out.println("常去城市: " + (frequentDestinations.isEmpty() ? "无" : frequentDestinations));
            System.out.println("常去城市列表: " + frequentCities);
            System.out.println("目标城市: " + (targetCity == null || targetCity.isEmpty() ? "不限" : targetCity));
            System.out.println("注意: 已收藏的景点也会被推荐（收藏不影响用户画像推荐）");
            
            // 打印偏好分布详情
            System.out.println("偏好分布详情:");
            for (Map<String, Object> dist : preferenceDistribution) {
                String typeName = (String) dist.getOrDefault("type", "");
                Integer percentage = (Integer) dist.getOrDefault("percentage", 0);
                System.out.println("  - " + typeName + ": " + percentage + "%");
            }
            System.out.println("======================================");
            
            // 根据主要偏好类型推荐（只推荐一部分，为其他类型留出空间）
            int primaryPreferenceLimit = Math.max(1, limit / 2); // 主要偏好类型最多推荐limit的一半
            if (!primaryPreference.isEmpty()) {
                Integer preferredType = getTypeIdByName(primaryPreference);
                System.out.println("主要偏好类型: " + primaryPreference + ", 类型ID: " + preferredType + ", 推荐数量限制: " + primaryPreferenceLimit);
                if (preferredType != null) {
                    // 查询更多景点，确保有足够的结果
                            List<Attraction> attractions = getAttractionsByType(preferredType, targetCity, limit * 3);
                    System.out.println("查询到类型 " + preferredType + " (" + primaryPreference + ") 的景点数量: " + attractions.size() + ", 已收藏数量: " + collectedAttractionIds.size());
                    int primaryCount = 0;
                    for (Attraction attraction : attractions) {
                        // 严格验证：确保景点类型与查询类型一致
                        if (attraction.getType() == null || !attraction.getType().equals(preferredType)) {
                            System.out.println("警告: 景点类型不匹配 - " + attraction.getName() + " (ID: " + attraction.getId() + ", 期望类型: " + preferredType + ", 实际类型: " + attraction.getType() + ")，跳过");
                            continue;
                        }
                        
                        // 不再过滤已收藏的景点，收藏不影响用户画像推荐
                        Map<String, Object> item = convertAttractionToMap(attraction, userId);
                        
                        // 检查是否是常去城市，如果是则增加权重
                        double baseScore = 0.9;
                        String reason = "基于您的偏好类型：" + primaryPreference;
                        boolean isFrequentCity = isFrequentCity(attraction, frequentCities);
                        if (isFrequentCity) {
                            baseScore += 0.05; // 常去城市额外加分
                            reason += " · 您常去的城市";
                        }
                        
                        item.put("score", baseScore);
                        item.put("reason", reason);
                        recommendations.add(item);
                        primaryCount++;
                        System.out.println("添加推荐景点: " + attraction.getName() + " (ID: " + attraction.getId() + ", 类型: " + attraction.getType() + ", 城市: " + attraction.getCity() + (isFrequentCity ? " [常去城市]" : "") + ")");
                        if (primaryCount >= primaryPreferenceLimit || recommendations.size() >= limit) {
                            break;
                        }
                    }
                    System.out.println("主要偏好类型推荐完成，已推荐 " + primaryCount + " 个景点");
                } else {
                    System.out.println("警告: 无法找到偏好类型 " + primaryPreference + " 对应的类型ID");
                }
            } else {
                System.out.println("主要偏好类型为空");
            }
            
            // 根据偏好分布推荐（只推荐百分比>=5%的类型，最多推荐前3个，跳过主要偏好类型）
            if (recommendations.size() < limit) {
                System.out.println("开始使用偏好分布推荐，当前推荐数量: " + recommendations.size() + ", 目标数量: " + limit);
                int typeCount = 0;
                for (Map<String, Object> dist : preferenceDistribution) {
                    if (typeCount >= 3 || recommendations.size() >= limit) break;
                    // 修复：使用 "type" 字段而不是 "name" 字段
                    String typeName = (String) dist.getOrDefault("type", "");
                    Integer percentage = (Integer) dist.getOrDefault("percentage", 0);
                    System.out.println("处理偏好分布: " + typeName + " (" + percentage + "%)");
                    
                    // 跳过主要偏好类型（已经在上面推荐过了）
                    if (typeName.equals(primaryPreference)) {
                        System.out.println("跳过主要偏好类型: " + typeName + "（已在主要偏好推荐中处理）");
                        typeCount++; // 仍然计数，但不推荐
                        continue;
                    }
                    
                    // 只推荐百分比>=5%的类型，避免推荐百分比太低的类型
                    if (!typeName.isEmpty() && percentage >= 5) {
                        Integer typeId = getTypeIdByName(typeName);
                        if (typeId != null) {
                            // 计算该类型应该推荐的数量（根据剩余空间和百分比）
                            int remainingSlots = limit - recommendations.size();
                            int typeLimit = Math.max(1, (int) Math.ceil(remainingSlots * (percentage / 100.0)));
                            System.out.println("偏好分布 - 类型 " + typeId + " (" + typeName + ") 计划推荐 " + typeLimit + " 个景点");
                            
                            List<Attraction> attractions = getAttractionsByType(typeId, targetCity, typeLimit * 2);
                            System.out.println("偏好分布 - 查询到类型 " + typeId + " (" + typeName + ") 的景点数量: " + attractions.size());
                            int typeCountAdded = 0;
                            for (Attraction attraction : attractions) {
                                if (recommendations.size() >= limit) {
                                    System.out.println("推荐数量已达上限(" + limit + ")，停止添加");
                                    break;
                                }
                                
                                // 严格验证：确保景点类型与查询类型一致
                                if (attraction.getType() == null || !attraction.getType().equals(typeId)) {
                                    System.out.println("警告: 景点类型不匹配 - " + attraction.getName() + " (ID: " + attraction.getId() + ", 期望类型: " + typeId + ", 实际类型: " + attraction.getType() + ")，跳过");
                                    continue;
                                }
                                
                                // 不再过滤已收藏的景点，收藏不影响用户画像推荐
                                // 检查是否已在推荐列表中（避免重复）
                                boolean alreadyInList = recommendations.stream().anyMatch(r -> r.get("id").equals(attraction.getId()));
                                if (alreadyInList) {
                                    System.out.println("跳过已在推荐列表中的景点: " + attraction.getName() + " (ID: " + attraction.getId() + ")");
                                    continue;
                                }
                                
                                // 添加到推荐列表
                                Map<String, Object> item = convertAttractionToMap(attraction, userId);
                                
                                // 检查是否是常去城市，如果是则增加权重
                                double baseScore = 0.7 + (percentage / 100.0) * 0.2;
                                String reason = "基于您的偏好类型：" + typeName + "（" + percentage + "%）";
                                boolean isFrequentCity = isFrequentCity(attraction, frequentCities);
                                if (isFrequentCity) {
                                    baseScore += 0.05; // 常去城市额外加分
                                    reason += " · 您常去的城市";
                                }
                                
                                item.put("score", baseScore);
                                item.put("reason", reason);
                                recommendations.add(item);
                                typeCountAdded++;
                                System.out.println("✓ 添加偏好分布推荐景点: " + attraction.getName() + " (ID: " + attraction.getId() + ", 类型: " + attraction.getType() + ", 城市: " + attraction.getCity() + ", 分数: " + baseScore + (isFrequentCity ? " [常去城市]" : "") + ")");
                            }
                            System.out.println("偏好分布 - 类型 " + typeName + " 实际推荐了 " + typeCountAdded + " 个景点");
                        } else {
                            System.out.println("警告: 无法找到偏好类型 " + typeName + " 对应的类型ID");
                        }
                        typeCount++;
                    } else {
                        System.out.println("跳过百分比过低的类型: " + typeName + " (" + percentage + "%)");
                    }
                }
            }
            
            // 如果仍然不足，根据兴趣标签推荐（但只推荐与用户偏好类型相关的标签）
            if (recommendations.size() < limit) {
                System.out.println("推荐不足(" + recommendations.size() + ")，开始使用兴趣标签推荐");
                for (Map<String, Object> tag : interestTags) {
                    if (recommendations.size() >= limit) break;
                    String tagName = (String) tag.getOrDefault("name", "");
                    Integer weight = (Integer) tag.getOrDefault("weight", 0);
                    System.out.println("处理兴趣标签: " + tagName + " (权重: " + weight + ")");
                    if (!tagName.isEmpty() && weight > 0) {
                        List<Attraction> attractions = getAttractionsByTag(tagName, targetCity, limit);
                        System.out.println("兴趣标签 - 查询到标签 " + tagName + " 的景点数量: " + attractions.size());
                        for (Attraction attraction : attractions) {
                            // 不再过滤已收藏的景点，只检查是否已在推荐列表中（避免重复）
                            if (recommendations.stream().noneMatch(r -> r.get("id").equals(attraction.getId()))) {
                                // 检查景点类型是否在用户偏好分布中（且百分比>=5%）
                                Integer attractionType = attraction.getType();
                                boolean isPreferredType = false;
                                if (attractionType != null) {
                                    String typeName = getTypeNameById(attractionType);
                                    for (Map<String, Object> dist : preferenceDistribution) {
                                        String distTypeName = (String) dist.getOrDefault("type", "");
                                        Integer distPercentage = (Integer) dist.getOrDefault("percentage", 0);
                                        if (typeName.equals(distTypeName) && distPercentage >= 5) {
                                            isPreferredType = true;
                                            break;
                                        }
                                    }
                                }
                                
                                // 只推荐符合用户偏好类型的景点（通过标签匹配到的）
                                if (isPreferredType || primaryPreference.equals(getTypeNameById(attractionType))) {
                                    Map<String, Object> item = convertAttractionToMap(attraction, userId);
                                    
                                    // 检查是否是常去城市，如果是则增加权重
                                    double baseScore = 0.6 + Math.min(weight / 10.0, 0.2);
                                    String reason = "基于您的兴趣标签：" + tagName;
                                    boolean isFrequentCity = isFrequentCity(attraction, frequentCities);
                                    if (isFrequentCity) {
                                        baseScore += 0.05; // 常去城市额外加分
                                        reason += " · 您常去的城市";
                                    }
                                    
                                    item.put("score", baseScore);
                                    item.put("reason", reason);
                                    recommendations.add(item);
                                    System.out.println("添加兴趣标签推荐景点: " + attraction.getName() + " (ID: " + attraction.getId() + ", 类型: " + attraction.getType() + ", 城市: " + attraction.getCity() + (isFrequentCity ? " [常去城市]" : "") + ")");
                                    if (recommendations.size() >= limit) {
                                        break;
                                    }
                                } else {
                                    System.out.println("跳过不符合用户偏好的景点: " + attraction.getName() + " (类型: " + getTypeNameById(attractionType) + ")");
                                }
                            }
                        }
                    }
                }
            }
            
            System.out.println("========== 用户画像推荐结束 ==========");
            System.out.println("最终推荐结果数量: " + recommendations.size());
            System.out.println("推荐结果详情:");
            for (Map<String, Object> rec : recommendations) {
                System.out.println("  - " + rec.get("name") + " (类型ID: " + rec.get("type") + ", 分数: " + rec.get("score") + ", 原因: " + rec.get("reason") + ")");
            }
            System.out.println("======================================");
            
        } catch (Exception e) {
            System.err.println("基于用户画像推荐失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recommendations;
    }
    
    /**
     * 获取用户已收藏的景点ID（不过滤已浏览的）
     */
    private Set<Long> getCollectedAttractionIds(Long userId) {
        Set<Long> collectedIds = new HashSet<>();
        
        try {
            // 只从收藏记录获取
            QueryWrapper<UserCollect> collectWrapper = new QueryWrapper<>();
            collectWrapper.eq("user_id", userId);
            collectWrapper.eq("collect_type", 2); // 景点
            List<UserCollect> collects = userCollectMapper.selectList(collectWrapper);
            for (UserCollect collect : collects) {
                collectedIds.add(collect.getCollectId());
            }
        } catch (Exception e) {
            System.err.println("获取用户已收藏景点ID失败: " + e.getMessage());
        }
        
        return collectedIds;
    }
    
    /**
     * 基于价格匹配的推荐（功能3：精准价格匹配）
     */
    private List<Map<String, Object>> getPriceBasedRecommendations(Long userId, Map<String, Object> portrait, String city, Integer limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            // 获取用户平均消费水平
            Object avgConsumptionObj = portrait.get("avgConsumption");
            BigDecimal avgConsumption = BigDecimal.ZERO;
            if (avgConsumptionObj != null) {
                if (avgConsumptionObj instanceof BigDecimal) {
                    avgConsumption = (BigDecimal) avgConsumptionObj;
                } else if (avgConsumptionObj instanceof Number) {
                    avgConsumption = BigDecimal.valueOf(((Number) avgConsumptionObj).doubleValue());
                }
            }
            
            // 如果用户没有消费数据，返回空列表
            if (avgConsumption.compareTo(BigDecimal.ZERO) == 0) {
                return recommendations;
            }
            
            // 获取用户已浏览和收藏的景点ID
            Set<Long> viewedAttractionIds = getViewedAttractionIds(userId);
            
            // 价格范围：平均消费的50%-150%
            BigDecimal minPrice = avgConsumption.multiply(BigDecimal.valueOf(0.5));
            BigDecimal maxPrice = avgConsumption.multiply(BigDecimal.valueOf(1.5));
            
            // 查询符合价格范围的景点
            QueryWrapper<Attraction> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1); // 只推荐正常状态的景点
            wrapper.eq("deleted", 0).or().isNull("deleted");
            
            if (city != null && !city.isEmpty()) {
                // 统一处理城市名称：移除"市"、"省"等后缀，同时支持带"市"和不带"市"的匹配
                String cityNameWithoutSuffix = city.replaceAll("市|省|自治区|特别行政区", "").trim();
                String cityNameWithSuffix = cityNameWithoutSuffix + "市";
                // 同时支持完全匹配和模糊匹配，确保能匹配到"南宁"和"南宁市"
                wrapper.and(w -> w.eq("city", city)
                        .or().eq("city", cityNameWithoutSuffix)
                        .or().eq("city", cityNameWithSuffix)
                        .or().like("city", cityNameWithoutSuffix));
            }
            
            // 价格范围：ticket_price在minPrice和maxPrice之间，或者为0（免费）
            wrapper.and(w -> w.between("ticket_price", minPrice.doubleValue(), maxPrice.doubleValue())
                            .or()
                            .eq("ticket_price", 0));
            
            wrapper.orderByDesc("view_count", "collect_count");
            wrapper.last("LIMIT " + (limit * 2));
            
            List<Attraction> attractions = attractionMapper.selectList(wrapper);
            
            for (Attraction attraction : attractions) {
                if (!viewedAttractionIds.contains(attraction.getId())) {
                    BigDecimal ticketPrice = attraction.getTicketPrice() != null ? 
                        BigDecimal.valueOf(attraction.getTicketPrice().doubleValue()) : BigDecimal.ZERO;
                    
                    // 计算价格匹配度
                    double priceMatchScore = 1.0;
                    if (ticketPrice.compareTo(BigDecimal.ZERO) > 0) {
                        // 价格越接近平均消费，匹配度越高
                        double diff = Math.abs(ticketPrice.subtract(avgConsumption).doubleValue());
                        double maxDiff = avgConsumption.doubleValue();
                        priceMatchScore = Math.max(0.5, 1.0 - (diff / maxDiff));
                    }
                    
                    Map<String, Object> item = convertAttractionToMap(attraction, userId);
                    item.put("score", priceMatchScore * 0.8); // 价格匹配权重
                    item.put("reason", "符合您的消费水平（¥" + avgConsumption.intValue() + "/天）");
                    recommendations.add(item);
                }
            }
            
        } catch (Exception e) {
            System.err.println("基于价格匹配推荐失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recommendations;
    }
    
    /**
     * 基于地理位置的推荐（功能2：个性化内容 - 优先推荐常去目的地）
     */
    private List<Map<String, Object>> getLocationBasedRecommendations(Long userId, Map<String, Object> portrait, String city, Integer limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            // 获取用户常去目的地
            String frequentDestinations = (String) portrait.getOrDefault("frequentDestinations", "");
            Set<Long> viewedAttractionIds = getViewedAttractionIds(userId);
            
            // 优先推荐常去目的地的景点
            if (!frequentDestinations.isEmpty()) {
                String[] cities = frequentDestinations.split("、");
                for (String destCity : cities) {
                    if (destCity.isEmpty()) continue;
                    
                    // 清理城市名称（移除"市"、"省"等后缀）
                    destCity = destCity.replaceAll("市|省|自治区|特别行政区", "").trim();
                    
                    QueryWrapper<Attraction> wrapper = new QueryWrapper<>();
                    wrapper.eq("status", 1);
                    wrapper.eq("deleted", 0).or().isNull("deleted");
                    wrapper.like("city", destCity);
                    wrapper.orderByDesc("view_count", "like_count");
                    wrapper.last("LIMIT " + limit);
                    
                    List<Attraction> attractions = attractionMapper.selectList(wrapper);
                    for (Attraction attraction : attractions) {
                        if (!viewedAttractionIds.contains(attraction.getId()) && 
                            recommendations.stream().noneMatch(r -> r.get("id").equals(attraction.getId()))) {
                            Map<String, Object> item = convertAttractionToMap(attraction, userId);
                            item.put("score", 0.7);
                            item.put("reason", "您常去的目的地：" + destCity);
                            recommendations.add(item);
                        }
                    }
                }
            }
            
            // 如果指定了城市参数，也推荐该城市的景点
            if (city != null && !city.isEmpty() && 
                (frequentDestinations.isEmpty() || !frequentDestinations.contains(city))) {
                // 统一处理城市名称：移除"市"、"省"等后缀，同时支持带"市"和不带"市"的匹配
                String cityNameWithoutSuffix = city.replaceAll("市|省|自治区|特别行政区", "").trim();
                String cityNameWithSuffix = cityNameWithoutSuffix + "市";
                
                QueryWrapper<Attraction> wrapper = new QueryWrapper<>();
                wrapper.eq("status", 1);
                wrapper.eq("deleted", 0).or().isNull("deleted");
                // 同时支持完全匹配和模糊匹配，确保能匹配到"南宁"和"南宁市"
                wrapper.and(w -> w.eq("city", city)
                        .or().eq("city", cityNameWithoutSuffix)
                        .or().eq("city", cityNameWithSuffix)
                        .or().like("city", cityNameWithoutSuffix));
                wrapper.orderByDesc("view_count", "like_count");
                wrapper.last("LIMIT " + limit);
                
                List<Attraction> attractions = attractionMapper.selectList(wrapper);
                for (Attraction attraction : attractions) {
                    if (!viewedAttractionIds.contains(attraction.getId()) && 
                        recommendations.stream().noneMatch(r -> r.get("id").equals(attraction.getId()))) {
                        Map<String, Object> item = convertAttractionToMap(attraction, userId);
                        item.put("score", 0.6);
                        item.put("reason", "推荐城市：" + city);
                        recommendations.add(item);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("基于地理位置推荐失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recommendations;
    }
    
    /**
     * 获取热门景点推荐（作为补充）
     */
    private List<Map<String, Object>> getHotAttractionsRecommendations(String city, Integer limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            QueryWrapper<Attraction> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1);
            wrapper.eq("deleted", 0).or().isNull("deleted");
            
            if (city != null && !city.isEmpty()) {
                wrapper.like("city", city);
            }
            
            wrapper.orderByDesc("view_count", "collect_count");
            wrapper.last("LIMIT " + limit);
            
            List<Attraction> attractions = attractionMapper.selectList(wrapper);
            for (Attraction attraction : attractions) {
                Map<String, Object> item = convertAttractionToMap(attraction, null); // 热门推荐不需要userId
                item.put("score", 0.5);
                item.put("reason", "热门推荐");
            recommendations.add(item);
            }
        } catch (Exception e) {
            System.err.println("获取热门景点推荐失败: " + e.getMessage());
        }
        
        return recommendations;
    }
    
    // 辅助方法
    
    /**
     * 根据类型名称获取类型ID
     */
    private Integer getTypeIdByName(String typeName) {
        Map<String, Integer> typeMap = new HashMap<>();
        typeMap.put("自然风光", 1);
        typeMap.put("人文历史", 2);
        typeMap.put("主题公园", 3);
        typeMap.put("美食街", 4);
        typeMap.put("古镇古村", 5);
        typeMap.put("温泉度假", 6);
        typeMap.put("宗教场所", 7);
        typeMap.put("购物娱乐", 8);
        return typeMap.get(typeName);
    }
    
    /**
     * 根据类型ID获取类型名称
     */
    private String getTypeNameById(Integer typeId) {
        if (typeId == null) return "";
        Map<Integer, String> typeMap = new HashMap<>();
        typeMap.put(1, "自然风光");
        typeMap.put(2, "人文历史");
        typeMap.put(3, "主题公园");
        typeMap.put(4, "美食街");
        typeMap.put(5, "古镇古村");
        typeMap.put(6, "温泉度假");
        typeMap.put(7, "宗教场所");
        typeMap.put(8, "购物娱乐");
        return typeMap.getOrDefault(typeId, "");
    }
    
    /**
     * 检查景点是否在常去城市列表中
     */
    private boolean isFrequentCity(Attraction attraction, List<String> frequentCities) {
        if (frequentCities == null || frequentCities.isEmpty() || attraction == null) {
            return false;
        }
        
        String attractionCity = attraction.getCity();
        if (attractionCity == null || attractionCity.isEmpty()) {
            return false;
        }
        
        // 清理景点城市名称（移除"市"、"省"等后缀）
        String cleanAttractionCity = attractionCity.replaceAll("市|省|自治区|特别行政区", "").trim();
        
        // 检查是否匹配常去城市列表中的任何一个
        for (String frequentCity : frequentCities) {
            if (cleanAttractionCity.contains(frequentCity) || frequentCity.contains(cleanAttractionCity)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 根据类型获取景点列表
     */
    private List<Attraction> getAttractionsByType(Integer type, String city, Integer limit) {
        QueryWrapper<Attraction> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        // 修复：使用 and 包裹 deleted 条件，避免影响 type 条件
        wrapper.and(w -> w.eq("deleted", 0).or().isNull("deleted"));
        wrapper.eq("type", type);
        
        if (city != null && !city.isEmpty()) {
            // 统一处理城市名称：移除"市"、"省"等后缀，同时支持带"市"和不带"市"的匹配
            String cityNameWithoutSuffix = city.replaceAll("市|省|自治区|特别行政区", "").trim();
            String cityNameWithSuffix = cityNameWithoutSuffix + "市";
            // 同时支持完全匹配和模糊匹配，确保能匹配到"南宁"和"南宁市"
            wrapper.and(w -> w.eq("city", city)
                    .or().eq("city", cityNameWithoutSuffix)
                    .or().eq("city", cityNameWithSuffix)
                    .or().like("city", cityNameWithoutSuffix));
            System.out.println("查询条件: type=" + type + ", city=" + city + " (支持匹配: " + city + ", " + cityNameWithoutSuffix + ", " + cityNameWithSuffix + "), limit=" + limit);
        } else {
            System.out.println("查询条件: type=" + type + ", city=null, limit=" + limit);
        }
        
        wrapper.orderByDesc("view_count", "collect_count");
        wrapper.last("LIMIT " + limit);
        
        List<Attraction> attractions = attractionMapper.selectList(wrapper);
        System.out.println("数据库查询结果: 类型 " + type + " 的景点数量 = " + attractions.size());
        if (attractions.size() > 0) {
            System.out.println("查询到的景点示例: " + attractions.get(0).getName() + " (ID: " + attractions.get(0).getId() + ", 类型: " + attractions.get(0).getType() + ", 城市: " + attractions.get(0).getCity() + ")");
            // 打印所有查询结果的类型，用于调试
            for (Attraction att : attractions) {
                System.out.println("  - 景点: " + att.getName() + " (ID: " + att.getId() + ", 类型: " + att.getType() + ", 城市: " + att.getCity() + ")");
            }
        }
        return attractions;
    }
    
    /**
     * 根据标签获取景点列表
     */
    private List<Attraction> getAttractionsByTag(String tag, String city, Integer limit) {
        QueryWrapper<Attraction> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.eq("deleted", 0).or().isNull("deleted");
        wrapper.like("tags", tag);
        
        if (city != null && !city.isEmpty()) {
            // 统一处理城市名称：移除"市"、"省"等后缀，同时支持带"市"和不带"市"的匹配
            String cityNameWithoutSuffix = city.replaceAll("市|省|自治区|特别行政区", "").trim();
            String cityNameWithSuffix = cityNameWithoutSuffix + "市";
            // 同时支持完全匹配和模糊匹配，确保能匹配到"南宁"和"南宁市"
            wrapper.and(w -> w.eq("city", city)
                    .or().eq("city", cityNameWithoutSuffix)
                    .or().eq("city", cityNameWithSuffix)
                    .or().like("city", cityNameWithoutSuffix));
        }
        
        wrapper.orderByDesc("view_count", "collect_count");
        wrapper.last("LIMIT " + limit);
        
        return attractionMapper.selectList(wrapper);
    }
    
    /**
     * 获取用户已浏览和收藏的景点ID
     */
    private Set<Long> getViewedAttractionIds(Long userId) {
        Set<Long> viewedIds = new HashSet<>();
        
        try {
            // 从浏览历史获取
            QueryWrapper<UserBrowseHistory> browseWrapper = new QueryWrapper<>();
            browseWrapper.eq("user_id", userId);
            browseWrapper.eq("browse_type", 1); // 景点
            List<UserBrowseHistory> browseHistories = browseHistoryMapper.selectList(browseWrapper);
            for (UserBrowseHistory history : browseHistories) {
                viewedIds.add(history.getBrowseId());
            }
            
            // 从收藏记录获取
            QueryWrapper<UserCollect> collectWrapper = new QueryWrapper<>();
            collectWrapper.eq("user_id", userId);
            collectWrapper.eq("collect_type", 2); // 景点
            List<UserCollect> collects = userCollectMapper.selectList(collectWrapper);
            for (UserCollect collect : collects) {
                viewedIds.add(collect.getCollectId());
            }
        } catch (Exception e) {
            System.err.println("获取用户已浏览景点ID失败: " + e.getMessage());
        }
        
        return viewedIds;
    }
    
    /**
     * 将景点实体转换为Map（包含收藏状态）
     */
    private Map<String, Object> convertAttractionToMap(Attraction attraction, Long userId) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", attraction.getId());
        item.put("name", attraction.getName());
        item.put("description", attraction.getDescription());
        item.put("type", attraction.getType());
        item.put("city", attraction.getCity());
        item.put("province", attraction.getProvince());
        item.put("address", attraction.getAddress());
        item.put("coverImage", attraction.getCoverImage());
        item.put("images", attraction.getImages());
        item.put("ticketPrice", attraction.getTicketPrice());
        item.put("score", attraction.getScore());
        item.put("rating", attraction.getRating()); // 景区级别：1-5星
        item.put("viewCount", attraction.getViewCount());
        item.put("collectCount", attraction.getCollectCount());
        item.put("tags", attraction.getTags());
        
        // 检查收藏状态
        boolean isFavorite = false;
        if (userId != null) {
            try {
                isFavorite = userCollectService.isCollected(userId, 2, attraction.getId()); // 2表示景点
            } catch (Exception e) {
                // 忽略错误，默认为未收藏
                System.err.println("检查收藏状态失败: " + e.getMessage());
            }
        }
        item.put("isFavorite", isFavorite);
        
        return item;
    }
    
    /**
     * 加权混合推荐算法
     */
    private List<Map<String, Object>> hybridRecommendationWithWeights(
            List<Map<String, Object>> list1, double weight1,
            List<Map<String, Object>> list2, double weight2,
            List<Map<String, Object>> list3, double weight3,
            List<Map<String, Object>> list4, double weight4) {
        
        Map<Long, Map<String, Object>> itemScores = new HashMap<>();
        
        // 合并所有推荐结果，加权计算分数
        addRecommendationsWithWeight(itemScores, list1, weight1);
        addRecommendationsWithWeight(itemScores, list2, weight2);
        addRecommendationsWithWeight(itemScores, list3, weight3);
        addRecommendationsWithWeight(itemScores, list4, weight4);
        
        // 按分数排序
        return itemScores.values().stream()
                .sorted((a, b) -> Double.compare(
                        (Double) b.getOrDefault("score", 0.0),
                        (Double) a.getOrDefault("score", 0.0)
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * 混合推荐（支持5个列表）
     */
    private List<Map<String, Object>> hybridRecommendationWithWeights5(
            List<Map<String, Object>> list1, double weight1,
            List<Map<String, Object>> list2, double weight2,
            List<Map<String, Object>> list3, double weight3,
            List<Map<String, Object>> list4, double weight4,
            List<Map<String, Object>> list5, double weight5) {
        
        Map<Long, Map<String, Object>> itemScores = new HashMap<>();
        
        // 合并所有推荐结果，加权计算分数
        addRecommendationsWithWeight(itemScores, list1, weight1);
        addRecommendationsWithWeight(itemScores, list2, weight2);
        addRecommendationsWithWeight(itemScores, list3, weight3);
        addRecommendationsWithWeight(itemScores, list4, weight4);
        addRecommendationsWithWeight(itemScores, list5, weight5);
        
        // 按分数排序
        return itemScores.values().stream()
                .sorted((a, b) -> Double.compare(
                        (Double) b.getOrDefault("score", 0.0),
                        (Double) a.getOrDefault("score", 0.0)
                ))
                .collect(Collectors.toList());
    }
    
    private void addRecommendationsWithWeight(Map<Long, Map<String, Object>> itemScores, 
                                             List<Map<String, Object>> recommendations, 
                                             double weight) {
        for (Map<String, Object> item : recommendations) {
            Object idObj = item.get("id");
            if (idObj == null) {
                continue; // 跳过没有id的项
            }
            
            Long id;
            // 安全地转换id为Long类型
            if (idObj instanceof Number) {
                id = ((Number) idObj).longValue();
            } else if (idObj instanceof String) {
                // 如果是字符串，尝试解析（跳过非数字字符串的id）
                try {
                    id = Long.parseLong((String) idObj);
                } catch (NumberFormatException e) {
                    continue; // 跳过无法解析为Long的字符串id
                }
            } else {
                continue; // 跳过其他类型的id
            }
            
            // 安全地获取score
            double baseScore = 0.0;
            Object scoreObj = item.getOrDefault("score", 0.0);
            if (scoreObj instanceof Number) {
                baseScore = ((Number) scoreObj).doubleValue();
            } else if (scoreObj instanceof String) {
                try {
                    baseScore = Double.parseDouble((String) scoreObj);
                } catch (NumberFormatException e) {
                    baseScore = 0.0;
                }
            }
            
            double weightedScore = baseScore * weight;
            
            if (itemScores.containsKey(id)) {
                // 累加分数
                Map<String, Object> existing = itemScores.get(id);
                double existingScore = 0.0;
                Object existingScoreObj = existing.getOrDefault("score", 0.0);
                if (existingScoreObj instanceof Number) {
                    existingScore = ((Number) existingScoreObj).doubleValue();
                } else if (existingScoreObj instanceof String) {
                    try {
                        existingScore = Double.parseDouble((String) existingScoreObj);
                    } catch (NumberFormatException e) {
                        existingScore = 0.0;
                    }
                }
                existing.put("score", existingScore + weightedScore);
            } else {
                Map<String, Object> newItem = new HashMap<>(item);
                newItem.put("score", weightedScore);
                itemScores.put(id, newItem);
            }
        }
    }
    
    /**
     * 记录推荐日志（功能4：持续学习优化）
     */
    private void recordRecommendationLog(Long userId, List<Map<String, Object>> recommendations, String itemType) {
        try {
            // 异步记录，不阻塞主流程
            new Thread(() -> {
                try {
                    for (Map<String, Object> item : recommendations) {
                        RecommendationLog log = new RecommendationLog();
                        log.setUserId(userId);
                        log.setRecommendType(itemType.equals("attraction") ? 1 : 2); // 1-景点，2-攻略
                        log.setRecommendId(((Number) item.get("id")).longValue());
                        log.setRecommendReason((String) item.getOrDefault("reason", "智能推荐"));
                        log.setAlgorithm("hybrid"); // 混合推荐算法
                        log.setScore(BigDecimal.valueOf(((Number) item.getOrDefault("score", 0.0)).doubleValue()));
                        log.setIsClicked(0); // 初始为未点击
                        log.setIsLiked(0); // 初始为未喜欢
                        
                        recommendationLogMapper.insert(log);
                    }
                } catch (Exception e) {
                    System.err.println("记录推荐日志失败: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            System.err.println("启动推荐日志记录线程失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录用户反馈（点击、收藏、浏览时长等）
     * 用于持续学习和优化推荐算法
     */
    public void recordUserFeedback(Long userId, Integer recommendType, Long recommendId, 
                                  Integer feedbackType, Integer browseDuration) {
        try {
            // 查找对应的推荐记录
            QueryWrapper<RecommendationLog> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.eq("recommend_type", recommendType);
            wrapper.eq("recommend_id", recommendId);
            wrapper.orderByDesc("create_time");
            wrapper.last("LIMIT 1");
            
            RecommendationLog log = recommendationLogMapper.selectOne(wrapper);
            if (log != null) {
                // 更新反馈信息
                if (feedbackType == 1) { // 点击查看
                    log.setIsClicked(1);
                    log.setClickedTime(LocalDateTime.now());
                } else if (feedbackType == 2) { // 收藏
                    log.setIsLiked(1);
                }
                
                // 如果浏览时长超过30秒，视为积极反馈
                if (browseDuration != null && browseDuration > 30) {
                    log.setIsLiked(1);
                }
                
                recommendationLogMapper.updateById(log);
            } else {
                // 如果没有推荐记录，创建新记录
                RecommendationLog newLog = new RecommendationLog();
                newLog.setUserId(userId);
                newLog.setRecommendType(recommendType);
                newLog.setRecommendId(recommendId);
                newLog.setIsClicked(feedbackType == 1 ? 1 : 0);
                newLog.setIsLiked(feedbackType == 2 || (browseDuration != null && browseDuration > 30) ? 1 : 0);
                if (feedbackType == 1) {
                    newLog.setClickedTime(LocalDateTime.now());
                }
                recommendationLogMapper.insert(newLog);
            }
        } catch (Exception e) {
            System.err.println("记录用户反馈失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据用户反馈优化推荐权重（持续学习）
     */
    private void optimizeRecommendationWeights(Long userId) {
        try {
            // 查询用户最近30天的反馈数据
            QueryWrapper<RecommendationLog> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.ge("create_time", LocalDateTime.now().minusDays(30));
            
            List<RecommendationLog> logs = recommendationLogMapper.selectList(wrapper);
            
            // 分析反馈数据，调整推荐权重
            // 这里可以统计：哪些类型的推荐更受欢迎，哪些理由更有效等
            // 实际实现中，可以根据反馈数据动态调整推荐算法的权重
            
            System.out.println("用户 " + userId + " 的推荐反馈分析完成，共 " + logs.size() + " 条记录");
        } catch (Exception e) {
            System.err.println("优化推荐权重失败: " + e.getMessage());
        }
    }
    
    private List<Map<String, Object>> getTagBasedRecommendations(Long userId, String type, String location, Integer limit) {
        // 保留旧方法以兼容其他调用
        return getProfileBasedRecommendations(userId, userPortraitService.getUserPortrait(userId), location, limit);
    }
    
    private List<Map<String, Object>> getCollaborativeRecommendations(Long userId, String type, String location, Integer limit) {
        // 协同过滤推荐逻辑
        List<Map<String, Object>> recommendations = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < limit; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", "collab_" + i);
            item.put("name", "协同过滤推荐" + (i + 1));
            item.put("type", type);
            item.put("score", 0.7 - i * 0.1);
            recommendations.add(item);
        }
        
        return recommendations;
    }
    
    private List<Map<String, Object>> getContentRecommendations(String location, String type, Integer limit) {
        // 基于内容的推荐逻辑
        List<Map<String, Object>> recommendations = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < limit; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", "content_" + i);
            item.put("name", "基于内容推荐" + (i + 1));
            item.put("type", type);
            item.put("score", 0.6 - i * 0.1);
            recommendations.add(item);
        }
        
        return recommendations;
    }
    
    private List<Map<String, Object>> getLocationRecommendations(String location, String type, Integer limit) {
        // 基于地理位置的推荐逻辑
        return locationBasedRecommendation(39.9042, 116.4074, type, limit);
    }
    
    private List<Map<String, Object>> hybridRecommendation(List<Map<String, Object>>... recommendationLists) {
        // 混合推荐算法
        Map<String, Map<String, Object>> itemScores = new HashMap<>();
        
        for (List<Map<String, Object>> recommendations : recommendationLists) {
            for (Map<String, Object> item : recommendations) {
                String itemId = (String) item.get("id");
                double score = (Double) item.getOrDefault("score", 0.0);
                
                if (itemScores.containsKey(itemId)) {
                    // 加权平均
                    Map<String, Object> existing = itemScores.get(itemId);
                    double existingScore = (Double) existing.getOrDefault("score", 0.0);
                    existing.put("score", (existingScore + score) / 2);
                } else {
                    itemScores.put(itemId, new HashMap<>(item));
                }
            }
        }
        
        return itemScores.values().stream()
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("score"),
                        (Double) a.get("score")
                ))
                .collect(Collectors.toList());
    }
    
    private List<Long> findSimilarUsers(Long userId) {
        // 查找相似用户
        List<Long> similarUsers = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < 5; i++) {
            similarUsers.add((long) (random.nextInt(1000) + 1));
        }
        
        return similarUsers;
    }
    
    private List<Map<String, Object>> getUserItems(Long userId, String type) {
        // 获取用户项目
        List<Map<String, Object>> items = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < 3; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", "user_" + userId + "_" + i);
            item.put("name", "用户" + userId + "的项目" + (i + 1));
            item.put("type", type);
            item.put("score", random.nextDouble() * 0.5 + 0.5);
            items.add(item);
        }
        
        return items;
    }
    
    private List<Map<String, Object>> getUserHistory(Long userId, String type) {
        // 获取用户历史
        List<Map<String, Object>> history = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", "history_" + i);
            item.put("name", "历史项目" + (i + 1));
            item.put("type", type);
            item.put("score", random.nextDouble());
            history.add(item);
        }
        
        return history;
    }
    
    private Map<String, Double> extractUserPreferences(List<Map<String, Object>> userHistory) {
        // 提取用户偏好
        Map<String, Double> preferences = new HashMap<>();
        preferences.put("nature", 0.8);
        preferences.put("culture", 0.6);
        preferences.put("food", 0.7);
        preferences.put("adventure", 0.5);
        return preferences;
    }
    
    private List<Map<String, Object>> getAllItems(String type) {
        // 获取所有项目
        List<Map<String, Object>> items = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < 20; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", "item_" + i);
            item.put("name", "项目" + (i + 1));
            item.put("type", type);
            item.put("features", Arrays.asList("nature", "culture", "food", "adventure"));
            items.add(item);
        }
        
        return items;
    }
    
    private double calculateContentSimilarity(Map<String, Double> userPreferences, Map<String, Object> item) {
        // 计算内容相似度
        return Math.random() * 0.8 + 0.2;
    }
    
    private List<Map<String, Object>> getDefaultRecommendations(String type, String location, Integer limit) {
        // 默认推荐
        List<Map<String, Object>> recommendations = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < limit; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", "default_" + i);
            item.put("name", "默认推荐" + (i + 1));
            item.put("type", type);
            item.put("location", location);
            item.put("score", 0.5 - i * 0.05);
            recommendations.add(item);
        }
        
        return recommendations;
    }
    
    private Map<String, List<String>> getSeasonRecommendations() {
        Map<String, List<String>> recommendations = new HashMap<>();
        recommendations.put("春季", Arrays.asList("樱花观赏", "踏青赏花", "春游"));
        recommendations.put("夏季", Arrays.asList("避暑胜地", "海滨度假", "水上活动"));
        recommendations.put("秋季", Arrays.asList("红叶观赏", "秋游", "收获节"));
        recommendations.put("冬季", Arrays.asList("滑雪", "温泉", "雪景"));
        return recommendations;
    }
    
    private Map<String, List<String>> getWeatherRecommendations() {
        Map<String, List<String>> recommendations = new HashMap<>();
        recommendations.put("晴天", Arrays.asList("户外活动", "登山", "摄影"));
        recommendations.put("雨天", Arrays.asList("室内景点", "博物馆", "购物"));
        recommendations.put("雪天", Arrays.asList("滑雪", "温泉", "雪景摄影"));
        return recommendations;
    }
    
    private List<String> analyzePreferredTypes(List<Map<String, Object>> userHistory) {
        // 分析用户偏好类型
        return Arrays.asList("自然风光", "历史文化", "美食体验");
    }
    
    /**
     * 基于用户画像的攻略推荐
     */
    private List<Map<String, Object>> getProfileBasedPlanRecommendations(Long userId, Map<String, Object> portrait, String destination, Integer limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            // 获取用户偏好类型
            String primaryPreference = (String) portrait.getOrDefault("primaryPreference", "");
            List<Map<String, Object>> preferenceDistribution = (List<Map<String, Object>>) portrait.getOrDefault("preferenceDistribution", new ArrayList<>());
            List<Map<String, Object>> interestTags = (List<Map<String, Object>>) portrait.getOrDefault("interestTags", new ArrayList<>());
            
            // 获取用户已浏览和收藏的攻略ID（避免重复推荐）
            Set<Long> viewedPlanIds = getViewedPlanIds(userId);
            
            // 根据主要偏好类型推荐
            if (!primaryPreference.isEmpty()) {
                QueryWrapper<TravelPlan> wrapper = new QueryWrapper<>();
                wrapper.eq("status", 1);
                wrapper.eq("deleted", 0).or().isNull("deleted");
                wrapper.like("tags", primaryPreference);
                
                if (destination != null && !destination.isEmpty()) {
                    // 统一处理城市名称：移除"市"、"省"等后缀，同时支持带"市"和不带"市"的匹配
                    String cityNameWithoutSuffix = destination.replaceAll("市|省|自治区|特别行政区", "").trim();
                    String cityNameWithSuffix = cityNameWithoutSuffix + "市";
                    // 同时支持完全匹配和模糊匹配，确保能匹配到"南宁"和"南宁市"
                    wrapper.and(w -> w.eq("destination", destination)
                            .or().eq("destination", cityNameWithoutSuffix)
                            .or().eq("destination", cityNameWithSuffix)
                            .or().like("destination", cityNameWithoutSuffix));
                }
                
                wrapper.orderByDesc("view_count", "like_count");
                wrapper.last("LIMIT " + (limit * 2));
                
                List<TravelPlan> plans = travelPlanMapper.selectList(wrapper);
                for (TravelPlan plan : plans) {
                    if (!viewedPlanIds.contains(plan.getId())) {
                        Map<String, Object> item = convertPlanToMap(plan);
                        item.put("score", 0.9);
                        item.put("reason", "基于您的偏好类型：" + primaryPreference);
                        recommendations.add(item);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("基于用户画像推荐攻略失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recommendations;
    }
    
    /**
     * 基于价格匹配的攻略推荐
     */
    private List<Map<String, Object>> getPriceBasedPlanRecommendations(Long userId, Map<String, Object> portrait, String destination, Integer limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            // 获取用户平均消费水平
            Object avgConsumptionObj = portrait.get("avgConsumption");
            BigDecimal avgConsumption = BigDecimal.ZERO;
            if (avgConsumptionObj != null) {
                if (avgConsumptionObj instanceof BigDecimal) {
                    avgConsumption = (BigDecimal) avgConsumptionObj;
                } else if (avgConsumptionObj instanceof Number) {
                    avgConsumption = BigDecimal.valueOf(((Number) avgConsumptionObj).doubleValue());
                }
            }
            
            if (avgConsumption.compareTo(BigDecimal.ZERO) == 0) {
                return recommendations;
            }
            
            Set<Long> viewedPlanIds = getViewedPlanIds(userId);
            
            // 价格范围：平均消费的50%-150%（按天数计算）
            BigDecimal minBudget = avgConsumption.multiply(BigDecimal.valueOf(0.5));
            BigDecimal maxBudget = avgConsumption.multiply(BigDecimal.valueOf(1.5));
            
            QueryWrapper<TravelPlan> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1);
            wrapper.eq("deleted", 0).or().isNull("deleted");
            
            if (destination != null && !destination.isEmpty()) {
                wrapper.like("destination", destination);
            }
            
            // 预算范围：budget在minBudget和maxBudget之间，或者为0（未设置）
            wrapper.and(w -> w.between("budget", minBudget.doubleValue(), maxBudget.doubleValue())
                            .or()
                            .eq("budget", 0));
            
            wrapper.orderByDesc("view_count", "collect_count");
            wrapper.last("LIMIT " + (limit * 2));
            
            List<TravelPlan> plans = travelPlanMapper.selectList(wrapper);
            for (TravelPlan plan : plans) {
                if (!viewedPlanIds.contains(plan.getId())) {
                    Map<String, Object> item = convertPlanToMap(plan);
                    item.put("score", 0.8);
                    item.put("reason", "符合您的消费水平（¥" + avgConsumption.intValue() + "/天）");
                    recommendations.add(item);
                }
            }
            
        } catch (Exception e) {
            System.err.println("基于价格匹配推荐攻略失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recommendations;
    }
    
    /**
     * 基于地理位置的攻略推荐
     */
    private List<Map<String, Object>> getLocationBasedPlanRecommendations(Long userId, Map<String, Object> portrait, String destination, Integer limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            String frequentDestinations = (String) portrait.getOrDefault("frequentDestinations", "");
            Set<Long> viewedPlanIds = getViewedPlanIds(userId);
            
            if (!frequentDestinations.isEmpty()) {
                String[] cities = frequentDestinations.split("、");
                for (String destCity : cities) {
                    if (destCity.isEmpty()) continue;
                    // 创建final变量用于lambda表达式
                    final String cleanCity = destCity.replaceAll("市|省|自治区|特别行政区", "").trim();
                    
                    QueryWrapper<TravelPlan> wrapper = new QueryWrapper<>();
                    wrapper.eq("status", 1);
                    wrapper.eq("deleted", 0).or().isNull("deleted");
                    // 统一处理城市名称，同时支持带"市"和不带"市"的匹配
                    final String cityNameWithSuffix = cleanCity + "市";
                    wrapper.and(w -> w.eq("destination", cleanCity)
                            .or().eq("destination", cityNameWithSuffix)
                            .or().like("destination", cleanCity));
                    wrapper.orderByDesc("view_count", "like_count");
                    wrapper.last("LIMIT " + limit);
                    
                    List<TravelPlan> plans = travelPlanMapper.selectList(wrapper);
                    for (TravelPlan plan : plans) {
                        if (!viewedPlanIds.contains(plan.getId()) && 
                            recommendations.stream().noneMatch(r -> r.get("id").equals(plan.getId()))) {
                            Map<String, Object> item = convertPlanToMap(plan);
                            item.put("score", 0.7);
                            item.put("reason", "您常去的目的地：" + destCity);
                            recommendations.add(item);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("基于地理位置推荐攻略失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recommendations;
    }
    
    /**
     * 获取热门攻略推荐
     */
    private List<Map<String, Object>> getHotPlansRecommendations(String destination, Integer limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            QueryWrapper<TravelPlan> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 1);
            wrapper.eq("deleted", 0).or().isNull("deleted");
            
            if (destination != null && !destination.isEmpty()) {
                wrapper.like("destination", destination);
            }
            
            wrapper.orderByDesc("view_count", "collect_count");
            wrapper.last("LIMIT " + limit);
            
            List<TravelPlan> plans = travelPlanMapper.selectList(wrapper);
            for (TravelPlan plan : plans) {
                Map<String, Object> item = convertPlanToMap(plan);
                item.put("score", 0.5);
                item.put("reason", "热门推荐");
                recommendations.add(item);
            }
            
        } catch (Exception e) {
            System.err.println("获取热门攻略推荐失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recommendations;
    }
    
    /**
     * 获取用户已浏览的攻略ID
     */
    private Set<Long> getViewedPlanIds(Long userId) {
        Set<Long> planIds = new HashSet<>();
        try {
            QueryWrapper<UserBrowseHistory> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.eq("browse_type", 2); // 2表示攻略
            wrapper.select("browse_id");
            
            List<UserBrowseHistory> histories = browseHistoryMapper.selectList(wrapper);
            for (UserBrowseHistory history : histories) {
                planIds.add(history.getBrowseId());
            }
        } catch (Exception e) {
            System.err.println("获取已浏览攻略ID失败: " + e.getMessage());
        }
        return planIds;
    }
    
    /**
     * 将攻略转换为Map
     */
    private Map<String, Object> convertPlanToMap(TravelPlan plan) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", plan.getId());
        item.put("title", plan.getTitle());
        item.put("destination", plan.getDestination());
        item.put("budget", plan.getBudget());
        item.put("days", plan.getDays());
        item.put("coverImage", plan.getCoverImage());
        item.put("viewCount", plan.getViewCount());
        item.put("likeCount", plan.getLikeCount() != null ? plan.getLikeCount() : 0);
        item.put("tags", plan.getTags());
        item.put("authorId", plan.getAuthorId());
        item.put("createTime", plan.getCreateTime());
        return item;
    }
    
    /**
     * 根据相同/相似地点推荐相关攻略
     */
    @Override
    public List<Map<String, Object>> getRelatedPlansByDestination(Long currentPlanId, String destination, Integer limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            if (destination == null || destination.trim().isEmpty()) {
                System.out.println("getRelatedPlansByDestination: destination为空，返回空列表");
                return recommendations;
            }
            
            // 清理destination，移除可能的空格和特殊字符
            String cleanDestination = destination.trim();
            // 统一处理城市名称：移除"市"、"省"等后缀，同时保留原值用于匹配
            String cityNameWithoutSuffix = cleanDestination.replaceAll("市|省|自治区|特别行政区", "").trim();
            String cityNameWithSuffix = cityNameWithoutSuffix + "市";
            System.out.println("getRelatedPlansByDestination: 当前攻略ID=" + currentPlanId + ", 目的地=" + cleanDestination + ", 清理后=" + cityNameWithoutSuffix + ", 限制=" + limit);
            
            QueryWrapper<TravelPlan> wrapper = new QueryWrapper<>();
            // 排除当前攻略
            if (currentPlanId != null) {
                wrapper.ne("id", currentPlanId);
            }
            // 只查询已发布且已审核通过的攻略
            wrapper.eq("status", 1);
            wrapper.eq("audit_status", 1);
            wrapper.eq("deleted", 0).or().isNull("deleted");
            
            // 根据目的地匹配：完全匹配优先，模糊匹配次之
            // 同时支持带"市"和不带"市"的匹配（如"南宁"和"南宁市"）
            // 先查询完全匹配的
            QueryWrapper<TravelPlan> exactMatchWrapper = new QueryWrapper<>();
            // 复制基础条件
            if (currentPlanId != null) {
                exactMatchWrapper.ne("id", currentPlanId);
            }
            exactMatchWrapper.eq("status", 1);
            exactMatchWrapper.eq("audit_status", 1);
            exactMatchWrapper.eq("deleted", 0).or().isNull("deleted");
            exactMatchWrapper.and(w -> w.eq("destination", cleanDestination)
                    .or().eq("destination", cityNameWithoutSuffix)
                    .or().eq("destination", cityNameWithSuffix));
            exactMatchWrapper.orderByDesc("view_count", "like_count", "create_time");
            exactMatchWrapper.last("LIMIT " + limit);
            
            List<TravelPlan> exactMatchPlans = travelPlanMapper.selectList(exactMatchWrapper);
            System.out.println("getRelatedPlansByDestination: 完全匹配的攻略数量=" + exactMatchPlans.size());
            
            // 去重：确保没有重复的攻略
            Set<Long> foundPlanIds = exactMatchPlans.stream()
                    .map(TravelPlan::getId)
                    .collect(Collectors.toSet());
            
            // 如果完全匹配的数量不够，再查询模糊匹配的
            if (exactMatchPlans.size() < limit) {
                QueryWrapper<TravelPlan> likeMatchWrapper = new QueryWrapper<>();
                // 复制基础条件
                if (currentPlanId != null) {
                    likeMatchWrapper.ne("id", currentPlanId);
                }
                likeMatchWrapper.eq("status", 1);
                likeMatchWrapper.eq("audit_status", 1);
                likeMatchWrapper.eq("deleted", 0).or().isNull("deleted");
                // 排除已经找到的完全匹配的攻略
                if (!foundPlanIds.isEmpty()) {
                    likeMatchWrapper.notIn("id", foundPlanIds);
                }
                // 模糊匹配时也支持带"市"和不带"市"的匹配
                likeMatchWrapper.and(w -> w.like("destination", cleanDestination)
                        .or().like("destination", cityNameWithoutSuffix)
                        .or().like("destination", cityNameWithSuffix));
                likeMatchWrapper.orderByDesc("view_count", "like_count", "create_time");
                likeMatchWrapper.last("LIMIT " + (limit - exactMatchPlans.size()));
                
                List<TravelPlan> likeMatchPlans = travelPlanMapper.selectList(likeMatchWrapper);
                
                // 去重：确保模糊匹配的结果中没有重复
                List<TravelPlan> uniqueLikeMatchPlans = likeMatchPlans.stream()
                        .filter(plan -> !foundPlanIds.contains(plan.getId()))
                        .collect(Collectors.toList());
                
                // 更新已找到的ID集合
                uniqueLikeMatchPlans.forEach(plan -> foundPlanIds.add(plan.getId()));
                
                // 合并结果：完全匹配的在前，模糊匹配的在后
                List<TravelPlan> allPlans = new ArrayList<>(exactMatchPlans);
                allPlans.addAll(uniqueLikeMatchPlans);
                
                // 转换为Map并计算相关度分数
                recommendations = allPlans.stream()
                        .map(plan -> {
                            Map<String, Object> item = convertPlanToMap(plan);
                            // 计算相关度分数
                            double relevanceScore = 0.0;
                            
                            // 目的地完全匹配 +10分
                            if (destination.equals(plan.getDestination())) {
                                relevanceScore += 10.0;
                            } else if (plan.getDestination() != null && plan.getDestination().contains(destination)) {
                                // 模糊匹配 +5分
                                relevanceScore += 5.0;
                            }
                            
                            // 浏览量加分（每1000次浏览 +1分）
                            relevanceScore += (plan.getViewCount() != null ? plan.getViewCount() : 0) / 1000.0;
                            
                            // 收藏数加分（每个收藏 +2分）
                            relevanceScore += (plan.getLikeCount() != null ? plan.getLikeCount() : 0) * 2.0;
                            
                            // 最近创建的加分（30天内 +2分）
                            if (plan.getCreateTime() != null) {
                                long daysSinceCreation = java.time.temporal.ChronoUnit.DAYS.between(
                                        plan.getCreateTime(), LocalDateTime.now());
                                if (daysSinceCreation <= 30) {
                                    relevanceScore += 2.0;
                                }
                            }
                            
                            item.put("relevanceScore", relevanceScore);
                            return item;
                        })
                        .sorted((a, b) -> {
                            // 按相关度分数降序排序
                            Double scoreA = (Double) a.get("relevanceScore");
                            Double scoreB = (Double) b.get("relevanceScore");
                            return scoreB.compareTo(scoreA);
                        })
                        .limit(limit)
                        .collect(Collectors.toList());
            } else {
                // 如果完全匹配的数量足够，直接转换
                recommendations = exactMatchPlans.stream()
                        .map(plan -> {
                            Map<String, Object> item = convertPlanToMap(plan);
                            double relevanceScore = 10.0; // 完全匹配基础分
                            relevanceScore += (plan.getViewCount() != null ? plan.getViewCount() : 0) / 1000.0;
                            relevanceScore += (plan.getLikeCount() != null ? plan.getLikeCount() : 0) * 2.0;
                            if (plan.getCreateTime() != null) {
                                long daysSinceCreation = java.time.temporal.ChronoUnit.DAYS.between(
                                        plan.getCreateTime(), LocalDateTime.now());
                                if (daysSinceCreation <= 30) {
                                    relevanceScore += 2.0;
                                }
                            }
                            item.put("relevanceScore", relevanceScore);
                            return item;
                        })
                        .sorted((a, b) -> {
                            Double scoreA = (Double) a.get("relevanceScore");
                            Double scoreB = (Double) b.get("relevanceScore");
                            return scoreB.compareTo(scoreA);
                        })
                        .limit(limit)
                        .collect(Collectors.toList());
            }
            
        } catch (Exception e) {
            System.err.println("获取相关攻略失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recommendations;
    }
    
    /**
     * 根据相同/相似地点推荐相关景点（按热度排序）
     */
    @Override
    public List<Map<String, Object>> getRelatedAttractionsByDestination(String destination, Integer limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            if (destination == null || destination.trim().isEmpty()) {
                System.out.println("getRelatedAttractionsByDestination: destination为空，返回空列表");
                return recommendations;
            }
            
            // 清理destination，移除可能的空格和特殊字符
            String cleanDestination = destination.trim();
            // 统一处理城市名称：移除"市"、"省"等后缀，同时保留原值用于匹配
            String cityNameWithoutSuffix = cleanDestination.replaceAll("市|省|自治区|特别行政区", "").trim();
            String cityNameWithSuffix = cityNameWithoutSuffix + "市";
            System.out.println("getRelatedAttractionsByDestination: 目的地=" + cleanDestination + ", 清理后=" + cityNameWithoutSuffix + ", 限制=" + limit);
            
            QueryWrapper<Attraction> wrapper = new QueryWrapper<>();
            // 只查询已发布的景点
            wrapper.eq("status", 1);
            wrapper.eq("deleted", 0).or().isNull("deleted");
            
            // 根据地点匹配：支持city、province、address字段匹配
            // 同时支持带"市"和不带"市"的匹配（如"南宁"和"南宁市"）
            // 先查询完全匹配city的
            QueryWrapper<Attraction> exactMatchWrapper = new QueryWrapper<>();
            exactMatchWrapper.eq("status", 1);
            exactMatchWrapper.eq("deleted", 0).or().isNull("deleted");
            exactMatchWrapper.and(w -> w.eq("city", cleanDestination)
                    .or().eq("city", cityNameWithoutSuffix)
                    .or().eq("city", cityNameWithSuffix)
                    .or().eq("province", cleanDestination)
                    .or().eq("province", cityNameWithoutSuffix)
                    .or().like("address", cleanDestination)
                    .or().like("address", cityNameWithoutSuffix));
            // 按热度排序：浏览量、收藏数、评分
            exactMatchWrapper.orderByDesc("view_count", "collect_count", "score");
            exactMatchWrapper.last("LIMIT " + limit);
            
            List<Attraction> exactMatchAttractions = attractionMapper.selectList(exactMatchWrapper);
            System.out.println("getRelatedAttractionsByDestination: 完全匹配的景点数量=" + exactMatchAttractions.size());
            
            // 去重：确保没有重复的景点
            Set<Long> foundAttractionIds = exactMatchAttractions.stream()
                    .map(Attraction::getId)
                    .collect(Collectors.toSet());
            
            // 如果完全匹配的数量不够，再查询模糊匹配的
            if (exactMatchAttractions.size() < limit) {
                QueryWrapper<Attraction> likeMatchWrapper = new QueryWrapper<>();
                likeMatchWrapper.eq("status", 1);
                likeMatchWrapper.eq("deleted", 0).or().isNull("deleted");
                // 排除已经找到的完全匹配的景点
                if (!foundAttractionIds.isEmpty()) {
                    likeMatchWrapper.notIn("id", foundAttractionIds);
                }
                // 模糊匹配时也支持带"市"和不带"市"的匹配
                likeMatchWrapper.and(w -> w.like("city", cleanDestination)
                        .or().like("city", cityNameWithoutSuffix)
                        .or().like("city", cityNameWithSuffix)
                        .or().like("province", cleanDestination)
                        .or().like("province", cityNameWithoutSuffix)
                        .or().like("address", cleanDestination)
                        .or().like("address", cityNameWithoutSuffix));
                likeMatchWrapper.orderByDesc("view_count", "collect_count", "score");
                likeMatchWrapper.last("LIMIT " + (limit - exactMatchAttractions.size()));
                
                List<Attraction> likeMatchAttractions = attractionMapper.selectList(likeMatchWrapper);
                
                // 去重：确保模糊匹配的结果中没有重复
                List<Attraction> uniqueLikeMatchAttractions = likeMatchAttractions.stream()
                        .filter(attraction -> !foundAttractionIds.contains(attraction.getId()))
                        .collect(Collectors.toList());
                
                // 更新已找到的ID集合
                uniqueLikeMatchAttractions.forEach(attraction -> foundAttractionIds.add(attraction.getId()));
                
                // 合并结果：完全匹配的在前，模糊匹配的在后
                List<Attraction> allAttractions = new ArrayList<>(exactMatchAttractions);
                allAttractions.addAll(uniqueLikeMatchAttractions);
                
                // 转换为Map并按热度排序
                recommendations = allAttractions.stream()
                        .map(attraction -> {
                            Map<String, Object> item = convertAttractionToMap(attraction, null);
                            // 计算热度分数
                            double popularityScore = 0.0;
                            
                            // 浏览量权重：每1000次浏览 +1分
                            popularityScore += (attraction.getViewCount() != null ? attraction.getViewCount() : 0) / 1000.0;
                            
                            // 收藏数权重：每个收藏 +2分
                            popularityScore += (attraction.getCollectCount() != null ? attraction.getCollectCount() : 0) * 2.0;
                            
                            // 评分权重：评分 * 10分
                            if (attraction.getScore() != null) {
                                popularityScore += attraction.getScore().doubleValue() * 10.0;
                            }
                            
                            // 景区级别权重：5A景区 +5分，4A景区 +3分，3A景区 +1分
                            if (attraction.getRating() != null) {
                                switch (attraction.getRating()) {
                                    case 5:
                                        popularityScore += 5.0;
                                        break;
                                    case 4:
                                        popularityScore += 3.0;
                                        break;
                                    case 3:
                                        popularityScore += 1.0;
                                        break;
                                }
                            }
                            
                            item.put("popularityScore", popularityScore);
                            return item;
                        })
                        .sorted((a, b) -> {
                            // 按热度分数降序排序
                            Double scoreA = (Double) a.get("popularityScore");
                            Double scoreB = (Double) b.get("popularityScore");
                            return scoreB.compareTo(scoreA);
                        })
                        .limit(limit)
                        .collect(Collectors.toList());
            } else {
                // 如果完全匹配的数量足够，直接转换并按热度排序
                recommendations = exactMatchAttractions.stream()
                        .map(attraction -> {
                            Map<String, Object> item = convertAttractionToMap(attraction, null);
                            double popularityScore = 0.0;
                            popularityScore += (attraction.getViewCount() != null ? attraction.getViewCount() : 0) / 1000.0;
                            popularityScore += (attraction.getCollectCount() != null ? attraction.getCollectCount() : 0) * 2.0;
                            // 评分权重：评分 * 10分
                            if (attraction.getScore() != null) {
                                popularityScore += attraction.getScore().doubleValue() * 10.0;
                            }
                            if (attraction.getRating() != null) {
                                switch (attraction.getRating()) {
                                    case 5:
                                        popularityScore += 5.0;
                                        break;
                                    case 4:
                                        popularityScore += 3.0;
                                        break;
                                    case 3:
                                        popularityScore += 1.0;
                                        break;
                                }
                            }
                            item.put("popularityScore", popularityScore);
                            return item;
                        })
                        .sorted((a, b) -> {
                            Double scoreA = (Double) a.get("popularityScore");
                            Double scoreB = (Double) b.get("popularityScore");
                            return scoreB.compareTo(scoreA);
                        })
                        .limit(limit)
                        .collect(Collectors.toList());
            }
            
        } catch (Exception e) {
            System.err.println("获取相关景点失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return recommendations;
    }
}
