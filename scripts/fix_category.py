import mysql.connector

DB_CONFIG = {
    'host': '127.0.0.1',
    'user': 'root',
    'password': '290390',
    'database': 'soccer_forum',
    'charset': 'utf8mb4'
}

CAT_MAP = {
    '中超': 1,
    '英超': 2,
    '德甲': 3,
    '意甲': 4,
    '欧冠': 5,
    '欧战': 5,
    '欧联': 5,
    '西甲': 6
}

def get_category_id(secondary_categories, primary_category, title=""):
    for key, val in CAT_MAP.items():
        if key in title:
            return val
            
    all_cats = [primary_category] + (secondary_categories or [])
    for cat in all_cats:
        if cat in CAT_MAP:
            return CAT_MAP[cat]
            
    for cat in all_cats:
        for key, val in CAT_MAP.items():
            if key in cat:
                return val
                
    return 0

def fix_db():
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)
    
    cursor.execute("SELECT id, title, category, tags FROM news")
    articles = cursor.fetchall()
    
    update_count = 0
    for article in articles:
        sec = article['tags'].split(',') if article['tags'] else []
        pri = article['category'] or ''
        title = article['title'] or ''
        
        cat_id = get_category_id(sec, pri, title)
        
        cursor.execute("UPDATE news SET category_id = %s WHERE id = %s", (cat_id, article['id']))
        update_count += 1
        
    conn.commit()
    print(f"Updated category_id for {update_count} articles.")
    cursor.close()
    conn.close()

if __name__ == '__main__':
    fix_db()
