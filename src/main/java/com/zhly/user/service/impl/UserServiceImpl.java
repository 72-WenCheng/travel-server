package com.zhly.user.service.impl;

import com.zhly.user.service.UserService;
import com.zhly.entity.User;
import com.zhly.entity.UserPoints;
import com.zhly.mapper.UserMapper;
import com.zhly.mapper.UserPointsMapper;
import com.zhly.util.JwtUtil;
import com.zhly.entity.UserDeactivateRequest;
import com.zhly.service.UserDeactivationManager;
import com.zhly.util.OnlineUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * 用户端用户服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service("userClientService")
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private com.zhly.service.FileUploadService fileUploadService;
    
    @Autowired
    private UserPointsMapper userPointsMapper;
    
    @Autowired
    private com.zhly.service.IUserPointsService userPointsService;
    
    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private UserDeactivationManager userDeactivationManager;
    
    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 参数验证
            if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
                result.put("success", false);
                result.put("message", "用户名和密码不能为空");
                return result;
            }
            
            // 检查用户名是否已存在
            User existingUser = userMapper.selectByUsername(user.getUsername());
            if (existingUser != null) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }
            
            // 检查邮箱是否已存在
            if (StringUtils.hasText(user.getEmail())) {
                User existingEmail = userMapper.selectByEmail(user.getEmail());
                if (existingEmail != null) {
                    result.put("success", false);
                    result.put("message", "邮箱已被注册");
                    return result;
                }
            }
            
            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setStatus(1); // 正常状态
            
            // 保存用户
            int insertResult = userMapper.insert(user);
            if (insertResult > 0) {
                result.put("success", true);
                result.put("message", "注册成功");
                result.put("userId", user.getId());
            } else {
                result.put("success", false);
                result.put("message", "注册失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "注册失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 参数验证
            if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                result.put("success", false);
                result.put("message", "用户名和密码不能为空");
                return result;
            }
            
            // 查询用户
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 登录前检查是否存在已到期的注销申请
            UserDeactivateRequest pendingRequest = userDeactivationManager.getPendingRequest(user.getId());
            if (userDeactivationManager.finalizeIfExpired(pendingRequest)) {
                result.put("success", false);
                result.put("message", "账号已超过7天未登录，注销申请已生效，如需使用请重新注册");
                return result;
            }

            // 检查用户状态
            if (user.getStatus() != 1) {
                result.put("success", false);
                result.put("message", "用户已被禁用");
                return result;
            }
            
            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                result.put("success", false);
                result.put("message", "密码错误");
                return result;
            }

            // 成功登录后，如仍处于冷静期，则视为取消注销
            if (pendingRequest != null) {
                userDeactivationManager.cancelPendingRequest(pendingRequest);
            }
            
            // 生成JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            
            // 更新最后登录时间和IP
            updateLastLoginInfo(user.getId());
            
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("userInfo", user);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "登录失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 更新用户最后登录信息
     */
    private void updateLastLoginInfo(Long userId) {
        try {
            // 获取客户端IP地址
            String clientIp = getClientIp();
            
            // 更新最后登录时间和IP
            userMapper.updateLastLoginInfo(userId, clientIp);
            
            System.out.println("更新用户登录信息成功 - 用户ID: " + userId + ", IP: " + clientIp);
        } catch (Exception e) {
            System.err.println("更新用户登录信息失败: " + e.getMessage());
            // 不抛出异常，避免影响登录流程
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp() {
        try {
            // 这里简化处理，实际应该从HttpServletRequest中获取
            // 对于测试环境，返回默认IP
            return "127.0.0.1";
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    @Override
    public User getUserByToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return null;
            }
            
            // 验证token
            if (!jwtUtil.validateToken(token)) {
                return null;
            }
            
            // 从token中获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return null;
            }
            
            // 查询用户信息
            User user = userMapper.selectById(userId);
            if (user != null && user.getStatus() == 1) {
                return user;
            }
            
        } catch (Exception e) {
            // 记录日志
            System.err.println("获取用户信息失败: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public void updateUserInfo(String token, User user) {
        try {
            User currentUser = getUserByToken(token);
            if (currentUser == null) {
                throw new RuntimeException("用户未登录");
            }
            
            // 检查是否是首次完善资料（判断头像、昵称等关键信息是否为空）
            boolean isFirstTimeComplete = (currentUser.getAvatar() == null || currentUser.getAvatar().isEmpty()) &&
                                        (currentUser.getNickname() == null || currentUser.getNickname().isEmpty()) &&
                                        (user.getAvatar() != null && !user.getAvatar().isEmpty()) &&
                                        (user.getNickname() != null && !user.getNickname().isEmpty());
            
            // 更新用户信息
            user.setId(currentUser.getId());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            // 首次完善资料给予10积分奖励（行为类型7：完善资料）
            if (isFirstTimeComplete) {
                try {
                    userPointsService.addPoints(currentUser.getId(), 10, 7, "首次完善个人资料", null, null);
                } catch (Exception e) {
                    // 积分发放失败不影响资料更新
                    System.err.println("发放首次完善资料积分失败: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("更新用户信息失败: " + e.getMessage());
        }
    }
    
    @Override
    public void setUserTags(String token, Map<String, Object> tags) {
        try {
            User currentUser = getUserByToken(token);
            if (currentUser == null) {
                throw new RuntimeException("用户未登录");
            }
            
            // 这里应该实现标签设置逻辑
            // 可以保存到user_tag表或用户实体的interest_tags字段
            System.out.println("设置用户标签: " + tags);
            
        } catch (Exception e) {
            throw new RuntimeException("设置用户标签失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserTags(String token) {
        Map<String, Object> tags = new HashMap<>();
        
        try {
            User currentUser = getUserByToken(token);
            if (currentUser == null) {
                return tags;
            }
            
            // 模拟标签数据，实际应该从数据库查询
            tags.put("interests", Arrays.asList("美食", "文化", "自然风光"));
            tags.put("travelPreference", "舒适型");
            tags.put("budget", "中等消费");
            tags.put("travelStyle", "休闲游");
            
        } catch (Exception e) {
            System.err.println("获取用户标签失败: " + e.getMessage());
        }
        
        return tags;
    }
    
    @Override
    public void updateUserPreferences(String token, Map<String, Object> preferences) {
        try {
            User currentUser = getUserByToken(token);
            if (currentUser == null) {
                throw new RuntimeException("用户未登录");
            }
            
            // 更新用户偏好
            User user = new User();
            user.setId(currentUser.getId());
            user.setTravelPreference((Integer) preferences.get("travelPreference"));
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            
        } catch (Exception e) {
            throw new RuntimeException("更新用户偏好失败: " + e.getMessage());
        }
    }
    
    @Override
    public Object getUserRecommendations(String token) {
        try {
            User currentUser = getUserByToken(token);
            if (currentUser == null) {
                return null;
            }
            
            // 基于用户标签和偏好生成推荐内容
            return userMapper.selectUserRecommendations(currentUser.getId());
            
        } catch (Exception e) {
            System.err.println("获取用户推荐失败: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Map<String, String> uploadAvatar(String token, org.springframework.web.multipart.MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        
        try {
            User currentUser = getUserByToken(token);
            if (currentUser == null) {
                throw new RuntimeException("用户未登录");
            }
            
            // 使用FileUploadService上传头像
            Map<String, Object> uploadResult = fileUploadService.uploadAvatar(file);
            
            // 获取文件URL
            String avatarUrl = (String) uploadResult.get("fileUrl");
            
            // 更新用户头像
            User user = new User();
            user.setId(currentUser.getId());
            user.setAvatar(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            result.put("avatar", avatarUrl);
            
        } catch (Exception e) {
            throw new RuntimeException("上传头像失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public void changePassword(String token, String oldPassword, String newPassword) {
        try {
            User currentUser = getUserByToken(token);
            if (currentUser == null) {
                throw new RuntimeException("用户未登录");
            }
            
            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
                throw new RuntimeException("原密码错误");
            }
            
            // 验证新密码长度
            if (newPassword.length() < 6) {
                throw new RuntimeException("新密码长度至少6位");
            }
            
            // 更新密码
            User user = new User();
            user.setId(currentUser.getId());
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            
        } catch (Exception e) {
            throw new RuntimeException("修改密码失败: " + e.getMessage());
        }
    }
    
    @Override
    public void logout(String token) {
        try {
            // 尝试从token中获取用户ID，标记用户下线
            // 即使token已过期，也尝试解析用户ID（因为用户确实要退出）
            Long userId = null;
            try {
                // 先尝试验证token
                if (jwtUtil.validateToken(token)) {
                    userId = jwtUtil.getUserIdFromToken(token);
                } else {
                    // token无效或过期，尝试直接解析（可能token刚过期）
                    try {
                        userId = jwtUtil.getUserIdFromToken(token);
                    } catch (Exception e) {
                        System.out.println("Token已过期，无法解析用户ID: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("解析Token失败: " + e.getMessage());
            }
            
            // 如果成功获取到用户ID，标记用户下线
            if (userId != null) {
                try {
                    onlineUserManager.markOffline(userId);
                    System.out.println("✅ 用户退出登录成功，用户ID: " + userId + "，已标记下线");
                } catch (Exception e) {
                    System.err.println("❌ 标记用户下线失败: " + e.getMessage());
                    // 即使标记下线失败，也继续执行退出流程
                }
            } else {
                System.out.println("⚠️ 无法获取用户ID，跳过标记下线");
            }
            
            // 可以在这里实现：记录退出日志、清理session等
            
        } catch (Exception e) {
            // 即使出现异常，也不抛出，允许退出流程继续
            System.err.println("退出登录处理异常: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserDetailWithPortrait(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取用户基本信息
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            result.put("id", user.getId());
            result.put("nickname", user.getNickname());
            result.put("username", user.getUsername());
            result.put("avatar", user.getAvatar());
            result.put("email", user.getEmail());
            result.put("phone", user.getPhone());
            result.put("age", user.getAge());
            result.put("gender", user.getGender());
            result.put("userType", user.getUserType());
            
            // 从user_points表获取真实的积分（根据升级指南，等级由前端根据积分计算）
            UserPoints userPoints = userPointsMapper.getByUserId(userId);
            if (userPoints != null) {
                // 只返回积分，等级由前端根据升级指南的规则计算
                result.put("points", userPoints.getTotalPoints() != null ? userPoints.getTotalPoints() : 0);
            } else {
                // 如果积分表没有数据，默认0积分
                result.put("points", 0);
            }
            
            // 构建用户画像信息
            Map<String, Object> portrait = new HashMap<>();
            
            // 从用户表的字段获取画像数据
            if (user.getInterestTags() != null && !user.getInterestTags().isEmpty()) {
                String interestTags = user.getInterestTags().trim();
                List<String> tags = new ArrayList<>();
                
                // 如果是JSON数组格式
                if (interestTags.startsWith("[") && interestTags.endsWith("]")) {
                    try {
                        // 尝试解析JSON数组
                        String jsonContent = interestTags.substring(1, interestTags.length() - 1);
                        if (!jsonContent.isEmpty()) {
                            String[] jsonTags = jsonContent.split(",");
                            for (String tag : jsonTags) {
                                String cleanedTag = tag.trim().replace("\"", "").replace("'", "");
                                if (!cleanedTag.isEmpty()) {
                                    tags.add(cleanedTag);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // JSON解析失败，按逗号分隔
                        String[] parts = interestTags.split("[,，、]");
                        for (String part : parts) {
                            String cleaned = part.trim();
                            if (!cleaned.isEmpty() && !cleaned.equals("[") && !cleaned.equals("]")) {
                                tags.add(cleaned.replace("\"", "").replace("'", ""));
                            }
                        }
                    }
                } else {
                    // 普通逗号或顿号分隔的字符串
                    String[] parts = interestTags.split("[,，、]");
                    for (String part : parts) {
                        String cleaned = part.trim();
                        if (!cleaned.isEmpty()) {
                            tags.add(cleaned);
                        }
                    }
                }
                
                portrait.put("interestTags", tags);
            } else {
                portrait.put("interestTags", new ArrayList<>());
            }
            
            // 将旅游偏好数字转换为文本
            if (user.getTravelPreference() != null) {
                String preferenceText = getTravelPreferenceText(user.getTravelPreference());
                portrait.put("travelPreference", preferenceText);
            } else {
                portrait.put("travelPreference", null);
            }
            // consumptionLevel可能不存在于User实体中，暂时注释
            // portrait.put("consumptionLevel", user.getConsumptionLevel());
            
            if (user.getAge() != null) {
                portrait.put("ageGroup", user.getAge() + "岁");
            }
            
            result.put("portrait", portrait);
            
        } catch (Exception e) {
            throw new RuntimeException("获取用户详情失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public void deactivateAccount(String token, String reason) {
        try {
            User currentUser = getUserByToken(token);
            if (currentUser == null) {
                throw new RuntimeException("用户未登录");
            }

            userDeactivationManager.scheduleDeactivation(currentUser.getId(), reason);

            try {
                onlineUserManager.markOffline(currentUser.getId());
            } catch (Exception e) {
                System.err.println("标记用户下线失败: " + e.getMessage());
            }

            System.out.println("用户提交注销申请，ID: " + currentUser.getId() + ", 原因: " + (reason != null ? reason : "未填写"));
        } catch (Exception e) {
            throw new RuntimeException("注销申请提交失败: " + e.getMessage());
        }
    }
    
    /**
     * 将旅游偏好数字转换为文本
     */
    private String getTravelPreferenceText(Integer preference) {
        if (preference == null) {
            return null;
        }
        switch (preference) {
            case 1:
                return "自然风光";
            case 2:
                return "人文历史";
            case 3:
                return "美食体验";
            case 4:
                return "购物娱乐";
            case 5:
                return "休闲度假";
            case 6:
                return "冒险探索";
            default:
                return "未知类型(" + preference + ")";
        }
    }
}

