package com.soccer.forum.service.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.domain.enums.UserRole;
import com.soccer.forum.domain.enums.UserStatus;
import com.soccer.forum.service.mapper.UserMapper;
import com.soccer.forum.service.security.model.LoginBody;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.security.service.AuthService;
import com.soccer.forum.service.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 认证服务实现类
 * <p>
 * 实现用户登录认证、注册及第三方登录的具体业务逻辑。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager, 
                           JwtUtils jwtUtils, 
                           UserMapper userMapper, 
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录实现
     * <p>
     * 使用 Spring Security 的 AuthenticationManager 进行认证。
     * </p>
     *
     * @param loginBody 登录请求参数
     * @return JWT 令牌
     * @throws RuntimeException 当用户名或密码错误时抛出
     */
    @Override
    public String login(LoginBody loginBody) {
        log.debug("尝试认证用户: {}", loginBody.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginBody.getUsername(), loginBody.getPassword())
            );
            
            log.debug("用户认证成功: {}", loginBody.getUsername());
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            return jwtUtils.generateToken(loginUser);
        } catch (AuthenticationException e) {
            log.warn("用户认证失败: {}", loginBody.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }
    }

    /**
     * 用户注册实现
     * <p>
     * 检查用户名唯一性，加密密码，设置默认角色和状态，保存用户信息。
     * </p>
     *
     * @param loginBody 注册请求参数
     * @throws RuntimeException 当用户名已存在时抛出
     */
    @Override
    public void register(LoginBody loginBody) {
        log.debug("尝试注册用户: {}", loginBody.getUsername());
        
        // 检查用户是否存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginBody.getUsername()));
        if (count > 0) {
            log.warn("注册失败: 用户名 {} 已存在", loginBody.getUsername());
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(loginBody.getUsername());
        user.setPassword(passwordEncoder.encode(loginBody.getPassword()));
        user.setNickname(loginBody.getNickname() != null ? loginBody.getNickname() : "User_" + System.currentTimeMillis());
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.NORMAL);
        
        userMapper.insert(user);
        log.info("用户注册成功: id={}, username={}", user.getId(), user.getUsername());
    }

    /**
     * 微信登录实现
     * <p>
     * 暂未实现微信登录逻辑，仅作为占位符。
     * </p>
     *
     * @param code 微信 code
     * @return JWT 令牌
     * @throws RuntimeException 暂时抛出功能未启用异常
     */
    @Override
    public String wechatLogin(String code) {
        log.warn("尝试微信登录但功能未启用");
        throw new RuntimeException("由于兼容性问题，微信登录暂时禁用。");
    }
}
