UPDATE teams SET logo_url = 'https://resources.premierleague.com/premierleague/badges/t14.png' WHERE id = 271;
COMMIT;
SELECT id, name, logo_url FROM teams WHERE id = 271;