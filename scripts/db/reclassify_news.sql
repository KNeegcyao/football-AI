-- 1. 重置所有非欧战、非社区的 category_id
UPDATE news SET category_id = 0 WHERE category_id NOT IN (5);

-- 2. 根据标题和标签重新进行精准映射
-- 中超 (1)
UPDATE news SET category_id = 1 WHERE (title LIKE '%中超%' OR category LIKE '%中超%' OR title LIKE '%中国足球%' OR category LIKE '%中国足球%' OR title LIKE '%泰山%' OR title LIKE '%申花%' OR title LIKE '%海港%' OR title LIKE '%国安%' OR title LIKE '%蓉城%');

-- 英超 (2)
UPDATE news SET category_id = 2 WHERE (title LIKE '%英超%' OR category LIKE '%英超%' OR title LIKE '%曼联%' OR title LIKE '%曼城%' OR title LIKE '%阿森纳%' OR title LIKE '%切尔西%' OR title LIKE '%利物浦%' OR title LIKE '%热刺%');

-- 德甲 (3)
UPDATE news SET category_id = 3 WHERE (title LIKE '%德甲%' OR category LIKE '%德甲%' OR title LIKE '%拜仁%' OR title LIKE '%多特%' OR title LIKE '%勒沃库森%');

-- 意甲 (4)
UPDATE news SET category_id = 4 WHERE (title LIKE '%意甲%' OR category LIKE '%意甲%' OR title LIKE '%尤文%' OR title LIKE '%国米%' OR title LIKE '%AC米兰%' OR title LIKE '%罗马%' OR title LIKE '%那不勒斯%');

-- 欧战 (5) - 包含欧冠、欧联、欧协联
UPDATE news SET category_id = 5 WHERE (title LIKE '%欧冠%' OR category LIKE '%欧冠%' OR title LIKE '%欧联%' OR category LIKE '%欧联%' OR title LIKE '%欧协%');

-- 西甲 (6)
UPDATE news SET category_id = 6 WHERE (title LIKE '%西甲%' OR category LIKE '%西甲%' OR title LIKE '%皇马%' OR title LIKE '%巴萨%' OR title LIKE '%马竞%');

-- 3. 再次强制校准一些明显的错误 ID
UPDATE news SET category_id = 2 WHERE id = 165; -- 天空析英超争冠 -> 英超
UPDATE news SET category_id = 6 WHERE id IN (11, 107, 142); -- 巴萨/皇马相关 -> 西甲
UPDATE news SET category_id = 4 WHERE id IN (160, 164, 178); -- 罗马相关 -> 意甲
UPDATE news SET category_id = 1 WHERE id IN (159, 157, 158); -- 泰山/蓉城相关 -> 中超
UPDATE news SET category_id = 3 WHERE id IN (101, 104, 136, 137); -- 拜仁/德甲相关 -> 德甲
