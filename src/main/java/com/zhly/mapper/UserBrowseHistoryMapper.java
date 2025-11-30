package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.UserBrowseHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户浏览历史Mapper接口
 */
@Mapper
public interface UserBrowseHistoryMapper extends BaseMapper<UserBrowseHistory> {
    
    /**
     * 获取用户最近浏览记录
     */
    List<UserBrowseHistory> selectRecentByUserId(Long userId, Integer limit);
}







