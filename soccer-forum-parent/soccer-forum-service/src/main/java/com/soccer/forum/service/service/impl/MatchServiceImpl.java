package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Match;
import com.soccer.forum.service.mapper.MatchMapper;
import com.soccer.forum.service.service.MatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 赛事服务实现类
 * <p>
 * 实现赛事日程管理的具体业务逻辑。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Service
public class MatchServiceImpl implements MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchServiceImpl.class);

    private final MatchMapper matchMapper;

    public MatchServiceImpl(MatchMapper matchMapper) {
        this.matchMapper = matchMapper;
    }

    /**
     * 创建赛事实现
     * <p>
     * 初始化赛事创建时间和更新时间，持久化到数据库。
     * </p>
     *
     * @param match 赛事实体对象
     * @return 新创建赛事的 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMatch(Match match) {
        log.debug("创建赛事: 主队ID={} vs 客队ID={}", match.getHomeTeamId(), match.getAwayTeamId());
        match.setCreatedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());
        matchMapper.insert(match);
        log.info("赛事创建成功: id={}", match.getId());
        return match.getId();
    }

    /**
     * 获取赛事详情实现
     * <p>
     * 根据 ID 查询赛事信息。
     * </p>
     *
     * @param id 赛事 ID
     * @return 赛事实体对象
     */
    @Override
    public Match getMatchDetail(Long id) {
        log.debug("获取赛事详情: id={}", id);
        Match match = matchMapper.selectById(id);
        if (match == null) {
            log.warn("赛事详情查询失败, 未找到赛事: id={}", id);
            throw new ServiceException(ServiceErrorCode.DATA_NOT_FOUND);
        }
        return match;
    }

    /**
     * 分页查询赛事列表实现
     * <p>
     * 支持按赛事名称和状态筛选，按比赛时间正序排列。
     * </p>
     *
     * @param page 页码
     * @param size 每页大小
     * @param competition 赛事名称
     * @param status 比赛状态
     * @return 赛事分页对象
     */
    @Override
    public Page<Match> listMatches(Integer page, Integer size, String competition, Integer status) {
        log.debug("分页查询赛事: 页码={}, 大小={}, 赛事={}, 状态={}", page, size, competition, status);
        Page<Match> matchPage = new Page<>(page, size);
        LambdaQueryWrapper<Match> query = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(competition)) {
            query.eq(Match::getCompetitionName, competition);
        }
        
        if (status != null) {
            query.eq(Match::getStatus, status);
        }
        
        query.orderByAsc(Match::getMatchTime);
        return matchMapper.selectPage(matchPage, query);
    }

    /**
     * 按日期查询赛事实现
     * <p>
     * 获取指定日期的全天比赛列表。
     * </p>
     *
     * @param date 查询日期
     * @return 赛事列表
     */
    @Override
    public List<Match> getMatchesByDate(LocalDate date) {
        log.debug("按日期查询赛事: 日期={}", date);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        return matchMapper.selectList(new LambdaQueryWrapper<Match>()
                .between(Match::getMatchTime, startOfDay, endOfDay)
                .orderByAsc(Match::getMatchTime));
    }

    /**
     * 更新赛事信息实现
     * <p>
     * 更新赛事的基本信息和更新时间。
     * </p>
     *
     * @param id 赛事 ID
     * @param match 更新后的赛事实体对象
     * @throws ServiceException 当赛事不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMatch(Long id, Match match) {
        log.debug("更新赛事: id={}", id);
        match.setId(id);
        match.setUpdatedAt(LocalDateTime.now());
        int rows = matchMapper.updateById(match);
        if (rows == 0) {
            log.warn("赛事更新失败, 未找到赛事: id={}", id);
            throw new ServiceException(ServiceErrorCode.DATA_NOT_FOUND);
        }
        log.info("赛事更新成功: id={}", id);
    }

    /**
     * 删除赛事实现
     * <p>
     * 根据 ID 删除赛事数据。
     * </p>
     *
     * @param id 赛事 ID
     * @throws ServiceException 当赛事不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMatch(Long id) {
        log.debug("删除赛事: id={}", id);
        int rows = matchMapper.deleteById(id);
        if (rows == 0) {
            log.warn("赛事删除失败, 未找到赛事: id={}", id);
            throw new ServiceException(ServiceErrorCode.DATA_NOT_FOUND);
        }
        log.info("赛事删除成功: id={}", id);
    }
}
