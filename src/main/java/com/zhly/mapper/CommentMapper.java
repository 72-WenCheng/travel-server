package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论Mapper接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    /**
     * 查询顶级评论列表（带用户信息）
     */
    List<Comment> selectTopComments(@Param("contentType") String contentType, 
                                   @Param("contentId") Long contentId,
                                   @Param("userId") Long userId,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);
    
    /**
     * 查询回复列表（带用户信息）
     */
    List<Comment> selectReplies(@Param("parentId") Long parentId, 
                                @Param("userId") Long userId);
    
    /**
     * 查询用户的评论列表（带内容标题）
     */
    List<Comment> selectUserComments(@Param("userId") Long userId,
                                    @Param("offset") Integer offset,
                                    @Param("limit") Integer limit);
    
    /**
     * 增加评论的点赞数
     */
    int increaseLikeCount(@Param("commentId") Long commentId);
    
    /**
     * 减少评论的点赞数
     */
    int decreaseLikeCount(@Param("commentId") Long commentId);
    
    /**
     * 增加评论的回复数
     */
    int increaseReplyCount(@Param("commentId") Long commentId);
    
    /**
     * 减少评论的回复数
     */
    int decreaseReplyCount(@Param("commentId") Long commentId);
    
    /**
     * 更新内容的评论数（travel_plan, attraction, culture_project）
     */
    int updateContentCommentCount(@Param("contentType") String contentType,
                                 @Param("contentId") Long contentId);
}
