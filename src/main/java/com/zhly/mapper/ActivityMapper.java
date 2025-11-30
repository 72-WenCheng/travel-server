package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 活动Mapper接口
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
    
    /**
     * 增加浏览量
     */
    @Update("UPDATE activity SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);
    
    /**
     * 增加参与人数
     */
    @Update("UPDATE activity SET participant_count = participant_count + 1 WHERE id = #{id}")
    int incrementParticipantCount(Long id);
    
    /**
     * 获取进行中的活动列表
     */
    List<Activity> selectActiveActivities();
}







