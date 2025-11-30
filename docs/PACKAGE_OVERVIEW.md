# `travel-server` 代码包结构说明

本文件描述 `src/main/java/com/zhly` 目录下各个一级包（文件夹）的职责与典型内容，方便快速定位代码。

| 包名 | 作用说明 | 常见内容/示例 |
| --- | --- | --- |
| `admin` | 管理端专用的业务逻辑，通常包含后台页面/接口所需的 controller、service、VO 等。 | 后台统计、用户管理、内容审核等模块的实现。 |
| `annotation` | 项目自定义注解定义处，配合 AOP/验证使用。 | 权限注解、日志注解、重复提交校验注解等。 |
| `aspect` | AOP 切面实现，用于集中处理横切关注点。 | 日志切面、权限校验、分布式锁、请求拦截等。 |
| `common` | 公共常量、枚举、统一响应结构等，与具体业务无关但供各模块复用。 | `Result`/`PageResult`、常量类、枚举等。 |
| `config` | Spring/第三方组件的配置类。 | MyBatis、Redis、Swagger、安全配置、跨域设置等。 |
| `consumer` | 消费者相关代码，通常是消息队列、事件总线的消费端。 | RabbitMQ/Kafka 消费者、异步任务监听。 |
| `controller` | 面向用户侧的 REST Controller，暴露 HTTP 接口。 | 登录注册、攻略、景点、消息等 API。 |
| `dto` | 数据传输对象，用于接收请求参数或向 service 传递数据。 | `UserLoginDTO`、`PlanCreateDTO` 等。 |
| `entity` | 与数据库表对应的实体（PO），通常配合 MyBatis/MyBatis-Plus 使用。 | `User`, `TravelPlan`, `Comment` 等。 |
| `exception` | 自定义异常、全局异常处理。 | `BusinessException`、`GlobalExceptionHandler`。 |
| `interceptor` | Spring MVC 或其他框架的拦截器实现。 | 登录状态校验、接口限流、签名验证等。 |
| `mapper` | MyBatis Mapper 接口，定义数据库访问方法。 | `UserMapper`, `TravelPlanMapper` 等。 |
| `security` | 安全和认证相关实现。 | JWT 认证、权限管理、密码策略等。 |
| `service` | 核心业务逻辑层，包含接口与实现。 | `UserService`, `TravelPlanServiceImpl` 等。 |
| `task` | 定时任务、调度相关代码。 | Quartz/Spring Scheduler 定时任务。 |
| `test` | （若存在）测试专用逻辑或测试工具类。 | 集成测试基类、Mock 工具。 |
| `user` | 用户端专属逻辑，可与 `admin` 相对，封装用户侧的独立业务。 | 用户画像、推荐、用户活动等子模块。 |
| `util` | 工具类集合，封装通用功能。 | 时间处理、加解密、缓存辅助、第三方 API 封装等。 |
| `validation` | 自定义校验器、Constraint 实现与验证规则。 | 参数校验注解、实现 `ConstraintValidator` 的类。 |
| `websocket` | WebSocket 服务端实现、消息处理逻辑。 | WebSocket 配置、消息推送、会话管理等。 |

如需进一步拆分或新增包，可在此文件补充说明以保持一致性。***
可以在 com.zhly 下人为划分几层“领域文件夹”，这样看起来更清晰。结合你现有的包，我建议如下分组命名：
com.zhly.platform.admin
放所有管理员端特有的 controller/service/dto（原 admin 包里的内容）。
例：platform.admin.controller.AdminDashboardController。
com.zhly.platform.user
对应用户端业务（原 user 包及用户相关 controller/service）。
例：platform.user.service.UserCenterServiceImpl。
com.zhly.core
存放项目核心通用层：entity、dto、mapper、service 接口、task 等。
可以按子目录再细分，如 core.entity, core.mapper, core.task。
com.zhly.infrastructure
技术基础设施层：config、security、websocket, consumer, annotation, validation, interceptor, aspect 等。
可以继续拆成 infrastructure.config, infrastructure.mq, infrastructure.security。
com.zhly.common（或 com.zhly.shared）
common、util, exception, constant（如果有）等纯工具/公共类。
这样移动后，目录示例：
com/zhly/
  common/...
  core/
    entity/...
    dto/...
    mapper/...
    service/...
  infrastructure/
    config/...
    security/...
    mq/consumer/...
    websocket/...
  platform/
    admin/...
    user/...
优点：
平衡“业务领域 (platform.)”和“技术层 (core/infrastructure)”的划分；
包名层级固定，IDE 中找到所需模块更快；
只需移动目录并调整包名，Spring Boot 仍能扫描（scanBasePackages = "com.zhly"）。
如果想进一步细化，比如把 controller 占位放在 platform.admin.controller / platform.user.controller，完全没问题；关键是先把顶层几个“域”建好，再把原有包内容按用途归入即可。

