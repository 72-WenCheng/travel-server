package com.zhly.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhly.common.R;
import com.zhly.entity.Notification;
import com.zhly.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户端-通知Controller
 * 
 * @author zhly
 * @since 2025-10-24
 */
@RestController
@RequestMapping("/api/user/notification")
public class UserNotificationController {
    
    @Autowired
    private INotificationService notificationService;
    
    /**
     * 获取我的通知列表
     */
    @GetMapping("/list")
    public R<Page<Notification>> list(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) Integer notifyType,
            @RequestParam(required = false) Integer isRead) {
        
        Page<Notification> pageObj = new Page<>(page, limit);
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        
        if (notifyType != null) {
            wrapper.eq("notify_type", notifyType);
        }
        if (isRead != null) {
            wrapper.eq("is_read", isRead);
        }
        
        wrapper.orderByDesc("create_time");
        notificationService.page(pageObj, wrapper);
        
        return R.ok(pageObj);
    }
    
    /**
     * 标记为已读
     */
    @PostMapping("/mark-read/{id}")
    public R<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return R.ok("已标记为已读");
    }
    
    /**
     * 标记全部已读
     */
    @PostMapping("/mark-all-read")
    public R<String> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return R.ok("已全部标记为已读");
    }
    
    /**
     * 获取未读数量
     */
    @GetMapping("/unread-count")
    public R<Map<String, Integer>> getUnreadCount(@RequestParam Long userId) {
        Integer count = notificationService.getUnreadCount(userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("unreadCount", count);
        return R.ok(result);
    }
}







