package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 通知Mapper
 * 
 * @author zhly
 * @since 2025-10-24
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    
    /**
     * 获取未读数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    Integer getUnreadCount(Long userId);
    
    /**
     * 标记全部已读
     */
    @Update("UPDATE notification SET is_read = 1, read_time = NOW() WHERE user_id = #{userId} AND is_read = 0")
    void markAllAsRead(Long userId);
}
