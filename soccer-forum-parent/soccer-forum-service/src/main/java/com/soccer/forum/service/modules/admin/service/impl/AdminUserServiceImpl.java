package com.soccer.forum.service.modules.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.service.modules.admin.dto.UserVO;
import com.soccer.forum.service.modules.admin.mapper.AdminUserMapper;
import com.soccer.forum.service.modules.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserMapper adminUserMapper;

    @Override
    public Page<UserVO> getUserList(int page, int size, String keyword, String role, Integer status) {
        Page<UserVO> pageParam = new Page<>(page, size);
        return adminUserMapper.selectUserPage(pageParam, keyword, role, status);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        adminUserMapper.updateUserStatus(id, status);
    }

    @Override
    public void updateUserRole(Long id, String role) {
        adminUserMapper.updateUserRole(id, role);
    }

    @Override
    public void deleteUser(Long id) {
        adminUserMapper.updateUserStatus(id, 0);
    }
}
