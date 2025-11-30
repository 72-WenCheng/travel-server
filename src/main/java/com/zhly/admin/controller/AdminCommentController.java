package com.zhly.admin.controller;

import com.zhly.service.ICommentService;
import com.zhly.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端评论控制器
 */
@RestController
@RequestMapping("/api/admin/comment")
public class AdminCommentController {
    
    @Autowired
    private ICommentService commentService;
    
    /**
     * 获取评论列表（管理端）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getComments(
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = commentService.getAdminComments(
                contentType, keyword, status, page, pageSize
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取评论列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取评论统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = commentService.getCommentStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{commentId}")
    public Result<Boolean> deleteComment(@PathVariable Long commentId) {
        try {
            boolean result = commentService.deleteComment(null, commentId, true);
            return Result.success("删除成功", result);
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量删除评论
     */
    @DeleteMapping("/batch-delete")
    public Result<Boolean> batchDelete(@RequestBody List<Long> commentIds) {
        try {
            boolean result = commentService.batchDelete(commentIds);
            return Result.success("批量删除成功", result);
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 置顶/取消置顶评论
     */
    @PutMapping("/toggle-top/{commentId}")
    public Result<Boolean> toggleTop(@PathVariable Long commentId) {
        try {
            boolean result = commentService.toggleTop(commentId);
            return Result.success("操作成功", result);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }
    
    /**
     * 隐藏评论
     */
    @PutMapping("/hide/{commentId}")
    public Result<Boolean> hideComment(@PathVariable Long commentId) {
        try {
            boolean result = commentService.hideComment(commentId);
            return Result.success("隐藏成功", result);
        } catch (Exception e) {
            return Result.error("隐藏失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量隐藏评论
     */
    @PutMapping("/batch-hide")
    public Result<Boolean> batchHide(@RequestBody List<Long> commentIds) {
        try {
            for (Long commentId : commentIds) {
                commentService.hideComment(commentId);
            }
            return Result.success("批量隐藏成功", true);
        } catch (Exception e) {
            return Result.error("批量隐藏失败：" + e.getMessage());
        }
    }
}

