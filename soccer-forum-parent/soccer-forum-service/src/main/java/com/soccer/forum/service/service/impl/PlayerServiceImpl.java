package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Player;
import com.soccer.forum.service.mapper.PlayerMapper;
import com.soccer.forum.service.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 球员服务实现类
 * <p>
 * 实现球员管理的具体业务逻辑。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final PlayerMapper playerMapper;

    public PlayerServiceImpl(PlayerMapper playerMapper) {
        this.playerMapper = playerMapper;
    }

    /**
     * 创建球员实现
     * <p>
     * 初始化球员创建时间和更新时间，持久化到数据库。
     * </p>
     *
     * @param player 球员实体对象
     * @return 新创建球员的 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlayer(Player player) {
        log.debug("创建球员: 姓名={}", player.getName());
        player.setCreatedAt(LocalDateTime.now());
        player.setUpdatedAt(LocalDateTime.now());
        playerMapper.insert(player);
        log.info("球员创建成功: id={}", player.getId());
        return player.getId();
    }

    /**
     * 获取球员详情实现
     * <p>
     * 根据 ID 查询球员信息。
     * </p>
     *
     * @param id 球员 ID
     * @return 球员实体对象
     */
    @Override
    public Player getPlayerDetail(Long id) {
        log.debug("获取球员详情: id={}", id);
        Player player = playerMapper.selectById(id);
        if (player == null) {
            log.warn("球员详情查询失败, 未找到球员: id={}", id);
            throw new ServiceException(ServiceErrorCode.DATA_NOT_FOUND);
        }
        return player;
    }

    /**
     * 分页查询球员列表实现
     * <p>
     * 支持按名称、国籍、位置模糊搜索，可按球队 ID 筛选。
     * </p>
     *
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param teamId 球队 ID
     * @return 球员分页对象
     */
    @Override
    public Page<Player> listPlayers(Integer page, Integer size, String keyword, Long teamId) {
        log.debug("分页查询球员: 页码={}, 大小={}, 关键词={}, 球队ID={}", page, size, keyword, teamId);
        Page<Player> playerPage = new Page<>(page, size);
        LambdaQueryWrapper<Player> query = new LambdaQueryWrapper<>();
        
        if (teamId != null) {
            query.eq(Player::getCurrentTeamId, teamId);
        }
        
        if (StringUtils.hasText(keyword)) {
            query.and(q -> q.like(Player::getName, keyword)
                          .or()
                          .like(Player::getNationality, keyword)
                          .or()
                          .like(Player::getPosition, keyword));
        }
        return playerMapper.selectPage(playerPage, query);
    }

    /**
     * 获取球队球员列表实现
     * <p>
     * 查询指定球队下的所有球员。
     * </p>
     *
     * @param teamId 球队 ID
     * @return 球员列表
     */
    @Override
    public List<Player> getPlayersByTeam(Long teamId) {
        log.debug("查询球队球员: 球队ID={}", teamId);
        return playerMapper.selectList(new LambdaQueryWrapper<Player>()
                .eq(Player::getCurrentTeamId, teamId));
    }

    /**
     * 更新球员信息实现
     * <p>
     * 更新球员的基本信息和更新时间。
     * </p>
     *
     * @param id 球员 ID
     * @param player 更新后的球员实体对象
     * @throws ServiceException 当球员不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlayer(Long id, Player player) {
        log.debug("更新球员: id={}", id);
        player.setId(id);
        player.setUpdatedAt(LocalDateTime.now());
        int rows = playerMapper.updateById(player);
        if (rows == 0) {
            log.warn("球员更新失败, 未找到球员: id={}", id);
            throw new ServiceException(ServiceErrorCode.DATA_NOT_FOUND);
        }
        log.info("球员更新成功: id={}", id);
    }

    /**
     * 删除球员实现
     * <p>
     * 根据 ID 删除球员数据。
     * </p>
     *
     * @param id 球员 ID
     * @throws ServiceException 当球员不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlayer(Long id) {
        log.debug("删除球员: id={}", id);
        int rows = playerMapper.deleteById(id);
        if (rows == 0) {
            log.warn("球员删除失败, 未找到球员: id={}", id);
            throw new ServiceException(ServiceErrorCode.DATA_NOT_FOUND);
        }
        log.info("球员删除成功: id={}", id);
    }
}
