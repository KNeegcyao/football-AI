package com.soccer.forum.service.security.service;

import com.soccer.forum.service.security.model.LoginBody;

public interface AuthService {
    String login(LoginBody loginBody);
    void register(LoginBody loginBody);
    String wechatLogin(String code);
}
