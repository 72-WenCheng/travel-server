package com.zhly.user.controller;

import com.zhly.common.Result;
import com.zhly.entity.UserBrowseHistory;
import com.zhly.service.UserBrowseHistoryService;
import com.zhly.util.IpAddressUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户端-浏览历史控制器
 */
@Tag(name = "用户端-浏览历史")
@RestController
@RequestMapping("/api/user/history")
@RequiredArgsConstructor
public class UserBrowseHistoryController {
    
    private final UserBrowseHistoryService historyService;
    
    @Operation(summary = "添加浏览记录")
    @PostMapping
    public Result<String> addHistory(
            @RequestParam Long userId,
            @RequestParam Integer browseType,
            @RequestParam Long browseId,
            @RequestParam(required = false) String browseTitle,
            @RequestParam(required = false) String browseImage,
            @RequestParam(required = false, defaultValue = "0") Integer duration,
            HttpServletRequest request) {
        String clientIp = IpAddressUtils.resolveClientIp(request);
        boolean success = historyService.addBrowseHistory(userId, browseType, browseId, browseTitle, browseImage, duration, clientIp);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }
    
    @Operation(summary = "更新浏览记录的停留时长")
    @PutMapping("/duration")
    public Result<String> updateDuration(
            @RequestParam Long userId,
            @RequestParam Integer browseType,
            @RequestParam Long browseId,
            @RequestParam Integer duration) {
        if (duration == null || duration < 0) {
            return Result.error("停留时长必须大于等于0");
        }
        boolean success = historyService.updateBrowseDuration(userId, browseType, browseId, duration);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    @Operation(summary = "获取最近浏览记录")
    @GetMapping("/recent")
    public Result<List<UserBrowseHistory>> getRecentHistory(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "20") Integer limit) {
        List<UserBrowseHistory> history = historyService.getRecentHistory(userId, limit);
        return Result.success(history);
    }
    
    @Operation(summary = "清除浏览历史")
    @DeleteMapping
    public Result<String> clearHistory(@RequestParam Long userId) {
        boolean success = historyService.clearHistory(userId);
        return success ? Result.success("清除成功") : Result.error("清除失败");
    }
}







