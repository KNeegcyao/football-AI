package com.soccer.forum.service.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.domain.enums.UserRole;
import com.soccer.forum.domain.enums.UserStatus;
import com.soccer.forum.service.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminUsername = "admin";
        String adminPassword = "admin123";
        String adminEmail = "admin@soccer-forum.com";

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, adminUsername);
        Long count = userMapper.selectCount(wrapper);

        if (count == 0) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setNickname("系统管理员");
            admin.setEmail(adminEmail);
            admin.setPhone("13800000000");
            admin.setAvatar("/static/default-avatar.png");
            admin.setBio("绿茵集系统管理员");
            admin.setRole(UserRole.ADMIN);
            admin.setStatus(UserStatus.NORMAL);
            admin.setLevel(1);
            admin.setExperience(0);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(admin);
            log.info("========================================");
            log.info("管理员账户初始化成功！");
            log.info("用户名: {}", adminUsername);
            log.info("密码: {}", adminPassword);
            log.info("邮箱: {}", adminEmail);
            log.info("========================================");
        } else {
            log.info("管理员账户已存在，跳过初始化");
        }
    }
}
