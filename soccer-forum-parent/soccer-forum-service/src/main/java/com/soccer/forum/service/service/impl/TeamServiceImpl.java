package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.mapper.TeamMapper;
import com.soccer.forum.service.service.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 球队服务实现类
 * <p>
 * 实现球队信息的增删改查具体逻辑。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Service
public class TeamServiceImpl implements TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamServiceImpl.class);

    private final TeamMapper teamMapper;

    public TeamServiceImpl(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    /**
     * 创建球队实现
     * <p>
     * 初始化球队创建时间和更新时间，持久化到数据库。
     * </p>
     *
     * @param team 球队实体对象
     * @return 新创建球队的 ID
     */
    @Override
    public Long createTeam(Team team) {
        log.debug("创建球队: 名称={}", team.getName());
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        teamMapper.insert(team);
        log.info("球队创建成功: id={}", team.getId());
        return team.getId();
    }

    /**
     * 获取球队详情实现
     * <p>
     * 根据 ID 查询球队信息。
     * </p>
     *
     * @param id 球队 ID
     * @return 球队实体对象
     */
    @Override
    public Team getTeamDetail(Long id) {
        log.debug("获取球队详情: id={}", id);
        return teamMapper.selectById(id);
    }

    /**
     * 分页查询球队列表实现
     * <p>
     * 支持按球队名称或联赛名称进行模糊搜索。
     * </p>
     *
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @return 球队分页对象
     */
    @Override
    public Page<Team> listTeams(Integer page, Integer size, String keyword) {
        log.debug("分页查询球队: 页码={}, 大小={}, 关键词={}", page, size, keyword);
        Page<Team> teamPage = new Page<>(page, size);
        LambdaQueryWrapper<Team> query = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            query.like(Team::getName, keyword)
                 .or()
                 .like(Team::getLeague, keyword);
        }
        return teamMapper.selectPage(teamPage, query);
    }

    /**
     * 更新球队信息实现
     * <p>
     * 更新球队的基本信息和更新时间。
     * </p>
     *
     * @param id 球队 ID
     * @param team 更新后的球队实体对象
     * @throws RuntimeException 当球队不存在时抛出
     */
    @Override
    public void updateTeam(Long id, Team team) {
        log.debug("更新球队: id={}", id);
        team.setId(id);
        team.setUpdatedAt(LocalDateTime.now());
        int rows = teamMapper.updateById(team);
        if (rows == 0) {
            log.warn("球队更新失败, 未找到球队: id={}", id);
            throw new RuntimeException("未找到球队");
        }
        log.info("球队更新成功: id={}", id);
    }

    /**
     * 删除球队实现
     * <p>
     * 根据 ID 删除球队数据。
     * </p>
     *
     * @param id 球队 ID
     * @throws RuntimeException 当球队不存在时抛出
     */
    @Override
    public void deleteTeam(Long id) {
        log.debug("删除球队: id={}", id);
        int rows = teamMapper.deleteById(id);
        if (rows == 0) {
            log.warn("球队删除失败, 未找到球队: id={}", id);
            throw new RuntimeException("未找到球队");
        }
        log.info("球队删除成功: id={}", id);
    }
}
