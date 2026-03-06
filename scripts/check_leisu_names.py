import subprocess
import re

url = "http://m.leisu.com/"
cmd = ["curl.exe", "-s", "-L", "-A", "Mozilla/5.0", url]
result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8')
html = result.stdout

# Find all team names
teams = re.findall(r'class="team-(?:home|away)"[^>]*>([^<]+)</div>', html)
print(f"Found {len(teams)} teams")
for t in set(teams):
    print(f"Leisu name: {t}")
