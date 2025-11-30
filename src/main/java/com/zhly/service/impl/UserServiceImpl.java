package com.zhly.service.impl;

import com.zhly.entity.User;
import com.zhly.mapper.UserMapper;
import com.zhly.security.LoginSecurityService;
import com.zhly.security.SecurityPolicy;
import com.zhly.security.SecurityPolicyService;
import com.zhly.service.UserService;
import com.zhly.service.EmailService;
import com.zhly.service.CaptchaService;
import com.zhly.service.UserInviteService;
import com.zhly.util.JwtUtil;
import com.zhly.util.OnlineUserManager;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private UserInviteService userInviteService;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private SecurityPolicyService securityPolicyService;

    @Autowired
    private LoginSecurityService loginSecurityService;

    @Override
    public boolean register(User user, String inviteCode) {
        System.out.println("开始注册流程...");

        try {
            // 参数验证
            System.out.println("1. 开始参数验证");
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                System.out.println("注册失败: 用户名为空");
                throw new RuntimeException("用户名不能为空");
            }
            System.out.println("✓ 用户名: " + user.getUsername());

            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                System.out.println("注册失败: 邮箱为空");
                throw new RuntimeException("邮箱不能为空");
            }
            System.out.println("✓ 邮箱: " + user.getEmail());

            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                System.out.println("注册失败: 密码为空");
                throw new RuntimeException("密码不能为空");
            }
            System.out.println("✓ 密码已填写");
            validatePasswordStrength(user.getPassword());

            // 验证邮箱格式：支持所有主流邮箱
            System.out.println("2. 验证邮箱格式");
            String email = user.getEmail().trim().toLowerCase();
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                System.out.println("注册失败: 邮箱格式不正确");
                throw new RuntimeException("邮箱格式不正确，请输入有效的邮箱地址");
            }
            System.out.println("✓ 邮箱格式验证通过: " + email);

            // 检查用户名是否已存在
            System.out.println("3. 检查用户名是否存在");
            if (checkUsernameExists(user.getUsername())) {
                System.out.println("注册失败: 用户名已存在");
                throw new RuntimeException("用户名已被使用，请选择其他用户名");
            }
            System.out.println("✓ 用户名可用");

            // 检查邮箱是否已存在
            System.out.println("4. 检查邮箱是否存在");
            if (checkEmailExists(email)) {
                System.out.println("注册失败: 邮箱已存在");
                throw new RuntimeException("邮箱已被注册，请使用其他邮箱或直接登录");
            }
            System.out.println("✓ 邮箱可用");

            // 检查手机号是否已存在
            System.out.println("5. 检查手机号是否存在");
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                if (checkPhoneExists(user.getPhone())) {
                    System.out.println("注册失败: 手机号已存在");
                    throw new RuntimeException("手机号已被使用，请使用其他手机号");
                }
                System.out.println("✓ 手机号可用: " + user.getPhone());
            } else {
                System.out.println("手机号为空，跳过验证");
            }

            // 统一使用小写邮箱
            user.setEmail(email);

            // 加密密码
            System.out.println("6. 加密密码");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            System.out.println("✓ 密码加密完成");

            user.setStatus(1); // 正常状态
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            System.out.println("7. 保存用户到数据库");
            int result = userMapper.insert(user);
            System.out.println("数据库插入结果: " + result);

            if (result > 0) {
                System.out.println("✓ 注册成功！用户ID: " + user.getId());

                // 处理邀请码（如果提供了）
                if (inviteCode != null && !inviteCode.trim().isEmpty() && userInviteService != null) {
                    try {
                        boolean inviteSuccess = userInviteService.verifyAndRecordInvite(inviteCode, user.getId());
                        if (inviteSuccess) {
                            System.out.println("✓ 邀请关系记录成功，邀请人将获得50积分");
                        } else {
                            System.out.println("ℹ️ 邀请码无效或已使用，跳过邀请奖励");
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ 处理邀请码失败: " + e.getMessage());
                        // 邀请码处理失败不影响注册流程
                    }
                }

                return true;
            } else {
                System.out.println("✗ 注册失败：数据库插入失败");
                return false;
            }
        } catch (RuntimeException e) {
            // 重新抛出业务异常，保持用户友好的错误信息
            System.out.println("业务异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            // 捕获技术异常，转换为用户友好的错误信息
            System.out.println("技术异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("注册服务暂时不可用，请稍后重试");
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, String loginType) {
        try {
            // 参数验证
            if (username == null || username.trim().isEmpty()) {
                System.out.println("登录失败: 账号不能为空");
                throw new RuntimeException("账号不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                System.out.println("登录失败: 密码不能为空");
                throw new RuntimeException("密码不能为空");
            }

            System.out.println("登录请求 - 用户名: " + username + ", 登录类型: " + loginType);

            // 先通过用户名查找
            QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
            usernameWrapper.eq("username", username);
            List<User> usernameUsers = userMapper.selectList(usernameWrapper);
            System.out.println("用户名查询结果数量: " + usernameUsers.size());
            if (!usernameUsers.isEmpty()) {
                System.out.println("找到用户: " + usernameUsers.get(0).getUsername());
            }

            // 再通过邮箱查找
            QueryWrapper<User> emailWrapper = new QueryWrapper<>();
            emailWrapper.eq("email", username);
            List<User> emailUsers = userMapper.selectList(emailWrapper);
            System.out.println("邮箱查询结果数量: " + emailUsers.size());

            User user = null;
            if (!usernameUsers.isEmpty()) {
                // 优先使用用户名匹配的结果
                user = usernameUsers.get(0);
                System.out.println("使用用户名匹配结果: " + user.getUsername());
            } else if (!emailUsers.isEmpty()) {
                // 如果用户名没找到，尝试邮箱匹配
                user = emailUsers.get(0);
                System.out.println("使用邮箱匹配结果: " + user.getUsername());
            }

            if (user == null) {
                loginSecurityService.recordFailedAttempt(null, username);
                throw new RuntimeException("用户名或密码错误");
            }

            if (user.getStatus() != 1) {
                throw new RuntimeException("账户已被禁用，请联系管理员");
            }

            // 验证登录类型是否匹配用户角色
            // loginType: "user" (用户端) 或 "admin" (管理端)
            // role: 1=管理员, 2=普通用户
            if (loginType != null && !loginType.isEmpty()) {
                System.out.println("验证登录类型 - 请求类型: " + loginType + ", 用户角色: " + user.getRole());

                // 判断是否为管理员：role = 1
                boolean isAdmin = user.getRole() != null && user.getRole() == 1;

                if ("user".equals(loginType) && isAdmin) {
                    throw new RuntimeException("该账号为管理员账号，请使用管理端登录");
                } else if ("admin".equals(loginType) && !isAdmin) {
                    throw new RuntimeException("该账号为普通用户账号，请使用用户端登录");
                }
            }

            // 使用BCrypt验证密码
            SecurityPolicy policy = securityPolicyService.getPolicy();

            // 校验多端登录限制
            if (!policy.isAllowMultiLogin() && onlineUserManager.hasActiveSession(user.getId())) {
                throw new RuntimeException("该账号已在其他设备登录，请先退出后再尝试");
            }

            // 登录锁定校验
            if (loginSecurityService.isLocked(user.getId(), username)) {
                throw new RuntimeException("账户已锁定，请稍后再试");
            }

            // 验证密码（不在日志中输出密码信息）
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("密码验证结果: " + (matches ? "通过" : "失败"));

            if (!matches) {
                loginSecurityService.recordFailedAttempt(user.getId(), username);
                throw new RuntimeException("用户名或密码错误");
            }

            loginSecurityService.clearFailures(user.getId(), username);

            // 更新最后登录时间和IP
            updateLastLoginInfo(user.getId());

            // 生成真实的JWT Token
            long sessionTimeoutMinutes = policy.getSessionTimeoutMinutes();
            long sessionTimeoutMillis = sessionTimeoutMinutes * 60_000L;
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), sessionTimeoutMillis);
            System.out.println("生成JWT Token成功，用户ID: " + user.getId() + ", 用户名: " + user.getUsername());

            // 标记用户在线（只统计普通用户，管理员不统计）
            onlineUserManager.markOnline(user.getId(), user.getRole());

            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("token", token);
            result.put("expiresIn", sessionTimeoutMinutes * 60); // 秒

            return result;
        } catch (RuntimeException e) {
            // 重新抛出业务异常，保持用户友好的错误信息
            throw e;
        } catch (Exception e) {
            // 捕获技术异常，转换为用户友好的错误信息
            throw new RuntimeException("登录服务暂时不可用，请稍后重试");
        }
    }

    @Override
    public boolean logout(Long userId) {
        try {
            // 标记用户下线
            onlineUserManager.markOffline(userId);
            // 这里可以实现token黑名单等逻辑
            return true;
        } catch (Exception e) {
            throw new RuntimeException("用户登出失败: " + e.getMessage());
        }
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new RuntimeException("原密码错误");
            }

            validatePasswordStrength(newPassword);
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdateTime(LocalDateTime.now());

            return userMapper.updateById(user) > 0;
        } catch (Exception e) {
            throw new RuntimeException("修改密码失败: " + e.getMessage());
        }
    }

    @Override
    public boolean resetPassword(String email, String newPassword) {
        try {
            // 参数验证
            if (email == null || email.trim().isEmpty()) {
                throw new RuntimeException("邮箱地址不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new RuntimeException("新密码不能为空");
            }

            // 验证密码强度
            validatePasswordStrength(newPassword);

            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("email", email);
            User user = userMapper.selectOne(wrapper);

            if (user == null) {
                throw new RuntimeException("用户不存在，请检查邮箱地址");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdateTime(LocalDateTime.now());

            return userMapper.updateById(user) > 0;
        } catch (RuntimeException e) {
            // 重新抛出业务异常，保持用户友好的错误信息
            throw e;
        } catch (Exception e) {
            // 捕获技术异常，转换为用户友好的错误信息
            throw new RuntimeException("密码重置失败，请稍后重试");
        }
    }

    @Override
    public boolean updateUserInfo(User user) {
        try {
            user.setUpdateTime(LocalDateTime.now());
            return userMapper.updateById(user) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public User getUserInfo(Long userId) {
        try {
            return userMapper.selectById(userId);
        } catch (Exception e) {
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public boolean checkUsernameExists(String username) {
        try {
            System.out.println("检查用户名是否存在: " + username);
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("username", username);
            long count = userMapper.selectCount(wrapper);
            System.out.println("用户名查询结果数量: " + count);
            return count > 0;
        } catch (Exception e) {
            System.out.println("检查用户名失败: " + e.getMessage());
            throw new RuntimeException("检查用户名失败: " + e.getMessage());
        }
    }

    @Override
    public boolean checkEmailExists(String email) {
        try {
            System.out.println("检查邮箱是否存在: " + email);
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("email", email);
            long count = userMapper.selectCount(wrapper);
            System.out.println("邮箱查询结果数量: " + count);
            return count > 0;
        } catch (Exception e) {
            System.out.println("检查邮箱失败: " + e.getMessage());
            throw new RuntimeException("检查邮箱失败: " + e.getMessage());
        }
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        try {
            System.out.println("检查手机号是否存在: " + phone);
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("phone", phone);
            long count = userMapper.selectCount(wrapper);
            System.out.println("手机号查询结果数量: " + count);
            return count > 0;
        } catch (Exception e) {
            System.out.println("检查手机号失败: " + e.getMessage());
            throw new RuntimeException("检查手机号失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return false;
            }
            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            return userMapper.updateById(user) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新用户状态失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 总用户数
            Long totalUsers = userMapper.selectCount(null);
            stats.put("totalUsers", totalUsers);

            // 活跃用户数（最近30天登录）
            QueryWrapper<User> activeWrapper = new QueryWrapper<>();
            activeWrapper.ge("last_login_time", LocalDateTime.now().minusDays(30));
            Long activeUsers = userMapper.selectCount(activeWrapper);
            stats.put("activeUsers", activeUsers);

            // 今日注册用户数
            QueryWrapper<User> todayWrapper = new QueryWrapper<>();
            todayWrapper.ge("create_time", LocalDateTime.now().toLocalDate().atStartOfDay());
            Long todayUsers = userMapper.selectCount(todayWrapper);
            stats.put("todayUsers", todayUsers);

            return stats;
        } catch (Exception e) {
            throw new RuntimeException("获取用户统计失败: " + e.getMessage());
        }
    }

    @Override
    public boolean sendCaptcha(String email) {
        try {
            // 参数验证
            if (email == null || email.trim().isEmpty()) {
                System.out.println("发送验证码失败: 邮箱地址不能为空");
                throw new RuntimeException("邮箱地址不能为空");
            }

            // 验证邮箱格式：支持所有主流邮箱
            String normalizedEmail = email.trim().toLowerCase();
            if (!normalizedEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                System.out.println("发送验证码失败: 邮箱格式不正确");
                throw new RuntimeException("邮箱格式不正确，请输入有效的邮箱地址");
            }

            System.out.println("发送验证码请求 - 邮箱: " + normalizedEmail);

            // 验证邮箱是否存在
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("email", normalizedEmail);
            User user = userMapper.selectOne(wrapper);

            if (user == null) {
                System.out.println("邮箱不存在: " + normalizedEmail);
                throw new RuntimeException("该邮箱未注册，请先注册账号");
            }

            System.out.println("邮箱验证通过，用户: " + user.getUsername());

            // 生成6位数字验证码
            String captcha = String.format("%06d", (int) (Math.random() * 1000000));

            // 存储验证码（5分钟过期）
            captchaService.storeCaptcha(normalizedEmail, captcha, 300);
            System.out.println("验证码已生成并存储，有效期5分钟");

            // 发送验证码邮件（使用系统SMTP配置，发件人显示为用户邮箱）
            boolean emailSent = emailService.sendCaptchaEmail(normalizedEmail, captcha);
            if (!emailSent) {
                throw new RuntimeException("验证码发送失败，请稍后重试");
            }

            System.out.println("验证码发送成功: " + normalizedEmail);
            return true;
        } catch (RuntimeException e) {
            // 重新抛出业务异常，保持用户友好的错误信息
            throw e;
        } catch (Exception e) {
            // 捕获技术异常，转换为用户友好的错误信息
            throw new RuntimeException("验证码服务暂时不可用，请稍后重试");
        }
    }

    @Override
    public boolean forgotPassword(String email, String captcha) {
        try {
            // 参数验证
            if (email == null || email.trim().isEmpty()) {
                System.out.println("忘记密码失败: 邮箱地址不能为空");
                throw new RuntimeException("邮箱地址不能为空");
            }
            if (captcha == null || captcha.trim().isEmpty()) {
                System.out.println("忘记密码失败: 验证码不能为空");
                throw new RuntimeException("验证码不能为空");
            }

            System.out.println("忘记密码请求 - 邮箱: " + email + ", 验证码: " + captcha);

            // 验证验证码
            boolean captchaValid = captchaService.verifyCaptcha(email, captcha);
            System.out.println("验证码验证结果: " + captchaValid);

            if (!captchaValid) {
                System.out.println("验证码验证失败");
                throw new RuntimeException("验证码错误或已过期，请重新获取");
            }

            System.out.println("验证码验证成功");

            // 生成重置密码token
            String resetToken = "reset_token_" + email + "_" + System.currentTimeMillis();
            System.out.println("生成重置密码token: " + resetToken);

            // 发送重置密码邮件（使用系统SMTP配置，发件人显示为用户邮箱）
            boolean emailSent = emailService.sendResetPasswordEmail(email, resetToken);
            if (!emailSent) {
                throw new RuntimeException("重置链接发送失败，请稍后重试");
            }

            System.out.println("重置密码邮件发送成功");
            return true;
        } catch (RuntimeException e) {
            // 重新抛出业务异常，保持用户友好的错误信息
            throw e;
        } catch (Exception e) {
            // 捕获技术异常，转换为用户友好的错误信息
            throw new RuntimeException("密码重置服务暂时不可用，请稍后重试");
        }
    }

    @Override
    public boolean resetPasswordByToken(String token, String newPassword) {
        try {
            // 参数验证
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("重置链接无效");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new RuntimeException("新密码不能为空");
            }

            // 验证token格式
            if (!token.startsWith("reset_token_")) {
                throw new RuntimeException("重置链接已失效，请重新申请");
            }

            // 从token中提取邮箱
            String[] tokenParts = token.split("_");
            if (tokenParts.length < 3) {
                throw new RuntimeException("重置链接格式错误，请重新申请");
            }

            String email = tokenParts[2];

            // 重置密码
            return resetPassword(email, newPassword);
        } catch (RuntimeException e) {
            // 重新抛出业务异常，保持用户友好的错误信息
            throw e;
        } catch (Exception e) {
            // 捕获技术异常，转换为用户友好的错误信息
            throw new RuntimeException("密码重置服务暂时不可用，请稍后重试");
        }
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

    private void validatePasswordStrength(String password) {
        SecurityPolicy policy = securityPolicyService.getPolicy();
        int minLength = Math.max(4, policy.getPasswordMinLength());
        if (password == null || password.length() < minLength) {
            throw new RuntimeException("密码长度不能少于" + minLength + "位");
        }
    }
}