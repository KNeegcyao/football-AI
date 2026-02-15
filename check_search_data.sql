SELECT 'news' as type, COUNT(*) as count FROM news WHERE title LIKE '%梅西%' OR tags LIKE '%梅西%'
UNION ALL
SELECT 'posts' as type, COUNT(*) as count FROM posts WHERE title LIKE '%梅西%' AND status = 1;
