package com.xju.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("lf_match_record")
public class MatchRecord implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long lostId;        // 失物ID
    private Long foundId;       // 招领ID
    private Double matchScore;  // 匹配得分
    private Integer status;     // 状态(0待确认,1已确认,2否决)
    private LocalDateTime createTime;
}