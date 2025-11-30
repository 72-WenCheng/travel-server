package com.zhly.mapper;

import com.zhly.entity.Attraction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 景点数据访问层
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface AttractionMapper extends BaseMapper<Attraction> {
    
    /**
     * 获取景点列表（带分页和搜索）
     */
    List<Attraction> selectAttractionList(@Param("page") Integer page, 
                                         @Param("size") Integer size, 
                                         @Param("keyword") String keyword,
                                         @Param("city") String city,
                                         @Param("type") Integer type,
                                         @Param("status") Integer status,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate);
    
    /**
     * 获取景点总数
     */
    Long selectAttractionCount(@Param("keyword") String keyword,
                              @Param("city") String city,
                              @Param("type") Integer type,
                              @Param("status") Integer status,
                              @Param("startDate") String startDate,
                              @Param("endDate") String endDate);
    
    /**
     * 获取热门景点
     */
    List<Attraction> selectHotAttractions(@Param("limit") Integer limit);
    
    /**
     * 获取城市景点列表
     */
    List<Attraction> selectCityAttractions(@Param("city") String city,
                                          @Param("limit") Integer limit);
    
    /**
     * 更新景点统计信息
     */
    int updateAttractionStats(@Param("id") Long id, 
                             @Param("type") String type);
    
    /**
     * 获取景点统计信息
     */
    java.util.Map<String, Object> selectAttractionStatistics();
}









