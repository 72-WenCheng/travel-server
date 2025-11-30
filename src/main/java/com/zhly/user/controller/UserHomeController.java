package com.zhly.user.controller;

import com.zhly.common.Result;
import com.zhly.entity.Activity;
import com.zhly.entity.Banner;
import com.zhly.entity.HotRecommendation;
import com.zhly.service.ActivityService;
import com.zhly.service.BannerService;
import com.zhly.service.HotRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端-首页控制器
 */
@Tag(name = "用户端-首页")
@RestController
@RequestMapping("/api/user/home")
@RequiredArgsConstructor
public class UserHomeController {
    
    private final BannerService bannerService;
    private final ActivityService activityService;
    private final HotRecommendationService recommendationService;
    
    @Operation(summary = "获取首页数据")
    @GetMapping("/data")
    public Result<Map<String, Object>> getHomeData() {
        Map<String, Object> data = new HashMap<>();
        
        // 获取轮播图
        List<Banner> banners = bannerService.getActiveBanners();
        data.put("banners", banners);
        
        // 获取进行中的活动
        List<Activity> activities = activityService.getActiveActivities();
        data.put("activities", activities);
        
        // 获取热门景点推荐
        List<HotRecommendation> hotAttractions = recommendationService.getRecommendationsByType(1);
        data.put("hotAttractions", hotAttractions);
        
        // 获取热门攻略推荐
        List<HotRecommendation> hotPlans = recommendationService.getRecommendationsByType(2);
        data.put("hotPlans", hotPlans);
        
        // 获取编辑推荐
        List<HotRecommendation> editorRecommendations = recommendationService.getRecommendationsByType(4);
        data.put("editorRecommendations", editorRecommendations);
        
        return Result.success(data);
    }
    
    @Operation(summary = "获取轮播图列表")
    @GetMapping("/banners")
    public Result<List<Banner>> getBanners(@RequestParam(required = false) String position) {
        List<Banner> banners;
        if (position != null && !position.isEmpty()) {
            banners = bannerService.getActiveBannersByPosition(position);
        } else {
            banners = bannerService.getActiveBanners();
        }
        return Result.success(banners);
    }
    
    @Operation(summary = "点击轮播图")
    @PostMapping("/banner/{id}/click")
    public Result<String> clickBanner(@PathVariable Long id) {
        bannerService.incrementClickCount(id);
        return Result.success("记录成功");
    }
    
    @Operation(summary = "获取活动列表")
    @GetMapping("/activities")
    public Result<List<Activity>> getActivities() {
        List<Activity> activities = activityService.getActiveActivities();
        return Result.success(activities);
    }
    
    @Operation(summary = "获取活动详情")
    @GetMapping("/activity/{id}")
    public Result<Activity> getActivityDetail(@PathVariable Long id) {
        Activity activity = activityService.getActivityDetail(id);
        return Result.success(activity);
    }
    
    @Operation(summary = "获取热门推荐")
    @GetMapping("/recommendations")
    public Result<List<HotRecommendation>> getRecommendations(@RequestParam Integer type) {
        List<HotRecommendation> recommendations = recommendationService.getRecommendationsByType(type);
        return Result.success(recommendations);
    }
    
    @Operation(summary = "点击推荐")
    @PostMapping("/recommendation/{id}/click")
    public Result<String> clickRecommendation(@PathVariable Long id) {
        recommendationService.incrementClickCount(id);
        return Result.success("记录成功");
    }
}







