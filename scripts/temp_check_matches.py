import mysql.connector

def check_today_matches():
    try:
        conn = mysql.connector.connect(
            host='127.0.0.1',
            user='root',
            password='290390',
            database='soccer_forum'
        )
        cursor = conn.cursor(dictionary=True)
        cursor.execute("""
            SELECT m.id, t1.name as home, t2.name as away, m.match_time, m.status, m.home_score, m.away_score
            FROM matches m
            JOIN teams t1 ON m.home_team_id = t1.id
            JOIN teams t2 ON m.away_team_id = t2.id
            WHERE DATE(m.match_time) = '2026-02-25'
        """)
        rows = cursor.fetchall()
        print(f"找到 2026-02-25 的比赛: {len(rows)} 场")
        for row in rows:
            print(f"ID: {row['id']}, {row['home']} {row['home_score']}:{row['away_score']} {row['away']} | Time: {row['match_time']} | Status: {row['status']}")
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"查询失败: {e}")

if __name__ == "__main__":
    check_today_matches()
