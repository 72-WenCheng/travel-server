package com.zhly.user.controller;

import com.zhly.entity.Comment;
import com.zhly.service.ICommentService;
import com.zhly.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * 用户端评论控制器
 */
@RestController
@RequestMapping("/api/user/comment")
public class UserCommentController {
    
    @Autowired
    private ICommentService commentService;
    
    /**
     * 获取内容的评论列表
     * @param contentType 内容类型（PLAN/ATTRACTION/CULTURE）
     * @param contentId 内容ID
     * @param page 页码
     * @param pageSize 每页数量
     */
    @GetMapping("/list")
    public Result getComments(
            @RequestParam String contentType,
            @RequestParam Long contentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            // 获取当前用户ID（用于判断点赞状态）
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                userId = 0L; // 未登录用户
            }
            
            Map<String, Object> result = commentService.getCommentsByContent(
                contentType, contentId, userId, page, pageSize
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取评论列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取评论的回复列表
     * @param parentId 父评论ID
     */
    @GetMapping("/replies/{parentId}")
    public Result getReplies(@PathVariable Long parentId, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                userId = 0L;
            }
            
            // 使用mapper查询所有回复
            List<Comment> replies = commentService.getRepliesByParentId(parentId, userId);
            return Result.success("获取回复列表成功", replies);
        } catch (Exception e) {
            return Result.error("获取回复列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 发布评论
     */
    @PostMapping("/publish")
    public Result publishComment(@RequestBody Comment comment, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            Comment result = commentService.publishComment(userId, comment);
            return Result.success("发布成功", result);
        } catch (Exception e) {
            return Result.error("发布评论失败：" + e.getMessage());
        }
    }
    
    /**
     * 回复评论
     */
    @PostMapping("/reply")
    public Result replyComment(
            @RequestParam Long parentId,
            @RequestParam Long replyToUserId,
            @RequestParam String content,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            Comment result = commentService.replyComment(userId, parentId, replyToUserId, content);
            return Result.success("回复成功", result);
        } catch (Exception e) {
            return Result.error("回复评论失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除评论（只能删除自己的）
     */
    @DeleteMapping("/delete/{commentId}")
    public Result deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            boolean result = commentService.deleteComment(userId, commentId, false);
            return Result.success("删除成功", result);
        } catch (Exception e) {
            return Result.error("删除评论失败：" + e.getMessage());
        }
    }
    
    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like/{commentId}")
    public Result toggleLike(@PathVariable Long commentId, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            boolean liked = commentService.toggleLike(userId, commentId);
            return Result.success(liked ? "点赞成功" : "取消点赞", liked);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取我的评论列表
     */
    @GetMapping("/my")
    public Result getMyComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            Map<String, Object> result = commentService.getUserComments(userId, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取评论列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查评论权限
     */
    @GetMapping("/check-permission")
    public Result checkPermission(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            boolean hasPermission = commentService.checkCommentPermission(userId);
            return Result.success(hasPermission);
        } catch (Exception e) {
            return Result.error("检查权限失败：" + e.getMessage());
        }
    }
}

