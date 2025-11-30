package com.zhly.user.controller;

import com.zhly.common.Result;
import com.zhly.entity.Attraction;
import com.zhly.entity.TravelPlan;
import com.zhly.service.AttractionService;
import com.zhly.service.UserCollectService;
import com.zhly.service.TravelPlanService;
import com.zhly.service.impl.SearchServiceImpl;
import com.zhly.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户端景点控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/user/attraction")
public class UserAttractionController {
    
    private static final int MAX_RECOMMENDATION_LIMIT = 5;
    private static final double MAX_NEARBY_DISTANCE_KM = 600D;
    
    @Autowired
    private AttractionService attractionService;
    
    @Autowired
    private UserCollectService userCollectService;
    
    @Autowired
    private TravelPlanService travelPlanService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private SearchServiceImpl searchServiceImpl;
    
    private static final Map<String, List<String>> PROVINCE_NEIGHBORS = createProvinceNeighborMap();
    
    /**
     * 根据景点获取相关攻略
     */
    @GetMapping("/plan/by-attraction")
    public Result<List<Map<String, Object>>> getPlansByAttraction(
            @RequestParam Long attractionId,
            @RequestParam(defaultValue = "5") Integer limit) {
        try {
            int maxLimit = limit != null && limit > 0 ? Math.min(limit, MAX_RECOMMENDATION_LIMIT) : MAX_RECOMMENDATION_LIMIT;
            Attraction attraction = attractionService.getAttractionById(attractionId);
            if (attraction == null) {
                return Result.error("景点不存在");
            }
            
            int fetchLimit = maxLimit * 3;
            Map<Long, TravelPlan> candidateMap = new LinkedHashMap<>();
            
            if (StringUtils.hasText(attraction.getCity())) {
                travelPlanService.lambdaQuery()
                    .eq(TravelPlan::getStatus, 1)
                    .eq(TravelPlan::getAuditStatus, 1)
                    .like(TravelPlan::getDestination, attraction.getCity())
                    .last("LIMIT " + fetchLimit)
                    .list()
                    .forEach(plan -> candidateMap.putIfAbsent(plan.getId(), plan));
            }
            
            if (candidateMap.size() < maxLimit && StringUtils.hasText(attraction.getProvince())) {
                travelPlanService.lambdaQuery()
                    .eq(TravelPlan::getStatus, 1)
                    .eq(TravelPlan::getAuditStatus, 1)
                    .like(TravelPlan::getDestination, attraction.getProvince())
                    .last("LIMIT " + fetchLimit)
                    .list()
                    .forEach(plan -> candidateMap.putIfAbsent(plan.getId(), plan));
            }
            
            List<Map<String, Object>> resultList = candidateMap.values().stream()
                .sorted(Comparator
                    .comparingInt((TravelPlan plan) -> safeValue(plan.getLikeCount())).reversed()
                    .thenComparing(Comparator.comparingInt((TravelPlan plan) -> safeValue(plan.getViewCount())).reversed()))
                .limit(maxLimit)
                .map(plan -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", plan.getId());
                    item.put("title", plan.getTitle());
                    item.put("coverImage", extractPlanCover(plan));
                    item.put("viewCount", safeValue(plan.getViewCount()));
                    item.put("likeCount", safeValue(plan.getLikeCount()));
                    return item;
                })
                .collect(Collectors.toList());
            
            return Result.success("获取相关攻略成功", resultList);
        } catch (Exception e) {
            return Result.error("获取相关攻略失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取景点详情
     */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getAttractionDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            Attraction attraction = attractionService.getAttractionById(id);
            if (attraction == null) {
                return Result.notFound("景点不存在");
            }
            
            // 重新统计收藏数，防止缓存字段与实际收藏记录不一致
            long realCollectCount = 0L;
            try {
                realCollectCount = userCollectService.lambdaQuery()
                        .eq(com.zhly.entity.UserCollect::getCollectType, 2)
                        .eq(com.zhly.entity.UserCollect::getCollectId, id)
                        .count();
            } catch (Exception e) {
                // 统计失败时使用原有字段
                realCollectCount = attraction.getCollectCount() != null ? attraction.getCollectCount() : 0;
            }

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("id", attraction.getId());
            result.put("name", attraction.getName());
            result.put("description", attraction.getDescription());
            result.put("type", attraction.getType());
            result.put("city", attraction.getCity());
            result.put("province", attraction.getProvince());
            result.put("address", attraction.getAddress());
            result.put("phone", attraction.getPhone());
            result.put("website", attraction.getWebsite());
            result.put("longitude", attraction.getLongitude());
            result.put("latitude", attraction.getLatitude());
            result.put("ticketPrice", attraction.getTicketPrice());
            result.put("openTime", attraction.getOpenTime());
            result.put("suggestedDuration", attraction.getSuggestedDuration());
            result.put("rating", attraction.getRating());
            result.put("score", attraction.getScore());
            result.put("coverImage", attraction.getCoverImage());
            result.put("images", parseImages(attraction.getImages(), attraction.getCoverImage()));
            result.put("tags", parseTags(attraction.getTags()));
            result.put("features", attraction.getFeatures());
            result.put("transportation", attraction.getTransportation());
            result.put("nearbyFood", attraction.getNearbyFood());
            result.put("nearbyHotel", attraction.getNearbyHotel());
            result.put("bestSeason", attraction.getBestSeason());
            result.put("notes", attraction.getNotes());
            result.put("viewCount", attraction.getViewCount() != null ? attraction.getViewCount() : 0);
            result.put("collectCount", realCollectCount);
            result.put("commentCount", attraction.getCommentCount() != null ? attraction.getCommentCount() : 0);
            
            // 检查用户是否已收藏
            try {
                Long userId = getUserIdFromToken(request);
                if (userId != null) {
                    boolean isCollected = userCollectService.isCollected(userId, 2, id); // 2表示景点
                    result.put("isCollected", isCollected);
                } else {
                    result.put("isCollected", false);
                }
            } catch (Exception e) {
                result.put("isCollected", false);
            }
            
            return Result.success("获取景点详情成功", result);
        } catch (Exception e) {
            return Result.error("获取景点详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 增加浏览量
     */
    @PostMapping("/view/{id}")
    public Result<String> increaseViewCount(@PathVariable Long id) {
        try {
            boolean success = attractionService.updateAttractionStats(id, "view");
            if (success) {
                return Result.success("增加浏览量成功");
            } else {
                return Result.error("增加浏览量失败");
            }
        } catch (Exception e) {
            return Result.error("增加浏览量失败: " + e.getMessage());
        }
    }
    
    /**
     * 收藏/取消收藏景点
     */
    @PostMapping("/collect/{id}")
    public Result<Boolean> toggleCollect(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            boolean isCollected = userCollectService.isCollected(userId, 2, id); // 2表示景点
            boolean success;
            if (isCollected) {
                // 取消收藏
                success = userCollectService.removeCollect(userId, 2, id);
                // 更新景点收藏数
                if (success) {
                    Attraction attraction = attractionService.getAttractionById(id);
                    if (attraction != null && attraction.getCollectCount() != null && attraction.getCollectCount() > 0) {
                        attraction.setCollectCount(attraction.getCollectCount() - 1);
                        attractionService.updateAttraction(attraction);
                    }
                }
            } else {
                // 添加收藏
                success = userCollectService.addCollect(userId, 2, id);
                // 更新景点收藏数
                if (success) {
                    Attraction attraction = attractionService.getAttractionById(id);
                    if (attraction != null) {
                        attraction.setCollectCount(attraction.getCollectCount() != null ? attraction.getCollectCount() + 1 : 1);
                        attractionService.updateAttraction(attraction);
                    }
                }
            }
            
            if (success) {
                return Result.success("操作成功", !isCollected);
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取景点列表（用户端）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getAttractionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer type,
            HttpServletRequest request) {
        try {
            // 获取用户ID（用于判断收藏状态和记录搜索日志）
            Long userId = null;
            try {
                userId = getUserIdFromToken(request);
            } catch (Exception e) {
                // 用户未登录，忽略
            }
            
            // 只获取正常状态的景点（status=1，deleted=0）
            System.out.println("获取景点列表 - 参数: page=" + page + ", size=" + size + ", keyword=" + keyword + ", city=" + city + ", type=" + type);
            Map<String, Object> result = attractionService.getAttractionList(page, size, keyword, city, type, 1, null, null);
            System.out.println("获取景点列表 - 结果总数: " + (result != null && result.get("total") != null ? result.get("total") : 0));
            
            // 如果有搜索关键词，记录搜索日志
            if (keyword != null && !keyword.trim().isEmpty()) {
                try {
                    int resultCount = result != null && result.get("total") != null ? 
                        ((Number) result.get("total")).intValue() : 0;
                    searchServiceImpl.recordSearchLog(userId, keyword.trim(), "attractions", resultCount);
                } catch (Exception e) {
                    // 记录搜索日志失败不应该影响搜索功能，只记录错误
                    System.err.println("记录搜索日志失败: " + e.getMessage());
                }
            }
            
            List<Attraction> attractions = new ArrayList<>();
            if (result != null && result.get("list") instanceof List<?>) {
                for (Object obj : (List<?>) result.get("list")) {
                    if (obj instanceof Attraction) {
                        attractions.add((Attraction) obj);
                    }
                }
            }
            List<Map<String, Object>> resultList = new ArrayList<>();
            
            if (!attractions.isEmpty()) {
                for (Attraction attraction : attractions) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", attraction.getId());
                    item.put("name", attraction.getName());
                    item.put("description", attraction.getDescription());
                    item.put("type", attraction.getType());
                    item.put("city", attraction.getCity());
                    item.put("province", attraction.getProvince());
                    item.put("location", attraction.getCity() + (attraction.getProvince() != null ? " · " + attraction.getProvince() : ""));
                    
                    // 处理图片
                    String image = "";
                    if (attraction.getCoverImage() != null && !attraction.getCoverImage().isEmpty()) {
                        image = attraction.getCoverImage();
                    } else if (attraction.getImages() != null && !attraction.getImages().isEmpty()) {
                        String[] images = attraction.getImages().split(",");
                        if (images.length > 0) {
                            image = images[0].trim();
                        }
                    }
                    item.put("image", image);
                    item.put("imageHeight", 200 + Math.random() * 100); // 随机高度用于瀑布流
                    
                    // 处理类型名称
                    String typeName = getTypeName(attraction.getType());
                    item.put("typeName", typeName);
                    
                    // 处理标签
                    List<String> tags = parseTags(attraction.getTags());
                    item.put("tags", tags);
                    
                    item.put("price", attraction.getTicketPrice() != null ? attraction.getTicketPrice().doubleValue() : 0);
                    item.put("ticketPrice", attraction.getTicketPrice() != null ? attraction.getTicketPrice().doubleValue() : 0); // 兼容字段
                    item.put("views", attraction.getViewCount() != null ? attraction.getViewCount() : 0);
                    item.put("viewCount", attraction.getViewCount() != null ? attraction.getViewCount() : 0); // 兼容字段
                    item.put("favorites", attraction.getCollectCount() != null ? attraction.getCollectCount() : 0);
                    item.put("collectCount", attraction.getCollectCount() != null ? attraction.getCollectCount() : 0); // 兼容字段
                    
                    // 添加评分和等级
                    item.put("score", attraction.getScore() != null ? attraction.getScore().doubleValue() : 0);
                    item.put("rating", attraction.getRating() != null ? attraction.getRating() : 0);
                    
                    // 添加地址信息
                    item.put("address", attraction.getAddress());
                    
                    // 添加封面图字段（兼容）
                    item.put("coverImage", image);
                    
                    // 检查收藏状态
                    boolean isFavorite = false;
                    if (userId != null) {
                        try {
                            isFavorite = userCollectService.isCollected(userId, 2, attraction.getId());
                        } catch (Exception e) {
                            // 忽略错误
                        }
                    }
                    item.put("isFavorite", isFavorite);
                    
                    resultList.add(item);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("list", resultList);
            response.put("total", result.get("total"));
            response.put("page", page);
            response.put("size", size);
            
            return Result.success("获取景点列表成功", response);
        } catch (Exception e) {
            return Result.error("获取景点列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取景点类型名称（与管理平台创建景点的类型对应）
     */
    private String getTypeName(Integer type) {
        if (type == null) return "其他";
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
    
    /**
     * 获取周边景点
     */
    @GetMapping("/nearby")
    public Result<List<Map<String, Object>>> getNearbyAttractions(
            @RequestParam Long attractionId,
            @RequestParam(defaultValue = "5") Integer limit) {
        try {
            int maxLimit = limit != null && limit > 0 ? Math.min(limit, MAX_RECOMMENDATION_LIMIT) : MAX_RECOMMENDATION_LIMIT;
            Attraction currentAttraction = attractionService.getAttractionById(attractionId);
            if (currentAttraction == null) {
                return Result.error("景点不存在");
            }
            
            List<Attraction> candidateList = new ArrayList<>();
            Set<Long> addedIds = new LinkedHashSet<>();
            int fetchLimit = maxLimit * 3;
            
            if (StringUtils.hasText(currentAttraction.getCity())) {
                attractionService.lambdaQuery()
                    .eq(Attraction::getCity, currentAttraction.getCity())
                    .eq(Attraction::getStatus, 1)
                    .ne(Attraction::getId, attractionId)
                    .ne(Attraction::getDeleted, 1)
                    .last("LIMIT " + fetchLimit)
                    .list()
                    .forEach(attraction -> addAttractionCandidate(attraction, candidateList, addedIds));
            }
            
            if (candidateList.size() < maxLimit && StringUtils.hasText(currentAttraction.getProvince())) {
                attractionService.lambdaQuery()
                    .eq(Attraction::getProvince, currentAttraction.getProvince())
                    .ne(Attraction::getCity, currentAttraction.getCity())
                    .eq(Attraction::getStatus, 1)
                    .ne(Attraction::getId, attractionId)
                    .ne(Attraction::getDeleted, 1)
                    .last("LIMIT " + fetchLimit)
                    .list()
                    .forEach(attraction -> addAttractionCandidate(attraction, candidateList, addedIds));
            }
            
            if (candidateList.size() < maxLimit && StringUtils.hasText(currentAttraction.getProvince())) {
                List<String> neighborProvinces = PROVINCE_NEIGHBORS.getOrDefault(
                    currentAttraction.getProvince(), Collections.emptyList());
                if (!neighborProvinces.isEmpty()) {
                    attractionService.lambdaQuery()
                        .in(Attraction::getProvince, neighborProvinces)
                        .eq(Attraction::getStatus, 1)
                        .ne(Attraction::getId, attractionId)
                        .ne(Attraction::getDeleted, 1)
                        .last("LIMIT " + fetchLimit)
                        .list()
                        .forEach(attraction -> addAttractionCandidate(attraction, candidateList, addedIds));
                }
            }
            
            List<Map<String, Object>> nearbyList = candidateList.stream()
                .filter(attraction -> isWithinAllowedRange(currentAttraction, attraction))
                .sorted(Comparator
                    .comparingInt((Attraction attraction) -> safeValue(attraction.getCollectCount())).reversed()
                    .thenComparing(Comparator.comparingInt((Attraction attraction) -> safeValue(attraction.getCommentCount())).reversed())
                    .thenComparing(Comparator.comparingInt((Attraction attraction) -> safeValue(attraction.getViewCount())).reversed()))
                .limit(maxLimit)
                .map(attraction -> buildNearbyAttractionItem(currentAttraction, attraction))
                .collect(Collectors.toList());
            
            return Result.success("获取周边景点成功", nearbyList);
        } catch (Exception e) {
            return Result.error("获取周边景点失败: " + e.getMessage());
        }
    }
    
    private void addAttractionCandidate(Attraction attraction, List<Attraction> collector, Set<Long> addedIds) {
        if (attraction == null || attraction.getId() == null || addedIds.contains(attraction.getId())) {
            return;
        }
        collector.add(attraction);
        addedIds.add(attraction.getId());
    }
    
    private Map<String, Object> buildNearbyAttractionItem(Attraction current, Attraction target) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", target.getId());
        item.put("name", target.getName());
        item.put("image", resolveAttractionImage(target));
        item.put("distance", buildDistanceLabel(current, target));
        item.put("collectCount", safeValue(target.getCollectCount()));
        item.put("commentCount", safeValue(target.getCommentCount()));
        item.put("viewCount", safeValue(target.getViewCount()));
        return item;
    }
    
    private String resolveAttractionImage(Attraction attraction) {
        List<String> images = parseImages(attraction.getImages(), attraction.getCoverImage());
        return images.isEmpty() ? "/default-attraction.jpg" : images.get(0);
    }
    
    private String buildDistanceLabel(Attraction current, Attraction target) {
        if (current.getLatitude() != null && current.getLongitude() != null
                && target.getLatitude() != null && target.getLongitude() != null) {
            double distance = calculateDistance(
                current.getLatitude().doubleValue(),
                current.getLongitude().doubleValue(),
                target.getLatitude().doubleValue(),
                target.getLongitude().doubleValue()
            );
            return formatDistance(distance);
        }
        
        if (StringUtils.hasText(current.getCity()) && current.getCity().equals(target.getCity())) {
            return "同城推荐";
        }
        if (StringUtils.hasText(current.getProvince()) && current.getProvince().equals(target.getProvince())) {
            return "同省推荐";
        }
        return "临省推荐";
    }

    private boolean isWithinAllowedRange(Attraction current, Attraction target) {
        if (current == null || target == null) {
            return false;
        }
        boolean hasCoords = current.getLatitude() != null && current.getLongitude() != null
            && target.getLatitude() != null && target.getLongitude() != null;
        
        if (hasCoords) {
            double distance = calculateDistance(
                current.getLatitude().doubleValue(),
                current.getLongitude().doubleValue(),
                target.getLatitude().doubleValue(),
                target.getLongitude().doubleValue()
            );
            return distance <= MAX_NEARBY_DISTANCE_KM;
        }
        
        if (StringUtils.hasText(current.getCity()) && current.getCity().equals(target.getCity())) {
            return true;
        }
        if (StringUtils.hasText(current.getProvince()) && current.getProvince().equals(target.getProvince())) {
            return true;
        }
        if (StringUtils.hasText(current.getProvince())) {
            List<String> neighbors = PROVINCE_NEIGHBORS.getOrDefault(current.getProvince(), Collections.emptyList());
            if (!neighbors.isEmpty() && StringUtils.hasText(target.getProvince())) {
                return neighbors.contains(target.getProvince());
            }
        }
        return false;
    }
    
    private String extractPlanCover(TravelPlan plan) {
        if (StringUtils.hasText(plan.getCoverImage())) {
            return plan.getCoverImage();
        }
        if (StringUtils.hasText(plan.getImages())) {
            String[] images = plan.getImages().split(",");
            for (String image : images) {
                String trimmed = image.trim().replace("\"", "").replace("'", "");
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        }
        return "/default-cover.jpg";
    }
    
    private int safeValue(Integer value) {
        return value == null ? 0 : value;
    }
    
    private static Map<String, List<String>> createProvinceNeighborMap() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("北京市", Arrays.asList("天津市", "河北省"));
        map.put("天津市", Arrays.asList("北京市", "河北省"));
        map.put("上海市", Arrays.asList("江苏省", "浙江省"));
        map.put("重庆市", Arrays.asList("四川省", "陕西省", "湖北省", "贵州省"));
        
        map.put("河北省", Arrays.asList("北京市", "天津市", "山西省", "内蒙古自治区", "河南省", "山东省", "辽宁省"));
        map.put("山西省", Arrays.asList("河北省", "内蒙古自治区", "陕西省", "河南省"));
        map.put("内蒙古自治区", Arrays.asList("辽宁省", "吉林省", "黑龙江省", "河北省", "山西省", "陕西省", "宁夏回族自治区", "甘肃省"));
        map.put("辽宁省", Arrays.asList("吉林省", "内蒙古自治区", "河北省"));
        map.put("吉林省", Arrays.asList("黑龙江省", "辽宁省"));
        map.put("黑龙江省", Arrays.asList("吉林省"));
        
        map.put("江苏省", Arrays.asList("山东省", "安徽省", "浙江省", "上海市"));
        map.put("浙江省", Arrays.asList("江苏省", "安徽省", "江西省", "福建省", "上海市"));
        map.put("安徽省", Arrays.asList("江苏省", "浙江省", "江西省", "湖北省", "河南省", "山东省"));
        map.put("福建省", Arrays.asList("浙江省", "江西省", "广东省"));
        map.put("江西省", Arrays.asList("浙江省", "福建省", "广东省", "湖南省", "湖北省", "安徽省"));
        map.put("山东省", Arrays.asList("河北省", "河南省", "安徽省", "江苏省"));
        
        map.put("河南省", Arrays.asList("河北省", "山西省", "陕西省", "湖北省", "安徽省", "山东省"));
        map.put("湖北省", Arrays.asList("河南省", "陕西省", "重庆市", "湖南省", "江西省", "安徽省"));
        map.put("湖南省", Arrays.asList("湖北省", "重庆市", "贵州省", "广西壮族自治区", "广东省", "江西省"));
        map.put("广东省", Arrays.asList("广西壮族自治区", "湖南省", "江西省", "福建省", "海南省"));
        map.put("广西壮族自治区", Arrays.asList("云南省", "贵州省", "湖南省", "广东省", "海南省"));
        map.put("海南省", Arrays.asList("广东省", "广西壮族自治区"));
        
        map.put("四川省", Arrays.asList("陕西省", "甘肃省", "青海省", "西藏自治区", "云南省", "贵州省", "重庆市"));
        map.put("贵州省", Arrays.asList("四川省", "重庆市", "云南省", "广西壮族自治区", "湖南省"));
        map.put("云南省", Arrays.asList("四川省", "贵州省", "广西壮族自治区", "西藏自治区"));
        map.put("陕西省", Arrays.asList("山西省", "内蒙古自治区", "宁夏回族自治区", "甘肃省", "四川省", "重庆市", "湖北省", "河南省"));
        map.put("甘肃省", Arrays.asList("陕西省", "宁夏回族自治区", "青海省", "新疆维吾尔自治区", "四川省", "内蒙古自治区"));
        map.put("青海省", Arrays.asList("甘肃省", "新疆维吾尔自治区", "四川省", "西藏自治区"));
        map.put("宁夏回族自治区", Arrays.asList("内蒙古自治区", "陕西省", "甘肃省"));
        map.put("新疆维吾尔自治区", Arrays.asList("甘肃省", "青海省", "西藏自治区"));
        map.put("西藏自治区", Arrays.asList("四川省", "云南省", "青海省", "新疆维吾尔自治区"));
        
        map.put("台湾省", Arrays.asList("福建省"));
        map.put("香港特别行政区", Arrays.asList("广东省"));
        map.put("澳门特别行政区", Arrays.asList("广东省"));
        return map;
    }
    
    /**
     * 解析图片列表
     */
    private List<String> parseImages(String imagesJson, String coverImage) {
        List<String> imageList = new ArrayList<>();
        
        // 如果有封面图，先添加封面图
        if (coverImage != null && !coverImage.trim().isEmpty()) {
            imageList.add(coverImage);
        }
        
        // 解析images字段
        if (imagesJson != null && !imagesJson.trim().isEmpty()) {
            try {
                // 尝试解析JSON数组
                if (imagesJson.startsWith("[")) {
                    imagesJson = imagesJson.substring(1, imagesJson.length() - 1);
                }
                String[] images = imagesJson.split(",");
                for (String img : images) {
                    img = img.trim().replace("\"", "").replace("'", "");
                    if (!img.isEmpty() && !imageList.contains(img)) {
                        imageList.add(img);
                    }
                }
            } catch (Exception e) {
                // 如果解析失败，直接作为单个图片
                if (!imagesJson.isEmpty() && !imageList.contains(imagesJson)) {
                    imageList.add(imagesJson);
                }
            }
        }
        
        // 如果没有图片，返回默认图片
        if (imageList.isEmpty()) {
            imageList.add("/default-attraction.jpg");
        }
        
        return imageList;
    }
    
    /**
     * 解析标签
     */
    private List<String> parseTags(String tags) {
        List<String> tagList = new ArrayList<>();
        if (tags != null && !tags.trim().isEmpty()) {
            String[] tagArray = tags.split(",");
            for (String tag : tagArray) {
                tag = tag.trim();
                if (!tag.isEmpty()) {
                    tagList.add(tag);
                }
            }
        }
        return tagList;
    }
    
    /**
     * 计算两点间距离（公里）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（公里）
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    /**
     * 格式化距离
     */
    private String formatDistance(double distance) {
        if (distance < 1) {
            return String.format("%.0f米", distance * 1000);
        } else if (distance < 10) {
            return String.format("%.1f公里", distance);
        } else {
            return String.format("%.0f公里", distance);
        }
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                return jwtUtil.getUserIdFromToken(jwtToken);
            }
        } catch (Exception e) {
            // 忽略错误，返回null表示未登录
        }
        return null;
    }
    
    /**
     * 获取景点评分概览
     */
    @GetMapping("/{id}/rating")
    public Result<Map<String, Object>> getRatingSummary(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            Map<String, Object> result = attractionService.getRatingSummary(id, userId);
            return Result.success("获取评分信息成功", result);
        } catch (Exception e) {
            return Result.error("获取评分信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交景点评分
     */
    @PostMapping("/{id}/rating")
    public Result<Map<String, Object>> submitRating(
            @PathVariable Long id,
            @RequestBody Map<String, Object> ratingData,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Integer score = null;
            if (ratingData.get("score") instanceof Integer) {
                score = (Integer) ratingData.get("score");
            } else if (ratingData.get("score") instanceof Number) {
                score = ((Number) ratingData.get("score")).intValue();
            }
            
            if (score == null || score < 1 || score > 5) {
                return Result.error("评分必须在1-5之间");
            }
            
            Map<String, Object> result = attractionService.submitRating(id, userId, score);
            return Result.success("评分成功", result);
        } catch (Exception e) {
            return Result.error("评分失败: " + e.getMessage());
        }
    }
}

