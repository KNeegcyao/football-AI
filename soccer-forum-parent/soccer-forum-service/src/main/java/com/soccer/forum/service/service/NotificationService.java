package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Notification;

public interface NotificationService {
    /**
     * 发送通知
     */
    void sendNotification(Long userId, Long fromUserId, Integer type, Long targetId, String content);

    /**
     * 获取未读消息数
     */
    long getUnreadCount(Long userId);

    /**
     * 分页查询通知
     */
    Page<Notification> getNotificationPage(Long userId, Integer page, Integer size);

    /**
     * 标记为已读
     */
    void markAsRead(Long id, Long userId);

    /**
     * 全部标记为已读
     */
    void markAllAsRead(Long userId);
}
