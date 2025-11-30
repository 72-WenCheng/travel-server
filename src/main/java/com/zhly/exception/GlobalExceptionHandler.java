package com.zhly.exception;

import com.zhly.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局异常处理器
 * 统一处理异常并返回用户友好的错误信息
 * 
 * @author zhly
 * @since 2024-01-01
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleRuntimeException(RuntimeException e) {
        // 输出到控制台，方便调试
        System.out.println("=== 业务异常 ===");
        System.out.println("异常信息: " + e.getMessage());
        System.out.println("异常类型: " + e.getClass().getName());
        
        logger.warn("业务异常: {}", e.getMessage());
        
        // 检查是否是登录相关的业务异常
        String message = e.getMessage();
        if (message.contains("账号不能为空") || message.contains("用户名不能为空")) {
            System.out.println("返回错误信息: 账号不能为空");
            return Result.error("账号不能为空");
        } else if (message.contains("密码不能为空")) {
            System.out.println("返回错误信息: 密码不能为空");
            return Result.error("密码不能为空");
        } else if (message.contains("用户名或密码错误")) {
            System.out.println("返回错误信息: 用户名或密码错误");
            return Result.error("用户名或密码错误");
        } else if (message.contains("账户已被禁用")) {
            System.out.println("返回错误信息: 账户已被禁用，请联系管理员");
            return Result.error("账户已被禁用，请联系管理员");
        } else if (message.contains("该账号为管理员账号")) {
            System.out.println("返回错误信息: 该账号为管理员账号，请使用管理端登录");
            return Result.error("该账号为管理员账号，请使用管理端登录");
        } else if (message.contains("该账号为普通用户账号")) {
            System.out.println("返回错误信息: 该账号为普通用户账号，请使用用户端登录");
            return Result.error("该账号为普通用户账号，请使用用户端登录");
        } else if (message.contains("用户名已被使用")) {
            System.out.println("返回错误信息: 用户名已被使用，请选择其他用户名");
            return Result.error("用户名已被使用，请选择其他用户名");
        } else if (message.contains("邮箱已被注册")) {
            System.out.println("返回错误信息: 邮箱已被注册，请使用其他邮箱");
            return Result.error("邮箱已被注册，请使用其他邮箱");
        } else if (message.contains("手机号已被使用")) {
            System.out.println("返回错误信息: 手机号已被使用，请使用其他手机号");
            return Result.error("手机号已被使用，请使用其他手机号");
        } else if (message.contains("验证码错误")) {
            System.out.println("返回错误信息: 验证码错误或已过期，请重新获取");
            return Result.error("验证码错误或已过期，请重新获取");
        } else if (message.contains("该邮箱未注册")) {
            System.out.println("返回错误信息: 该邮箱未注册，请先注册账号");
            return Result.error("该邮箱未注册，请先注册账号");
        } else if (message.contains("重置链接")) {
            System.out.println("返回错误信息: 重置链接已失效，请重新申请");
            return Result.error("重置链接已失效，请重新申请");
        } else if (message.contains("原密码错误")) {
            System.out.println("返回错误信息: 原密码错误");
            return Result.error("原密码错误");
        } else if (message.contains("密码长度不能少于")) {
            System.out.println("返回错误信息: " + message);
            return Result.error(message);
        } else if (message.contains("账户已锁定")) {
            System.out.println("返回错误信息: " + message);
            return Result.error(message);
        } else if (message.contains("请求过于频繁")) {
            System.out.println("返回错误信息: 请求过于频繁，请稍后再试");
            return Result.error("请求过于频繁，请稍后再试");
        } else if (message.contains("该账号已在其他设备登录")) {
            System.out.println("返回错误信息: " + message);
            return Result.error(message);
        } else if (message.contains("邮箱格式")) {
            System.out.println("返回错误信息: 邮箱格式不正确");
            return Result.error("邮箱格式不正确");
        } else if (message.contains("邮箱地址不能为空")) {
            System.out.println("返回错误信息: 邮箱地址不能为空");
            return Result.error("邮箱地址不能为空");
        } else if (message.contains("验证码不能为空")) {
            System.out.println("返回错误信息: 验证码不能为空");
            return Result.error("验证码不能为空");
        } else if (message.contains("验证码错误") || message.contains("验证码已过期")) {
            System.out.println("返回错误信息: 验证码错误或已过期，请重新获取");
            return Result.error("验证码错误或已过期，请重新获取");
        } else if (message.contains("用户名长度")) {
            System.out.println("返回错误信息: 用户名长度在3到20个字符");
            return Result.error("用户名长度在3到20个字符");
        } else if (message.contains("手机号格式")) {
            System.out.println("返回错误信息: 请输入正确的手机号格式");
            return Result.error("请输入正确的手机号格式");
        } else if (message.contains("注册")) {
            // 注册相关的错误
            if (message.contains("注册失败") || message.contains("注册服务暂时不可用")) {
                System.out.println("返回错误信息: 注册服务暂时不可用，请稍后重试");
                return Result.error("注册服务暂时不可用，请稍后重试");
            } else if (message.contains("注册成功")) {
                // 不应该到这里，但保留处理
                return Result.success("注册成功");
            } else {
                // 直接返回业务异常消息
                System.out.println("返回注册错误信息: " + message);
                return Result.error(message);
            }
        }
        
        System.out.println("返回默认错误信息: " + e.getMessage());
        return Result.error(e.getMessage());
    }
    
    /**
     * 处理参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("参数异常: {}", e.getMessage());
        return Result.error("请求参数错误: " + e.getMessage());
    }
    
    /**
     * 处理数据库异常
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleDataAccessException(org.springframework.dao.DataAccessException e) {
        logger.error("数据库异常: {}", e.getMessage(), e);
        
        // 根据异常信息判断业务场景
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("login") || message.contains("Login") || message.contains("user")) {
                return Result.error("用户名或密码错误");
            } else if (message.contains("register") || message.contains("Register") || message.contains("insert")) {
                return Result.error("注册服务暂时不可用，请稍后重试");
            } else if (message.contains("select") || message.contains("query")) {
                return Result.error("查询服务暂时不可用，请稍后重试");
            } else if (message.contains("update") || message.contains("modify")) {
                return Result.error("更新服务暂时不可用，请稍后重试");
            } else if (message.contains("delete") || message.contains("remove")) {
                return Result.error("删除服务暂时不可用，请稍后重试");
            }
        }
        
        return Result.error("数据服务暂时不可用，请稍后重试");
    }
    
    /**
     * 处理SQL异常
     */
    @ExceptionHandler(java.sql.SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleSQLException(java.sql.SQLException e) {
        logger.error("SQL异常: {}", e.getMessage(), e);
        
        // 根据SQL异常信息判断业务场景
        String message = e.getMessage();
        if (message.contains("login") || message.contains("Login") || message.contains("user")) {
            return Result.error("登录服务暂时不可用，请稍后重试");
        } else if (message.contains("register") || message.contains("Register") || message.contains("insert")) {
            return Result.error("注册服务暂时不可用，请稍后重试");
        } else if (message.contains("select") || message.contains("query")) {
            return Result.error("查询服务暂时不可用，请稍后重试");
        } else if (message.contains("update") || message.contains("modify")) {
            return Result.error("更新服务暂时不可用，请稍后重试");
        } else if (message.contains("delete") || message.contains("remove")) {
            return Result.error("删除服务暂时不可用，请稍后重试");
            } else {
            return Result.error("数据服务暂时不可用，请稍后重试");
        }
    }
    
    /**
     * 处理MyBatis异常
     */
    @ExceptionHandler(org.apache.ibatis.exceptions.PersistenceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handlePersistenceException(org.apache.ibatis.exceptions.PersistenceException e) {
        logger.error("MyBatis异常: {}", e.getMessage(), e);
        
        // 根据异常信息判断业务场景
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("login") || message.contains("Login") || message.contains("user")) {
                return Result.error("用户名或密码错误");
            } else if (message.contains("register") || message.contains("Register") || message.contains("insert")) {
                return Result.error("注册服务暂时不可用，请稍后重试");
            } else if (message.contains("select") || message.contains("query")) {
                return Result.error("查询服务暂时不可用，请稍后重试");
            } else if (message.contains("update") || message.contains("modify")) {
                return Result.error("更新服务暂时不可用，请稍后重试");
            } else if (message.contains("delete") || message.contains("remove")) {
                return Result.error("删除服务暂时不可用，请稍后重试");
            }
        }
        
        return Result.error("数据服务暂时不可用，请稍后重试");
    }
    
    /**
     * 处理MyBatis TooManyResultsException异常
     */
    @ExceptionHandler(org.apache.ibatis.exceptions.TooManyResultsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleTooManyResultsException(org.apache.ibatis.exceptions.TooManyResultsException e) {
        logger.error("查询结果过多异常: {}", e.getMessage(), e);
        
        // 根据异常堆栈信息判断业务场景
        String stackTrace = e.getStackTrace().toString();
        if (stackTrace.contains("login") || stackTrace.contains("Login")) {
            return Result.error("用户名或密码错误");
        } else if (stackTrace.contains("register") || stackTrace.contains("Register")) {
            return Result.error("注册信息异常，请稍后重试");
        } else if (stackTrace.contains("select") || stackTrace.contains("query")) {
            return Result.error("数据查询异常，请稍后重试");
        } else if (stackTrace.contains("insert") || stackTrace.contains("save")) {
            return Result.error("数据保存异常，请稍后重试");
        } else if (stackTrace.contains("update") || stackTrace.contains("modify")) {
            return Result.error("数据更新异常，请稍后重试");
        } else if (stackTrace.contains("delete") || stackTrace.contains("remove")) {
            return Result.error("数据删除异常，请稍后重试");
        } else {
            return Result.error("用户名或密码错误");
        }
    }
    
    /**
     * 处理MyBatis BindingException异常
     */
    @ExceptionHandler(org.apache.ibatis.binding.BindingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleBindingException(org.apache.ibatis.binding.BindingException e) {
        logger.error("MyBatis绑定异常: {}", e.getMessage(), e);
        
        // 根据异常信息判断业务场景
        String message = e.getMessage();
        if (message.contains("login") || message.contains("Login")) {
            return Result.error("登录服务暂时不可用，请稍后重试");
        } else if (message.contains("register") || message.contains("Register")) {
            return Result.error("注册服务暂时不可用，请稍后重试");
        } else if (message.contains("select") || message.contains("query")) {
            return Result.error("查询服务暂时不可用，请稍后重试");
        } else if (message.contains("insert") || message.contains("save")) {
            return Result.error("保存服务暂时不可用，请稍后重试");
        } else if (message.contains("update") || message.contains("modify")) {
            return Result.error("更新服务暂时不可用，请稍后重试");
        } else if (message.contains("delete") || message.contains("remove")) {
            return Result.error("删除服务暂时不可用，请稍后重试");
        } else {
            return Result.error("数据服务暂时不可用，请稍后重试");
        }
    }
    
    /**
     * 处理网络异常
     */
    @ExceptionHandler(org.springframework.web.client.ResourceAccessException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<String> handleResourceAccessException(org.springframework.web.client.ResourceAccessException e) {
        logger.error("网络异常: {}", e.getMessage(), e);
        return Result.error("网络服务暂时不可用，请稍后重试");
    }
    
    /**
     * 处理MyBatis BuilderException异常
     */
    @ExceptionHandler(org.apache.ibatis.builder.BuilderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleBuilderException(org.apache.ibatis.builder.BuilderException e) {
        logger.error("MyBatis构建异常: {}", e.getMessage(), e);
        return Result.error("数据服务暂时不可用，请稍后重试");
    }
    
    /**
     * 处理数据库连接异常
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
        logger.error("数据完整性异常: {}", e.getMessage(), e);
        
        String message = e.getMessage();
        if (message.contains("username") || message.contains("用户名")) {
            return Result.error("用户名已存在，请选择其他用户名");
        } else if (message.contains("email") || message.contains("邮箱")) {
            return Result.error("邮箱已被注册，请使用其他邮箱");
        } else if (message.contains("phone") || message.contains("手机")) {
            return Result.error("手机号已被使用，请使用其他手机号");
        } else {
            return Result.error("数据冲突，请检查输入信息");
        }
    }
    
    /**
     * 处理数据库约束异常
     */
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleDuplicateKeyException(org.springframework.dao.DuplicateKeyException e) {
        logger.error("重复键异常: {}", e.getMessage(), e);
        
        String message = e.getMessage();
        if (message.contains("username") || message.contains("用户名")) {
            return Result.error("用户名已存在，请选择其他用户名");
        } else if (message.contains("email") || message.contains("邮箱")) {
            return Result.error("邮箱已被注册，请使用其他邮箱");
        } else if (message.contains("phone") || message.contains("手机")) {
            return Result.error("手机号已被使用，请使用其他手机号");
        } else {
            return Result.error("信息已存在，请检查输入");
        }
    }
    
    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleNullPointerException(NullPointerException e) {
        logger.error("空指针异常: {}", e.getMessage(), e);
        return Result.error("系统服务异常，请稍后重试");
    }
    
    /**
     * 处理JWT相关异常
     */
    @ExceptionHandler(io.jsonwebtoken.security.WeakKeyException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleWeakKeyException(io.jsonwebtoken.security.WeakKeyException e) {
        logger.error("JWT密钥异常: {}", e.getMessage(), e);
        return Result.error("系统配置异常，请联系管理员");
    }
    
    /**
     * 处理JWT签名异常
     */
    @ExceptionHandler(io.jsonwebtoken.security.SignatureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<String> handleSignatureException(io.jsonwebtoken.security.SignatureException e) {
        logger.error("JWT签名异常: {}", e.getMessage(), e);
        return Result.error("登录状态已失效，请重新登录");
    }
    
    /**
     * 处理JWT过期异常
     */
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<String> handleExpiredJwtException(io.jsonwebtoken.ExpiredJwtException e) {
        logger.warn("JWT已过期: {}", e.getMessage());
        return Result.error("登录已过期，请重新登录");
    }
    
    /**
     * 处理JWT格式异常
     */
    @ExceptionHandler(io.jsonwebtoken.MalformedJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<String> handleMalformedJwtException(io.jsonwebtoken.MalformedJwtException e) {
        logger.error("JWT格式异常: {}", e.getMessage(), e);
        return Result.error("登录信息无效，请重新登录");
    }
    
    /**
     * 处理所有JWT相关异常
     */
    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<String> handleJwtException(io.jsonwebtoken.JwtException e) {
        logger.error("JWT异常: {}", e.getMessage(), e);
        return Result.error("登录状态异常，请重新登录");
    }
    
    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception e) {
        logger.error("未知异常: {}", e.getMessage(), e);
        
        // 检查异常信息中是否包含登录相关的关键词
        String message = e.getMessage();
        if (message != null) {
            // JWT相关错误
            if (message.contains("WeakKeyException") || message.contains("key byte array")) {
                return Result.error("系统配置异常，请联系管理员");
            } else if (message.contains("JWT") || message.contains("jwt") || message.contains("token")) {
                return Result.error("登录状态异常，请重新登录");
            }
            // MyBatis查询错误
            else if (message.contains("selectOne") && message.contains("Expected one result") && message.contains("but found")) {
                return Result.error("用户名或密码错误");
            } else if (message.contains("TooManyResultsException")) {
                return Result.error("数据异常，请联系管理员");
            }
            // 业务功能错误
            else if (message.contains("login") || message.contains("Login")) {
                return Result.error("登录服务异常，请稍后重试");
            } else if (message.contains("register") || message.contains("Register")) {
                return Result.error("注册服务异常，请稍后重试");
            } else if (message.contains("password") || message.contains("Password")) {
                return Result.error("密码验证异常，请检查密码");
            } else if (message.contains("username") || message.contains("Username")) {
                return Result.error("用户名验证异常，请检查用户名");
            } else if (message.contains("email") || message.contains("Email")) {
                return Result.error("邮箱验证异常，请检查邮箱");
            }
            // 数据库连接错误
            else if (message.contains("CommunicationsException") || message.contains("Connection refused")) {
                return Result.error("数据库连接失败，请稍后重试");
            } else if (message.contains("SQLException") || message.contains("SQL syntax")) {
                return Result.error("数据服务异常，请稍后重试");
            }
            // 网络相关错误
            else if (message.contains("SocketTimeoutException") || message.contains("Read timed out")) {
                return Result.error("请求超时，请稍后重试");
            } else if (message.contains("ConnectException") || message.contains("Connection refused")) {
                return Result.error("网络连接失败，请检查网络");
            }
            // 安全相关错误
            else if (message.contains("SecurityException") || message.contains("Unauthorized")) {
                return Result.error("权限不足，请联系管理员");
            } else if (message.contains("Forbidden")) {
                return Result.error("访问被拒绝");
            }
            // 参数相关错误
            else if (message.contains("IllegalArgumentException") || message.contains("参数")) {
                return Result.error("请求参数错误，请检查输入");
            } else if (message.contains("MethodArgumentNotValidException")) {
                return Result.error("参数验证失败，请检查输入");
            }
            // JSON解析错误
            else if (message.contains("JSON") || message.contains("json") || message.contains("parse")) {
                return Result.error("数据格式错误，请重试");
            }
            // 空指针错误
            else if (message.contains("NullPointerException") || message.contains("null")) {
                return Result.error("系统服务异常，请稍后重试");
            }
            // 类未找到错误
            else if (message.contains("ClassNotFoundException") || message.contains("NoClassDefFoundError")) {
                return Result.error("系统配置异常，请联系管理员");
            }
            // 反射相关错误
            else if (message.contains("IllegalAccessException") || message.contains("InvocationTargetException")) {
                return Result.error("系统服务异常，请稍后重试");
            }
        }
        
        return Result.error("系统服务暂时不可用，请稍后重试");
    }
}