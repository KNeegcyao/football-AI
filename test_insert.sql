INSERT INTO notifications (user_id, from_user_id, type, target_id, content, is_read, created_at) 
VALUES (1, 2, 4, 7, '这是一条通过数据库手动插入的测试回复通知', 0, NOW());