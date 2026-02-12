# Soccer Forum (足球社区)

这是一个基于 Spring Boot 的多模块足球社区后端项目。

## 项目模块

- `soccer-forum-parent`: 父模块，管理依赖版本
- `soccer-forum-common`: 公共模块，包含通用工具类、枚举和异常定义
- `soccer-forum-domain`: 领域模型模块（暂未启用，当前使用简单 DTO）
- `soccer-forum-service`: 业务服务模块，包含 API 接口、业务逻辑和持久层实现

## 目录结构规范

- `sql/`: 存放数据库建表语句及初始化脚本
- `docs/`: 存放项目相关的设计文档、API 文档说明等
- `.tasks/`: Trae IDE 任务跟踪目录

## 快速开始

1. 执行 `sql/` 目录下的脚本初始化数据库
2. 修改 `soccer-forum-service/src/main/resources/application.yml` 中的数据库和 Redis 连接信息
3. 启动 `SoccerForumApplication`

## 技术栈

- Spring Boot 3.x
- MyBatis Plus
- Spring Security + JWT
- Redis
- MySQL
- Swagger/Knife4j
