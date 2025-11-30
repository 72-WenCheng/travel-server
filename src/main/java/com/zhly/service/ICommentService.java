package com.zhly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhly.entity.Comment;

import java.util.List;
import java.util.Map;

/**
 * 评论服务接口
 */
public interface ICommentService extends IService<Comment> {
    
    /**
     * 发布评论（带权限检查）
     */
    Comment publishComment(Long userId, Comment comment) throws Exception;
    
    /**
     * 回复评论（带权限检查）
     */
    Comment replyComment(Long userId, Long parentId, Long replyToUserId, String content) throws Exception;
    
    /**
     * 获取内容的评论列表（分页，带回复）
     */
    Map<String, Object> getCommentsByContent(String contentType, Long contentId, Long currentUserId, Integer page, Integer pageSize);
    
    /**
     * 获取用户的评论列表
     */
    Map<String, Object> getUserComments(Long userId, Integer page, Integer pageSize);
    
    /**
     * 删除评论（用户只能删除自己的评论）
     */
    boolean deleteComment(Long userId, Long commentId, boolean isAdmin) throws Exception;
    
    /**
     * 点赞/取消点赞
     */
    boolean toggleLike(Long userId, Long commentId) throws Exception;
    
    /**
     * 管理员操作：置顶/取消置顶
     */
    boolean toggleTop(Long commentId);
    
    /**
     * 管理员操作：隐藏评论
     */
    boolean hideComment(Long commentId);
    
    /**
     * 管理员操作：批量删除评论
     */
    boolean batchDelete(List<Long> commentIds);
    
    /**
     * 获取管理端评论列表（所有评论）
     */
    Map<String, Object> getAdminComments(String contentType, String keyword, String status, Integer page, Integer pageSize);
    
    /**
     * 检查用户评论权限
     */
    boolean checkCommentPermission(Long userId) throws Exception;
    
    /**
     * 获取评论统计数据
     */
    Map<String, Object> getCommentStats();
    
    /**
     * 获取评论的所有回复列表
     */
    List<Comment> getRepliesByParentId(Long parentId, Long currentUserId);
}







