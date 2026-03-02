package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.ExperienceRecord;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.mapper.ExperienceRecordMapper;
import com.soccer.forum.service.mapper.UserMapper;
import com.soccer.forum.service.service.ExperienceService;
import com.soccer.forum.service.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 经验值与等级服务实现类
 */
@Slf4j
@Service
public class ExperienceServiceImpl implements ExperienceService {

    private final UserMapper userMapper;
    private final ExperienceRecordMapper experienceRecordMapper;
    private final NotificationService notificationService;

    // 等级经验配置 (每级所需总经验 = 等级 * 100)
    // Lv1: 0, Lv2: 100, Lv3: 200, Lv4: 300, Lv5: 400
    private static final int EXP_PER_LEVEL = 100;
    private static final int MAX_LEVEL = 5;

    public ExperienceServiceImpl(UserMapper userMapper, ExperienceRecordMapper experienceRecordMapper, NotificationService notificationService) {
        this.userMapper = userMapper;
        this.experienceRecordMapper = experienceRecordMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addExperience(Long userId, int amount, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("增加经验失败: 用户不存在, userId={}", userId);
            return;
        }

        int currentExp = user.getExperience() == null ? 0 : user.getExperience();
        int currentLevel = user.getLevel() == null ? 1 : user.getLevel();
        
        int newExp = currentExp + amount;
        user.setExperience(newExp);
        
        log.info("用户经验增加: userId={}, amount={}, reason={}, currentExp={}, newExp={}", 
                userId, amount, reason, currentExp, newExp);

        // 升级检查
        checkLevelUp(user, currentLevel, newExp);
        
        userMapper.updateById(user);

        // 保存流水
        ExperienceRecord record = ExperienceRecord.builder()
                .userId(userId)
                .amount(amount)
                .reason(reason)
                .build();
        experienceRecordMapper.insert(record);
    }

    @Override
    public List<ExperienceRecord> getRecordsByUserId(Long userId) {
        return experienceRecordMapper.selectList(new LambdaQueryWrapper<ExperienceRecord>()
                .eq(ExperienceRecord::getUserId, userId)
                .orderByDesc(ExperienceRecord::getCreatedAt));
    }

    @Override
    public Page<ExperienceRecord> getRecordsPageByUserId(Long userId, int page, int size) {
        return experienceRecordMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<ExperienceRecord>()
                        .eq(ExperienceRecord::getUserId, userId)
                        .orderByDesc(ExperienceRecord::getCreatedAt));
    }

    private void checkLevelUp(User user, int currentLevel, int newExp) {
        if (currentLevel >= MAX_LEVEL) {
            return;
        }

        // 计算当前等级升级所需总经验
        // 规则: 升到第 L 级需要 (L-1)*100 总经验
        // 例如: 升到 Lv2 需要 100, Lv3 需要 200, Lv4 需要 300, Lv5 需要 400
        int nextLevel = currentLevel + 1;
        int nextLevelExpRequired = (nextLevel - 1) * EXP_PER_LEVEL;

        if (newExp >= nextLevelExpRequired) {
            user.setLevel(nextLevel);
            log.info("用户升级: userId={}, oldLevel={}, newLevel={}", user.getId(), currentLevel, nextLevel);
            
            // 发送系统通知 (类型 1 代表系统通知/等级奖励)
            String title = getLevelTitle(nextLevel);
            notificationService.sendNotification(user.getId(), 0L, 1, user.getId(), 
                    String.format("恭喜！您已升级到 LV.%d，获得称号：%s", nextLevel, title));
            
            // 递归检查是否能连升多级
            checkLevelUp(user, nextLevel, newExp);
        }
    }

    private String getLevelTitle(int level) {
        switch (level) {
            case 1: return "新秀观察员 | 足球爱好者";
            case 2: return "战术研究员 | 比赛分析者";
            case 3: return "资深分析师 | 绿茵智囊";
            case 4: return "战术大师 | 绿茵战略家";
            case 5: return "战术宗师 | 足球哲学家";
            default: return "新秀观察员";
        }
    }

    @Override
    public void addPostExperience(Long userId) {
        addExperience(userId, 10, "发布帖子");
    }

    @Override
    public void addLikeExperience(Long userId) {
        addExperience(userId, 5, "获得点赞");
    }

    @Override
    public void addCommentExperience(Long userId) {
        addExperience(userId, 2, "发布评论");
    }
}
