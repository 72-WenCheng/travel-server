package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.HotRecommendation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 热门推荐Mapper接口
 */
@Mapper
public interface HotRecommendationMapper extends BaseMapper<HotRecommendation> {
    
    /**
     * 增加点击次数
     */
    @Update("UPDATE hot_recommendation SET click_count = click_count + 1 WHERE id = #{id}")
    int incrementClickCount(Long id);
    
    /**
     * 根据推荐类型获取推荐列表
     */
    List<HotRecommendation> selectByRecommendType(Integer recommendType);
}







