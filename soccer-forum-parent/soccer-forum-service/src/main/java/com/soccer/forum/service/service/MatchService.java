package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Match;

import java.time.LocalDate;
import java.util.List;

/**
 * 赛事服务接口
 * <p>
 * 定义赛事日程管理的核心业务逻辑，包括创建、查询、更新和删除。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
public interface MatchService {

    /**
     * 创建赛事
     *
     * @param match 赛事实体对象
     * @return 新创建赛事的 ID
     */
    Long createMatch(Match match);

    /**
     * 根据 ID 获取赛事详情
     *
     * @param id 赛事 ID
     * @return 赛事实体对象，若不存在返回 null
     */
    Match getMatchDetail(Long id);

    /**
     * 分页查询赛事列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param competition 赛事名称（可选）
     * @param status 比赛状态（可选）
     * @return 赛事分页对象
     */
    Page<Match> listMatches(Integer page, Integer size, String competition, Integer status);

    /**
     * 根据日期查询赛事
     *
     * @param date 查询日期
     * @return 当日赛事列表
     */
    List<Match> getMatchesByDate(LocalDate date);

    /**
     * 更新赛事信息
     *
     * @param id 赛事 ID
     * @param match 更新后的赛事实体对象
     */
    void updateMatch(Long id, Match match);

    /**
     * 删除赛事
     *
     * @param id 赛事 ID
     */
    void deleteMatch(Long id);
}
