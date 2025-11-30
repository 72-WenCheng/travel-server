package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告Mapper
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}

