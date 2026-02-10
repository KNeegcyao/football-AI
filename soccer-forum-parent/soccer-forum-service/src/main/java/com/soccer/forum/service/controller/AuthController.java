package com.soccer.forum.service.controller;

import com.soccer.forum.service.security.model.LoginBody;
import com.soccer.forum.service.security.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginBody loginBody) {
        String token = authService.login(loginBody);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody LoginBody loginBody) {
        authService.register(loginBody);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "Registration successful");
        return result;
    }

    @PostMapping("/wechat")
    public Map<String, Object> wechatLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String token = authService.wechatLogin(code);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        return result;
    }
}
