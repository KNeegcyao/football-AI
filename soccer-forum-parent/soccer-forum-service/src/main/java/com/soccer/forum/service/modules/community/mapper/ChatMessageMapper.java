package com.soccer.forum.service.modules.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.ChatMessage; 
import org.apache.ibatis.annotations.Mapper;       
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY created_at DESC")
    IPage<ChatMessage> getSessionMessagesBySessionId(Page<ChatMessage> page, @Param("sessionId") Long sessionId);

    @Update("UPDATE chat_message SET status = 1 WHERE session_id = #{sessionId} AND receiver_id = #{receiverId} AND status = 0")
    void markAsReadByReceiver(@Param("sessionId") Long sessionId, @Param("receiverId") Long receiverId);
}
