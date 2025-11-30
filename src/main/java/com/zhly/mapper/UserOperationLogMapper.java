package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.UserOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户操作日志Mapper接口
 */
@Mapper
public interface UserOperationLogMapper extends BaseMapper<UserOperationLog> {
    
    /**
     * 获取用户操作日志（分页）
     */
    List<Map<String, Object>> selectUserOperationLogs(@Param("userId") Long userId,
                                                      @Param("page") Integer page,
                                                      @Param("size") Integer size);
    
    /**
     * 获取系统操作日志（分页，不限制用户）
     */
    List<Map<String, Object>> selectSystemOperationLogs(@Param("page") Integer page,
                                                         @Param("size") Integer size);
    
    /**
     * 获取系统操作日志总数
     */
    Long selectSystemOperationLogsCount();
    
    /**
     * 记录操作日志
     */
    int insertOperationLog(UserOperationLog log);
}

