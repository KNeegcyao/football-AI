# 背景
文件名：2026-02-12_1_swagger-docs-completion.md
创建于：2026-02-12_19:27:00
创建者：Gemini-3-Preview
主分支：main
任务分支：task/swagger-docs-completion_2026-02-12_1
Yolo模式：Off

# 任务描述
确保 Knife4j 中的所有接口都有完整的参数说明。修复实体类（Player, Team, Match, News 等）在 Knife4j UI 中缺失字段描述的问题，方法是将 @Schema 注解同步到 Getter 方法上。同时完善 Controller 中的 @Parameter 描述。

# 项目概览
本项目是一个足球论坛后端，使用 Spring Boot + MyBatis-Plus + Knife4j (Swagger 3)。需要为所有对外暴露的 API 提供高质量的文档。

⚠️ 警告：永远不要修改此部分 ⚠️
1. 系统思维：从整体架构到具体实现进行分析
2. 辩证思维：评估多种解决方案及其利弊
3. 创新思维：打破常规模式，寻求创造性解决方案
4. 批判性思维：从多个角度验证和优化解决方案
⚠️ 警告：永远不要修改此部分 ⚠️

# 分析
- 观察到 Knife4j 在某些配置下无法正确解析实体类的字段级 @Schema 注解。
- 经过验证（在 Player.java 中），将 @Schema 注解移动或复制到 Getter 方法上可以解决此问题。
- 需要对所有涉及 API 请求和响应的 Entity 和 DTO 进行此类同步。
- 部分 Controller 方法的 @RequestBody 参数缺少 @Parameter 描述，导致 UI 显示不直观。

# 提议的解决方案
1. 遍历所有实体类（Team, Match, News, Comment, Favorite, Like, Notification, User），将字段上的 @Schema 描述同步到对应的 Getter 方法。
2. 遍历所有 DTO 类（CommentCreateReq, PostCreateReq 等），执行相同的 @Schema 同步。
3. 检查并完善 Controller（FavoriteController, LikeController, NotificationController, UserController）中的 @Operation 和 @Parameter 注解。

# 当前执行步骤："1. 同步实体类注解"
- 处理 Team.java
- 处理 Match.java
- 处理 News.java
- 处理 Comment.java
- 处理 Favorite.java
- 处理 Like.java
- 处理 Notification.java

# 任务进度
- 2026-02-12_19:27:00: 创建任务文件，开始同步实体类注解。
- 2026-02-12_19:35:00: 已完成 Team, Match, News, Comment 实体类及 CommentCreateReq, CommentPageReq DTO 的注解同步。
- 2026-02-12_19:45:00: 已完成 LoginBody, PostPageReq, PostCreateReq, UserUpdateReq, LikeReq, FavoriteReq 的注解同步。
- 2026-02-12_19:48:00: 已完善 PostController, AuthController, FavoriteController, LikeController, UserController 的 @Parameter 描述。
- 2026-02-12_19:55:00: 已将所有被弃用的 `required = true` 替换为 `requiredMode = Schema.RequiredMode.REQUIRED`。

# 最终审查
[完成后的总结]
