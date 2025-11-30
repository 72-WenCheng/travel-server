package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论点赞Mapper接口
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
    
    /**
     * 检查用户是否点赞了某条评论
     */
    int checkUserLike(@Param("commentId") Long commentId, @Param("userId") Long userId);
    
    /**
     * 删除点赞记录
     */
    int deleteLike(@Param("commentId") Long commentId, @Param("userId") Long userId);
}







