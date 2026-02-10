package com.soccer.forum.service.security.service.impl;

//import cn.binarywang.wx.miniapp.api.WxMaService;
//import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soccer.forum.domain.entity.User;
import com.soccer.forum.domain.enums.UserRole;
import com.soccer.forum.domain.enums.UserStatus;
import com.soccer.forum.service.mapper.UserMapper;
import com.soccer.forum.service.security.model.LoginBody;
import com.soccer.forum.service.security.model.LoginUser;
import com.soccer.forum.service.security.service.AuthService;
import com.soccer.forum.service.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    //private final WxMaService wxMaService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, 
                           JwtUtils jwtUtils, 
                           UserMapper userMapper, 
                           PasswordEncoder passwordEncoder 
                           /*WxMaService wxMaService*/) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        //this.wxMaService = wxMaService;
    }

    @Override
    public String login(LoginBody loginBody) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginBody.getUsername(), loginBody.getPassword())
        );

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return jwtUtils.generateToken(loginUser);
    }

    @Override
    public void register(LoginBody loginBody) {
        // 检查用户是否存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginBody.getUsername()));
        if (count > 0) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(loginBody.getUsername());
        user.setPassword(passwordEncoder.encode(loginBody.getPassword()));
        user.setNickname(loginBody.getNickname() != null ? loginBody.getNickname() : "User_" + System.currentTimeMillis());
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.NORMAL);
        
        userMapper.insert(user);
    }

    @Override
    public String wechatLogin(String code) {
        throw new RuntimeException("WeChat login is temporarily disabled due to compatibility issues.");
        /*
        try {
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            String openid = session.getOpenid();
            
            // 查找用户是否存在
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getOpenid, openid));
            
            if (user == null) {
                // 自动注册
                user = new User();
                user.setUsername("wx_" + openid.substring(0, 8));
                user.setOpenid(openid);
                user.setNickname("微信用户");
                user.setRole(UserRole.USER);
                user.setStatus(UserStatus.NORMAL);
                // 随机密码
                user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                userMapper.insert(user);
            }
            
            return jwtUtils.generateToken(new LoginUser(user));
        } catch (Exception e) {
            throw new RuntimeException("WeChat login failed: " + e.getMessage());
        }
        */
    }
}
