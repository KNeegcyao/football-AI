import sys
import requests
from bs4 import BeautifulSoup
import mysql.connector
import json
from datetime import datetime

sys.stdout.reconfigure(encoding='utf-8')

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
    # 优先匹配标题中的关键字，例如标题带有“欧冠”就归入欧冠
    for key, val in CAT_MAP.items():
        if key in title:
            return val
            
    all_cats = [primary_category] + (secondary_categories or [])
    
    # 精确匹配
    for cat in all_cats:
        if cat in CAT_MAP:
            return CAT_MAP[cat]
            
    # 模糊匹配
    for cat in all_cats:
        for key, val in CAT_MAP.items():
            if key in cat:
                return val
                
    return 0 # 默认推荐/其他

def get_db_connection():
    return mysql.connector.connect(**DB_CONFIG)

def fetch_dongqiudi_news():
    print("[NewsCrawler] Fetching news list from Dongqiudi...")
    # 懂球帝各分类Tab: 1-头条, 3-中超, 4-英超, 5-西甲, 6-意甲, 56-德甲, 57-五洲(欧冠等)
    tabs = [1, 3, 4, 5, 6, 56, 57]
    all_articles = []
    
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
    
    for tab in tabs:
        url = f"https://api.dongqiudi.com/app/tabs/web/{tab}.json"
        try:
            print(f"[NewsCrawler] Fetching tab {tab}...")
            response = requests.get(url, headers=headers, timeout=10)
            response.raise_for_status()
            response.encoding = 'utf-8' # 强制使用 utf-8 编码
            data = response.json()
            articles = data.get("articles", [])
            all_articles.extend(articles)
            print(f"[NewsCrawler] Got {len(articles)} articles from tab {tab}.")
        except Exception as e:
            print(f"[NewsCrawler] Error fetching news list for tab {tab}: {e}")
            
    return all_articles

def fetch_article_content(article_id):
    url = f"https://www.dongqiudi.com/articles/{article_id}.html"
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
    try:
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()
        response.encoding = 'utf-8' # 强制使用 utf-8 编码
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Try to find the content div
        content_div = soup.find('div', class_='con')
        if not content_div:
            return None
            
        # Fix dongqiudi hidden content
        for tag in content_div.find_all(style=lambda value: value and 'display:none' in value.replace(' ', '')):
            del tag['style']
            
        # Clean up scripts or ads inside content if needed
        for s in content_div.select('script, style'):
            s.extract()
            
        # Keep HTML structure or extract text
        # Let's keep HTML so it can be rendered as rich text in the frontend
        html_content = str(content_div)
        return html_content
    except Exception as e:
        print(f"[NewsCrawler] Error fetching article {article_id} content: {e}")
        return None

def sync_news_to_db():
    articles = fetch_dongqiudi_news()
    if not articles:
        return
        
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    
    new_count = 0
    
    for article in articles:
        # Check if it's an ad or invalid
        if article.get('is_business_ad', 0) == 1 or not article.get('title'):
            continue
            
        title = article['title'].strip()
        
        # Check if already exists
        cursor.execute("SELECT id FROM news WHERE title = %s", (title,))
        if cursor.fetchone():
            print(f"[NewsCrawler] Skip existing: {title}")
            continue
            
        article_id = article.get('id')
        if not article_id:
            continue
            
        print(f"[NewsCrawler] Fetching content for: {title}")
        content = fetch_article_content(article_id)
        if not content:
            print(f"[NewsCrawler] Content empty, skip.")
            continue
            
        summary = article.get('description', '')
        cover_url = article.get('thumb', '')
        author = article.get('author_name', '懂球帝')
        publish_time_str = article.get('published_at')
        if not publish_time_str:
            publish_time_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            
        category = article.get('category', '足球')
        secondary = article.get('secondary_category', [])
        
        # 只保留足球新闻，过滤其他体育项目
        if category in ['篮球', '体坛', '电竞', '综合体育', '乒乓球', '网球', '台球'] or '篮球' in category:
            print(f"[NewsCrawler] Skip non-football: {title} ({category})")
            continue
            
        tags = ",".join(secondary) if secondary else category
        
        # 计算我们系统的 category_id
        category_id = get_category_id(secondary, category, title)
        
        # Insert to DB
        insert_sql = """
            INSERT INTO news (title, summary, content, cover_url, author, publish_time, tags, category, category_id, view_count, like_count, comment_count, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
        """
        
        # Simulate some random view/like counts based on Dongqiudi data
        view_count = int(article.get('comments_total', 0)) * 15 + 100
        like_count = int(article.get('comments_total', 0)) * 2 + 5
        comment_count = int(article.get('comments_total', 0))
        
        try:
            cursor.execute(insert_sql, (
                title, summary, content, cover_url, author, publish_time_str, tags, category, category_id,
                view_count, like_count, comment_count
            ))
            new_count += 1
        except Exception as e:
            print(f"[NewsCrawler] DB insert error for '{title}': {e}")
            
    conn.commit()
    cursor.close()
    conn.close()
    
    print(f"[NewsCrawler] Finished. Inserted {new_count} new articles.")

if __name__ == "__main__":
    sync_news_to_db()
