
import sys
import io

# 强制使用 UTF-8 输出，防止 Windows 环境下的 GBK 编码错误
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

import mysql.connector
import requests
from datetime import datetime, timedelta
import time
import json

# ==========================================
# 核心配置 - 已根据 application.yml 自动同步
# ==========================================
DB_CONFIG = {
    'host': '127.0.0.1',
    'user': 'root',
    'password': '290390',
    'database': 'soccer_forum',
    'charset': 'utf8mb4'
}

# 从 application.yml 获取的最新配置
API_KEY = "fec2cf7e61b4407b95b4dcc0e239c73e"
BASE_URL = "https://api.football-data.org/v4"

# 代理配置 (如果需要)
PROXIES = {
    'http': 'http://127.0.0.1:7890',
    'https': 'http://127.0.0.1:7890'
}

# 联赛代码映射 (Football-Data.org 标准)
LEAGUES = {
    'PL': '英超',
    'PD': '西甲',
    'BL1': '德甲',
    'SA': '意甲',
    'FL1': '法甲',
    'CL': '欧冠'
}

# 球队名称中英文映射
TEAM_NAME_MAP = {
    # 英超
    "Arsenal FC": "阿森纳",
    "Chelsea FC": "切尔西",
    "Liverpool FC": "利物浦",
    "Manchester City FC": "曼城",
    "Manchester United FC": "曼联",
    "Tottenham Hotspur FC": "热刺",
    "Aston Villa FC": "阿斯顿维拉",
    "Newcastle United FC": "纽卡斯尔联",
    "West Ham United FC": "西汉姆联",
    "Brighton & Hove Albion FC": "布莱顿",
    "Everton FC": "埃弗顿",
    "Wolverhampton Wanderers FC": "狼队",
    "Crystal Palace FC": "水晶宫",
    "Fulham FC": "富勒姆",
    "Brentford FC": "布伦特福德",
    "Nottingham Forest FC": "诺丁汉森林",
    "AFC Bournemouth": "伯恩茅斯",
    "Luton Town FC": "卢顿",
    "Burnley FC": "伯恩利",
    "Sheffield United FC": "谢菲尔德联",
    "Sunderland AFC": "桑德兰",
    "Sunderland": "桑德兰",
    "Leicester City FC": "莱斯特城",
    "Leicester": "莱斯特城",
    "Ipswich Town FC": "伊普斯维奇",
    "Ipswich": "伊普斯维奇",
    "Southampton FC": "南安普顿",
    "Southampton": "南安普顿",
    
    # 西甲
    "FC Barcelona": "巴塞罗那",
    "Barca": "巴塞罗那",
    "Real Madrid CF": "皇家马德里",
    "Real Madrid": "皇家马德里",
    "Atlético de Madrid": "马德里竞技",
    "Atletico": "马德里竞技",
    "Sevilla FC": "塞维利亚",
    "Sevilla": "塞维利亚",
    "Real Sociedad de Fútbol": "皇家社会",
    "Real Sociedad": "皇家社会",
    "Athletic Club": "毕尔巴鄂竞技",
    "Athletic": "毕尔巴鄂竞技",
    "Villarreal CF": "比利亚雷亚尔",
    "Villarreal": "比利亚雷亚尔",
    "Real Betis Balompié": "皇家贝蒂斯",
    "Betis": "皇家贝蒂斯",
    "Girona FC": "赫罗纳",
    "Girona": "赫罗纳",
    "Valencia CF": "瓦伦西亚",
    "Valencia": "瓦伦西亚",
    "CA Osasuna": "奥萨苏纳",
    "Osasuna": "奥萨苏纳",
    "Getafe CF": "赫塔费",
    "Getafe": "赫塔费",
    "Rayo Vallecano de Madrid": "巴列卡诺",
    "Rayo Vallecano": "巴列卡诺",
    "RC Celta de Vigo": "塞尔塔",
    "Celta": "塞尔塔",
    "RCD Mallorca": "马略卡",
    "Mallorca": "马略卡",
    "Cádiz CF": "加的斯",
    "Cadiz": "加的斯",
    "UD Almería": "阿尔梅里亚",
    "Almeria": "阿尔梅里亚",
    "Granada CF": "格拉纳达",
    "Granada": "格拉纳达",
    "Deportivo Alavés": "阿拉维斯",
    "Alaves": "阿拉维斯",
    "UD Las Palmas": "拉斯帕尔马斯",
    "Las Palmas": "拉斯帕尔马斯",
    "RCD Espanyol de Barcelona": "西班牙人",
    "Espanyol": "西班牙人",
    "Stade Brestois 29": "布雷斯特",
    "Brest": "布雷斯特",
    "Hamburger SV": "汉堡",
    "Elche CF": "埃尔切",
    "Real Oviedo": "皇家奥维耶多",
    "Como 1907": "科莫",
    "Como": "科莫",
    "Leeds United FC": "利兹联",
    "Leeds": "利兹联",
    "FC St. Pauli 1910": "圣保利",
    "St. Pauli": "圣保利",
    "Hellas Verona FC": "维罗纳",
    "Verona": "维罗纳",
    "US Sassuolo Calcio": "萨索洛",
    "Sassuolo": "萨索洛",
    "SS Lazio": "拉齐奥",
    "Lazio": "拉齐奥",
    "Genoa CFC": "热那亚",
    "Genoa": "热那亚",
    "Torino FC": "都灵",
    "Torino": "都灵",
    "SSC Napoli": "那不勒斯",
    "Napoli": "那不勒斯",
    "US Cremonese": "克雷莫纳",
    "AC Pisa 1909": "比萨",
    "Le Havre AC": "勒阿弗尔",
    "Le Havre": "勒阿弗尔",
    "OGC Nice": "尼斯",
    "Nice": "尼斯",
    "FC Lorient": "洛里昂",
    "Lorient": "洛里昂",
    "Lille OSC": "里尔",
    "Lille": "里尔",
    "Olympique Lyonnais": "里昂",
    "Lyon": "里昂",
    "RC Lens": "朗斯",
    "Lens": "朗斯",
    "AS Monaco FC": "摩纳哥",
    "Monaco": "摩纳哥",
    "FC Metz": "梅斯",
    "Metz": "梅斯",
    "Stade Rennais FC 1901": "雷恩",
    "Rennes": "雷恩",
    "Club Brugge KV": "布鲁日",
    "Qarabağ Ağdam FK": "卡拉巴赫",
    "PAE Olympiakos SFP": "奥林匹亚科斯",
    "FK Bodø/Glimt": "博多格林特",
    "Sport Lisboa e Benfica": "本菲卡",
    "Galatasaray SK": "加拉塔萨雷",
    "Angers SCO": "安热",
    "Angers": "安热",
    "Paris FC": "巴黎FC",
    "AJ Auxerre": "欧塞尔",
    "Auxerre": "欧塞尔",
    "Udinese Calcio": "乌迪内斯",
    "Udinese": "乌迪内斯",
    "Cagliari Calcio": "卡利亚里",
    "Cagliari": "卡利亚里",
    "US Lecce": "莱切",
    "Lecce": "莱切",
    "Empoli FC": "恩波利",
    "Empoli": "恩波利",
    "Frosinone Calcio": "弗罗西诺内",
    "US Salernitana 1919": "萨勒尼塔纳",
    "Monza": "蒙扎",
    "AC Monza": "蒙扎",
    "Bologna FC 1909": "博洛尼亚",
    "Bologna": "博洛尼亚",
    "ACF Fiorentina": "佛罗伦萨",
    "Fiorentina": "佛罗伦萨",
    "Racing Club de Lens": "朗斯",
    "RC Strasbourg Alsace": "斯特拉斯堡",
    "Strasbourg": "斯特拉斯堡",
    "Toulouse FC": "图卢兹",
    "Toulouse": "图卢兹",
    "FC Nantes": "南特",
    "Nantes": "南特",
    "Olympique de Marseille": "马赛",
    "Marseille": "马赛",
    "Montpellier HSC": "蒙彼利埃",
    "Montpellier": "蒙彼利埃",
    "Stade de Reims": "兰斯",
    "Reims": "兰斯",
    "Clermont Foot 63": "克莱蒙",
    "Clermont": "克莱蒙",
    "FC Internazionale Milano": "国际米兰",
    "Inter": "国际米兰",
    "Inter Milan": "国际米兰",
    
    # 德甲
    "FC Bayern München": "拜仁慕尼黑",
    "Bayern": "拜仁慕尼黑",
    "Bayer 04 Leverkusen": "勒沃库森",
    "Leverkusen": "勒沃库森",
    "Borussia Dortmund": "多特蒙德",
    "Dortmund": "多特蒙德",
    "RB Leipzig": "莱比锡红牛",
    "Leipzig": "莱比锡红牛",
    "VfB Stuttgart": "斯图加特",
    "Stuttgart": "斯图加特",
    "Eintracht Frankfurt": "法兰克福",
    "Frankfurt": "法兰克福",
    "VfL Wolfsburg": "沃尔夫斯堡",
    "Wolfsburg": "沃尔夫斯堡",
    "SC Freiburg": "弗赖堡",
    "Freiburg": "弗赖堡",
    "1. FC Union Berlin": "柏林联合",
    "Union Berlin": "柏林联合",
    "TSG 1899 Hoffenheim": "霍芬海姆",
    "Hoffenheim": "霍芬海姆",
    "Borussia Mönchengladbach": "门兴格拉德巴赫",
    "M'gladbach": "门兴格拉德巴赫",
    "SV Werder Bremen": "云达不莱梅",
    "Werder Bremen": "云达不莱梅",
    "1. FSV Mainz 05": "美因茨",
    "Mainz": "美因茨",
    "FC Augsburg": "奥格斯堡",
    "Augsburg": "奥格斯堡",
    "VfL Bochum 1848": "波鸿",
    "Bochum": "波鸿",
    "1. FC Heidenheim 1846": "海登海姆",
    "Heidenheim": "海登海姆",
    "SV Darmstadt 98": "达姆施塔特",
    "Darmstadt": "达姆施塔特",
    "1. FC Köln": "科隆",
    "FC Koln": "科隆",
    "Holstein Kiel": "荷尔斯泰因基尔",
    "St. Pauli": "圣保利",
    
    # 意甲
    "AC Milan": "AC米兰",
    "Inter Milan": "国际米兰",
    "Juventus FC": "尤文图斯",
    "SSC Napoli": "那不勒斯",
    "Atalanta BC": "亚特兰大",
    "Atalanta": "亚特兰大",
    "Dortmund": "多特蒙德",
    "Borussia Dortmund": "多特蒙德",
    "AS Roma": "罗马",
    "SS Lazio": "拉齐奥",
    "ACF Fiorentina": "佛罗伦萨",
    "Atalanta BC": "亚特兰大",
    "Bologna FC 1909": "博洛尼亚",
    "Torino FC": "都灵",
    "AC Monza": "蒙扎",
    "Udinese Calcio": "乌迪内斯",
    "US Sassuolo Calcio": "萨索洛",
    "Genoa CFC": "热那亚",
    "US Lecce": "莱切",
    "Empoli FC": "恩波利",
    "Hellas Verona FC": "维罗纳",
    "Cagliari Calcio": "卡利亚里",
    "Frosinone Calcio": "弗罗西诺内",
    "US Salernitana 1919": "萨勒尼塔纳",
    "Parma Calcio 1913": "帕尔马",
    "Levante UD": "莱万特",
    
    # 法甲
    "Paris Saint-Germain FC": "巴黎圣日耳曼",
    "Olympique de Marseille": "马赛",
    "Olympique Lyonnais": "里昂",
    "AS Monaco FC": "摩纳哥",
    "Lille OSC": "里尔",
    "Stade Rennais FC 1901": "雷恩",
    "OGC Nice": "尼斯",
    "RC Lens": "朗斯",
    "Stade de Reims": "兰斯",
    "Montpellier HSC": "蒙彼利埃",
    "RC Strasbourg Alsace": "斯特拉斯堡",
    "FC Nantes": "南特",
    "Toulouse FC": "图卢兹",
    "FC Lorient": "洛里昂",
    "Clermont Foot 63": "克莱蒙",
    "Stade Brestois 29": "布雷斯特",
    "Le Havre AC": "勒阿弗尔",
    "FC Metz": "梅斯",
    
    # 欧冠/其他常见
    "Sporting Clube de Portugal": "葡萄牙体育",
    "SL Benfica": "本菲卡",
    "FC Porto": "波尔图",
    "Feyenoord Rotterdam": "费耶诺德",
    "PSV Eindhoven": "埃因霍温",
    "AFC Ajax": "阿贾克斯",
    "Celtic FC": "凯尔特人",
    "Rangers FC": "流浪者",
    "Galatasaray SK": "加拉塔萨雷",
    "Fenerbahçe SK": "费内巴切",
    "Beşiktaş JK": "贝西克塔斯",
    "FC Shakhtar Donetsk": "顿涅茨克矿工",
    "FC Red Bull Salzburg": "萨尔茨堡红牛",
    "Club Atlético de Madrid": "马德里竞技",
    "Club Brugge KV": "布鲁日",
    "Qarabağ Ağdam FK": "卡拉巴赫",
    "PAE Olympiakos SFP": "奥林匹亚科斯",
    "FC Internazionale Milano": "国际米兰",
    "FK Bodø/Glimt": "博多格林特",
    "Sport Lisboa e Benfica": "本菲卡",
    "Racing Club de Lens": "朗斯"
}

def get_db_connection():
    return mysql.connector.connect(**DB_CONFIG)

def fetch_matches_from_api(days_back=0, days_ahead=7):
    """
    从 Football-Data.org API 获取赛程
    """
    # 强制只查询五大联赛和欧冠
    headers = {'X-Auth-Token': API_KEY}
    all_matches = []
    
    # 获取指定范围内的赛程
    date_from = (datetime.now() - timedelta(days=days_back)).strftime("%Y-%m-%d")
    date_to = (datetime.now() + timedelta(days=days_ahead)).strftime("%Y-%m-%d")
    
    print(f"正在按联赛逐个获取赛程 (五大联赛+欧冠): {date_from} 到 {date_to}...")
    
    for league_code, league_name in LEAGUES.items():
        league_url = f"{BASE_URL}/competitions/{league_code}/matches?dateFrom={date_from}&dateTo={date_to}"
        print(f"正在获取 {league_name} 赛程...")
        try:
            # 尝试不使用代理，如果失败再试代理
            try:
                res = requests.get(league_url, headers=headers, timeout=10)
            except:
                res = requests.get(league_url, headers=headers, proxies=PROXIES, timeout=10)
                
            if res.status_code == 200:
                matches = res.json().get('matches', [])
                all_matches.extend(matches)
                print(f"成功获取 {len(matches)} 场比赛")
            else:
                print(f"获取 {league_name} 失败: {res.status_code}")
            
            # 免费版频率限制: 每分钟10次请求，所以每次请求后等待 6.5 秒确保安全
            time.sleep(6.5) 
        except Exception as e:
            print(f"获取 {league_code} 异常: {e}")
            
    return all_matches

def ensure_team(cursor, api_team_data):
    """
    确保球队存在并更新头像
    """
    en_name = api_team_data['name']
    short_name = api_team_data.get('shortName', en_name)
    cn_name = TEAM_NAME_MAP.get(en_name, TEAM_NAME_MAP.get(short_name, short_name))
    logo_url = api_team_data.get('crest')
    
    # 扩大查询范围，同时按中英文查询
    cursor.execute("SELECT id, name, logo_url FROM teams WHERE name = %s OR name = %s OR name = %s", (cn_name, en_name, short_name))
    teams = cursor.fetchall()
    
    # 调试日志
    if en_name in ["Atalanta BC", "Atalanta", "Borussia Dortmund", "Dortmund"]:
        print(f"  [调试] 处理球队 {en_name}, 匹配到数据库记录数: {len(teams)}")
        for t in teams:
            print(f"    - ID: {t['id']}, Name: {t['name']}")
    
    if teams:
        # 如果发现重复项（一个中文名，一个英文名），合并它们
        if len(teams) > 1:
            print(f"  [发现重复球队]: 合并 {en_name} 到 {cn_name}")
            target_id = None
            
            # 优先选择已有的中文名作为目标
            for t in teams:
                if t['name'] == cn_name: 
                    target_id = t['id']
                    break
            
            # 如果没找到中文名记录，把第一个记录当做目标
            if not target_id:
                target_id = teams[0]['id']
                print(f"  [强制同步名称]: {teams[0]['name']} -> {cn_name}")
                cursor.execute("UPDATE teams SET name = %s WHERE id = %s", (cn_name, target_id))
            
            # 收集所有需要删除的非目标 ID
            to_delete_ids = [t['id'] for t in teams if t['id'] != target_id]
            
            for to_delete_id in to_delete_ids:
                # 更新所有关联的比赛
                cursor.execute("UPDATE matches SET home_team_id = %s WHERE home_team_id = %s", (target_id, to_delete_id))
                cursor.execute("UPDATE matches SET away_team_id = %s WHERE away_team_id = %s", (target_id, to_delete_id))
                # 删除重复球队
                cursor.execute("DELETE FROM teams WHERE id = %s", (to_delete_id,))
            
            team_id = target_id
            current_logo = next((t['logo_url'] for t in teams if t['id'] == target_id), None)
        else:
            team_id = teams[0]['id']
            current_logo = teams[0]['logo_url']
            current_name = teams[0]['name']
            
            # 强制同步名称
            if current_name != cn_name:
                print(f"  [强制同步名称]: {current_name} -> {cn_name}")
                cursor.execute("UPDATE teams SET name = %s WHERE id = %s", (cn_name, team_id))
            
        # 更新头像
        # 优化：如果是维基百科的 SVG 链接，尝试转换为更稳定的 PNG 缩略图链接
        if logo_url and 'wikipedia' in logo_url and logo_url.endswith('.svg'):
            # 尝试多种转换方式
            if 'thumb' not in logo_url:
                # 原始链接: https://upload.wikimedia.org/wikipedia/en/f/f4/Atletico_Madrid_2017_logo.svg
                # 转换后: https://upload.wikimedia.org/wikipedia/en/thumb/f/f4/Atletico_Madrid_2017_logo.svg/200px-Atletico_Madrid_2017_logo.svg.png
                parts = logo_url.split('/')
                filename = parts[-1]
                logo_url = logo_url.replace(f'/{filename}', f'/thumb/{parts[-4]}/{parts[-3]}/{parts[-2]}/{parts[-1]}/200px-{filename}.png')
            
        if logo_url and (not current_logo or 'static/' in current_logo or 'localhost' in current_logo or '.svg' in current_logo):
            print(f"  [更新球队头像]: {cn_name} -> {logo_url}")
            cursor.execute("UPDATE teams SET logo_url = %s WHERE name = %s", (logo_url, cn_name))
            
        return team_id
    else:
        print(f"  [新增球队]: {cn_name} (Logo: {logo_url})")
        cursor.execute("INSERT INTO teams (name, logo_url) VALUES (%s, %s)", (cn_name, logo_url))
        return cursor.lastrowid

def sync_schedule(days_back=0, days_ahead=3):
    """
    主同步逻辑
    """
    # 彻底清理数据库中的存量非目标数据
    print(f"执行深度数据库清理...")
    
    # 获取连接
    conn = get_db_connection()
    # 使用 buffered=True 解决 Unread result found 问题
    cursor = conn.cursor(dictionary=True, buffered=True)
    
    try:
        # ... (keep existing cleanup logic) ...
        target_leagues = list(LEAGUES.values())
        placeholders = ', '.join(['%s'] * len(target_leagues))
        
        # 1. 删除所有不在目标中文列表中的比赛
        cursor.execute(f"DELETE FROM matches WHERE competition_name NOT IN ({placeholders})", tuple(target_leagues))
        print(f"  已清理非目标赛事比赛: {cursor.rowcount} 条")
        
        # 2. 针对存量的英文赛事名进行针对性删除
        cursor.execute("DELETE FROM matches WHERE competition_name IN ('Championship', 'Campeonato Brasileiro Série A', 'Copa Libertadores', 'Friendlies')")
        print(f"  已清理英文残余比赛: {cursor.rowcount} 条")
        
        conn.commit()

        # 获取指定范围内的比赛
        api_matches = fetch_matches_from_api(days_back=days_back, days_ahead=days_ahead)
        if not api_matches:
            print("未获取到数据。")
            return
        
        count = 0
        # 处理每一场比赛
        for match in api_matches:
            try:
                # 调试日志
                try:
                    print(f"正在处理比赛: {match['homeTeam']['name']} vs {match['awayTeam']['name']}")
                except UnicodeEncodeError:
                    print(f"正在处理比赛: {match['homeTeam'].get('shortName', 'Unknown')} vs {match['awayTeam'].get('shortName', 'Unknown')}")
                
                comp_name = LEAGUES.get(match['competition']['code'], match['competition']['name'])
                match_time_utc = match['utcDate'].replace('Z', '')
                match_time = datetime.strptime(match_time_utc, "%Y-%m-%dT%H:%M:%S") + timedelta(hours=8)
                
                home_team_id = ensure_team(cursor, match['homeTeam'])
                away_team_id = ensure_team(cursor, match['awayTeam'])
                
                # 状态映射: 0:未开始, 1:进行中, 2:已结束, 3:已推迟
                api_status = match.get('status')
                status_map = {
                    'TIMED': 0, 'SCHEDULED': 0, 
                    'IN_PLAY': 1, 'LIVE': 1, 'PAUSED': 1, 
                    'FINISHED': 2, 'AWARDED': 2,
                    'POSTPONED': 3, 'CANCELLED': 3, 'SUSPENDED': 3
                }
                status = status_map.get(api_status, 0)
                
                # 智能状态补偿: 
                # 规则1: 只要过了开始时间 且在 135 分钟内，且 API 说是 TIMED，强制设为进行中 (status=1)
                # 规则2: 只要过了开始时间 且超过 135 分钟，且 API 还没说是 FINISHED，强制设为已结束 (status=2)
                now = datetime.now()
                if status == 0: # 原本是即将开始
                    if now >= match_time:
                        if now <= (match_time + timedelta(minutes=135)):
                            print(f"  [智能补偿] 比赛 {match['homeTeam'].get('name')} 已经开始，强制设为进行中")
                            status = 1
                        else:
                            print(f"  [智能补偿] 比赛 {match['homeTeam'].get('name')} 已经结束(超时补偿)，强制设为已结束")
                            status = 2
                elif status == 1: # 原本是进行中
                    if now > (match_time + timedelta(minutes=135)):
                        print(f"  [智能补偿] 比赛 {match['homeTeam'].get('name')} 进行时间过长，强制设为已结束")
                        status = 2
                
                # 获取比分
                score_data = match.get('score', {})
                full_time = score_data.get('fullTime', {})
                half_time = score_data.get('halfTime', {})
                regular_time = score_data.get('regularTime', {}) # 增加常规时间校验
                
                home_score = full_time.get('home')
                away_score = full_time.get('away')

                # 多级比分抓取策略
                if home_score is None or away_score is None:
                    # 尝试常规时间
                    if regular_time.get('home') is not None:
                        home_score = regular_time.get('home')
                        away_score = regular_time.get('away')
                        print(f"  [调试] 从 regularTime 获取比分: {home_score}:{away_score}")
                    # 尝试半场时间
                    elif half_time.get('home') is not None:
                        home_score = half_time.get('home')
                        away_score = half_time.get('away')
                        print(f"  [调试] 从 halfTime 获取比分: {home_score}:{away_score}")

                # 只有当 API 明确给出了非空比分，或者比赛已结束且我们确实没拿到比分时，才进行赋值
                # 如果数据库里已经有比分了，且 API 返回 None，我们选择保持现状（不覆盖）
                # 逻辑: 只有当拿到的比分不是 None 时才更新，否则保留数据库原值
                if status in [1, 2]:
                    # 检查数据库当前值
                    cursor.execute("SELECT home_score, away_score FROM matches WHERE home_team_id = %s AND away_team_id = %s AND ABS(TIMESTAMPDIFF(HOUR, match_time, %s)) < 12", (home_team_id, away_team_id, match_time))
                    current = cursor.fetchone()
                    
                    if current:
                        # 如果 API 给的比分是 None，但数据库已经有值了，我们保持现状
                        if home_score is None and current['home_score'] is not None:
                            home_score = current['home_score']
                        if away_score is None and current['away_score'] is not None:
                            away_score = current['away_score']
                    
                    # 兜底：如果最终还是 None，设为 0
                    if home_score is None: home_score = 0
                    if away_score is None: away_score = 0
                
                # 查重逻辑增强：不仅检查相同时间，还检查相同对阵的 ID，防止异步更新时产生新记录
                cursor.execute("""
                    SELECT id FROM matches 
                    WHERE home_team_id = %s AND away_team_id = %s 
                    AND (
                        ABS(TIMESTAMPDIFF(HOUR, match_time, %s)) < 12 
                        OR (status = 0 AND ABS(TIMESTAMPDIFF(DAY, match_time, %s)) < 1)
                    )
                """, (home_team_id, away_team_id, match_time, match_time))
                
                existing = cursor.fetchone()
                
                if existing:
                    cursor.execute("""
                        UPDATE matches SET competition_name = %s, status = %s, home_score = %s, away_score = %s, updated_at = NOW()
                        WHERE id = %s
                    """, (comp_name, status, home_score, away_score, existing['id']))
                else:
                    cursor.execute("""
                        INSERT INTO matches (home_team_id, away_team_id, competition_name, match_time, status, home_score, away_score)
                        VALUES (%s, %s, %s, %s, %s, %s, %s)
                    """, (home_team_id, away_team_id, comp_name, match_time, status, home_score, away_score))
                count += 1
            except Exception as e:
                print(f"跳过比赛: {e}")
                continue
                
        conn.commit()
        print(f"同步完成，处理了 {count} 场比赛。")
        
    finally:
        cursor.close()
        conn.close()

if __name__ == "__main__":
    # 同步过去4天到未来3天的赛程，确保头像更新
    sync_schedule(days_back=4, days_ahead=3)
