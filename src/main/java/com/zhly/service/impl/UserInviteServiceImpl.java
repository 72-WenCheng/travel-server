package com.zhly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhly.entity.UserInvite;
import com.zhly.mapper.UserInviteMapper;
import com.zhly.service.IUserPointsService;
import com.zhly.service.UserInviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户邀请服务实现
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class UserInviteServiceImpl extends ServiceImpl<UserInviteMapper, UserInvite> implements UserInviteService {
    
    @Autowired
    private UserInviteMapper userInviteMapper;
    
    @Autowired(required = false)
    private IUserPointsService userPointsService;
    
    @Override
    public String getOrGenerateInviteCode(Long userId) {
        // 查询是否已有邀请码（查找该用户作为邀请人的记录，且invitee_id为null的记录，用于存储邀请码）
        QueryWrapper<UserInvite> wrapper = new QueryWrapper<>();
        wrapper.eq("inviter_id", userId)
               .isNull("invitee_id")
               .isNotNull("invite_code")
               .last("LIMIT 1");
        UserInvite existing = userInviteMapper.selectOne(wrapper);
        
        if (existing != null && existing.getInviteCode() != null) {
            return existing.getInviteCode();
        }
        
        // 生成新的邀请码（使用用户ID + 随机字符串）
        String inviteCode = "INV" + String.format("%06d", userId) + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // 创建邀请记录（用于存储邀请码，invitee_id为null表示这是邀请码记录，不是邀请关系记录）
        UserInvite invite = new UserInvite();
        invite.setInviterId(userId);
        invite.setInviteeId(null);  // 邀请码记录，还没有被邀请人
        invite.setInviteCode(inviteCode);
        invite.setRewarded(0);
        invite.setCreateTime(LocalDateTime.now());
        userInviteMapper.insert(invite);
        
        return inviteCode;
    }
    
    @Override
    public Map<String, Object> getInviteInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取或生成邀请码
        String inviteCode = getOrGenerateInviteCode(userId);
        result.put("inviteCode", inviteCode);
        
        // 统计邀请人数（只统计invitee_id不为null的记录，即实际邀请关系）
        QueryWrapper<UserInvite> statsWrapper = new QueryWrapper<>();
        statsWrapper.eq("inviter_id", userId)
                   .isNotNull("invitee_id");
        Long totalInvited = userInviteMapper.selectCount(statsWrapper);
        
        result.put("stats", Map.of(
            "totalInvited", totalInvited != null ? totalInvited : 0L,
            "totalPoints", totalInvited != null ? totalInvited * 50 : 0L  // 每个邀请50积分
        ));
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyAndRecordInvite(String inviteCode, Long inviteeId) {
        if (inviteCode == null || inviteCode.trim().isEmpty() || inviteeId == null) {
            return false;
        }
        
        // 查询邀请码记录（invitee_id为null的记录）
        QueryWrapper<UserInvite> codeWrapper = new QueryWrapper<>();
        codeWrapper.eq("invite_code", inviteCode)
                   .isNull("invitee_id");
        UserInvite inviteCodeRecord = userInviteMapper.selectOne(codeWrapper);
        
        if (inviteCodeRecord == null) {
            System.out.println("ℹ️ 邀请码不存在: " + inviteCode);
            return false;
        }
        
        Long inviterId = inviteCodeRecord.getInviterId();
        
        // 检查是否是自己邀请自己
        if (inviterId != null && inviterId.equals(inviteeId)) {
            System.out.println("ℹ️ 不能使用自己的邀请码");
            return false;
        }
        
        // 检查是否已经被这个邀请码邀请过（防止重复奖励）
        QueryWrapper<UserInvite> existingWrapper = new QueryWrapper<>();
        existingWrapper.eq("invite_code", inviteCode)
                      .eq("invitee_id", inviteeId);
        UserInvite existing = userInviteMapper.selectOne(existingWrapper);
        if (existing != null) {
            System.out.println("ℹ️ 该用户已使用过此邀请码");
            return false;  // 已经记录过了
        }
        
        // 创建邀请关系记录（invitee_id不为null，表示这是实际的邀请关系）
        UserInvite newInvite = new UserInvite();
        newInvite.setInviterId(inviterId);
        newInvite.setInviteeId(inviteeId);
        newInvite.setInviteCode(inviteCode);
        newInvite.setRewarded(0);
        newInvite.setCreateTime(LocalDateTime.now());
        userInviteMapper.insert(newInvite);
        
        // 奖励邀请人积分
        rewardInviterPoints(newInvite.getId());
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rewardInviterPoints(Long inviteId) {
        try {
            UserInvite invite = userInviteMapper.selectById(inviteId);
            if (invite == null || invite.getRewarded() == 1) {
                return;  // 已经奖励过了
            }
            
            Long inviterId = invite.getInviterId();
            if (inviterId == null || userPointsService == null) {
                return;
            }
            
            // 奖励50积分
            userPointsService.addPoints(
                inviterId,
                50,  // 奖励50积分
                8,   // 行为类型：8-邀请好友
                "邀请好友注册成功",
                "invite",  // 关联类型
                inviteId   // 关联ID
            );
            
            // 标记为已奖励
            invite.setRewarded(1);
            invite.setRewardTime(LocalDateTime.now());
            userInviteMapper.updateById(invite);
            
            System.out.println("✅ 用户 " + inviterId + " 邀请好友成功，已奖励50积分，邀请记录ID: " + inviteId);
        } catch (Exception e) {
            System.err.println("⚠️ 邀请好友积分奖励失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

