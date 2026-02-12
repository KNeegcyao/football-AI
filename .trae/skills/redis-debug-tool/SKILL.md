---
name: Redis-Debug-Tool
description: 用于快速查看、验证或清理 Redis 中的缓存数据（如资讯热度、Session 等）。
---

# Redis 调试标准流程

## 执行环境
- 使用宿主机命令：`docker exec -it redis redis-cli`。

## 常用操作模板
- 查看所有 Key：`keys *`
- 查看 Key 类型：`type [key]`
- 获取 Hash 数据：`hgetall [key]`

## 严谨性要求
- 在修改或删除任何缓存前，必须先执行 `get` 或 `hgetall` 并将原始数据备份在对话记录中。
- 严禁在没有 Key 前缀的情况下执行 `flushall`。