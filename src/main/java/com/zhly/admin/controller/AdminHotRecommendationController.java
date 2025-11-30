package com.zhly.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.Result;
import com.zhly.entity.HotRecommendation;
import com.zhly.service.HotRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-热门推荐管理控制器
 */
@Tag(name = "管理端-热门推荐管理")
@RestController
@RequestMapping("/api/admin/recommendation")
@RequiredArgsConstructor
public class AdminHotRecommendationController {
    
    private final HotRecommendationService recommendationService;
    
    @Operation(summary = "分页查询热门推荐")
    @GetMapping("/page")
    public Result<Page<HotRecommendation>> pageRecommendations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer recommendType,
            @RequestParam(required = false) Integer itemType,
            @RequestParam(required = false) Integer status) {
        Page<HotRecommendation> pageResult = recommendationService.pageRecommendations(page, size, recommendType, itemType, status);
        return Result.success(pageResult);
    }
    
    @Operation(summary = "获取推荐详情")
    @GetMapping("/{id}")
    public Result<HotRecommendation> getRecommendation(@PathVariable Long id) {
        HotRecommendation recommendation = recommendationService.getById(id);
        return Result.success(recommendation);
    }
    
    @Operation(summary = "新增推荐")
    @PostMapping
    public Result<String> createRecommendation(@RequestBody HotRecommendation recommendation) {
        boolean success = recommendationService.save(recommendation);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }
    
    @Operation(summary = "更新推荐")
    @PutMapping("/{id}")
    public Result<String> updateRecommendation(@PathVariable Long id, @RequestBody HotRecommendation recommendation) {
        recommendation.setId(id);
        boolean success = recommendationService.updateById(recommendation);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    @Operation(summary = "删除推荐")
    @DeleteMapping("/{id}")
    public Result<String> deleteRecommendation(@PathVariable Long id) {
        boolean success = recommendationService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    @Operation(summary = "批量删除推荐")
    @DeleteMapping("/batch")
    public Result<String> deleteRecommendations(@RequestBody java.util.List<Long> ids) {
        boolean success = recommendationService.removeByIds(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }
    
    @Operation(summary = "更新推荐状态")
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        HotRecommendation recommendation = new HotRecommendation();
        recommendation.setId(id);
        recommendation.setStatus(status);
        boolean success = recommendationService.updateById(recommendation);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }
    
    @Operation(summary = "获取热门推荐统计")
    @GetMapping("/stats")
    public Result<java.util.Map<String, Object>> getStats() {
        try {
            java.util.Map<String, Object> stats = recommendationService.getRecommendationStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计失败：" + e.getMessage());
        }
    }
}







