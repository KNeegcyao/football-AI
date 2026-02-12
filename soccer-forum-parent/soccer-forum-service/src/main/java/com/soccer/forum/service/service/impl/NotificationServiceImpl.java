package com.soccer.forum.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.enums.ServiceErrorCode;
import com.soccer.forum.common.exception.ServiceException;
import com.soccer.forum.domain.entity.Notification;
import com.soccer.forum.service.mapper.NotificationMapper;
import com.soccer.forum.service.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long userId, Long fromUserId, Integer type, Long targetId, String content) {
        // 如果是自己触发的通知，不发送（比如自己给自己点赞/评论）
        if (userId.equals(fromUserId)) {
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
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public Page<Notification> getNotificationPage(Long userId, Integer page, Integer size) {
        Page<Notification> p = new Page<>(page, size);
        return notificationMapper.selectPage(p, new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt));
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
}
