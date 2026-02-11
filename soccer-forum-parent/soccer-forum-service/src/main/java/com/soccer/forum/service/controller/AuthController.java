package com.soccer.forum.service.controller;

import com.soccer.forum.service.security.model.LoginBody;
import com.soccer.forum.service.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证接口控制器
 * <p>
 * 提供用户登录、注册以及第三方（微信）登录的功能。
 * </p>
 *
 * @author Soccer Forum Dev Team
 * @version 1.0
 */
@Tag(name = "用户认证", description = "用户认证接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录接口
     * <p>
     * 接收用户名和密码，验证通过后返回 JWT 令牌。
     * </p>
     *
     * @param loginBody 包含用户名和密码的请求体
     * @return 包含 token 的响应结果
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回JWT令牌")
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginBody loginBody) {
        log.info("收到用户登录请求: 用户名={}", loginBody.getUsername());
        try {
            String token = authService.login(loginBody);
            log.info("用户登录成功: 用户名={}", loginBody.getUsername());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("code", 200);
            result.put("msg", "登录成功");
            return result;
        } catch (Exception e) {
            log.error("用户登录失败: 用户名={}", loginBody.getUsername(), e);
            throw e;
        }
    }

    /**
     * 用户注册接口
     * <p>
     * 注册新用户，用户名不能重复。
     * </p>
     *
     * @param loginBody 包含用户名、密码和昵称的请求体
     * @return 注册结果
     */
    @Operation(summary = "用户注册", description = "注册新用户")
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody LoginBody loginBody) {
        log.info("收到用户注册请求: 用户名={}", loginBody.getUsername());
        try {
            authService.register(loginBody);
            log.info("用户注册成功: 用户名={}", loginBody.getUsername());
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "注册成功");
            return result;
        } catch (Exception e) {
            log.error("用户注册失败: 用户名={}", loginBody.getUsername(), e);
            throw e;
        }
    }

    /**
     * 微信登录接口
     * <p>
     * 使用微信小程序返回的 code 进行登录（暂未完全启用）。
     * </p>
     *
     * @param body 包含 code 的 JSON 对象
     * @return 包含 token 的响应结果
     */
    @Operation(summary = "微信登录", description = "使用微信code登录 (暂未启用)")
    @PostMapping("/wechat")
    public Map<String, Object> wechatLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        log.info("收到微信登录请求: code={}", code);
        try {
            String token = authService.wechatLogin(code);
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("code", 200);
            return result;
        } catch (Exception e) {
            log.error("微信登录失败", e);
            throw e;
        }
    }
}
