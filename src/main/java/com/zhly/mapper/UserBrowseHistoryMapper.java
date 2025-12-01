package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.UserBrowseHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户浏览历史Mapper接口
 */
@Mapper
public interface UserBrowseHistoryMapper extends BaseMapper<UserBrowseHistory> {
    
    /**
     * 获取用户最近浏览记录
     */
    List<UserBrowseHistory> selectRecentByUserId(Long userId, Integer limit);

    /**
     * 获取按国家聚合的访问统计数据
     */
    @Select("""
            SELECT 
                CASE 
                  WHEN country_code IS NULL OR country_code = '' THEN 'CN' 
                  ELSE country_code 
                END AS countryCode,
                CASE 
                  WHEN country_name IS NULL OR country_name = '' THEN '中国' 
                  ELSE country_name 
                END AS countryName,
                COUNT(*) AS visits,
                SUM(CASE WHEN browse_type = 2 THEN 1 ELSE 0 END) AS queries,
                AVG(COALESCE(duration, 0)) AS avgDuration
            FROM user_browse_history
            GROUP BY country_code, country_name
            """)
    List<Map<String, Object>> selectWorldTrafficAggregates();
}







