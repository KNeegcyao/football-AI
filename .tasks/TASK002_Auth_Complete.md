# 任务记录：TASK002-003 & 004 登录注册功能

## 执行概览
- **日期**: 2026-02-09
- **执行人**: TraeAI
- **状态**: 完成

## 已完成工作
1. **基础认证 API (TASK002-004)**:
   - 实现了 `AuthController`，提供 `/api/auth/login` 和 `/api/auth/register`。
   - 实现了基于 JWT 的 Token 生成与验证。
2. **微信小程序登录 (TASK002-003)**:
   - 集成了 `wx-java-miniapp` SDK。
   - 更新了 `User` 表，添加了 `openid` 字段。
   - 实现了 `/api/auth/wechat` 接口，支持自动注册和登录。
   - 配置了 `application.yml` 中的微信参数占位符。

## 数据库变更
- `users` 表新增 `openid` 字段，并添加了唯一索引 `uk_openid`。

## 注意事项
- 微信登录需要配置环境变量 `WX_APPID` 和 `WX_SECRET`，或者在 `application.yml` 中直接修改默认值。
