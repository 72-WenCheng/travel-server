package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.UserLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户等级Mapper
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Mapper
public interface UserLevelMapper extends BaseMapper<UserLevel> {
    
    /**
     * 根据积分获取对应等级
     */
    @Select("SELECT * FROM user_level WHERE required_points <= #{points} ORDER BY required_points DESC LIMIT 1")
    UserLevel getLevelByPoints(Integer points);
    
    /**
     * 根据等级代码获取等级信息
     */
    @Select("SELECT * FROM user_level WHERE level_code = #{levelCode}")
    UserLevel getLevelByCode(Integer levelCode);
}







