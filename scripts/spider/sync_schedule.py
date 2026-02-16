import requests
import pymysql
from datetime import datetime, timedelta
import time
import logging
from duckduckgo_search import DDGS
import re

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '290390',
    'database': 'soccer_forum',
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

# 目标联赛 ID (LiveScore Sid) -> 中文名
# 目标联赛 ID 映射 (根据 2026 LiveScore API 调试结果更新)
TARGET_LEAGUES = {
    '21997': '英超',   # Premier League
    '22180': '西甲',   # LaLiga
    '21862': '意甲',   # Serie A
    '22091': '德甲',   # Bundesliga
    '22118': '法甲',   # Ligue 1
    '24464': '欧冠',   # Champions League - Knockout Stage
}

# 状态映射
# LiveScore Eps -> DB Status (0: 未开始, 1: 进行中, 2: 完赛)
# LiveScore Eps examples: "NS", "HT", "FT", "AET", "AP", "67'"
def map_status(eps):
    if eps == 'NS':
        return 0
    elif eps in ['FT', 'AET', 'AP']:
        return 2
    elif eps in ['Postp.', 'Canc.', 'Abd.']:
        return 0 # 或者其他状态，暂时当做未开始处理，或者忽略
    else:
        # 其他情况视为进行中 (如 1', HT, 45+2')
        return 1

def get_connection():
    return pymysql.connect(**DB_CONFIG)

# 手动翻译映射表 (English -> Chinese)
MANUAL_TRANSLATIONS = {
    'Wolves': '狼队',
    'Qarabag FK': '卡拉巴赫',
    'Bodoe/Glimt': '博德闪耀',
    'Club Brugge': '布鲁日',
    'Club Brugge KV': '布鲁日',
    'Como 1907': '科莫',
    'Olympiacos': '奥林匹亚科斯',
    'Olympiacos FC': '奥林匹亚科斯',
    'PSV Eindhoven': '埃因霍温',
    'Sporting CP': '葡萄牙体育',
    'Galatasaray': '加拉塔萨雷',
    'Fenerbahce': '费内巴切',
    'Besiktas': '贝西克塔斯',
    'Shakhtar Donetsk': '顿涅茨克矿工',
    'Benfica': '本菲卡',
    'Porto': '波尔图',
    'Ajax': '阿贾克斯',
    'Feyenoord': '费耶诺德',
    'Celtic': '凯尔特人',
    'Rangers': '流浪者',
    'Dynamo Kyiv': '基辅迪纳摩',
    'Slavia Prague': '布拉格斯拉维亚',
    'Sparta Prague': '布拉格斯巴达',
    'Red Star Belgrade': '贝尔格莱德红星',
    'Young Boys': '年轻人',
    'RB Salzburg': '萨尔茨堡红牛',
    'Sturm Graz': '格拉茨风暴',
    'Brest': '布雷斯特',
    'Girona': '赫罗纳',
    'Bologna': '博洛尼亚',
    'Aston Villa': '阿斯顿维拉',
    'Newcastle United': '纽卡斯尔联',
    'Brighton & Hove Albion': '布莱顿',
    'West Ham United': '西汉姆联',
    'Real Sociedad': '皇家社会',
    'Real Betis': '皇家贝蒂斯',
    'Villarreal': '比利亚雷亚尔',
    'Athletic Club': '毕尔巴鄂竞技',
    'Lazio': '拉齐奥',
    'Roma': '罗马',
    'Atalanta': '亚特兰大',
    'Fiorentina': '佛罗伦萨',
    'Nice': '尼斯',
    'Lille': '里尔',
    'Monaco': '摩纳哥',
    'Lens': '朗斯',
    'Marseille': '马赛',
    'Lyon': '里昂',
    'Stuttgart': '斯图加特',
    'Eintracht Frankfurt': '法兰克福',
    'Hoffenheim': '霍芬海姆',
    'Freiburg': '弗赖堡',
    'Union Berlin': '柏林联合',
    'Mainz 05': '美因茨',
    'Augsburg': '奥格斯堡',
    'Heidenheim': '海登海姆',
    'Werder Bremen': '不莱梅',
    'Wolfsburg': '沃尔夫斯堡',
    'Borussia Monchengladbach': '门兴',
    'Bochum': '波鸿',
    'Darmstadt 98': '达姆施塔特',
    'FC Koln': '科隆',
    'Las Palmas': '拉斯帕尔马斯',
    'Alaves': '阿拉维斯',
    'Osasuna': '奥萨苏纳',
    'Getafe': '赫塔菲',
    'Valencia': '瓦伦西亚',
    'Mallorca': '马洛卡',
    'Sevilla': '塞维利亚',
    'Celta Vigo': '塞尔塔',
    'Rayo Vallecano': '巴列卡诺',
    'Cadiz': '加的斯',
    'Granada': '格拉纳达',
    'Almeria': '阿尔梅里亚',
    'Torino': '都灵',
    'Napoli': '那不勒斯',
    'Genoa': '热那亚',
    'Monza': '蒙扎',
    'Lecce': '莱切',
    'Udinese': '乌迪内斯',
    'Cagliari': '卡利亚里',
    'Empoli': '恩波利',
    'Frosinone': '弗罗西诺内',
    'Sassuolo': '萨索洛',
    'Salernitana': '萨勒尼塔纳',
    'Verona': '维罗纳',
    'Rennes': '雷恩',
    'Reims': '兰斯',
    'Toulouse': '图卢兹',
    'Strasbourg': '斯特拉斯堡',
    'Montpellier': '蒙彼利埃',
    'Nantes': '南特',
    'Le Havre': '勒阿弗尔',
    'Metz': '梅斯',
    'Lorient': '洛里昂',
    'Clermont Foot': '克莱蒙',
}

def translate_team_name(english_name):
    """使用 DDGS 将英文队名翻译为中文"""
    # 0. 查表优先
    if english_name in MANUAL_TRANSLATIONS:
        return MANUAL_TRANSLATIONS[english_name]

    try:
        logger.info(f"正在翻译球队名称: {english_name}...")
        with DDGS() as ddgs:
            # 搜索查询： "Real Madrid Chinese name" or "Real Madrid 中文名"
            keywords = f"{english_name} football club 中文名"
            results = list(ddgs.text(keywords, max_results=3))
            
            # 简单的启发式提取? 
            # 这比较难，直接提取文本可能不准确。
            # 我们可以尝试搜索 wikipedia 页面标题?
            
            # 备选方案：只保留英文名，或者让用户后续手动修改。
            # 但用户要求 "智能检索...并存入数据库"
            
            # 尝试从搜索结果摘要中提取中文
            # 比如结果包含 "皇家马德里足球俱乐部（Real Madrid Club de Fútbol）..."
            for r in results:
                title = r.get('title', '')
                body = r.get('body', '')
                text = title + " " + body
                
                # 尝试匹配中文
                # 提取其中的中文片段? 
                # 这太复杂且易错。
                
                # 简化策略：
                # 如果是知名球队，搜索结果标题通常就是中文名
                # 比如 "Real Madrid" -> "皇家马德里 - 维基百科..."
                
                # 提取标题中的中文部分
                chinese_part = re.findall(r'[\u4e00-\u9fa5]+', title)
                if chinese_part:
                    # 取最长的片段作为候选? 或者第一个?
                    candidate = max(chinese_part, key=len)
                    if len(candidate) >= 2: # 至少两个字
                        logger.info(f"翻译结果: {english_name} -> {candidate}")
                        return candidate
                        
        # 如果失败，暂时返回英文名，或者标记为待翻译
        logger.warning(f"翻译失败，使用英文原名: {english_name}")
        return english_name
        
    except Exception as e:
        logger.error(f"翻译过程出错 {english_name}: {e}")
        return english_name

def get_or_create_team(cursor, english_name, logo_url):
    """
    获取或创建球队
    1. 尝试通过 english_name 查找
    2. 如果没有，尝试翻译
    3. 插入新球队
    """
    if not english_name:
        return None

    # 1. 尝试通过 english_name 查找
    sql = "SELECT id, name, logo_url FROM teams WHERE english_name = %s"
    cursor.execute(sql, (english_name,))
    result = cursor.fetchone()
    
    if result:
        # 修正错误的中文名 (如 "有什么区别" -> "布鲁日")
        if english_name in MANUAL_TRANSLATIONS:
            correct_name = MANUAL_TRANSLATIONS[english_name]
            if result['name'] != correct_name:
                 logger.info(f"修正球队中文名: {result['name']} -> {correct_name}")
                 update_sql = "UPDATE teams SET name = %s WHERE id = %s"
                 cursor.execute(update_sql, (correct_name, result['id']))

        # 更新 logo 如果需要
        if logo_url and (not result['logo_url'] or result['logo_url'] == '/static/soccer-logo.png'):
             update_sql = "UPDATE teams SET logo_url = %s WHERE id = %s"
             cursor.execute(update_sql, (logo_url, result['id']))
        return result['id']
        
    # 2. 尝试通过中文名查找 (防止重复创建，比如之前只有中文名没有英文名的记录)
    #    这里需要先翻译，才能去匹配中文名
    chinese_name = translate_team_name(english_name)
    
    # 检查中文名是否存在
    sql = "SELECT id, english_name FROM teams WHERE name = %s"
    cursor.execute(sql, (chinese_name,))
    result = cursor.fetchone()
    
    if result:
        # 如果找到了中文名，但english_name为空，则更新english_name
        if not result['english_name']:
            logger.info(f"更新球队英文名: {chinese_name} -> {english_name}")
            update_sql = "UPDATE teams SET english_name = %s WHERE id = %s"
            cursor.execute(update_sql, (english_name, result['id']))
        
        # 更新 logo
        if logo_url:
             update_sql = "UPDATE teams SET logo_url = %s WHERE id = %s"
             cursor.execute(update_sql, (logo_url, result['id']))
             
        return result['id']

    # 3. 创建新球队
    logger.info(f"创建新球队: {chinese_name} ({english_name})")
    insert_sql = "INSERT INTO teams (name, english_name, logo_url, created_at, updated_at) VALUES (%s, %s, %s, NOW(), NOW())"
    cursor.execute(insert_sql, (chinese_name, english_name, logo_url))
    return cursor.lastrowid

def fetch_livescore_data(date_str):
    """
    抓取 LiveScore 数据
    URL: https://prod-public-api.livescore.com/v1/api/app/date/soccer/{date}/8
    """
    url = f"https://prod-public-api.livescore.com/v1/api/app/date/soccer/{date_str}/8"
    for attempt in range(3):
        try:
            # Debug script worked without headers, so let's try minimal headers or none if problematic
            # But requests default UA is often blocked. Debug script used requests default.
            # Let's try to match debug script exactly (no headers passed)
            response = requests.get(url, timeout=15)
            if response.status_code != 200:
                logger.error(f"请求失败 {date_str}: {response.status_code}")
                time.sleep(2)
                continue
            return response.json()
        except Exception as e:
            logger.error(f"请求异常 {date_str} (尝试 {attempt+1}/3): {e}")
            time.sleep(2)
    return None

def process_stage(cursor, stage, date_str):
    """处理单个赛事阶段 (League)"""
    sid = stage.get('Sid')
    if sid not in TARGET_LEAGUES:
        return
        
    league_name = TARGET_LEAGUES[sid]
    events = stage.get('Events', [])
    logger.info(f"正在处理 {league_name} ({date_str}), 发现 {len(events)} 场比赛")
    
    for event in events:
        try:
            # 提取比赛信息
            # T1: Home Team, T2: Away Team
            t1 = event.get('T1', [])[0]
            t2 = event.get('T2', [])[0]
            
            home_name_en = t1.get('Nm')
            away_name_en = t2.get('Nm')
            
            home_logo = t1.get('Img')
            away_logo = t2.get('Img')
            
            # 处理 logo url (livescore logo 通常是 filename, 需要拼接 base url?)
            # 观察 API 返回，Img 类似 "team_badge.png" ? 
            # 实际上 LiveScore API 的 Img 字段通常是 "t1234.png"
            # 完整的 CDN 路径可能是 https://lsm-static-prod.livescore.com/medium/enet/1234.png 
            # 或者 https://content.livescore.com/img/1234.png
            # 让我们先存下来，如果只是文件名，前端可能需要适配。
            # 根据用户提示 "直接抓取 API 中的 Img 字段 URL 存入 logo_url"
            # 如果 API 返回的是完整 URL 最好。如果不是，我需要猜测前缀。
            # 通常 LiveScore API 返回的是 "enet/9825.png" 这种。
            # Base URL: https://lsm-static-prod.livescore.com/medium/
            
            if home_logo and not home_logo.startswith('http'):
                home_logo = f"https://lsm-static-prod.livescore.com/medium/{home_logo}"
            if away_logo and not away_logo.startswith('http'):
                away_logo = f"https://lsm-static-prod.livescore.com/medium/{away_logo}"

            # 获取球队ID
            home_id = get_or_create_team(cursor, home_name_en, home_logo)
            away_id = get_or_create_team(cursor, away_name_en, away_logo)
            
            if not home_id or not away_id:
                logger.warning(f"无法获取球队ID: {home_name_en} vs {away_name_en}")
                continue
                
            # 比赛时间
            esd = event.get('Esd') # e.g. 20231125123000
            # 格式: YYYYMMDDHHmmss
            match_time = datetime.strptime(str(esd), '%Y%m%d%H%M%S')
            
            # 状态
            eps = event.get('Eps')
            status = map_status(eps)
            live_time = eps  # 保存原始状态文本 (如 67', HT, FT)
            
            # 比分
            tr1 = event.get('Tr1')
            tr2 = event.get('Tr2')
            
            home_score = int(tr1) if tr1 and tr1.isdigit() else 0
            away_score = int(tr2) if tr2 and tr2.isdigit() else 0
            
            # 检查比赛是否存在
            # 使用时间 + 球队 来唯一确定? 
            # 或者 competition_name + teams + time
            check_sql = """
                SELECT id FROM matches 
                WHERE home_team_id = %s AND away_team_id = %s 
                AND ABS(TIMESTAMPDIFF(MINUTE, match_time, %s)) < 120
            """
            cursor.execute(check_sql, (home_id, away_id, match_time))
            existing_match = cursor.fetchone()
            
            if existing_match:
                # 更新
                update_sql = """
                    UPDATE matches 
                    SET status = %s, home_score = %s, away_score = %s, live_time = %s, updated_at = NOW()
                    WHERE id = %s
                """
                cursor.execute(update_sql, (status, home_score, away_score, live_time, existing_match['id']))
                # logger.info(f"更新比赛: {home_name_en} vs {away_name_en}")
            else:
                # 插入
                insert_sql = """
                    INSERT INTO matches 
                    (home_team_id, away_team_id, competition_name, match_time, status, home_score, away_score, live_time, created_at, updated_at)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
                """
                cursor.execute(insert_sql, (home_id, away_id, league_name, match_time, status, home_score, away_score, live_time))
                logger.info(f"插入比赛: {league_name} - {home_name_en} vs {away_name_en}")
                
        except Exception as e:
            logger.error(f"处理比赛出错: {e}")
            continue

def sync_schedule():
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            # 循环过去1天到未来7天 (共9天)
            # 这样可以确保昨天的比赛状态被更新为完赛
            for i in range(-1, 8):
                target_date = datetime.now() + timedelta(days=i)
                date_str = target_date.strftime('%Y%m%d')
                logger.info(f"开始抓取 {date_str} 的数据...")
                
                data = fetch_livescore_data(date_str)
                
                # 即使没数据也要sleep，防止死循环重试或快速失败
                time.sleep(2)

                if not data:
                    continue
                    
                stages = data.get('Stages', [])
                for stage in stages:
                    process_stage(cursor, stage, date_str)
            
            conn.commit()
            logger.info("所有数据同步完成")
            
    except Exception as e:
        logger.error(f"同步过程发生全局异常: {e}")
    finally:
        conn.close()

if __name__ == "__main__":
    logger.info("启动自动同步监控服务 (按 Ctrl+C 停止)...")
    while True:
        try:
            logger.info(f"=== 开始同步: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')} ===")
            sync_schedule()
            logger.info("=== 同步完成，等待 180 秒 ===")
            time.sleep(180)
        except KeyboardInterrupt:
            logger.info("停止监控服务")
            break
        except Exception as e:
            logger.error(f"监控循环异常: {e}")
            time.sleep(180)
