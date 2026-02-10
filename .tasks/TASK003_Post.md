# 任务记录：TASK003-001 帖子管理

## 执行概览
- **日期**: 2026-02-09
- **执行人**: TraeAI
- **状态**: 完成

## 已完成工作
1. **数据库设计**:
   - 在 `init.sql` 中添加了 `posts` 表，包含 `title`, `content`, `user_id`, `views`, `likes`, `status` 等字段。
2. **后端接口**:
   - `PostController`: 实现了 `/api/posts` 的 CRUD 接口。
   - `PostService`: 封装了发帖、查询详情（自动增加阅读量）、分页列表（支持关键词搜索）、软删除逻辑。
   - `PostCreateReq` / `PostPageReq`: 定义了入参 DTO。

## 接口说明
- `POST /api/posts`: 发布帖子（需登录）。
- `GET /api/posts/{id}`: 获取详情（增加阅读量）。
- `GET /api/posts?page=1&size=10&keyword=...`: 分页搜索。
- `DELETE /api/posts/{id}`: 删除帖子（仅作者）。

## 下一步计划
- 执行 TASK003-002：评论功能。
