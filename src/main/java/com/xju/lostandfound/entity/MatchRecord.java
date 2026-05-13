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

    private LocalDateTime createTime; // 创建时间

    // ==========================================
    // 下方为接入 Coze 大模型新增的 AI 解释与判定字段
    // ==========================================

    /**
     * AI 生成的综合匹配理由
     */
    private String matchReason;

    /**
     * 高度重合的属性字段 (例如: ["name","location"])
     */
    private String matchedFields;

    /**
     * 判定风险等级 (例如: low, medium, high)
     */
    private String riskLevel;
}