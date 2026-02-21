package com.xju.lostandfound.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xju.lostandfound.common.utils.JwtUtils;
import com.xju.lostandfound.entity.User;
import com.xju.lostandfound.mapper.UserMapper;
import com.xju.lostandfound.service.UserService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public String login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username).eq("password", password);
        User user = this.getOne(wrapper);

        if (user != null) {
            // 检查账号是否被封禁 (status == 0)
            if (user.getStatus() != null && user.getStatus() == 0) {
                throw new RuntimeException("该账号已被禁用，请联系管理员");
            }
            // 登录成功，生成并返回 Token (把用户真实ID存进Token)
            return JwtUtils.generateToken(user.getId().toString());
        }
        return null; // 账号或密码错误
    }

    @Override
    public boolean register(User user) {
        // 1. 检查学号是否被注册过
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (this.count(wrapper) > 0) {
            return false; // 学号已存在
        }

        // 2. 赋予新用户初始属性
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setCreditScore(100); // 默认 100 信用分
        user.setRole(1);          // 默认 1 (普通用户)
        user.setStatus(1);        // 默认 1 (账号正常)

        // 如果用户没填昵称，给他一个默认昵称
        if(user.getNickname() == null || user.getNickname().isEmpty()){
            user.setNickname("校园用户_" + user.getUsername());
        }

        // 3. 保存到数据库
        return this.save(user);
    }
}