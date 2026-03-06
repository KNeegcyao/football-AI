SELECT id, name FROM teams WHERE name LIKE '%巴塞罗那%' OR name LIKE '%巴萨%';
SELECT id, name FROM teams WHERE name LIKE '%巴黎%';
SELECT id, name FROM teams WHERE name LIKE 'P%';
SELECT id, name FROM teams WHERE name LIKE 'B%';
SELECT id, name FROM teams WHERE name LIKE 'F%';
SELECT * FROM teams LIMIT 10;