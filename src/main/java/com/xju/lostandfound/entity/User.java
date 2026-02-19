package com.xju.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_user") // 对应数据库表名
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO) // 主键自增
    private Long id;

    private String username; // 学号

    private String password; // 密码

    private String nickname; // 昵称

    private String realName; // 真实姓名

    private String avatar;   // 头像

    private String mobile;   // 手机号

    private Integer creditScore; // 信用积分

    private Integer role;    // 角色 0:管理员 1:用户

    private Integer status;  // 状态

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}