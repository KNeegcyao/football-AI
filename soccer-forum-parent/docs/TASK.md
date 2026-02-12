# 任务清单与开发规范

## 任务编号规范

### 任务编号格式
`TASK{版本号}-{模块}-{序号}`
- 版本号：1.0, 2.0, 3.0
- 模块：001-初始化, 002-用户, 003-帖子, 004-互动, 005-球队, 006-资讯, 007-AI, 008-审计
- 序号：001, 002, 003...

### 任务状态流转
- **计划中** → **开发中** → **测试中** → **完成**
- **阻塞** → **待解决** → **已解决**

## 开发规范与最佳实践

### 1. 代码规范

#### Java后端规范
```java
// 命名规范
- 包名：com.soccer.forum.{module}.{layer}
- 类名：UpperCamelCase
- 方法名：lowerCamelCase
- 常量：UPPER_SNAKE_CASE
- 数据库字段：snake_case

// 代码格式
- 使用4个空格缩进
- 行宽限制：120字符
- 方法长度：不超过50行
- 类长度：不超过500行
```

#### 前端规范
```javascript
// Vue组件命名
- 组件名：PascalCase
- 文件名：kebab-case
- props：camelCase
- 事件名：kebab-case

// 代码组织
- 单文件组件顺序：template -> script -> style
- 组件props必须定义类型和默认值
- 使用Composition API
```

### 2. 数据库规范

#### 表设计规范
```sql
-- 统一字段
CREATE TABLE table_name (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version INT DEFAULT 1
);

-- 索引规范
- 主键：id
- 外键：{table}_id
- 唯一索引：uk_{column1}_{column2}
- 普通索引：idx_{column}
```

#### Redis Key规范
```
-- 格式：{业务}:{功能}:{标识}
-- 示例：
user:profile:{userId}
post:like:count:{postId}
cache:post:list:{page}:{size}
lock:post:update:{postId}
```

### 3. API设计规范

#### RESTful API规范
```
GET    /api/v1/posts           - 获取列表
POST   /api/v1/posts           - 创建
GET    /api/v1/posts/{id}      - 获取详情
PUT    /api/v1/posts/{id}      - 全量更新
PATCH  /api/v1/posts/{id}      - 部分更新
DELETE /api/v1/posts/{id}      - 删除
```

#### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1640995200000
}

{
  "code": 400,
  "message": "参数错误",
  "data": null,
  "timestamp": 1640995200000
}
```

### 4. 测试规范

#### 单元测试要求
```java
// 测试类命名
- 测试类：{ClassName}Test
- 测试方法：{methodName}_{scenario}_{expectedResult}

// 覆盖率要求
- 行覆盖率：>80%
- 分支覆盖率：>70%
- 类覆盖率：>90%
```

#### 测试数据准备
```java
@Test
void createPost_WithValidData_ShouldReturnSuccess() {
    // G iven
    PostCreateRequest request = PostCreateRequest.builder()
        .title("测试标题")
        .content("测试内容")
        .build();
    
    // When
    PostResponse response = postService.createPost(request);
    
    // Then
    assertNotNull(response);
    assertEquals("测试标题", response.getTitle());
}
```

### 5. 安全规范

#### 输入验证
```java
// 使用注解验证
@NotBlank(message = "标题不能为空")
@Size(max = 100, message = "标题长度不能超过100字符")
private String title;

@NotNull(message = "用户ID不能为空")
@Positive(message = "用户ID必须为正数")
private Long userId;
```

#### SQL注入防护
```java
// 使用MyBatis-Plus的Wrapper
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>()
    .eq(User::getUsername, username)
    .eq(User::getStatus, UserStatus.ACTIVE);

// 禁止字符串拼接SQL
// 禁止："SELECT * FROM user WHERE username = '" + username + "'"
```

### 6. 性能规范

#### Redis使用规范
```java
// 缓存策略
@Cacheable(value = "posts", key = "#id", unless = "#result == null")
public Post getPostById(Long id) {
    return postMapper.selectById(id);
}

// 批量操作
public List<Post> getPostsByIds(List<Long> ids) {
    return redisTemplate.opsForValue()
        .multiGet(ids.stream()
            .map(id -> "post:" + id)
            .collect(Collectors.toList()));
}
```

#### 分页查询优化
```java
// 使用MyBatis-Plus分页
Page<Post> page = new Page<>(pageNum, pageSize);
LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
    .eq(Post::getStatus, PostStatus.PUBLISHED)
    .orderByDesc(Post::getCreatedAt);

return postMapper.selectPage(page, wrapper);
```

### 7. 日志规范

#### 日志级别使用
```
ERROR - 系统错误，需要立即处理
WARN  - 潜在问题，需要关注
INFO  - 关键业务流程
DEBUG - 调试信息
TRACE - 详细跟踪信息
```

#### 日志格式
```
[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] %-5level %logger{50} - %msg%n
```

### 8. 异常处理规范

#### 统一异常处理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("系统繁忙，请稍后重试"));
    }
}
```

### 9. 部署规范

#### 环境配置
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/soccer_forum_dev
    username: dev_user
    password: dev_password
  
  redis:
    host: localhost
    port: 6379
    password: 

# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://prod-mysql:3306/soccer_forum
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  redis:
    host: prod-redis
    port: 6379
    password: ${REDIS_PASSWORD}
```

#### Docker配置
```dockerfile
FROM openjdk:17-jre-slim

COPY target/soccer-forum-service.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 10. 监控规范

#### 健康检查
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // 检查数据库连接
            return Health.up()
                .withDetail("database", "MySQL")
                .withDetail("status", "UP")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

#### 指标监控
```yaml
# 关键指标
- 接口响应时间（P95, P99）
- 错误率
- 并发用户数
- 数据库连接数
- Redis命中率
- JVM内存使用率
```

## 开发工具推荐

### 必备工具
- **IDE**: IntelliJ IDEA / VS Code
- **数据库**: MySQL Workbench / DataGrip
- **Redis**: Redis Desktop Manager
- **API测试**: Postman / Apifox
- **版本控制**: Git + GitHub/GitLab

### 开发插件
- **Lombok**: 简化Java代码
- **MyBatisX**: MyBatis增强
- **SonarLint**: 代码质量检查
- **CheckStyle**: 代码规范检查

### 构建工具
- **Maven**: 依赖管理
- **Docker**: 容器化部署
- **Jenkins**: 持续集成
- **Prometheus + Grafana**: 监控告警

## 代码审查清单

### 提交前检查
- [ ] 代码符合规范
- [ ] 单元测试通过
- [ ] 注释完整清晰
- [ ] 无敏感信息泄露
- [ ] 性能影响评估
- [ ] 安全漏洞检查

### 代码审查要点
- 业务逻辑正确性
- 异常处理完整性
- 性能优化空间
- 安全漏洞风险
- 代码可读性
- 测试覆盖率

## 文档维护

### 必须更新的文档
- API文档（OpenAPI）
- 数据库变更记录
- 部署手册
- 故障处理手册
- 用户手册

### 文档格式
- 使用Markdown格式
- 包含版本历史
- 提供示例代码
- 包含截图说明
- 定期更新维护

通过以上规范，确保项目的高质量交付和可持续维护。
