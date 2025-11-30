package com.zhly.admin.service;

import com.zhly.admin.entity.AdminUser;
import java.util.List;

/**
 * 管理端用户服务接口
 * 
 * @author zhly
 * @since 2024-01-01
 */
public interface AdminUserService {
    
    /**
     * 获取用户列表
     */
    List<AdminUser> getUserList(Integer page, Integer size, String keyword);
    
    /**
     * 根据ID获取用户
     */
    AdminUser getUserById(Long id);
    
    /**
     * 更新用户状态
     */
    void updateUserStatus(Long id, Integer status);
    
    /**
     * 更新用户信息
     */
    void updateUser(AdminUser user);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 批量删除用户
     */
    void batchDeleteUsers(List<Long> ids);
    
    /**
     * 导出用户数据
     */
    void exportUsers(jakarta.servlet.http.HttpServletResponse response, String format, 
                     String keyword, Integer userType, Integer role, Integer status);
    
    /**
     * 获取用户统计信息
     */
    java.util.Map<String, Object> getUserStatistics();
    
    /**
     * 批量更新用户状态
     */
    void batchUpdateStatus(java.util.List<Long> ids, Integer status);
    
    /**
     * 批量更新用户角色
     */
    void batchUpdateRole(java.util.List<Long> ids, Integer role);
    
    /**
     * 获取用户详情（包含行为数据）
     */
    java.util.Map<String, Object> getUserDetail(Long id);
    
    /**
     * 获取用户登录历史
     */
    java.util.List<java.util.Map<String, Object>> getUserLoginHistory(Long id, Integer page, Integer size);
    
    /**
     * 获取用户操作日志
     */
    java.util.List<java.util.Map<String, Object>> getUserOperationLogs(Long id, Integer page, Integer size);
    
    /**
     * 调整用户积分
     */
    void adjustUserPoints(Long id, Integer points, String reason);
    
    /**
     * 调整用户等级
     */
    void adjustUserLevel(Long id, Integer level, String reason);
}











