---
name: MBP-Entity-Generator
description: 用于根据数据库表结构生成或完善 MyBatis-Plus 实体类代码。
---

# MBP-Entity-Generator 规范

## 强制要求
- 实体类必须实现 `Serializable` 接口。
- 必须使用 Lombok 注解：`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`。
- 必须明确指定 `@TableName("table_name")`。
- 主键字段必须标注 `@TableId(type = IdType.AUTO)`。

## 逻辑检查
- 检查数据库中的 `created_at` 是否对应 Java 的 `LocalDateTime`。
- 检查是否存在逻辑删除字段 `is_deleted`，若有则添加 `@TableLogic`。