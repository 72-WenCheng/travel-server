package com.zhly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhly.entity.UserInvite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户邀请Mapper
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Mapper
public interface UserInviteMapper extends BaseMapper<UserInvite> {
    
    /**
     * 根据邀请码查询邀请记录
     */
    UserInvite selectByInviteCode(@Param("inviteCode") String inviteCode);
    
    /**
     * 根据邀请人ID查询邀请列表
     */
    List<UserInvite> selectByInviterId(@Param("inviterId") Long inviterId);
    
    /**
     * 统计邀请人数
     */
    Long countByInviterId(@Param("inviterId") Long inviterId);
}







































































