USE soccer_forum;

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
