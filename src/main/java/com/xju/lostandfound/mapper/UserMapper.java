package com.xju.lostandfound.mapper;

import com.xju.lostandfound.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper 已经内置了 insert, delete, update, selectById, selectList 等方法
    // 除非有复杂的自定义SQL，否则这里什么都不用写
}