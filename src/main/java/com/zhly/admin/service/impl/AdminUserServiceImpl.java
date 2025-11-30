package com.zhly.admin.service.impl;

import com.zhly.admin.service.AdminUserService;
import com.zhly.admin.entity.AdminUser;
import com.zhly.admin.mapper.AdminUserMapper;
import com.zhly.mapper.UserLoginHistoryMapper;
import com.zhly.mapper.UserOperationLogMapper;
import com.zhly.mapper.UserBrowseHistoryMapper;
import com.zhly.mapper.UserCollectMapper;
import com.zhly.mapper.CommentMapper;
import com.zhly.mapper.PointsLogMapper;
import com.zhly.mapper.OrderMapper;
import com.zhly.entity.PointsLog;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理端用户服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    @Autowired(required = false)
    private UserLoginHistoryMapper userLoginHistoryMapper;
    
    @Autowired(required = false)
    private UserOperationLogMapper userOperationLogMapper;
    
    @Autowired(required = false)
    private UserBrowseHistoryMapper userBrowseHistoryMapper;
    
    @Autowired(required = false)
    private UserCollectMapper userCollectMapper;
    
    @Autowired(required = false)
    private CommentMapper commentMapper;
    
    @Autowired(required = false)
    private PointsLogMapper pointsLogMapper;
    
    @Autowired(required = false)
    private OrderMapper orderMapper;
    
    @Override
    public List<AdminUser> getUserList(Integer page, Integer size, String keyword) {
        try {
            return adminUserMapper.selectUserList(page, size, keyword);
        } catch (Exception e) {
            throw new RuntimeException("获取用户列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public AdminUser getUserById(Long id) {
        try {
            AdminUser user = adminUserMapper.selectById(id);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            return user;
        } catch (Exception e) {
            throw new RuntimeException("获取用户详情失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        try {
            LambdaUpdateWrapper<AdminUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AdminUser::getId, id)
                        .set(AdminUser::getStatus, status);
            adminUserMapper.update(null, updateWrapper);
        } catch (Exception e) {
            throw new RuntimeException("更新用户状态失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(AdminUser user) {
        try {
            // 检查用户是否存在
            AdminUser existingUser = adminUserMapper.selectById(user.getId());
            if (existingUser == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 防止修改的字段：id, username, password, create_time, deleted
            // 只能修改的字段：nickname, email, phone, userType, travelPreference, interestTags, frequentCities, avatar
            
            LambdaUpdateWrapper<AdminUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AdminUser::getId, user.getId());
            
            // 只更新允许修改的字段
            if (user.getNickname() != null) {
                updateWrapper.set(AdminUser::getNickname, user.getNickname());
            }
            if (user.getEmail() != null) {
                updateWrapper.set(AdminUser::getEmail, user.getEmail());
            }
            if (user.getPhone() != null) {
                updateWrapper.set(AdminUser::getPhone, user.getPhone());
            }
            if (user.getUserType() != null) {
                updateWrapper.set(AdminUser::getUserType, user.getUserType());
            }
            if (user.getTravelPreference() != null) {
                updateWrapper.set(AdminUser::getTravelPreference, user.getTravelPreference());
            }
            if (user.getInterestTags() != null) {
                updateWrapper.set(AdminUser::getInterestTags, user.getInterestTags());
            }
            if (user.getFrequentCities() != null) {
                updateWrapper.set(AdminUser::getFrequentCities, user.getFrequentCities());
            }
            if (user.getAvatar() != null) {
                updateWrapper.set(AdminUser::getAvatar, user.getAvatar());
            }
            
            int result = adminUserMapper.update(null, updateWrapper);
            if (result <= 0) {
                throw new RuntimeException("更新用户信息失败");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("更新用户信息失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        try {
            // 检查用户是否存在
            AdminUser user = adminUserMapper.selectById(id);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 检查用户状态：只有禁用状态的用户才能删除
            if (user.getStatus() == null || user.getStatus() != 0) {
                throw new RuntimeException("只能删除禁用状态的用户，请先禁用该用户");
            }
            
            // 物理删除：真正从数据库中删除
            int result = adminUserMapper.deleteById(id);
            
            if (result <= 0) {
                throw new RuntimeException("删除用户失败，可能用户不存在");
            }
        } catch (RuntimeException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("删除用户失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                throw new RuntimeException("删除的用户ID列表不能为空");
            }
            
            // 检查所有用户是否都是禁用状态
            for (Long id : ids) {
                AdminUser user = adminUserMapper.selectById(id);
                if (user == null) {
                    throw new RuntimeException("用户ID " + id + " 不存在");
                }
                if (user.getStatus() == null || user.getStatus() != 0) {
                    throw new RuntimeException("用户 " + user.getUsername() + " 不是禁用状态，无法删除");
                }
            }
            
            // 批量物理删除
            for (Long id : ids) {
                int result = adminUserMapper.deleteById(id);
                if (result <= 0) {
                    throw new RuntimeException("删除用户ID " + id + " 失败");
                }
            }
        } catch (RuntimeException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("批量删除用户失败: " + e.getMessage());
        }
    }
    
    @Override
    public void exportUsers(HttpServletResponse response, String format, 
                           String keyword, Integer userType, Integer role, Integer status) {
        try {
            // 获取用户列表（不分页，获取所有符合条件的用户）
            List<AdminUser> users = adminUserMapper.selectUserListForExport(keyword, userType, role, status);
            
            if ("csv".equals(format)) {
                exportToCSV(response, users);
            } else {
                exportToExcel(response, users);
            }
        } catch (Exception e) {
            throw new RuntimeException("导出用户数据失败: " + e.getMessage());
        }
    }
    
    private void exportToCSV(HttpServletResponse response, List<AdminUser> users) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=users_" + 
                          LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv");
        response.setCharacterEncoding("UTF-8");
        
        // 添加BOM以支持Excel正确识别UTF-8
        response.getOutputStream().write(0xEF);
        response.getOutputStream().write(0xBB);
        response.getOutputStream().write(0xBF);
        
        PrintWriter writer = response.getWriter();
        
        // 写入表头
        writer.println("ID,用户名,昵称,邮箱,手机号,个性化标签,用户角色,旅游偏好,积分,等级,状态,注册时间,最后登录时间");
        
        // 写入数据
        for (AdminUser user : users) {
            writer.println(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%d,%d,%s,%s,%s",
                user.getId(),
                escapeCSV(user.getUsername()),
                escapeCSV(user.getNickname()),
                escapeCSV(user.getEmail()),
                escapeCSV(user.getPhone()),
                getUserTypeText(user.getUserType()),
                getRoleText(user.getRole()),
                getTravelPreferenceText(user.getTravelPreference()),
                user.getPoints() != null ? user.getPoints() : 0,
                user.getLevel() != null ? user.getLevel() : 0,
                user.getStatus() != null && user.getStatus() == 1 ? "正常" : "禁用",
                user.getCreateTime() != null ? user.getCreateTime().toString() : "",
                user.getLastLoginTime() != null ? user.getLastLoginTime().toString() : ""
            ));
        }
        
        writer.flush();
        writer.close();
    }
    
    private void exportToExcel(HttpServletResponse response, List<AdminUser> users) throws IOException {
        // 简化版Excel导出（使用CSV格式，Excel可以打开）
        exportToCSV(response, users);
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private String getUserTypeText(Integer userType) {
        if (userType == null) return "";
        Map<Integer, String> types = Map.of(1, "个人", 2, "情侣", 3, "家庭", 4, "团队");
        return types.getOrDefault(userType, "");
    }
    
    private String getRoleText(Integer role) {
        if (role == null) return "";
        Map<Integer, String> roles = Map.of(1, "管理员", 2, "普通用户");
        return roles.getOrDefault(role, "");
    }
    
    private String getTravelPreferenceText(Integer preference) {
        if (preference == null) return "";
        Map<Integer, String> preferences = Map.of(1, "自然风光", 2, "人文历史", 3, "美食体验", 4, "休闲度假");
        return preferences.getOrDefault(preference, "");
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserStatistics() {
        try {
            Object statsObj = adminUserMapper.selectUserStatistics();
            Map<String, Object> stats;
            if (statsObj instanceof Map) {
                stats = (Map<String, Object>) statsObj;
            } else {
                stats = new HashMap<>();
            }
            
            // 补充今日新增用户数
            QueryWrapper<AdminUser> todayWrapper = new QueryWrapper<>();
            todayWrapper.ge("create_time", LocalDate.now().atStartOfDay());
            todayWrapper.eq("deleted", 0);
            Long todayNew = adminUserMapper.selectCount(todayWrapper);
            stats.put("todayNew", todayNew != null ? todayNew : 0L);
            
            // VIP用户数（假设level >= 5为VIP）
            QueryWrapper<AdminUser> vipWrapper = new QueryWrapper<>();
            vipWrapper.ge("level", 5);
            vipWrapper.eq("deleted", 0);
            Long vipUsers = adminUserMapper.selectCount(vipWrapper);
            stats.put("vipUsers", vipUsers != null ? vipUsers : 0L);
            
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取用户统计信息失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        try {
            if (ids == null || ids.isEmpty()) {
                throw new RuntimeException("用户ID列表不能为空");
            }
            
            LambdaUpdateWrapper<AdminUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(AdminUser::getId, ids)
                         .set(AdminUser::getStatus, status)
                         .set(AdminUser::getUpdateTime, LocalDateTime.now());
            adminUserMapper.update(null, updateWrapper);
        } catch (Exception e) {
            throw new RuntimeException("批量更新状态失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateRole(List<Long> ids, Integer role) {
        try {
            if (ids == null || ids.isEmpty()) {
                throw new RuntimeException("用户ID列表不能为空");
            }
            
            LambdaUpdateWrapper<AdminUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(AdminUser::getId, ids)
                         .set(AdminUser::getRole, role)
                         .set(AdminUser::getUpdateTime, LocalDateTime.now());
            adminUserMapper.update(null, updateWrapper);
        } catch (Exception e) {
            throw new RuntimeException("批量更新角色失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserDetail(Long id) {
        try {
            AdminUser user = adminUserMapper.selectById(id);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            Map<String, Object> detail = new HashMap<>();
            detail.put("basicInfo", user);
            
            // 浏览历史统计
            Map<String, Object> browseStats = new HashMap<>();
            if (userBrowseHistoryMapper != null) {
                QueryWrapper<com.zhly.entity.UserBrowseHistory> browseWrapper = new QueryWrapper<>();
                browseWrapper.eq("user_id", id);
                Long totalBrowses = userBrowseHistoryMapper.selectCount(browseWrapper);
                browseStats.put("totalBrowses", totalBrowses != null ? totalBrowses : 0L);
                
                // 按类型统计
                browseWrapper.clear();
                browseWrapper.eq("user_id", id).eq("browse_type", 1);
                Long attractionBrowses = userBrowseHistoryMapper.selectCount(browseWrapper);
                browseStats.put("attractionBrowses", attractionBrowses != null ? attractionBrowses : 0L);
                
                browseWrapper.clear();
                browseWrapper.eq("user_id", id).eq("browse_type", 2);
                Long planBrowses = userBrowseHistoryMapper.selectCount(browseWrapper);
                browseStats.put("planBrowses", planBrowses != null ? planBrowses : 0L);
            } else {
                browseStats.put("totalBrowses", 0L);
                browseStats.put("attractionBrowses", 0L);
                browseStats.put("planBrowses", 0L);
            }
            detail.put("browseStats", browseStats);
            
            // 收藏数量
            if (userCollectMapper != null) {
                QueryWrapper<com.zhly.entity.UserCollect> collectWrapper = new QueryWrapper<>();
                collectWrapper.eq("user_id", id);
                Long collectCount = userCollectMapper.selectCount(collectWrapper);
                detail.put("collectCount", collectCount != null ? collectCount : 0L);
            } else {
                detail.put("collectCount", 0L);
            }
            
            // 评论数量
            if (commentMapper != null) {
                QueryWrapper<com.zhly.entity.Comment> commentWrapper = new QueryWrapper<>();
                commentWrapper.eq("user_id", id);
                Long commentCount = commentMapper.selectCount(commentWrapper);
                detail.put("commentCount", commentCount != null ? commentCount : 0L);
            } else {
                detail.put("commentCount", 0L);
            }
            
            // 订单统计
            if (orderMapper != null) {
                try {
                    Map<String, Object> orderStats = orderMapper.selectUserOrderStats(id);
                    if (orderStats != null) {
                        detail.put("orderCount", orderStats.get("total") != null ? orderStats.get("total") : 0L);
                        detail.put("totalOrderAmount", orderStats.get("totalAmount") != null ? orderStats.get("totalAmount") : 0.0);
                        detail.put("orderStats", orderStats);
                    } else {
                        detail.put("orderCount", 0L);
                        detail.put("totalOrderAmount", 0.0);
                        detail.put("orderStats", new HashMap<>());
                    }
                } catch (Exception e) {
                    detail.put("orderCount", 0L);
                    detail.put("totalOrderAmount", 0.0);
                    detail.put("orderStats", new HashMap<>());
                }
            } else {
                detail.put("orderCount", 0L);
                detail.put("totalOrderAmount", 0.0);
                detail.put("orderStats", new HashMap<>());
            }
            
            return detail;
        } catch (Exception e) {
            throw new RuntimeException("获取用户详情失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getUserLoginHistory(Long id, Integer page, Integer size) {
        try {
            if (userLoginHistoryMapper == null) {
                return new ArrayList<>();
            }
            return userLoginHistoryMapper.selectUserLoginHistory(id, page, size);
        } catch (Exception e) {
            throw new RuntimeException("获取登录历史失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> getUserOperationLogs(Long id, Integer page, Integer size) {
        try {
            if (userOperationLogMapper == null) {
                return new ArrayList<>();
            }
            return userOperationLogMapper.selectUserOperationLogs(id, page, size);
        } catch (Exception e) {
            throw new RuntimeException("获取操作日志失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustUserPoints(Long id, Integer points, String reason) {
        try {
            AdminUser user = adminUserMapper.selectById(id);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
            int newPoints = currentPoints + points;
            if (newPoints < 0) {
                throw new RuntimeException("积分不足，无法扣除");
            }
            
            LambdaUpdateWrapper<AdminUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AdminUser::getId, id)
                         .set(AdminUser::getPoints, newPoints)
                         .set(AdminUser::getUpdateTime, LocalDateTime.now());
            adminUserMapper.update(null, updateWrapper);
            
            // 记录积分变动日志
            if (pointsLogMapper != null) {
                PointsLog pointsLog = new PointsLog();
                pointsLog.setUserId(id);
                pointsLog.setPoints(points);
                pointsLog.setActionType(10); // 10-管理员调整
                pointsLog.setActionDesc(reason != null ? reason : "管理员调整积分");
                pointsLog.setBalanceAfter(newPoints);
                pointsLog.setCreateTime(LocalDateTime.now());
                pointsLogMapper.insert(pointsLog);
            }
        } catch (Exception e) {
            throw new RuntimeException("调整积分失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustUserLevel(Long id, Integer level, String reason) {
        try {
            AdminUser user = adminUserMapper.selectById(id);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            if (level < 1 || level > 10) {
                throw new RuntimeException("等级必须在1-10之间");
            }
            
            LambdaUpdateWrapper<AdminUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AdminUser::getId, id)
                         .set(AdminUser::getLevel, level)
                         .set(AdminUser::getUpdateTime, LocalDateTime.now());
            adminUserMapper.update(null, updateWrapper);
            
            // 记录等级变动日志（可以记录到操作日志表）
            if (userOperationLogMapper != null) {
                try {
                    com.zhly.entity.UserOperationLog log = new com.zhly.entity.UserOperationLog();
                    log.setUserId(id);
                    log.setOperationType("EDIT");
                    log.setOperationModule("USER");
                    log.setOperationContent("管理员调整用户等级：" + user.getLevel() + " -> " + level);
                    log.setOperationTime(LocalDateTime.now());
                    log.setStatus(1);
                    log.setRemark(reason != null ? reason : "管理员调整等级");
                    log.setCreateTime(LocalDateTime.now());
                    userOperationLogMapper.insert(log);
                } catch (Exception e) {
                    // 日志记录失败不影响主流程
                    System.err.println("记录等级变动日志失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("调整等级失败: " + e.getMessage());
        }
    }
}









