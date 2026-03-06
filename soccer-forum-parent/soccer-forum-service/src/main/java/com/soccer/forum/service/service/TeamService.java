package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Team;

/**
 * 球队服务接口
 * <p>
 * 定义球队信息管理的核心业务逻辑，包括增删改查。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
public interface TeamService {

    /**
     * 创建球队
     *
     * @param team 球队实体对象
     * @return 新创建球队的 ID
     */
    Long createTeam(Team team);

    /**
     * 根据 ID 获取球队详情
     *
     * @param id 球队 ID
     * @return 球队实体对象，若不存在返回 null
     */
    Team getTeamDetail(Long id);

    /**
     * 分页查询球队列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词（名称或联赛）
     * @return 球队分页对象
     */
    Page<Team> listTeams(Integer page, Integer size, String keyword);

    /**
     * 分页查询球队列表（支持热门筛选）
     *
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param isHot 是否热门
     * @return 球队分页对象
     */
    Page<Team> listTeams(Integer page, Integer size, String keyword, Boolean isHot);

    /**
     * 获取热门球队列表
     *
     * @return 热门球队列表
     */
    java.util.List<Team> listHotTeams();

    /**
     * 获取推荐球队列表（首页显示）
     *
     * @return 推荐球队列表
     */
    java.util.List<Team> listRecommendTeams();


    /**
     * 根据名称列表获取球队
     *
     * @param names 球队名称列表
     * @return 球队列表
     */
    java.util.List<Team> getTeamsByNames(java.util.List<String> names);

    /**
     * 更新球队信息
     *
     * @param id 球队 ID
     * @param team 更新后的球队实体对象
     */
    void updateTeam(Long id, Team team);

    /**
     * 删除球队
     *
     * @param id 球队 ID
     */
    void deleteTeam(Long id);
}
