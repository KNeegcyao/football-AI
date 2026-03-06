import mysql.connector
conn = mysql.connector.connect(user='root', password='290390', host='127.0.0.1', database='soccer_forum')
cursor = conn.cursor()
cursor.execute('SELECT h.name, a.name FROM matches m JOIN teams h ON m.home_team_id = h.id JOIN teams a ON m.away_team_id = a.id WHERE m.match_time >= "2026-02-28 20:00:00"')
for row in cursor.fetchall():
    print(row)
conn.close()
