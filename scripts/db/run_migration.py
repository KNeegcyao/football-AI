import pymysql

DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '290390',
    'database': 'soccer_forum',
    'charset': 'utf8mb4'
}

def run_migration():
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cursor:
            # Check if english_name exists in teams
            cursor.execute("DESCRIBE teams")
            columns = [col[0] for col in cursor.fetchall()]
            if 'english_name' not in columns:
                print("Adding english_name column to teams...")
                cursor.execute("ALTER TABLE teams ADD COLUMN english_name VARCHAR(255) DEFAULT NULL")
            else:
                print("english_name column already exists in teams.")

            # Check if live_time exists in matches
            cursor.execute("DESCRIBE matches")
            columns = [col[0] for col in cursor.fetchall()]
            if 'live_time' not in columns:
                print("Adding live_time column to matches...")
                cursor.execute("ALTER TABLE matches ADD COLUMN live_time VARCHAR(20) DEFAULT NULL")
            else:
                print("live_time column already exists in matches.")

        conn.commit()
        print("Migration complete.")
    except Exception as e:
        print(f"Migration failed: {e}")
    finally:
        conn.close()

if __name__ == "__main__":
    run_migration()
