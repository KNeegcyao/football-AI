package com.soccer.forum.service.security.service;

import com.soccer.forum.service.security.model.LoginBody;

/**
 * 认证服务接口
 * <p>
 * 定义用户登录、注册及第三方登录的核心业务逻辑契约。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginBody 登录请求参数（用户名、密码）
     * @return 登录成功后生成的 JWT 令牌
     */
    String login(LoginBody loginBody);

    /**
     * 用户注册
     *
     * @param loginBody 注册请求参数（用户名、密码、昵称等）
     */
    void register(LoginBody loginBody);

    /**
     * 微信登录
     *
     * @param code 微信小程序端获取的临时登录凭证 code
     * @return 登录成功后生成的 JWT 令牌
     */
    String wechatLogin(String code);

    /**
     * 重置密码
     *
     * @param loginBody 重置密码请求参数（用户名、新密码）
     */
    void resetPassword(LoginBody loginBody);

    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return 生成的验证码
     */
    String sendCode(String phone);

    /**
     * 验证码登录
     *
     * @param loginBody 登录请求参数（手机号、验证码）
     * @return 登录成功后生成的 JWT 令牌
     */
    String loginByCode(LoginBody loginBody);
}
