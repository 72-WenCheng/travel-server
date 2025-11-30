package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户积分Mapper
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {
    
    /**
     * 根据用户ID获取积分信息
     */
    @Select("SELECT * FROM user_points WHERE user_id = #{userId}")
    UserPoints getByUserId(Long userId);
}







