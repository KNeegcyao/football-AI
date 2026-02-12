
USE soccer_forum;

CREATE TABLE IF NOT EXISTS news (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT '标题',
    summary VARCHAR(500) COMMENT '摘要',
    content LONGTEXT COMMENT '内容',
    cover_url VARCHAR(255) COMMENT '封面图URL',
    author VARCHAR(100) COMMENT '作者',
    publish_time DATETIME COMMENT '发布时间',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    tags VARCHAR(255) COMMENT '标签',
    category VARCHAR(50) COMMENT '分类',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资讯表';

CREATE TABLE IF NOT EXISTS matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    home_team_id BIGINT NOT NULL COMMENT '主队ID',
    away_team_id BIGINT NOT NULL COMMENT '客队ID',
    competition_name VARCHAR(100) COMMENT '赛事名称',
    match_time DATETIME NOT NULL COMMENT '比赛时间',
    venue VARCHAR(255) COMMENT '场馆',
    status INT DEFAULT 0 COMMENT '状态: 0-未开始, 1-进行中, 2-已结束, 3-延期',
    home_score INT DEFAULT 0 COMMENT '主队得分',
    away_score INT DEFAULT 0 COMMENT '客队得分',
    round VARCHAR(50) COMMENT '轮次',
    broadcast_url VARCHAR(255) COMMENT '直播链接',
    live_text_id BIGINT COMMENT '文字直播ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事表';
