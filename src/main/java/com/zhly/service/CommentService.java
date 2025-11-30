package com.zhly.service;

import com.zhly.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 评论服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface CommentService extends IService<Comment> {
    
    /**
     * 添加评论
     */
    boolean addComment(Comment comment);
    
    /**
     * 删除评论
     */
    boolean deleteComment(Long id);
    
    /**
     * 获取评论列表
     */
    Map<String, Object> getCommentList(Integer entityType, Long entityId, Integer page, Integer size);
    
    /**
     * 获取用户评论列表
     */
    Map<String, Object> getUserComments(Long userId, Integer page, Integer size);
    
    /**
     * 获取评论统计
     */
    Map<String, Object> getCommentStatistics();
    
    /**
     * 更新评论状态
     */
    boolean updateCommentStatus(Long id, Integer status);
}


















