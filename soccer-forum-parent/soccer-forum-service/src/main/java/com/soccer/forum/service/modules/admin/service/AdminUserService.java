package com.soccer.forum.service.modules.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.UserVO;

public interface AdminUserService {
    Page<UserVO> getUserList(int page, int size, String keyword, String role, Integer status);
    void updateUserStatus(Long id, Integer status);
    void updateUserRole(Long id, String role);
    void deleteUser(Long id);
}
