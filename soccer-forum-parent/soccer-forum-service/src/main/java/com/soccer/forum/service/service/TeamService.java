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
