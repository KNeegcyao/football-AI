<div align="center">
  <img src="./frontend/src/static/logo.png" width="120" alt="Football AI Forum Logo" />
  <h1>⚽ 绿茵集 (Football AI Forum)</h1>
  <p><strong>一个由大语言模型（LLM）深度驱动的现代化足球球迷社区</strong></p>

  <p>
    <img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg" alt="Spring Boot">
    <img src="https://img.shields.io/badge/Vue-3.x-blue.svg" alt="Vue">
    <img src="https://img.shields.io/badge/Uni--app-Vite-2b9939.svg" alt="Uni-app">
    <img src="https://img.shields.io/badge/AI-LangChain4j%20%7C%20Dify-orange.svg" alt="AI Driven">
    <img src="https://img.shields.io/badge/License-MIT-lightgrey.svg" alt="License">
  </p>
</div>

---

## 📖 项目简介

**绿茵集** 采用前后端分离架构（Spring Boot + Uni-app），旨在为足球爱好者打造一个兼具沉浸感与智能化的讨论社区。项目不仅包含完善的论坛基础闭环（发帖、回复、点赞、通知），更引入了 **真实赛事/球员数据接入** 与 **全场景 AI 落地**（虚拟球迷回帖、AI 智能助手、帖子摘要生成），让球迷的每一次互动都不再孤单。

## ✨ 核心功能模块

### 1. 🤖 AI 智能矩阵
- **FanAgent 虚拟球迷**：后端基于 `LangChain4j` 结合 Spring `@Async` 定时任务，在用户发布新帖时，大模型会根据预设的 3 种球迷人设（`战术宅`、`情怀粉`、`毒舌喷子`）自动生成带有特定语气的评论，极大地活跃了社区初始氛围。
- **AI 赛事助手 (Dify 集成)**：深度接入 Dify 会话引擎，支持通过 `conversation_id` 保持多轮上下文。前端使用 `marked.js` 渲染 Markdown，配合打字机动画，提供沉浸式的问答体验。
- **AI 智能发评**：在帖子详情页，用户可一键调用 AI，根据当前帖子的上下文自动生成高质量、有深度的足球评论。

### 2. 🏟️ 社区互动闭环
- **动态与话题**：支持图文混排的帖子发布，按“热门话题”、“圈子”进行分类流展示。
- **互动机制**：集成点赞 (Like)、收藏 (Favorite)、评论楼层。后端采用事务级的计数器更新与逻辑删除 (`MyBatis-Plus`)，保障数据一致性。
- **全局通知与未读红点**：前端使用 `Pinia` 结合事件总线 (`uni.$on`)，实时聚合私信消息与系统通知（如经验值升级、官方欢迎语，类型 8），并在底部 TabBar 动态渲染未读徽章。
- **角色权限管理**：区分普通用户与管理员 (`UserRole.ADMIN`)，管理员及帖子原作者具有前端可见的物理/逻辑删除权限。

### 3. 📊 真实数据与容错降级
- **SportAPI 接入**：通过后端 `SportApiClient` 实时拉取 RapidAPI 的球队、球员及比赛数据。
- **优雅降级 (Mock Fallback)**：内置完善的异常捕获与容错机制。当遇到接口限流 (429)、网络不通或 API 密钥未配置时，系统将无缝切回本地 Mock 数据，确保前端页面永不崩溃。

### 4. 📱 极致的前端体验
- **跨端架构**：基于 `Uni-app (Vue 3 + Vite)`，一次编写即可编译为 H5、微信小程序和 App。
- **全局 UI 动画系统**：深度定制了 UX 级交互动画，包括：
  - `.anim-slide-up`：卡片/列表的物理弹性上浮入场。
  - `.stagger-*`：多图文列表的交错延迟加载（告别一次性生硬渲染）。
  - `.btn-active`：全局按压微缩放与透明度反馈，全面提升指尖触感。

---

## 🛠️ 技术栈详情

### 💻 前端技术 (Frontend)
- **核心框架**: Vue 3 (Composition API) + Vite
- **跨端框架**: Uni-app
- **状态管理**: Pinia
- **UI 组件库**: uView Plus (全面兼容 Vue 3)
- **网络请求**: 自定义 Request 封装 (支持 JWT Token 自动拦截与无感刷新)
- **样式处理**: SCSS (抽取全局主题变量与动画帧)

### ⚙️ 后端技术 (Backend)
- **核心框架**: Spring Boot 3.x / JDK 17
- **项目架构**: Maven 多模块化设计 (Parent > Common, Domain, Service)
- **持久层**: MyBatis-Plus + MySQL 8.x
- **缓存与会话**: Redis (用于资讯热度、验证码、Session 缓存、Token 黑名单)
- **大模型框架**: LangChain4j (用于后端直连大模型)
- **安全认证**: Spring Security + JWT
- **第三方 SDK**: Aliyun OSS/SMS (可选配置)

---

## 📂 项目目录结构

```text
football/
├── frontend/                 # 前端项目根目录
│   ├── src/
│   │   ├── api/              # 后端接口请求封装 (按业务模块划分)
│   │   ├── components/       # 公共业务组件 (骨架屏、空状态等)
│   │   ├── pages/            # 页面视图 (首页、社区、消息、AI、我的等)
│   │   ├── static/           # 静态资源 (默认头像、图标)
│   │   ├── store/            # Pinia 状态管理 (chatStore, userStore)
│   │   ├── App.vue           # 全局应用入口与核心动画样式库
│   │   └── uni.scss          # 全局 SCSS 主题变量
│   ├── vite.config.js        # Vite 编译配置 (已开放局域网 --host 访问)
│   └── package.json          # 前端依赖配置
│
└── soccer-forum-parent/      # 后端 Spring Boot 根目录
    ├── soccer-forum-common/  # 基础通用模块 (全局异常处理、常量、Utils)
    ├── soccer-forum-domain/  # 领域实体模块 (POJO, DTO, VO)
    └── soccer-forum-service/ # 核心服务模块 (Controller, Service, Mapper)
        ├── src/main/java/com/soccer/forum/service/
        │   ├── modules/      # 业务模块 (auth, post, user, match, ai)
        │   ├── config/       # Spring 配置类 (Redis, Mybatis, Security, 跨域)
        │   └── handler/      # 拦截器与切面处理
        └── src/main/resources/
            ├── application.yml     # 主配置文件
            └── application-dev.yml # 开发环境配置 (数据库、API 密钥等)
```

---

## 🚀 快速开始指南

### 第一步：环境准备
确保您的本地开发环境已安装以下软件：
- [Node.js](https://nodejs.org/) (v16.0.0 及以上版本)
- [JDK 17](https://adoptium.net/zh-CN/temurin/releases/) 及以上版本
- [Maven](https://maven.apache.org/) (3.6+版本)
- [MySQL](https://www.mysql.com/) (8.0+版本)
- [Redis](https://redis.io/) (默认 6379 端口)

### 第二步：后端配置与启动
1. **初始化数据库**
   - 在 MySQL 中创建数据库 `soccer_forum`，字符集推荐使用 `utf8mb4`。
   - 导入项目提供的初始 SQL 脚本（如存在）或依赖 MyBatis-Plus/Flyway 自动生成表结构。
2. **修改配置文件**
   - 进入 `soccer-forum-parent/soccer-forum-service/src/main/resources/`。
   - 打开 `application-dev.yml`，修改 MySQL 和 Redis 的连接信息（账号、密码）。
   - 配置第三方 API 密钥（如无需测试真实数据，系统会自动使用 Mock 数据）：
     ```yaml
     sportapi:
       host: sportapi7.p.rapidapi.com
       key: ${SPORT_API_KEY:你的API_KEY}
     ```
3. **启动服务**
   - 在 `soccer-forum-service` 目录下执行 Maven 命令：
     ```bash
     mvn clean compile
     mvn spring-boot:run
     ```
   - 后端服务将默认运行在 `http://localhost:8080`。

### 第三步：前端配置与启动
1. **安装依赖**
   - 进入前端根目录 `frontend`：
     ```bash
     npm install
     ```
2. **启动开发服务器 (H5 模式)**
   - 执行启动命令（已默认开启局域网共享）：
     ```bash
     npm run dev:h5
     ```
   - 启动成功后，终端会打印出访问地址：
     ```text
     Network: http://192.168.x.x:5173/
     Local:   http://localhost:5173/
     ```
3. **移动端调试注意事项**
   - 确保手机与电脑处于**同一 Wi-Fi 网络**。
   - 若手机无法访问 `5173` 或无法获取数据 (`8080`)，请检查 Windows 防火墙，将网络配置文件设为**“专用网络”**，或手动在命令行放行端口。

---

## 📝 贡献指南
欢迎各位开发者提交 Issue 和 Pull Request！
1. Fork 本仓库。
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)。
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)。
4. 推送到分支 (`git push origin feature/AmazingFeature`)。
5. 开启一个 Pull Request。

## 📄 许可证
本项目采用 [MIT License](LICENSE) 开源协议。
