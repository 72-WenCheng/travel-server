package com.zhly.mapper;

import com.zhly.entity.CultureExperience;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 文化体验数据访问层
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface CultureExperienceMapper extends BaseMapper<CultureExperience> {
    
    /**
     * 获取文化体验列表
     */
    List<CultureExperience> selectExperienceList(@Param("page") Integer page, 
                                                 @Param("size") Integer size, 
                                                 @Param("keyword") String keyword,
                                                 @Param("categoryName") String categoryName,
                                                 @Param("status") String status);
    
    /**
     * 获取文化体验总数
     */
    Long selectExperienceCount(@Param("keyword") String keyword,
                              @Param("categoryName") String categoryName,
                              @Param("status") String status);
    
    /**
     * 获取热门文化体验
     */
    List<CultureExperience> selectHotExperiences(@Param("limit") Integer limit);
    
    /**
     * 根据分类获取文化体验列表
     */
    List<CultureExperience> selectExperiencesByCategory(@Param("categoryName") String categoryName,
                                                        @Param("limit") Integer limit);
    
    /**
     * 更新浏览量
     */
    int updateViewCount(@Param("id") Long id);
    
    /**
     * 更新预订量
     */
    int updateOrderCount(@Param("id") Long id);
    
    /**
     * 获取文化体验统计信息
     */
    Map<String, Object> selectExperienceStatistics();
}











































































