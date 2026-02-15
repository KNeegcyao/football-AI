SET NAMES utf8mb4;
-- 插入测试资讯
INSERT INTO news (title, content, author, summary, tags, cover_url, publish_time, created_at, updated_at)
VALUES ('梅西率领迈阿密国际夺冠', '在今晨结束的比赛中，梅西表现出色，带领球队获得胜利。', '体育编辑', '梅西再次展现球王风采。', '梅西,足球,迈阿密国际', 'https://example.com/messi.jpg', NOW(), NOW(), NOW());

-- 插入测试帖子
INSERT INTO posts (user_id, title, content, status, created_at, updated_at)
VALUES (1, '大家觉得梅西还是世界第一吗？', '讨论一下梅西目前的竞技状态。', 1, NOW(), NOW());
