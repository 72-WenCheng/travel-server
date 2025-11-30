package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户关注Mapper
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
    
    /**
     * 获取关注数
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId} AND status = 1")
    Integer getFollowingCount(Long userId);
    
    /**
     * 获取粉丝数
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE followee_id = #{userId} AND status = 1")
    Integer getFollowersCount(Long userId);
    
    /**
     * 检查是否关注
     */
    @Select("SELECT COUNT(*) > 0 FROM user_follow WHERE follower_id = #{followerId} AND followee_id = #{followeeId} AND status = 1")
    Boolean isFollowing(Long followerId, Long followeeId);
}







