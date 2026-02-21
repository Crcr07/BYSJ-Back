package com.xju.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_credit_record")
public class CreditRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;         // 所属用户ID

    private Integer changeAmount; // 变动分数 (正负数)

    private String reason;       // 变动原因

    private LocalDateTime createTime; // 记录时间
}