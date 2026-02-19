package com.xju.lostandfound.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xju.lostandfound.common.utils.JwtUtils;
import com.xju.lostandfound.entity.User;
import com.xju.lostandfound.mapper.UserMapper;
import com.xju.lostandfound.model.dto.Logindto;
import com.xju.lostandfound.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public String login(Logindto loginDto) {
        // 1. 根据用户名查询数据库
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", loginDto.getUsername());
        User user = baseMapper.selectOne(wrapper);

        // 2. 判断用户是否存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 判断密码是否正确 (这里演示用的明文比对，建议后续改为加密比对)
        if (!user.getPassword().equals(loginDto.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 4. 生成 Token 并返回
        return JwtUtils.generateToken(user.getUsername());
    }
}