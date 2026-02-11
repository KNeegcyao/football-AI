package com.soccer.forum.service.security.service;

import com.soccer.forum.service.security.service.impl.AuthServiceImpl;
import com.soccer.forum.service.security.model.LoginBody;
import com.soccer.forum.service.mapper.UserMapper;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.service.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginBody loginBody;

    @BeforeEach
    void setUp() {
        loginBody = new LoginBody();
        loginBody.setUsername("testUser");
        loginBody.setPassword("password123");
        loginBody.setNickname("TestUser");
    }

    @Test
    void register_Success() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> authService.register(loginBody));
        
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_DuplicateUsername_ThrowsException() {
        when(userMapper.selectCount(any())).thenReturn(1L);

        Exception exception = assertThrows(RuntimeException.class, () -> authService.register(loginBody));
        
        assertEquals("用户名已存在", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }
}
