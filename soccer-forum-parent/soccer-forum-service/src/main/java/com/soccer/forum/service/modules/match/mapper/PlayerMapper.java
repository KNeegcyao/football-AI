package com.soccer.forum.service.modules.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soccer.forum.domain.entity.Player;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlayerMapper extends BaseMapper<Player> {

    @Select("SELECT * FROM players WHERE current_team_id = #{teamId} ORDER BY jersey_number ASC")
    List<Player> selectByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT * FROM players WHERE api_id = #{apiId} LIMIT 1")
    Player selectByApiId(@Param("apiId") Integer apiId);

    @Select("SELECT * FROM players WHERE id = #{id} LIMIT 1")
    Player selectByPrimaryKey(@Param("id") Long id);
}
