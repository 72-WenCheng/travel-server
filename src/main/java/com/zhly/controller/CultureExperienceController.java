package com.zhly.controller;

import com.zhly.common.Result;
import com.zhly.entity.CultureExperience;
import com.zhly.service.CultureExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户端-文化体验控制器
 */
@Tag(name = "用户端-文化体验")
@RestController
@RequestMapping("/api/culture/experiences")
@RequiredArgsConstructor
public class CultureExperienceController {
    
    private final CultureExperienceService cultureExperienceService;
    
    @Operation(summary = "分页查询文化体验（用户端）")
    @GetMapping("/page")
    public Result<Map<String, Object>> pageExperiences(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryName) {
        try {
            // 用户端只查询上架状态的体验
            Map<String, Object> result = cultureExperienceService.getExperienceList(page, size, keyword, categoryName, "active");
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取体验详情（用户端）")
    @GetMapping("/{id}")
    public Result<CultureExperience> getExperience(@PathVariable Long id) {
        try {
            CultureExperience experience = cultureExperienceService.getExperienceById(id);
            if (experience == null) {
                return Result.error("体验不存在");
            }
            // 只有上架状态的体验才能查看
            if (!"active".equals(experience.getStatus())) {
                return Result.error("体验已下架");
            }
            // 更新浏览量
            cultureExperienceService.updateViewCount(id);
            return Result.success(experience);
        } catch (Exception e) {
            return Result.error("获取详情失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取热门文化体验")
    @GetMapping("/hot")
    public Result<List<CultureExperience>> getHotExperiences(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<CultureExperience> experiences = cultureExperienceService.getHotExperiences(limit);
            return Result.success(experiences);
        } catch (Exception e) {
            return Result.error("获取热门体验失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "根据分类获取文化体验")
    @GetMapping("/category/{categoryName}")
    public Result<List<CultureExperience>> getExperiencesByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<CultureExperience> experiences = cultureExperienceService.getExperiencesByCategory(categoryName, limit);
            return Result.success(experiences);
        } catch (Exception e) {
            return Result.error("获取分类体验失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取所有分类")
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        try {
            // 这里可以从数据库查询所有分类，暂时返回常用分类
            List<String> categories = java.util.Arrays.asList(
                "非遗体验", "手工艺体验", "民俗体验", "文化研学", "传统技艺", "节庆活动"
            );
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("获取分类失败: " + e.getMessage());
        }
    }
}











































































