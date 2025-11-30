package com.zhly.mapper;

import com.zhly.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 用户数据访问层
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 获取用户统计信息
     */
    Map<String, Object> selectUserStatistics();
    
    /**
     * 获取活跃用户列表
     */
    List<User> selectActiveUsers(@Param("days") Integer days, @Param("limit") Integer limit);
    
    /**
     * 获取用户标签统计
     */
    Map<String, Object> selectUserTagStatistics();
    
    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查询用户
     */
    User selectByEmail(@Param("email") String email);
    
    /**
     * 更新用户最后登录时间
     */
    int updateLastLoginTime(@Param("userId") Long userId);
    
    /**
     * 更新用户最后登录信息（时间和IP）
     */
    int updateLastLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp);
    
    /**
     * 更新用户积分
     */
    int updateUserPoints(@Param("userId") Long userId, @Param("points") Integer points);
    
    /**
     * 获取用户推荐内容
     */
    Object selectUserRecommendations(@Param("userId") Long userId);
    
    /**
     * 获取最新注册用户
     */
    List<User> selectLatestUsers(@Param("limit") Integer limit);
    
    /**
     * 获取每日用户增长数据
     */
    List<Map<String, Object>> selectDailyUserGrowth(@Param("days") Integer days);
}

