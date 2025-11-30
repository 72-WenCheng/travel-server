package com.zhly.mapper;

import com.zhly.entity.TravelPlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 旅游攻略数据访问层
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface TravelPlanMapper extends BaseMapper<TravelPlan> {
    
    /**
     * 获取攻略列表（带分页和搜索）- 旧版本，保留兼容性
     */
    List<TravelPlan> selectPlanList(@Param("page") Integer page, 
                                   @Param("size") Integer size, 
                                   @Param("keyword") String keyword,
                                   @Param("destination") String destination,
                                   @Param("status") Integer status);
    
    /**
     * 获取攻略总数 - 旧版本，保留兼容性
     */
    Long selectPlanCount(@Param("keyword") String keyword,
                        @Param("destination") String destination,
                        @Param("status") Integer status);
    
    /**
     * 获取攻略列表（支持所有筛选条件）- 新版本
     */
    List<TravelPlan> selectPlanListWithFilters(
        @Param("page") Integer page,
        @Param("size") Integer size,
        @Param("keyword") String keyword,
        @Param("destination") String destination,
        @Param("status") Integer status,
        @Param("auditStatus") Integer auditStatus,
        @Param("authorId") Long authorId,
        @Param("authorName") String authorName,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 获取攻略总数（支持所有筛选条件）- 新版本
     */
    Long selectPlanCountWithFilters(
        @Param("keyword") String keyword,
        @Param("destination") String destination,
        @Param("status") Integer status,
        @Param("auditStatus") Integer auditStatus,
        @Param("authorId") Long authorId,
        @Param("authorName") String authorName,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 获取热门攻略
     */
    List<TravelPlan> selectHotPlans(@Param("limit") Integer limit);
    
    /**
     * 获取用户攻略列表
     */
    List<TravelPlan> selectUserPlans(@Param("userId") Long userId,
                                    @Param("page") Integer page,
                                    @Param("size") Integer size);
    
    /**
     * 获取最新攻略
     */
    List<TravelPlan> selectLatestPlans(@Param("limit") Integer limit);
    
    /**
     * 获取攻略详情（关联用户表获取作者信息）
     */
    TravelPlan selectPlanByIdWithAuthor(@Param("id") Long id);
    
}










