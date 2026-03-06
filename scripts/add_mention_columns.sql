ALTER TABLE posts ADD COLUMN mentioned_user_ids JSON COMMENT '提及的用户ID列表';
ALTER TABLE comments ADD COLUMN mentioned_user_ids JSON COMMENT '提及的用户ID列表';
