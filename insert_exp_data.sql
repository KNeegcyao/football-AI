INSERT INTO experience_record (user_id, amount, reason, created_at) SELECT id, 10, '发布新帖子', DATE_SUB(NOW(), INTERVAL 1 HOUR) FROM users WHERE phone = '13183375830';
INSERT INTO experience_record (user_id, amount, reason, created_at) SELECT id, 5, '帖子获赞', DATE_SUB(NOW(), INTERVAL 2 HOUR) FROM users WHERE phone = '13183375830';
INSERT INTO experience_record (user_id, amount, reason, created_at) SELECT id, 2, '发表评论', DATE_SUB(NOW(), INTERVAL 3 HOUR) FROM users WHERE phone = '13183375830';
INSERT INTO experience_record (user_id, amount, reason, created_at) SELECT id, 10, '发布新帖子', DATE_SUB(NOW(), INTERVAL 1 DAY) FROM users WHERE phone = '13183375830';
