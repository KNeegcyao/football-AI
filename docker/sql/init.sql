CREATE DATABASE IF NOT EXISTS soccer_forum DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE soccer_forum;

-- 用户表 (基础结构，后续任务会完善)
CREATE TABLE IF NOT EXISTS `users` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
    `role` varchar(20) DEFAULT 'USER' COMMENT '角色',
    `status` tinyint(4) DEFAULT 1 COMMENT '状态 1:正常 0:禁用',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 帖子表
CREATE TABLE IF NOT EXISTS `posts` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` varchar(100) NOT NULL COMMENT '标题',
    `content` text NOT NULL COMMENT '内容',
    `user_id` bigint(20) NOT NULL COMMENT '作者ID',
    `views` int(11) DEFAULT 0 COMMENT '浏览量',
    `likes` int(11) DEFAULT 0 COMMENT '点赞数',
    `status` tinyint(4) DEFAULT 1 COMMENT '状态 1:正常 0:删除',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- 插入测试数据 (密码为 bcrypt 加密的 "123456")
INSERT INTO `users` (`username`, `password`, `nickname`, `role`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBk8o2a.beWWpMkdrcjS', '管理员', 'ADMIN');
