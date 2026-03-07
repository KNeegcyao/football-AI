package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.domain.entity.Team;
import com.soccer.forum.service.mapper.TeamFollowMapper;
import com.soccer.forum.service.mapper.TeamMapper;
import com.soccer.forum.service.service.TeamService;
import com.soccer.forum.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Random;

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
    private final TeamFollowMapper teamFollowMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public TeamServiceImpl(TeamMapper teamMapper, TeamFollowMapper teamFollowMapper, RedisTemplate<String, Object> redisTemplate) {
        this.teamMapper = teamMapper;
        this.teamFollowMapper = teamFollowMapper;
        this.redisTemplate = redisTemplate;
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
    @Transactional(rollbackFor = Exception.class)
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
        Team team = teamMapper.selectById(id);
        if (team == null) {
            log.warn("球队详情查询失败, 未找到球队: id={}", id);
            throw new ServiceException(ServiceErrorCode.DATA_NOT_FOUND);
        }
        
        populateTeamStats(team);
        
        return team;
    }

    private void populateTeamStats(Team team) {
        if (team == null) return;
        
        // 填充关注者数量
        Long followerCount = teamFollowMapper.selectCount(new LambdaQueryWrapper<com.soccer.forum.domain.entity.TeamFollow>()
                .eq(com.soccer.forum.domain.entity.TeamFollow::getTeamId, team.getId()));
        team.setFollowerCount(followerCount.intValue());
        
        // 填充在线人数
        team.setOnlineCount(getOnlineCount(team.getId()));
    }

    @Override
    public void updateOnlineCount(Long teamId, Long userId, boolean isOnline) {
        String key = "team:online:set:" + teamId;
        if (isOnline) {
            redisTemplate.opsForSet().add(key, userId.toString());
            // 设置过期时间为 1 小时，防止死数据，实际应该由心跳或退出维护
            redisTemplate.expire(key, java.time.Duration.ofHours(1));
        } else {
            redisTemplate.opsForSet().remove(key, userId.toString());
        }
    }

    @Override
    public int getOnlineCount(Long teamId) {
        String key = "team:online:set:" + teamId;
        Long size = redisTemplate.opsForSet().size(key);
        int realCount = size != null ? size.intValue() : 0;
        
        // 兜底策略：如果真实人数为0，且有关注者，则返回一个模拟的小数值，否则返回真实值
        if (realCount == 0) {
            Long followerCount = teamFollowMapper.selectCount(new LambdaQueryWrapper<com.soccer.forum.domain.entity.TeamFollow>()
                    .eq(com.soccer.forum.domain.entity.TeamFollow::getTeamId, teamId));
            if (followerCount > 0) {
                return new Random().nextInt(Math.max(1, followerCount.intValue() / 10)) + 1;
            }
        }
        return realCount;
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
        return listTeams(page, size, keyword, null);
    }

    @Override
    public Page<Team> listTeams(Integer page, Integer size, String keyword, Boolean isHot) {
        log.debug("分页查询球队: 页码={}, 大小={}, 关键词={}, 热门={}", page, size, keyword, isHot);
        Page<Team> teamPage = new Page<>(page, size);
        LambdaQueryWrapper<Team> query = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            query.and(q -> q.like(Team::getName, keyword)
                          .or()
                          .like(Team::getLeague, keyword));
        }
        
        if (isHot != null && isHot) {
            query.eq(Team::getIsHot, true);
        }
        
        Page<Team> result = teamMapper.selectPage(teamPage, query);
        result.getRecords().forEach(this::populateTeamStats);
        
        return result;
    }

    /**
     * 获取热门球队列表
     *
     * @return 热门球队列表
     */
    @Override
    public java.util.List<Team> listHotTeams() {
        java.util.List<Team> teams = teamMapper.selectList(new LambdaQueryWrapper<Team>()
                .eq(Team::getIsHot, true)
                .orderByDesc(Team::getTotalMatches));
        teams.forEach(this::populateTeamStats);
        return teams;
    }

    /**
     * 获取推荐球队列表（首页显示）
     *
     * @return 推荐球队列表
     */
    @Override
    public java.util.List<Team> listRecommendTeams() {
        java.util.List<Team> teams = teamMapper.selectList(new LambdaQueryWrapper<Team>()
                .eq(Team::getIsRecommend, true));
        teams.forEach(this::populateTeamStats);
        return teams;
    }


    @Override
    public java.util.List<Team> getTeamsByNames(java.util.List<String> names) {
        log.debug("根据名称获取球队: names={}", names);
        if (names == null || names.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        LambdaQueryWrapper<Team> query = new LambdaQueryWrapper<>();
        query.in(Team::getName, names);
        java.util.List<Team> teams = teamMapper.selectList(query);
        teams.forEach(this::populateTeamStats);
        return teams;
    }

    /**
     * 更新球队信息实现
     * <p>
     * 更新球队的基本信息和更新时间。
     * </p>
     *
     * @param id 球队 ID
     * @param team 更新后的球队实体对象
     * @throws ServiceException 当球队不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTeam(Long id, Team team) {
        log.debug("更新球队: id={}", id);
        team.setId(id);
        team.setUpdatedAt(LocalDateTime.now());
        int rows = teamMapper.updateById(team);
        if (rows == 0) {
            log.warn("球队更新失败, 未找到球队: id={}", id);
            throw new ServiceException(ServiceErrorCode.DATA_NOT_FOUND);
        }
        log.info("球队更新成功: id={}", id);
    }

    /**
     * 删除球队实现
     * <p>
     * 根据 ID 删除球队信息。
     * </p>
     *
     * @param id 球队 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeam(Long id) {
        log.debug("删除球队: id={}", id);
        int rows = teamMapper.deleteById(id);
        if (rows == 0) {
            log.warn("球队删除失败, 未找到球队: id={}", id);
            throw new ServiceException(ServiceErrorCode.DATA_NOT_FOUND);
        }
        log.info("球队删除成功: id={}", id);
    }
}
