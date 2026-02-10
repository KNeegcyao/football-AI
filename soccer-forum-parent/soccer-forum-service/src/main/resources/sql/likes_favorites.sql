CREATE TABLE IF NOT EXISTS `likes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `target_type` TINYINT NOT NULL COMMENT '类型:1帖子,2评论',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_target_user` (`target_id`, `target_type`, `user_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

CREATE TABLE IF NOT EXISTS `favorites` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
