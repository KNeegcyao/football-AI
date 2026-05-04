import mysql.connector

DB_CONFIG = {
    'host': '127.0.0.1',
    'user': 'root',
    'password': '290390',
    'database': 'soccer_forum',
    'charset': 'utf8mb4'
}

def fix_db():
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()
    cursor.execute("UPDATE news SET content = REPLACE(content, 'style=\"display:none;\"', '')")
    conn.commit()
    print(f"Fixed {cursor.rowcount} rows in news table.")
    cursor.close()
    conn.close()

if __name__ == '__main__':
    fix_db()
