package com.soccer.forum.service.modules.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.domain.entity.Notification;
import java.util.List;

public interface NotificationService {
    /**
     * 发送通知
     */
    void sendNotification(Long userId, Long fromUserId, Integer type, Long targetId, String content);

    /**
     * 获取未读消息数量
     */
    long getUnreadCount(Long userId);

    /**
     * 获取分类未读消息数量
     */
    java.util.Map<Integer, Long> getUnreadCountByType(Long userId);

    /**
     * 分页查询通知
     */
    Page<Notification> getNotificationPage(Long userId, Integer page, Integer size, List<Integer> types);

    /**
     * 标记为已读
     */
    void markAsRead(Long id, Long userId);

    /**
     * 全部标记为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 根据类型将消息标记为已读
     */
    void markAsReadByType(Long userId, List<Integer> types);

    /**
     * 删除通知
     */
    void deleteNotification(Long id, Long userId);
}
