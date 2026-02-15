-- 初始化数据脚本
-- 禁用外键检查以便清理旧数据
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE matches;
TRUNCATE TABLE players;
TRUNCATE TABLE teams;
TRUNCATE TABLE news;
TRUNCATE TABLE posts;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 插入球队数据 (teams)
INSERT INTO teams (id, name, logo_url, founded_year, home_stadium, league, coach_name, formation) VALUES
(1, '皇家马德里', '/static/teams/real_madrid.png', 1902, '圣地亚哥·伯纳乌', '西甲', '安切洛蒂', '4-3-3'),
(2, '曼彻斯特城', '/static/teams/man_city.png', 1880, '阿提哈德球场', '英超', '瓜博拉', '4-1-4-1'),
(3, '阿森纳', '/static/teams/arsenal.png', 1886, '酋长球场', '英超', '阿尔特塔', '4-3-3'),
(4, '巴塞罗那', '/static/teams/barcelona.png', 1899, '诺坎普', '西甲', '弗里克', '4-2-3-1'),
(5, '利物浦', '/static/teams/liverpool.png', 1892, '安菲尔德', '英超', '斯洛特', '4-3-3'),
(6, '拜仁慕尼黑', '/static/teams/bayern.png', 1900, '安联球场', '德甲', '孔帕尼', '4-2-3-1');

-- 2. 插入球员数据 (players)
INSERT INTO players (name, position, current_team_id, jersey_number, nationality, market_value) VALUES
('姆巴佩', '前锋', 1, 9, '法国', 18000),
('贝林厄姆', '中场', 1, 5, '英格兰', 18000),
('哈兰德', '前锋', 2, 9, '挪威', 18000),
('德布劳内', '中场', 2, 17, '比利时', 6000),
('萨卡', '前锋', 3, 7, '英格兰', 14000),
('亚马尔', '前锋', 4, 19, '西班牙', 12000),
('萨拉赫', '前锋', 5, 11, '埃及', 5500),
('凯恩', '前锋', 6, 9, '英格兰', 10000);

-- 3. 插入资讯数据 (news)
INSERT INTO news (title, summary, content, category, author, publish_time, cover_url) VALUES
('欧冠抽签揭晓：皇马再遇曼城，强强对话提前上演', '欧冠1/4决赛抽签结果揭晓，卫冕冠军曼城将对阵欧冠之王皇家马德里。', '今日欧足联总部举行了本赛季欧冠联赛1/4决赛的抽签仪式。最引人注目的对决莫过于皇家马德里与曼彻斯特城的强强对话。', '战报', '足球周刊', NOW(), '/static/news/cl_draw.png'),
('转会流言：阿森纳领跑维尔茨争夺战，勒沃库森标价1.5亿', '据多家媒体报道，阿森纳在药厂球星维尔茨的争夺战中处于领先位置。', '勒沃库森的德国天才球员维尔茨本赛季表现依旧火热，吸引了多家豪门关注。', '转会', '泰晤士报', DATE_SUB(NOW(), INTERVAL 2 HOUR), '/static/news/wirtz.png'),
('亚马尔创纪录：成为金球奖历史上最年轻的前三入围者', '巴萨天才少年亚马尔再次刷新纪录，入围金球奖前三。', '17岁的亚马尔成功入围前三名，打破了此前由罗纳尔多保持的最年轻纪录...', '资讯', '世界体育报', DATE_SUB(NOW(), INTERVAL 1 DAY), '/static/news/yamal.png');

-- 4. 插入赛事数据 (matches)
INSERT INTO matches (home_team_id, away_team_id, match_time, status, home_score, away_score, competition_name, round, venue) VALUES
(1, 4, DATE_SUB(NOW(), INTERVAL 1 DAY), 2, 3, 2, '西甲', '第26轮', '圣地亚哥·伯纳乌'),
(2, 5, DATE_SUB(NOW(), INTERVAL 1 DAY), 2, 1, 1, '英超', '第25轮', '阿提哈德球场'),
(3, 6, NOW(), 1, 2, 1, '欧冠', '1/8决赛', '酋长球场'),
(1, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 0, 0, 0, '欧冠', '1/8决赛', '圣地亚哥·伯纳乌'),
(4, 5, DATE_ADD(NOW(), INTERVAL 2 DAY), 0, 0, 0, '友谊赛', '季前赛', '诺坎普');

-- 5. 插入帖子数据 (posts)
INSERT INTO posts (title, content, user_id, views, likes) VALUES
('如何评价皇马本赛季的表现？', '我觉得虽然有伤病，但安切洛蒂的调整能力真的很强。', 1, 1500, 120),
('曼城本赛季还能拿三冠王吗？', '虽然现在积分落后，但曼城的后劲一直很足，我看好他们。', 1, 800, 45),
('讨论：谁才是当今世界第一前锋？', '是哈兰德的进球效率，还是姆巴佩的全面技术？大家怎么看？', 1, 3000, 210);
