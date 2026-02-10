package com.soccer.forum.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soccer.forum.domain.entity.Team;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeamMapper extends BaseMapper<Team> {
}
