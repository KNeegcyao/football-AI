-- 资讯表
CREATE TABLE IF NOT EXISTS news (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    summary VARCHAR(500) COMMENT '摘要',
    content TEXT COMMENT '正文',
    cover_url VARCHAR(500) COMMENT '封面图URL',
    author VARCHAR(50) COMMENT '作者/来源',
    publish_time DATETIME COMMENT '发布时间',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    tags VARCHAR(200) COMMENT '标签(逗号分隔)',
    category VARCHAR(50) COMMENT '分类(转会,战报,花边等)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_publish_time (publish_time),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='足球资讯表';

-- 赛事表
CREATE TABLE IF NOT EXISTS matches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    home_team_id BIGINT NOT NULL COMMENT '主队ID',
    away_team_id BIGINT NOT NULL COMMENT '客队ID',
    competition_name VARCHAR(100) COMMENT '赛事名称(英超,欧冠等)',
    match_time DATETIME NOT NULL COMMENT '比赛时间',
    venue VARCHAR(100) COMMENT '场馆',
    status INT DEFAULT 0 COMMENT '状态: 0-未开始, 1-进行中, 2-已结束, 3-延期',
    home_score INT DEFAULT 0 COMMENT '主队得分',
    away_score INT DEFAULT 0 COMMENT '客队得分',
    round VARCHAR(50) COMMENT '轮次',
    broadcast_url VARCHAR(500) COMMENT '直播链接',
    live_text_id BIGINT COMMENT '关联的文字直播ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (home_team_id) REFERENCES teams(id),
    FOREIGN KEY (away_team_id) REFERENCES teams(id),
    INDEX idx_match_time (match_time),
    INDEX idx_competition (competition_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事日程表';
