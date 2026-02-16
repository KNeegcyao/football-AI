package com.soccer.forum.service.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soccer.forum.common.exception.ServiceException;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
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
    private final StringRedisTemplate redisTemplate;

    public AuthServiceImpl(AuthenticationManager authenticationManager, 
                           JwtUtils jwtUtils, 
                           UserMapper userMapper, 
                           PasswordEncoder passwordEncoder,
                           StringRedisTemplate redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
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
        
        // 验证码校验 (如果是手机号注册)
        if (loginBody.getUsername().matches("^1[3-9]\\d{9}$")) {
            if (loginBody.getCode() == null || loginBody.getCode().isEmpty()) {
                throw new RuntimeException("请输入验证码");
            }
            String cacheKey = "AUTH:CODE:" + loginBody.getUsername();
            String code = redisTemplate.opsForValue().get(cacheKey);
            
            if (code == null) {
                throw new RuntimeException("验证码已过期或未发送");
            }
            if (!code.equals(loginBody.getCode())) {
                throw new RuntimeException("验证码错误");
            }
            // 验证通过，删除验证码
            redisTemplate.delete(cacheKey);
        }
        
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
        
        if (loginBody.getEmail() != null && !loginBody.getEmail().isEmpty()) {
            user.setEmail(loginBody.getEmail());
        }
        
        if (loginBody.getPhone() != null && !loginBody.getPhone().isEmpty()) {
            user.setPhone(loginBody.getPhone());
        } else if (loginBody.getUsername().matches("^1[3-9]\\d{9}$")) {
            user.setPhone(loginBody.getUsername());
        }
        
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

    /**
     * 重置密码实现
     *
     * @param loginBody 重置密码请求参数
     */
    @Override
    public void resetPassword(LoginBody loginBody) {
        try {
            log.debug("尝试重置密码: 用户名={}", loginBody.getUsername());
            
            // 验证码校验
            String cacheKey = "AUTH:CODE:" + loginBody.getUsername();
            String code = redisTemplate.opsForValue().get(cacheKey);
            
            if (code == null) {
                throw new ServiceException("验证码已过期或未发送");
            }
            if (!code.equals(loginBody.getCode())) {
                throw new ServiceException("验证码错误");
            }

            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, loginBody.getUsername()));
            
            if (user == null) {
                log.warn("重置密码失败: 用户不存在 {}", loginBody.getUsername());
                throw new ServiceException("用户不存在");
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(loginBody.getPassword()));
            userMapper.updateById(user);
            
            // 验证通过，删除验证码
            redisTemplate.delete(cacheKey);
            
            log.info("用户重置密码成功: id={}, username={}", user.getId(), user.getUsername());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("重置密码系统异常", e);
            throw new ServiceException("重置密码失败: " + e.getMessage());
        }
    }

    /**
     * 发送验证码实现
     *
     * @param phone 手机号
     * @return 生成的验证码
     */
    @Override
    public String sendCode(String phone) {
        // 生成6位随机验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        
        // 存入 Redis，有效期5分钟
        String key = "AUTH:CODE:" + phone;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        
        log.info("发送验证码: phone={}, code={}", phone, code);
        
        // TODO: 对接真实短信服务 (如阿里云、腾讯云)
        // 目前仅返回验证码用于测试
        return code;
    }

    /**
     * 验证码登录实现
     *
     * @param loginBody 登录请求参数
     * @return JWT 令牌
     */
    @Override
    public String loginByCode(LoginBody loginBody) {
        log.debug("尝试验证码登录: {}", loginBody.getPhone());
        
        String phone = loginBody.getPhone();
        String code = loginBody.getCode();
        
        if (phone == null || code == null) {
            throw new ServiceException("手机号和验证码不能为空");
        }
        
        // 验证码校验
        String cacheKey = "AUTH:CODE:" + phone;
        String cachedCode = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedCode == null) {
            throw new ServiceException("验证码已过期或未发送");
        }
        if (!cachedCode.equals(code)) {
            throw new ServiceException("验证码错误");
        }
        
        // 验证通过，删除验证码
        redisTemplate.delete(cacheKey);
        
        // 查找用户，如果不存在则自动注册
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .or()
                .eq(User::getUsername, phone));
                
        if (user == null) {
            log.info("用户不存在，自动注册: phone={}", phone);
            user = new User();
            user.setUsername(phone);
            user.setPhone(phone);
            // 设置随机密码
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString().substring(0, 8)));
            user.setNickname("User_" + phone.substring(phone.length() - 4));
            user.setRole(UserRole.USER);
            user.setStatus(UserStatus.NORMAL);
            userMapper.insert(user);
        } else {
            // 如果用户状态不正常
            if (user.getStatus() != UserStatus.NORMAL) {
                throw new ServiceException("账号已被禁用");
            }
        }
        
        // 生成 Token
        LoginUser loginUser = new LoginUser(user);
        return jwtUtils.generateToken(loginUser);
    }
}
