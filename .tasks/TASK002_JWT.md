# 任务记录：TASK002-002 JWT认证实现

## 执行概览
- **日期**: 2026-02-09
- **执行人**: TraeAI
- **状态**: 完成

## 已完成工作
1. **JWT 工具类**: 实现了 Token 生成、解析、验证功能 (HMAC-SHA256)。
2. **UserDetailsService**: 实现了基于数据库 (`UserMapper`) 的用户加载逻辑。
3. **安全配置**:
   - 禁用了 CSRF。
   - 配置了无状态会话 (Stateless)。
   - 配置了异常处理 (`AuthenticationEntryPoint`)，返回 401 JSON。
   - 配置了 JWT 过滤器，拦截请求并注入认证信息到 `SecurityContext`。
4. **密码加密**: 配置了 `BCryptPasswordEncoder`。

## 关键类说明
- `SecurityConfig`: Spring Security 核心配置。
- `JwtAuthenticationTokenFilter`: 请求拦截器，解析 Header 中的 Bearer Token。
- `LoginUser`: `UserDetails` 的实现，封装了 `User` 实体。

## 下一步计划
- 执行 TASK002-003：微信小程序授权登录（或先实现基础的注册登录 API）。
