UPDATE teams SET logo_url = 'https://crests.football-data.org/64.png' WHERE id = 271;
COMMIT;
SELECT id, name, logo_url FROM teams WHERE id = 271;