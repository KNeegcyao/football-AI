package com.soccer.forum.service.modules.admin.mapper;

import com.soccer.forum.service.modules.admin.dto.DailyTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminDashboardMapper {

    @Select("SELECT COUNT(*) FROM users WHERE status = 1")
    Long countActiveUsers();

    @Select("SELECT COUNT(*) FROM posts WHERE status = 0")
    Long countActivePosts();

    @Select("SELECT COUNT(*) FROM comments WHERE status = 0")
    Long countComments();

    @Select("SELECT COUNT(*) FROM topics")
    Long countTopics();

    @Select("SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE() AND status = 1")
    Long countTodayNewUsers();

    @Select("SELECT COUNT(*) FROM posts WHERE DATE(created_at) = CURDATE() AND status = 0")
    Long countTodayNewPosts();

    @Select("SELECT COUNT(*) FROM comments WHERE DATE(created_at) = CURDATE() AND status = 0")
    Long countTodayNewComments();

    List<DailyTrendVO> getDailyTrend(@Param("days") int days);
}
