package com.soccer.forum.service.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.soccer.forum.common.R;
import com.soccer.forum.service.modules.admin.dto.UserVO;
import com.soccer.forum.service.modules.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public R<Page<UserVO>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        return R.ok(adminUserService.getUserList(page, size, keyword, role, status));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminUserService.updateUserStatus(id, body.get("status"));
        return R.ok();
    }

    @PutMapping("/{id}/role")
    public R<Void> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminUserService.updateUserRole(id, body.get("role"));
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return R.ok();
    }
}
