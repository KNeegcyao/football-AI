package com.soccer.forum.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soccer.forum.domain.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
