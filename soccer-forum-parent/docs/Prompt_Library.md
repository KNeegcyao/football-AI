# AI编程助手提示词库

## 使用说明

本文档包含毕业设计开发所需的标准化AI编程助手提示词，每个提示词都经过简化设计，确保能满足本科毕设的要求。

## 核心提示词库

### 后端开发提示词

#### 实体类生成提示词
```
角色：Java工程师

任务：根据数据库表结构生成MyBatis-Plus实体类

表结构：
{table_structure}

要求：
1. 使用Lombok注解简化代码
2. 添加基本JPA注解
3. 包含创建时间和更新时间字段
4. 添加必要验证注解
5. 生成对应DTO类

技术栈：
- Spring Boot 3.x
- MyBatis-Plus 3.5.x
- Lombok 1.18.x

示例：
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @TableField("username")
    private String username;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
```
```

#### Service层生成提示词
```
角色：Java工程师

任务：根据实体类生成Service层代码

实体类：
{entity_class}

要求：
1. 创建Service接口和实现类
2. 实现基本CRUD操作
3. 添加分页查询功能
4. 集成简单缓存
5. 添加基础事务管理

功能要求：
- 创建记录
- 更新记录
- 删除记录（逻辑删除）
- 根据ID查询
- 分页查询
- 条件查询

示例：
```java
public interface UserService extends IService<User> {
    UserDTO createUser(UserCreateRequest request);
    UserDTO updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    UserDTO getUserById(Long id);
    Page<UserDTO> getUserPage(UserQuery query);
}
```
```

#### Controller层生成提示词
```
角色：Java工程师

任务：根据Service层生成RESTful Controller

Service接口：
{service_interface}

要求：
1. 创建RESTful API控制器
2. 实现标准HTTP方法
3. 添加基础参数验证
4. 统一响应格式
5. 简单异常处理

接口规范：
- 路径：/api/v1/{resource}
- GET /api/v1/{resource} - 查询列表
- GET /api/v1/{resource}/{id} - 查询详情
- POST /api/v1/{resource} - 创建
- PUT /api/v1/{resource}/{id} - 更新
- DELETE /api/v1/{resource}/{id} - 删除

示例：
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ApiResponse<Page<UserDTO>> getUsers(@Valid UserQuery query) {
        return ApiResponse.success(userService.getUserPage(query));
    }
    
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }
}
```
```

### 前端开发提示词

#### Vue组件生成提示词
```
角色：Vue.js工程师

任务：生成uni-app组件代码

需求描述：
{component_requirement}

技术栈：
- uni-app 3.x
- Vue 3 Composition API
- uView Plus

组件要求：
1. 使用Composition API
2. 响应式设计
3. 基础错误处理
4. 加载状态显示
5. 简单用户交互

示例：
```vue
<template>
  <view class="user-profile">
    <u-navbar title="用户详情" />
    
    <view v-if="loading" class="loading">
      <u-loading mode="circle" />
    </view>
    
    <view v-else-if="user" class="content">
      <u-avatar :src="user.avatar" size="120" />
      <text class="username">{{ user.username }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  userId: Number
})

const user = ref(null)
const loading = ref(false)

onMounted(async () => {
  try {
    loading.value = true
    // 调用API获取用户信息
  } finally {
    loading.value = false
  }
})
</script>
```
```

### AI集成提示词

#### LangChain4j基础集成
```
角色：AI应用开发者

任务：实现基础的LangChain4j集成

需求：创建一个简单的AI解说功能

技术栈：
- LangChain4j 0.25.x
- Spring Boot 3.x
- OpenAI GPT-3.5

实现要求：
1. 基础配置
2. 简单提示词模板
3. 基础缓存使用

示例代码：
```java
@Configuration
public class LangChainConfig {
    
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
            .apiKey("your-api-key")
            .modelName("gpt-3.5-turbo")
            .temperature(0.7)
            .build();
    }
}

@Service
public class CommentaryService {
    
    private final ChatLanguageModel model;
    
    public String generateCommentary(String event) {
        String prompt = "请用中文解说这段足球事件：" + event;
        return model.generate(prompt);
    }
}
```
```

## 使用建议

1. **选择合适提示词**：根据具体开发阶段选择对应提示词
2. **逐步完善**：先实现基础功能，再逐步添加细节
3. **注重实用性**：以满足毕业设计要求为目标
4. **保持简洁**：避免过度设计，专注核心功能

## 注意事项

- 本科毕设重在功能实现和技术展示
- 代码质量和文档完整性很重要
- 适当的技术创新点能加分
- 按时完成各阶段开发任务
