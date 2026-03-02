package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.ExperienceRecord;
import java.util.List;

/**
 * 经验值与等级服务接口
 */
public interface ExperienceService {
    
    /**
     * 增加经验值
     * @param userId 用户ID
     * @param amount 增加数量
     * @param reason 增加原因
     */
    void addExperience(Long userId, int amount, String reason);
    
    /**
     * 获取用户的经验流水
     */
    List<ExperienceRecord> getRecordsByUserId(Long userId);

    /**
     * 分页获取用户的经验流水
     */
    Page<ExperienceRecord> getRecordsPageByUserId(Long userId, int page, int size);
    
    /**
     * 发帖增加经验 (+10)
     */
    void addPostExperience(Long userId);
    
    /**
     * 获赞增加经验 (+5)
     */
    void addLikeExperience(Long userId);
    
    /**
     * 评论增加经验 (+2)
     */
    void addCommentExperience(Long userId);
}
