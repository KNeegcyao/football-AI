package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Player;

import java.util.List;

/**
 * 球员服务接口
 * <p>
 * 定义球员信息管理的核心业务逻辑，包括增删改查及按球队筛选。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
public interface PlayerService {

    /**
     * 创建球员
     *
     * @param player 球员实体对象
     * @return 新创建球员的 ID
     */
    Long createPlayer(Player player);

    /**
     * 根据 ID 获取球员详情
     *
     * @param id 球员 ID
     * @return 球员实体对象，若不存在返回 null
     */
    Player getPlayerDetail(Long id);

    /**
     * 分页查询球员列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词（名称、国籍、位置）
     * @param teamId 球队 ID（可选，用于筛选）
     * @return 球员分页对象
     */
    Page<Player> listPlayers(Integer page, Integer size, String keyword, Long teamId);

    /**
     * 获取球队的所有球员
     *
     * @param teamId 球队 ID
     * @return 球员列表
     */
    List<Player> getPlayersByTeam(Long teamId);

    /**
     * 更新球员信息
     *
     * @param id 球员 ID
     * @param player 更新后的球员实体对象
     */
    void updatePlayer(Long id, Player player);

    /**
     * 删除球员
     *
     * @param id 球员 ID
     */
    void deletePlayer(Long id);
}
