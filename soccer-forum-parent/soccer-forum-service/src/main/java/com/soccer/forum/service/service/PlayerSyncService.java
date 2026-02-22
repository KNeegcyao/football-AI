package com.soccer.forum.service.service;

import java.time.LocalDate;

/**
 * 球员数据同步服务接口
 * <p>
 * 负责与第三方体育数据 API (如 API-Football, Sportmonks) 进行数据同步。
 * 包括全量初始化和增量更新。
 * </p>
 */
public interface PlayerSyncService {

    /**
     * 全量同步指定球队的球员数据
     * <p>
     * 对应 API: GET /players?team={teamId}&season={season}
     * </p>
     *
     * @param teamId 本地球队ID
     * @param apiTeamId 第三方API球队ID
     * @param season 赛季年份 (e.g., 2025)
     * @return 同步结果描述
     */
    String syncTeamPlayers(Long teamId, Integer apiTeamId, Integer season);

    /**
     * 增量更新球员身价与状态
     * <p>
     * 建议每周运行一次
     * </p>
     */
    void syncMarketValuesAndStatus();

    /**
     * 同步比赛数据更新球员统计
     * <p>
     * 比赛结束后调用，累加进球、助攻等数据
     * </p>
     *
     * @param matchApiId 第三方比赛ID
     */
    void syncMatchStats(Integer matchApiId);

    /**
     * 从 SportAPI 同步单个球员数据
     * @param sportApiId SportAPI 球员ID
     * @return 同步结果消息
     */
    String syncPlayerFromSportApi(Integer sportApiId);

    /**
     * 从 SportAPI 获取单个球员数据 (测试用)
     * @param playerId SportAPI 球员ID
     * @return 原始 JSON 数据
     */
    /**
     * 获取球员详情（优先查本地，返回前端适配的JSON）
     *
     * @param id 球员数据库ID
     * @return JSON数据
     */
    com.fasterxml.jackson.databind.JsonNode getPlayerDetailJson(Long id);

    /**
     * 从 SportAPI 获取原始数据
     *
     * @param playerId API球员ID
     * @return 原始JSON
     */
    com.fasterxml.jackson.databind.JsonNode getPlayerFromSportApi(Integer playerId);

    /**
     * 从 SportAPI 获取球队球员列表 (实时调用)
     * @param teamId 本地球队ID
     * @return 原始 JSON 数据
     */
    com.fasterxml.jackson.databind.JsonNode getTeamPlayersFromSportApi(Long teamId);

    /**
     * 使用 Football-Data.org 同步指定豪门球队数据
     * @return 结果描述
     */
    String syncAllTeamsFromFootballData();

    /**
     * 同步联赛射手榜数据 (用于补充赛季进球/助攻/出场数据)
     * @return 结果描述
     */
    String syncLeagueScorers();
}
