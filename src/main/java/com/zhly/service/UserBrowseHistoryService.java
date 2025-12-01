package com.zhly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhly.entity.UserBrowseHistory;

import java.util.List;

/**
 * 用户浏览历史服务接口
 */
public interface UserBrowseHistoryService extends IService<UserBrowseHistory> {
    
    /**
     * 添加浏览记录
     */
    default boolean addBrowseHistory(Long userId, Integer browseType, Long browseId, String browseTitle, String browseImage) {
        return addBrowseHistory(userId, browseType, browseId, browseTitle, browseImage, 0, null);
    }
    
    /**
     * 添加浏览记录（支持传入浏览时长）
     */
    default boolean addBrowseHistory(Long userId, Integer browseType, Long browseId, String browseTitle, String browseImage, Integer duration) {
        return addBrowseHistory(userId, browseType, browseId, browseTitle, browseImage, duration, null);
    }

    /**
     * 添加浏览记录（包含IP信息，用于地理统计）
     */
    boolean addBrowseHistory(Long userId, Integer browseType, Long browseId, String browseTitle, String browseImage, Integer duration, String ipAddress);
    
    /**
     * 更新浏览记录的停留时长
     * @param userId 用户ID
     * @param browseType 浏览类型：1-景点，2-攻略，3-文旅项目，4-活动
     * @param browseId 浏览对象ID
     * @param duration 停留时长（秒）
     * @return 是否更新成功
     */
    boolean updateBrowseDuration(Long userId, Integer browseType, Long browseId, Integer duration);
    
    /**
     * 获取用户最近浏览记录
     */
    List<UserBrowseHistory> getRecentHistory(Long userId, Integer limit);
    
    /**
     * 清除用户浏览历史
     */
    boolean clearHistory(Long userId);
}







