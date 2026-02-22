-- 球队表
CREATE TABLE IF NOT EXISTS teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '球队名称',
    logo_url VARCHAR(500) COMMENT '队徽URL',
    founded_year INT COMMENT '成立年份',
    home_stadium VARCHAR(100) COMMENT '主场',
    league VARCHAR(50) COMMENT '所属联赛',
    coach_name VARCHAR(50) COMMENT '主教练',
    formation VARCHAR(20) COMMENT '常用阵型',
    total_matches INT DEFAULT 0 COMMENT '总比赛场次',
    wins INT DEFAULT 0 COMMENT '胜场',
    draws INT DEFAULT 0 COMMENT '平场',
    losses INT DEFAULT 0 COMMENT '负场',
    goals_for INT DEFAULT 0 COMMENT '进球数',
    goals_against INT DEFAULT 0 COMMENT '失球数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_league (league),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='球队表';

-- 球员表
CREATE TABLE IF NOT EXISTS players (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 外部关联
    api_id INT NULL COMMENT '第三方API的唯一ID',
    source_from VARCHAR(20) NULL COMMENT '数据来源标识(如: api-football)',
    
    -- 基本信息
    name VARCHAR(100) NOT NULL COMMENT '英文原名',
    display_name VARCHAR(100) NULL COMMENT '中文显示名称',
    slug VARCHAR(100) NULL COMMENT 'URL别名(用于SEO和高级感路径)',
    photo_url VARCHAR(500) COMMENT '头像URL',
    
    -- 球员属性
    birth_date DATE COMMENT '出生日期',
    nationality VARCHAR(50) COMMENT '国籍',
    position VARCHAR(20) COMMENT '场上位置(如: G, D, M, F)',
    detailed_pos VARCHAR(50) NULL COMMENT '详细位置(如: Center-Back)',
    height INT COMMENT '身高(cm)',
    weight INT COMMENT '体重(kg)',
    preferred_foot ENUM('left', 'right', 'both') COMMENT '惯用脚',
    
    -- 俱乐部/竞技
    current_team_id BIGINT COMMENT '当前所属球队ID',
    jersey_number INT COMMENT '球衣号码',
    market_value DECIMAL(12, 2) COMMENT '身价(欧元)',
    status VARCHAR(20) DEFAULT 'active' NULL COMMENT '状态(active, injured, suspended)',
    
    -- 赛季汇总统计
    contract_until DATE COMMENT '合同到期日',
    appearances INT DEFAULT 0 COMMENT '出场次数',
    goals INT DEFAULT 0 COMMENT '进球数',
    assists INT DEFAULT 0 COMMENT '助攻数',
    yellow_cards INT DEFAULT 0 COMMENT '黄牌',
    red_cards INT DEFAULT 0 COMMENT '红牌',
    rating DECIMAL(3, 2) DEFAULT 0.00 NULL COMMENT '赛季平均评分',
    
    -- 扩展
    career_history JSON COMMENT '履历(JSON)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT players_api_id_uindex UNIQUE (api_id),
    INDEX idx_api_id (api_id),
    INDEX idx_team_pos (current_team_id, position),
    FOREIGN KEY (current_team_id) REFERENCES teams(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='球员基本信息表';
