package com.soccer.forum.service.modules.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soccer.forum.domain.entity.Match;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MatchMapper extends BaseMapper<Match> {

    @Select("SELECT *, status as matchStatus FROM matches WHERE match_time BETWEEN #{startTime} AND #{endTime} ORDER BY match_time ASC")
    List<Match> selectMatchesByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
