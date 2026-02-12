# 智能AI足球社区论坛 - 开发计划

## 版本规划总览

| 版本 | 核心目标 | 开发周期 | 主要功能 |
|------|----------|----------|----------|
| 1.0 | MVP最小可行产品 | 4-6周 | 基础社区CRUD、用户体系、数据库搭建 |
| 2.0 | 垂直领域深化 | 3-4周 | 球队/球员库、资讯流、赛事日程 |
| 3.0 | AI智能化升级 | 4-5周 | LangChain4j集成、智能评论员、内容审计 |

---

## 1.0版本 - MVP最小可行产品

### 版本目标
构建一个可运行的基础社区论坛，包含用户注册登录、发帖回帖、点赞收藏等核心功能，完成基础技术架构搭建。

### TASK001 - 项目初始化与环境搭建
**版本**: 1.0  
**状态**: 计划中  
**优先级**: P0 - 阻塞性

#### 子任务清单
- [x] **TASK001-001** 创建SpringBoot多模块项目结构
- [x] **TASK001-002** 配置MySQL数据库和Redis缓存环境
- [x] **TASK001-003** 搭建uni-app微信小程序前端框架
- [x] **TASK001-004** 配置开发环境和Docker容器化

#### AI编程助手提示词
```
你现在是一名高级Java架构师，请根据以下要求创建SpringBoot多模块项目：

项目结构要求：
1. 创建父工程soccer-forum-parent，统一管理依赖版本
2. 创建三个子模块：
   - soccer-forum-common：公共工具类、常量、异常定义
   - soccer-forum-domain：实体类、DTO、VO定义
   - soccer-forum-service：核心业务服务（包含controller、service、repository）

技术栈要求：
- Spring Boot 3.2.x
- MyBatis-Plus 3.5.x
- MySQL 8.0驱动
- Redis 7.x (Spring Data Redis)
- Spring Security + JWT
- SpringDoc OpenAPI 3.x

请生成完整的pom.xml配置，包括：
1. 父pom的dependencyManagement
2. 各子模块的pom.xml
3. 必要的Maven插件配置
4. 版本号统一管理

注意：需要包含测试依赖（JUnit 5、Mockito、TestContainers）
```

#### 验收标准
- [ ] 项目能够成功编译并运行
- [ ] 所有依赖版本统一管理
- [ ] 单元测试框架配置完成
- [ ] Docker环境能够正常启动

#### 注意事项
- 确保所有依赖版本兼容性
- 配置国内Maven镜像源加速下载
- 预留AI模块的依赖位置

---

### TASK002 - 用户认证与权限系统
**版本**: 1.0  
**状态**: 计划中  
**优先级**: P0

#### 子任务清单
- [x] **TASK002-001** 设计用户表结构和实体类
- [x] **TASK002-002** 实现JWT认证过滤器
- [x] **TASK002-003** 微信小程序授权登录
- [x] **TASK002-004** 用户注册、登录、登出API

#### AI编程助手提示词
```
你现在是一名高级Java工程师，请根据以下实体类生成完整的用户认证系统：

实体类：
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    private String phone;
    private String password;
    private String avatar;
    private String nickname;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    private Integer level;
    private Integer points;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

```


要求：
1. 生成对应的MyBatis-Plus实体类（包含注解）
2. 创建UserService接口和实现类，包含：
    - 用户注册（密码加密、唯一性校验）
    - 用户登录（JWT生成、密码验证）
    - 用户信息查询
    - 用户信息更新
3. 实现JWT工具类，包含：
    - Token生成（包含用户ID、角色、过期时间）
    - Token验证
    - Token刷新机制
4. 创建Spring Security配置：
    - JWT认证过滤器
    - 密码加密配置（BCrypt）
    - 接口权限控制
5. 生成对应的Controller层代码，包含：
    - POST /api/v1/auth/register
    - POST /api/v1/auth/login
    - POST /api/v1/auth/logout
    - GET /api/v1/users/profile
    - PUT /api/v1/users/profile

安全要求：
- 密码使用BCrypt加密存储
- JWT密钥使用环境变量配置
- 登录失败次数限制（Redis计数）
- 敏感信息脱敏返回


#### 验收标准
- [ ] 用户能够成功注册和登录
- [ ] JWT Token能够正确生成和验证
- [ ] 密码加密存储验证通过
- [ ] 接口权限控制正常工作
- [ ] 单元测试覆盖率>80%

#### 注意事项
- 密码加密强度设置为10-12轮
- JWT过期时间设置为7天，支持刷新
- 登录接口需要防暴力破解
- 用户敏感信息需要脱敏返回

---

### TASK003 - 帖子管理模块
**版本**: 1.0  
**状态**: 计划中  
**优先级**: P0

#### 子任务清单
- [x] **TASK003-001** 设计帖子、评论表结构
- [x] **TASK003-002** 实现帖子CRUD操作
- [ ] **TASK003-003** 实现评论系统（树形结构）
- [ ] **TASK003-004** 集成Redis缓存优化查询

#### AI编程助手提示词
```
你现在是一名高级Java工程师，请根据以下需求生成帖子管理模块的完整代码：

数据库表结构：
CREATE TABLE posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    images JSON,
    tags VARCHAR(500),
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    collect_count INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    is_top TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    FULLTEXT idx_title_content (title, content)
);

CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT 0,
    content TEXT NOT NULL,
    like_count INT DEFAULT 0,
    reply_count INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
);

要求：
1. 生成对应的MyBatis-Plus实体类（包含逻辑删除、乐观锁）
2. 创建PostService包含以下功能：
    - 发布帖子（包含图片上传处理）
    - 分页查询帖子列表（支持按时间、热度排序）
    - 查询帖子详情（包含作者信息、统计信息）
    - 更新帖子（仅作者可编辑）
    - 删除帖子（软删除）
    - 帖子置顶/取消置顶（管理员权限）
3. 创建CommentService包含：
    - 发表评论（支持回复他人评论）
    - 查询评论列表（树形结构返回）
    - 删除评论（软删除，级联更新统计）
4. 集成Redis缓存：
    - 帖子详情缓存（TTL: 10分钟）
    - 帖子列表缓存（TTL: 5分钟，支持分页）
    - 评论数量缓存（TTL: 30分钟）
    - 使用Redis分布式锁防止缓存击穿
5. 创建对应的Controller：
    - POST /api/v1/posts
    - GET /api/v1/posts?page=1&size=10&sort=hot
    - GET /api/v1/posts/{id}
    - PUT /api/v1/posts/{id}
    - DELETE /api/v1/posts/{id}
    - POST /api/v1/posts/{postId}/comments
    - GET /api/v1/posts/{postId}/comments

性能要求：
- 支持分页查询，每页最多50条
- 支持模糊搜索（使用MySQL全文索引）
- 缓存更新使用Cache-Aside模式
- 评论树查询使用一次性查询+内存组装
```

#### 验收标准
- [ ] 帖子CRUD功能完整实现
- [ ] 评论树形结构正确展示
- [ ] Redis缓存命中率>80%
- [ ] 分页查询性能<100ms
- [ ] 单元测试覆盖率>85%

#### 注意事项
- 图片上传需要限制大小和格式
- 防止用户刷评论需要限流
- 软删除需要同步更新统计信息
- 缓存更新需要考虑并发情况
---

### TASK004 - 互动系统（点赞收藏）
**版本**: 1.0  
**状态**: 计划中  
**优先级**: P1

#### 子任务清单
- [ ] **TASK004-001** 设计点赞、收藏表结构
- [ ] **TASK004-002** 实现高频点赞的Redis优化
- [ ] **TASK004-003** 实现收藏功能
- [ ] **TASK004-004** 实现用户个人中心

#### AI编程助手提示词
```
你现在是一名高级Java工程师，请实现一个高性能的点赞收藏系统：

需求描述：
实现一个支持高并发的点赞收藏系统，要求：
1. 支持帖子点赞/取消点赞
2. 支持评论点赞/取消点赞
3. 支持帖子收藏/取消收藏
4. 支持查询用户点赞/收藏列表
5. 支持热门帖子排行榜

技术约束：
- 使用Redis处理高频点赞操作
- 使用MySQL做持久化存储
- 支持每秒1万次点赞操作
- 保证数据最终一致性

请实现以下组件：

1. Redis Key设计：
    - 帖子点赞总数：post:like:count:{postId}
    - 用户点赞记录：user:like:posts:{userId}
    - 帖子点赞用户集合：post:like:users:{postId}
    - 用户收藏列表：user:collect:posts:{userId}
    - 热门帖子榜：hot:posts:zset

2. LikeService实现：
    - likePost(Long userId, Long postId) - 帖子点赞
    - unlikePost(Long userId, Long postId) - 取消点赞
    - likeComment(Long userId, Long commentId) - 评论点赞
    - unlikeComment(Long userId, Long commentId) - 取消点赞
    - isPostLiked(Long userId, Long postId) - 检查是否点赞
    - getUserLikedPosts(Long userId, PageParam page) - 用户点赞列表

3. CollectService实现：
    - collectPost(Long userId, Long postId) - 收藏帖子
    - uncollectPost(Long userId, Long postId) - 取消收藏
    - getUserCollections(Long userId, PageParam page) - 用户收藏列表
    - isPostCollected(Long userId, Long postId) - 检查是否收藏

4. 定时任务：
    - 每5分钟将Redis点赞数据同步到MySQL
    - 每10分钟更新热门帖子排行榜
    - 使用分布式锁确保任务只执行一次

5. 数据库表：
     CREATE TABLE post_likes (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       user_id BIGINT NOT NULL,
       post_id BIGINT NOT NULL,
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       UNIQUE KEY uk_user_post (user_id, post_id)
   );
   
   CREATE TABLE user_collections (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       user_id BIGINT NOT NULL,
       post_id BIGINT NOT NULL,
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       UNIQUE KEY uk_user_post (user_id, post_id)
   );


6. 接口设计：
    - POST /api/v1/posts/{id}/like - 点赞帖子
    - DELETE /api/v1/posts/{id}/like - 取消点赞
    - POST /api/v1/posts/{id}/collect - 收藏帖子
    - DELETE /api/v1/posts/{id}/collect - 取消收藏
    - GET /api/v1/users/{id}/likes - 用户点赞列表
    - GET /api/v1/users/{id}/collections - 用户收藏列表
    - GET /api/v1/posts/hot - 热门帖子榜

性能优化要求：
- 使用Redis Pipeline批量操作
- 使用Lua脚本保证原子性
- 使用布隆过滤器减少无效查询
- 使用异步消息队列处理数据同步
```

#### 验收标准
- [ ] 点赞操作响应时间<50ms
- [ ] 支持1万TPS并发点赞
- [ ] 数据最终一致性保证
- [ ] 热门排行榜实时更新
- [ ] 用户个人中心数据完整

#### 注意事项
- 防止用户重复点赞需要幂等处理
- 热门排行榜需要考虑时间衰减因子
- 数据同步需要考虑异常情况重试
- 用户隐私数据需要脱敏展示

---

## 2.0版本 - 垂直领域深化

### 版本目标
在1.0基础上增加足球垂直领域功能，包括球队/球员资料库、资讯流、赛事日程等，打造专业的足球社区。

### TASK005 - 球队/球员资料库
**版本**: 2.0  
**状态**: 计划中  
**优先级**: P0

#### 子任务清单
- [ ] **TASK005-001** 设计球队/球员数据模型
- [ ] **TASK005-002** 实现球队CRUD管理
- [ ] **TASK005-003** 实现球员档案管理
- [ ] **TASK005-004** 实现数据搜索和筛选

#### AI编程助手提示词
```
你现在是一名足球数据专家，请设计并实现一个完整的球队/球员资料库系统：

数据模型设计：
1. 球队表结构：
    - 基本信息：名称、logo、成立年份、主场、所属联赛
    - 统计数据：总比赛场次、胜平负、进球数、失球数
    - 教练信息：主教练、助理教练
    - 阵容信息：当前阵容、常用阵型

2. 球员表结构：
    - 基本信息：姓名、照片、出生日期、国籍、位置
    - 技术数据：身高、体重、惯用脚
    - 职业数据：当前球队、球衣号码、转会费、合同到期日
    - 统计数据：出场次数、进球数、助攻数、红黄牌
    - 生涯轨迹：JSON格式存储历史球队数据

3. 关联关系：
    - 球队-球员：一对多关系
    - 球员-比赛：多对多关系（通过比赛统计表）

请实现以下功能：

1. TeamService实现：
    - createTeam(TeamDTO team) - 创建球队
    - updateTeam(Long teamId, TeamDTO team) - 更新球队信息
    - getTeamDetail(Long teamId) - 获取球队详情（包含球员列表）
    - listTeams(PageParam page, TeamQuery query) - 分页查询球队
    - getTeamStats(Long teamId) - 获取球队统计数据

2. PlayerService实现：
    - createPlayer(PlayerDTO player) - 创建球员档案
    - updatePlayer(Long playerId, PlayerDTO player) - 更新球员信息
    - getPlayerDetail(Long playerId) - 获取球员详情
    - listPlayers(PageParam page, PlayerQuery query) - 分页查询球员
    - getPlayerStats(Long playerId) - 获取球员统计数据
    - getPlayerCareer(Long playerId) - 获取球员生涯轨迹

3. 搜索功能：
    - 支持按球队名称模糊搜索
    - 支持按球员姓名、位置、国籍搜索
    - 支持按联赛筛选球队
    - 支持按年龄范围筛选球员
    - 使用Elasticsearch实现全文搜索（可选）

4. 数据导入：
    - 支持Excel批量导入球队数据
    - 支持JSON格式导入球员数据
    - 数据验证和错误处理

5. 数据库表设计：
 
   CREATE TABLE teams (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       name VARCHAR(100) NOT NULL,
       logo_url VARCHAR(500),
       founded_year INT,
       home_stadium VARCHAR(100),
       league VARCHAR(50),
       coach_name VARCHAR(50),
       formation VARCHAR(10),
       total_matches INT DEFAULT 0,
       wins INT DEFAULT 0,
       draws INT DEFAULT 0,
       losses INT DEFAULT 0,
       goals_for INT DEFAULT 0,
       goals_against INT DEFAULT 0,
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   );
   
   CREATE TABLE players (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       name VARCHAR(50) NOT NULL,
       photo_url VARCHAR(500),
       birth_date DATE,
       nationality VARCHAR(50),
       position VARCHAR(20),
       height INT,
       weight INT,
       preferred_foot VARCHAR(10),
       current_team_id BIGINT,
       jersey_number INT,
       market_value DECIMAL(12,2),
       contract_until DATE,
       appearances INT DEFAULT 0,
       goals INT DEFAULT 0,
       assists INT DEFAULT 0,
       yellow_cards INT DEFAULT 0,
       red_cards INT DEFAULT 0,
       career_history JSON,
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       FOREIGN KEY (current_team_id) REFERENCES teams(id)
   );
6. 接口设计：
    - POST /api/v2/teams - 创建球队
    - GET /api/v2/teams/{id} - 获取球队详情
    - PUT /api/v2/teams/{id} - 更新球队信息
    - GET /api/v2/teams - 分页查询球队
    - POST /api/v2/players - 创建球员
    - GET /api/v2/players/{id} - 获取球员详情
    - GET /api/v2/players - 分页查询球员
    - GET /api/v2/teams/{id}/players - 获取球队球员列表

数据验证要求：
- 球队名称唯一性校验
- 球员姓名+出生日期唯一性校验
- 转会费必须为正数
- 球衣号码在1-99之间
- 日期格式统一为YYYY-MM-DD
```

#### 验收标准
- [ ] 球队/球员数据模型完整
- [ ] CRUD操作功能正常
- [ ] 搜索功能响应时间<200ms
- [ ] 数据验证规则完整
- [ ] 支持批量数据导入

#### 注意事项
- 图片存储使用OSS服务
- 球员转会信息需要历史记录
- 数据更新需要考虑缓存同步
- 敏感信息需要权限控制

---

### TASK006 - 资讯流与赛事日程
**版本**: 2.0  
**状态**: 计划中  
**优先级**: P1

#### 子任务清单
- [ ] **TASK006-001** 设计资讯文章数据模型
- [ ] **TASK006-002** 实现资讯发布与管理
- [ ] **TASK006-003** 实现赛事日程管理
- [ ] **TASK006-004** 实现资讯推荐算法

#### AI编程助手提示词
```
你现在是一名内容管理系统专家，请实现足球资讯和赛事日程管理功能：

需求描述：
创建一个足球资讯发布系统和赛事日程管理，包含：
1. 资讯文章管理（支持富文本、图片、视频）
2. 赛事日程管理（支持订阅提醒）
3. 资讯推荐算法（基于用户兴趣）
4. 内容审核机制

数据模型设计：

1. 资讯文章表：
   CREATE TABLE articles (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       title VARCHAR(200) NOT NULL,
       summary VARCHAR(500),
       content LONGTEXT,
       cover_image VARCHAR(500),
       author_id BIGINT,
       category VARCHAR(50),
       tags JSON,
       source VARCHAR(100),
       source_url VARCHAR(500),
       view_count INT DEFAULT 0,
       like_count INT DEFAULT 0,
       comment_count INT DEFAULT 0,
       status TINYINT DEFAULT 1,
       is_top TINYINT DEFAULT 0,
       published_at DATETIME,
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   );

2. 赛事日程表：
   CREATE TABLE matches (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       home_team_id BIGINT NOT NULL,
       away_team_id BIGINT NOT NULL,
       match_time DATETIME NOT NULL,
       venue VARCHAR(100),
       league VARCHAR(50),
       round VARCHAR(20),
       home_score INT,
       away_score INT,
       status VARCHAR(20) DEFAULT 'SCHEDULED',
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   );

3. 用户订阅表：
   CREATE TABLE user_subscriptions (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       user_id BIGINT NOT NULL,
       target_type VARCHAR(20) NOT NULL,
       target_id BIGINT NOT NULL,
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       UNIQUE KEY uk_user_target (user_id, target_type, target_id)
   );
 
功能实现要求：

1. ArticleService实现：
    - createArticle(ArticleDTO article) - 创建资讯
    - publishArticle(Long articleId) - 发布资讯
    - getArticleDetail(Long articleId) - 获取资讯详情
    - listArticles(PageParam page, ArticleQuery query) - 分页查询资讯
    - getHotArticles(Integer days) - 获取热门资讯
    - searchArticles(String keyword, PageParam page) - 搜索资讯

2. MatchService实现：
    - createMatch(MatchDTO match) - 创建赛事
    - updateMatchScore(Long matchId, Integer homeScore, Integer awayScore) - 更新比分
    - getMatchDetail(Long matchId) - 获取赛事详情
    - listMatches(PageParam page, MatchQuery query) - 分页查询赛事
    - getUpcomingMatches(Integer days) - 获取即将开始的比赛
    - getMatchByTeam(Long teamId, PageParam page) - 获取球队赛程

3. SubscriptionService实现：
    - subscribe(Long userId, String targetType, Long targetId) - 订阅
    - unsubscribe(Long userId, String targetType, Long targetId) - 取消订阅
    - getUserSubscriptions(Long userId) - 获取用户订阅
    - getSubscribedMatches(Long userId) - 获取订阅的比赛

4. 推荐算法：
    - 基于用户浏览历史的协同过滤
    - 基于球队/球员偏好的内容推荐
    - 热门资讯加权推荐
    - 新资讯冷启动推荐

5. 内容审核：
    - 敏感词过滤
    - 图片内容审核（调用第三方API）
    - 人工审核流程
    - 举报处理机制

6. 通知提醒：
    - 比赛开始前1小时提醒
    - 关注球队比赛结果推送
    - 新资讯发布通知

接口设计：
- POST /api/v2/articles - 创建资讯
- GET /api/v2/articles/{id} - 获取资讯详情
- GET /api/v2/articles - 分页查询资讯
- POST /api/v2/matches - 创建赛事
- GET /api/v2/matches/{id} - 获取赛事详情
- GET /api/v2/matches - 分页查询赛事
- POST /api/v2/subscriptions - 创建订阅
- DELETE /api/v2/subscriptions - 取消订阅

性能优化：
- 资讯内容使用CDN加速
- 热门资讯缓存1小时
- 赛事数据缓存30分钟
- 使用Elasticsearch实现全文搜索
```

#### 验收标准
- [ ] 资讯发布流程完整
- [ ] 赛事日程管理功能正常
- [ ] 推荐算法准确率>70%
- [ ] 通知提醒及时送达
- [ ] 内容审核机制有效

#### 注意事项
- 资讯内容需要SEO优化
- 赛事数据需要实时更新
- 推荐算法需要A/B测试
- 通知推送需要考虑用户偏好

---

## 3.0版本 - AI智能化升级

### 版本目标
集成LangChain4j框架，实现智能足球评论员、赛后战报自动生成和内容智能审计，打造AI驱动的足球社区。

### TASK007 - LangChain4j集成与智能评论员
**版本**: 3.0  
**状态**: 计划中  
**优先级**: P0

#### 子任务清单
- [ ] **TASK007-001** 集成LangChain4j框架
- [ ] **TASK007-002** 实现智能足球评论员
- [ ] **TASK007-003** 实现赛后智能战报
- [ ] **TASK007-004** 实现提示模板管理系统

#### AI编程助手提示词
```
你现在是一名AI应用架构师，请基于LangChain4j实现智能足球评论员系统：

系统架构要求：
1. LangChain4j集成配置：
    - 配置多模型支持（OpenAI GPT-4、通义千问、文心一言）
    - 实现模型切换策略（基于成本、响应时间、质量）
    - 配置连接池和重试机制
    - 实现Token用量监控和成本控制

2. 智能评论员实现：
    - 基于比赛数据的实时解说生成
    - 支持多种解说风格：
        * 激情型（黄健翔风格）
        * 技术型（张路风格）
        * 幽默型（董路风格）
    - 支持个性化偏好学习
    - 实现上下文记忆（记住关键事件）

3. 数据输入结构：

   public class MatchEvent {
       private Long matchId;
       private String eventType; // GOAL, CARD, SUBSTITUTION, etc.
       private Integer minute;
       private String playerName;
       private String teamName;
       private Map<String, Object> metadata;
   }
   
   public class MatchContext {
       private Long matchId;
       private String homeTeam;
       private String awayTeam;
       private Integer homeScore;
       private Integer awayScore;
       private List<MatchEvent> recentEvents;
       private Map<String, Object> statistics;
   }

4. 提示模板设计：
  
   你是一位专业的足球解说员，正在解说{homeTeam} vs {awayTeam}的比赛。
   当前比分：{homeScore} - {awayScore}
   比赛时间：第{minute}分钟
   
   刚刚发生的事件：{eventDescription}
   
   请用{style}的风格，生成一段30-50字的解说词。
   要求：
   1. 语言生动形象，符合{style}特点
   2. 包含关键信息（球员、时间、比分影响）
   3. 避免重复描述
   4. 如果是进球，要体现激动情绪


5. 服务实现：

   @Service
   public class CommentaryService {
       
       public String generateCommentary(MatchContext context, CommentaryStyle style) {
           // 实现解说生成逻辑
       }
       
       public List<String> generateMatchSummary(Long matchId) {
           // 生成赛后总结
       }
       
       public void learnUserPreference(Long userId, CommentaryPreference preference) {
           // 学习用户偏好
       }
   }

6. 缓存策略：
    - 解说结果缓存5分钟
    - 用户偏好缓存1小时
    - 使用Redis存储历史解说
    - 实现缓存预热机制

7. 异步处理：
    - 使用Spring Async异步生成解说
    - 使用消息队列处理批量生成
    - 实现生成任务状态跟踪

8. 成本控制：
    - 实现Token用量统计
    - 设置每日调用上限
    - 实现动态模型选择（基于成本）
    - 提供生成质量评估

9. 接口设计：
    - POST /api/v3/ai/commentary - 生成解说
    - GET /api/v3/ai/commentary/{matchId} - 获取比赛解说
    - POST /api/v3/ai/commentary/style - 设置解说风格
    - GET /api/v3/ai/commentary/styles - 获取可用风格

10. 监控指标：
    - 生成延迟（P95, P99）
    - Token使用量
    - 用户满意度评分
    - 缓存命中率
    - 错误率

技术实现要求：
- 使用LangChain4j的ChatLanguageModel
- 实现StreamingResponseHandler支持流式输出
- 使用PromptTemplate管理提示模板
- 实现CircuitBreaker防止服务雪崩
- 使用RateLimiter控制调用频率
```

#### 验收标准
- [ ] LangChain4j集成配置完成
- [ ] 支持3种以上解说风格
- [ ] 生成延迟<2秒
- [ ] 支持用户偏好学习
- [ ] Token成本控制有效

#### 注意事项
- 需要考虑大模型调用成本
- 敏感内容需要过滤
- 多语言支持预留接口
- 需要A/B测试不同风格效果

---

### TASK006 - 资讯流与赛事日程
**版本**: 2.0  
**状态**: 计划中  
**优先级**: P1

#### 子任务清单
- [ ] **TASK006-001** 设计资讯文章数据模型
- [ ] **TASK006-002** 实现资讯发布与管理
- [ ] **TASK006-003** 实现赛事日程管理
- [ ] **TASK006-004** 实现资讯推荐算法

#### AI编程助手提示词
```
你现在是一名内容管理系统专家，请实现足球资讯和赛事日程管理功能：

需求描述：
创建一个足球资讯发布系统和赛事日程管理，包含：
1. 资讯文章管理（支持富文本、图片、视频）
2. 赛事日程管理（支持订阅提醒）
3. 资讯推荐算法（基于用户兴趣）
4. 内容审核机制

数据模型设计：

1. 资讯文章表：
   
   CREATE TABLE articles (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       title VARCHAR(200) NOT NULL,
       summary VARCHAR(500),
       content LONGTEXT,
       cover_image VARCHAR(500),
       author_id BIGINT,
       category VARCHAR(50),
       tags JSON,
       source VARCHAR(100),
       source_url VARCHAR(500),
       view_count INT DEFAULT 0,
       like_count INT DEFAULT 0,
       comment_count INT DEFAULT 0,
       status TINYINT DEFAULT 1,
       is_top TINYINT DEFAULT 0,
       published_at DATETIME,
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   );
  
2. 赛事日程表：
   
   CREATE TABLE matches (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       home_team_id BIGINT NOT NULL,
       away_team_id BIGINT NOT NULL,
       match_time DATETIME NOT NULL,
       venue VARCHAR(100),
       league VARCHAR(50),
       round VARCHAR(20),
       home_score INT,
       away_score INT,
       status VARCHAR(20) DEFAULT 'SCHEDULED',
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   );
  

3. 用户订阅表：

   CREATE TABLE user_subscriptions (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       user_id BIGINT NOT NULL,
       target_type VARCHAR(20) NOT NULL,
       target_id BIGINT NOT NULL,
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
       UNIQUE KEY uk_user_target (user_id, target_type, target_id)
   );

功能实现要求：

1. ArticleService实现：
    - createArticle(ArticleDTO article) - 创建资讯
    - publishArticle(Long articleId) - 发布资讯
    - getArticleDetail(Long articleId) - 获取资讯详情
    - listArticles(PageParam page, ArticleQuery query) - 分页查询资讯
    - getHotArticles(Integer days) - 获取热门资讯
    - searchArticles(String keyword, PageParam page) - 搜索资讯

2. MatchService实现：
    - createMatch(MatchDTO match) - 创建赛事
    - updateMatchScore(Long matchId, Integer homeScore, Integer awayScore) - 更新比分
    - getMatchDetail(Long matchId) - 获取赛事详情
    - listMatches(PageParam page, MatchQuery query) - 分页查询赛事
    - getUpcomingMatches(Integer days) - 获取即将开始的比赛
    - getMatchByTeam(Long teamId, PageParam page) - 获取球队赛程

3. SubscriptionService实现：
    - subscribe(Long userId, String targetType, Long targetId) - 订阅
    - unsubscribe(Long userId, String targetType, Long targetId) - 取消订阅
    - getUserSubscriptions(Long userId) - 获取用户订阅
    - getSubscribedMatches(Long userId) - 获取订阅的比赛

4. 推荐算法：
    - 基于用户浏览历史的协同过滤
    - 基于球队/球员偏好的内容推荐
    - 热门资讯加权推荐
    - 新资讯冷启动推荐

5. 内容审核：
    - 敏感词过滤
    - 图片内容审核（调用第三方API）
    - 人工审核流程
    - 举报处理机制

6. 通知提醒：
    - 比赛开始前1小时提醒
    - 关注球队比赛结果推送
    - 新资讯发布通知

接口设计：
- POST /api/v2/articles - 创建资讯
- GET /api/v2/articles/{id} - 获取资讯详情
- GET /api/v2/articles - 分页查询资讯
- POST /api/v2/matches - 创建赛事
- GET /api/v2/matches/{id} - 获取赛事详情
- GET /api/v2/matches - 分页查询赛事
- POST /api/v2/subscriptions - 创建订阅
- DELETE /api/v2/subscriptions - 取消订阅

性能优化：
- 资讯内容使用CDN加速
- 热门资讯缓存1小时
- 赛事数据缓存30分钟
- 使用Elasticsearch实现全文搜索
```

#### 验收标准
- [ ] 资讯发布流程完整
- [ ] 赛事日程管理功能正常
- [ ] 推荐算法准确率>70%
- [ ] 通知提醒及时送达
- [ ] 内容审核机制有效

#### 注意事项
- 资讯内容需要SEO优化
- 赛事数据需要实时更新
- 推荐算法需要A/B测试
- 通知推送需要考虑用户偏好

---

## 3.0版本 - AI智能化升级

### 版本目标
集成LangChain4j框架，实现智能足球评论员、赛后战报自动生成和内容智能审计，打造AI驱动的足球社区。

### TASK007 - LangChain4j集成与智能评论员
**版本**: 3.0  
**状态**: 计划中  
**优先级**: P0

#### 子任务清单
- [ ] **TASK007-001** 集成LangChain4j框架
- [ ] **TASK007-002** 实现智能足球评论员
- [ ] **TASK007-003** 实现赛后智能战报
- [ ] **TASK007-004** 实现提示模板管理系统

#### AI编程助手提示词
```
你现在是一名AI应用架构师，请基于LangChain4j实现智能足球评论员系统：

系统架构要求：
1. LangChain4j集成配置：
    - 配置多模型支持（OpenAI GPT-4、通义千问、文心一言）
    - 实现模型切换策略（基于成本、响应时间、质量）
    - 配置连接池和重试机制
    - 实现Token用量监控和成本控制

2. 智能评论员实现：
    - 基于比赛数据的实时解说生成
    - 支持多种解说风格：
        * 激情型（黄健翔风格）
        * 技术型（张路风格）
        * 幽默型（董路风格）
    - 支持个性化偏好学习
    - 实现上下文记忆（记住关键事件）

3. 数据输入结构：
   
   public class MatchEvent {
       private Long matchId;
       private String eventType; // GOAL, CARD, SUBSTITUTION, etc.
       private Integer minute;
       private String playerName;
       private String teamName;
       private Map<String, Object> metadata;
   }
   
   public class MatchContext {
       private Long matchId;
       private String homeTeam;
       private String awayTeam;
       private Integer homeScore;
       private Integer awayScore;
       private List<MatchEvent> recentEvents;
       private Map<String, Object> statistics;
   }

4. 提示模板设计：

   你是一位专业的足球解说员，正在解说{homeTeam} vs {awayTeam}的比赛。
   当前比分：{homeScore} - {awayScore}
   比赛时间：第{minute}分钟
   
   刚刚发生的事件：{eventDescription}
   
   请用{style}的风格，生成一段30-50字的解说词。
   要求：
   1. 语言生动形象，符合{style}特点
   2. 包含关键信息（球员、时间、比分影响）
   3. 避免重复描述
   4. 如果是进球，要体现激动情绪

5. 服务实现：
 
   @Service
   public class CommentaryService {
       
       public String generateCommentary(MatchContext context, CommentaryStyle style) {
           // 实现解说生成逻辑
       }
       
       public List<String> generateMatchSummary(Long matchId) {
           // 生成赛后总结
       }
       
       public void learnUserPreference(Long userId, CommentaryPreference preference) {
           // 学习用户偏好
       }
   }

6. 缓存策略：
    - 解说结果缓存5分钟
    - 用户偏好缓存1小时
    - 使用Redis存储历史解说
    - 实现缓存预热机制

7. 异步处理：
    - 使用Spring Async异步生成解说
    - 使用消息队列处理批量生成
    - 实现生成任务状态跟踪

8. 成本控制：
    - 实现Token用量统计
    - 设置每日调用上限
    - 实现动态模型选择（基于成本）
    - 提供生成质量评估

9. 接口设计：
    - POST /api/v3/ai/commentary - 生成解说
    - GET /api/v3/ai/commentary/{matchId} - 获取比赛解说
    - POST /api/v3/ai/commentary/style - 设置解说风格
    - GET /api/v3/ai/commentary/styles - 获取可用风格

10. 监控指标：
    - 生成延迟（P95, P99）
    - Token使用量
    - 用户满意度评分
    - 缓存命中率
    - 错误率

技术实现要求：
- 使用LangChain4j的ChatLanguageModel
- 实现StreamingResponseHandler支持流式输出
- 使用PromptTemplate管理提示模板
- 实现CircuitBreaker防止服务雪崩
- 使用RateLimiter控制调用频率
```
#### 验收标准
- [ ] LangChain4j集成配置完成
- [ ] 支持3种以上解说风格
- [ ] 生成延迟<2秒
- [ ] 支持用户偏好学习
- [ ] Token成本控制有效

#### 注意事项
- 需要考虑大模型调用成本
- 敏感内容需要过滤
- 多语言支持预留接口
- 需要A/B测试不同风格效果
---

## 5. �ܽ���չ��

����Ŀ�����ƻ�ּ��ͨ�������׶εĵ�����MVP -> ��ֱ� -> AI���ܻ���������һ�������Ƚ��������������������ƽ̨��

### �ؼ��ɹ�Ҫ��
1. **ִ����**: �ϸ��� TASK �嵥ִ�У����������ơ�
2. **��������**: ÿ��ģ�������������ĵ�Ԫ���ԣ�JUnit 5���ͼ��ɲ��ԡ�
3. **AI �ں�**: ��ע AI ���ܵ�ʵ�����Ч����ȷ����Ӧ�ٶȺͳɱ��ɿء�

### ����ά��
- ���ڸ��������汾�����ּ���ջ�Ƚ��ԡ�
- �����ռ��û��������Ż� AI ��ʾ��ģ�塣
- ��������������ܣ���ʱ�������ݿ�ͻ�����š�

**Let's build the future of football community!**

