package com.zhly.mapper;

import com.zhly.entity.CultureProject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 文旅项目数据访问层
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface CultureProjectMapper extends BaseMapper<CultureProject> {
    
    /**
     * 获取文旅项目列表
     */
    List<CultureProject> selectProjectList(@Param("page") Integer page, 
                                          @Param("size") Integer size, 
                                          @Param("keyword") String keyword,
                                          @Param("region") String region,
                                          @Param("type") Integer type,
                                          @Param("status") Integer status);
    
    /**
     * 获取文旅项目总数
     */
    Long selectProjectCount(@Param("keyword") String keyword,
                           @Param("region") String region,
                           @Param("type") Integer type,
                           @Param("status") Integer status);
    
    /**
     * 获取热门文旅项目
     */
    List<CultureProject> selectHotProjects(@Param("limit") Integer limit);
    
    /**
     * 获取地区文旅项目列表
     */
    List<CultureProject> selectRegionProjects(@Param("region") String region,
                                             @Param("limit") Integer limit);
    
    /**
     * 更新文旅项目统计信息
     */
    int updateProjectStats(@Param("id") Long id, 
                          @Param("orderCount") Integer orderCount,
                          @Param("revenue") java.math.BigDecimal revenue);
    
    /**
     * 获取文旅项目统计信息
     */
    Map<String, Object> selectProjectStatistics();
}



















