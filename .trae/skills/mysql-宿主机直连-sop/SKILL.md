---
name: MySQL 宿主机直连 SOP
description: 当需要创建数据库表、初始化数据或执行 SQL 查询时触发。用于绕过 Docker 网络隔离导致的 localhost 连接失败问题
---

# MySQL-Native-Handler

## 核心原则
- 禁止使用 MCP Database Server 插件进行写操作。
- 必须通过宿主机 PowerShell 环境直接调用 `mysql` 客户端。

## 使用场景
- 执行 `.sql` 初始化文件。
- 验证表结构或查询具体数据记录。
- 插入开发阶段的测试数据。

## 指令模板
执行命令时，必须包含完整的连接参数：
`mysql -h 127.0.0.1 -P 3306 -u root -p290390 --default-character-set=utf8mb4 soccer_forum -e "YOUR_SQL_HERE"`

## 输出解释
- 执行前：先输出拟执行的 SQL 逻辑。
- 执行后：展示受影响的行数或查询结果的前 5 行，并确认数据是否已持久化。

## 示例
当用户要求“同步资讯表”时：
1. 定位项目中的 SQL 文件路径。
2. 执行：`mysql -h 127.0.0.1 -P 3306 -u root -p290390 soccer_forum < d:\project\football\create_news_match_tables.sql`