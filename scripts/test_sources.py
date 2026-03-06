import subprocess
import re
from bs4 import BeautifulSoup

def test_url(name, url):
    print(f"\n--- Testing {name}: {url} ---")
    cmd = ["curl.exe", "-s", "-L", "-A", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36", url]
    result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')
    html = result.stdout
    
    if not html:
        print("Error: Empty response")
        return
        
    soup = BeautifulSoup(html, 'html.parser')
    
    # 查找可能的比分和队名
    # 模式1: 正则匹配
    matches = re.findall(r'([^\d\s\-]{2,10})\s*(\d+)\s*-\s*(\d+)\s*([^\d\s\-]{2,10})', html)
    if matches:
        print(f"Found {len(matches)} matches by regex:")
        for m in matches[:5]:
            print(f"  {m[0]} {m[1]}-{m[2]} {m[3]}")
    else:
        print("No matches found by regex")
        
    # 模式2: 队名包含 vs
    vs_matches = re.findall(r'([^\d\s\-]{2,10})\s*vs\s*([^\d\s\-]{2,10})', html, re.I)
    if vs_matches:
        print(f"Found {len(vs_matches)} VS matches:")
        for m in vs_matches[:5]:
            print(f"  {m[0]} vs {m[1]}")
            
    # 模式3: 打印一些文本摘要
    text = soup.get_text()
    cleaned_text = re.sub(r'\s+', ' ', text).strip()
    print(f"Text preview: {cleaned_text[:200]}...")

test_url("Sporttery", "https://m.sporttery.cn/score/fb_index.shtml")
test_url("Livescore", "https://www.livescore.com/zh/zuqiu/")
test_url("7M BF", "http://bf.7m.com.cn/")
test_url("Win007 Mobile", "http://m.win007.com/")
