package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.UserMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户私信消息Mapper
 *
 * @author zhly
 * @since 2025-11-19
 */
@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {
}

