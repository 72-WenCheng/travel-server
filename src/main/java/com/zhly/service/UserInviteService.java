package com.zhly.service;

import java.util.Map;

/**
 * 用户邀请服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface UserInviteService {
    
    /**
     * 获取或生成用户的邀请码
     */
    String getOrGenerateInviteCode(Long userId);
    
    /**
     * 获取邀请信息（邀请码、统计等）
     */
    Map<String, Object> getInviteInfo(Long userId);
    
    /**
     * 验证邀请码并记录邀请关系
     * @param inviteCode 邀请码
     * @param inviteeId 被邀请人ID
     * @return 是否成功
     */
    boolean verifyAndRecordInvite(String inviteCode, Long inviteeId);
    
    /**
     * 奖励邀请人积分
     * @param inviteId 邀请记录ID
     */
    void rewardInviterPoints(Long inviteId);
}







































































