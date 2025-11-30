package com.zhly.common;

/**
 * Result 类的简写别名
 * 提供更简洁的返回结果调用方式
 * 
 * @author zhly
 * @since 2024-01-01
 */
public class R<T> extends Result<T> {
    
    public R() {
        super();
    }
    
    public R(Integer code, String message) {
        super(code, message);
    }
    
    public R(Integer code, String message, T data) {
        super(code, message, data);
    }
    
    /**
     * 成功返回
     */
    public static <T> R<T> success() {
        return new R<>(200, "操作成功");
    }
    
    /**
     * 成功返回带数据
     */
    public static <T> R<T> success(T data) {
        return new R<>(200, "操作成功", data);
    }
    
    /**
     * 成功返回带消息和数据
     */
    public static <T> R<T> success(String message, T data) {
        return new R<>(200, message, data);
    }
    
    /**
     * 失败返回
     */
    public static <T> R<T> error() {
        return new R<>(500, "操作失败");
    }
    
    /**
     * 失败返回带消息
     */
    public static <T> R<T> error(String message) {
        return new R<>(500, message);
    }
    
    /**
     * 失败返回带状态码和消息
     */
    public static <T> R<T> error(Integer code, String message) {
        return new R<>(code, message);
    }
    
    /**
     * 参数错误
     */
    public static <T> R<T> badRequest(String message) {
        return new R<>(400, message);
    }
    
    /**
     * 未授权
     */
    public static <T> R<T> unauthorized(String message) {
        return new R<>(401, message);
    }
    
    /**
     * 禁止访问
     */
    public static <T> R<T> forbidden(String message) {
        return new R<>(403, message);
    }
    
    /**
     * 资源不存在
     */
    public static <T> R<T> notFound(String message) {
        return new R<>(404, message);
    }
    
    // ==================== ok 方法（success 的别名）====================
    
    /**
     * 成功返回（ok 是 success 的别名）
     */
    public static <T> R<T> ok() {
        return success();
    }
    
    /**
     * 成功返回带数据（ok 是 success 的别名）
     */
    public static <T> R<T> ok(T data) {
        return success(data);
    }
    
    /**
     * 成功返回带消息（ok 是 success 的别名）
     */
    public static <T> R<T> ok(String message) {
        return new R<>(200, message);
    }
    
    /**
     * 成功返回带消息和数据（ok 是 success 的别名）
     */
    public static <T> R<T> ok(String message, T data) {
        return success(message, data);
    }
}

