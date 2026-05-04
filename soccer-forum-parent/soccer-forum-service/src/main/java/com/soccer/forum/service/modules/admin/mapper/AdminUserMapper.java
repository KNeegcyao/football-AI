package com.soccer.forum.service.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.UserVO;
import com.soccer.forum.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminUserMapper extends BaseMapper<User> {

    @Select("<script>" +
            "SELECT id, username, nickname, email, phone, avatar, role, status, level, experience, created_at, updated_at " +
            "FROM users " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='role != null and role != \"\"'>" +
            "AND role = #{role} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    Page<UserVO> selectUserPage(Page<UserVO> page, @Param("keyword") String keyword,
                                @Param("role") String role, @Param("status") Integer status);

    @Update("UPDATE users SET status = #{status} WHERE id = #{id}")
    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE users SET role = #{role} WHERE id = #{id}")
    int updateUserRole(@Param("id") Long id, @Param("role") String role);
}
