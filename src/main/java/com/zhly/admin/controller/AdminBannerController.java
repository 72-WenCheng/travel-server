package com.zhly.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.Result;
import com.zhly.entity.Banner;
import com.zhly.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-轮播图管理控制器
 */
@Tag(name = "管理端-轮播图管理")
@RestController
@RequestMapping("/api/admin/banner")
@RequiredArgsConstructor
public class AdminBannerController {
    
    private final BannerService bannerService;
    
    @Operation(summary = "分页查询轮播图")
    @GetMapping("/page")
    public Result<Page<Banner>> pageBanners(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        Page<Banner> pageResult = bannerService.pageBanners(page, size, title, status);
        return Result.success(pageResult);
    }
    
    @Operation(summary = "获取轮播图详情")
    @GetMapping("/{id}")
    public Result<Banner> getBanner(@PathVariable Long id) {
        Banner banner = bannerService.getById(id);
        return Result.success(banner);
    }
    
    @Operation(summary = "新增轮播图")
    @PostMapping
    public Result<String> createBanner(@RequestBody Banner banner) {
        boolean success = bannerService.save(banner);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }
    
    @Operation(summary = "更新轮播图")
    @PutMapping("/{id}")
    public Result<String> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        boolean success = bannerService.updateById(banner);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    @Operation(summary = "删除轮播图")
    @DeleteMapping("/{id}")
    public Result<String> deleteBanner(@PathVariable Long id) {
        boolean success = bannerService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    @Operation(summary = "批量删除轮播图")
    @DeleteMapping("/batch")
    public Result<String> deleteBanners(@RequestBody java.util.List<Long> ids) {
        boolean success = bannerService.removeByIds(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }
    
    @Operation(summary = "更新轮播图状态")
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Banner banner = new Banner();
        banner.setId(id);
        banner.setStatus(status);
        boolean success = bannerService.updateById(banner);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }
    
    @Operation(summary = "获取轮播图统计")
    @GetMapping("/stats")
    public Result<java.util.Map<String, Object>> getStats() {
        try {
            java.util.Map<String, Object> stats = bannerService.getBannerStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计失败：" + e.getMessage());
        }
    }
}







