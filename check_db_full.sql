SELECT id, name, logo_url FROM teams WHERE name LIKE '%利物浦%';
SELECT id, home_team_id, away_team_id, competition_name, match_time FROM matches LIMIT 10;
SELECT COUNT(*) FROM matches;
SELECT COUNT(*) FROM teams;