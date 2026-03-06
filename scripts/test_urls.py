import requests
import urllib3
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

urls = [
    "http://m.leisu.com/",
    "http://m.win007.com/",
    "http://m.titan24.com/",
    "https://m.sporttery.cn/score/fb_index.shtml"
]

headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

for url in urls:
    try:
        resp = requests.get(url, headers=headers, timeout=5, verify=False)
        print(f"URL: {url}, Status: {resp.status_code}, Length: {len(resp.text)}")
    except Exception as e:
        print(f"URL: {url}, Error: {e}")
