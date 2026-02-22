package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.service.mapper.PlayerMapper;
import com.soccer.forum.service.service.PlayerSyncService;
import com.soccer.forum.service.utils.RapidApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.mapper.TeamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PlayerSyncServiceImpl implements PlayerSyncService {

    private static final Logger log = LoggerFactory.getLogger(PlayerSyncServiceImpl.class);

    private static final java.util.Map<String, Integer> FOOTBALL_DATA_TEAMS = new java.util.LinkedHashMap<>();
    static {
        FOOTBALL_DATA_TEAMS.put("阿森纳", 57);
        FOOTBALL_DATA_TEAMS.put("切尔西", 61);
        FOOTBALL_DATA_TEAMS.put("利物浦", 64);
        FOOTBALL_DATA_TEAMS.put("曼城", 65);
        FOOTBALL_DATA_TEAMS.put("曼联", 66);
        FOOTBALL_DATA_TEAMS.put("巴塞罗那", 81);
        FOOTBALL_DATA_TEAMS.put("皇家马德里", 86);
        FOOTBALL_DATA_TEAMS.put("拜仁慕尼黑", 5);
        FOOTBALL_DATA_TEAMS.put("勒沃库森", 3);
        FOOTBALL_DATA_TEAMS.put("AC米兰", 98);
        FOOTBALL_DATA_TEAMS.put("国际米兰", 108);
        FOOTBALL_DATA_TEAMS.put("尤文图斯", 109);
        FOOTBALL_DATA_TEAMS.put("那不勒斯", 113);
        FOOTBALL_DATA_TEAMS.put("巴黎圣日耳曼", 524);
    }

    private static final java.util.Map<String, String> NATIONALITY_MAP = new java.util.HashMap<>();
    static {
        NATIONALITY_MAP.put("England", "英格兰");
        NATIONALITY_MAP.put("Spain", "西班牙");
        NATIONALITY_MAP.put("Germany", "德国");
        NATIONALITY_MAP.put("France", "法国");
        NATIONALITY_MAP.put("Italy", "意大利");
        NATIONALITY_MAP.put("Portugal", "葡萄牙");
        NATIONALITY_MAP.put("Netherlands", "荷兰");
        NATIONALITY_MAP.put("Belgium", "比利时");
        NATIONALITY_MAP.put("Brazil", "巴西");
        NATIONALITY_MAP.put("Argentina", "阿根廷");
        NATIONALITY_MAP.put("Uruguay", "乌拉圭");
        NATIONALITY_MAP.put("Croatia", "克罗地亚");
        NATIONALITY_MAP.put("USA", "美国");
        NATIONALITY_MAP.put("Russia", "俄罗斯");
        NATIONALITY_MAP.put("Ireland", "爱尔兰");
        NATIONALITY_MAP.put("Northern Ireland", "北爱尔兰");
        NATIONALITY_MAP.put("Scotland", "苏格兰");
        NATIONALITY_MAP.put("Wales", "威尔士");
        NATIONALITY_MAP.put("Switzerland", "瑞士");
        NATIONALITY_MAP.put("Austria", "奥地利");
        NATIONALITY_MAP.put("Denmark", "丹麦");
        NATIONALITY_MAP.put("Sweden", "瑞典");
        NATIONALITY_MAP.put("Norway", "挪威");
        NATIONALITY_MAP.put("Poland", "波兰");
        NATIONALITY_MAP.put("Ukraine", "乌克兰");
        NATIONALITY_MAP.put("Serbia", "塞尔维亚");
        NATIONALITY_MAP.put("Turkey", "土耳其");
        NATIONALITY_MAP.put("Japan", "日本");
        NATIONALITY_MAP.put("South Korea", "韩国");
        NATIONALITY_MAP.put("Korea, South", "韩国");
        NATIONALITY_MAP.put("China", "中国");
        NATIONALITY_MAP.put("Colombia", "哥伦比亚");
        NATIONALITY_MAP.put("Chile", "智利");
        NATIONALITY_MAP.put("Mexico", "墨西哥");
        NATIONALITY_MAP.put("Morocco", "摩洛哥");
        NATIONALITY_MAP.put("Senegal", "塞内加尔");
        NATIONALITY_MAP.put("Nigeria", "尼日利亚");
        NATIONALITY_MAP.put("Egypt", "埃及");
        NATIONALITY_MAP.put("Algeria", "阿尔及利亚");
        NATIONALITY_MAP.put("Ivory Coast", "科特迪瓦");
        NATIONALITY_MAP.put("Ghana", "加纳");
        NATIONALITY_MAP.put("Cameroon", "喀麦隆");
        NATIONALITY_MAP.put("Paraguay", "巴拉圭");
        NATIONALITY_MAP.put("Slovenia", "斯洛文尼亚");
        NATIONALITY_MAP.put("Ecuador", "厄瓜多尔");
        NATIONALITY_MAP.put("Georgia", "格鲁吉亚");
        NATIONALITY_MAP.put("Hungary", "匈牙利");
        NATIONALITY_MAP.put("Uzbekistan", "乌兹别克斯坦");
        NATIONALITY_MAP.put("Canada", "加拿大");
        NATIONALITY_MAP.put("Turkiye", "土耳其");
        NATIONALITY_MAP.put("Guinea", "几内亚");
        NATIONALITY_MAP.put("Burkina Faso", "布基纳法索");
        NATIONALITY_MAP.put("Mali", "马里");
        NATIONALITY_MAP.put("Czech Republic", "捷克");
        NATIONALITY_MAP.put("Romania", "罗马尼亚");
        NATIONALITY_MAP.put("Armenia", "亚美尼亚");
        NATIONALITY_MAP.put("Kosovo", "科索沃");
        NATIONALITY_MAP.put("Montenegro", "黑山");
        NATIONALITY_MAP.put("North Macedonia", "北马其顿");
        NATIONALITY_MAP.put("Slovakia", "斯洛伐克");
    }

    private static final java.util.Map<String, String> POSITION_MAP = new java.util.HashMap<>();
    static {
        POSITION_MAP.put("Goalkeeper", "门将");
        POSITION_MAP.put("Defence", "后卫");
        POSITION_MAP.put("Defender", "后卫");
        POSITION_MAP.put("Midfield", "中场");
        POSITION_MAP.put("Midfielder", "中场");
        POSITION_MAP.put("Offence", "前锋");
        POSITION_MAP.put("Attacker", "前锋");
        POSITION_MAP.put("Forward", "前锋");
        POSITION_MAP.put("Winger", "边锋");
        
        // Detailed positions
        POSITION_MAP.put("Centre-Back", "中后卫");
        POSITION_MAP.put("Left-Back", "左后卫");
        POSITION_MAP.put("Right-Back", "右后卫");
        POSITION_MAP.put("Defensive Midfield", "防守型中场");
        POSITION_MAP.put("Central Midfield", "中前卫");
        POSITION_MAP.put("Attacking Midfield", "进攻型中场");
        POSITION_MAP.put("Left Winger", "左边锋");
        POSITION_MAP.put("Right Winger", "右边锋");
        POSITION_MAP.put("Centre-Forward", "中锋");
        POSITION_MAP.put("Second Striker", "影锋");
    }

    private String translatePosition(String enPos) {
        if (enPos == null) return null;
        return POSITION_MAP.getOrDefault(enPos, enPos);
    }

    private void fillFootballDataDetails(Player player, Integer apiPlayerId) {
        try {
            JsonNode personData = footballDataApiClient.getPerson(apiPlayerId);
            if (personData != null) {
                if (personData.hasNonNull("firstName") && !personData.get("firstName").asText().isEmpty()) {
                    player.setFirstname(personData.get("firstName").asText());
                } else if (player.getName() != null) {
                    // Fallback: split name
                    String[] parts = player.getName().split(" ", 2);
                    if (parts.length > 0) player.setFirstname(parts[0]);
                    if (parts.length > 1 && player.getLastname() == null) player.setLastname(parts[1]);
                }
                
                if (personData.hasNonNull("lastName") && !personData.get("lastName").asText().isEmpty()) {
                    player.setLastname(personData.get("lastName").asText());
                }

                if (personData.hasNonNull("placeOfBirth")) {
                    player.setBirthPlace(personData.get("placeOfBirth").asText());
                }
                
                if (personData.hasNonNull("nationality")) {
                    String nat = personData.get("nationality").asText();
                    if (NATIONALITY_MAP.containsKey(nat)) {
                        nat = NATIONALITY_MAP.get(nat);
                    }
                    player.setNationality(nat);
                }
                
                if (personData.hasNonNull("dateOfBirth")) {
                    try {
                        player.setBirthDate(java.time.LocalDate.parse(personData.get("dateOfBirth").asText().substring(0, 10)));
                    } catch (Exception e) {
                        log.warn("解析生日失败: {}", player.getName());
                    }
                }

                if (personData.hasNonNull("height")) {
                    player.setHeight(personData.get("height").asInt());
                }
                if (personData.hasNonNull("weight")) {
                    player.setWeight(personData.get("weight").asInt());
                }

                if (personData.hasNonNull("marketValue")) {
                    String mvStr = personData.get("marketValue").asText();
                    try {
                        // Remove currency symbol and spaces, keep numbers and suffix
                        String cleanMv = mvStr.replaceAll("[^0-9.kmb]", "").toLowerCase();
                        BigDecimal value;
                        if (cleanMv.endsWith("m")) {
                            value = new BigDecimal(cleanMv.substring(0, cleanMv.length() - 1)).multiply(new BigDecimal(1000000));
                        } else if (cleanMv.endsWith("k")) {
                            value = new BigDecimal(cleanMv.substring(0, cleanMv.length() - 1)).multiply(new BigDecimal(1000));
                        } else if (cleanMv.endsWith("b")) {
                            value = new BigDecimal(cleanMv.substring(0, cleanMv.length() - 1)).multiply(new BigDecimal(1000000000));
                        } else {
                            value = new BigDecimal(cleanMv);
                        }
                        player.setMarketValue(value);
                    } catch (Exception e) {
                        log.warn("解析身价失败: {} - {}", player.getName(), mvStr);
                    }
                }

                if (personData.hasNonNull("section")) { 
                    // Sometimes position is in section
                    if (player.getPosition() == null || player.getPosition().equals("Unknown")) {
                        String sectionPos = personData.get("section").asText();
                        player.setPosition(translatePosition(sectionPos));
                    }
                }
                
                if (personData.hasNonNull("foot")) { 
                    player.setPreferredFoot(personData.get("foot").asText());
                }
                if (personData.hasNonNull("shirtNumber")) {
                    player.setJerseyNumber(personData.get("shirtNumber").asInt());
                }
                if (personData.hasNonNull("currentTeam") && personData.get("currentTeam").hasNonNull("contract")) {
                    JsonNode contract = personData.get("currentTeam").get("contract");
                    if (contract.hasNonNull("until")) {
                        try {
                            String until = contract.get("until").asText();
                            if (until != null && !until.isEmpty()) {
                                if (until.length() == 7) until += "-01";
                                player.setContractUntil(java.time.LocalDate.parse(until));
                            }
                        } catch(Exception e) {
                            log.warn("解析合同日期失败: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取球员详情失败: {}", e.getMessage());
        }
    }

    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private RapidApiClient rapidApiClient;

    @Autowired
    private com.soccer.forum.service.utils.SportApiClient sportApiClient;

    @Autowired
    private com.soccer.forum.service.utils.FootballDataApiClient footballDataApiClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public JsonNode getPlayerDetailJson(Long id) {
        Player player = playerMapper.selectById(id);
        if (player == null) {
            return null;
        }

        // 自动补充数据: 如果关键字段缺失且有 API ID，尝试同步
        if (player.getApiId() != null && (player.getBirthPlace() == null || player.getContractUntil() == null 
            || player.getPreferredFoot() == null || player.getWeight() == null || player.getHeight() == null)) {
            // 避免频繁请求: 如果最近1分钟内已更新，则跳过
            boolean shouldSync = true;
            if (player.getUpdatedAt() != null) {
                long minutes = java.time.Duration.between(player.getUpdatedAt(), java.time.LocalDateTime.now()).toMinutes();
                // log.info("自动同步检查: id={}, updatedAt={}, now={}, diffMinutes={}", id, player.getUpdatedAt(), java.time.LocalDateTime.now(), minutes);
                if (minutes < 1) {
                    shouldSync = false;
                }
            }
            
            if (shouldSync) {
                try {
                    log.info("检测到球员数据不全 (id={}), 触发自动同步...", id);
                    if (player.getSourceFrom() != null && player.getSourceFrom().contains("Football-Data")) {
                        fillFootballDataDetails(player, player.getApiId());
                        playerMapper.updateById(player);
                    } else {
                        syncPlayerFromSportApi(player.getApiId());
                        // 重新加载
                        player = playerMapper.selectById(id);
                    }
                } catch (Exception e) {
                    log.error("自动同步失败", e);
                }
            }
        }

        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode playerNode = root.putObject("player");

        // 基本信息
        playerNode.put("id", player.getId());
        playerNode.put("apiId", player.getApiId());
        playerNode.put("name", player.getName());
        playerNode.put("displayName", player.getDisplayName());
        
        // 名字拆分
        String fullName = player.getDisplayName() != null ? player.getDisplayName() : player.getName();
        if (fullName != null) {
            String[] names = fullName.split(" ");
            if (names.length > 0) playerNode.put("firstname", names[0]);
            if (names.length > 1) playerNode.put("lastname", names[names.length - 1]);
            else playerNode.put("lastname", "");
        }

        playerNode.put("photo", player.getPhotoUrl());
        playerNode.put("birthPlace", player.getBirthPlace());
        if (player.getBirthDate() != null) {
            playerNode.put("birthDate", player.getBirthDate().toString());
            // 计算年龄
            int age = java.time.Period.between(player.getBirthDate(), java.time.LocalDate.now()).getYears();
            playerNode.put("age", age);
        }
        
        playerNode.put("nationality", player.getNationality());
        playerNode.put("height", player.getHeight()); 
        playerNode.put("weight", player.getWeight()); 
        playerNode.put("position", player.getPosition());
        playerNode.put("detailedPos", player.getDetailedPos());
        playerNode.put("jerseyNumber", player.getJerseyNumber());
        playerNode.put("preferredFoot", player.getPreferredFoot());
        playerNode.put("contractUntil", player.getContractUntil() != null ? player.getContractUntil().toString() : null);
        if (player.getMarketValue() != null) {
             playerNode.put("proposedMarketValue", player.getMarketValue());
        }
        playerNode.put("status", player.getStatus());

        // 球队信息
        if (player.getCurrentTeamId() != null) {
            Team team = teamMapper.selectById(player.getCurrentTeamId());
            if (team != null) {
                playerNode.put("teamName", team.getName());
                playerNode.put("teamLogo", team.getLogoUrl());
                
                ObjectNode teamObj = playerNode.putObject("team");
                teamObj.put("id", team.getApiId()); 
                teamObj.put("name", team.getName());
                teamObj.put("logo", team.getLogoUrl());
            }
        }

        // 统计数据
        ArrayNode statsArr = root.putArray("statistics");
        ObjectNode stats = statsArr.addObject();
        
        ObjectNode games = stats.putObject("games");
        games.put("appearences", player.getAppearances() != null ? player.getAppearances() : 0);
        games.put("rating", player.getRating() != null ? player.getRating().toString() : "0.0");
        games.put("position", player.getPosition());

        ObjectNode goals = stats.putObject("goals");
        goals.put("total", player.getGoals() != null ? player.getGoals() : 0);
        goals.put("assists", player.getAssists() != null ? player.getAssists() : 0);

        return root;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String syncTeamPlayers(Long teamId, Integer apiTeamId, Integer season) {
        // 如果 season 为 null，默认使用 2023 或当前年份
        if (season == null) {
            season = 2023; // 默认赛季，或者应该从系统配置获取
        }
        log.info("开始同步球队球员数据: teamId={}, apiTeamId={}, season={}", teamId, apiTeamId, season);
        
        try {
            // 调用 API-Football
            // Endpoint: /players?team={team}&season={season}
            String endpoint = "/players?team=" + apiTeamId + "&season=" + season;
            JsonNode rootNode = rapidApiClient.get(endpoint);
            
            if (rootNode == null) {
                return "同步失败: API返回空数据";
            }
            if (!rootNode.has("response")) {
                if (rootNode.has("message")) {
                     return "同步失败: " + rootNode.get("message").asText();
                }
                if (rootNode.has("errors")) {
                     return "同步失败: " + rootNode.get("errors").toString();
                }
                return "同步失败: API响应格式错误";
            }

            JsonNode playersNode = rootNode.get("response");
            int count = 0;
            if (playersNode.isArray()) {
                for (JsonNode item : playersNode) {
                    try {
                        processPlayerSync(teamId, item);
                        count++;
                    } catch (Exception e) {
                        log.error("处理球员数据出错: " + item, e);
                    }
                }
            }
            
            log.info("球队球员数据同步完成，共处理 {} 条记录", count);
            return "同步成功: 更新/新增 " + count + " 名球员";
        } catch (Exception e) {
            log.error("同步过程发生异常", e);
            return "同步异常: " + e.getMessage();
        }
    }

    private void processPlayerSync(Long teamId, JsonNode item) {
        JsonNode playerInfo = item.get("player");
        JsonNode statsArr = item.get("statistics");
        JsonNode stats = statsArr != null && statsArr.isArray() && statsArr.size() > 0 ? statsArr.get(0) : null;

        if (playerInfo == null) return;

        Integer apiId = playerInfo.get("id").asInt();
        String name = playerInfo.get("name").asText();
        String firstname = playerInfo.get("firstname").asText();
        String lastname = playerInfo.get("lastname").asText();
        String photo = playerInfo.get("photo").asText();
        
        // 查找是否存在
        Player player = playerMapper.selectOne(new LambdaQueryWrapper<Player>()
                .eq(Player::getApiId, apiId));

        boolean isNew = false;
        if (player == null) {
            player = new Player();
            player.setApiId(apiId);
            player.setSourceFrom("api-football");
            player.setCurrentTeamId(teamId);
            isNew = true;
        }

        // 更新基本信息
        player.setName(name);
        player.setDisplayName(lastname + " " + firstname); // 简易拼接，实际可用翻译API
        player.setPhotoUrl(photo);
        
        if (playerInfo.hasNonNull("nationality")) {
            String nat = playerInfo.get("nationality").asText();
            if (NATIONALITY_MAP.containsKey(nat)) {
                nat = NATIONALITY_MAP.get(nat);
            }
            player.setNationality(nat);
        }
        
        if (playerInfo.hasNonNull("injured")) {
            boolean injured = playerInfo.get("injured").asBoolean();
            player.setStatus(injured ? "injured" : "active");
        }
        
        if (stats != null) {
            JsonNode games = stats.get("games");
            if (games != null) {
                player.setPosition(games.get("position").asText());
                player.setDetailedPos(games.get("position").asText()); // API-Football position is usually short like "Midfielder"
                player.setRating(games.hasNonNull("rating") ? new BigDecimal(games.get("rating").asText()) : BigDecimal.ZERO);
                player.setAppearances(games.hasNonNull("appearences") ? games.get("appearences").asInt() : 0);
                if (games.hasNonNull("number")) {
                    player.setJerseyNumber(games.get("number").asInt());
                }
            }
            
            JsonNode goals = stats.get("goals");
            if (goals != null) {
                player.setGoals(goals.hasNonNull("total") ? goals.get("total").asInt() : 0);
                player.setAssists(goals.hasNonNull("assists") ? goals.get("assists").asInt() : 0);
            }
        }
        
        // 默认状态
        if (player.getStatus() == null) {
            player.setStatus("active");
        }

        if (isNew) {
            playerMapper.insert(player);
        } else {
            playerMapper.updateById(player);
        }
    }

    @Override
    public void syncMarketValuesAndStatus() {
        log.info("开始同步球员身价与状态...");
        // 伪代码: 获取全量 API 数据或按球队循环更新
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncMatchStats(Integer matchApiId) {
        log.info("开始同步比赛球员统计: matchApiId={}", matchApiId);
        // 伪代码: 
        // 1. 获取比赛统计 (goals, assists, cards, rating)
        // 2. 找到对应球员 (通过 api_id)
        // 3. 累加数据:
        //    player.setGoals(player.getGoals() + newGoals);
        //    player.setAppearances(player.getAppearances() + 1);
        //    ...
        //    playerMapper.updateById(player);
    }

    @Override
    public JsonNode getPlayerFromSportApi(Integer playerId) {
        log.info("从 SportAPI (Mock/DB) 获取球员数据: id={}", playerId);
        
        // 1. Try DB first
        Player p = playerMapper.selectOne(new LambdaQueryWrapper<Player>()
                .eq(Player::getApiId, playerId));
        
        if (p != null) {
            return convertPlayerDetailToSportApiJson(p);
        }
        
        // 2. Fallback to SportAPI mock
        return sportApiClient.getPlayer(playerId);
    }

    private JsonNode convertPlayerDetailToSportApiJson(Player p) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ArrayNode responseArray = mapper.createArrayNode();
        
        ObjectNode item = mapper.createObjectNode();
        
        // Player object
        ObjectNode playerObj = mapper.createObjectNode();
        playerObj.put("id", p.getApiId() != null ? p.getApiId() : p.getId());
        playerObj.put("name", p.getDisplayName() != null ? p.getDisplayName() : p.getName());
        playerObj.put("shortName", p.getDisplayName());
        playerObj.put("slug", p.getSlug());
        
        // Parse English name from slug
        String slug = p.getSlug();
        String firstname = "";
        String lastname = "";
        if (slug != null && !slug.isEmpty()) {
            String[] parts = slug.split("-");
            if (parts.length > 0) {
                firstname = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
            }
            if (parts.length > 1) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    if (parts[i].length() > 0) {
                        sb.append(parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1));
                        if (i < parts.length - 1) sb.append(" ");
                    }
                }
                lastname = sb.toString();
            }
        }
        playerObj.put("firstname", firstname);
        playerObj.put("lastname", lastname);
        
        // Add detailed fields
        playerObj.put("nationality", p.getNationality());
        playerObj.put("birthPlace", p.getBirthPlace());
        playerObj.put("birthDate", p.getBirthDate() != null ? p.getBirthDate().toString() : null);
        if (p.getBirthDate() != null) {
            playerObj.put("age", java.time.Period.between(p.getBirthDate(), java.time.LocalDate.now()).getYears());
        } else {
            playerObj.put("age", (Integer)null);
        }

        playerObj.put("position", p.getPosition());
        playerObj.put("jerseyNumber", p.getJerseyNumber() != null ? p.getJerseyNumber().toString() : "-");
        playerObj.put("height", p.getHeight());
        playerObj.put("weight", p.getWeight());
        playerObj.put("preferredFoot", p.getPreferredFoot());
        playerObj.put("contractUntil", p.getContractUntil() != null ? p.getContractUntil().toString() : null);
        playerObj.put("status", p.getStatus());
        playerObj.put("proposedMarketValue", p.getMarketValue() != null ? p.getMarketValue().toString() : null);
        
        // Stats
        ArrayNode statsArray = mapper.createArrayNode();
        ObjectNode stat = mapper.createObjectNode();
        ObjectNode games = mapper.createObjectNode();
        games.put("position", p.getDetailedPos() != null ? p.getDetailedPos() : p.getPosition());
        games.put("number", p.getJerseyNumber() != null ? p.getJerseyNumber().toString() : "-");
        games.put("rating", p.getRating() != null ? p.getRating().toString() : "0.0");
        games.put("appearences", p.getAppearances() != null ? p.getAppearances() : 0);
        
        stat.set("games", games);
        
        ObjectNode goals = mapper.createObjectNode();
        goals.put("total", p.getGoals() != null ? p.getGoals() : 0);
        goals.put("assists", p.getAssists() != null ? p.getAssists() : 0);
        stat.set("goals", goals);
        
        // Team info
        if (p.getCurrentTeamId() != null) {
            Team team = teamMapper.selectById(p.getCurrentTeamId());
            if (team != null) {
                ObjectNode teamObj = mapper.createObjectNode();
                teamObj.put("name", team.getName());
                teamObj.put("id", team.getApiId());
                stat.set("team", teamObj);
                playerObj.put("teamName", team.getName()); // Extra field for convenience
            }
        }
        
        statsArray.add(stat);
        
        item.set("player", playerObj);
        item.set("statistics", statsArray);
        
        responseArray.add(item);
        root.set("response", responseArray);
        return root;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String syncPlayerFromSportApi(Integer sportApiId) {
        // Implementation remains same, but maybe skip if source is fotmob?
        // For now, keep it as is.
        log.info("开始同步 SportAPI 球员: apiId={}", sportApiId);
        try {
            JsonNode rootNode = sportApiClient.getPlayer(sportApiId);
            if (rootNode == null) {
                return "同步失败: API返回空数据";
            }

            JsonNode playerNode = null;
            if (rootNode.has("player")) {
                playerNode = rootNode.get("player");
            } else if (rootNode.has("data") && rootNode.get("data").has("player")) {
                playerNode = rootNode.get("data").get("player");
            }
            
            if (playerNode == null) {
                 return "同步失败: 未找到球员信息";
            }

            // 解析基本信息
            Integer apiId = playerNode.hasNonNull("id") ? playerNode.get("id").asInt() : sportApiId;
            String name = playerNode.hasNonNull("name") ? playerNode.get("name").asText() : "";
            String shortName = playerNode.hasNonNull("shortName") ? playerNode.get("shortName").asText() : name;
            String slug = playerNode.hasNonNull("slug") ? playerNode.get("slug").asText() : "";
            
            // 查找是否存在 (优先查找已存在的记录，不限来源)
            Player player = playerMapper.selectOne(new LambdaQueryWrapper<Player>()
                    .eq(Player::getApiId, apiId)
                    .last("LIMIT 1"));
            
            boolean isNew = false;
            if (player == null) {
                player = new Player();
                player.setApiId(apiId);
                player.setCreatedAt(LocalDateTime.now());
                isNew = true;
            }
            // 更新来源为 sportapi (因为我们正在使用 sportapi 数据覆盖)
            player.setSourceFrom("sportapi");
            player.setUpdatedAt(LocalDateTime.now());
            
            // 填充字段
            player.setName(name);
            player.setDisplayName(shortName);
            player.setSlug(slug);
            
            if (playerNode.hasNonNull("position")) {
                player.setPosition(playerNode.get("position").asText());
            }
            if (playerNode.hasNonNull("height")) {
                player.setHeight(playerNode.get("height").asInt());
            }
            if (playerNode.hasNonNull("weight")) {
                player.setWeight(playerNode.get("weight").asInt());
            }
            if (playerNode.hasNonNull("jerseyNumber")) {
                player.setJerseyNumber(playerNode.get("jerseyNumber").asInt());
            }
            if (playerNode.hasNonNull("preferredFoot")) {
                player.setPreferredFoot(playerNode.get("preferredFoot").asText());
            }

            // 合同到期日
            if (playerNode.hasNonNull("contractUntilTimestamp")) {
                long timestamp = playerNode.get("contractUntilTimestamp").asLong();
                player.setContractUntil(java.time.Instant.ofEpochSecond(timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            } else if (playerNode.hasNonNull("contractUntil")) {
                 try {
                     String contractStr = playerNode.get("contractUntil").asText();
                     // Try parsing as ISO date or timestamp string
                     if (contractStr.matches("\\d+")) {
                         long timestamp = Long.parseLong(contractStr);
                         player.setContractUntil(java.time.Instant.ofEpochSecond(timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                     } else {
                         try {
                             player.setContractUntil(java.time.LocalDate.parse(contractStr));
                         } catch (Exception e1) {
                             // Some APIs return "30 Jun 2026" or ISO
                             // For now assume ISO or let it fail gracefully
                             ZonedDateTime zdt = ZonedDateTime.parse(contractStr);
                             player.setContractUntil(zdt.toLocalDate());
                         }
                     }
                 } catch (Exception e) {
                     log.warn("合同到期日解析失败: {}", e.getMessage());
                 }
            }
            
            // 市场价值
            if (playerNode.hasNonNull("proposedMarketValue")) {
                player.setMarketValue(new BigDecimal(playerNode.get("proposedMarketValue").asText()));
            }

            // 头像 URL (SportAPI/SofaScore 模式)
            if (player.getPhotoUrl() == null || player.getPhotoUrl().isEmpty()) {
                player.setPhotoUrl("https://api.sofascore.app/api/v1/player/" + apiId + "/image");
            }

            // 国籍
            if (playerNode.hasNonNull("country")) {
                JsonNode country = playerNode.get("country");
                if (country.hasNonNull("name")) {
                    player.setNationality(country.get("name").asText());
                }
            }
            
            // 出生地
            if (playerNode.hasNonNull("birthCity")) {
                JsonNode city = playerNode.get("birthCity");
                if (city.hasNonNull("name")) {
                    player.setBirthPlace(city.get("name").asText());
                }
            } else if (playerNode.hasNonNull("placeOfBirth")) {
                 player.setBirthPlace(playerNode.get("placeOfBirth").asText());
            } else if (playerNode.hasNonNull("birth") && playerNode.get("birth").hasNonNull("place")) {
                 player.setBirthPlace(playerNode.get("birth").get("place").asText());
            }

            // 出生日期
            if (playerNode.hasNonNull("dateOfBirthTimestamp")) {
                long timestamp = playerNode.get("dateOfBirthTimestamp").asLong();
                player.setBirthDate(java.time.Instant.ofEpochSecond(timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            } else if (playerNode.hasNonNull("dateOfBirth")) {
                 try {
                     // 格式: 1985-02-05T00:00:00+00:00
                     String dobStr = playerNode.get("dateOfBirth").asText();
                     ZonedDateTime zdt = ZonedDateTime.parse(dobStr);
                     player.setBirthDate(zdt.toLocalDate());
                 } catch (Exception e) {
                     log.warn("出生日期解析失败: {}", e.getMessage());
                 }
            }

            // 尝试关联球队
            if (playerNode.hasNonNull("team")) {
                JsonNode teamNode = playerNode.get("team");
                if (teamNode.hasNonNull("name")) {
                    String teamName = teamNode.get("name").asText();
                    // 1. 按名称查找
                    Team team = teamMapper.selectOne(new LambdaQueryWrapper<Team>()
                            .eq(Team::getName, teamName)
                            .last("LIMIT 1"));

                    // 2. 如果没找到，尝试按英文名称查找
                    if (team == null) {
                        team = teamMapper.selectOne(new LambdaQueryWrapper<Team>()
                                .eq(Team::getEnglishName, teamName)
                                .last("LIMIT 1"));
                    }

                    if (team != null) {
                        player.setCurrentTeamId(team.getId());
                    } else {
                        log.warn("未找到关联球队: {}", teamName);
                    }
                }
            }
            
            // 保存
            if (isNew) {
                playerMapper.insert(player);
                return "同步成功: 新增球员 " + name + " (ID: " + player.getId() + ")";
            } else {
                playerMapper.updateById(player);
                return "同步成功: 更新球员 " + name + " (ID: " + player.getId() + ")";
            }

        } catch (Exception e) {
            log.error("SportAPI 同步异常", e);
            return "同步失败: " + e.getMessage();
        }
    }

    @Override
    public JsonNode getTeamPlayersFromSportApi(Long teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("球队不存在");
        }
        
        // 1. Try DB first
        List<Player> dbPlayers = playerMapper.selectList(new LambdaQueryWrapper<Player>()
                .eq(Player::getCurrentTeamId, teamId)
                .orderByAsc(Player::getJerseyNumber));
        
        if (dbPlayers != null && !dbPlayers.isEmpty()) {
            return convertPlayersToSportApiJson(dbPlayers, team);
        }

        // 2. Check if it is a Football-Data.org team (by name)
        Integer footballDataId = null;
        for (java.util.Map.Entry<String, Integer> entry : FOOTBALL_DATA_TEAMS.entrySet()) {
            // Check if DB name contains the key (e.g. "阿森纳足球俱乐部" contains "阿森纳")
            if (team.getName() != null && team.getName().contains(entry.getKey())) {
                footballDataId = entry.getValue();
                break;
            }
        }

        if (footballDataId != null) {
            log.info("Team {} found in Football-Data map (ID: {}). Syncing...", team.getName(), footballDataId);
            try {
                String result = syncFootballDataForTeam(team, footballDataId);
                log.info("On-demand sync result: {}", result);
                
                // Re-fetch from DB
                dbPlayers = playerMapper.selectList(new LambdaQueryWrapper<Player>()
                        .eq(Player::getCurrentTeamId, teamId)
                        .orderByAsc(Player::getJerseyNumber));
                
                if (dbPlayers != null && !dbPlayers.isEmpty()) {
                    return convertPlayersToSportApiJson(dbPlayers, team);
                }
            } catch (Exception e) {
                log.error("On-demand sync failed for team {}", team.getName(), e);
            }
        }

        if (team.getApiId() == null) {
            // Just return mock if no API ID
            return sportApiClient.getMockTeamPlayers(teamId);
        }
        // 3. Fallback to SportAPI (Mock if quota exceeded)
        return sportApiClient.getTeamPlayers(team.getApiId());
    }

    private JsonNode convertPlayersToSportApiJson(List<Player> players, Team team) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ArrayNode responseArray = mapper.createArrayNode();

        for (Player p : players) {
            ObjectNode item = mapper.createObjectNode();
            
            // Player object
        ObjectNode playerObj = mapper.createObjectNode();
        playerObj.put("id", p.getApiId() != null ? p.getApiId() : p.getId());
        playerObj.put("name", p.getDisplayName() != null ? p.getDisplayName() : p.getName());
        playerObj.put("shortName", p.getDisplayName());
        playerObj.put("slug", p.getSlug());
        
        // Parse English name from slug
        String slug = p.getSlug();
        String firstname = "";
        String lastname = "";
        if (slug != null && !slug.isEmpty()) {
            String[] parts = slug.split("-");
            if (parts.length > 0) {
                firstname = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
            }
            if (parts.length > 1) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    if (parts[i].length() > 0) {
                        sb.append(parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1));
                        if (i < parts.length - 1) sb.append(" ");
                    }
                }
                lastname = sb.toString();
            }
        }
        playerObj.put("firstname", firstname);
        playerObj.put("lastname", lastname);
        
        playerObj.put("jerseyNumber", p.getJerseyNumber() != null ? p.getJerseyNumber().toString() : "-");
            playerObj.put("position", p.getPosition());
            
            // Stats object
            ArrayNode statsArray = mapper.createArrayNode();
            ObjectNode stat = mapper.createObjectNode();
            ObjectNode games = mapper.createObjectNode();
            games.put("position", p.getDetailedPos() != null ? p.getDetailedPos() : p.getPosition());
        games.put("number", p.getJerseyNumber() != null ? p.getJerseyNumber().toString() : "-");
        games.put("rating", p.getRating() != null ? p.getRating().toString() : "0.0");
        games.put("appearences", p.getAppearances() != null ? p.getAppearances() : 0);
        
        stat.set("games", games);
        
        ObjectNode goals = mapper.createObjectNode();
        goals.put("total", p.getGoals() != null ? p.getGoals() : 0);
        goals.put("assists", p.getAssists() != null ? p.getAssists() : 0);
        stat.set("goals", goals);
        
        // Team info
            ObjectNode teamObj = mapper.createObjectNode();
            teamObj.put("name", team.getName());
            teamObj.put("id", team.getApiId());
            stat.set("team", teamObj);
            
            statsArray.add(stat);
            
            item.set("player", playerObj);
            item.set("statistics", statsArray);
            
            responseArray.add(item);
        }
        
        root.set("response", responseArray);
        return root;
    }

    @Override
    public String syncAllTeamsFromFootballData() {
        log.info("开始使用 Football-Data.org 同步豪门球队数据...");
        
        try {
            int updatedCount = 0;
            int createdCount = 0;

            for (java.util.Map.Entry<String, Integer> entry : FOOTBALL_DATA_TEAMS.entrySet()) {
                String teamName = entry.getKey();
                Integer apiTeamId = entry.getValue();
                
                log.info("正在同步球队: {} (ID: {})", teamName, apiTeamId);
                
                // 查找本地 Team
                Team localTeam = teamMapper.selectOne(new LambdaQueryWrapper<Team>()
                        .like(Team::getName, teamName).last("LIMIT 1"));
                
                if (localTeam == null) {
                    log.info("本地未找到球队: {}, 正在创建...", teamName);
                    localTeam = new Team();
                    localTeam.setName(teamName);
                    localTeam.setApiId(Long.valueOf(apiTeamId));
                    localTeam.setCreatedAt(java.time.LocalDateTime.now());
                    localTeam.setUpdatedAt(java.time.LocalDateTime.now());
                    teamMapper.insert(localTeam);
                }

                try {
                    String result = syncFootballDataForTeam(localTeam, apiTeamId);
                    log.info("球队 {} 同步结果: {}", teamName, result);
                } catch (Exception e) {
                    log.error("同步球队 {} 失败: {}", teamName, e.getMessage(), e);
                }
            }
            return "同步完成";
        } catch (Exception e) {
            log.error("全量同步发生异常", e);
            return "同步失败: " + e.getMessage();
        }
    }

    private String syncFootballDataForTeam(Team localTeam, Integer apiTeamId) {
        int updatedCount = 0;
        int createdCount = 0;

        JsonNode squadData = footballDataApiClient.getTeamSquad(apiTeamId);
        if (squadData == null || !squadData.hasNonNull("squad")) {
            log.warn("无法获取球队阵容数据: {}", localTeam.getName());
            return "Failed to get squad";
        }

        ArrayNode squad = (ArrayNode) squadData.get("squad");
        for (JsonNode pNode : squad) {
            String pName = pNode.get("name").asText();
            String position = pNode.hasNonNull("position") ? pNode.get("position").asText() : "Unknown";
            Integer apiPlayerId = pNode.get("id").asInt();
            Integer jerseyNumber = pNode.hasNonNull("shirtNumber") ? pNode.get("shirtNumber").asInt() : null;
            java.time.LocalDate birthDate = null;
            if (pNode.hasNonNull("dateOfBirth")) {
                try {
                    birthDate = java.time.LocalDate.parse(pNode.get("dateOfBirth").asText().substring(0, 10));
                } catch (Exception e) {
                    // ignore
                }
            }
            
            // 查找现有球员
            // 1. 按名字和球队
            Player player = playerMapper.selectOne(new LambdaQueryWrapper<Player>()
                    .eq(Player::getName, pName)
                    .eq(Player::getCurrentTeamId, localTeam.getId())
                    .last("LIMIT 1"));

            if (player == null) {
                // 2. 按名字查找 (不限球队)
                player = playerMapper.selectOne(new LambdaQueryWrapper<Player>()
                        .eq(Player::getName, pName)
                        .last("LIMIT 1"));
            }

            // 3. 尝试用 球队ID + 背号 匹配 (针对中文名的情况)
            if (player == null && jerseyNumber != null) {
                player = playerMapper.selectOne(new LambdaQueryWrapper<Player>()
                        .eq(Player::getCurrentTeamId, localTeam.getId())
                        .eq(Player::getJerseyNumber, jerseyNumber)
                        .last("LIMIT 1"));
                if (player != null) {
                    log.info("通过背号匹配到球员: {} -> {} (API Name: {})", player.getJerseyNumber(), player.getName(), pName);
                }
            }

            // 4. 尝试用 球队ID + 生日 匹配
            if (player == null && birthDate != null) {
                player = playerMapper.selectOne(new LambdaQueryWrapper<Player>()
                        .eq(Player::getCurrentTeamId, localTeam.getId())
                        .eq(Player::getBirthDate, birthDate)
                        .last("LIMIT 1"));
                if (player != null) {
                    log.info("通过生日匹配到球员: {} -> {} (API Name: {})", player.getBirthDate(), player.getName(), pName);
                }
            }
            
            // 过滤逻辑: 如果是新球员，且看起来像预备队/青年队，则跳过
            if (player == null) {
                // 1. 必须有背号
                if (jerseyNumber == null) {
                    continue;
                }
                
                // 计算年龄
                int age = 0;
                if (birthDate != null) {
                    age = java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
                }
                
                // 2. 过滤掉背号过大且年轻的球员 (通常是预备队提拔)
                // 规则: 背号 > 55 且 年龄 < 19 -> 跳过
                if (jerseyNumber > 55 && age < 19) {
                    log.info("跳过年轻球员/预备队: {} (No. {}, Age {})", pName, jerseyNumber, age);
                    continue;
                }
            }

            boolean isNew = false;
            if (player == null) {
                player = new Player();
                player.setName(pName);
                player.setCurrentTeamId(localTeam.getId());
                player.setSourceFrom("Football-Data");
                isNew = true;
            } else {
                // 更新现有球员
                // 如果原来的 source 不是 Football-Data，可以保留或追加
                if (player.getSourceFrom() == null || !player.getSourceFrom().contains("Football-Data")) {
                     // 可选：标记已被 Football-Data 更新
                     // player.setSourceFrom(player.getSourceFrom() + " / Football-Data");
                }
                
                if (!player.getCurrentTeamId().equals(localTeam.getId())) {
                    player.setCurrentTeamId(localTeam.getId());
                }
            }

            // 仅在字段为空时更新，或者如果它是我们创建的记录则更新
            if (player.getApiId() == null) player.setApiId(apiPlayerId);
            if (player.getPosition() == null || "Unknown".equals(player.getPosition())) {
                player.setPosition(translatePosition(position));
            }
            
            if (pNode.hasNonNull("nationality") && player.getNationality() == null) {
                String nat = pNode.get("nationality").asText();
                if (NATIONALITY_MAP.containsKey(nat)) {
                    nat = NATIONALITY_MAP.get(nat);
                }
                player.setNationality(nat);
            }
            if (birthDate != null && player.getBirthDate() == null) {
                player.setBirthDate(birthDate);
            }
            
            player.setUpdatedAt(java.time.LocalDateTime.now());

            // 检查是否需要详细信息
            boolean needDetail = isNew || player.getContractUntil() == null || player.getJerseyNumber() == null 
                || player.getBirthPlace() == null || player.getBirthDate() == null 
                || player.getHeight() == null || player.getWeight() == null || player.getPreferredFoot() == null;
            
            if (needDetail) {
                fillFootballDataDetails(player, apiPlayerId);
            }

            if (isNew) {
                // 新增球员必须有身价，否则删除（跳过）
                if (player.getMarketValue() == null) {
                    // 尝试再次确认是否能从其他途径获取（目前仅依赖 fillFootballDataDetails）
                    // 如果确实没有，则跳过
                    log.warn("跳过无身价的新球员: {} (ID: {})", pName, apiPlayerId);
                    continue;
                }
                
                playerMapper.insert(player);
                createdCount++;
            } else {
                playerMapper.updateById(player);
                updatedCount++;
            }
        }
        return "Updated: " + updatedCount + ", Created: " + createdCount;
    }

    @Override
    public String syncLeagueScorers() {
        String[] leagues = {"PL", "PD", "SA", "BL1", "FL1", "CL"}; // Premier League, La Liga, Serie A, Bundesliga, Ligue 1, Champions League
        StringBuilder sb = new StringBuilder();
        
        for (String league : leagues) {
            try {
                JsonNode root = footballDataApiClient.getScorers(league);
                if (root == null || !root.has("scorers")) {
                    sb.append(league).append(": Failed/No Data; ");
                    continue;
                }
                
                JsonNode scorers = root.get("scorers");
                int updatedCount = 0;
                
                for (JsonNode scorer : scorers) {
                    JsonNode playerNode = scorer.get("player");
                    
                    if (playerNode == null) continue;
                    
                    Integer apiId = playerNode.get("id").asInt();
                    
                    // Stats
                    int goals = scorer.hasNonNull("goals") ? scorer.get("goals").asInt() : 0;
                    int assists = scorer.hasNonNull("assists") ? scorer.get("assists").asInt() : 0;
                    int appearances = scorer.hasNonNull("playedMatches") ? scorer.get("playedMatches").asInt() : 0;
                    
                    // Find player
                    Player player = playerMapper.selectOne(new LambdaQueryWrapper<Player>().eq(Player::getApiId, apiId).last("LIMIT 1"));
                    
                    if (player == null) {
                        continue;
                    }
                    
                    // Update stats
                    player.setGoals(goals);
                    player.setAssists(assists);
                    player.setAppearances(appearances);
                    player.setUpdatedAt(java.time.LocalDateTime.now());
                    
                    playerMapper.updateById(player);
                    updatedCount++;
                }
                
                sb.append(league).append(": ").append(updatedCount).append(" updated; ");
                
            } catch (Exception e) {
                log.error("Sync scorers failed for {}", league, e);
                sb.append(league).append(": Error; ");
            }
        }
        
        return sb.toString();
    }
}