package com.soccer.forum.service.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.PostVO;
import com.soccer.forum.domain.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminPostMapper extends BaseMapper<Post> {

    @Select("<script>" +
            "SELECT p.id, p.title, p.content, p.user_id as author_id, p.views, p.likes, p.comment_count, p.status, " +
            "p.topic_id, p.images, p.created_at, u.nickname as author_name, t.title as topic_name " +
            "FROM posts p " +
            "LEFT JOIN users u ON p.user_id = u.id " +
            "LEFT JOIN topics t ON p.topic_id = t.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR p.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null'>" +
            "AND p.status = #{status} " +
            "</if>" +
            "<if test='topicId != null'>" +
            "AND p.topic_id = #{topicId} " +
            "</if>" +
            "ORDER BY p.created_at DESC" +
            "</script>")
    Page<PostVO> selectPostPage(Page<PostVO> page, @Param("keyword") String keyword,
                                @Param("status") Integer status, @Param("topicId") Long topicId);

    @Update("UPDATE posts SET status = #{status} WHERE id = #{id}")
    int updatePostStatus(@Param("id") Long id, @Param("status") Integer status);
}
