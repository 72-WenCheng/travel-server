package com.zhly.mapper;

import com.zhly.entity.AiGenerateLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * AI生成记录数据访问层
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface AiGenerateLogMapper extends BaseMapper<AiGenerateLog> {
    
    /**
     * 获取AI日志列表
     */
    List<AiGenerateLog> selectLogList(@Param("page") Integer page, 
                                      @Param("size") Integer size,
                                      @Param("offset") Integer offset,
                                      @Param("userId") Long userId,
                                      @Param("status") Integer status);
    
    /**
     * 获取AI日志总数
     */
    Long selectLogCount(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 获取AI统计信息
     */
    Map<String, Object> selectAiStatistics();
    
    /**
     * 获取用户AI使用统计
     */
    Map<String, Object> selectUserAiStats(@Param("userId") Long userId);
    
    /**
     * 清理过期日志
     */
    int cleanExpiredLogs(@Param("days") Integer days);
}









