import requests
from bs4 import BeautifulSoup
import random
import os
import time
import logging
import hashlib
from datetime import datetime

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('spider.log', encoding='utf-8')
    ]
)
logger = logging.getLogger(__name__)

# 配置参数
BASE_URL = "https://news.zhibo8.com/zuqiu/more.htm"
API_URL = "http://localhost:8080/api/news"
# 源码目录 (用于持久化)
SRC_UPLOAD_DIR = r"d:\project\football\soccer-forum-parent\soccer-forum-service\src\main\resources\static\uploads\news"
# 运行目录 (用于立即生效)
TARGET_UPLOAD_DIR = r"d:\project\football\soccer-forum-parent\soccer-forum-service\target\classes\static\uploads\news"
RELATIVE_UPLOAD_PATH = "/uploads/news/"
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
}

# 确保目录存在
for d in [SRC_UPLOAD_DIR, TARGET_UPLOAD_DIR]:
    if not os.path.exists(d):
        os.makedirs(d)

def get_md5(text):
    return hashlib.md5(text.encode('utf-8')).hexdigest()

def get_with_retry(url, headers=None, params=None, max_retries=3, timeout=10):
    """带重试机制的 GET 请求"""
    for i in range(max_retries):
        try:
            response = requests.get(url, headers=headers or HEADERS, params=params, timeout=timeout)
            if response.status_code == 200:
                return response
            elif response.status_code == 403:
                logger.warning(f"请求被拒绝(403): {url}, 尝试第 {i+1} 次重试...")
            else:
                logger.warning(f"请求失败(状态码 {response.status_code}): {url}, 尝试第 {i+1} 次重试...")
        except Exception as e:
            logger.error(f"请求发生异常: {url}, 错误: {str(e)}, 尝试第 {i+1} 次重试...")
        
        # 随机等待 2-5 秒后重试
        time.sleep(random.uniform(2, 5))
    return None

def download_image(img_url):
    """下载图片并双向保存到源码和运行目录"""
    if not img_url:
        return None
    try:
        # 提取文件名
        url_path = img_url.split('?')[0]
        ext = os.path.splitext(url_path)[-1].lower()
        if ext not in ['.jpg', '.jpeg', '.png', '.gif', '.webp']:
            ext = '.jpg'
            
        filename = f"{get_md5(img_url)}{ext}"
        relative_path = RELATIVE_UPLOAD_PATH + filename
        
        content = None
        
        # 检查是否已存在于任一目录
        for base_dir in [SRC_UPLOAD_DIR, TARGET_UPLOAD_DIR]:
            filepath = os.path.join(base_dir, filename)
            if os.path.exists(filepath):
                # 如果已存在，我们只需要读取一次内容以便同步到另一个目录（如果需要）
                if content is None:
                    with open(filepath, 'rb') as f:
                        content = f.read()
                continue
        
        # 如果内容仍为空，则下载
        if content is None:
            response = get_with_retry(img_url)
            if response:
                content = response.content
            else:
                logger.error(f"图片下载最终失败: {img_url}")
                return None
        
        # 同步保存到两个目录
        for base_dir in [SRC_UPLOAD_DIR, TARGET_UPLOAD_DIR]:
            filepath = os.path.join(base_dir, filename)
            if not os.path.exists(filepath):
                with open(filepath, 'wb') as f:
                    f.write(content)
                logger.info(f"图片同步保存至: {filepath}")
                
        return relative_path
    except Exception as e:
        logger.error(f"下载图片过程发生异常 {img_url}: {str(e)}")
    return None

def check_exists(title):
    """通过标题检查新闻是否已存在"""
    try:
        params = {'keyword': title, 'size': 1}
        response = get_with_retry(f"{API_URL}/list", params=params)
        if response:
            data = response.json()
            if data.get('code') == 200:
                records = data.get('data', {}).get('records', [])
                for record in records:
                    if record.get('title') == title:
                        return True
    except Exception as e:
        logger.error(f"检查新闻是否存在失败: {str(e)}")
    return False

def parse_detail(url):
    """解析新闻详情页"""
    try:
        response = get_with_retry(url)
        if not response:
            return None
            
        response.encoding = 'utf-8'
        soup = BeautifulSoup(response.text, 'lxml')
        
        content_div = soup.find('div', class_='content')
        if not content_div:
            content_div = soup.find('div', id='signals-content') or soup.find('div', class_='article')
            
        if content_div:
            # 清理不需要的标签
            for s in content_div.find_all(['script', 'style', 'iframe']):
                s.decompose()
            return str(content_div)
    except Exception as e:
        logger.error(f"解析详情页失败 {url}: {str(e)}")
    return None

# 类别映射表
CATEGORY_MAP = {
    "中超": 1,
    "英超": 2,
    "德甲": 3,
    "意甲": 4,
    "AC米兰": 4,
    "国际米兰": 4,
    "尤文": 4,
    "罗马": 4,
    "那不勒斯": 4,
    "欧联": 5,
    "欧冠": 5,
    "西甲": 6,
    "皇家马德里": 6,
    "巴萨": 6,
    "马竞": 6,
}

def get_category_id(label_text):
    if not label_text:
        return 0
    # 优先匹配欧战
    if "欧冠" in label_text or "欧联" in label_text:
        return 5
    for key, val in CATEGORY_MAP.items():
        if key in label_text:
            return val
    return 0  # 综合

def start_spider(max_count=50):
    logger.info(f"开始采集新闻，目标数量: {max_count} 条...")
    
    # 预检后端
    try:
        test_res = requests.get(f"http://localhost:8080/api/news/list", params={'size': 1}, timeout=5)
        if test_res.status_code == 401:
            logger.error("后端接口仍返回 401 Unauthorized，请检查 SecurityConfig 配置并确保服务已重启生效")
            return
        elif test_res.status_code != 200:
            logger.warning(f"后端接口连接测试异常 (状态码 {test_res.status_code})，尝试继续运行...")
        else:
            logger.info("后端接口连接测试成功")
    except Exception as e:
        logger.error(f"无法连接到后端接口: {str(e)}")
        return

    success_count = 0
    try:
        target_url = "https://news.zhibo8.com/zuqiu/more.htm"
        response = get_with_retry(target_url)
        if not response:
            logger.error("无法获取新闻列表页")
            return
            
        response.encoding = 'utf-8'
        soup = BeautifulSoup(response.text, 'lxml')
        
        # 使用更宽泛的选择器获取列表项
        news_items = soup.find_all('li')
        if not news_items:
            logger.error("未找到新闻列表项")
            return

        logger.info(f"找到 {len(news_items)} 条可能的列表项")
        
        for item in news_items:
            if success_count >= max_count:
                logger.info(f"已达到目标数量 {max_count}，停止采集")
                break
                
            try:
                # 1. 提取标题和链接 (必须有 zuqiu 链接)
                a_tag = item.find('a', href=True)
                if not a_tag or '/zuqiu/' not in a_tag['href']:
                    continue
                
                title = a_tag.get_text(strip=True)
                if not title or len(title) < 5: continue
                
                # 排除分页和“更多”链接
                detail_url = a_tag['href']
                if 'more.htm' in detail_url or 'index.htm' in detail_url:
                    continue
                if not detail_url.startswith('http'):
                    detail_url = 'https:' + detail_url
                
                if 'news.zhibo8.com' not in detail_url: continue
                
                # 3. 查重
                if check_exists(title):
                    logger.info(f"新闻已存在，跳过: {title}")
                    continue
                
                # 提取标签
                label_text = item.get('data-label', '')
                # 如果 data-label 为空，尝试查找 b 标签或 span.label
                if not label_text:
                    label_tag = item.find('b') or item.find('span', class_='label')
                    if label_tag:
                        label_text = label_tag.get_text(strip=True)
                
                cat_id = get_category_id(label_text)
                
                logger.info(f"正在处理 [{success_count+1}/{max_count}]: [{label_text}] {title}")
                
                # 4. 获取详情
                content_html = parse_detail(detail_url)
                if not content_html:
                    logger.warning(f"获取内容为空，跳过: {title}")
                    continue
                
                # 5. 提取摘要和封面
                detail_soup = BeautifulSoup(content_html, 'lxml')
                p_tags = detail_soup.find_all('p')
                summary = p_tags[0].get_text(strip=True)[:150] if p_tags else ""
                
                img_url = ""
                content_div = detail_soup.find('div', class_='content') or \
                              detail_soup.find('div', id='signals-content') or \
                              detail_soup.find('div', class_='article')
                
                if content_div:
                    all_imgs = content_div.find_all('img')
                    for img in all_imgs:
                        src = img.get('src') or img.get('data-src')
                        if src and not any(x in src.lower() for x in ['logo', 'icon', 'pixel']):
                            img_url = src
                            break
                
                if not img_url:
                    img_tag = item.find('img')
                    if img_tag:
                        img_url = img_tag.get('src') or img_tag.get('data-src')
                
                if img_url and not img_url.startswith('http'):
                    img_url = 'https:' + img_url
                
                # 6. 构造数据
                news_data = {
                    "title": title,
                    "summary": summary,
                    "content": content_html,
                    "coverUrl": img_url,  # 直接使用原始 URL
                    "categoryId": cat_id,
                    "category": label_text.strip(','), # 备用字段
                    "author": "直播吧",
                    "publishTime": datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
                }
                
                # 7. 入库 (使用 / 接口)
                api_res = requests.post(f"{API_URL}", json=news_data, timeout=10)
                if api_res.status_code == 200:
                    success_count += 1
                    logger.info(f"[{success_count}/{max_count}] 新闻入库成功: {title}")
                else:
                    logger.error(f"新闻入库失败: {title}, 状态码: {api_res.status_code}, 响应: {api_res.text}")
                
                time.sleep(random.uniform(1, 2))
                
            except Exception as e:
                logger.error(f"处理单条新闻失败: {str(e)}")
                continue
                
    except Exception as e:
        logger.error(f"采集过程发生错误: {str(e)}")

if __name__ == "__main__":
    start_spider(50)
