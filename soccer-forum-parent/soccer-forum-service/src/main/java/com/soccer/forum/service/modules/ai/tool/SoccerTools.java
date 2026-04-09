package com.soccer.forum.service.modules.ai.tool;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.modules.match.model.MatchVO;
import com.soccer.forum.service.modules.match.service.MatchService;
import com.soccer.forum.service.modules.match.service.PlayerService;
import com.soccer.forum.service.modules.match.service.PlayerSyncService;
import com.soccer.forum.service.modules.match.service.TeamService;
import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 足球数据查询工具
 * 提供给 AI Agent 调用的函数，用于查询数据库或 API
 */
@Component
public class SoccerTools {

    private static final Logger log = LoggerFactory.getLogger(SoccerTools.class);

    private final TeamService teamService;
    private final PlayerService playerService;
    private final MatchService matchService;
    private final PlayerSyncService playerSyncService;

    public SoccerTools(TeamService teamService, PlayerService playerService, MatchService matchService, PlayerSyncService playerSyncService) {
        this.teamService = teamService;
        this.playerService = playerService;
        this.matchService = matchService;
        this.playerSyncService = playerSyncService;
    }

    @Tool("查询球队基本信息和近期表现")
    public String getTeamHistory(String teamName) {
        log.info("AI 正在查询球队信息: {}", teamName);
        Page<Team> teamPage = teamService.listTeams(1, 1, teamName);
        if (teamPage.getRecords().isEmpty()) {
            return String.format("本地数据库暂无“%s”的记录。请基于你的足球知识库尝试回答其历史、荣誉等信息。", teamName);
        }
        Team team = teamPage.getRecords().get(0);
        
        // 查询该球队的近期比赛
        Page<MatchVO> matchPage = matchService.searchMatches(1, 5, teamName);
        String recentMatches = matchPage.getRecords().stream()
                .map(m -> String.format("%s %d-%d %s (%s)", 
                    m.getHomeTeam() != null ? m.getHomeTeam().getName() : "未知", 
                    m.getHomeScore() != null ? m.getHomeScore() : 0, 
                    m.getAwayScore() != null ? m.getAwayScore() : 0, 
                    m.getAwayTeam() != null ? m.getAwayTeam().getName() : "未知", 
                    m.getCompetitionName()))
                .collect(Collectors.joining("; "));

        return String.format("球队名称: %s; 联赛: %s; 主教练: %s; 主场: %s; 成立年份: %d; 近期比赛: %s", 
                team.getName(), team.getLeague(), team.getCoachName(), team.getHomeStadium(), 
                team.getFoundedYear(), recentMatches.isEmpty() ? "暂无近期比赛记录" : recentMatches);
    }

    @Tool("查询球星个人数据，返回出场次数、进球数和助攻数")
    public String getPlayerStats(String playerName) {
        log.info("AI 正在查询球员信息: {}", playerName);
        
        // 1. 尝试直接查询
        Page<Player> playerPage = playerService.listPlayers(1, 1, playerName, null);
        
        // 2. 如果没查到，尝试处理常见的中文简称/别名
        if (playerPage.getRecords().isEmpty()) {
            String processedName = playerName;
            if ("梅西".equals(playerName)) processedName = "Messi";
            else if ("C罗".equalsIgnoreCase(playerName) || "罗纳尔多".equals(playerName)) processedName = "Ronaldo";
            else if ("内马尔".equals(playerName)) processedName = "Neymar";
            else if ("姆巴佩".equals(playerName)) processedName = "Mbappe";
            else if ("哈兰德".equals(playerName)) processedName = "Haaland";
            else if ("孙兴慜".equals(playerName)) processedName = "Son Heung-Min";
            
            if (!processedName.equals(playerName)) {
                log.info("尝试使用处理后的名称查询: {}", processedName);
                playerPage = playerService.listPlayers(1, 1, processedName, null);
            }
        }

        if (playerPage.getRecords().isEmpty()) {
            log.info("本地数据库未查到球员: {}，返回指令引导 AI 使用其内部知识", playerName);
            return String.format("本地数据库中暂无“%s”的详细记录。请基于你的专业足球知识或搜索能力为用户解答，并说明这是基于你的知识储备而非本地实时数据。", playerName);
        }
        
        Player player = playerPage.getRecords().get(0);
        
        // 3. 使用 PlayerSyncService 获取最新的统计数据（这会自动触发数据同步）
        try {
            JsonNode detailJson = playerSyncService.getPlayerDetailJson(player.getId());
            if (detailJson != null && detailJson.has("player")) {
                JsonNode pNode = detailJson.get("player");
                JsonNode stats = detailJson.get("statistics").get(0);
                
                return String.format("【实时球员数据】姓名: %s; 国籍: %s; 俱乐部: %s; 位置: %s; 年龄: %d; 身高: %s; 体重: %s; " +
                        "本赛季数据: 出场 %d, 进球 %d, 助攻 %d, 评分 %s", 
                        pNode.path("displayName").asText(pNode.path("name").asText()),
                        pNode.path("nationality").asText("未知"),
                        pNode.path("teamName").asText("未知"),
                        pNode.path("position").asText("未知"),
                        pNode.path("age").asInt(0),
                        pNode.path("height").asText("未知") + "cm",
                        pNode.path("weight").asText("未知") + "kg",
                        stats.path("games").path("appearences").asInt(0),
                        stats.path("goals").path("total").asInt(0),
                        stats.path("goals").path("assists").asInt(0),
                        stats.path("games").path("rating").asText("0.0"));
            }
        } catch (Exception e) {
            log.error("获取球员实时数据失败", e);
        }
        
        // 兜底返回本地数据库记录
        return String.format("【球员数据(本地)】姓名: %s; 国籍: %s; 位置: %s; 身高: %dcm; 体重: %dkg; 出场: %d; 进球: %d; 助攻: %d", 
                player.getDisplayName() != null ? player.getDisplayName() : player.getName(), 
                player.getNationality(), player.getPosition(), 
                player.getHeight(), player.getWeight(), 
                player.getAppearances() != null ? player.getAppearances() : 0,
                player.getGoals() != null ? player.getGoals() : 0,
                player.getAssists() != null ? player.getAssists() : 0);
    }
    
    @Tool("查询最新的比赛安排或搜索特定对阵的时间和双方")
    public String getNextMatch(String keyword) {
        log.info("AI 正在搜索比赛: {}", keyword);
        Page<MatchVO> matchPage = matchService.searchMatches(1, 5, keyword);
        if (matchPage.getRecords().isEmpty()) {
            return "暂时没有找到关于 " + keyword + " 的比赛安排。";
        }
        return matchPage.getRecords().stream()
                .map(m -> String.format("[%s] %s vs %s, 时间: %s, 状态: %s", 
                        m.getCompetitionName(), 
                        m.getHomeTeam() != null ? m.getHomeTeam().getName() : "未知", 
                        m.getAwayTeam() != null ? m.getAwayTeam().getName() : "未知", 
                        m.getMatchTime(), 
                        m.getMatchStatus() == 1 ? "进行中" : (m.getMatchStatus() == 2 ? "已结束" : "未开始")))
                .collect(Collectors.joining("\n"));
    }
}
