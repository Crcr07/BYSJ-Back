package com.xju.lostandfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xju.lostandfound.entity.User;
import com.xju.lostandfound.model.dto.Logindto;

public interface UserService extends IService<User> {
    // 定义登录方法
    String login(Logindto loginDto);
}