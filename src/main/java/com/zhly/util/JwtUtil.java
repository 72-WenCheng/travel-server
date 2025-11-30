package com.zhly.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * @author zhly
 * @since 2024-01-01
 */
@Component
public class JwtUtil {
    
    @Value("${travel.jwt.secret:zhly-travel-system-secret-key-2024}")
    private String secret;
    
    @Value("${travel.jwt.expiration:86400000}")
    private Long expiration;
    
    /**
     * 生成JWT Token（使用默认有效期）
     */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, this.expiration);
    }

    /**
     * 生成指定有效期的Token
     */
    public String generateToken(Long userId, String username, long customExpirationMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        long expire = customExpirationMillis > 0 ? customExpirationMillis : this.expiration;
        return createToken(claims, username, expire);
    }
    
    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims, String subject, long expireMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireMillis))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 获取签名密钥
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes();
        
        // 确保密钥长度至少32字节（256位）以满足HMAC-SHA256的要求
        if (keyBytes.length < 32) {
            System.out.println("警告: JWT密钥长度不足32字节(" + keyBytes.length + "字节)，正在扩展...");
            byte[] extendedKey = new byte[32];
            System.arraycopy(keyBytes, 0, extendedKey, 0, keyBytes.length);
            // 使用密钥本身填充剩余字节
            for (int i = keyBytes.length; i < 32; i++) {
                extendedKey[i] = keyBytes[i % keyBytes.length];
            }
            keyBytes = extendedKey;
            System.out.println("密钥已扩展到32字节");
        }
        
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 从Token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * 从Token中获取指定声明
     */
    public <T> T getClaimFromToken(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 从Token中获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 检查Token是否过期
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 验证Token
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }
    
    /**
     * 验证Token（不需要用户名）
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        // 创建新的claims map，因为Claims是只读的
        Map<String, Object> newClaims = new HashMap<>();
        newClaims.putAll(claims);
        newClaims.put("iat", new Date().getTime() / 1000); // 设置新的签发时间
        return createToken(newClaims, claims.getSubject(), this.expiration);
    }
}
