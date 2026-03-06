DESCRIBE teams;
SELECT team_id, COUNT(*) FROM team_follows GROUP BY team_id;
SELECT id, name, is_hot, is_recommend FROM teams WHERE name = '国际米兰';
SELECT id, name, is_hot, is_recommend FROM teams WHERE id <= 5;