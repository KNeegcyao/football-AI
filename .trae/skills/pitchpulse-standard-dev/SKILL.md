---
name: PitchPulse-Standard-Dev
description: 用于将 Google Stitch 的原型设计无缝转换为本项目 uniapp 页面，并自动对接现有的 Spring Boot 后端接口
---

# PitchPulse 开发准则

## 1. UI 转换规范 (Web to uniapp)
- **标签替换**：强制将 HTML 标签映射为 uniapp 标签（div -> view, span/p -> text, img -> image）。
- **单位换算**：所有设计稿中的 px 必须乘以 2 转换为 rpx，以适配移动端。
- **主题色**：强制使用预定义的 SCSS 变量：背景 `$pitch-pulse-bg-dark`，主色 `$pitch-pulse-primary` (金黄色)。

## 2. 定位与适配规则
- **固定定位**：所有固定在底部(tab-bar)或悬浮(fab-btn)的元素，必须参考 index.vue 的居中逻辑（left: 50% + translateX）以适配 H5 预览模式。
- **安全区**：所有底部元素必须添加 `env(safe-area-inset-bottom)` 填充。

## 3. 后端对接协议
- **搜索逻辑**：当生成新页面或功能时，AI 必须先扫描 soccer-forum-service 模块下所有的 @RestController。

- **接口匹配**：根据当前页面业务（如：赛事 -> MatchController，动态 -> PostController），精准定位具体的 RequestMapping 路径。

- **参数规范**：强制查看 Controller 方法中的 @RequestBody 或 @RequestParam，确保前端 uni.request 的 data 结构与后端 DTO 完全对等。

- **统一响应**：所有接口必须适配项目中的 R.java（统一返回对象），根据 code 码（如 200 为成功）来编写前端的 if 逻辑。
## 4. 视觉防冲突
- 所有的图片标签必须自带 `mode="aspectFill"` 或 `mode="aspectFit"` 属性，严禁拉伸变形。
- 顶部 Logo 统一使用 `/static/soccer-logo.png`，并保持 `padding` 比例。