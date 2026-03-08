import requests
import mysql.connector
from datetime import datetime, timedelta
from bs4 import BeautifulSoup
import re
import urllib3
import os

# 禁用 SSL 警告
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# 日志文件路径
LOG_FILE = "D:/project/football/scripts/crawler.log"

def write_log(message):
    timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    log_entry = f"[{timestamp}] {message}\n"
    print(log_entry.strip())
    with open(LOG_FILE, "a", encoding="utf-8") as f:
        f.write(log_entry)

import subprocess

def fetch_from_leisu():
    """从雷速体育抓取实时比分"""
    url = "http://m.leisu.com/"
    try:
        # 使用 curl.exe 获取网页内容
        cmd = ["curl.exe", "-s", "-L", "-A", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36", url]
        result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')
        
        if result.returncode != 0:
            write_log(f"[爬虫] curl 执行失败，退出码: {result.returncode}")
            return {}
        
        html = result.stdout
        if not html:
            write_log(f"[爬虫] 获取到的 HTML 为空")
            return {}
            
        soup = BeautifulSoup(html, 'html.parser')
        matches_data = {}
        
        # 寻找所有的比赛行
        match_rows = soup.find_all('li', class_='ftb-lier-base')
        for row in match_rows:
            try:
                home_elem = row.find(class_='team-home')
                away_elem = row.find(class_='team-away')
                score_elem = row.find(class_='score')
                
                if home_elem and away_elem and score_elem:
                    home_name = home_elem.get_text().strip()
                    away_name = away_elem.get_text().strip()
                    score_text = score_elem.get_text().strip()
                    
                    if '-' in score_text:
                        h_score, a_score = score_text.split('-')
                        matches_data[home_name] = (int(h_score), int(a_score), 1) # 雷速首页通常是进行中的
                        matches_data[away_name] = (int(h_score), int(a_score), 1)
            except Exception as e:
                continue

        write_log(f"[爬虫] 成功从雷速抓取到 {len(matches_data)} 条比分数据。")
        # 调试输出前 5 条数据
        debug_info = ", ".join([f"{k}:{v}" for k, v in list(matches_data.items())[:10]])
        write_log(f"[调试] 抓取数据样例: {debug_info}")
        return matches_data
    except Exception as e:
        write_log(f"[爬虫] 雷速抓取异常: {e}")
        return {}

def fetch_from_7m():
    """从 7M 体育抓取实时比分"""
    url = "http://bf.7m.com.cn/soccer_f.aspx"
    try:
        # 7M 的数据在 JavaScript 变量中，通过 curl 获取
        cmd = ["curl.exe", "-s", "-L", "-A", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36", url]
        result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')
        
        if result.returncode != 0:
            write_log(f"[爬虫] 7M curl 执行失败，退出码: {result.returncode}")
            return {}
        
        html = result.stdout
        if not html:
            write_log(f"[爬虫] 7M 获取到的 HTML 为空")
            return {}
            
        matches_data = {}
        
        # 7M 核心数据结构:
        # sDt[match_id] = [..., home_name, away_name, ...]
        # sDt2[match_id] = [status, home_score, away_score, ...]
        
        # 1. 提取 sDt (队名信息)
        # 格式示例: sDt[2654312]=["西甲","1","皇家马德里","巴塞罗那",...]
        sdt_pattern = r'sDt\[(\d+)\]\s*=\s*\[(.*?)\];'
        sdt_matches = re.findall(sdt_pattern, html)
        
        # 2. 提取 sDt2 (比分信息)
        # 格式示例: sDt2[2654312]=[1,2,1,0,0,...]
        sdt2_pattern = r'sDt2\[(\d+)\]\s*=\s*\[(.*?)\];'
        sdt2_matches = re.findall(sdt2_pattern, html)
        
        # 转换 sDt 为字典 {match_id: [home, away]}
        sdt_dict = {}
        for mid, content in sdt_matches:
            # 移除引号并分割
            parts = [p.strip('"') for p in content.split(',')]
            if len(parts) >= 4:
                # 根据 Wr 函数推断: sDt[i][2] 是主队, sDt[i][3] 是客队
                sdt_dict[mid] = (parts[2], parts[3])
        
        # 结合 sDt2 获取比分
        for mid, content in sdt2_matches:
            if mid in sdt_dict:
                parts = content.split(',')
                if len(parts) >= 3:
                    # sDt2[i][1] 是主队比分, sDt2[i][2] 是客队比分
                    try:
                        h_score = int(parts[1])
                        a_score = int(parts[2])
                        status_7m = int(parts[0])
                        home_name, away_name = sdt_dict[mid]
                        
                        # status_7m 处理: 
                        # 0: 未开, 1: 上半场, 2: 中场, 3: 下半场, 4: 完场, 5: 推迟, 6: 点球...
                        # 统一为我们的状态: 0:未开, 1:进行中, 2:完场
                        our_status = 1 # 默认进行中
                        if status_7m == 4: # 完场
                            our_status = 2
                        elif status_7m == 0:
                            our_status = 0
                            
                        matches_data[home_name] = (h_score, a_score, our_status)
                        matches_data[away_name] = (h_score, a_score, our_status)
                    except:
                        continue

        write_log(f"[爬虫] 成功从 7M 抓取到 {len(matches_data)} 条比分数据。")
        return matches_data
    except Exception as e:
        write_log(f"[爬虫] 7M 抓取异常: {e}")
        return {}

def get_live_scores():
    write_log("启动秒级实时同步引擎...")
    
    config = {
        'user': 'root',
        'password': '290390',
        'host': '127.0.0.1',
        'database': 'soccer_forum'
    }
    
    try:
        conn = mysql.connector.connect(**config)
        cursor = conn.cursor(dictionary=True)
        
        # 1. 抓取远程实时数据 (多源融合)
        leisu_data = fetch_from_leisu()
        m7_data = fetch_from_7m()
        
        # 合并数据源，雷速优先
        live_data = {**m7_data, **leisu_data}
        
        # 2. 获取数据库中正在进行的比赛 (扩大范围到前后3小时)
        now = datetime.now()
        start_time = now - timedelta(hours=3)
        end_time = now + timedelta(hours=3)
        
        cursor.execute("""
            SELECT m.id, h.name as home, a.name as away, m.home_score, m.away_score, m.status 
            FROM matches m
            JOIN teams h ON m.home_team_id = h.id
            JOIN teams a ON m.away_team_id = a.id
            WHERE m.status IN (1, 2) OR (m.match_time >= %s AND m.match_time <= %s)
        """, (start_time, end_time))
        
        target_matches = cursor.fetchall()
        write_log(f"[引擎] 数据库待监控比赛数量: {len(target_matches)}")
        
        updated_count = 0
        for match in target_matches:
            home_name = match['home']
            away_name = match['away']
            
            # 在抓取的数据中寻找匹配项 (支持模糊匹配或队名包含)
            new_score = None
            
            # 精确匹配
            if home_name in live_data:
                new_score = live_data[home_name]
            elif away_name in live_data:
                new_score = live_data[away_name]
            else:
                # 模糊匹配：如果抓取到的队名包含数据库队名，或反之
                for crawler_name, score in live_data.items():
                    if home_name in crawler_name or crawler_name in home_name:
                        new_score = score
                        break
                    if away_name in crawler_name or crawler_name in away_name:
                        new_score = score
                        break
            
            if new_score:
                new_h, new_a, crawler_status = new_score
                # 只要比分变化或状态变化就更新
                if (new_h != match['home_score'] or new_a != match['away_score'] or crawler_status != match['status']):
                    write_log(f"[数据同步] {home_name} vs {away_name}: 比分({match['home_score']}:{match['away_score']} -> {new_h}:{new_a}) 状态({match['status']} -> {crawler_status})")
                    
                    # 保护性处理: 如果数据库状态已经是 2(完场)，则不再回退
                    if match['status'] == 2 and crawler_status != 2:
                        continue
                        
                    update_sql = "UPDATE matches SET home_score = %s, away_score = %s, status = %s, updated_at = NOW() WHERE id = %s"
                    cursor.execute(update_sql, (new_h, new_a, crawler_status, match['id']))
                    updated_count += 1
            else:
                # 记录未匹配到的比赛，方便调试
                pass
                
        conn.commit()
        cursor.close()
        conn.close()
        write_log(f"[引擎] 循环结束。更新场次: {updated_count}")
    except Exception as e:
        write_log(f"实时同步引擎错误: {e}")

if __name__ == "__main__":
    get_live_scores()
