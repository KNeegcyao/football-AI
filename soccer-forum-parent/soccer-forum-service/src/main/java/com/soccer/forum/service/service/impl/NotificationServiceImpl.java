package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Notification;
import com.soccer.forum.service.mapper.NotificationMapper;
import com.soccer.forum.service.service.NotificationService;
import com.soccer.forum.service.service.UserRelationshipService;
import com.soccer.forum.service.service.UserService;
import com.soccer.forum.domain.entity.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserRelationshipService userRelationshipService;
    private final UserService userService;

    public NotificationServiceImpl(NotificationMapper notificationMapper, 
                                   @Lazy UserRelationshipService userRelationshipService,
                                   @Lazy UserService userService) {
        this.notificationMapper = notificationMapper;
        this.userRelationshipService = userRelationshipService;
        this.userService = userService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long userId, Long fromUserId, Integer type, Long targetId, String content) {
        // 如果是自己触发的通知，不发送（比如自己给自己点赞/评论）
        if (userId.equals(fromUserId)) {
            return;
        }

        // 消息提醒过滤逻辑 (仅针对回复/评论/@提及 类型: 3, 4, 6)
        if (type == 3 || type == 4 || type == 6) {
            User targetUser = userService.getById(userId);
            if (targetUser != null && targetUser.getReplyNotificationType() != null) {
                String setting = targetUser.getReplyNotificationType();
                if ("none".equals(setting)) {
                    return; // 关闭提醒
                } else if ("following".equals(setting)) {
                    // 仅接收关注的人的提醒
                    if (!userRelationshipService.isFollowing(userId, fromUserId)) {
                        return; // 目标用户未关注发送者，过滤
                    }
                }
            }
        }

        // 防重检查：如果同一个用户在短时间内（例如24小时内）对同一个目标已经发送过同类型的通知，则更新时间并设为未读，不再新增
        // 特别是关注通知（type=5），防止反复 关注-取关 产生大量垃圾通知
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getFromUserId, fromUserId)
                .eq(Notification::getType, type)
                .eq(Notification::getTargetId, targetId)
                .ge(Notification::getCreatedAt, LocalDateTime.now().minusDays(1)); // 24小时内

        List<Notification> existingNotifications = notificationMapper.selectList(queryWrapper);
        if (!existingNotifications.isEmpty()) {
            Notification existing = existingNotifications.get(0);
            existing.setIsRead(0);
            existing.setCreatedAt(LocalDateTime.now());
            if (content != null) {
                existing.setContent(content);
            }
            notificationMapper.updateById(existing);
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setContent(content);
        notification.setIsRead(0);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    @Override
    public long getUnreadCount(Long userId) {
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        return count != null ? count : 0L;
    }

    @Override
    public Page<Notification> getNotificationPage(Long userId, Integer page, Integer size, List<Integer> types) {
        Page<Notification> p = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId);
        
        if (types != null && !types.isEmpty()) {
            wrapper.in(Notification::getType, types);
        }
        
        return notificationMapper.selectPage(p, wrapper.orderByDesc(Notification::getCreatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new ServiceException(ServiceErrorCode.DATA_NOT_FOUND);
        }
        if (!notification.getUserId().equals(userId)) {
            throw new ServiceException(ServiceErrorCode.FORBIDDEN);
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        Notification notification = new Notification();
        notification.setIsRead(1);
        notificationMapper.update(notification, new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            return;
        }
        if (!notification.getUserId().equals(userId)) {
            throw new ServiceException(ServiceErrorCode.FORBIDDEN);
        }
        notificationMapper.deleteById(id);
    }
}
