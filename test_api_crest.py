import requests
headers = {'X-Auth-Token': 'fec2cf7e61b4407b95b4dcc0e239c73e'}
res = requests.get('https://api.football-data.org/v4/teams/64', headers=headers)
print(res.json().get('crest'))