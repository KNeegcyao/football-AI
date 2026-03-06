
import mysql.connector

def check_notifications():
    try:
        conn = mysql.connector.connect(
            host='127.0.0.1',
            user='root',
            password='290390',
            database='soccer_forum'
        )
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT id, user_id, from_user_id, type, target_id, content FROM notifications")
        rows = cursor.fetchall()
        print(f"Total notifications: {len(rows)}")
        for row in rows:
            print(row)
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"Query failed: {e}")

if __name__ == "__main__":
    check_notifications()
