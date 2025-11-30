package com.zhly.mapper;

import com.zhly.entity.TravelRoute;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 旅游路线数据访问层
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface TravelRouteMapper extends BaseMapper<TravelRoute> {
    
    /**
     * 获取攻略的所有路线
     */
    List<TravelRoute> selectPlanRoutes(@Param("planId") Long planId);
    
    /**
     * 获取攻略某天的路线
     */
    List<TravelRoute> selectPlanDayRoutes(@Param("planId") Long planId, @Param("dayNumber") Integer dayNumber);
    
    /**
     * 获取路线统计
     */
    Map<String, Object> selectRouteStatistics();
    
    /**
     * 更新路线顺序
     */
    int updateRouteOrder(@Param("id") Long id, @Param("orderNumber") Integer orderNumber);
    
    /**
     * 获取路线列表
     */
    List<TravelRoute> selectRouteList(@Param("offset") int offset, @Param("size") int size, 
                                     @Param("keyword") String keyword, @Param("startCity") String startCity, 
                                     @Param("endCity") String endCity, @Param("status") Integer status);
    
    /**
     * 获取路线总数
     */
    Long selectRouteCount(@Param("keyword") String keyword, @Param("startCity") String startCity, 
                         @Param("endCity") String endCity, @Param("status") Integer status);
}

