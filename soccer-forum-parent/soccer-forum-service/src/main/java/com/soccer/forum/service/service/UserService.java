package com.soccer.forum.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.model.dto.UserInfoResp;
import com.soccer.forum.service.model.dto.UserStatsResp;
import com.soccer.forum.service.model.dto.UserUpdateReq;

/**
 * 用户管理服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 获取当前登录用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoResp getUserInfo(Long userId);

    /**
     * 获取用户统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    UserStatsResp getUserStats(Long userId);

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param req 更新请求
     */
    void updateUserInfo(Long userId, UserUpdateReq req);
}
