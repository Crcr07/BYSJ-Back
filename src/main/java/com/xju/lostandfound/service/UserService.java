package com.xju.lostandfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xju.lostandfound.entity.User;

public interface UserService extends IService<User> {

    // 🌟 注意这里：参数必须是两个 String，和实现类保持绝对一致！
    String login(String username, String password);

    boolean register(User user);
}