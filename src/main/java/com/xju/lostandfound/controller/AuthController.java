package com.xju.lostandfound.controller;

import com.xju.lostandfound.common.result.Result;
import com.xju.lostandfound.model.dto.Logindto;
import com.xju.lostandfound.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 登录接口
     * POST /auth/login
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody Logindto logindto) {
        try {
            String token = userService.login(logindto);
            // 登录成功，返回 Token
            return Result.success(token);
        } catch (Exception e) {
            // 登录失败，返回错误信息
            return Result.error(e.getMessage());
        }
    }
}