package com.soccer.forum.service.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.CommentVO;
import com.soccer.forum.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminCommentMapper extends BaseMapper<Comment> {

    @Select("<script>" +
            "SELECT c.id, c.post_id, c.user_id as author_id, c.parent_id, c.content, c.likes, c.status, c.created_at, " +
            "u.nickname as author_name, p.title as post_title " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "LEFT JOIN posts p ON c.post_id = p.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND c.content LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "<if test='postId != null'>" +
            "AND c.post_id = #{postId} " +
            "</if>" +
            "ORDER BY c.created_at DESC" +
            "</script>")
    Page<CommentVO> selectCommentPage(Page<CommentVO> page, @Param("keyword") String keyword,
                                      @Param("postId") Long postId);

    @Update("UPDATE comments SET status = 1 WHERE id = #{id}")
    int deleteComment(@Param("id") Long id);
}
