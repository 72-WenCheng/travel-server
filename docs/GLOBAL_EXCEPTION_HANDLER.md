# 全局异常处理器配置

## 设计理念

通过全局异常处理器统一处理所有技术异常，将技术错误信息转换为用户友好的业务提示，避免在每个Mapper或Service中重复处理异常。

## 异常处理策略

### 1. 业务异常（RuntimeException）
- **处理方式**：直接返回用户友好的错误信息
- **示例**：`用户名或密码错误`、`邮箱已被注册`

### 2. 数据库异常
- **SQLException** → `数据服务暂时不可用，请稍后重试`
- **DataAccessException** → `数据服务暂时不可用，请稍后重试`
- **PersistenceException** → `数据服务暂时不可用，请稍后重试`

### 3. MyBatis异常
- **TooManyResultsException** → `数据查询异常，请稍后重试`
- **BindingException** → `数据服务暂时不可用，请稍后重试`
- **BuilderException** → `数据服务暂时不可用，请稍后重试`

### 4. 网络异常
- **ResourceAccessException** → `网络服务暂时不可用，请稍后重试`

### 5. 系统异常
- **Exception** → `系统服务暂时不可用，请稍后重试`

## 异常处理流程

```
用户请求 → Controller → Service → Mapper → 数据库
                ↓
            异常发生
                ↓
        全局异常处理器
                ↓
        转换为用户友好信息
                ↓
        返回给前端
```

## 优势

### 1. 统一处理
- 所有异常都在一个地方处理
- 避免重复的异常处理代码
- 统一的错误信息格式

### 2. 用户友好
- 技术错误不暴露给用户
- 提供明确的解决建议
- 保持一致的错误体验

### 3. 开发友好
- 异常信息记录在日志中
- 便于调试和问题定位
- 减少重复代码

## 配置示例

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        return Result.error(e.getMessage());
    }
    
    @ExceptionHandler(SQLException.class)
    public Result<String> handleSQLException(SQLException e) {
        return Result.error("数据服务暂时不可用，请稍后重试");
    }
    
    @ExceptionHandler(TooManyResultsException.class)
    public Result<String> handleTooManyResultsException(TooManyResultsException e) {
        return Result.error("数据查询异常，请稍后重试");
    }
}
```

## 错误信息对照表

| 技术错误 | 用户友好提示 |
|---------|-------------|
| `TooManyResultsException: Expected one result (or null) to be returned by selectOne(), but found: 2` | 数据查询异常，请稍后重试 |
| `SQLException: Table 'travel_db.user' doesn't exist` | 数据服务暂时不可用，请稍后重试 |
| `BindingException: Invalid bound statement (not found)` | 数据服务暂时不可用，请稍后重试 |
| `DataAccessException: Connection refused` | 数据服务暂时不可用，请稍后重试 |
| `ResourceAccessException: Connection timeout` | 网络服务暂时不可用，请稍后重试 |

## 注意事项

1. **异常优先级**：具体的异常处理在前，通用的异常处理在后
2. **日志记录**：所有异常都要记录到日志中，便于调试
3. **用户友好**：错误信息要简洁明了，不要暴露技术细节
4. **一致性**：相同类型的错误使用相同的提示信息

## 测试方法

1. **模拟异常**：在代码中抛出各种异常
2. **检查响应**：确认返回用户友好的错误信息
3. **查看日志**：确认异常信息被正确记录
4. **用户体验**：确认用户看到的是友好的提示

## 扩展性

- 可以轻松添加新的异常处理
- 可以根据业务需求调整错误信息
- 可以添加异常监控和告警
- 可以集成第三方异常处理服务





