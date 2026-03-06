SELECT id, name, logo_url FROM teams WHERE name LIKE '%利物浦%' OR id = 271;
SELECT m.id, m.home_team_id, m.away_team_id, t1.name as home_name, t1.logo_url as home_logo, t2.name as away_name, t2.logo_url as away_logo 
FROM matches m 
JOIN teams t1 ON m.home_team_id = t1.id 
JOIN teams t2 ON m.away_team_id = t2.id 
WHERE m.match_time LIKE '2026-03-04%' AND (t1.name LIKE '%利物浦%' OR t2.name LIKE '%利物浦%');