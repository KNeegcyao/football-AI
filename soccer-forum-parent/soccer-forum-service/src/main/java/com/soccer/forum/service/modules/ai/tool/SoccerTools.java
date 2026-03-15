package com.soccer.forum.service.modules.ai.tool;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 足球数据查询工具
 * 提供给 AI Agent 调用的函数，用于查询数据库或 API
 */
@Component
public class SoccerTools {

    @Tool("查询球队历史战绩，返回胜率和近期表现")
    public String getTeamHistory(String teamName) {
        // 模拟数据库查询
        if ("曼城".equals(teamName) || "Manchester City".equals(teamName)) {
            return "曼城历史战绩：胜率 75%，最近 5 场全胜。主教练：瓜迪奥拉。";
        } else if ("阿森纳".equals(teamName)) {
            return "阿森纳历史战绩：胜率 68%，最近 5 场 4 胜 1 平。主教练：阿尔特塔。";
        }
        return teamName + " 的历史战绩数据暂未收录。";
    }

    @Tool("查询球星个人数据，返回出场次数、进球数和助攻数")
    public String getPlayerStats(String playerName) {
        // 模拟数据库查询
        if ("哈兰德".equals(playerName)) {
            return "哈兰德本赛季数据：出场 25 次，进球 28 个，助攻 5 次。";
        } else if ("萨卡".equals(playerName)) {
            return "萨卡本赛季数据：出场 26 次，进球 12 个，助攻 10 次。";
        }
        return playerName + " 的个人数据暂未收录。";
    }
    
    @Tool("查询下一场比赛的对阵双方和时间")
    public String getNextMatch(String teamName) {
        return teamName + " 的下一场比赛将在本周六 20:00 进行，对手是利物浦。";
    }
}
