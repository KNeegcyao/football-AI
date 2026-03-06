SELECT m.id, m.home_team_id, m.away_team_id, t1.name as home_name, t2.name as away_name, m.match_time 
FROM matches m 
JOIN teams t1 ON m.home_team_id = t1.id 
JOIN teams t2 ON m.away_team_id = t2.id 
WHERE t1.id = 271 OR t2.id = 271
ORDER BY m.match_time DESC;