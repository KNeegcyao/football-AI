import mysql.connector

def deep_clean():
    try:
        conn = mysql.connector.connect(
            host='127.0.0.1',
            user='root',
            password='290390',
            database='soccer_forum'
        )
        cursor = conn.cursor(dictionary=True)
        
        # 1. 删除所有历史日期（21-23日）且状态为 0（未开始）的比赛
        # 这些通常是孤立记录或由于球队 ID 变化导致的冗余记录
        print("正在清理历史未开始比赛...")
        cursor.execute("""
            DELETE FROM matches 
            WHERE match_time < '2026-02-24 00:00:00' 
            AND status = 0
        """)
        print(f"已删除历史未开始比赛: {cursor.rowcount} 条")

        # 2. 删除非目标联赛比赛 (使用中文名，因为 fetch_schedule.py 存储的是中文)
        target_leagues = ['英超', '西甲', '德甲', '意甲', '法甲', '欧冠', 'Premier League', 'Championship', 'Bundesliga', 'Serie A', 'Ligue 1', 'Primera Division', 'UEFA Champions League']
        placeholders = ', '.join(['%s'] * len(target_leagues))
        cursor.execute(f"DELETE FROM matches WHERE competition_name NOT IN ({placeholders})", tuple(target_leagues))
        print(f"已清理非目标联赛比赛: {cursor.rowcount} 条")

        # 3. 删除无关联球队（仅删除没有外键约束的球队）
        # 如果球队有关联球员，则保留
        cursor.execute("""
            DELETE FROM teams 
            WHERE id NOT IN (SELECT DISTINCT home_team_id FROM matches) 
            AND id NOT IN (SELECT DISTINCT away_team_id FROM matches)
            AND id NOT IN (SELECT DISTINCT current_team_id FROM players WHERE current_team_id IS NOT NULL)
        """)
        print(f"已清理无关联球队: {cursor.rowcount} 条")

        # 4. 修复常见名称和头像
        cursor.execute("UPDATE teams SET name = '昂热', logo_url = 'https://crests.football-data.org/532.png' WHERE name = '雑貨' OR name = 'Angers SCO'")
        
        conn.commit()
        print("深度清理完成。")
    except Exception as e:
        print(f"清理失败: {e}")
        conn.rollback()
    finally:
        if 'cursor' in locals(): cursor.close()
        if 'conn' in locals(): conn.close()

if __name__ == "__main__":
    deep_clean()
