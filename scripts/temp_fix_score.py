import mysql.connector

def fix_score():
    config = {
        'user': 'root',
        'password': '290390',
        'host': '127.0.0.1',
        'database': 'soccer_forum'
    }
    
    try:
        conn = mysql.connector.connect(**config)
        cursor = conn.cursor()
        
        # 修正利物浦 vs 西汉姆联的比分为 1:0
        sql = "UPDATE matches SET home_score = 1, away_score = 0, status = 1 WHERE home_team_id = (SELECT id FROM teams WHERE name = '利物浦') AND status = 1"
        cursor.execute(sql)
        conn.commit()
        print(f"成功更新了 {cursor.rowcount} 场比赛。")
        
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"更新失败: {e}")

if __name__ == "__main__":
    fix_score()
