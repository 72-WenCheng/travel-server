package com.zhly.mapper;

import com.zhly.entity.UserLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 用户点赞数据访问层
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface UserLikeMapper extends BaseMapper<UserLike> {
    
    /**
     * 检查是否已点赞
     */
    boolean checkUserLike(@Param("userId") Long userId,
                         @Param("likeType") Integer likeType,
                         @Param("likeId") Long likeId);
    
    /**
     * 获取用户点赞列表
     */
    List<UserLike> selectUserLikes(@Param("userId") Long userId,
                                  @Param("likeType") Integer likeType,
                                  @Param("page") Integer page,
                                  @Param("size") Integer size);
    
    /**
     * 获取点赞统计
     */
    Map<String, Object> selectLikeStatistics();
    
    /**
     * 获取用户点赞统计
     */
    Map<String, Object> selectUserLikeStats(@Param("userId") Long userId);
}


















