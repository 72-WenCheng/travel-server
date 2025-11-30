package com.zhly.admin.controller;

import com.zhly.common.Result;
import com.zhly.entity.Attraction;
import com.zhly.service.AttractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端-景点控制器
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Tag(name = "管理端-景点管理")
@RestController
@RequestMapping("/api/admin/attraction")
@RequiredArgsConstructor
public class AdminAttractionController {
    
    private final AttractionService attractionService;
    
    /**
     * 获取景点列表（管理端）
     */
    @GetMapping("/list")
    @Operation(summary = "获取景点列表")
    public Result<Map<String, Object>> getAttractionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Map<String, Object> result = attractionService.getAttractionList(
                page, size, keyword, city, type, status, startDate, endDate);
            return Result.success("获取景点列表成功", result);
        } catch (Exception e) {
            return Result.error("获取景点列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取景点详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取景点详情")
    public Result<Attraction> getAttractionById(@PathVariable Long id) {
        try {
            Attraction attraction = attractionService.getAttractionById(id);
            if (attraction == null) {
                return Result.notFound("景点不存在");
            }
            return Result.success("获取景点详情成功", attraction);
        } catch (Exception e) {
            return Result.error("获取景点详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建景点
     */
    @PostMapping
    @Operation(summary = "创建景点")
    public Result<String> createAttraction(@RequestBody Attraction attraction) {
        try {
            boolean success = attractionService.createAttraction(attraction);
            if (success) {
                return Result.success("创建景点成功");
            } else {
                return Result.error("创建景点失败");
            }
        } catch (Exception e) {
            return Result.error("创建景点失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新景点
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新景点")
    public Result<String> updateAttraction(@PathVariable Long id, @RequestBody Attraction attraction) {
        try {
            attraction.setId(id);
            boolean success = attractionService.updateAttraction(attraction);
            if (success) {
                return Result.success("更新景点成功");
            } else {
                return Result.error("更新景点失败");
            }
        } catch (Exception e) {
            return Result.error("更新景点失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除景点
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除景点")
    public Result<String> deleteAttraction(@PathVariable Long id) {
        try {
            boolean success = attractionService.deleteAttraction(id);
            if (success) {
                return Result.success("删除景点成功");
            } else {
                return Result.error("删除景点失败");
            }
        } catch (Exception e) {
            return Result.error("删除景点失败: " + e.getMessage());
        }
    }
    
    /**
     * 切换景点状态
     */
    @PostMapping("/{id}/status")
    @Operation(summary = "切换景点状态")
    public Result<String> toggleAttractionStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        try {
            boolean success = attractionService.toggleAttractionStatus(id, status);
            if (success) {
                return Result.success("更新景点状态成功");
            } else {
                return Result.error("更新景点状态失败");
            }
        } catch (Exception e) {
            return Result.error("更新景点状态失败: " + e.getMessage());
        }
    }
}

