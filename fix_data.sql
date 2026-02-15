DELETE FROM news WHERE title LIKE '%?%';
DELETE FROM posts WHERE title LIKE '%?%';

-- 插入测试资讯 (梅西率领迈阿密国际夺冠)
INSERT INTO news (title, content, author, summary, tags, cover_url, publish_time, created_at, updated_at)
VALUES (UNHEX('E6A285E8A5BFE78E87E9A286E8BF88E998BFE5AFA6E59B9DE99985E5A4BEE586A0'), 'Messi leads Miami to victory.', 'Admin', 'Summary', 'Messi', 'https://example.com/messi.jpg', NOW(), NOW(), NOW());

-- 插入测试帖子 (大家觉得梅西还是世界第一吗？)
INSERT INTO posts (user_id, title, content, status, created_at, updated_at)
VALUES (1, UNHEX('E5A4A7E5AEB6E8A789E5BE97E6A285E8A5BFE8BF98E698AFE4B896E7958CE7ACACE4B880E59097EFBC9F'), 'Discussion about Messi.', 1, NOW(), NOW());
