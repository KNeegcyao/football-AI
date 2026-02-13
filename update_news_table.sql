USE soccer_forum;
ALTER TABLE news ADD COLUMN like_count INT DEFAULT 0 AFTER view_count;
ALTER TABLE news ADD COLUMN comment_count INT DEFAULT 0 AFTER like_count;
