package com.zhly.service.impl;

import com.zhly.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 邮件服务实现
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Service
public class EmailServiceImpl implements EmailService {
    
    // SMTP服务器配置（从配置文件读取）
    @Value("${spring.mail.host:smtp.qq.com}")
    private String host;
    
    @Value("${spring.mail.port:587}")
    private int port;
    
    @Value("${spring.mail.username:}")
    private String username;
    
    @Value("${spring.mail.password:}")
    private String password;
    
    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean auth;
    
    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean starttls;
    
    // 前端域名配置（用于生成重置密码链接）
    @Value("${travel.frontend.url:http://localhost:5173}")
    private String frontendUrl;
    
    @Override
    public boolean sendCaptchaEmail(String email, String captcha) {
        try {
            System.out.println("=== 发送验证码邮件 ===");
            System.out.println("收件人: " + email);
            System.out.println("发件人: " + username);
            System.out.println("使用系统SMTP配置进行认证");
            
            // 如果未配置邮箱信息，使用模拟发送
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                System.out.println("未配置真实邮箱服务，使用模拟发送");
                System.out.println("发件人: " + (username != null ? username : "system@example.com"));
                System.out.println("收件人: " + email);
                System.out.println("主题: 智慧生态旅游系统 - 验证码");
                System.out.println("验证码: " + captcha);
                System.out.println("内容: 您的验证码是: " + captcha + "，5分钟内有效，请勿泄露给他人。");
                System.out.println("==================");
                Thread.sleep(1000);
                return true;
            }
            
            // 使用系统SMTP配置发送邮件，发件人显示为系统邮箱
            return sendEmail(email, "智慧生态旅游系统 - 验证码", 
                "您的验证码是: " + captcha + "\n\n5分钟内有效，请勿泄露给他人。", username);
                
        } catch (Exception e) {
            System.err.println("发送验证码邮件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean sendResetPasswordEmail(String email, String resetToken) {
        try {
            System.out.println("=== 发送重置密码邮件 ===");
            System.out.println("收件人: " + email);
            System.out.println("发件人: " + username);
            System.out.println("重置Token: " + resetToken);
            System.out.println("前端URL: " + frontendUrl);
            System.out.println("使用系统SMTP配置进行认证");
            
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
            
            // 如果未配置邮箱信息，使用模拟发送
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                System.out.println("未配置真实邮箱服务，使用模拟发送");
                System.out.println("发件人: " + (username != null ? username : "system@example.com"));
                System.out.println("收件人: " + email);
                System.out.println("主题: 智慧生态旅游系统 - 重置密码");
                System.out.println("内容: 请点击以下链接重置您的密码: " + resetUrl);
                System.out.println("链接24小时内有效，请勿泄露给他人。");
                System.out.println("=============================");
                Thread.sleep(1000);
                return true;
            }
            
            // 使用系统SMTP配置发送邮件（HTML格式），发件人显示为系统邮箱
            String htmlContent = "<html><body>" +
                "<p>请点击以下链接重置您的密码:</p>" +
                "<p><a href=\"" + resetUrl + "\">" + resetUrl + "</a></p>" +
                "<p>链接24小时内有效，请勿泄露给他人。</p>" +
                "<p>如果链接无法点击，请复制链接到浏览器地址栏访问。</p>" +
                "</body></html>";
            return sendEmail(email, "智慧生态旅游系统 - 重置密码", htmlContent, username);
                
        } catch (Exception e) {
            System.err.println("发送重置密码邮件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 根据邮箱地址自动识别SMTP服务器配置
     */
    private SmtpConfig getSmtpConfigByEmail(String email) {
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        
        // 根据邮箱域名自动识别SMTP配置
        switch (domain) {
            case "qq.com":
                return new SmtpConfig("smtp.qq.com", 587, true);
            case "163.com":
                return new SmtpConfig("smtp.163.com", 465, false);
            case "126.com":
                return new SmtpConfig("smtp.126.com", 465, false);
            case "gmail.com":
                return new SmtpConfig("smtp.gmail.com", 587, true);
            case "outlook.com":
            case "hotmail.com":
                return new SmtpConfig("smtp-mail.outlook.com", 587, true);
            case "sina.com":
                return new SmtpConfig("smtp.sina.com", 465, false);
            case "foxmail.com":
                return new SmtpConfig("smtp.qq.com", 587, true);
            default:
                // 默认使用配置文件的设置
                return new SmtpConfig(host, port, starttls);
        }
    }
    
    /**
     * 使用SMTP发送邮件
     * @param toEmail 收件人邮箱（用户输入的邮箱）
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param fromEmail 发件人显示地址（系统邮箱）
     */
    private boolean sendEmail(String toEmail, String subject, String content, String fromEmail) {
        try {
            // 使用系统配置的SMTP服务器（根据系统邮箱域名识别SMTP服务器地址）
            SmtpConfig smtpConfig = getSmtpConfigByEmail(fromEmail != null ? fromEmail : username);
            
            // 创建Properties对象
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpConfig.host);
            props.put("mail.smtp.port", smtpConfig.port);
            props.put("mail.smtp.auth", auth);
            props.put("mail.smtp.starttls.enable", smtpConfig.starttls);
            
            // 如果使用SSL（端口465），需要额外配置
            if (smtpConfig.port == 465) {
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.fallback", "false");
            }
            
            // 创建Session对象（始终使用系统配置的邮箱账号和授权码进行SMTP认证）
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // 使用系统配置的邮箱账号和授权码进行SMTP认证
                    System.out.println("使用系统邮箱配置进行SMTP认证: " + username);
                    return new PasswordAuthentication(username, password);
                }
            });
            
            // 创建MimeMessage对象
            MimeMessage message = new MimeMessage(session);
            // 发件人显示为系统邮箱
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);
            
            // 判断是否是HTML格式
            if (content.startsWith("<html>")) {
                message.setContent(content, "text/html;charset=UTF-8");
            } else {
                message.setText(content);
            }
            
            // 发送邮件
            Transport.send(message);
            
            System.out.println("邮件发送成功! 发件人: " + fromEmail + ", 收件人: " + toEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("SMTP邮件发送失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * SMTP配置内部类
     */
    private static class SmtpConfig {
        String host;
        int port;
        boolean starttls;
        
        SmtpConfig(String host, int port, boolean starttls) {
            this.host = host;
            this.port = port;
            this.starttls = starttls;
        }
    }
}





