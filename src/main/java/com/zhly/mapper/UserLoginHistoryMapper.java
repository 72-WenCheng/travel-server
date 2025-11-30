package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.UserLoginHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户登录历史Mapper接口
 */
@Mapper
public interface UserLoginHistoryMapper extends BaseMapper<UserLoginHistory> {
    
    /**
     * 获取用户登录历史（分页）
     */
    List<Map<String, Object>> selectUserLoginHistory(@Param("userId") Long userId,
                                                      @Param("page") Integer page,
                                                      @Param("size") Integer size);
    
    /**
     * 记录登录历史
     */
    int insertLoginHistory(UserLoginHistory history);
}












































































