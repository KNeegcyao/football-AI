import requests
import json
import mysql.connector
from datetime import datetime, timedelta
import time
import sys
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

DB_CONFIG = {
    'host': '127.0.0.1',
    'user': 'root',
    'password': '290390',
    'database': 'soccer_forum',
    'charset': 'utf8mb4'
}

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "Accept": "*/*",
    "Origin": "https://www.sofascore.com",
    "Referer": "https://www.sofascore.com/"
}

# Teams mapped in fetch_schedule.py (English -> Chinese)
TEAM_NAME_MAP = {
    "Arsenal FC": "阿森纳", "Chelsea FC": "切尔西", "Liverpool FC": "利物浦",
    "Manchester City FC": "曼城", "Manchester United FC": "曼联", "Tottenham Hotspur FC": "热刺",
    "Aston Villa FC": "阿斯顿维拉", "Newcastle United FC": "纽卡斯尔联", "West Ham United FC": "西汉姆联",
    "Brighton & Hove Albion FC": "布莱顿", "Everton FC": "埃弗顿", "Wolverhampton Wanderers FC": "狼队",
    "Crystal Palace FC": "水晶宫", "Fulham FC": "富勒姆", "Brentford FC": "布伦特福德",
    "Nottingham Forest FC": "诺丁汉森林", "AFC Bournemouth": "伯恩茅斯", "Luton Town FC": "卢顿",
    "Burnley FC": "伯恩利", "Sheffield United FC": "谢菲尔德联", "Sunderland AFC": "桑德兰",
    "Leicester City FC": "莱斯特城", "Ipswich Town FC": "伊普斯维奇", "Southampton FC": "南安普顿",
    "FC Barcelona": "巴塞罗那", "Real Madrid CF": "皇家马德里", "Atlético de Madrid": "马德里竞技",
    "Sevilla FC": "塞维利亚", "Real Sociedad de Fútbol": "皇家社会", "Athletic Club": "毕尔巴鄂竞技",
    "Villarreal CF": "比利亚雷亚尔", "Real Betis Balompié": "皇家贝蒂斯", "Girona FC": "赫罗纳",
    "Valencia CF": "瓦伦西亚", "CA Osasuna": "奥萨苏纳", "Getafe CF": "赫塔费",
    "Rayo Vallecano de Madrid": "巴列卡诺", "RC Celta de Vigo": "塞尔塔", "RCD Mallorca": "马略卡",
    "Cádiz CF": "加的斯", "UD Almería": "阿尔梅里亚", "Granada CF": "格拉纳达",
    "Deportivo Alavés": "阿拉维斯", "UD Las Palmas": "拉斯帕尔马斯", "RCD Espanyol de Barcelona": "西班牙人",
    "Stade Brestois 29": "布雷斯特", "Hamburger SV": "汉堡", "Elche CF": "埃尔切",
    "Real Oviedo": "皇家奥维耶多", "Como 1907": "科莫", "Leeds United FC": "利兹联",
    "FC St. Pauli 1910": "圣保利", "Hellas Verona FC": "维罗纳", "US Sassuolo Calcio": "萨索洛",
    "SS Lazio": "拉齐奥", "Genoa CFC": "热那亚", "Torino FC": "都灵", "SSC Napoli": "那不勒斯",
    "US Cremonese": "克雷莫纳", "AC Pisa 1909": "比萨", "Le Havre AC": "勒阿弗尔",
    "OGC Nice": "尼斯", "FC Lorient": "洛里昂", "Lille OSC": "里尔", "Olympique Lyonnais": "里昂",
    "RC Lens": "朗斯", "AS Monaco FC": "摩纳哥", "FC Metz": "梅斯", "Stade Rennais FC 1901": "雷恩",
    "Club Brugge KV": "布鲁日", "Qarabağ Ağdam FK": "卡拉巴赫", "PAE Olympiakos SFP": "奥林匹亚科斯",
    "FK Bodø/Glimt": "博多格林特", "Sport Lisboa e Benfica": "本菲卡", "Galatasaray SK": "加拉塔萨雷",
    "Angers SCO": "安热", "Paris FC": "巴黎FC", "AJ Auxerre": "欧塞尔", "Udinese Calcio": "乌迪内斯",
    "Cagliari Calcio": "卡利亚里", "US Lecce": "莱切", "Empoli FC": "恩波利", "Frosinone Calcio": "弗罗西诺内",
    "US Salernitana 1919": "萨勒尼塔纳", "AC Monza": "蒙扎", "Bologna FC 1909": "博洛尼亚",
    "ACF Fiorentina": "佛罗伦萨", "Racing Club de Lens": "朗斯", "RC Strasbourg Alsace": "斯特拉斯堡",
    "Toulouse FC": "图卢兹", "FC Nantes": "南特", "Olympique de Marseille": "马赛",
    "Montpellier HSC": "蒙彼利埃", "FC Bayern München": "拜仁慕尼黑", "Borussia Dortmund": "多特蒙德",
    "RB Leipzig": "莱比锡", "Bayer 04 Leverkusen": "勒沃库森", "VfB Stuttgart": "斯图加特",
    "Eintracht Frankfurt": "法兰克福", "VfL Wolfsburg": "沃尔夫斯堡", "SC Freiburg": "弗赖堡",
    "TSG 1899 Hoffenheim": "霍芬海姆", "SV Werder Bremen": "沃尔夫斯堡", "1. FSV Mainz 05": "不来梅",
    "Borussia Mönchengladbach": "门兴格拉德巴赫", "1. FC Köln": "科隆", "VfL Bochum 1848": "波鸿",
    "FC Augsburg": "奥格斯堡", "1. FC Heidenheim 1899": "海登海姆", "SV Darmstadt 98": "达姆施塔特",
    "Juventus FC": "尤文图斯", "AC Milan": "AC米兰", "FC Internazionale Milano": "国际米兰",
    "AS Roma": "罗马", "Atalanta BC": "亚特兰大", "Paris Saint-Germain FC": "巴黎圣日耳曼"
}

# Reverse mapping for faster lookup: Chinese -> English
CH_TO_EN = {v: k for k, v in TEAM_NAME_MAP.items()}

def log(msg):
    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] {msg}")

def get_sofascore_schedule(date_str):
    url = f"https://api.sofascore.com/api/v1/sport/football/scheduled-events/{date_str}"
    try:
        r = requests.get(url, headers=HEADERS, timeout=10)
        if r.status_code == 200:
            return r.json().get('events', [])
    except Exception as e:
        log(f"Error fetching schedule: {e}")
    return []

def get_sofascore_stats(event_id):
    url = f"https://api.sofascore.com/api/v1/event/{event_id}/statistics"
    try:
        r = requests.get(url, headers=HEADERS, timeout=10)
        if r.status_code == 200:
            return r.json().get('statistics', [])
    except:
        pass
    return []

def get_sofascore_incidents(event_id):
    url = f"https://api.sofascore.com/api/v1/event/{event_id}/incidents"
    try:
        r = requests.get(url, headers=HEADERS, timeout=10)
        if r.status_code == 200:
            return r.json().get('incidents', [])
    except:
        pass
    return []

def parse_stats(raw_stats):
    result = {
        "homePossession": 50, "awayPossession": 50,
        "homeShots": 0, "awayShots": 0,
        "homeXG": 0.0, "awayXG": 0.0,
        "homeTackles": 0, "awayTackles": 0,
        "homePassSuccess": 0, "awayPassSuccess": 0
    }
    if not raw_stats: return result
    
    groups = raw_stats[0].get('groups', [])
    for group in groups:
        for item in group.get('statisticsItems', []):
            name = item.get('name')
            home = item.get('home')
            away = item.get('away')
            
            try:
                home_val = float(str(home).replace('%', '')) if home else 0
                away_val = float(str(away).replace('%', '')) if away else 0
                
                if name == "Ball possession":
                    result["homePossession"] = int(home_val)
                    result["awayPossession"] = int(away_val)
                elif name == "Total shots":
                    result["homeShots"] = int(home_val)
                    result["awayShots"] = int(away_val)
                elif name == "Expected goals":
                    result["homeXG"] = home_val
                    result["awayXG"] = away_val
                elif name == "Tackles":
                    result["homeTackles"] = int(home_val)
                    result["awayTackles"] = int(away_val)
                elif name == "Passes accurate":
                    pass
            except: pass
    return result

def parse_incidents(raw_incidents):
    events = []
    if not raw_incidents: return events
    
    for inc in raw_incidents:
        inc_type = inc.get('incidentType')
        if inc_type in ['goal', 'card']:
            if inc_type == 'card' and inc.get('incidentClass') != 'red': continue
            team = "home" if inc.get('isHome') else "away"
            player = inc.get('player', {}).get('name', 'Unknown')
            minute = inc.get('time', 0)
            events.append({
                "minute": minute,
                "playerName": player,
                "teamType": team,
                "type": "goal" if inc_type == 'goal' else "red_card"
            })
    return sorted(events, key=lambda x: x['minute'])

def match_teams(db_name, sofa_name):
    if not db_name or not sofa_name: return False
    
    s = sofa_name.lower().replace("fc", "").replace("cf", "").replace("city", "").replace("united", "").strip()
    
    # Try mapping
    en_name = CH_TO_EN.get(db_name, db_name).lower().replace("fc", "").replace("cf", "").replace("city", "").replace("united", "").strip()
    
    if len(en_name) > 3 and en_name in s: return True
    if len(s) > 3 and s in en_name: return True
    
    # special cases
    if db_name == "国际米兰" and "inter" in s: return True
    if db_name == "巴黎圣日耳曼" and "paris" in s: return True
    if db_name == "尤文图斯" and "juventus" in s: return True
    if db_name == "AC米兰" and "milan" in s: return True
    
    return False

def sync_match_stats():
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor(dictionary=True)
    
    cursor.execute("""
        SELECT m.id, m.match_time, h.name as home, a.name as away
        FROM matches m
        JOIN teams h ON m.home_team_id = h.id
        JOIN teams a ON m.away_team_id = a.id
        WHERE m.status = 2 AND (m.stats IS NULL OR m.events IS NULL OR JSON_LENGTH(m.stats) = 0)
        ORDER BY m.match_time DESC LIMIT 20
    """)
    target_matches = cursor.fetchall()
    log(f"Found {len(target_matches)} finished matches needing stats.")
    
    cache_schedules = {}
    
    for m in target_matches:
        date_str = m['match_time'].strftime('%Y-%m-%d')
        if date_str not in cache_schedules:
            log(f"Fetching SofaScore schedule for {date_str}")
            cache_schedules[date_str] = get_sofascore_schedule(date_str)
            time.sleep(1)
            
        sofa_events = cache_schedules[date_str]
        
        matched_sofa_id = None
        for se in sofa_events:
            s_home = se.get('homeTeam', {}).get('name', '')
            s_away = se.get('awayTeam', {}).get('name', '')
            if match_teams(m['home'], s_home) or match_teams(m['away'], s_away):
                matched_sofa_id = se.get('id')
                log(f"Matched DB {m['home']} vs {m['away']} to SofaScore {s_home} vs {s_away} (ID: {matched_sofa_id})")
                break
                
        if matched_sofa_id:
            raw_stats = get_sofascore_stats(matched_sofa_id)
            raw_incidents = get_sofascore_incidents(matched_sofa_id)
            
            stats = parse_stats(raw_stats)
            events = parse_incidents(raw_incidents)
            
            cursor.execute("""
                UPDATE matches SET stats = %s, events = %s WHERE id = %s
            """, (json.dumps(stats), json.dumps(events), m['id']))
            conn.commit()
            log(f"Updated stats for match {m['id']}")
            time.sleep(1)
        else:
            log(f"Could not find SofaScore match for {m['home']} vs {m['away']} on {date_str}")
            # Instead of keeping them null forever and looping, set them to "{}" and "[]"
            cursor.execute("""
                UPDATE matches SET stats = %s, events = %s WHERE id = %s
            """, ('{}', '[]', m['id']))
            conn.commit()

    cursor.close()
    conn.close()
    log("Sync completed.")

if __name__ == "__main__":
    sync_match_stats()
